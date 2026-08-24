package com.shiftschedule.app.util

import com.shiftschedule.app.data.model.Schedule
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth

class StatsUtilsTest {
    private fun manual(id: Int, ex: Map<String, String>) = Schedule(
        id = id, name = "T$id", color = "#5856D6", templateId = null,
        startDate = "2026-08-01", exceptions = ex
    )

    @Test
    fun monthCountsAllShiftTypes() {
        val s = manual(1, mapOf(
            "2026-08-01" to "D", "2026-08-02" to "N", "2026-08-03" to "O",
            "2026-08-04" to "H", "2026-08-05" to "S", "2026-08-06" to "V"
        ))
        val st = StatsUtils.monthStats(listOf(s), emptyMap(), listOf(1), YearMonth.of(2026, 8))
        assertEquals(1, st["total_day"]); assertEquals(1, st["total_night"])
        assertEquals(1, st["total_off"]); assertEquals(1, st["total_holiday"])
        assertEquals(1, st["total_sick"]); assertEquals(1, st["total_vacation"])
    }

    @Test
    fun sharedOffNeedsTwoSchedules() {
        val a = manual(1, mapOf("2026-08-10" to "O"))
        val b = manual(2, mapOf("2026-08-10" to "O"))
        assertEquals(0, StatsUtils.monthStats(listOf(a), emptyMap(), listOf(1), YearMonth.of(2026, 8))["shared_off"])
        assertEquals(1, StatsUtils.monthStats(listOf(a, b), emptyMap(), listOf(1, 2), YearMonth.of(2026, 8))["shared_off"])
    }

    @Test
    fun commonWorkingRequiresSameShift() {
        val day = manual(1, mapOf("2026-08-10" to "D"))
        val day2 = manual(2, mapOf("2026-08-10" to "D"))
        val night = manual(3, mapOf("2026-08-10" to "N"))

        val sameDay = StatsUtils.monthStats(listOf(day, day2), emptyMap(), listOf(1, 2), YearMonth.of(2026, 8))
        assertEquals(1, sameDay["shared_day"])
        assertEquals(0, sameDay["shared_night"])
        assertEquals(1, sameDay["all_working"])

        val mixed = StatsUtils.monthStats(listOf(day, night), emptyMap(), listOf(1, 3), YearMonth.of(2026, 8))
        assertEquals(0, mixed["shared_day"])
        assertEquals(0, mixed["shared_night"])
        assertEquals(0, mixed["all_working"])
    }

    @Test
    fun yearSumsMonths() {
        val s = manual(1, mapOf("2026-08-01" to "D", "2026-09-01" to "D"))
        assertEquals(2, StatsUtils.yearStats(listOf(s), emptyMap(), listOf(1), 2026)["total_day"])
    }
}
