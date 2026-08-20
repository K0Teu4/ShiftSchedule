package com.shiftschedule.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ShiftScheduleTheme(theme: String = "dark", content: @Composable () -> Unit) {
    val colorScheme = when (theme) {
        "light" -> lightColorScheme(background = Color(0xFFFAF9F6), surface = Color(0xFFFFFFFF), primary = Color(0xFF4F46E5), secondary = Color(0xFF1B8A4C), onBackground = Color(0xFF17171A), onSurface = Color(0xFF17171A), onPrimary = Color(0xFFFFFFFF), onSecondary = Color(0xFFFFFFFF), error = Color(0xFFB3261E))
        "sand" -> lightColorScheme(background = Color(0xFFFFF6E9), surface = Color(0xFFFFFBF2), primary = Color(0xFFB26B1F), secondary = Color(0xFF6E8F3C), onBackground = Color(0xFF241A0E), onSurface = Color(0xFF241A0E), onPrimary = Color(0xFFFFFFFF), error = Color(0xFFC2452F))
        "sepia" -> darkColorScheme(background = Color(0xFF241B12), surface = Color(0xFF33291D), primary = Color(0xFFD9A05B), secondary = Color(0xFF9DB87A), onBackground = Color(0xFFF4EAD9), onSurface = Color(0xFFF4EAD9), onPrimary = Color(0xFF241B12), error = Color(0xFFE06C5A))
        "midnight" -> darkColorScheme(background = Color(0xFF000000), surface = Color(0xFF0A0F16), primary = Color(0xFF00E5FF), secondary = Color(0xFF7C4DFF), onBackground = Color(0xFFE4F6FF), onSurface = Color(0xFFE4F6FF), onPrimary = Color(0xFF001318), error = Color(0xFFFF5252))
        "ocean" -> darkColorScheme(background = Color(0xFF032A33), surface = Color(0xFF07404C), primary = Color(0xFF22B8CF), secondary = Color(0xFF66D19E), onBackground = Color(0xFFDFF6F9), onSurface = Color(0xFFDFF6F9), onPrimary = Color(0xFF002229), error = Color(0xFFFF7A6B))
        "forest" -> darkColorScheme(background = Color(0xFF0B1410), surface = Color(0xFF142019), primary = Color(0xFF7BC46A), secondary = Color(0xFFD9B45B), onBackground = Color(0xFFE8F3E9), onSurface = Color(0xFFE8F3E9), onPrimary = Color(0xFF10240C), error = Color(0xFFFF8A7A))
        "berry" -> darkColorScheme(background = Color(0xFF150A14), surface = Color(0xFF221220), primary = Color(0xFFD985C7), secondary = Color(0xFF9A8CFF), onBackground = Color(0xFFF6E9F4), onSurface = Color(0xFFF6E9F4), onPrimary = Color(0xFF33102C), error = Color(0xFFFF7A6B))
        "plum" -> darkColorScheme(background = Color(0xFF1B0B26), surface = Color(0xFF2A1439), primary = Color(0xFFB388FF), secondary = Color(0xFFFF8AC2), onBackground = Color(0xFFF1E9FF), onSurface = Color(0xFFF1E9FF), onPrimary = Color(0xFF221033), error = Color(0xFFFF7A6B))
        "graphite" -> darkColorScheme(background = Color(0xFF101114), surface = Color(0xFF1A1C20), primary = Color(0xFFC9CCD3), secondary = Color(0xFF8AB4F8), onBackground = Color(0xFFE8EAED), onSurface = Color(0xFFE8EAED), onPrimary = Color(0xFF17181B), error = Color(0xFFFF8A80))
        else -> darkColorScheme(background = Color(0xFF0A0A0A), surface = Color(0xFF1A1A1A), primary = Color(0xFF5856D6), secondary = Color(0xFF34C759), onBackground = Color(0xFFFFFFFF), onSurface = Color(0xFFFFFFFF), onPrimary = Color(0xFFFFFFFF), error = Color(0xFFFF3B30))
    }
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

