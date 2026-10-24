package org.dev.healthbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.CalendarDays
import com.composables.icons.lucide.Lucide
import org.dev.healthbuddy.data.AppState
import org.dev.healthbuddy.domain.BarrierCatalog
import org.dev.healthbuddy.domain.DoseSlot
import org.dev.healthbuddy.domain.DoseStatus
import org.dev.healthbuddy.ui.Fmt
import org.dev.healthbuddy.ui.components.EmptyState
import org.dev.healthbuddy.ui.components.HbCard
import org.dev.healthbuddy.ui.components.SectionTitle
import org.dev.healthbuddy.ui.components.StatusPill
import org.dev.healthbuddy.ui.components.statusColors
import org.dev.healthbuddy.ui.components.statusContainerColors
import org.dev.healthbuddy.ui.components.statusLabel
import org.dev.healthbuddy.ui.i18n.LocalStrings

@Composable
fun DashboardScreen(
    state: AppState,
    onOpenFlow: (DoseSlot) -> Unit
) {
    val s = LocalStrings.current
    val attention = state.attentionSlots()
    val slots = state.todaySlots()
    val appointment = state.nextAppointment()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background, shape = MaterialTheme.shapes.medium),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = Fmt.todayHeader(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        if (attention.isNotEmpty()) {
            item {
                SectionTitle(
                    text = s.needsAttention,
                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                )
            }
            items(attention, key = { "att-${it.id}" }) { slot ->
                AttentionCard(slot = slot, state = state, onClick = { onOpenFlow(slot) })
            }
        }

        item {
            SectionTitle(
                text = s.todaySchedule,
                modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
            )
        }

        if (slots.isEmpty()) {
            item {
                EmptyState(
                    icon = Lucide.CalendarDays,
                    title = s.nothingToday,
                    body = s.nothingTodayBody
                )
            }
        } else {
           item {
               Column(
                   verticalArrangement = Arrangement.spacedBy(2.dp)
               ) {
                   slots.forEach { slot ->
                       DoseCard(slot = slot, state = state, onClick = { onOpenFlow(slot) }, shape = joinedBlockShape(isFirst = slot == slots.first(), isLast = slot == slots.last()))
                   }
               }
           }
        }

        if (appointment != null) {
            item {
                SectionTitle(
                    text = s.upcoming,
                    modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                )
            }
            item {
                HbCard(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
                    Text(
                        text = appointment.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = appointment.doctor,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${Fmt.dayLabel(appointment.date, s)} · ${Fmt.time(appointment.time)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            SectionTitle(
                text = s.tipOfDay,
                modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
            )
        }
        item {
            HbCard(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                Text(
                    text = state.tip(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun AttentionCard(
    slot: DoseSlot,
    state: AppState,
    onClick: () -> Unit
) {
    val s = LocalStrings.current
    val (container, content) = statusContainerColors(DoseStatus.MISSED)
    val (pillContainer, pillContent) = statusColors(DoseStatus.MISSED)
    HbCard(
        containerColor = container,
        contentColor = content,
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (state.privateMode) s.medicinesTitle else slot.medicine.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${Fmt.dayLabel(slot.date, s)} · ${Fmt.time(slot.time)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            StatusPill(
                text = s.missed,
                container = pillContainer,
                content = pillContent
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = s.tapToExplain,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DoseCard(
    slot: DoseSlot,
    state: AppState,
    onClick: () -> Unit,
    shape : Shape,
) {
    val s = LocalStrings.current
    val (container, content) = statusColors(slot.status)
    val barrier = slot.log?.barrier
    val isPending = slot.status == DoseStatus.PENDING


    val statusPill: (@Composable () -> Unit)? = if (isPending) {
        null
    } else {
        {
            StatusPill(
                text = statusLabel(slot.status, s),
                container = container,
                content = content
            )
        }
    }

    ListItem(
        onClick = onClick,
        verticalAlignment = Alignment.CenterVertically,
        shapes = ListItemDefaults.shapes(shape),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        leadingContent = {
            Text(
                text = Fmt.time(slot.time),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isPending) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.width(64.dp)
            )
        },
        trailingContent = statusPill,
        supportingContent = {
            Text(
                text = if (state.privateMode) slot.medicine.condition
                else "${slot.medicine.dose} · ${slot.medicine.condition}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (slot.status == DoseStatus.MISSED && barrier != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${s.identified}: " +
                        BarrierCatalog.of(barrier).text(state.lang).label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) {
        Text(
            text = if (state.privateMode) s.medicinesTitle else slot.medicine.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

internal fun joinedBlockShape(isFirst: Boolean, isLast: Boolean): AbsoluteRoundedCornerShape =
    AbsoluteRoundedCornerShape(
        topLeft = if (isFirst) 16.dp else 4.dp,
        topRight = if (isFirst) 16.dp else 4.dp,
        bottomLeft = if (isLast) 16.dp else 4.dp,
        bottomRight = if (isLast) 16.dp else 4.dp
    )