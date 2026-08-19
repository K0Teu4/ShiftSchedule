package com.shiftschedule.app.data.model

import androidx.compose.ui.graphics.Color

enum class ShiftType(
    val code: String,
    private val nameRu: String,
    private val nameEn: String,
    val emoji: String,
    val color: Color
) {
    DAY("D", "День", "Day", "☀️", Color(0xFF34C759)),
    NIGHT("N", "Ночь", "Night", "🌙", Color(0xFF5856D6)),
    OFF("O", "Выходной", "Day off", "🏠", Color(0xFFFF9500)),
    SICK("S", "Больничный", "Sick leave", "🤒", Color(0xFFFF3B30)),
    VACATION("V", "Отпуск", "Vacation", "🌴", Color(0xFF00C7BE)),
    HOLIDAY("H", "Праздник", "Holiday", "🎉", Color(0xFFFF2D55));

    val displayName: String get() = nameRu

    fun displayName(lang: String): String = if (lang == "en") nameEn else nameRu
}