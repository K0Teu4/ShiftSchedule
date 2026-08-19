package com.shiftschedule.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "schedules")
@TypeConverters(ExceptionsConverter::class, CycleShiftsConverter::class)
data class Schedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val color: String,
    val templateId: Int?,
    val startDate: String,
    val isActive: Boolean = true,
    val exceptions: Map<String, String> = emptyMap(),
    val cycleShifts: Map<String, Int> = emptyMap(),
    val sortIndex: Int = 0
)

class ExceptionsConverter {
    private val gson = Gson()
    @TypeConverter
    fun fromExceptions(exceptions: Map<String, String>): String = gson.toJson(exceptions)
    @TypeConverter
    fun toExceptions(json: String): Map<String, String> {
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }
}

class CycleShiftsConverter {
    private val gson = Gson()
    @TypeConverter
    fun fromCycleShifts(shifts: Map<String, Int>): String = gson.toJson(shifts)
    @TypeConverter
    fun toCycleShifts(json: String): Map<String, Int> {
        val type = object : TypeToken<Map<String, Int>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }
}