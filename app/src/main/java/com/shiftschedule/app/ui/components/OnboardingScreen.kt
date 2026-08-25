package com.shiftschedule.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shiftschedule.app.ui.theme.SharedDayWork
import com.shiftschedule.app.ui.theme.SharedNightWork
import com.shiftschedule.app.util.tr

@Composable
fun OnboardingScreen(onCreateClick: () -> Unit) {
    val scroll = rememberScrollState()
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).verticalScroll(scroll).padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primary) {
                Text("SW", color = MaterialTheme.colorScheme.onPrimary, fontSize = 25.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 17.dp, vertical = 13.dp))
            }
            Spacer(Modifier.size(14.dp))
            Text("ShiftWeave", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.size(6.dp))
            Text(tr("onb_sub"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.size(30.dp))
            Text(tr("onb_hero"), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
            Spacer(Modifier.size(10.dp))
            Text(tr("onb_intro"), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.size(22.dp))
            PreviewCalendar()
            Spacer(Modifier.size(18.dp))
            SectionFeature(Icons.Filled.CalendarMonth, tr("onb1"), tr("onb1d"))
            SectionFeature(Icons.Filled.CompareArrows, tr("onb2"), tr("onb2d"))
            SectionFeature(Icons.Filled.NotificationsNone, tr("onb3"), tr("onb3d"))
            Spacer(Modifier.size(8.dp))
        }
        Surface(color = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 1.dp) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Button(onClick = onCreateClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp)) {
                    Text(tr("onb_btn"), modifier = Modifier.padding(vertical = 4.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                }
                Text(tr("onb_note"), modifier = Modifier.fillMaxWidth().padding(top = 8.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}

@Composable
private fun PreviewCalendar() {
    val cells = listOf(
        "Д" to SharedDayWork, "Д" to SharedDayWork, "Н" to SharedNightWork, "Н" to SharedNightWork,
        "24" to Color(0xFFFFB52E), "В" to MaterialTheme.colorScheme.surfaceVariant,
        "Д" to SharedDayWork, "Н" to SharedNightWork, "24" to Color(0xFFFFB52E), "В" to MaterialTheme.colorScheme.surfaceVariant,
        "Д" to SharedDayWork, "Н" to SharedNightWork
    )
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(tr("onb_preview_month"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Text(tr("onb_preview_sub"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("2–2–2", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.size(12.dp))
            cells.chunked(6).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    row.forEach { (label, color) ->
                        Box(Modifier.weight(1f).size(42.dp).clip(RoundedCornerShape(11.dp)).background(color.copy(alpha = if (label == "В") .38f else .92f)), contentAlignment = Alignment.Center) {
                            Text(label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = if (label == "В") MaterialTheme.colorScheme.onSurface else Color.Black)
                        }
                    }
                }
                Spacer(Modifier.size(7.dp))
            }
        }
    }
}

@Composable
private fun SectionFeature(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.Top) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(21.dp))
        }
        Column(Modifier.padding(start = 12.dp)) {
            Text(title, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
        }
    }
}
