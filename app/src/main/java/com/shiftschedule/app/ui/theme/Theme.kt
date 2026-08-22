package com.shiftschedule.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val Dark = darkColorScheme(
    primary = Color(0xFF8B82FF), onPrimary = Color(0xFF16133B), primaryContainer = Color(0xFF2D285D), onPrimaryContainer = Color(0xFFE8E5FF),
    secondary = Color(0xFF79D7A0), onSecondary = Color(0xFF062014), secondaryContainer = Color(0xFF174B32), onSecondaryContainer = Color(0xFFB9F3D0),
    background = Color(0xFF0B0B10), onBackground = Color(0xFFF1EFF7), surface = Color(0xFF111117), onSurface = Color(0xFFF1EFF7),
    surfaceVariant = Color(0xFF26262E), onSurfaceVariant = Color(0xFFB9B7C1), surfaceContainer = Color(0xFF17171E), surfaceContainerLow = Color(0xFF14141A), surfaceContainerHigh = Color(0xFF1E1E26),
    error = Color(0xFFFF8A80), outline = Color(0xFF44434C)
)

private val Light = lightColorScheme(
    primary = Color(0xFF5147D9), onPrimary = Color.White, primaryContainer = Color(0xFFE8E5FF), onPrimaryContainer = Color(0xFF17104A),
    secondary = Color(0xFF22764B), onSecondary = Color.White, secondaryContainer = Color(0xFFC7F2D7), onSecondaryContainer = Color(0xFF052513),
    background = Color(0xFFF7F7FA), onBackground = Color(0xFF1B1B20), surface = Color(0xFFFFFFFF), onSurface = Color(0xFF1B1B20),
    surfaceVariant = Color(0xFFE7E6ED), onSurfaceVariant = Color(0xFF65636D), surfaceContainer = Color(0xFFF0EFF5), surfaceContainerLow = Color(0xFFF4F3F8), surfaceContainerHigh = Color(0xFFECEBF1),
    error = Color(0xFFBA1A1A), outline = Color(0xFF77757E)
)

@Composable
fun ShiftScheduleTheme(theme: String = "dark", content: @Composable () -> Unit) {
    val scheme = when (theme) {
        "light", "sand" -> if (theme == "sand") Light.copy(primary = Color(0xFF9A5C16), primaryContainer = Color(0xFFFFE6C5), background = Color(0xFFFFF8EE)) else Light
        "sepia" -> darkColorScheme(background = Color(0xFF241B12), surface = Color(0xFF30251A), surfaceContainer = Color(0xFF352A1E), surfaceContainerLow = Color(0xFF2B2118), surfaceContainerHigh = Color(0xFF3A2F22), primary = Color(0xFFE0A35E), onPrimary = Color(0xFF2B1A0A), onBackground = Color(0xFFF4EAD9), onSurface = Color(0xFFF4EAD9), onSurfaceVariant = Color(0xFFCDBFA9))
        "midnight" -> darkColorScheme(background = Color(0xFF05070B), surface = Color(0xFF0B1118), surfaceContainer = Color(0xFF101820), surfaceContainerLow = Color(0xFF0D141B), surfaceContainerHigh = Color(0xFF15212B), primary = Color(0xFF5CE1FF), secondary = Color(0xFF9D7BFF), onPrimary = Color(0xFF00151C), onBackground = Color(0xFFE7F8FF), onSurface = Color(0xFFE7F8FF), onSurfaceVariant = Color(0xFFAABDC7))
        "ocean" -> darkColorScheme(background = Color(0xFF06252C), surface = Color(0xFF0B3540), surfaceContainer = Color(0xFF0E3B46), surfaceContainerLow = Color(0xFF0B3038), surfaceContainerHigh = Color(0xFF124651), primary = Color(0xFF4DD8E8), secondary = Color(0xFF83DDB2), onPrimary = Color(0xFF002228), onBackground = Color(0xFFE0F8FA), onSurface = Color(0xFFE0F8FA), onSurfaceVariant = Color(0xFFA8CFD3))
        "forest" -> darkColorScheme(background = Color(0xFF0B1510), surface = Color(0xFF142019), surfaceContainer = Color(0xFF18271F), surfaceContainerLow = Color(0xFF111D17), surfaceContainerHigh = Color(0xFF1D3026), primary = Color(0xFF8AD979), secondary = Color(0xFFE0BC68), onPrimary = Color(0xFF10240C), onBackground = Color(0xFFE8F3E9), onSurface = Color(0xFFE8F3E9), onSurfaceVariant = Color(0xFFB5C7B8))
        "berry" -> darkColorScheme(background = Color(0xFF160A16), surface = Color(0xFF241326), surfaceContainer = Color(0xFF2A172C), surfaceContainerLow = Color(0xFF211123), surfaceContainerHigh = Color(0xFF331B35), primary = Color(0xFFE08CD0), secondary = Color(0xFFA69AFF), onPrimary = Color(0xFF35102F), onBackground = Color(0xFFF6E9F4), onSurface = Color(0xFFF6E9F4), onSurfaceVariant = Color(0xFFD0B7CC))
        "plum" -> darkColorScheme(background = Color(0xFF1B0B26), surface = Color(0xFF2A1439), surfaceContainer = Color(0xFF301943), surfaceContainerLow = Color(0xFF251130), surfaceContainerHigh = Color(0xFF371C4A), primary = Color(0xFFC3A0FF), secondary = Color(0xFFFF9FCA), onPrimary = Color(0xFF28113B), onBackground = Color(0xFFF1E9FF), onSurface = Color(0xFFF1E9FF), onSurfaceVariant = Color(0xFFCDB9DD))
        "graphite" -> darkColorScheme(background = Color(0xFF101114), surface = Color(0xFF1A1C20), surfaceContainer = Color(0xFF202227), surfaceContainerLow = Color(0xFF17191D), surfaceContainerHigh = Color(0xFF25272C), primary = Color(0xFFD0D3DA), secondary = Color(0xFF8AB4F8), onPrimary = Color(0xFF17181B), onBackground = Color(0xFFE8EAED), onSurface = Color(0xFFE8EAED), onSurfaceVariant = Color(0xFFB8BAC1))
        "dynamic" -> if (android.os.Build.VERSION.SDK_INT >= 31) if (isSystemInDarkTheme()) dynamicDarkColorScheme(LocalContext.current) else dynamicLightColorScheme(LocalContext.current) else Dark
        else -> Dark
    }
    MaterialTheme(colorScheme = scheme, typography = Typography, shapes = Shapes(RoundedCornerShape(12.dp), RoundedCornerShape(18.dp), RoundedCornerShape(26.dp)), content = content)
}
