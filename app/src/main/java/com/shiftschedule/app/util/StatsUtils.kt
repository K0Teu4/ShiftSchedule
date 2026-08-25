package com.shiftschedule.app.util

import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.data.model.ShiftType
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
            "total_24" to 0,
            "total_work" to 0,
            "total_holiday" to 0,
            "total_sick" to 0,
            "total_vacation" to 0,
            "shared_off" to 0,
            "shared_day" to 0,
            "shared_night" to 0,
            "all_working" to 0
        )

        DateUtils.getDaysInMonth(yearMonth).forEach { date ->
            val shifts = selected.map { schedule ->
                ShiftResolver.resolve(schedule, date, templates[schedule.templateId])
            }

            shifts.forEach { shift ->
                when (shift) {
                    ShiftType.OFF -> stats["total_off"] = stats.getValue("total_off") + 1
                    ShiftType.DAY -> {
                        stats["total_day"] = stats.getValue("total_day") + 1
                        stats["total_work"] = stats.getValue("total_work") + 1
                    }
                    ShiftType.NIGHT -> {
                        stats["total_night"] = stats.getValue("total_night") + 1
                        stats["total_work"] = stats.getValue("total_work") + 1
                    }
                    ShiftType.TWENTY_FOUR -> {
                        stats["total_24"] = stats.getValue("total_24") + 1
                        stats["total_day"] = stats.getValue("total_day") + 1
                        stats["total_night"] = stats.getValue("total_night") + 1
                        stats["total_work"] = stats.getValue("total_work") + 1
                    }
                    ShiftType.HOLIDAY -> stats["total_holiday"] = stats.getValue("total_holiday") + 1
                    ShiftType.SICK -> stats["total_sick"] = stats.getValue("total_sick") + 1
                    ShiftType.VACATION -> stats["total_vacation"] = stats.getValue("total_vacation") + 1
                    null -> Unit
                }
            }

            if (selected.size >= 2) {
                if (shifts.all { it == ShiftType.OFF }) stats["shared_off"] = stats.getValue("shared_off") + 1
                if (shifts.all { it?.isDayLike == true }) stats["shared_day"] = stats.getValue("shared_day") + 1
                if (shifts.all { it?.isNightLike == true }) stats["shared_night"] = stats.getValue("shared_night") + 1
            }
        }
        stats["all_working"] = stats.getValue("shared_day") + stats.getValue("shared_night")
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
            "total_24" to 0,
            "total_work" to 0,
            "total_holiday" to 0,
            "total_sick" to 0,
            "total_vacation" to 0,
            "shared_off" to 0,
            "shared_day" to 0,
            "shared_night" to 0,
            "all_working" to 0
        )
        for (month in 1..12) {
            val stats = monthStats(schedules, templates, scheduleIds, YearMonth.of(year, month))
            stats.forEach { (key, value) -> totals[key] = totals.getValue(key) + value }
        }
        return totals
    }
}
