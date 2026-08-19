package com.shiftschedule.app.util

import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.data.model.ShiftType
import com.shiftschedule.app.data.model.Template
import java.time.LocalDate

object PatternUtils {
    fun getShiftForDate(
        schedule: Schedule,
        date: LocalDate,
        template: Template?
    ): ShiftType? {
        val dateString = DateUtils.formatDate(date)

        schedule.exceptions[dateString]?.let { code ->
            return ShiftType.values().find { it.code == code }
        }

        if (template == null) return null

        val startDate = DateUtils.parseDate(schedule.startDate)
        var daysDiff = DateUtils.daysBetween(startDate, date).toInt()

        if (daysDiff < 0) return null

        for ((shiftStart, shiftDays) in schedule.cycleShifts) {
            val shiftStartDate = DateUtils.parseDate(shiftStart)
            if (!date.isBefore(shiftStartDate.plusDays(shiftDays.toLong()))) {
                daysDiff -= shiftDays
            }
        }

        val patternList = template.getPatternList()
        if (patternList.isEmpty()) return null

        val size = patternList.size
        val index = ((daysDiff % size) + size) % size
        return ShiftType.values().find { it.code == patternList[index] }
    }

    fun applyChange(
        schedule: Schedule,
        startDate: LocalDate,
        shiftCode: String,
        applyRange: String
    ): Schedule {
        val newExceptions = schedule.exceptions.toMutableMap()

        when (applyRange) {
            "this_day" -> {
                newExceptions[DateUtils.formatDate(startDate)] = shiftCode
            }
            "this_and_following" -> {
                var date = startDate
                val maxDate = startDate.plusYears(5)
                while (date.isBefore(maxDate)) {
                    newExceptions[DateUtils.formatDate(date)] = shiftCode
                    date = date.plusDays(1)
                }
            }
            "entire_schedule" -> {
                var date = DateUtils.parseDate(schedule.startDate)
                val maxDate = date.plusYears(5)
                while (date.isBefore(maxDate)) {
                    newExceptions[DateUtils.formatDate(date)] = shiftCode
                    date = date.plusDays(1)
                }
            }
        }

        return schedule.copy(exceptions = newExceptions)
    }

    fun applyPeriod(
        schedule: Schedule,
        startDate: LocalDate,
        days: Int,
        shiftCode: String,
        shiftCycle: Boolean
    ): Schedule {
        val newExceptions = schedule.exceptions.toMutableMap()
        for (i in 0 until days) {
            newExceptions[DateUtils.formatDate(startDate.plusDays(i.toLong()))] = shiftCode
        }

        val newShifts = if (shiftCycle) {
            schedule.cycleShifts + (DateUtils.formatDate(startDate) to days)
        } else {
            schedule.cycleShifts
        }

        return schedule.copy(exceptions = newExceptions, cycleShifts = newShifts)
    }
}