package com.shiftschedule.app

import android.content.Context
import com.shiftschedule.app.data.local.SettingsDataStore
import com.shiftschedule.app.data.local.ShiftDao
import com.shiftschedule.app.data.model.AppSettings
import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.data.model.Template
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeShiftDao : ShiftDao {
    private val schedules = mutableListOf<Schedule>()
    private val templates = mutableListOf<Template>()
    private val schedulesFlow = MutableStateFlow<List<Schedule>>(emptyList())
    private val templatesFlow = MutableStateFlow<List<Template>>(emptyList())

    private fun emitS() { schedulesFlow.value = schedules.sortedBy { it.sortIndex }.toList() }
    private fun emitT() { templatesFlow.value = templates.sortedBy { it.sortIndex }.toList() }

    override fun getAllSchedules(): Flow<List<Schedule>> = schedulesFlow
    override suspend fun getScheduleById(id: Int): Schedule? = schedules.find { it.id == id }
    override suspend fun insertSchedule(schedule: Schedule): Long {
        val id = if (schedule.id == 0) (schedules.maxOfOrNull { it.id } ?: 0) + 1 else schedule.id
        schedules.removeAll { it.id == id }
        schedules.add(schedule.copy(id = id))
        emitS()
        return id.toLong()
    }
    override suspend fun updateSchedule(schedule: Schedule) {
        val i = schedules.indexOfFirst { it.id == schedule.id }
        if (i >= 0) schedules[i] = schedule
        emitS()
    }
    override suspend fun deleteSchedule(schedule: Schedule) {
        schedules.removeAll { it.id == schedule.id }
        emitS()
    }
    override suspend fun deleteAllSchedules() { schedules.clear(); emitS() }
    override suspend fun getMaxScheduleSortIndex(): Int = schedules.maxOfOrNull { it.sortIndex } ?: 0
    override suspend fun getMaxTemplateSortIndex(): Int = templates.maxOfOrNull { it.sortIndex } ?: 0
    override fun getAllTemplates(): Flow<List<Template>> = templatesFlow
    override suspend fun getTemplateById(id: Int): Template? = templates.find { it.id == id }
    override suspend fun insertTemplate(template: Template): Long {
        val id = if (template.id == 0) (templates.maxOfOrNull { it.id } ?: 0) + 1 else template.id
        templates.removeAll { it.id == id }
        templates.add(template.copy(id = id))
        emitT()
        return id.toLong()
    }
    override suspend fun updateTemplate(template: Template) {
        val i = templates.indexOfFirst { it.id == template.id }
        if (i >= 0) templates[i] = template
        emitT()
    }
    override suspend fun deleteTemplate(template: Template) {
        templates.removeAll { it.id == template.id }
        emitT()
    }
    override suspend fun deleteAllTemplates() { templates.clear(); emitT() }
}

class FakeSettingsStore : SettingsDataStore(mockk<Context>(relaxed = true)) {
    private val flow = MutableStateFlow(AppSettings())
    override val settingsFlow: Flow<AppSettings> = flow
    override suspend fun updateSettings(settings: AppSettings) {
        flow.value = settings
    }
}