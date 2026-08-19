package com.shiftschedule.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.data.model.Template
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftDao {
    @Query("SELECT * FROM schedules ORDER BY sortIndex ASC, isActive DESC, name")
    fun getAllSchedules(): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getScheduleById(id: Int): Schedule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: Schedule): Long

    @Update
    suspend fun updateSchedule(schedule: Schedule)

    @Delete
    suspend fun deleteSchedule(schedule: Schedule)

    @Query("DELETE FROM schedules")
    suspend fun deleteAllSchedules()

    @Query("SELECT COALESCE(MAX(sortIndex), 0) FROM schedules")
    suspend fun getMaxScheduleSortIndex(): Int

    @Query("SELECT COALESCE(MAX(sortIndex), 0) FROM templates")
    suspend fun getMaxTemplateSortIndex(): Int

    @Query("SELECT * FROM templates ORDER BY isBuiltIn DESC, sortIndex ASC, name")
    fun getAllTemplates(): Flow<List<Template>>

    @Query("SELECT * FROM templates WHERE id = :id")
    suspend fun getTemplateById(id: Int): Template?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: Template): Long

    @Update
    suspend fun updateTemplate(template: Template)

    @Delete
    suspend fun deleteTemplate(template: Template)

    @Query("DELETE FROM templates")
    suspend fun deleteAllTemplates()
}