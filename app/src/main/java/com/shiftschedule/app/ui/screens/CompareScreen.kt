package com.shiftschedule.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.ui.components.ScreenHeader
import com.shiftschedule.app.ui.components.SectorDayCell
import com.shiftschedule.app.ui.components.TipCard
import com.shiftschedule.app.ui.components.WeekHeader
import com.shiftschedule.app.ui.theme.SharedDayOff
import com.shiftschedule.app.ui.viewmodel.ShiftViewModel
import com.shiftschedule.app.util.DateUtils
import com.shiftschedule.app.util.LocalLang
import com.shiftschedule.app.util.monthLocale
import com.shiftschedule.app.util.tr
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle

@Composable
fun CompareScreen(viewModel: ShiftViewModel) {
    val schedules by viewModel.allSchedules.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val selectedCompareIds by viewModel.selectedCompareIds.collectAsState()
    val haptics = LocalHapticFeedback.current
    var dayDetailsDate by remember { mutableStateOf<LocalDate?>(null) }
    var dragAmountX by remember { mutableStateOf(0f) }
    var yearMode by remember { mutableStateOf(false) }

    LaunchedEffect(schedules) {
        val valid = selectedCompareIds.filter { id -> schedules.any { it.id == id } }.toSet()
        if (schedules.isNotEmpty() && valid.isEmpty()) {
            schedules.forEach { viewModel.toggleCompareSchedule(it.id) }
        } else if (valid != selectedCompareIds) {
            selectedCompareIds.filter { it !in valid }.forEach { viewModel.toggleCompareSchedule(it) }
        }
    }

    val selectedSchedules = schedules.filter { it.id in selectedCompareIds }
    val stats = viewModel.getMonthStats(selectedCompareIds.toList(), currentMonth)

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).widthIn(max = 900.dp).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()).pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            dragAmountX > 100f -> if (yearMode) viewModel.previousYear() else viewModel.previousMonth()
                            dragAmountX < -100f -> if (yearMode) viewModel.nextYear() else viewModel.nextMonth()
                        }
                        dragAmountX = 0f
                    }
                ) { change, dragAmount -> dragAmountX += dragAmount; change.consume() }
            }
        ) {
            ScreenHeader(title = tr("compare_title"), subtitle = DateUtils.monthTitle(currentMonth, monthLocale()), modifier = Modifier.padding(bottom = 4.dp))
            if (!viewModel.isTipSeen("compare")) {
                TipCard(text = tr("tip_compare"), onClose = { viewModel.markTipSeen("compare") }, modifier = Modifier.padding(bottom = 8.dp))
            }
            Row(modifier = Modifier.padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PeriodChip(tr("month_view"), !yearMode) { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); yearMode = false }
                PeriodChip(tr("year_view"), yearMode) { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); yearMode = true }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (schedules.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(tr("no_schedules"), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(tr("create_one"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
            if (schedules.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    schedules.forEach { schedule ->
                        val selected = schedule.id in selectedCompareIds
                        Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface).clickable { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); viewModel.toggleCompareSchedule(schedule.id) }.padding(horizontal = 14.dp, vertical = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(runCatching { Color(android.graphics.Color.parseColor(schedule.color)) }.getOrElse { MaterialTheme.colorScheme.primary }))
                                Text(schedule.name, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 6.dp))
                            }
                        }
                    }
                }
            }
            if (yearMode) {
                YearReport(viewModel = viewModel, year = currentMonth.year, scheduleIds = selectedCompareIds.toList(), sharedLabel = selectedSchedules.size >= 2, schedules = selectedSchedules)
            } else {
                if (selectedSchedules.size >= 2) {
                    val shared = stats["shared_off"] ?: 0
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("\u2726", color = SharedDayOff, fontSize = 22.sp)
                            Spacer(modifier = Modifier.size(10.dp))
                            Column {
                                Text(if (shared > 0) tr("shared_off") + ": $shared" else tr("no_shared"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(if (shared > 0) tr("find_star") else tr("try_other"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                WeekHeader(weekStart = settings.weekStart, lang = LocalLang.current)
                Spacer(modifier = Modifier.height(8.dp))
                val days = DateUtils.getDaysInMonth(currentMonth)
                val firstDayOffset = DateUtils.getFirstDayOffset(currentMonth, settings.weekStart)
                val today = LocalDate.now()
                val cells = mutableListOf<LocalDate?>()
                repeat(firstDayOffset) { cells.add(null) }
                cells.addAll(days)
                while (cells.size % 7 != 0) cells.add(null)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    cells.chunked(7).forEach { rowCells ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            rowCells.forEach { date ->
                                if (date == null) {
                                    Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                                } else {
                                    val shiftsForDay = selectedSchedules.map { it.name to viewModel.getShiftForDate(it, date) }
                                    val isSharedDayOff = selectedSchedules.size >= 2 && shiftsForDay.isNotEmpty() && shiftsForDay.all { it.second?.code == "O" }
                                    SectorDayCell(day = date.dayOfMonth, shifts = shiftsForDay, isToday = date == today, isCurrentMonth = date.month == currentMonth.month, isSharedDayOff = isSharedDayOff,
                                        isHoliday = settings.rfHolidays && com.shiftschedule.app.util.RuHolidays.isHoliday(date), modifier = Modifier.weight(1f), onClick = { dayDetailsDate = date })
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(tr("stats"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (selectedSchedules.size == 1) {
                            StatRow(tr("total_off"), stats["total_off"] ?: 0)
                            StatRow(tr("total_day"), stats["total_day"] ?: 0)
                            StatRow(tr("total_night"), stats["total_night"] ?: 0)
                            StatRow(tr("total_holiday"), stats["total_holiday"] ?: 0)
                            StatRow(tr("total_sick"), stats["total_sick"] ?: 0)
                            StatRow(tr("total_vacation"), stats["total_vacation"] ?: 0)
                            StatRow(tr("total_hours"), stats["total_hours"] ?: 0)
                            if ((stats["total_salary"] ?: 0) > 0) StatRow(tr("estimated_salary"), stats["total_salary"] ?: 0)
                        } else {
                            StatRow(tr("shared_off") + " \u2726", stats["shared_off"] ?: 0)
                            StatRow(tr("total_day"), stats["total_day"] ?: 0)
                            StatRow(tr("total_night"), stats["total_night"] ?: 0)
                            StatRow(tr("total_holiday"), stats["total_holiday"] ?: 0)
                            StatRow(tr("total_sick"), stats["total_sick"] ?: 0)
                            StatRow(tr("total_vacation"), stats["total_vacation"] ?: 0)
                            StatRow(tr("total_hours"), stats["total_hours"] ?: 0)
                            if ((stats["total_salary"] ?: 0) > 0) StatRow(tr("estimated_salary"), stats["total_salary"] ?: 0)
                            StatRow(tr("all_working"), stats["all_working"] ?: 0)
                            StatRow(tr("total_hours"), stats["total_hours"] ?: 0)
                            if ((stats["total_salary"] ?: 0) > 0) StatRow(tr("estimated_salary"), stats["total_salary"] ?: 0)
                            if (settings.rfHolidays) StatRow(tr("rf_holidays") + " 🎉", com.shiftschedule.app.util.RuHolidays.countInMonth(currentMonth))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        selectedSchedules.forEach { schedule ->
                            val s = viewModel.getMonthStats(listOf(schedule.id), currentMonth)
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(schedule.name, style = MaterialTheme.typography.bodySmall)
                                Text("\u2600\uFE0F ${s["total_day"] ?: 0} \u00B7 \uD83C\uDF19 ${s["total_night"] ?: 0} \u00B7 \uD83C\uDFE0 ${s["total_off"] ?: 0} \u00B7 \uD83C\uDF89 ${s["total_holiday"] ?: 0}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    dayDetailsDate?.let { date ->
        val list = viewModel.getShiftsForDate(date).filter { (sched, _) -> sched.id in selectedCompareIds }
        AlertDialog(
            onDismissRequest = { dayDetailsDate = null },
            title = { Text("${date.dayOfMonth} ${DateUtils.monthName(currentMonth, monthLocale())}") },
            text = {
                Column {
                    list.forEach { (schedule, shift) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(schedule.name, style = MaterialTheme.typography.bodyLarge)
                            Text(text = shift?.let { it.emoji + " " + it.displayName } ?: "\u2014", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { dayDetailsDate = null }) { Text(tr("close")) } }
        )
    }
}

@Composable
private fun PeriodChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(label, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun YearReport(viewModel: ShiftViewModel, year: Int, scheduleIds: List<Int>, sharedLabel: Boolean, schedules: List<Schedule>) {
    val totals = viewModel.getYearStats(scheduleIds, year)
    Spacer(modifier = Modifier.height(12.dp))
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                TextButton(onClick = { viewModel.previousYear() }) { Text("\u2039", fontSize = 22.sp) }
                Text(tr("year_view") + " $year", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp))
                TextButton(onClick = { viewModel.nextYear() }) { Text("\u203A", fontSize = 22.sp) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                YearTotal("\u2600\uFE0F", totals["total_day"] ?: 0)
                YearTotal("\uD83C\uDF19", totals["total_night"] ?: 0)
                YearTotal("\uD83C\uDFE0", totals["total_off"] ?: 0)
                YearTotal("\uD83C\uDF89", totals["total_holiday"] ?: 0)
                YearTotal("\uD83E\uDD12", totals["total_sick"] ?: 0)
                YearTotal("\uD83C\uDF34", totals["total_vacation"] ?: 0)
                if (sharedLabel) YearTotal("\u2726", totals["shared_off"] ?: 0)
            }
        }
    }
    if (schedules.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(tr("year_by_schedule"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        schedules.forEach { schedule ->
            val s = viewModel.getYearStats(listOf(schedule.id), year)
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(runCatching { Color(android.graphics.Color.parseColor(schedule.color)) }.getOrElse { MaterialTheme.colorScheme.primary }))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(schedule.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Text("\u2600\uFE0F ${s["total_day"] ?: 0} \u00B7 \uD83C\uDF19 ${s["total_night"] ?: 0} \u00B7 \uD83C\uDFE0 ${s["total_off"] ?: 0} \u00B7 \uD83C\uDF89 ${s["total_holiday"] ?: 0}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
    for (m in 1..12) {
        val ym = YearMonth.of(year, m)
        val s = viewModel.getMonthStats(scheduleIds, ym)
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(ym.month.getDisplayName(TextStyle.SHORT_STANDALONE, monthLocale()).replaceFirstChar { if (it.isLowerCase()) it.uppercase(monthLocale()) else it.toString() }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text("\u2600\uFE0F ${s["total_day"] ?: 0} \u00B7 \uD83C\uDF19 ${s["total_night"] ?: 0} \u00B7 \uD83C\uDFE0 ${s["total_off"] ?: 0} \u00B7 \uD83C\uDF89 ${s["total_holiday"] ?: 0}" + if (sharedLabel) " \u00B7 \u2726 ${s["shared_off"] ?: 0}" else "", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun YearTotal(icon: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 18.sp)
        Text(value.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatRow(label: String, value: Int) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}



