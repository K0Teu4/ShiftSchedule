package com.shiftschedule.app.data.model

data class BackupData(
    val version: Int = 1,
    val schedules: List<Schedule> = emptyList(),
    val templates: List<Template> = emptyList()
)