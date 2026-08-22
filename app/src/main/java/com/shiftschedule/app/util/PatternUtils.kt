package com.shiftschedule.app.util

import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.data.model.ShiftType
import com.shiftschedule.app.data.model.Template
import com.shiftschedule.app.domain.ShiftResolver
import java.time.LocalDate

/**
 * Compatibility facade for existing callers. New domain code should use
 * ShiftResolver directly.
 */
object PatternUtils {
    fun getShiftForDate(
        schedule: Schedule,
        date: LocalDate,
        template: Template?
    ): ShiftType? = ShiftResolver.resolve(schedule, date, template)

    fun applyChange(
        schedule: Schedule,
        startDate: LocalDate,
        shiftCode: String,
        applyRange: String
    ): Schedule {
        val normalizedCode = ShiftType.fromCode(shiftCode)?.code ?: return schedule
        val newExceptions = schedule.exceptions.toMutableMap()

        when (applyRange) {
            "this_day" -> {
                newExceptions[DateUtils.formatDate(startDate)] = normalizedCode
            }
            "this_and_following" -> {
                var date = startDate
                val maxDate = startDate.plusYears(5)
                while (date.isBefore(maxDate)) {
                    newExceptions[DateUtils.formatDate(date)] = normalizedCode
                    date = date.plusDays(1)
                }
            }
            "entire_schedule" -> {
                val scheduleStart = DateUtils.tryParseDate(schedule.startDate) ?: return schedule
                var date = scheduleStart
                val maxDate = scheduleStart.plusYears(5)
                while (date.isBefore(maxDate)) {
                    newExceptions[DateUtils.formatDate(date)] = normalizedCode
                    date = date.plusDays(1)
                }
            }
            else -> return schedule
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
        val normalizedCode = ShiftType.fromCode(shiftCode)?.code ?: return schedule
        if (days <= 0) return schedule

        val newExceptions = schedule.exceptions.toMutableMap()
        repeat(days) { index ->
            newExceptions[DateUtils.formatDate(startDate.plusDays(index.toLong()))] = normalizedCode
        }

        val newEnd = startDate.plusDays(days.toLong())
        val nonOverlappingShifts = schedule.cycleShifts.filter { (rawStart, existingDays) ->
            val existingStart = DateUtils.tryParseDate(rawStart) ?: return@filter false
            val existingEnd = existingStart.plusDays(existingDays.toLong())
            existingEnd <= startDate || newEnd <= existingStart
        }
        val newShifts = if (shiftCycle) {
            nonOverlappingShifts.toMutableMap().apply {
                put(DateUtils.formatDate(startDate), days)
            }
        } else {
            nonOverlappingShifts
        }

        return schedule.copy(exceptions = newExceptions, cycleShifts = newShifts)
    }
}
