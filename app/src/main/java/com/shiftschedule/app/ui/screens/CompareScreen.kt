package com.shiftschedule.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.ui.components.AppHeader
import com.shiftschedule.app.ui.components.EmptyState
import com.shiftschedule.app.ui.components.SectionLabel
import com.shiftschedule.app.ui.components.SectorDayCell
import com.shiftschedule.app.ui.components.StatPill
import com.shiftschedule.app.ui.components.SurfaceCard
import com.shiftschedule.app.ui.components.WeekHeader
import com.shiftschedule.app.ui.viewmodel.ShiftViewModel
import com.shiftschedule.app.util.DateUtils
import com.shiftschedule.app.util.LocalLang
import com.shiftschedule.app.util.RuHolidays
import com.shiftschedule.app.util.monthLocale
import com.shiftschedule.app.util.tr
import java.time.LocalDate

@Composable
fun CompareScreen(viewModel: ShiftViewModel) {
    val schedules by viewModel.allSchedules.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val selectedIds by viewModel.selectedCompareIds.collectAsState()
    var detailsDate by remember { mutableStateOf<LocalDate?>(null) }
    var yearMode by remember { mutableStateOf(false) }

    LaunchedEffect(schedules) {
        val valid = selectedIds.filter { id -> schedules.any { it.id == id } }.toSet()
        if (schedules.isNotEmpty() && valid.isEmpty()) schedules.forEach { viewModel.toggleCompareSchedule(it.id) }
        else selectedIds.filter { it !in valid }.forEach { viewModel.toggleCompareSchedule(it) }
    }

    val selected = schedules.filter { it.id in selectedIds }
    val stats = viewModel.getMonthStats(selectedIds.toList(), currentMonth)

    Scaffold { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).widthIn(max = 720.dp).padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Spacer(Modifier.height(6.dp)); AppHeader("Сравнение", "Найдите общие выходные и быстро сверяйте несколько графиков") }
            if (schedules.isEmpty()) {
                item { EmptyState("Нечего сравнивать", "Добавьте хотя бы два графика, чтобы увидеть общие выходные.", "Перейти к графикам", {}) }
            } else {
                item {
                    SurfaceCard {
                        Column(Modifier.padding(16.dp)) {
                            SectionLabel("Графики", "${selected.size} выбрано")
                            Spacer(Modifier.height(10.dp))
                            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                schedules.forEach { schedule ->
                                    val active = schedule.id in selectedIds
                                    val color = parseColor(schedule.color)
                                    Surface(onClick = { viewModel.toggleCompareSchedule(schedule.id) }, shape = RoundedCornerShape(16.dp), color = if (active) color.copy(alpha = .20f) else MaterialTheme.colorScheme.surfaceContainerHigh) {
                                        Row(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(Modifier.size(9.dp).clip(CircleShape).background(color))
                                            Text(schedule.name, modifier = Modifier.padding(start = 7.dp), style = MaterialTheme.typography.labelLarge, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    SurfaceCard {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = .15f)) { Icon(Icons.Filled.Groups, null, modifier = Modifier.padding(11.dp), tint = MaterialTheme.colorScheme.primary) }
                            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                Text(if (selected.size >= 2 && (stats["shared_off"] ?: 0) > 0) "Нашли общие выходные" else "Общие выходные", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                                Text(if (selected.size >= 2) "${stats["shared_off"] ?: 0} дней в этом месяце" else "Выберите минимум два графика", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("✦", color = MaterialTheme.colorScheme.primary, fontSize = 22.sp)
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(onClick = { yearMode = false }, shape = RoundedCornerShape(14.dp), color = if (!yearMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.weight(1f)) { Text("Месяц", Modifier.padding(10.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold) }
                        Surface(onClick = { yearMode = true }, shape = RoundedCornerShape(14.dp), color = if (yearMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.weight(1f)) { Text("Год", Modifier.padding(10.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold) }
                    }
                }
                if (!yearMode) {
                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.previousMonth() }) { Icon(Icons.Filled.ChevronLeft, "Предыдущий месяц") }
                        Text(DateUtils.monthTitle(currentMonth, monthLocale()), Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        IconButton(onClick = { viewModel.nextMonth() }) { Icon(Icons.Filled.ChevronRight, "Следующий месяц") }
                    }
                }
                item { WeekHeader(settings.weekStart, LocalLang.current) }
                item {
                    val days = DateUtils.getDaysInMonth(currentMonth)
                    val offset = DateUtils.getFirstDayOffset(currentMonth, settings.weekStart)
                    val cells = buildList<LocalDate?> { repeat(offset) { add(null) }; addAll(days); while (size % 7 != 0) add(null) }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        cells.chunked(7).forEach { week ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                week.forEach { date ->
                                    if (date == null) Box(Modifier.weight(1f).aspectRatio(.92f)) else {
                                        val pairs = selected.map { it.name to viewModel.getShiftForDate(it, date) }
                                        SectorDayCell(day = date.dayOfMonth, shifts = pairs, isToday = date == LocalDate.now(), isCurrentMonth = true, isSharedDayOff = selected.size >= 2 && pairs.all { it.second?.code == "O" }, isHoliday = settings.rfHolidays && RuHolidays.isHoliday(date), modifier = Modifier.weight(1f), onClick = { detailsDate = date })
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatPill("✦", (stats["shared_off"] ?: 0).toString(), "Общие", Modifier.weight(1f))
                        StatPill("☀️", (stats["total_day"] ?: 0).toString(), "Дневные", Modifier.weight(1f))
                        StatPill("🌙", (stats["total_night"] ?: 0).toString(), "Ночные", Modifier.weight(1f))
                    }
                }
                item {
                    SurfaceCard {
                        Column(Modifier.padding(16.dp)) {
                            SectionLabel("Итоги")
                            SummaryRow("Выходные", stats["total_off"] ?: 0)
                            SummaryRow("Часы", stats["total_hours"] ?: 0)
                            if ((stats["total_salary"] ?: 0) > 0) SummaryRow("Заработок", stats["total_salary"] ?: 0, "₽")
                            if (settings.rfHolidays) SummaryRow("Праздники РФ", RuHolidays.countInMonth(currentMonth))
                        }
                    }
                }
                } else {
                    item {
                        val totals = viewModel.getYearStats(selectedIds.toList(), currentMonth.year)
                        SurfaceCard {
                            Column(Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { viewModel.previousYear() }) { Icon(Icons.Filled.ChevronLeft, null) }
                                    Text(currentMonth.year.toString(), Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                                    IconButton(onClick = { viewModel.nextYear() }) { Icon(Icons.Filled.ChevronRight, null) }
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    StatPill("☀️", (totals["total_day"] ?: 0).toString(), "День", Modifier.weight(1f))
                                    StatPill("🌙", (totals["total_night"] ?: 0).toString(), "Ночь", Modifier.weight(1f))
                                    StatPill("🏠", (totals["total_off"] ?: 0).toString(), "Выход", Modifier.weight(1f))
                                }
                                Spacer(Modifier.height(10.dp))
                                for (month in 1..12) {
                                    val ym = java.time.YearMonth.of(currentMonth.year, month)
                                    val m = viewModel.getMonthStats(selectedIds.toList(), ym)
                                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(ym.month.getDisplayName(java.time.format.TextStyle.SHORT_STANDALONE, monthLocale()), Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                                        Text("☀️ ${m["total_day"] ?: 0} · 🌙 ${m["total_night"] ?: 0} · 🏠 ${m["total_off"] ?: 0}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(90.dp)) }
        }
    }

    detailsDate?.let { date ->
        AlertDialog(onDismissRequest = { detailsDate = null }, title = { Text("${date.dayOfMonth}.${date.monthValue}.${date.year}") }, text = { Column { selected.forEach { schedule -> Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(schedule.name); val shift = viewModel.getShiftForDate(schedule, date); Text(shift?.let { "${it.emoji} ${it.displayName}" } ?: "—") } } } }, confirmButton = { TextButton(onClick = { detailsDate = null }) { Text(tr("close")) } })
    }
}

@Composable private fun SummaryRow(label: String, value: Int, suffix: String = "") { Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("$value$suffix", fontWeight = FontWeight.Bold) } }
private fun parseColor(value: String): Color = runCatching { Color(android.graphics.Color.parseColor(value)) }.getOrDefault(Color(0xFF6750A4))
