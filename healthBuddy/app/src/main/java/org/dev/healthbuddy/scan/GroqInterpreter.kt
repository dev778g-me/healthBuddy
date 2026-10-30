package org.dev.healthbuddy.scan

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Groq pass: the on-device OCR text is sent to Groq's chat API with a system
 * prompt; the model decides which medicine lines exist and returns JSON.
 * The AI result — not the regex parser — is what the review screen shows.
 *
 * If no key is configured or the request fails, the caller falls back to the
 * on-device draft.
 */
class GroqInterpreter {

    private val connectTimeoutMs = 15000
    private val readTimeoutMs = 30000

    /** Text-only model: we send OCR text, not the image.
     *  llama3-70b-8192 was removed from Groq; gpt-oss-120b is the current
     *  strongest chat model that returns our JSON shape reliably. */
    private val model = "openai/gpt-oss-120b"

    private val systemPrompt = """You are the medicine-line extractor for a medication reminder app.
You receive raw OCR text from a photographed prescription (it may be noisy, in English or Indian languages, and may contain doctor/clinic headers).

Your job: read the OCR text and decide which lines are medicines, then return exactly one JSON object.

Rules:
- Only include medicines that are actually present in the OCR text. Never invent one.
- "name" is the medicine name ONLY (e.g. "Morphine"), never route/dosage-form words like "Oral", "Tablet", "Syrup" by themselves — if the text says "Morphine Oral", name = "Morphine" and put "oral" in instructions if relevant.
- "dose" is strength/form only, e.g. "13 mg", "500 mg", "1 tablet".
- "frequency" as written: "1-0-1", "1-1-1", "OD", "BD", "TDS", "once daily", etc.
- "times": 24-hour times derived from the frequency. 1-0-1 → ["08:00","20:00"]; 1-1-1 → ["08:00","14:00","20:00"]; OD → ["08:00"]; BD → ["08:00","20:00"]; TDS → ["08:00","14:00","20:00"]; SOS/PRN → [].
- "duration_days": integer days if written, else null.
- "low_confidence_fields": field keys you are unsure about ("name","dose","frequency","duration").
- If a field is not readable use "" and add it to low_confidence_fields.
- Set "handwriting_unclear" true if the text is hard to read.
- Add short "warnings" only when something needs the user's attention.

Return EXACTLY this JSON shape, no markdown, no commentary:
{
  "handwriting_unclear": false,
  "warnings": [],
  "medicines": [
    {
      "name": "Morphine",
      "dose": "13 mg",
      "frequency": "1-0-1",
      "duration_days": null,
      "times": ["08:00", "20:00"],
      "instructions": "after food",
      "low_confidence_fields": []
    }
  ]
}"""

    /**
     * Sends [ocrText] to Groq and returns the AI-decided draft.
     * Returns null on any failure so the caller can fall back.
     */
    suspend fun interpret(bitmap: Bitmap, ocrText: String, groqApiKey: String): PrescriptionDraft? {
        if (groqApiKey.isBlank()) return null
        if (ocrText.isBlank()) return null

        // Build a properly-escaped JSON payload (org.json handles quoting).
        val payload = JSONObject()
            .put("model", model)
            .put(
                "messages",
                JSONArray()
                    .put(JSONObject().put("role", "system").put("content", systemPrompt))
                    .put(
                        JSONObject().put(
                            "role", "user"
                        ).put(
                            "content",
                            "OCR text from the prescription:\n\n$ocrText"
                        )
                    )
            )
            .put("temperature", 0.1)
            .toString()

        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://api.groq.com/openai/v1/chat/completions")
                val conn = url.openConnection() as HttpURLConnection
                try {
                    conn.requestMethod = "POST"
                    conn.connectTimeout = connectTimeoutMs
                    conn.readTimeout = readTimeoutMs
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.setRequestProperty("Authorization", "Bearer $groqApiKey")
                    conn.doOutput = true
                    conn.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }

                    if (conn.responseCode != 200) {
                        val err = readStream(conn.errorStream)
                        android.util.Log.e("GroqInterpreter", "HTTP ${conn.responseCode}: $err")
                        null
                    } else {
                        val body = readStream(conn.inputStream)
                        val content = JSONObject(body)
                            .optJSONArray("choices")
                            ?.optJSONObject(0)
                            ?.optJSONObject("message")
                            ?.optString("content")
                            .orEmpty()
                        parseGroqResponse(content, ocrText)
                    }
                } finally {
                    conn.disconnect()
                }
            } catch (e: Exception) {
                android.util.Log.e("GroqInterpreter", "Groq API error: ${e.message}", e)
                null
            }
        }
    }

    private fun readStream(stream: java.io.InputStream?): String {
        if (stream == null) return ""
        return BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
    }

    /** Parses the model's JSON (tolerates markdown fences or stray prose). */
    private fun parseGroqResponse(content: String, ocrText: String): PrescriptionDraft? {
        try {
            val json = extractJson(content) ?: return null
            val mArray = json.optJSONArray("medicines") ?: return null

            val medicines = mutableListOf<ScannedMedicine>()
            for (i in 0 until mArray.length()) {
                val j = mArray.getJSONObject(i)
                val name = j.optString("name").trim()
                if (name.isBlank()) continue

                val frequency = j.optString("frequency").trim()
                val instructions = j.optString("instructions").trim().takeIf { it.isNotBlank() }
                var times = mutableListOf<String>()
                val tArray = j.optJSONArray("times")
                if (tArray != null) {
                    for (k in 0 until tArray.length()) {
                        val t = tArray.optString(k).trim()
                        if (t.isNotBlank()) times.add(t)
                    }
                }
                // AI didn't give times — derive from frequency the same way
                // the on-device parser does, so reminders still work.
                if (times.isEmpty() && frequency.isNotBlank()) {
                    times = PrescriptionParser.timesForFrequency(frequency, instructions).toMutableList()
                }

                val low = buildSet {
                    val lowArray = j.optJSONArray("low_confidence_fields")
                    if (lowArray != null) {
                        for (k in 0 until lowArray.length()) {
                            lowArray.optString(k).trim().takeIf { it.isNotBlank() }?.let { add(it) }
                        }
                    }
                }
                val missing = buildSet {
                    if (name.isBlank()) add("name")
                    if (j.optString("dose").isBlank()) add("dose")
                    if (frequency.isBlank()) add("frequency")
                    if (!j.has("duration_days") || j.isNull("duration_days")) add("duration")
                }

                medicines += ScannedMedicine(
                    name = name,
                    dose = j.optString("dose").trim(),
                    frequency = frequency,
                    durationDays = if (j.has("duration_days") && !j.isNull("duration_days"))
                        j.optInt("duration_days") else null,
                    times = times,
                    instructions = instructions,
                    lowConfidenceFields = low,
                    missingFields = missing
                )
            }

            if (medicines.isEmpty()) return null

            val warnings = buildList {
                val wArray = json.optJSONArray("warnings")
                if (wArray != null) {
                    for (i in 0 until wArray.length()) {
                        wArray.optString(i).trim().takeIf { it.isNotBlank() }?.let { add(it) }
                    }
                }
                if (medicines.any { it.missingFields.isNotEmpty() }) {
                    add("Some fields could not be read. Check every field before saving.")
                }
                if (medicines.any { it.times.isEmpty() }) {
                    add("A reminder time could not be inferred. Add one before saving.")
                }
            }

            return PrescriptionDraft(
                rawText = ocrText.trim(),
                medicines = medicines,
                warnings = warnings,
                handwritingUnclear = json.optBoolean("handwriting_unclear", false),
                source = ScanSource.AI_ASSISTED,
                aiConfigured = true
            )
        } catch (e: Exception) {
            android.util.Log.e("GroqInterpreter", "Response parse error: ${e.message}", e)
            return null
        }
    }

    /** Pulls the first balanced JSON object out of the model's reply. */
    private fun extractJson(content: String): JSONObject? {
        val start = content.indexOf('{')
        if (start < 0) return null
        var depth = 0
        var inString = false
        var escaped = false
        for (i in start until content.length) {
            val c = content[i]
            when {
                escaped -> escaped = false
                c == '\\' -> escaped = true
                c == '"' -> inString = !inString
                !inString && c == '{' -> depth++
                !inString && c == '}' -> {
                    depth--
                    if (depth == 0) {
                        return JSONObject(content.substring(start, i + 1))
                    }
                }
            }
        }
        return null
    }
}
