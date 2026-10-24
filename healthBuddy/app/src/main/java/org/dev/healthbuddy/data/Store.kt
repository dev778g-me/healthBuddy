package org.dev.healthbuddy.data

import android.content.Context
import org.dev.healthbuddy.domain.Appointment
import org.dev.healthbuddy.domain.DoseLog
import org.dev.healthbuddy.domain.DoseStatus
import org.dev.healthbuddy.domain.Helpful
import org.dev.healthbuddy.domain.Lang
import org.dev.healthbuddy.domain.Medicine
import org.dev.healthbuddy.domain.Barrier
import org.dev.healthbuddy.domain.Seed
import org.dev.healthbuddy.domain.ThemeMode
import org.json.JSONArray
import org.json.JSONObject

data class Snapshot(
    val onboarded: Boolean,
    val name: String,
    val lang: Lang,
    val themeMode: ThemeMode,
    val privateMode: Boolean,
    val medicines: List<Medicine>,
    val logs: List<DoseLog>,
    val appointments: List<Appointment>,
    /** Local dose reminders are on unless the user opts out. */
    val remindersEnabled: Boolean = true
)

/**
 * Single JSON blob in SharedPreferences. No database, no network, no DI.
 * The whole app state is small enough that this is the fastest safe option.
 */
class Store(context: Context) {

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun load(): Snapshot {
        val raw = prefs.getString(KEY_DATA, null) ?: return defaults()
        return runCatching { decode(JSONObject(raw)) }.getOrElse { defaults() }
    }

    fun save(s: Snapshot) {
        runCatching { prefs.edit().putString(KEY_DATA, encode(s).toString()).apply() }
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private fun defaults() = Snapshot(
        onboarded = false,
        name = "",
        lang = Lang.EN,
        themeMode = ThemeMode.SYSTEM,
        privateMode = false,
        medicines = Seed.medicines(),
        logs = Seed.demoLogs(),
        appointments = Seed.appointments()
    )

    // ---- encoding ---------------------------------------------------------

    private fun encode(s: Snapshot): JSONObject = JSONObject().apply {
        put("onboarded", s.onboarded)
        put("name", s.name)
        put("lang", s.lang.name)
        put("themeMode", s.themeMode.name)
        put("privateMode", s.privateMode)
        put("remindersEnabled", s.remindersEnabled)

        put("medicines", JSONArray().apply {
            s.medicines.forEach { m ->
                put(JSONObject().apply {
                    put("id", m.id)
                    put("name", m.name)
                    put("dose", m.dose)
                    put("condition", m.condition)
                    put("times", JSONArray(m.times))
                    put("active", m.active)
                    put("offset", m.reminderOffsetMinutes)
                })
            }
        })

        put("logs", JSONArray().apply {
            s.logs.forEach { l ->
                put(JSONObject().apply {
                    put("id", l.id)
                    put("medicineId", l.medicineId)
                    put("date", l.date)
                    put("time", l.time)
                    put("status", l.status.name)
                    put("barrier", l.barrier?.name ?: JSONObject.NULL)
                    put("helpful", l.helpful?.name ?: JSONObject.NULL)
                    put("note", l.note ?: JSONObject.NULL)
                    put("loggedAt", l.loggedAt)
                })
            }
        })

        put("appointments", JSONArray().apply {
            s.appointments.forEach { a ->
                put(JSONObject().apply {
                    put("id", a.id)
                    put("title", a.title)
                    put("doctor", a.doctor)
                    put("date", a.date)
                    put("time", a.time)
                })
            }
        })
    }

    // ---- decoding ---------------------------------------------------------

    private fun decode(o: JSONObject) = Snapshot(
        onboarded = o.optBoolean("onboarded", false),
        name = o.optString("name", ""),
        lang = runCatching { Lang.valueOf(o.optString("lang", "EN")) }.getOrDefault(Lang.EN),
        themeMode = runCatching { ThemeMode.valueOf(o.optString("themeMode", "SYSTEM")) }
            .getOrDefault(ThemeMode.SYSTEM),
        privateMode = o.optBoolean("privateMode", false),
        remindersEnabled = o.optBoolean("remindersEnabled", true),
        medicines = o.optJSONArray("medicines").mapObjects { m ->
            Medicine(
                id = m.optString("id"),
                name = m.optString("name"),
                dose = m.optString("dose"),
                condition = m.optString("condition"),
                times = m.optJSONArray("times").mapStrings().sorted(),
                active = m.optBoolean("active", true),
                reminderOffsetMinutes = m.optInt("offset", 0)
            )
        },
        logs = o.optJSONArray("logs").mapObjects { l ->
            DoseLog(
                id = l.optString("id"),
                medicineId = l.optString("medicineId"),
                date = l.optString("date"),
                time = l.optString("time"),
                status = runCatching { DoseStatus.valueOf(l.optString("status", "PENDING")) }
                    .getOrDefault(DoseStatus.PENDING),
                barrier = l.optString("barrier").takeIf { it.isNotBlank() && it != "null" }
                    ?.let { b -> runCatching { Barrier.valueOf(b) }.getOrNull() },
                helpful = l.optString("helpful").takeIf { it.isNotBlank() && it != "null" }
                    ?.let { h -> runCatching { Helpful.valueOf(h) }.getOrNull() },
                note = l.optString("note").takeIf { it.isNotBlank() && it != "null" },
                loggedAt = l.optLong("loggedAt", 0L)
            )
        },
        appointments = o.optJSONArray("appointments").mapObjects { a ->
            Appointment(
                id = a.optString("id"),
                title = a.optString("title"),
                doctor = a.optString("doctor"),
                date = a.optString("date"),
                time = a.optString("time")
            )
        }
    )

    private companion object {
        const val PREF_NAME = "healthbuddy"
        const val KEY_DATA = "app_data_v1"
    }
}

private fun JSONArray?.mapStrings(): List<String> {
    if (this == null) return emptyList()
    return (0 until length()).map { optString(it) }.filter { it.isNotBlank() }
}

private fun <T> JSONArray?.mapObjects(block: (JSONObject) -> T): List<T> {
    if (this == null) return emptyList()
    return (0 until length()).mapNotNull { optJSONObject(it)?.let(block) }
}
