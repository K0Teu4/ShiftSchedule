package com.shiftschedule.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.data.model.ShiftType
import com.shiftschedule.app.ui.components.AppHeader
import com.shiftschedule.app.ui.components.EmptyState
import com.shiftschedule.app.ui.components.SectorDayCell
import com.shiftschedule.app.ui.components.SurfaceCard
import com.shiftschedule.app.ui.components.WeekHeader
import com.shiftschedule.app.ui.theme.SharedDayOff
import com.shiftschedule.app.ui.theme.SharedDayWork
import com.shiftschedule.app.ui.theme.SharedNightWork
import com.shiftschedule.app.util.DateUtils
import com.shiftschedule.app.util.LocalLang
import com.shiftschedule.app.util.RuHolidays
import com.shiftschedule.app.util.monthLocale
import com.shiftschedule.app.util.tr
import com.shiftschedule.app.ui.viewmodel.ShiftViewModel
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CompareScreen(viewModel: ShiftViewModel) {
    val schedules by viewModel.allSchedules.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val selectedIds by viewModel.selectedCompareIds.collectAsState()
    val lang = LocalLang.current
    var detailsDate by remember { mutableStateOf<LocalDate?>(null) }

    LaunchedEffect(schedules) {
        val valid = selectedIds.filter { id -> schedules.any { it.id == id } }.toSet()
        if (schedules.isNotEmpty() && valid.isEmpty()) {
            schedules.forEach { viewModel.toggleCompareSchedule(it.id) }
        } else {
            selectedIds.filter { it !in valid }.forEach { viewModel.toggleCompareSchedule(it) }
        }
    }

    val selected = schedules.filter { it.id in selectedIds }
    val stats = viewModel.getMonthStats(selectedIds.toList(), currentMonth)

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).widthIn(max = 720.dp).padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(Modifier.height(6.dp))
                AppHeader(tr("compare_title"), tr("compare_subtitle"))
            }
            if (schedules.isEmpty()) {
                item { EmptyState(tr("no_schedules"), tr("create_one"), tr("add_schedule"), {}) }
            } else {
                item {
                    SurfaceCard {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tr("schedules"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                                Text("${selected.size} ${tr("selected_count")}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                schedules.forEach { schedule ->
                                    val active = schedule.id in selectedIds
                                    val color = parseColor(schedule.color)
                                    Surface(
                                        onClick = { viewModel.toggleCompareSchedule(schedule.id) },
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (active) color.copy(alpha = .24f) else MaterialTheme.colorScheme.surfaceContainerHigh
                                    ) {
                                        Row(Modifier.padding(horizontal = 11.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(Modifier.size(9.dp).clip(CircleShape).background(color))
                                            Text(schedule.name, Modifier.padding(start = 7.dp), style = MaterialTheme.typography.labelLarge, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    SurfaceCard {
                        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = .14f)) {
                                Icon(Icons.Filled.Groups, null, modifier = Modifier.padding(10.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                            Column(Modifier.weight(1f).padding(start = 11.dp)) {
                                Text(
                                    if (selected.size >= 2 && (stats["shared_off"] ?: 0) > 0) tr("shared_off_found") else tr("shared_off"),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    if (selected.size >= 2) "${stats["shared_off"] ?: 0} ${tr("days_this_month")}" else tr("select_two_schedules"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                item {
                    SurfaceCard {
                        Column(Modifier.padding(16.dp)) {
                            Text(tr("compare_read_title"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                            Spacer(Modifier.height(10.dp))
                            CompareLegendRow(SharedDayWork, tr("shared_day"), tr("compare_day_hint"))
                            Spacer(Modifier.height(9.dp))
                            CompareLegendRow(SharedNightWork, tr("shared_night"), tr("compare_night_hint"))
                            Spacer(Modifier.height(9.dp))
                            CompareLegendRow(SharedDayOff, tr("shared_short"), tr("compare_off_hint"))
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(9.dp).clip(CircleShape).background(ShiftType.TWENTY_FOUR.color))
                                Text(tr("compare_24_hint"), Modifier.padding(start = 8.dp).weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.previousMonth() }) { Icon(Icons.Filled.ChevronLeft, tr("prev_month")) }
                        Text(DateUtils.monthTitle(currentMonth, monthLocale()), Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                        IconButton(onClick = { viewModel.nextMonth() }) { Icon(Icons.Filled.ChevronRight, tr("next_month")) }
                    }
                }
                item { WeekHeader(settings.weekStart, lang) }
                item {
                    CompareCalendarGrid(
                        month = currentMonth,
                        weekStart = settings.weekStart,
                        selected = selected,
                        viewModel = viewModel,
                        showHolidays = settings.rfHolidays,
                        onSwipeLeft = viewModel::nextMonth,
                        onSwipeRight = viewModel::previousMonth,
                        onDayClick = { detailsDate = it }
                    )
                }
                item {
                    SurfaceCard {
                        Column(Modifier.padding(16.dp)) {
                            Text(tr("month_summary"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                            SummaryRow(tr("shared_day"), stats["shared_day"] ?: 0, SharedDayWork)
                            SummaryRow(tr("shared_night"), stats["shared_night"] ?: 0, SharedNightWork)
                            SummaryRow(tr("shared_short"), stats["shared_off"] ?: 0, SharedDayOff)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(90.dp)) }
        }
    }

    detailsDate?.let { date ->
        AlertDialog(
            onDismissRequest = { detailsDate = null },
            title = { Text("${date.dayOfMonth}.${date.monthValue}.${date.year}") },
            text = {
                Column {
                    if (settings.rfHolidays && RuHolidays.isHoliday(date)) {
                        Text(tr("holiday_legend"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    selected.forEach { schedule ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(schedule.name, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            val shift = viewModel.getShiftForDate(schedule, date)
                            Text(
                                shift?.let { if (settings.showEmoji) "${it.emoji} ${it.displayName(lang)}" else it.displayName(lang) } ?: "—",
                                Modifier.padding(start = 12.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { detailsDate = null }) { Text(tr("close")) } }
        )
    }
}

@Composable
private fun CompareCalendarGrid(
    month: YearMonth,
    weekStart: String,
    selected: List<Schedule>,
    viewModel: ShiftViewModel,
    showHolidays: Boolean,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onDayClick: (LocalDate) -> Unit
) {
    val days = DateUtils.getDaysInMonth(month)
    val offset = DateUtils.getFirstDayOffset(month, weekStart)
    val cells = buildList<LocalDate?> {
        repeat(offset) { add(null) }
        addAll(days)
        while (size % 7 != 0) add(null)
    }
    Column(
        Modifier
            .fillMaxWidth()
            .pointerInput(month) {
                var drag = 0f
                detectHorizontalDragGestures(
                    onDragStart = { drag = 0f },
                    onHorizontalDrag = { change, amount -> change.consume(); drag += amount },
                    onDragEnd = { if (drag < -70f) onSwipeLeft() else if (drag > 70f) onSwipeRight() }
                )
            },
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                week.forEach { date ->
                    if (date == null) {
                        Box(Modifier.weight(1f).aspectRatio(.94f))
                    } else {
                        val pairs = selected.map { it.name to viewModel.getShiftForDate(it, date) }
                        val dayMatch = selected.size >= 2 && pairs.all { it.second?.isDayLike == true }
                        val nightMatch = selected.size >= 2 && pairs.all { it.second?.isNightLike == true }
                        SectorDayCell(
                            day = date.dayOfMonth,
                            shifts = pairs,
                            isToday = date == LocalDate.now(),
                            isCurrentMonth = true,
                            isSharedDayOff = selected.size >= 2 && pairs.all { it.second == ShiftType.OFF },
                            isSharedDayWork = dayMatch,
                            isSharedNightWork = nightMatch,
                            isHoliday = showHolidays && RuHolidays.isHoliday(date),
                            modifier = Modifier.weight(1f),
                            onClick = { onDayClick(date) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: Int, color: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(color))
        Text(label, Modifier.weight(1f).padding(start = 9.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(value.toString(), fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun CompareLegendRow(color: Color, title: String, description: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(20.dp).border(2.dp, color, RoundedCornerShape(6.dp)))
        Column(Modifier.padding(start = 10.dp).weight(1f)) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun parseColor(value: String): Color = runCatching { Color(android.graphics.Color.parseColor(value)) }.getOrDefault(Color(0xFF6750A4))
