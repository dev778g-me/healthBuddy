package org.dev.healthbuddy.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.composables.icons.lucide.Camera
import com.composables.icons.lucide.Image
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ScanLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dev.healthbuddy.BuildConfig
import org.dev.healthbuddy.data.AppState
import org.dev.healthbuddy.scan.PrescriptionScanner
import org.dev.healthbuddy.scan.ScanOutcome
import org.dev.healthbuddy.scan.ScanStage
import org.dev.healthbuddy.ui.components.SafetyNote
import org.dev.healthbuddy.ui.i18n.LocalStrings
import org.dev.healthbuddy.ui.i18n.scanStrings
import java.io.File

/**
 * Capture sheet: camera or gallery, then the scan runs while the sheet stays
 * open. The sheet never saves a medicine; it hands a draft to the review screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanSheet(
    state: AppState,
    onDismiss: () -> Unit,
    onReview: () -> Unit
) {
    val s = LocalStrings.current
    val scanText = state.lang.scanStrings()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val scanner = remember { PrescriptionScanner(context) }
    val aiConfigured = remember {
        BuildConfig.GROQ_API_KEY.isNotBlank()
    }

    var busy by remember { mutableStateOf(false) }
    // AI decides the medicine lines; the on-device parser is only the fallback.
    var useAi by remember { mutableStateOf(aiConfigured) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var pendingCamera by remember { mutableStateOf<File?>(null) }
    // Live progress shown under the spinner while the scan runs.
    var stageMessage by remember { mutableStateOf<String?>(null) }

    fun newScanFile(): File {
        val dir = File(context.cacheDir, "scans").apply { mkdirs() }
        return File(dir, "capture-${System.currentTimeMillis()}.jpg")
    }

    fun startProcessing(file: File) {
        if (busy) return
        busy = true
        errorMessage = null
        stageMessage = ScanStage.PREPARING.message
        scope.launch {
            when (val outcome = scanner.scan(file, useAi && aiConfigured) { stage ->
                stageMessage = stage.message
            }) {
                is ScanOutcome.Failure -> {
                    errorMessage = outcome.message
                    busy = false
                    stageMessage = null
                }
                is ScanOutcome.Success -> {
                    busy = false
                    stageMessage = null
                    if (outcome.draft.medicines.isEmpty()) {
                        errorMessage = outcome.draft.warnings.firstOrNull()
                            ?: scanText.scanNoMedicinesBody
                    } else {
                        state.openScanReview(outcome.draft)
                        onReview()
                        onDismiss()
                    }
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val file = pendingCamera
        pendingCamera = null
        if (success && file != null && file.exists()) {
            startProcessing(file)
        } else {
            file?.delete()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            busy = true
            errorMessage = null
            stageMessage = "Loading the photo…"
            var partial: File? = null
            val copied = withContext(Dispatchers.IO) {
                runCatching {
                    val file = newScanFile()
                    partial = file
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        file.outputStream().use { output -> input.copyTo(output) }
                    } ?: throw IllegalStateException("The photo could not be opened.")
                    file
                }.getOrNull()
            }
            busy = false
            if (copied == null) {
                partial?.delete()
                stageMessage = null
                errorMessage = scanText.scanImageError
            } else {
                startProcessing(copied)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!busy) onDismiss() },
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Lucide.ScanLine,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = scanText.scanPrescription,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            Text(
                text = scanText.scanBody,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (busy) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text(
                        text = stageMessage
                            ?: if (useAi && aiConfigured) scanText.scanAiSending else scanText.scanReading,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Button(
                    onClick = {
                        runCatching {
                            val file = newScanFile()
                            pendingCamera = file
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            cameraLauncher.launch(uri)
                        }.onFailure {
                            pendingCamera?.delete()
                            pendingCamera = null
                            errorMessage = scanText.scanNoCamera
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Lucide.Camera,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(scanText.scanCamera)
                }
                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Lucide.Image,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(scanText.scanGallery)
                }
            }

            if (aiConfigured) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = scanText.scanAiConsent,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = scanText.scanAiBody,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = useAi,
                        onCheckedChange = { useAi = it },
                        enabled = !busy
                    )
                }
            } else {
                Text(
                    text = scanText.scanAiUnavailable,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SafetyNote(text = scanText.scanPrivacy)

            errorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { errorMessage = null }) {
                        Text(scanText.scanTryAgain)
                    }
                    TextButton(
                        onClick = {
                            onDismiss()
                            state.openAddMedicineSheet()
                        }
                    ) {
                        Text(scanText.scanEnterManually)
                    }
                }
            }

            if (!busy) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(s.cancel)
                }
            }
        }
    }
}
