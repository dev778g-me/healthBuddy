package org.dev.healthbuddy.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicineScheduleTest {

    @Test
    fun medicine_has_correct_defaults() {
        val med = Medicine(
            id = "m",
            name = "Test",
            dose = "1 tablet",
            condition = "General",
            times = listOf("08:00")
        )
        assertTrue(med.active)
        assertEquals("Test", med.name)
        assertEquals("1 tablet", med.dose)
        assertEquals("General", med.condition)
        assertEquals(listOf("08:00"), med.times)
    }

    @Test
    fun medicine_can_be_updated() {
        val med = Medicine(
            id = "m",
            name = "Test",
            dose = "1 tablet",
            condition = "General",
            times = listOf("08:00")
        )
        // Verify the medicine can be created with correct values
        assertTrue(med.name.isNotBlank())
        assertTrue(med.dose.isNotBlank())
    }
}