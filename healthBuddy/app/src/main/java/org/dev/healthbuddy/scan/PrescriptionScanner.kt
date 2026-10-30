package org.dev.healthbuddy.scan

import android.content.Context
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dev.healthbuddy.BuildConfig
import org.dev.healthbuddy.scan.GroqInterpreter
import org.dev.healthbuddy.scan.MlKitOcr
import org.dev.healthbuddy.scan.PrescriptionParser
import org.dev.healthbuddy.scan.ScanOutcome
import java.io.File

/** Progress stages surfaced live in the scan sheet while the scan runs. */
enum class ScanStage(val message: String) {
    PREPARING("Preparing the photo…"),
    READING("Reading the handwriting…"),
    THINKING("Asking the AI to interpret it…"),
    FINISHING("Almost done…")
}

/** Runs capture → on-device OCR → deterministic parser → optional Groq AI.
 * The image is deleted in [finally], whatever happens.
 */
class PrescriptionScanner(context: Context) {

    private val appContext = context.applicationContext

    private val groqApiKey: String get() = BuildConfig.GROQ_API_KEY.trim()

    suspend fun scan(
        image: File,
        useAi: Boolean,
        onStage: (ScanStage) -> Unit = {}
    ): ScanOutcome = withContext(Dispatchers.IO) {
        var prepared: PreparedImage? = null
        try {
            onStage(ScanStage.PREPARING)
            prepared = ImagePreparer(appContext).prepare(image)
            onStage(ScanStage.READING)
            val ocrText = MlKitOcr().recognize(prepared.bitmap)
            val onDevice = PrescriptionParser.parse(ocrText)

            val aiConfigured = useAi && !groqApiKey.isBlank()

            if (useAi && aiConfigured) {
                onStage(ScanStage.THINKING)
                val ai = GroqInterpreter().interpret(prepared.bitmap, ocrText, groqApiKey)
                onStage(ScanStage.FINISHING)
                if (ai != null) {
                    return@withContext ScanOutcome.Success(
                        ai.copy(warnings = (ai.warnings + onDevice.warnings).distinct())
                    )
                }
                // Groq call failed or returned nothing; fall back to on-device + extra warning
                return@withContext ScanOutcome.Success(
                    onDevice.copy(
                        warnings = onDevice.warnings + listOf(
                            "The AI (GPT-OSS 120B) could not read this prescription. The on‑device text is shown instead."
                        ),
                        aiConfigured = true
                    )
                )
            }

            onStage(ScanStage.FINISHING)
            ScanOutcome.Success(
                onDevice.copy(
                    warnings = onDevice.warnings + if (useAi) {
                        listOf("AI is not configured. Using on‑device OCR only.")
                    } else {
                        emptyList()
                    },
                    aiConfigured = aiConfigured
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            ScanOutcome.Failure("The prescription could not be read. Try again or enter it manually.")
        } finally {
            runCatching { prepared?.bitmap?.recycle() }
            runCatching { prepared?.file?.delete() }
            runCatching { image.delete() }
            ImagePreparer(appContext).cleanupOldFiles()
        }
    }
}