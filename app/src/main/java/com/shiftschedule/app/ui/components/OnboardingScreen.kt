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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun OnboardingScreen(onCreateClick: () -> Unit) {
    val scroll = rememberScrollState()

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(scroll)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        "SW",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp)
                    )
                }
                Column(Modifier.padding(start = 11.dp)) {
                    Text("ShiftWeave", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text("График смен без лишних расчётов", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.size(28.dp))
            Text(
                "Смены —\nна одном экране.",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.size(10.dp))
            Text(
                "Создайте график один раз. ShiftWeave покажет смены в календаре, повторит ваш цикл и поможет сравнить несколько графиков.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.size(20.dp))
            PreviewCalendar()

            Spacer(Modifier.size(20.dp))
            SectionFeature(
                icon = Icons.Filled.CalendarMonth,
                title = "Календарь",
                description = "День, ночь и выходной сразу видны по цвету. Нажмите на дату, чтобы изменить смену."
            )
            SectionFeature(
                icon = Icons.Filled.CompareArrows,
                title = "Сравнение",
                description = "Сверяйте несколько графиков и находите совпадающие дневные, ночные смены и выходные."
            )
            SectionFeature(
                icon = Icons.Filled.NotificationsNone,
                title = "Напоминания",
                description = "Получайте уведомление о следующей смене в выбранное время."
            )

            Spacer(Modifier.size(8.dp))
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 2.dp,
            shadowElevation = 0.dp
        ) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Button(
                    onClick = onCreateClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(17.dp)
                ) {
                    Text(
                        "Создать первый график",
                        modifier = Modifier.padding(vertical = 4.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    "Данные хранятся на вашем устройстве",
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PreviewCalendar() {
    val cells = listOf(
        Pair("1", Color(0xFF34C759)), Pair("2", Color(0xFF34C759)),
        Pair("3", Color(0xFF8B82FF)), Pair("4", Color(0xFF8B82FF)),
        Pair("5", MaterialTheme.colorScheme.surfaceVariant), Pair("6", MaterialTheme.colorScheme.surfaceVariant),
        Pair("7", Color(0xFF34C759)), Pair("8", Color(0xFF8B82FF)),
        Pair("9", MaterialTheme.colorScheme.surfaceVariant), Pair("10", Color(0xFF34C759)),
        Pair("11", Color(0xFF8B82FF)), Pair("12", MaterialTheme.colorScheme.surfaceVariant)
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Август 2026", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Text("Ваш текущий цикл", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("2–2–2", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.size(12.dp))
            cells.chunked(6).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    row.forEach { (day, color) ->
                        val isOff = color == MaterialTheme.colorScheme.surfaceVariant
                        Box(
                            Modifier
                                .weight(1f)
                                .size(42.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(color.copy(alpha = if (isOff) 0.45f else 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(day, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                Spacer(Modifier.size(7.dp))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendDot(SharedDayWork, "День")
                LegendDot(SharedNightWork, "Ночь")
                LegendDot(MaterialTheme.colorScheme.outline, "Выходной")
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(text, Modifier.padding(start = 5.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}

@Composable
private fun SectionFeature(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 7.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(21.dp))
        }
        Column(Modifier.padding(start = 12.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
