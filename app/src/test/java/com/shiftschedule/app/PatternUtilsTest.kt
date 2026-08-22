package com.shiftschedule.app

import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.data.model.Template
import com.shiftschedule.app.util.PatternUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class PatternUtilsTest {
    private val template = Template(
        id = 1,
        name = "2-2-2",
        description = "",
        pattern = "D,D,N,N,O,O",
        isBuiltIn = true
    )

    private fun schedule(start: String = "2026-08-01") = Schedule(
        id = 1,
        name = "Тест",
        color = "#5856D6",
        templateId = 1,
        startDate = start
    )

    @Test
    fun `basic pattern maps days correctly`() {
        val s = schedule()
        assertEquals("D", PatternUtils.getShiftForDate(s, LocalDate.of(2026, 8, 1), template)?.code)
        assertEquals("D", PatternUtils.getShiftForDate(s, LocalDate.of(2026, 8, 2), template)?.code)
        assertEquals("N", PatternUtils.getShiftForDate(s, LocalDate.of(2026, 8, 3), template)?.code)
        assertEquals("O", PatternUtils.getShiftForDate(s, LocalDate.of(2026, 8, 5), template)?.code)
        assertEquals("D", PatternUtils.getShiftForDate(s, LocalDate.of(2026, 8, 7), template)?.code)
    }

    @Test
    fun `before start date returns null`() {
        val s = schedule()
        assertNull(PatternUtils.getShiftForDate(s, LocalDate.of(2026, 7, 31), template))
    }

    @Test
    fun `manual schedule returns only exceptions`() {
        val s = schedule().copy(templateId = null, exceptions = mapOf("2026-08-03" to "V"))
        assertEquals("V", PatternUtils.getShiftForDate(s, LocalDate.of(2026, 8, 3), null)?.code)
        assertNull(PatternUtils.getShiftForDate(s, LocalDate.of(2026, 8, 4), null))
    }

    @Test
    fun `exception overrides pattern`() {
        val s = schedule().copy(exceptions = mapOf("2026-08-01" to "S"))
        assertEquals("S", PatternUtils.getShiftForDate(s, LocalDate.of(2026, 8, 1), template)?.code)
    }

    @Test
    fun `cycle shift moves pattern after vacation`() {
        // Отпуск 5 дней с 5 августа: после него цикл сдвинут на 5
        val s = schedule().copy(
            exceptions = (0 until 5).associate { i -> "2026-08-0${5 + i}" to "V" },
            cycleShifts = mapOf("2026-08-05" to 5)
        )
        assertEquals("V", PatternUtils.getShiftForDate(s, LocalDate.of(2026, 8, 6), template)?.code)
        // 10 августа: daysDiff=9, минус 5 = 4 -> pattern[4]="O"
        assertEquals("O", PatternUtils.getShiftForDate(s, LocalDate.of(2026, 8, 10), template)?.code)
        // Без сдвига было бы pattern[9%6=3]="N"
    }

    @Test
    fun `applyPeriod sets exceptions and cycle shift`() {
        val s = schedule()
        val updated = PatternUtils.applyPeriod(s, LocalDate.of(2026, 8, 10), 3, "V", true)
        assertEquals("V", updated.exceptions["2026-08-10"])
        assertEquals("V", updated.exceptions["2026-08-11"])
        assertEquals("V", updated.exceptions["2026-08-12"])
        assertNull(updated.exceptions["2026-08-13"])
        assertEquals(3, updated.cycleShifts["2026-08-10"])
    }

    @Test
    fun `applyPeriod without cycle shift keeps cycleShifts empty`() {
        val s = schedule()
        val updated = PatternUtils.applyPeriod(s, LocalDate.of(2026, 8, 10), 2, "S", false)
        assertEquals(0, updated.cycleShifts.size)
        assertEquals("S", updated.exceptions["2026-08-10"])
    }

    @Test
    fun `applyChange this day only`() {
        val s = schedule()
        val updated = PatternUtils.applyChange(s, LocalDate.of(2026, 8, 3), "O", "this_day")
        assertEquals("O", updated.exceptions["2026-08-03"])
        assertEquals(1, updated.exceptions.size)
    }

    @Test
    fun `applyChange entire schedule fills from schedule start`() {
        val s = schedule("2026-08-01")
        val updated = PatternUtils.applyChange(s, LocalDate.of(2026, 8, 3), "N", "entire_schedule")
        assertEquals("N", updated.exceptions["2026-08-01"])
        assertEquals("N", updated.exceptions["2026-08-03"])
    }
    @Test
    fun `overlapping cycle period replaces previous cycle adjustment`() {
        val s = schedule().copy(cycleShifts = mapOf("2026-08-10" to 5))
        val updated = PatternUtils.applyPeriod(s, LocalDate.of(2026, 8, 12), 3, "V", true)
        assertEquals(1, updated.cycleShifts.size)
        assertEquals(3, updated.cycleShifts["2026-08-12"])
    }

    @Test
    fun `applyPeriod without cycle removes overlapping old cycle adjustment`() {
        val s = schedule().copy(cycleShifts = mapOf("2026-08-10" to 3))
        val updated = PatternUtils.applyPeriod(s, LocalDate.of(2026, 8, 11), 2, "S", false)
        assertEquals(0, updated.cycleShifts.size)
    }

}