package org.dev.healthbuddy.scan

/**
 * Deterministic, offline parser for printed prescriptions.
 *
 * It is deliberately conservative: if a line does not look like a medicine it
 * is left out, and every uncertain field is marked for the mandatory review.
 * Handwriting is handled best-effort by the optional Gemini interpreter.
 */
object PrescriptionParser {

    private val FREQ_321 = Regex("""\b([0-2])\s*[-–—]\s*([0-2])\s*[-–—]\s*([0-2])\b""")
    private val FREQ_2 = Regex("""\b([0-2])\s*[-–—]\s*([0-2])\b""")
    private val FREQ_ABBR = Regex(
        """(?i)\b(OD|O\.D\.|BD|B\.D\.|TDS|T\.D\.S\.|TID|QID|Q\.I\.D\.|SOS|PRN|HS|NOCTE|STAT)\b"""
    )
    private val FREQ_WORDS = Regex(
        """(?i)\b(once\s+(?:a\s+)?daily|once\s+a\s+day|twice\s+(?:a\s+)?daily|twice\s+a\s+day|""" +
            """thrice\s+(?:a\s+)?daily|three\s+times\s+(?:a\s+)?day|four\s+times\s+(?:a\s+)?day|daily|bedtime|at\s+night)\b"""
    )
    private val DOSE = Regex(
        """(?i)\b(\d+(?:\.\d+)?)\s*(mg|mcg|µg|g|ml|iu|units?|tabs?|tablets?|caps?|capsules?|drops?|puffs?)\b"""
    )
    private val DURATION = Regex(
        """(?i)\b(?:for|x|×)?\s*(\d+)\s*(day|days|week|weeks|month|months)\b"""
    )
    private val SERIAL = Regex("""^\s*\d+[\).\-:]\s*""")
    private val FORM = Regex(
        """(?i)^(tab|tab\.|tablet|cap|cap\.|capsule|syp|syrup|inj|injection|oint|ointment|drops?|spray|inhaler|टैब|टैबलेट|कैप|कैप्सूल|ଟାବ|ଟାବଲେଟ|କ୍ୟାପ୍)\s*[:\-]?\s*"""
    )
    private val NOISE = Regex("""[|,;:•*_]+""")
    private val SPACES = Regex("""\s+""")

    private val INSTRUCTIONS = listOf(
        Regex("""(?i)\bafter\s+(?:food|meals?)\b"""),
        Regex("""(?i)\bbefore\s+(?:food|meals?)\b"""),
        Regex("""(?i)\bwith\s+(?:food|meals?)\b"""),
        Regex("""(?i)\bempty\s+stomach\b"""),
        Regex("""(?i)\bat\s+bedtime\b"""),
        Regex("""(?i)\bat\s+night\b"""),
        Regex("""(?i)\bin\s+the\s+morning\b"""),
        Regex("""(?i)\bas\s+needed\b"""),
        Regex("""(?i)\bsos\b""")
    )

    private val HEADER_WORDS = listOf(
        "prescription", "doctor", "clinic", "hospital", "patient", "date",
        "age", "sex", "address", "diagnosis", "advice", "follow up",
        "signature", "registration", "reg no", "phone", "mobile", "rx"
    )

    fun parse(raw: String): PrescriptionDraft {
        val lines = raw.lines()
            .map { normalizeDigits(it).replace(SPACES, " ").trim() }
            .filter { it.isNotBlank() }

        val found = mutableListOf<ScannedMedicine>()

        lines.forEach { line ->
            if (isHeader(line)) return@forEach

            val freq321 = FREQ_321.find(line)
            val freq2 = if (freq321 == null) FREQ_2.find(line) else null
            val freqAbbr = FREQ_ABBR.find(line)
            val freqWords = FREQ_WORDS.find(line)
            val dose = DOSE.find(line)
            val duration = DURATION.find(line)
            val hasForm = FORM.containsMatchIn(line)

            // A real medicine line needs at least one prescription signal.
            if (freq321 == null && freq2 == null && freqAbbr == null &&
                freqWords == null && dose == null && !hasForm
            ) {
                return@forEach
            }

            val frequency = when {
                freq321 != null -> {
                    val (a, b, c) = freq321.destructured
                    "$a-$b-$c"
                }
                freq2 != null -> {
                    val (a, b) = freq2.destructured
                    "$a-$b"
                }
                freqAbbr != null -> canonicalAbbreviation(freqAbbr.value)
                freqWords != null -> canonicalWords(freqWords.value)
                else -> ""
            }

            val instruction = extractInstructions(line)
            var name = line
            listOf(FREQ_321, FREQ_2, FREQ_ABBR, FREQ_WORDS, DOSE, DURATION)
                .forEach { regex -> name = regex.replace(name, " ") }
            INSTRUCTIONS.forEach { regex -> name = regex.replace(name, " ") }
            name = SERIAL.replace(name, "")
            name = FORM.replace(name, "")
            name = NOISE.replace(name, " ")
            name = name.replace(SPACES, " ")
                .trim(' ', '-', '.', ':', '/')

            if (name.length < 2 || name.all { it.isDigit() }) return@forEach
            if (name.lowercase() in HEADER_WORDS) return@forEach

            val missing = buildSet {
                if (dose == null) add("dose")
                if (frequency.isBlank()) add("frequency")
                if (duration == null) add("duration")
            }
            val low = buildSet {
                addAll(missing)
                if (name.length <= 3 || line.contains("?")) add("name")
            }

            found += ScannedMedicine(
                name = name,
                dose = dose?.value?.replace(SPACES, " ")?.trim().orEmpty(),
                frequency = frequency,
                durationDays = duration?.let { durationDays(it) },
                times = timesForFrequency(frequency, instruction),
                instructions = instruction,
                lowConfidenceFields = low,
                missingFields = missing
            )
        }

        val medicines = found.distinctBy { it.name.lowercase() to it.times }
        val warnings = buildList {
            if (medicines.isEmpty()) {
                add("No medicine lines were found. Try a clearer photo or enter the medicine manually.")
            } else {
                if (medicines.any { it.missingFields.isNotEmpty() }) {
                    add("Some fields could not be read. Check every field before saving.")
                }
                if (medicines.any { it.times.isEmpty() }) {
                    add("A reminder time could not be inferred. Add one before saving.")
                }
            }
        }

        return PrescriptionDraft(
            rawText = raw.trim(),
            medicines = medicines,
            warnings = warnings,
            source = ScanSource.ON_DEVICE
        )
    }

    /** Maps common prescription shorthand to reminder times. */
    fun timesForFrequency(frequency: String, instructions: String? = null): List<String> {
        val text = frequency.uppercase().replace(".", "").trim()
        val night = instructions?.lowercase()?.let {
            it.contains("night") || it.contains("bedtime")
        } == true

        val numbered = Regex("""^([0-2])-([0-2])-([0-2])$""").matchEntire(text)
        if (numbered != null) {
            val (a, b, c) = numbered.destructured
            return numberedTimes(a.toInt(), b.toInt(), c.toInt())
        }
        val two = Regex("""^([0-2])-([0-2])$""").matchEntire(text)
        if (two != null) {
            val (a, b) = two.destructured
            return numberedTimes(a.toInt(), 0, b.toInt())
        }

        return when (text) {
            "OD", "DAILY", "ONCE" -> listOf(if (night) "20:00" else "08:00")
            "BD", "TWICE" -> listOf("08:00", "20:00")
            "TDS", "TID", "THRICE" -> listOf("08:00", "14:00", "20:00")
            "QID" -> listOf("08:00", "12:00", "16:00", "20:00")
            "HS", "NOCTE" -> listOf("20:00")
            "SOS", "PRN", "STAT" -> emptyList()
            else -> emptyList()
        }
    }

    private fun numberedTimes(morning: Int, afternoon: Int, evening: Int): List<String> =
        buildList {
            if (morning > 0) add("08:00")
            if (afternoon > 0) add("14:00")
            if (evening > 0) add("20:00")
        }

    private fun durationDays(match: MatchResult): Int? {
        val count = match.groupValues[1].toIntOrNull() ?: return null
        val unit = match.groupValues[2].lowercase()
        return when {
            unit.startsWith("day") -> count
            unit.startsWith("week") -> count * 7
            unit.startsWith("month") -> count * 30
            else -> null
        }
    }

    /** Devanagari/Odia numerals are common on Indian prescriptions. */
    private fun normalizeDigits(text: String): String = buildString(text.length) {
        text.forEach { c ->
            when (c) {
                in '\u0966'..'\u096F' -> append('0' + (c - '\u0966'))
                in '\u0B66'..'\u0B6F' -> append('0' + (c - '\u0B66'))
                else -> append(c)
            }
        }
    }

    private fun canonicalAbbreviation(raw: String): String =
        raw.uppercase().replace(".", "").trim()

    private fun canonicalWords(raw: String): String {
        val text = raw.lowercase()
        return when {
            text.contains("once") || text == "daily" -> "OD"
            text.contains("twice") -> "BD"
            text.contains("thrice") || text.contains("three") -> "TDS"
            text.contains("four") -> "QID"
            text.contains("night") || text.contains("bedtime") -> "HS"
            else -> raw.uppercase()
        }
    }

    private fun extractInstructions(line: String): String? =
        INSTRUCTIONS.mapNotNull { regex ->
            regex.find(line)?.value?.lowercase()?.replace(SPACES, " ")
        }.distinct().joinToString(", ").ifBlank { null }

    private fun isHeader(line: String): Boolean {
        val lower = line.lowercase()
        return HEADER_WORDS.any { lower == it || lower.startsWith("$it ") || lower.startsWith("$it:") }
    }
}
