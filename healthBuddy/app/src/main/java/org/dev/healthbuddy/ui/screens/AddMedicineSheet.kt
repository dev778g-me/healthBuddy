package org.dev.healthbuddy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.composables.icons.lucide.X
import kotlinx.coroutines.launch
import org.dev.healthbuddy.data.AppState
import org.dev.healthbuddy.ui.i18n.LocalStrings
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val PRESET_TIMES = listOf("08:00", "09:00", "13:00", "20:00", "21:00")
private val HH_MM: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

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
        mutableStateOf(editing?.times?.toSet() ?: setOf("08:00"))
    }
    var custom by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
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
                .padding(horizontal = 22.dp)
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = if (editing == null) s.addMedicine else s.editMedicine,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(s.medicineName) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = dose,
                onValueChange = { dose = it },
                label = { Text(s.doseAmount) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
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
                    FilterChip(
                        selected = selected.contains(time),
                        onClick = {
                            selected =
                                if (selected.contains(time)) selected - time else selected + time
                        },
                        label = { Text(time) }
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
                            label = { Text(time) },
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = custom,
                    onValueChange = { custom = it },
                    label = { Text("HH:mm") },
                    singleLine = true,
                    isError = error != null,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = {
                        val parsed = runCatching {
                            LocalTime.parse(custom.trim()).format(HH_MM)
                        }.getOrNull()
                        if (parsed != null) {
                            selected = selected + parsed
                            custom = ""
                            error = null
                        } else {
                            error = "HH:mm"
                        }
                    }
                ) { Text(s.addTime) }
            }
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = s.timeFormatHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(26.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { close() },
                    enabled = !closing,
                    modifier = Modifier.weight(1f)
                ) { Text(s.cancel) }
                Button(
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
                    enabled = canSave && !closing,
                    modifier = Modifier.weight(1f)
                ) { Text(s.save) }
            }
        }
    }
}
