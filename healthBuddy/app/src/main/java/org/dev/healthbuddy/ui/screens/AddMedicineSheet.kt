package org.dev.healthbuddy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.X
import kotlinx.coroutines.launch
import org.dev.healthbuddy.data.AppState
import org.dev.healthbuddy.ui.Fmt
import org.dev.healthbuddy.ui.i18n.LocalStrings
import java.util.Locale

private val PRESET_TIMES = listOf("08:00", "09:00", "13:00", "20:00", "21:00")

/**
 * Adding a medicine is a sheet, not a destination: the Medicines tab stays
 * visible behind it, there is no extra route to enter or leave, and the back
 * gesture has one obvious meaning.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineSheet(state: AppState) {
    val s = LocalStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val editing = state.editingMedicine

    var name by remember(editing?.id) { mutableStateOf(editing?.name ?: "") }
    var dose by remember(editing?.id) {
        mutableStateOf(editing?.dose?.takeUnless { it == "—" } ?: "")
    }
    var condition by remember(editing?.id) {
        mutableStateOf(editing?.condition?.takeUnless { it == "General" } ?: "")
    }
    var selected by remember(editing?.id) {
        mutableStateOf(editing?.times?.toSet() ?: emptySet())
    }
    val timePickerState = rememberTimePickerState(
        initialHour = 8,
        initialMinute = 0,
        is24Hour = false
    )
    var showTimePicker by remember { mutableStateOf(false) }
    var closing by remember { mutableStateOf(false) }

    val canSave = name.isNotBlank() && selected.isNotEmpty()
    val customTimes = selected.filterNot { it in PRESET_TIMES }.sorted()

    fun close() {
        if (closing) return
        closing = true
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            state.closeAddMedicineSheet()
        }
    }

    ModalBottomSheet(
        onDismissRequest = { state.closeAddMedicineSheet() },
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = if (editing == null) s.addMedicine else s.editMedicine,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                shape = MaterialTheme.shapes.medium,
                value = name,
                onValueChange = { name = it },
                label = { Text(s.medicineName) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                shape = MaterialTheme.shapes.medium,

                value = dose,
                onValueChange = { dose = it },
                label = { Text(s.doseAmount) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                shape = MaterialTheme.shapes.medium,

                value = condition,
                onValueChange = { condition = it },
                label = { Text(s.conditionLabel) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(22.dp))
            Text(s.timesLabel, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PRESET_TIMES.forEach { time ->
                    ToggleButton (
                        checked = selected.contains(time),
                        onCheckedChange =  {
                            selected =
                                if (selected.contains(time)) selected - time else selected + time
                        },
                        content  = { Text(Fmt.time(time)) }
                    )
                }
            }

            if (customTimes.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    customTimes.forEach { time ->
                        InputChip(
                            selected = true,
                            onClick = { selected = selected - time },
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
            }

            Spacer(Modifier.height(14.dp))
            OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Lucide.Plus,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(s.addTime)
            }

            Spacer(Modifier.height(26.dp))

            ButtonGroup(modifier = Modifier.fillMaxWidth(), overflowIndicator = {}) {
                
                clickableItem(
                    weight = 1f,
                    onClick = { close() },
                    enabled = !closing,
                    label = s.cancel
                )
                clickableItem(
                    weight = 1f,
                    onClick = {
                        if (editing == null) {
                            state.addMedicine(name, dose, condition, selected.toList())
                        } else {
                            state.updateMedicine(
                                editing.id, name, dose, condition, selected.toList()
                            )
                        }
                        close()
                    },
                    enabled =  canSave && !closing,
                    label = s.save
                )
            }

        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(s.addTime) },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    selected = selected + "%02d:%02d".format(
                        Locale.US,
                        timePickerState.hour,
                        timePickerState.minute
                    )
                    showTimePicker = false
                }) { Text(s.done) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(s.cancel) }
            }
        )
    }
}
