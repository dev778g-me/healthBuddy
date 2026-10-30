package org.dev.healthbuddy.scan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PrescriptionParserTest {

    @Test
    fun parsesPrintedMedicineLine() {
        val draft = PrescriptionParser.parse(
            """
            City Clinic
            Patient: Ravi
            Rx
            Tab Paracetamol 500 mg 1-0-1 for 5 days after food
            """.trimIndent()
        )

        assertEquals(1, draft.medicines.size)
        val med = draft.medicines.single()
        assertEquals("Paracetamol", med.name)
        assertEquals("500 mg", med.dose)
        assertEquals("1-0-1", med.frequency)
        assertEquals(5, med.durationDays)
        assertEquals(listOf("08:00", "20:00"), med.times)
        assertTrue(med.instructions.orEmpty().contains("after food"))
    }

    @Test
    fun parsesAbbreviationAndWeekDuration() {
        val draft = PrescriptionParser.parse(
            "Cap Amoxicillin 250 mg BD x 2 weeks"
        )

        val med = draft.medicines.single()
        assertEquals("Amoxicillin", med.name)
        assertEquals("BD", med.frequency)
        assertEquals(14, med.durationDays)
        assertEquals(listOf("08:00", "20:00"), med.times)
    }

    @Test
    fun mapsNumberedSlots() {
        assertEquals(listOf("08:00"), PrescriptionParser.timesForFrequency("1-0-0"))
        assertEquals(listOf("14:00"), PrescriptionParser.timesForFrequency("0-1-0"))
        assertEquals(listOf("20:00"), PrescriptionParser.timesForFrequency("0-0-1"))
        assertEquals(
            listOf("08:00", "14:00", "20:00"),
            PrescriptionParser.timesForFrequency("1-1-1")
        )
        assertEquals(listOf("20:00"), PrescriptionParser.timesForFrequency("OD", "at night"))
        assertEquals(emptyList<String>(), PrescriptionParser.timesForFrequency("SOS"))
    }

    @Test
    fun ignoresHeaderOnlyText() {
        val draft = PrescriptionParser.parse("City Clinic\nPatient: Ravi\nDate: 29/10/2026")
        assertTrue(draft.medicines.isEmpty())
        assertTrue(draft.warnings.isNotEmpty())
    }

    @Test
    fun normalizesIndianNumeralsAndHindiFormPrefix() {
        val draft = PrescriptionParser.parse("टैब पैरासिटामोल ५०० mg १-०-१ for ५ days")
        val med = draft.medicines.single()
        assertEquals("पैरासिटामोल", med.name)
        assertEquals("500 mg", med.dose)
        assertEquals("1-0-1", med.frequency)
        assertEquals(5, med.durationDays)
    }

    @Test
    fun flagsMissingFieldsForReview() {
        val draft = PrescriptionParser.parse("Tab Paracetamol 500 mg")
        val med = draft.medicines.single()
        assertTrue("frequency" in med.missingFields)
        assertTrue("duration" in med.missingFields)
        assertTrue(med.times.isEmpty())
    }
}
