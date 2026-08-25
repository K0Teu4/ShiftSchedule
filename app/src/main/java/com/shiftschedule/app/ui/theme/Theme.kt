package com.shiftschedule.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val Dark = darkColorScheme(
    primary = Color(0xFF9B8CFF),
    onPrimary = Color(0xFF17132F),
    primaryContainer = Color(0xFF312A63),
    onPrimaryContainer = Color(0xFFEAE6FF),
    secondary = Color(0xFF62E681),
    onSecondary = Color(0xFF06200E),
    secondaryContainer = Color(0xFF164D25),
    onSecondaryContainer = Color(0xFFBDF6C9),
    tertiary = Color(0xFFFF6B8A),
    onTertiary = Color(0xFF3B0011),
    background = Color(0xFF080B0E),
    onBackground = Color(0xFFF3F4F6),
    surface = Color(0xFF11161A),
    onSurface = Color(0xFFF3F4F6),
    surfaceVariant = Color(0xFF252B31),
    onSurfaceVariant = Color(0xFFB9C0C7),
    surfaceContainer = Color(0xFF171C21),
    surfaceContainerLow = Color(0xFF13181C),
    surfaceContainerHigh = Color(0xFF1E242A),
    error = Color(0xFFFF8A80),
    outline = Color(0xFF4B535B)
)

private val Light = lightColorScheme(
    primary = Color(0xFF5548D8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8E5FF),
    onPrimaryContainer = Color(0xFF17104A),
    secondary = Color(0xFF22764B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC7F2D7),
    onSecondaryContainer = Color(0xFF052513),
    tertiary = Color(0xFFD82E63),
    onTertiary = Color.White,
    background = Color(0xFFF7F8FA),
    onBackground = Color(0xFF191B1F),
    surface = Color.White,
    onSurface = Color(0xFF191B1F),
    surfaceVariant = Color(0xFFE6E7EB),
    onSurfaceVariant = Color(0xFF626970),
    surfaceContainer = Color(0xFFF0F1F4),
    surfaceContainerLow = Color(0xFFF5F6F8),
    surfaceContainerHigh = Color(0xFFE9EBEF),
    error = Color(0xFFBA1A1A),
    outline = Color(0xFF747A81)
)

@Composable
fun ShiftScheduleTheme(theme: String = "system", content: @Composable () -> Unit) {
    val darkSystem = isSystemInDarkTheme()
    val scheme = when (theme) {
        "light" -> Light
        "dark" -> Dark
        else -> if (android.os.Build.VERSION.SDK_INT >= 31) {
            if (darkSystem) dynamicDarkColorScheme(LocalContext.current) else dynamicLightColorScheme(LocalContext.current)
        } else {
            if (darkSystem) Dark else Light
        }
    }
    MaterialTheme(
        colorScheme = scheme,
        typography = Typography,
        shapes = androidx.compose.material3.Shapes(
            RoundedCornerShape(12.dp),
            RoundedCornerShape(18.dp),
            RoundedCornerShape(24.dp)
        ),
        content = content
    )
}
