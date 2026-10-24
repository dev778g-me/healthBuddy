package org.dev.healthbuddy.domain

/** The nine reasons a dose or a visit can be missed. See PRD §10.2. */
enum class Barrier {
    FORGOT,
    COST,
    SIDE_EFFECTS,
    CONFUSED,
    STIGMA,
    ACCESS,
    DISTRESS,
    OTHER,
    PREFER_NOT_TO_SAY
}

enum class DoseStatus { PENDING, TAKEN, MISSED, UNSURE }

enum class Helpful { YES, NOT_SURE, NO }

enum class Lang { EN, HI, MR }

/** System-following by default; the override exists because demo phones lie. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class Medicine(
    val id: String,
    val name: String,
    val dose: String,
    val condition: String,
    val times: List<String>,              // "HH:mm", sorted
    val active: Boolean = true,
    /** Reminder shifted later by this many minutes (FR-4 "forgot" support). */
    val reminderOffsetMinutes: Int = 0
)

data class DoseLog(
    val id: String,                       // "$medicineId|$date|$time"
    val medicineId: String,
    val date: String,                     // "yyyy-MM-dd"
    val time: String,                     // scheduled "HH:mm"
    val status: DoseStatus,
    val barrier: Barrier? = null,
    val helpful: Helpful? = null,
    val loggedAt: Long = 0L
)

data class Appointment(
    val id: String,
    val title: String,
    val doctor: String,
    val date: String,                     // "yyyy-MM-dd"
    val time: String                      // "HH:mm"
)

/** A medicine at a specific date/time, merged with whatever the user logged. */
data class DoseSlot(
    val medicine: Medicine,
    val date: String,
    /** Time shown to the user (scheduled time + any reminder offset). */
    val time: String,
    /** The scheduled time as stored — this is what identifies the dose. */
    val baseTime: String,
    val log: DoseLog?
) {
    val id: String get() = doseId(medicine.id, date, baseTime)
    val status: DoseStatus get() = log?.status ?: DoseStatus.PENDING
}

fun doseId(medicineId: String, date: String, time: String) = "$medicineId|$date|$time"

// ---- Navigation -----------------------------------------------------------

enum class Tab { TODAY, MEDICINES, HISTORY, LEARN }

