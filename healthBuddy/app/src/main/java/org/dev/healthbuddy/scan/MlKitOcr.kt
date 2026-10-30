package org.dev.healthbuddy.scan

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.android.gms.tasks.Tasks

/** On-device OCR via ML Kit Latin text recognition. */
class MlKitOcr {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /** Recognizes text lines from [bitmap]; returns the joined text or "" on failure. */
    suspend fun recognize(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = Tasks.await(recognizer.process(image))
            // Join recognized blocks/lines — keeps line breaks so the parser
            // can split medicine lines reliably.
            buildString {
                for (block in result.textBlocks) {
                    for (line in block.lines) {
                        if (isNotEmpty()) append('\n')
                        append(line.text)
                    }
                }
            }
        } catch (_: Exception) {
            ""
        }
    }

    fun close() {
        runCatching { recognizer.close() }
    }
}
