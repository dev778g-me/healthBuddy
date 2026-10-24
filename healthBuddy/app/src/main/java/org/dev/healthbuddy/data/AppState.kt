package org.dev.healthbuddy.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.dev.healthbuddy.domain.Appointment
import org.dev.healthbuddy.domain.Barrier
import org.dev.healthbuddy.domain.DoseLog
import org.dev.healthbuddy.domain.DoseSlot
import org.dev.healthbuddy.domain.DoseStatus
import org.dev.healthbuddy.domain.Helpful
import org.dev.healthbuddy.domain.Lang
import org.dev.healthbuddy.domain.Medicine
import org.dev.healthbuddy.domain.Seed
import org.dev.healthbuddy.domain.Tab
import org.dev.healthbuddy.domain.ThemeMode
import org.dev.healthbuddy.domain.doseId
import org.dev.healthbuddy.notifications.ReminderScheduler
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AppState(
    private val store: Store,
    private val reminders: ReminderScheduler
) {

    private var snapshot: Snapshot = store.load()

    var onboarded by mutableStateOf(snapshot.onboarded)
        private set
    var name by mutableStateOf(snapshot.name)
        private set
    var lang by mutableStateOf(snapshot.lang)
        private set
    var themeMode by mutableStateOf(snapshot.themeMode)
        private set
    var privateMode by mutableStateOf(snapshot.privateMode)
        private set
    var remindersEnabled by mutableStateOf(snapshot.remindersEnabled)
        private set
    var medicines by mutableStateOf(snapshot.medicines)
        private set
    var logs by mutableStateOf(snapshot.logs)
        private set
    var appointments by mutableStateOf(snapshot.appointments)
        private set

    // ---- navigation (routes owned by Navigation Compose) ------------------
    var tab by mutableStateOf(Tab.TODAY)
        private set
    var selectedEduId by mutableStateOf<String?>(null)

    fun selectTab(next: Tab) {
        if (tab != next) tab = next
    }

    /**
     * Tapping a reminder should land on Today even if Settings (or a Learn
     * detail) was the last screen. MainShell consumes this and pops.
     */
    var pendingOpenToday by mutableStateOf(false)
        private set

    fun openTodayFromNotification() {
        tab = Tab.TODAY
        pendingOpenToday = true
    }

    fun consumeOpenToday() {
        pendingOpenToday = false
    }

    private fun resetTab() {
        tab = Tab.TODAY
    }

    // ---- onboarding / settings -------------------------------------------

    fun completeOnboarding(displayName: String, chosen: Lang, remindersWanted: Boolean = true) {
        name = displayName.trim()
        lang = chosen
        remindersEnabled = remindersWanted
        onboarded = true
        persist()
    }

    fun changeLanguage(chosen: Lang) {
        lang = chosen
        persist()
    }

    fun changeThemeMode(mode: ThemeMode) {
        themeMode = mode
        persist()
    }

    fun changePrivateMode(enabled: Boolean) {
        privateMode = enabled
        persist()
    }

    /** Settings toggle. Turning it back on after a permission grant re-arms alarms. */
    fun changeRemindersEnabled(enabled: Boolean) {
        if (remindersEnabled == enabled) {
            syncReminders()
            return
        }
        remindersEnabled = enabled
        if (!enabled) reminders.cancelAllNotifications()
        persist()
    }

    /** Re-check permissions and rebuild the local alarm set. */
    fun syncReminders() {
        reminders.sync(medicines, onboarded && remindersEnabled)
    }

    // ---- medicines --------------------------------------------------------

    fun addMedicine(name: String, dose: String, condition: String, times: List<String>) {
        val clean = times.filter { it.isNotBlank() }.sorted()
        if (name.isBlank() || clean.isEmpty()) return
        val med = Medicine(
            id = "med-" + System.currentTimeMillis().toString(36),
            name = name.trim(),
            dose = dose.ifBlank { "—" },
            condition = condition.ifBlank { "General" },
            times = clean
        )
        medicines = medicines + med
        persist()
    }

    fun updateMedicine(
        id: String,
        name: String,
        dose: String,
        condition: String,
        times: List<String>
    ) {
        val clean = times.filter { it.isNotBlank() }.sorted()
        if (name.isBlank() || clean.isEmpty()) return
        medicines = medicines.map {
            if (it.id != id) it else it.copy(
                name = name.trim(),
                dose = dose.ifBlank { "—" },
                condition = condition.ifBlank { "General" },
                times = clean
            )
        }
        persist()
    }

    fun removeMedicine(id: String) {
        medicines = medicines.filterNot { it.id == id }
        logs = logs.filterNot { it.medicineId == id }
        persist()
    }

    fun toggleMedicine(id: String) {
        medicines = medicines.map { if (it.id == id) it.copy(active = !it.active) else it }
        persist()
    }

    /** "Forgot" support: push today's reminder later by N minutes. */
    fun adjustReminder(id: String, minutes: Int) {
        medicines = medicines.map {
            if (it.id == id) it.copy(reminderOffsetMinutes = it.reminderOffsetMinutes + minutes) else it
        }
        persist()
    }

    // ---- dose logs --------------------------------------------------------

    fun logDose(slot: DoseSlot, status: DoseStatus) {
        // Re-logging a dose resets the barrier/follow-up, but never silently
        // throws away a note the user typed.
        val priorNote = logs.firstOrNull { it.id == slot.id }?.note
        val entry = DoseLog(
            id = slot.id,
            medicineId = slot.medicine.id,
            date = slot.date,
            time = slot.baseTime,
            status = status,
            barrier = null,
            helpful = null,
            note = priorNote,
            loggedAt = System.currentTimeMillis()
        )
        logs = logs.filterNot { it.id == entry.id } + entry
        // Logging from the app dismisses today's reminder for this dose.
        if (slot.date == today()) {
            reminders.cancelNotification(slot.medicine.id, slot.baseTime)
        }
        persist()
    }

    fun setBarrier(logId: String, barrier: Barrier) {
        logs = logs.map { if (it.id == logId) it.copy(barrier = barrier) else it }
        persist()
    }

    fun setHelpful(logId: String, value: Helpful) {
        logs = logs.map { if (it.id == logId) it.copy(helpful = value) else it }
        persist()
    }

    /** Free-text note on a logged dose. Blank clears the note. */
    fun setNote(logId: String, note: String) {
        val clean = note.trim().take(140)
        logs = logs.map {
            if (it.id == logId) it.copy(note = clean.ifBlank { null }) else it
        }
        persist()
    }

    // ---- add / edit-medicine sheet (UI-only, never persisted) --------------

    var addMedicineSheet by mutableStateOf(false)
        private set

    /** Non-null while the sheet is editing an existing medicine. */
    var editingMedicine by mutableStateOf<Medicine?>(null)
        private set

    fun openAddMedicineSheet() {
        editingMedicine = null
        addMedicineSheet = true
    }

    fun openEditMedicine(medicine: Medicine) {
        editingMedicine = medicine
        addMedicineSheet = true
    }

    fun closeAddMedicineSheet() {
        addMedicineSheet = false
        editingMedicine = null
    }

    // ---- derived views ----------------------------------------------------

    fun todaySlots(): List<DoseSlot> = slotsFor(today())

    fun slotsFor(date: String): List<DoseSlot> {
        val result = mutableListOf<DoseSlot>()
        medicines.filter { it.active }.forEach { med ->
            med.times.forEach { base ->
                val shown = effectiveTime(med, base)
                val id = doseId(med.id, date, base)
                result += DoseSlot(
                    medicine = med,
                    date = date,
                    time = shown,
                    baseTime = base,
                    log = logs.firstOrNull { it.id == id }
                )
            }
        }
        return result.sortedBy { it.time }
    }

    /** Missed doses from the last week that were never explained. */
    fun attentionSlots(): List<DoseSlot> {
        val since = LocalDate.now().minusDays(7).toString()
        return logs
            .filter { it.status == DoseStatus.MISSED && it.barrier == null && it.date >= since }
            .sortedByDescending { it.date + it.time }
            .mapNotNull { slotFor(it) }
    }

    fun slotFor(log: DoseLog): DoseSlot? {
        val med = medicines.firstOrNull { it.id == log.medicineId } ?: return null
        val shown = effectiveTime(med, log.time)
        return DoseSlot(
            medicine = med,
            date = log.date,
            time = shown,
            baseTime = log.time,
            log = log
        )
    }

    fun logsDesc(): List<DoseLog> = logs.sortedByDescending { it.date + it.time }

    /** Barriers logged in the last 7 days, most frequent first. Counts only — no advice. */
    fun barrierCountsThisWeek(): List<Pair<Barrier, Int>> {
        val since = LocalDate.now().minusDays(6).toString()
        return logs
            .filter { it.barrier != null && it.date >= since }
            .groupingBy { it.barrier!! }
            .eachCount()
            .toList()
            .sortedWith(
                compareByDescending<Pair<Barrier, Int>> { it.second }
                    .thenBy { it.first.ordinal }
            )
    }

    fun medicineName(id: String): String =
        medicines.firstOrNull { it.id == id }?.name ?: "Medicine"

    fun nextAppointment(): Appointment? =
        appointments
            .filter { it.date >= today() }
            .sortedBy { it.date + it.time }
            .firstOrNull()

    fun tip(): String = Seed.tip(lang)

    // ---- demo / reset -----------------------------------------------------

    /** One-shot signal consumed by MainShell: open the missed-dose flow after a reset. */
    var pendingDemoFlow by mutableStateOf(false)
        private set

    fun consumeDemoFlow() {
        pendingDemoFlow = false
    }

    /** Deterministic reset to Ravi's story — used on stage. */
    fun loadDemo() {
        name = "Ravi"
        lang = Lang.EN
        privateMode = false
        medicines = Seed.medicines()
        appointments = Seed.appointments()
        logs = Seed.demoLogs()
        addMedicineSheet = false
        editingMedicine = null
        onboarded = true
        resetTab()
        pendingDemoFlow = true
        persist()
    }

    fun resetAll() {
        store.clear()
        val fresh = Snapshot(
            onboarded = false,
            name = "",
            lang = Lang.EN,
            themeMode = ThemeMode.SYSTEM,
            privateMode = false,
            medicines = Seed.medicines(),
            logs = emptyList(),
            appointments = Seed.appointments()
        )
        snapshot = fresh
        onboarded = false
        name = ""
        lang = Lang.EN
        themeMode = ThemeMode.SYSTEM
        privateMode = false
        remindersEnabled = fresh.remindersEnabled
        medicines = fresh.medicines
        logs = fresh.logs
        appointments = fresh.appointments
        addMedicineSheet = false
        editingMedicine = null
        resetTab()
        store.save(fresh)
        reminders.cancelAll()
        reminders.cancelAllNotifications()
    }

    // ---- internals --------------------------------------------------------

    private fun effectiveTime(med: Medicine, base: String): String =
        runCatching {
            LocalTime.parse(base).plusMinutes(med.reminderOffsetMinutes.toLong())
                .format(HH_MM)
        }.getOrDefault(base)

    private fun persist() {
        val s = Snapshot(
            onboarded = onboarded,
            name = name,
            lang = lang,
            themeMode = themeMode,
            privateMode = privateMode,
            medicines = medicines,
            logs = logs,
            appointments = appointments,
            remindersEnabled = remindersEnabled
        )
        snapshot = s
        store.save(s)
        // One place to keep the alarm set honest: every mutation already calls
        // persist(), so no edit path can forget to reschedule.
        reminders.sync(medicines, onboarded && remindersEnabled)
    }

    private fun today(): String = LocalDate.now().toString()

    private companion object {
        val HH_MM: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
