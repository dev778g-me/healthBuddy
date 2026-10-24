package org.dev.healthbuddy.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.dev.healthbuddy.domain.DoseStatus
import org.dev.healthbuddy.ui.i18n.Strings

/**
 * A missed dose is NEVER red. It is "information that needs attention",
 * shown in the tertiary (amber) role. Red is reserved for destructive actions.
 */
@Composable
fun statusColors(status: DoseStatus): Pair<Color, Color> = when (status) {
    DoseStatus.PENDING ->
        MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    DoseStatus.TAKEN ->
        MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    DoseStatus.MISSED ->
        MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
    DoseStatus.UNSURE ->
        MaterialTheme.colorScheme.surfaceContainerHighest to MaterialTheme.colorScheme.onSurfaceVariant
}

fun statusLabel(status: DoseStatus, s: Strings): String = when (status) {
    DoseStatus.PENDING -> s.todaySchedule
    DoseStatus.TAKEN -> s.taken
    DoseStatus.MISSED -> s.missed
    DoseStatus.UNSURE -> s.notSure
}
