package com.shiftschedule.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ShiftScheduleTheme(
    theme: String = "dark",
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        "light" -> lightColorScheme(
            background = Color(0xFFFAF9F6),
            surface = Color(0xFFFFFFFF),
            primary = Color(0xFF4F46E5),
            secondary = Color(0xFF1B8A4C),
            onBackground = Color(0xFF17171A),
            onSurface = Color(0xFF17171A),
            onPrimary = Color(0xFFFFFFFF),
            onSecondary = Color(0xFFFFFFFF),
            error = Color(0xFFB3261E)
        )
        "sepia" -> darkColorScheme(
            background = Color(0xFF241B12),
            surface = Color(0xFF33291D),
            primary = Color(0xFFD9A05B),
            secondary = Color(0xFF9DB87A),
            onBackground = Color(0xFFF4EAD9),
            onSurface = Color(0xFFF4EAD9),
            onPrimary = Color(0xFF241B12),
            error = Color(0xFFE06C5A)
        )
        "midnight" -> darkColorScheme(
            background = Color(0xFF04070D),
            surface = Color(0xFF0B1220),
            primary = Color(0xFF4C8DFF),
            secondary = Color(0xFF37C79A),
            onBackground = Color(0xFFE6EEFA),
            onSurface = Color(0xFFE6EEFA),
            onPrimary = Color(0xFF04070D),
            error = Color(0xFFFF6B6B)
        )
        else -> darkColorScheme(
            background = Color(0xFF0A0A0A),
            surface = Color(0xFF1A1A1A),
            primary = Color(0xFF5856D6),
            secondary = Color(0xFF34C759),
            onBackground = Color(0xFFFFFFFF),
            onSurface = Color(0xFFFFFFFF),
            onPrimary = Color(0xFFFFFFFF),
            error = Color(0xFFFF3B30)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}