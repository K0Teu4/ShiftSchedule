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
        "ocean" -> darkColorScheme(
            background = Color(0xFF06121E),
            surface = Color(0xFF0D1F2E),
            primary = Color(0xFF4FB3D9),
            secondary = Color(0xFF7FD1AE),
            onBackground = Color(0xFFE3F2F9),
            onSurface = Color(0xFFE3F2F9),
            onPrimary = Color(0xFF062033),
            error = Color(0xFFFF7A6B)
        )
        "forest" -> darkColorScheme(
            background = Color(0xFF0B1410),
            surface = Color(0xFF142019),
            primary = Color(0xFF7BC46A),
            secondary = Color(0xFFD9B45B),
            onBackground = Color(0xFFE8F3E9),
            onSurface = Color(0xFFE8F3E9),
            onPrimary = Color(0xFF10240C),
            error = Color(0xFFFF8A7A)
        )
        "berry" -> darkColorScheme(
            background = Color(0xFF150A14),
            surface = Color(0xFF221220),
            primary = Color(0xFFD985C7),
            secondary = Color(0xFF9A8CFF),
            onBackground = Color(0xFFF6E9F4),
            onSurface = Color(0xFFF6E9F4),
            onPrimary = Color(0xFF33102C),
            error = Color(0xFFFF7A6B)
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

