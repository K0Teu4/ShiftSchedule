package com.shiftschedule.app.util

import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.data.model.Template
import com.shiftschedule.app.domain.ShiftResolver
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
            "total_holiday" to 0,
            "total_sick" to 0,
            "total_vacation" to 0,
            "shared_off" to 0,
            "all_working" to 0,
            "total_hours" to 0,
            "total_salary" to 0
        )

        DateUtils.getDaysInMonth(yearMonth).forEach { date ->
            val shifts = selected.map { schedule ->
                schedule to ShiftResolver.resolve(schedule, date, templates[schedule.templateId])
            }

            shifts.forEach { (schedule, shift) ->
                when (shift?.code) {
                    "O" -> stats["total_off"] = stats.getValue("total_off") + 1
                    "D" -> {
                        stats["total_day"] = stats.getValue("total_day") + 1
                        stats["total_hours"] = stats.getValue("total_hours") + schedule.dayHours
                        stats["total_salary"] = stats.getValue("total_salary") + schedule.dayHours * schedule.hourRate
                    }
                    "N" -> {
                        stats["total_night"] = stats.getValue("total_night") + 1
                        stats["total_hours"] = stats.getValue("total_hours") + schedule.nightHours
                        stats["total_salary"] = stats.getValue("total_salary") + schedule.nightHours * schedule.hourRate
                    }
                    "H" -> stats["total_holiday"] = stats.getValue("total_holiday") + 1
                    "S" -> stats["total_sick"] = stats.getValue("total_sick") + 1
                    "V" -> stats["total_vacation"] = stats.getValue("total_vacation") + 1
                }
            }

            val codes = shifts.map { it.second?.code }
            val offCount = codes.count { it == "O" }
            val dayCount = codes.count { it == "D" }
            val nightCount = codes.count { it == "N" }
            if (selected.size >= 2 && offCount == selected.size) {
                stats["shared_off"] = stats.getValue("shared_off") + 1
            }
            if (selected.isNotEmpty() && dayCount + nightCount == selected.size) {
                stats["all_working"] = stats.getValue("all_working") + 1
            }
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
            "total_off" to 0,
            "total_day" to 0,
            "total_night" to 0,
            "total_holiday" to 0,
            "total_sick" to 0,
            "total_vacation" to 0,
            "shared_off" to 0,
            "all_working" to 0,
            "total_hours" to 0,
            "total_salary" to 0
        )
        for (month in 1..12) {
            val stats = monthStats(schedules, templates, scheduleIds, YearMonth.of(year, month))
            stats.forEach { (key, value) -> totals[key] = totals.getValue(key) + value }
        }
        return totals
    }
}
