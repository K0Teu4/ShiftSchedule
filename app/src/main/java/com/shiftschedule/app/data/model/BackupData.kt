package com.shiftschedule.app.data.model

data class BackupData(
    val version: Int = CURRENT_VERSION,
    val schedules: List<Schedule> = emptyList(),
    val templates: List<Template> = emptyList()
) {
    companion object {
        const val CURRENT_VERSION = 2
        const val MIN_SUPPORTED_VERSION = 1
    }
}
