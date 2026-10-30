package org.dev.healthbuddy.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.composables.icons.lucide.Camera
import com.composables.icons.lucide.CircleCheck
import com.composables.icons.lucide.Image
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ScanLine
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.TriangleAlert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dev.healthbuddy.BuildConfig
import org.dev.healthbuddy.data.AppState
import org.dev.healthbuddy.scan.PrescriptionScanner
import org.dev.healthbuddy.scan.ScanOutcome
import org.dev.healthbuddy.scan.ScanStage
import org.dev.healthbuddy.ui.components.HbCard
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
    // Live stage shown in the stepper while the scan runs.
    var currentStage by remember { mutableStateOf<ScanStage?>(null) }

    fun newScanFile(): File {
        val dir = File(context.cacheDir, "scans").apply { mkdirs() }
        return File(dir, "capture-${System.currentTimeMillis()}.jpg")
    }

    fun startProcessing(file: File) {
        if (busy) return
        busy = true
        errorMessage = null
        currentStage = ScanStage.PREPARING
        scope.launch {
            when (val outcome = scanner.scan(file, useAi && aiConfigured) { stage ->
                currentStage = stage
            }) {
                is ScanOutcome.Failure -> {
                    errorMessage = outcome.message
                    busy = false
                    currentStage = null
                }
                is ScanOutcome.Success -> {
                    busy = false
                    currentStage = null
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
            currentStage = ScanStage.PREPARING
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
                currentStage = null
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
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Lucide.ScanLine,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = scanText.scanPrescription,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = scanText.scanBody,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (busy) {
                ScanProgressCard(
                    currentStage = currentStage,
                    aiEnabled = useAi && aiConfigured,
                    fallbackText = if (useAi && aiConfigured) {
                        scanText.scanAiSending
                    } else {
                        scanText.scanReading
                    }
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ScanOptionCard(
                        icon = Lucide.Camera,
                        label = scanText.scanCamera,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
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
                        }
                    )
                    ScanOptionCard(
                        icon = Lucide.Image,
                        label = scanText.scanGallery,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                    )
                }
            }

            if (aiConfigured) {
                HbCard(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    shape = MaterialTheme.shapes.large
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Lucide.Sparkles,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
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
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = useAi,
                            onCheckedChange = { useAi = it },
                            enabled = !busy
                        )
                    }
                }
            } else {
                Text(
                    text = scanText.scanAiUnavailable,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SafetyNote(text = scanText.scanPrivacy)

            errorMessage?.let { message ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            MaterialTheme.shapes.large
                        )
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Lucide.TriangleAlert,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
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

/** Big tappable tile for "Camera" / "Gallery" — a large, obvious target. */
@Composable
private fun ScanOptionCard(
    icon: ImageVector,
    label: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    HbCard(
        onClick = onClick,
        containerColor = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large,
        modifier = modifier.height(116.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(text = label, style = MaterialTheme.typography.titleSmall)
        }
    }
}

/**
 * Busy state: a spinner plus a live stepper of the scan stages, so the wait
 * reads as progress instead of a frozen sheet.
 */
@Composable
private fun ScanProgressCard(
    currentStage: ScanStage?,
    aiEnabled: Boolean,
    fallbackText: String
) {
    val stages = remember(aiEnabled) {
        buildList {
            add(ScanStage.PREPARING)
            add(ScanStage.READING)
            if (aiEnabled) add(ScanStage.THINKING)
            add(ScanStage.FINISHING)
        }
    }
    val currentIndex = currentStage?.let { stages.indexOf(it) } ?: 0

    HbCard(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(14.dp))
            Text(
                text = currentStage?.message ?: fallbackText,
                style = MaterialTheme.typography.titleSmall
            )
        }
        Spacer(Modifier.height(14.dp))
        stages.forEachIndexed { index, stage ->
            val done = index < currentIndex
            val active = index == currentIndex
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (done) {
                    Icon(
                        imageVector = Lucide.CircleCheck,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                } else if (active) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceContainerHighest,
                                CircleShape
                            )
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stage.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (done || active) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            if (index < stages.lastIndex) Spacer(Modifier.height(8.dp))
        }
    }
}
