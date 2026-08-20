package com.shiftschedule.app.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth

class DateUtilsTest {
    @Test
    fun daysInMonth() {
        assertEquals(31, DateUtils.getDaysInMonth(YearMonth.of(2026, 8)).size)
        assertEquals(28, DateUtils.getDaysInMonth(YearMonth.of(2026, 2)).size)
    }

    @Test
    fun weekStartOffset() {
        // 1 августа 2026 — суббота
        assertEquals(5, DateUtils.getFirstDayOffset(YearMonth.of(2026, 8), "mon"))
        assertEquals(6, DateUtils.getFirstDayOffset(YearMonth.of(2026, 8), "sun"))
    }
}
