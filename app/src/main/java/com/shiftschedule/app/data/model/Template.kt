package com.shiftschedule.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "templates")
data class Template(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val pattern: String,
    val isBuiltIn: Boolean = false,
    val sortIndex: Int = 0
) {
    fun getPatternList(): List<String> = pattern.split(",").filter { it.isNotBlank() }

    fun localizedName(lang: String): String {
        if (!isBuiltIn || lang != "en") return name
        return when (id) {
            1 -> "2-2-2"
            2 -> "1-1-1"
            3 -> "1-1-2"
            4 -> "5-2"
            5 -> "24h-3"
            else -> name
        }
    }

    fun displayDescription(lang: String): String {
        if (lang != "en") return description
        return when (id) {
            1 -> "2 day, 2 night, 2 days off"
            2 -> "1 day, 1 night, 1 day off"
            3 -> "1 day, 1 night, 2 days off"
            4 -> "5 days, 2 days off"
            5 -> "1 24-hour shift, 3 days off"
            else -> description
        }
    }

    companion object {
        fun getBuiltInTemplates(): List<Template> = listOf(
            Template(id = 1, name = "2-2-2", description = "2 день, 2 ночь, 2 выходных", pattern = "D,D,N,N,O,O", isBuiltIn = true, sortIndex = 0),
            Template(id = 2, name = "1-1-1", description = "1 день, 1 ночь, 1 выходной", pattern = "D,N,O", isBuiltIn = true, sortIndex = 1),
            Template(id = 3, name = "1-1-2", description = "1 день, 1 ночь, 2 выходных", pattern = "D,N,O,O", isBuiltIn = true, sortIndex = 2),
            Template(id = 4, name = "5-2", description = "5 дней, 2 выходных", pattern = "D,D,D,D,D,O,O", isBuiltIn = true, sortIndex = 3),
            Template(id = 5, name = "Сутки-3", description = "1 сутки, 3 выходных", pattern = "24,O,O,O", isBuiltIn = true, sortIndex = 4)
        )
    }
}