package org.dev.healthbuddy.ui

import org.dev.healthbuddy.data.AppState
import org.dev.healthbuddy.domain.Barrier
import org.dev.healthbuddy.domain.BarrierCatalog
import org.dev.healthbuddy.domain.DoseStatus
import org.dev.healthbuddy.ui.i18n.Strings
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Plain-text summary of the last 30 days, handed to the Android share sheet.
 *
 * Same rule as every other screen: counts and lists only. No score, no streak,
 * no interpretation, and no advice — the clinician reads the pattern, the app
 * never draws a conclusion from it (PRD §9).
 */
object VisitSummary {

    private const val WINDOW_DAYS = 30L
    private val DATE: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())

    fun build(state: AppState, s: Strings): String {
        val today = LocalDate.now()
        val from = today.minusDays(WINDOW_DAYS - 1)
        val start = from.toString()
        val end = today.toString()

        val inWindow = state.logs.filter { it.date in start..end }

        val taken = inWindow.count { it.status == DoseStatus.TAKEN }
        val missed = inWindow.count { it.status == DoseStatus.MISSED }
        val unsure = inWindow.count { it.status == DoseStatus.UNSURE }

        val barriers: List<Pair<Barrier, Int>> = inWindow
            .mapNotNull { it.barrier }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedWith(
                compareByDescending<Pair<Barrier, Int>> { it.second }
                    .thenBy { it.first.ordinal }
            )

        val period = "${from.format(DATE)} – ${today.format(DATE)}"

        return buildString {
            appendLine(s.summaryTitle)
            if (state.name.isNotBlank() && !state.privateMode) {
                appendLine("${state.name} · $period")
            } else {
                // Private mode: the person's name never leaves the phone unless
                // they typed it into the share sheet themselves.
                appendLine(period)
            }
            appendLine()
            appendLine("${s.summaryDoses}: ${inWindow.size}")
            appendLine("  ${s.taken}: $taken")
            appendLine("  ${s.missed}: $missed")
            appendLine("  ${s.notSure}: $unsure")

            if (barriers.isNotEmpty()) {
                appendLine()
                appendLine("${s.summaryBarriers}:")
                barriers.forEach { (barrier, count) ->
                    appendLine("  • ${BarrierCatalog.of(barrier).text(state.lang).label} — $count")
                }
            }

            if (state.medicines.isNotEmpty()) {
                appendLine()
                appendLine("${s.summaryMedicines}:")
                state.medicines.forEach { med ->
                    val times = med.times.joinToString(", ") { Fmt.time(it) }
                    appendLine("  • ${med.name} ${med.dose} — $times")
                }
            }

            appendLine()
            append(s.summaryFooter)
        }
    }
}
