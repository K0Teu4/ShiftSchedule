package com.shiftschedule.app.data.model

import androidx.compose.ui.graphics.Color

enum class ShiftType(
    val code: String,
    val emoji: String,
    val letter: String,
    val displayName: String,
    val color: Color
) {
    DAY("D", "☀️", "Д", "День", Color(0xFF34C759)),
    NIGHT("N", "🌙", "Н", "Ночь", Color(0xFF5856D6)),
    OFF("O", "🏠", "В", "Выходной", Color(0xFFFF9500)),
    SICK("S", "🤒", "Б", "Больничный", Color(0xFFFF3B30)),
    VACATION("V", "🌴", "О", "Отпуск", Color(0xFFAF52DE))
}