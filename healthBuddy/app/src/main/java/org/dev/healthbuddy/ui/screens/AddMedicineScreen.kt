package org.dev.healthbuddy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dev.healthbuddy.data.AppState
import org.dev.healthbuddy.ui.components.HbCard
import org.dev.healthbuddy.ui.i18n.LocalStrings
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val PRESET_TIMES = listOf("08:00", "09:00", "13:00", "20:00", "21:00")
private val HH_MM: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineScreen(
    state: AppState,
    onDone: () -> Unit
) {
    val s = LocalStrings.current
    var name by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(setOf("08:00")) }
    var custom by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val canSave = name.isNotBlank() && selected.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
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

        Spacer(Modifier.height(20.dp))
        Text(s.timesLabel, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))

        HbCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PRESET_TIMES.take(3).forEach { time ->
                    FilterChip(
                        selected = selected.contains(time),
                        onClick = {
                            selected = if (selected.contains(time)) selected - time
                            else selected + time
                        },
                        label = { Text(time) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PRESET_TIMES.drop(3).forEach { time ->
                    FilterChip(
                        selected = selected.contains(time),
                        onClick = {
                            selected = if (selected.contains(time)) selected - time
                            else selected + time
                        },
                        label = { Text(time) }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = custom,
                    onValueChange = { custom = it },
                    label = { Text("HH:mm") },
                    singleLine = true,
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
                ) { Text(s.save) }
            }
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Use 24-hour format, e.g. 20:30",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                state.addMedicine(name, dose, condition, selected.toList())
                onDone()
            },
            enabled = canSave,
            modifier = Modifier.fillMaxWidth()
        ) { Text(s.save) }

        Spacer(Modifier.height(32.dp))
    }
}
