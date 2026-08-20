package com.shiftschedule.app.util

import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.data.model.Template
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class PatternUtilsTest {
    private val template = Template(id = 1, name = "t", description = "", pattern = "D,N,O")
    private val schedule = Schedule(id = 1, name = "s", color = "#000000", templateId = 1, startDate = "2026-08-01")

    @Test
    fun cycleRepeats() {
        assertEquals("D", PatternUtils.getShiftForDate(schedule, LocalDate.of(2026, 8, 1), template)?.code)
        assertEquals("N", PatternUtils.getShiftForDate(schedule, LocalDate.of(2026, 8, 2), template)?.code)
        assertEquals("O", PatternUtils.getShiftForDate(schedule, LocalDate.of(2026, 8, 3), template)?.code)
        assertEquals("D", PatternUtils.getShiftForDate(schedule, LocalDate.of(2026, 8, 4), template)?.code)
    }

    @Test
    fun exceptionOverridesTemplate() {
        val s = schedule.copy(exceptions = mapOf("2026-08-01" to "H"))
        assertEquals("H", PatternUtils.getShiftForDate(s, LocalDate.of(2026, 8, 1), template)?.code)
    }
}
