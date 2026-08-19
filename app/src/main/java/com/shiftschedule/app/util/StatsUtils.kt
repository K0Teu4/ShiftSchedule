package com.shiftschedule.app.util

import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.data.model.Template
import java.time.YearMonth

object StatsUtils {
    fun monthStats(
        schedules: List<Schedule>,
        templates: Map<Int, Template>,
        scheduleIds: List<Int>,
        yearMonth: YearMonth
    ): Map<String, Int> {
        val selected = schedules.filter { it.id in scheduleIds }

        val stats = mutableMapOf(
            "total_off" to 0,
            "total_day" to 0,
            "total_night" to 0,
            "shared_off" to 0,
            "all_working" to 0
        )

        DateUtils.getDaysInMonth(yearMonth).forEach { date ->
            val codes = selected.map { schedule ->
                PatternUtils.getShiftForDate(schedule, date, templates[schedule.templateId])?.code
            }

            val offCount = codes.count { it == "O" }
            val dayCount = codes.count { it == "D" }
            val nightCount = codes.count { it == "N" }

            stats["total_off"] = stats["total_off"]!! + offCount
            stats["total_day"] = stats["total_day"]!! + dayCount
            stats["total_night"] = stats["total_night"]!! + nightCount
            if (selected.isNotEmpty() && offCount == selected.size) stats["shared_off"] = stats["shared_off"]!! + 1
            if (selected.isNotEmpty() && dayCount + nightCount == selected.size) stats["all_working"] = stats["all_working"]!! + 1
        }

        return stats
    }

    fun yearStats(
        schedules: List<Schedule>,
        templates: Map<Int, Template>,
        scheduleIds: List<Int>,
        year: Int
    ): Map<String, Int> {
        val totals = mutableMapOf(
            "total_off" to 0, "total_day" to 0, "total_night" to 0, "shared_off" to 0, "all_working" to 0
        )
        for (month in 1..12) {
            val m = monthStats(schedules, templates, scheduleIds, YearMonth.of(year, month))
            m.forEach { (k, v) -> totals[k] = (totals[k] ?: 0) + v }
        }
        return totals
    }
}