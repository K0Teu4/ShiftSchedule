package com.shiftschedule.app

import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.data.model.Template
import com.shiftschedule.app.util.ListUtils
import com.shiftschedule.app.util.StatsUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth

class StatsUtilsTest {
    private val template = Template(1, "D/O", "", "D,O", true)
    private val s1 = Schedule(1, "Я", "#1", 1, "2026-08-01")
    private val s2 = Schedule(2, "Жена", "#2", 1, "2026-08-02")

    @Test
    fun `month stats counts shifts`() {
        val stats = StatsUtils.monthStats(listOf(s1), mapOf(1 to template), listOf(1), YearMonth.of(2026, 8))
        // 31 день: 16 D, 15 O
        assertEquals(16, stats["total_day"])
        assertEquals(15, stats["total_off"])
        assertEquals(0, stats["total_night"])
        assertEquals(128, stats["total_hours"])
    }

    @Test
    fun `shared off counts days when all off`() {
        val stats = StatsUtils.monthStats(listOf(s1, s2), mapOf(1 to template), listOf(1, 2), YearMonth.of(2026, 8))
        // s1: D на нечётных; s2: D на чётных -> общих выходных: нечёт у s1=O(чёт даты), s2=O(нечёт) -> пересечений нет
        assertEquals(0, stats["shared_off"])
    }

    @Test
    fun `year stats sums months`() {
        val year = StatsUtils.yearStats(listOf(s1), mapOf(1 to template), listOf(1), 2026)
        val aug = StatsUtils.monthStats(listOf(s1), mapOf(1 to template), listOf(1), YearMonth.of(2026, 8))
        assertTrue((year["total_day"] ?: 0) >= (aug["total_day"] ?: 0))
    }
}

class ListUtilsTest {
    @Test
    fun `move shifts item`() {
        val list = listOf("a", "b", "c")
        assertEquals(listOf("b", "c", "a"), ListUtils.move(list, 0, 2))
        assertEquals(listOf("c", "a", "b"), ListUtils.move(list, 2, 0))
    }

    @Test
    fun `move with invalid indices returns same`() {
        val list = listOf("a", "b")
        assertEquals(list, ListUtils.move(list, -1, 1))
        assertEquals(list, ListUtils.move(list, 0, 5))
    }
}