package org.dev.healthbuddy.ui.flow

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.TriangleAlert
import org.dev.healthbuddy.data.AppState
import org.dev.healthbuddy.domain.ActionType
import org.dev.healthbuddy.domain.Barrier
import org.dev.healthbuddy.domain.BarrierCatalog
import org.dev.healthbuddy.domain.DoseSlot
import org.dev.healthbuddy.domain.DoseStatus
import org.dev.healthbuddy.domain.Helpful
import org.dev.healthbuddy.ui.Fmt
import org.dev.healthbuddy.ui.components.BulletRow
import org.dev.healthbuddy.ui.components.SafetyNote
import org.dev.healthbuddy.ui.i18n.LocalStrings
import org.dev.healthbuddy.ui.i18n.Strings

private enum class Step { LOG, BARRIER, SUPPORT, FOLLOWUP, DONE, UNSURE }

/**
 * REMIND -> IDENTIFY -> SUPPORT -> FOLLOW UP.
 * The entire product lives in this one sheet, deliberately: four separate
 * sheets is the classic way to lose the user (and the demo) mid-flow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoseFlowSheet(
    slot: DoseSlot,
    state: AppState,
    onLogged: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val s = LocalStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val log = slot.log

    fun record(status: DoseStatus, label: String) {
        haptics.performHapticFeedback(HapticFeedbackType.Confirm)
        state.logDose(slot, status)
        onLogged("$label · ${Fmt.time(slot.time)} · ${slot.medicine.name}")
    }

    var step by remember(slot.id) {
        mutableStateOf(
            when {
                log == null || log.status == DoseStatus.PENDING -> Step.LOG
                log.status == DoseStatus.TAKEN -> Step.DONE
                log.status == DoseStatus.UNSURE -> Step.UNSURE
                log.status == DoseStatus.MISSED && log.barrier == null -> Step.BARRIER
                else -> Step.SUPPORT
            }
        )
    }
    var chosen by remember(slot.id) { mutableStateOf(log?.barrier) }

    fun dial(number: String) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(bottom = 36.dp)
        ) {
            FlowHeader(slot, s)

            Spacer(Modifier.height(18.dp))

            when (step) {
                Step.LOG -> {
                    Text(s.didYouTake, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = {
                            record(DoseStatus.TAKEN, s.taken)
                            step = Step.DONE
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(s.taken) }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            record(DoseStatus.MISSED, s.missed)
                            step = Step.BARRIER
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(s.missed) }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            record(DoseStatus.UNSURE, s.notSure)
                            step = Step.UNSURE
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(s.notSure) }
                }

                Step.BARRIER -> {
                    Text(s.whyQuestion, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = s.needsAttentionBody,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(18.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        BarrierCatalog.all.forEach { support ->
                            org.dev.healthbuddy.ui.components.OptionRow(
                                label = support.text(state.lang).label,
                                onClick = {
                                    chosen = support.barrier
                                    state.setBarrier(slot.id, support.barrier)
                                    step = Step.SUPPORT
                                }
                            )
                        }
                    }
                }

                Step.SUPPORT -> {
                    val barrier = chosen
                    if (barrier == null) {
                        // Defensive: never render an empty support step.
                        Text(
                            text = s.whyQuestion,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    } else {
                        val support = BarrierCatalog.of(barrier)
                        val text = support.text(state.lang)

                        Text(
                            text = text.title,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = text.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (text.bullets.isNotEmpty()) {
                            Spacer(Modifier.height(14.dp))
                            val bulletsAreTheAction = support.action == ActionType.FIND_FACILITY ||
                                support.action == ActionType.READ
                            if (bulletsAreTheAction && text.actionLabel != null) {
                                Text(
                                    text = text.actionLabel,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                text.bullets.forEach { BulletRow(it) }
                            }
                        }

                        if (support.action == ActionType.ADJUST_REMINDER) {
                            Spacer(Modifier.height(16.dp))
                            var moved by remember { mutableStateOf(false) }
                            if (!moved) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AssistChip(onClick = {
                                        state.adjustReminder(slot.medicine.id, 15)
                                        moved = true
                                    }, label = { Text(s.later15) })
                                    AssistChip(onClick = {
                                        state.adjustReminder(slot.medicine.id, 30)
                                        moved = true
                                    }, label = { Text(s.later30) })
                                    AssistChip(onClick = {
                                        state.adjustReminder(slot.medicine.id, 60)
                                        moved = true
                                    }, label = { Text(s.later60) })
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Lucide.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = s.reminderMoved,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        if (support.callNumber != null) {
                            Spacer(Modifier.height(16.dp))
                            FilledTonalButton(
                                onClick = { dial(support.callNumber) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("${s.callNow} ${support.callNumber}")
                            }
                        }

                        if (text.safety != null) {
                            Spacer(Modifier.height(16.dp))
                            SafetyNote(text.safety)
                        }

                        Spacer(Modifier.height(22.dp))

                        if (support.action == ActionType.NONE) {
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(s.close) }
                        } else {
                            Button(
                                onClick = { step = Step.FOLLOWUP },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(s.done) }
                        }
                    }
                }

                Step.FOLLOWUP -> {
                    Text(s.wasHelpful, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = {
                            state.setHelpful(slot.id, Helpful.YES)
                            step = Step.DONE
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(s.yes) }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            state.setHelpful(slot.id, Helpful.NOT_SURE)
                            step = Step.DONE
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(s.somewhat) }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            state.setHelpful(slot.id, Helpful.NO)
                            step = Step.DONE
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(s.no) }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = s.disclaimer,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Step.DONE -> {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Lucide.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = s.didYouTake,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = s.done,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(22.dp))
                    Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                        Text(s.close)
                    }
                }

                Step.UNSURE -> {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Lucide.TriangleAlert,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(s.notSureTitle, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = s.notSureBody,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(22.dp))
                    Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                        Text(s.close)
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowHeader(slot: DoseSlot, s: Strings) {
    Column {
        Text(
            text = if (slot.medicine.name.isBlank()) s.appName else slot.medicine.name,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "${Fmt.time(slot.time)} · ${slot.medicine.dose}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
