package org.dev.healthbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.History
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pill
import com.composables.icons.lucide.Trash2
import org.dev.healthbuddy.data.AppState
import org.dev.healthbuddy.domain.Barrier
import org.dev.healthbuddy.domain.BarrierCatalog
import org.dev.healthbuddy.domain.Lang
import org.dev.healthbuddy.domain.Medicine
import org.dev.healthbuddy.ui.Fmt
import org.dev.healthbuddy.ui.components.EmptyState
import org.dev.healthbuddy.ui.components.HbCard
import org.dev.healthbuddy.ui.i18n.LocalStrings

@Composable
fun MedicinesScreen(state: AppState) {
    val s = LocalStrings.current

    if (state.medicines.isEmpty()) {
        EmptyState(
            icon = Lucide.Pill,
            title = s.noMedicines,
            body = s.noMedicinesBody,
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background, shape = MaterialTheme.shapes.medium)
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background, shape = MaterialTheme.shapes.medium),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(state.medicines, key = { it.id }) { med ->
            MedicineRow(
                med = med,
                timesText = med.times.joinToString(" · ") { Fmt.time(it) },
                onToggle = { state.toggleMedicine(med.id) },
                onDelete = { state.removeMedicine(med.id) }
            )
        }
    }
}

@Composable
private fun MedicineRow(
    med: Medicine,
    timesText: String,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val s = LocalStrings.current
    HbCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = med.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${med.dose} · ${med.condition}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = timesText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Switch(checked = med.active, onCheckedChange = { onToggle() })
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Lucide.Trash2,
                    contentDescription = s.remove,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun HistoryScreen(
    state: AppState,
    onOpenFlow: (org.dev.healthbuddy.domain.DoseSlot) -> Unit
) {
    val s = LocalStrings.current
    val grouped = state.logsDesc().groupBy { it.date }
    val weekly = state.barrierCountsThisWeek()

    if (grouped.isEmpty()) {
        EmptyState(
            icon = Lucide.History,
            title = s.noHistory,
            body = s.noHistoryBody,
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background, shape = MaterialTheme.shapes.medium)
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background, shape = MaterialTheme.shapes.medium),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (weekly.isNotEmpty()) {
            item(key = "weekly-barriers") {
                WeeklyBarrierCard(weekly, state.lang)
            }
        }

        grouped.forEach { (date, entries) ->
            item(key = "h-$date") {
                Text(
                    text = Fmt.dayLabel(date, s),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(entries, key = { "log-${it.id}" }) { log ->
                val needsReason = log.status == org.dev.healthbuddy.domain.DoseStatus.MISSED
                    && log.barrier == null
                HbCard(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    onClick = if (needsReason) {
                        { state.slotFor(log)?.let(onOpenFlow) }
                    } else null
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = state.medicineName(log.medicineId),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = Fmt.time(log.time),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = org.dev.healthbuddy.ui.components.statusLabel(log.status, s),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (log.barrier != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${s.barrierLabel}: " +
                                org.dev.healthbuddy.domain.BarrierCatalog
                                    .of(log.barrier).text(state.lang).label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (log.helpful != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${s.helpfulLabel}: " + when (log.helpful) {
                                org.dev.healthbuddy.domain.Helpful.YES -> s.yes
                                org.dev.healthbuddy.domain.Helpful.NOT_SURE -> s.somewhat
                                org.dev.healthbuddy.domain.Helpful.NO -> s.no
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (needsReason) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = s.tapToExplain,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    }
}

/**
 * The research question made visible: counts of what got in the way, nothing else.
 * No score, no streak, no advice — the pattern is the user's to read.
 */
@Composable
private fun WeeklyBarrierCard(counts: List<Pair<Barrier, Int>>, lang: Lang) {
    val s = LocalStrings.current
    val max = counts.maxOf { it.second }

    HbCard(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Text(text = s.weeklyTitle, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(12.dp))
        counts.forEach { (barrier, count) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = BarrierCatalog.of(barrier).text(lang).label,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(5.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(count.toFloat() / max)
                            .height(5.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(10.dp))
        }
        Text(
            text = s.weeklyCaption,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
