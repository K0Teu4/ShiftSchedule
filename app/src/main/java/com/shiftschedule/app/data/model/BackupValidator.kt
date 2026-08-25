package com.shiftschedule.app.data.model

import com.shiftschedule.app.util.DateUtils

object BackupValidator {
    private val builtInIds = Template.getBuiltInTemplates().map { it.id }.toSet()
    private val validShiftCodes = ShiftType.values().map { it.code }.toSet()

    fun validate(data: BackupData): String? {
        if (data.version !in BackupData.MIN_SUPPORTED_VERSION..BackupData.CURRENT_VERSION) {
            return "Unsupported backup version: ${data.version}"
        }
        if (data.schedules.any { it.id <= 0 }) return "Invalid schedule id"
        if (data.templates.any { it.id <= 0 }) return "Invalid template id"

        val scheduleIds = data.schedules.map { it.id }
        if (scheduleIds.size != scheduleIds.toSet().size) return "Duplicate schedule ids"

        val templateIds = data.templates.map { it.id }
        if (templateIds.size != templateIds.toSet().size) return "Duplicate template ids"

        val templateById = data.templates.associateBy { it.id }
        if (data.templates.any { it.id in builtInIds && !it.isBuiltIn }) {
            return "Built-in template id collision"
        }
        data.templates.forEach { template ->
            if (template.name.trim().isEmpty()) return "Template name is empty"
            if (template.name.length > 120 || template.description.length > 500) return "Template text is too long"
            val codes = template.getPatternList()
            if (codes.size > 366) return "Template pattern is too long"
            if (codes.isEmpty() || codes.any { it !in validShiftCodes }) {
                return "Template '${template.name}' contains invalid shift codes"
            }
        }

        data.schedules.forEach { schedule ->
            if (schedule.name.trim().isEmpty()) return "Schedule name is empty"
            if (schedule.name.length > 120) return "Schedule name is too long"
            if (!schedule.color.matches(Regex("^#[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?$"))) return "Invalid schedule color"
            if (DateUtils.tryParseDate(schedule.startDate) == null) return "Invalid schedule start date"
            if (schedule.hourRate !in 0..1_000_000) return "Invalid hourly rate"
            if (schedule.dayHours !in 1..24 || schedule.nightHours !in 1..24) return "Invalid shift hours"
            if (schedule.templateId != null && schedule.templateId != 0 && schedule.templateId !in templateById) {
                if (schedule.templateId !in builtInIds) return "Missing template ${schedule.templateId}"
            }
            if (schedule.exceptions.entries.any { (date, code) ->
                    DateUtils.tryParseDate(date) == null || code !in validShiftCodes
                }) return "Invalid schedule exception"
            if (schedule.cycleShifts.entries.any { (date, days) ->
                    DateUtils.tryParseDate(date) == null || days <= 0
                }) return "Invalid cycle shift"
            val cycleRanges = schedule.cycleShifts.mapNotNull { (date, days) ->
                DateUtils.tryParseDate(date)?.let { start -> start to start.plusDays(days.toLong()) }
            }.sortedBy { it.first }
            if (cycleRanges.zipWithNext().any { (a, b) -> b.first.isBefore(a.second) }) {
                return "Overlapping cycle shifts"
            }
        }

        return null
    }
}
