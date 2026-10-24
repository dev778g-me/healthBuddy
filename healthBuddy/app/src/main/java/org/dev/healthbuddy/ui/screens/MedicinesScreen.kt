package org.dev.healthbuddy.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.History
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pill
import com.composables.icons.lucide.Share2
import com.composables.icons.lucide.Trash2
import org.dev.healthbuddy.data.AppState
import org.dev.healthbuddy.domain.Barrier
import org.dev.healthbuddy.domain.BarrierCatalog
import org.dev.healthbuddy.domain.Helpful
import org.dev.healthbuddy.domain.Lang
import org.dev.healthbuddy.domain.Medicine
import org.dev.healthbuddy.ui.Fmt
import org.dev.healthbuddy.ui.VisitSummary
import org.dev.healthbuddy.ui.components.EmptyState
import org.dev.healthbuddy.ui.components.HbCard
import org.dev.healthbuddy.ui.i18n.LocalStrings
import org.dev.healthbuddy.ui.i18n.Strings

@Composable
fun MedicinesScreen(state: AppState) {
    val s = LocalStrings.current

    if (state.addMedicineSheet) {
        AddMedicineSheet(state)
    }

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
        item(key = "swipe-hint") {
            Text(
                text = s.swipeToRemoveHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        item(key = "medicine-block") {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                state.medicines.forEachIndexed { index, med ->
                    val shape = joinedBlockShape(
                        isFirst = index == 0,
                        isLast = index == state.medicines.lastIndex
                    )
                    key(med.id) {
                        SwipeToRemoveMedicine(
                            shape = shape,
                            onRemove = { state.removeMedicine(med.id) }
                        ) {
                            MedicineRow(
                                med = med,
                                shape = shape,
                                timesText = med.times.joinToString(" · ") { Fmt.time(it) },
                                onToggle = { state.toggleMedicine(med.id) },
                                onEdit = { state.openEditMedicine(med) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Destructive action lives behind a swipe instead of an always-visible icon:
 * it keeps the row calm and keeps deletion out of the tap path.
 */
@Composable
private fun SwipeToRemoveMedicine(
    shape: Shape,
    onRemove: () -> Unit,
    content: @Composable () -> Unit
) {
    val s = LocalStrings.current
    val dismissState = rememberSwipeToDismissBoxState()

    SwipeToDismissBox(
        state = dismissState,
        modifier = Modifier.clip(shape),
        enableDismissFromStartToEnd = false,
        onDismiss = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) onRemove()
        },
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = s.remove,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Lucide.Trash2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    ) {
        content()
    }
}

@Composable
private fun MedicineRow(
    med: Medicine,
    shape: Shape,
    timesText: String,
    onToggle: () -> Unit,
    onEdit: () -> Unit
) {
    HbCard(shape = shape, onClick = onEdit) {
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
        }
    }
}

@Composable
fun HistoryScreen(
    state: AppState,
    onOpenFlow: (org.dev.healthbuddy.domain.DoseSlot) -> Unit
) {
    val s = LocalStrings.current
    val context = LocalContext.current
    val since = java.time.LocalDate.now().minusDays(29).toString()
    val grouped = state.logsDesc()
        .filter { it.date >= since }
        .groupBy { it.date }
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
        item(key = "share-summary") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FilledTonalButton(onClick = { shareVisitSummary(context, state, s) }) {
                    Icon(
                        imageVector = Lucide.Share2,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = s.shareSummary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

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
                    if (log.barrier != null && log.helpful == null) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = s.wasHelpful,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(
                                onClick = { state.setHelpful(log.id, Helpful.YES) },
                                label = { Text(s.yes) }
                            )
                            AssistChip(
                                onClick = { state.setHelpful(log.id, Helpful.NOT_SURE) },
                                label = { Text(s.somewhat) }
                            )
                            AssistChip(
                                onClick = { state.setHelpful(log.id, Helpful.NO) },
                                label = { Text(s.no) }
                            )
                        }
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

/**
 * Hands the summary to whatever app the user picks. No permission, no network:
 * Android's share sheet does the sending, so the data leaves the phone only
 * when the user chooses a destination.
 */
private fun shareVisitSummary(context: Context, state: AppState, s: Strings) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, s.summaryTitle)
        putExtra(Intent.EXTRA_TEXT, VisitSummary.build(state, s))
    }
    context.startActivity(Intent.createChooser(send, s.shareSummary))
}
