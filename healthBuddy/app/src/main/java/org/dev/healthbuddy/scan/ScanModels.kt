package org.dev.healthbuddy.scan

/** One medicine row extracted from a prescription. Never saved until confirmed. */
data class ScannedMedicine(
    val name: String = "",
    val dose: String = "",
    val frequency: String = "",
    val durationDays: Int? = null,
    val times: List<String> = emptyList(),
    val condition: String = "",
    val instructions: String? = null,
    /** Field keys the interpreter was unsure about: name, dose, frequency, duration. */
    val lowConfidenceFields: Set<String> = emptySet(),
    val missingFields: Set<String> = emptySet()
)

enum class ScanSource { ON_DEVICE, AI_ASSISTED }

/**
 * The review screen's input. [rawText] is the on-device OCR output; it is shown
 * only in the review UI and is not persisted with the medicine.
 */
data class PrescriptionDraft(
    val rawText: String,
    val medicines: List<ScannedMedicine>,
    val warnings: List<String> = emptyList(),
    val handwritingUnclear: Boolean = false,
    val source: ScanSource = ScanSource.ON_DEVICE,
    val aiConfigured: Boolean = false
)

/** Result of the whole capture → OCR → optional AI pipeline. */
sealed interface ScanOutcome {
    data class Success(val draft: PrescriptionDraft) : ScanOutcome
    data class Failure(val message: String) : ScanOutcome
}
