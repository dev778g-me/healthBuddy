package org.dev.healthbuddy.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.compose.successColors
import org.dev.healthbuddy.domain.DoseStatus
import org.dev.healthbuddy.ui.i18n.Strings

/**
 * Status colours come straight from the theme, so they follow the light/dark
 * preference picked in Settings: success for a logged dose, error for a missed
 * one. M3 has no success role, so [MaterialTheme.successColors] supplies it
 * next to the error role.
 */
@Composable
fun statusColors(status: DoseStatus): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    val success = MaterialTheme.successColors
    return when (status) {
        DoseStatus.PENDING ->
            scheme.secondaryContainer to scheme.onSecondaryContainer
        DoseStatus.TAKEN ->
            success.success to success.onSuccess
        DoseStatus.MISSED ->
            scheme.error to scheme.onError
        DoseStatus.UNSURE ->
            scheme.tertiaryContainer to scheme.onTertiaryContainer
    }
}

/** Softer variant of [statusColors], for surfaces that are already loud. */
@Composable
fun statusContainerColors(status: DoseStatus): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    val success = MaterialTheme.successColors
    return when (status) {
        DoseStatus.PENDING ->
            scheme.secondaryContainer to scheme.onSecondaryContainer
        DoseStatus.TAKEN ->
            success.successContainer to success.onSuccessContainer
        DoseStatus.MISSED ->
            scheme.errorContainer to scheme.onErrorContainer
        DoseStatus.UNSURE ->
            scheme.tertiaryContainer to scheme.onTertiaryContainer
    }
}

fun statusLabel(status: DoseStatus, s: Strings): String = when (status) {
    DoseStatus.PENDING -> s.todaySchedule
    DoseStatus.TAKEN -> s.taken
    DoseStatus.MISSED -> s.missed
    DoseStatus.UNSURE -> s.notSure
}
