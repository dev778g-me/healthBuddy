package org.dev.healthbuddy.ui

import org.dev.healthbuddy.domain.Seed
import org.dev.healthbuddy.ui.i18n.Strings
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object Fmt {

    private val TWELVE_HOUR: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    private val DAY_MONTH: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM")
    private val FULL: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM")

    /** "20:00" -> "8:00 PM" */
    fun time(hhmm: String): String =
        runCatching { LocalTime.parse(hhmm).format(TWELVE_HOUR) }.getOrDefault(hhmm)

    /** "2026-02-14" -> "Today" / "Yesterday" / "14 Feb" */
    fun dayLabel(date: String, s: Strings): String {
        if (date == Seed.today()) return s.today
        if (date == Seed.yesterday()) return s.yesterday
        return runCatching { LocalDate.parse(date).format(DAY_MONTH) }.getOrDefault(date)
    }

    fun fullDate(date: String): String =
        runCatching { LocalDate.parse(date).format(FULL) }.getOrDefault(date)

    fun todayHeader(): String = LocalDate.now().format(FULL)
}
