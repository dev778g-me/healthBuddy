package org.dev.healthbuddy.domain

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** Demo seed data. Everything here is fictional. */
object Seed {

    private val HH_MM = DateTimeFormatter.ofPattern("HH:mm")

    fun today(): String = LocalDate.now().toString()
    fun yesterday(): String = LocalDate.now().minusDays(1).toString()
    fun daysFromNow(days: Long): String = LocalDate.now().plusDays(days).toString()

    fun medicines(): List<Medicine> = listOf(
        Medicine(
            id = "med-metformin",
            name = "Metformin",
            dose = "500 mg",
            condition = "Type 2 diabetes",
            times = listOf("08:00", "20:00")
        ),
        Medicine(
            id = "med-amlodipine",
            name = "Amlodipine",
            dose = "5 mg",
            condition = "Hypertension",
            times = listOf("09:00")
        ),
        Medicine(
            id = "med-sertraline",
            name = "Sertraline",
            dose = "50 mg",
            condition = "Mental health",
            times = listOf("21:00")
        )
    )

    fun appointments(): List<Appointment> = listOf(
        Appointment(
            id = "appt-phc",
            title = "Follow-up visit",
            doctor = "Dr. Rao · PHC",
            date = daysFromNow(3),
            time = "10:30"
        ),
        Appointment(
            id = "appt-diabetes",
            title = "Diabetes review",
            doctor = "Dr. Nair · Diabetes clinic",
            date = daysFromNow(12),
            time = "16:00"
        )
    )

    /**
     * Yesterday's evening Metformin, missed and never explained.
     * This is what makes the demo start immediately instead of waiting
     * for a dose to fall due.
     */
    fun demoLogs(): List<DoseLog> = listOf(
        DoseLog(
            id = doseId("med-metformin", yesterday(), "20:00"),
            medicineId = "med-metformin",
            date = yesterday(),
            time = "20:00",
            status = DoseStatus.MISSED,
            barrier = null,
            helpful = null,
            loggedAt = System.currentTimeMillis() - 20L * 60L * 60L * 1000L
        )
    )

    /** Tips rotate by day-of-year so the dashboard changes daily but is stable. */
    private val tipsEn = listOf(
        "Take your medicine at the same time as something you already do — tea, brushing your teeth.",
        "Keep one strip of your medicine where you'll see it, not in a cupboard.",
        "If you feel unwell after a dose, write it down instead of skipping silently.",
        "Carry a small note of your medicines to every appointment.",
        "Ask your pharmacist to write the timing in your own language.",
        "A missed dose is information, not a failure. Note it and carry on."
    )

    private val tipsHi = listOf(
        "दवा उसी समय लें जब आप रोज़ कुछ और करते हैं — चाय, ब्रश।",
        "दवा की एक पत्ती वहाँ रखें जहाँ आपकी नज़र पड़े, अलमारी में नहीं।",
        "खुराक के बाद अस्वस्थ लगे तो चुपचाप छोड़ने के बजाय लिख लें।",
        "हर मुलाक़ात पर अपनी दवाओं की छोटी सूची साथ रखें।",
        "फार्मासिस्ट से समय अपनी भाषा में लिखने के लिए कहें।",
        "छूटी खुराक जानकारी है, असफलता नहीं। दर्ज करें और आगे बढ़ें।"
    )

    fun tip(lang: Lang): String {
        val list = if (lang == Lang.HI) tipsHi else tipsEn
        val index = LocalDate.now().dayOfYear % list.size
        return list[index]
    }

    fun nowHHmm(): String = LocalTime.now().format(HH_MM)
}
