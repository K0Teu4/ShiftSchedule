package com.shiftschedule.app.domain

import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.data.model.ShiftType
import com.shiftschedule.app.data.model.Template
import com.shiftschedule.app.util.DateUtils
import java.time.LocalDate

/**
 * Single source of truth for resolving a schedule into a shift for a date.
 *
 * The resolver is deliberately independent from Room, Compose and ViewModels so
 * that calendar, statistics, notifications and the widget can all use exactly
 * the same calendar semantics.
 */
object ShiftResolver {
    fun resolve(schedule: Schedule, date: LocalDate, template: Template?): ShiftType? {
        val dateString = DateUtils.formatDate(date)

        // A valid explicit exception always wins over the calculated pattern.
        // Unknown/corrupt exception codes are ignored rather than hiding a valid
        // base pattern forever.
        schedule.exceptions[dateString]
            ?.let(ShiftType::fromCode)
            ?.let { return it }

        val startDate = DateUtils.tryParseDate(schedule.startDate) ?: return null
        if (date.isBefore(startDate)) return null
        if (template == null) return null

        val pattern = template.getPatternList()
            .mapNotNull(ShiftType::fromCode)
        if (pattern.isEmpty()) return null

        var effectiveDayIndex = DateUtils.daysBetween(startDate, date)

        // cycleShifts represents periods that consume days from the base cycle.
        // Map iteration order must not affect the result, so entries are parsed,
        // validated and processed chronologically.
        val cycleRanges = schedule.cycleShifts
            .asSequence()
            .mapNotNull { (rawStart, rawDays) ->
                val cycleStart = DateUtils.tryParseDate(rawStart)
                if (cycleStart == null || rawDays <= 0 || cycleStart.isBefore(startDate)) null
                else cycleStart to rawDays.toLong()
            }
            .sortedBy { it.first }
            .toList()

        var coveredUntil: LocalDate? = null
        cycleRanges.forEach { (cycleStart, cycleDays) ->
            val cycleEnd = cycleStart.plusDays(cycleDays)
            if (!date.isBefore(cycleEnd)) {
                val effectiveStart = coveredUntil?.takeIf { it.isAfter(cycleStart) } ?: cycleStart
                if (effectiveStart.isBefore(cycleEnd)) {
                    effectiveDayIndex -= DateUtils.daysBetween(effectiveStart, cycleEnd)
                }
                if (coveredUntil == null || cycleEnd.isAfter(coveredUntil)) coveredUntil = cycleEnd
            }
        }

        val index = Math.floorMod(effectiveDayIndex, pattern.size.toLong()).toInt()
        return pattern[index]
    }
}
