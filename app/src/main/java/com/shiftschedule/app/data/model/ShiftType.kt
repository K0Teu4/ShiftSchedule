package com.shiftschedule.app.data.model

import androidx.compose.ui.graphics.Color

enum class ShiftType(
    val code: String,
    private val nameRu: String,
    private val nameEn: String,
    val emoji: String,
    val color: Color
) {
    DAY("D", "День", "Day", "☀️", Color(0xFF2FE36B)),
    NIGHT("N", "Ночь", "Night", "🌙", Color(0xFF8F82FF)),
    TWENTY_FOUR("24", "Сутки", "24 hours", "🕐", Color(0xFFFFB52E)),
    OFF("O", "Выходной", "Day off", "🏠", Color(0xFFD98924)),
    SICK("S", "Больничный", "Sick leave", "🤒", Color(0xFFFF4B4B)),
    VACATION("V", "Отпуск", "Vacation", "🌴", Color(0xFF19D4C5)),
    HOLIDAY("H", "Праздник", "Holiday", "🎉", Color(0xFFFF3970));

    val displayName: String get() = nameRu

    fun displayName(lang: String): String = if (lang == "en") nameEn else nameRu

    fun shortLabel(lang: String): String = when (this) {
        DAY -> if (lang == "en") "D" else "Д"
        NIGHT -> if (lang == "en") "N" else "Н"
        TWENTY_FOUR -> "24"
        OFF -> if (lang == "en") "O" else "В"
        SICK -> if (lang == "en") "S" else "Б"
        VACATION -> if (lang == "en") "V" else "О"
        HOLIDAY -> if (lang == "en") "H" else "П"
    }

    val isWorking: Boolean get() = this == DAY || this == NIGHT || this == TWENTY_FOUR
    val isDayLike: Boolean get() = this == DAY || this == TWENTY_FOUR
    val isNightLike: Boolean get() = this == NIGHT || this == TWENTY_FOUR

    companion object {
        fun fromCode(code: String?): ShiftType? = values().firstOrNull { it.code == code }
    }
}
