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

    companion object {
        fun getBuiltInTemplates(): List<Template> = listOf(
            Template(id = 1, name = "2-2-2", description = "2 день, 2 ночь, 2 выходных", pattern = "D,D,N,N,O,O", isBuiltIn = true, sortIndex = 0),
            Template(id = 2, name = "1-1-1", description = "1 день, 1 ночь, 1 выходной", pattern = "D,N,O", isBuiltIn = true, sortIndex = 1),
            Template(id = 3, name = "1-1-2", description = "1 день, 1 ночь, 2 выходных", pattern = "D,N,O,O", isBuiltIn = true, sortIndex = 2),
            Template(id = 4, name = "5-2", description = "5 дней, 2 выходных", pattern = "D,D,D,D,D,O,O", isBuiltIn = true, sortIndex = 3)
        )
    }
}