package com.shiftschedule.app.data.repository

import com.shiftschedule.app.data.local.ShiftDao
import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.data.model.Template
import kotlinx.coroutines.flow.Flow

class ShiftRepository(private val shiftDao: ShiftDao) {
    val allSchedules: Flow<List<Schedule>> = shiftDao.getAllSchedules()
    val allTemplates: Flow<List<Template>> = shiftDao.getAllTemplates()

    suspend fun getScheduleById(id: Int): Schedule? = shiftDao.getScheduleById(id)
    suspend fun insertSchedule(schedule: Schedule): Long = shiftDao.insertSchedule(schedule)
    suspend fun updateSchedule(schedule: Schedule) = shiftDao.updateSchedule(schedule)
    suspend fun deleteSchedule(schedule: Schedule) = shiftDao.deleteSchedule(schedule)
    suspend fun deleteAllSchedules() = shiftDao.deleteAllSchedules()
    suspend fun getMaxScheduleSortIndex(): Int = shiftDao.getMaxScheduleSortIndex()
    suspend fun getMaxTemplateSortIndex(): Int = shiftDao.getMaxTemplateSortIndex()

    suspend fun getTemplateById(id: Int): Template? = shiftDao.getTemplateById(id)
    suspend fun insertTemplate(template: Template): Long = shiftDao.insertTemplate(template)
    suspend fun updateTemplate(template: Template) = shiftDao.updateTemplate(template)
    suspend fun deleteTemplate(template: Template) = shiftDao.deleteTemplate(template)
    suspend fun deleteAllTemplates() = shiftDao.deleteAllTemplates()
}