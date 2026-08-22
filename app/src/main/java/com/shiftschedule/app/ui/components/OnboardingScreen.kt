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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(onCreateClick: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 28.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.primary) { Text("SW", color = MaterialTheme.colorScheme.onPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp)) }
                Column(Modifier.padding(start = 12.dp)) { Text("ShiftWeave", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold); Text("Ваш график. Без хаоса.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Spacer(Modifier.size(36.dp))
            Text("Сразу видно,\nкогда работать.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.size(10.dp))
            Text("Создайте один или несколько графиков, задайте цикл и больше не считайте смены вручную.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.size(24.dp))
            PreviewCalendar()
            Spacer(Modifier.size(24.dp))
            FeatureRow("01", "Календарь", "Сегодня, следующая смена и весь месяц на одном экране")
            FeatureRow("02", "Сравнение", "Общие выходные для семьи, пары или команды")
            FeatureRow("03", "Автоматика", "Напоминания, статистика, зарплата и резервные копии")
        }
        Column {
            Button(onClick = onCreateClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) { Text("Создать первый график", modifier = Modifier.padding(vertical = 5.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            Text("Данные остаются на вашем устройстве", modifier = Modifier.fillMaxWidth().padding(top = 12.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable private fun PreviewCalendar() {
    val types = listOf(Color(0xFF34C759), Color(0xFF5856D6), Color(0xFF5856D6), Color(0xFFFF9500), Color(0xFFFF9500), Color(0xFF34C759), Color(0xFF34C759), Color(0xFF5856D6), Color(0xFFFF9500), Color(0xFF34C759), Color(0xFF34C759), Color(0xFF5856D6))
    Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("Август", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f)); Text("2 / 2", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.size(12.dp))
            types.chunked(6).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { row.forEach { color -> Box(Modifier.weight(1f).size(38.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = .18f)), contentAlignment = Alignment.Center) { Box(Modifier.size(8.dp).clip(CircleShape).background(color)) } } } }
        }
    }
}

@Composable private fun FeatureRow(number: String, title: String, description: String) { Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.Top) { Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = .15f)) { Text(number, modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelMedium) }; Column(Modifier.padding(start = 12.dp)) { Text(title, fontWeight = FontWeight.Bold); Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp)) } } }
