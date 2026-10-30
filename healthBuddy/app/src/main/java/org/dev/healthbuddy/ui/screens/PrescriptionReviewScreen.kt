package org.dev.healthbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.CircleCheck
import com.composables.icons.lucide.Copy
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.X
import org.dev.healthbuddy.data.AppState
import org.dev.healthbuddy.scan.ScannedMedicine
import org.dev.healthbuddy.scan.ScanSource
import org.dev.healthbuddy.ui.Fmt
import org.dev.healthbuddy.ui.components.HbCard
import org.dev.healthbuddy.ui.components.SafetyNote
import org.dev.healthbuddy.ui.i18n.LocalStrings
import org.dev.healthbuddy.ui.i18n.scanStrings
import java.util.Locale

/**
 * Mandatory confirmation step. Nothing is written to the medicine list until
 * the user has seen the OCR/AI draft and pressed save.
 */
@Composable
fun PrescriptionReviewScreen(state: AppState, onDone: () -> Unit) {
    val s = LocalStrings.current
    val scanText = state.lang.scanStrings()
    val draft = state.scanDraft
    val context = LocalContext.current

    // Do not keep the OCR text in memory after leaving the review screen.
    DisposableEffect(Unit) {
        onDispose { state.clearScanDraft() }
    }

    if (draft == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(scanText.scanNoMedicines, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(scanText.scanNoMedicinesBody)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onDone) { Text(s.close) }
        }
        return
    }

    val items = remember(draft) {
        draft.medicines.map { EditableScanMedicine(it) }.toMutableStateList()
    }
    var showRaw by remember { mutableStateOf(false) }
    var rawCopied by remember { mutableStateOf(false) }
    var showSaveError by remember { mutableStateOf(false) }
    var timeTarget by remember { mutableStateOf<EditableScanMedicine?>(null) }
    val timePickerState = rememberTimePickerState(initialHour = 8, initialMinute = 0, is24Hour = false)

    val included = items.filter { it.included }
    val invalid = included.any { it.name.isBlank() || it.times.isEmpty() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "review-header") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = scanText.scanReviewTitle,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = scanText.scanReviewBody,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                if (draft.source == ScanSource.AI_ASSISTED) {
                                    scanText.scanAiBadge
                                } else {
                                    scanText.scanOcrBadge
                                }
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (draft.source == ScanSource.AI_ASSISTED) {
                                    Lucide.Sparkles
                                } else {
                                    Lucide.FileText
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }

        if (draft.warnings.isNotEmpty()) {
            item(key = "warnings") {
                SafetyNote(text = draft.warnings.joinToString("\n"))
            }
        }

        items(items, key = { System.identityHashCode(it) }) { item ->
            MedicineDraftCard(
                item = item,
                existingNames = state.medicines.map { it.name.lowercase() }.toSet(),
                duplicateCount = items.count {
                    it.included && it.name.trim().equals(item.name.trim(), ignoreCase = true)
                },
                scanText = scanText,
                onAddTime = { timeTarget = item }
            )
        }

        item(key = "raw-text") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showRaw = !showRaw },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Lucide.FileText, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (showRaw) scanText.scanHideRaw else scanText.scanShowRaw)
                }
                if (showRaw) {
                    HbCard(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
                        Text(
                            text = draft.rawText.ifBlank { "—" },
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                                as android.content.ClipboardManager
                            clipboard.setPrimaryClip(
                                android.content.ClipData.newPlainText(
                                    scanText.scanRawText,
                                    draft.rawText
                                )
                            )
                            rawCopied = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (rawCopied) Lucide.CircleCheck else Lucide.Copy,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (rawCopied) scanText.scanCopied else scanText.scanCopyText)
                    }
                }
            }
        }

        item(key = "save") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (showSaveError) {
                    Text(
                        text = scanText.scanFixFields,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Button(
                    onClick = {
                        if (included.isEmpty() || invalid) {
                            showSaveError = true
                        } else {
                            state.addScannedMedicines(included.map { it.toScanned() })
                            onDone()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Lucide.CircleCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(scanText.scanSaveMedicines)
                }
            }
        }
    }

    timeTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { timeTarget = null },
            title = { Text(s.addTime) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val formatted = "%02d:%02d".format(
                        Locale.US,
                        timePickerState.hour,
                        timePickerState.minute
                    )
                    target.times = (target.times + formatted).distinct().sorted()
                    timeTarget = null
                }) { Text(s.done) }
            },
            dismissButton = {
                TextButton(onClick = { timeTarget = null }) { Text(s.cancel) }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicineDraftCard(
    item: EditableScanMedicine,
    existingNames: Set<String>,
    duplicateCount: Int,
    scanText: org.dev.healthbuddy.ui.i18n.ScanStrings,
    onAddTime: () -> Unit
) {
    val s = LocalStrings.current
    val duplicate = item.name.trim().lowercase() in existingNames || duplicateCount > 1
    val needsName = item.included && item.name.isBlank()
    val needsTime = item.included && item.times.isEmpty()

    HbCard(
        containerColor = if (item.included) {
            MaterialTheme.colorScheme.surfaceContainerLow
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = item.included,
                onCheckedChange = { item.included = it }
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name.ifBlank { scanText.scanNoMedicines },
                    style = MaterialTheme.typography.titleMedium
                )
                if (duplicate) {
                    Text(
                        text = scanText.scanDuplicate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = item.name,
            onValueChange = { item.name = it },
            label = { Text(s.medicineName) },
            singleLine = true,
            isError = needsName,
            supportingText = {
                if (needsName || "name" in item.original.lowConfidenceFields) {
                    Text(scanText.scanCheckField)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = item.dose,
                onValueChange = { item.dose = it },
                label = { Text(s.doseAmount) },
                supportingText = {
                    if ("dose" in item.original.lowConfidenceFields) {
                        Text(scanText.scanCheckField)
                    }
                },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = item.frequency,
                onValueChange = { item.frequency = it },
                label = { Text(scanText.scanFrequency) },
                supportingText = {
                    if ("frequency" in item.original.lowConfidenceFields) {
                        Text(scanText.scanCheckField)
                    }
                },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = item.durationText,
            onValueChange = { value ->
                item.durationText = value.filter { it.isDigit() }.take(3)
            },
            label = { Text(scanText.scanDuration) },
            supportingText = {
                Text(
                    if ("duration" in item.original.lowConfidenceFields) {
                        scanText.scanCheckField
                    } else {
                        scanText.scanDurationHint
                    }
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = item.instructions,
            onValueChange = { item.instructions = it },
            label = { Text(scanText.scanInstructions) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))
        Text(
            text = s.timesLabel,
            style = MaterialTheme.typography.titleSmall,
            color = if (needsTime) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item.times.forEach { time ->
                InputChip(
                    selected = true,
                    onClick = { item.times = item.times - time },
                    label = { Text(Fmt.time(time)) },
                    trailingIcon = {
                        Icon(
                            imageVector = Lucide.X,
                            contentDescription = s.remove,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
        if (needsTime) {
            Text(
                text = scanText.scanFixFields,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        Spacer(Modifier.height(4.dp))
        OutlinedButton(onClick = onAddTime) {
            Icon(Lucide.Plus, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(s.addTime)
        }
        if (item.included) {
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

private class EditableScanMedicine(initial: ScannedMedicine) {
    val original = initial
    var included by mutableStateOf(true)
    var name by mutableStateOf(initial.name)
    var dose by mutableStateOf(initial.dose)
    var frequency by mutableStateOf(initial.frequency)
    var durationText by mutableStateOf(initial.durationDays?.toString().orEmpty())
    var times by mutableStateOf(initial.times)
    var instructions by mutableStateOf(initial.instructions.orEmpty())

    fun toScanned(): ScannedMedicine = original.copy(
        name = name.trim(),
        dose = dose.trim(),
        frequency = frequency.trim(),
        durationDays = durationText.toIntOrNull()?.takeIf { it > 0 },
        times = times.distinct().sorted(),
        instructions = instructions.trim().ifBlank { null }
    )
}
