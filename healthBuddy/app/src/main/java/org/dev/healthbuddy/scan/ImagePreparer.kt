package org.dev.healthbuddy.scan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import java.io.File
import java.io.FileOutputStream
import kotlin.math.ceil
import kotlin.math.max

data class PreparedImage(
    val bitmap: Bitmap,
    val file: File
)

/**
 * Downscales a captured/picked photo once, honouring EXIF orientation, and
 * writes a temporary JPEG for the OCR and AI calls.
 */
class ImagePreparer(private val context: Context) {

    fun prepare(source: File): PreparedImage {
        cleanupOldFiles()
        val decoded = decode(source) ?: error("This image could not be opened.")
        val bitmap = scaleDown(decoded, MAX_DIMENSION)
        if (bitmap !== decoded) decoded.recycle()

        val dir = File(context.cacheDir, SCAN_DIR).apply { mkdirs() }
        val output = File(dir, "prepared-${System.currentTimeMillis()}.jpg")
        val written = runCatching {
            FileOutputStream(output).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)
            }
        }.getOrElse { cause ->
            output.delete()
            throw cause
        }
        if (!written) {
            output.delete()
            error("This image could not be prepared.")
        }
        return PreparedImage(bitmap, output)
    }

    fun cleanupOldFiles() {
        val dir = File(context.cacheDir, SCAN_DIR)
        val cutoff = System.currentTimeMillis() - CACHE_MAX_AGE_MS
        runCatching {
            dir.listFiles()?.forEach { file ->
                if (file.lastModified() < cutoff) file.delete()
            }
        }
    }

    private fun decode(source: File): Bitmap? = runCatching {
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(source)) { decoder, info, _ ->
            val longest = max(info.size.width, info.size.height)
            if (longest > MAX_DIMENSION) {
                decoder.setTargetSampleSize(ceil(longest.toDouble() / MAX_DIMENSION).toInt())
            }
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.isMutableRequired = false
        }
    }.getOrElse {
        // ImageDecoder can reject some unusual camera formats; BitmapFactory
        // still gives ML Kit a usable bitmap.
        runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(source.absolutePath, bounds)
            var sample = 1
            while (max(bounds.outWidth, bounds.outHeight) / sample > MAX_DIMENSION * 2) {
                sample *= 2
            }
            BitmapFactory.decodeFile(
                source.absolutePath,
                BitmapFactory.Options().apply { inSampleSize = sample }
            )
        }.getOrNull()
    }

    private fun scaleDown(source: Bitmap, maxDimension: Int): Bitmap {
        val longest = max(source.width, source.height)
        if (longest <= maxDimension) return source
        val ratio = maxDimension.toFloat() / longest
        return Bitmap.createScaledBitmap(
            source,
            (source.width * ratio).toInt().coerceAtLeast(1),
            (source.height * ratio).toInt().coerceAtLeast(1),
            true
        )
    }

    companion object {
        const val SCAN_DIR = "scans"
        private const val MAX_DIMENSION = 1600
        private const val JPEG_QUALITY = 88
        private const val CACHE_MAX_AGE_MS = 60L * 60L * 1000L
    }
}
