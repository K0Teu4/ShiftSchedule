package com.shiftschedule.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.ui.components.CalendarSkeleton
import com.shiftschedule.app.ui.components.DayCell
import com.shiftschedule.app.ui.components.EditDayModal
import com.shiftschedule.app.ui.components.EditScheduleModal
import com.shiftschedule.app.ui.components.OnboardingScreen
import com.shiftschedule.app.ui.components.TipCard
import com.shiftschedule.app.ui.components.WeekHeader
import com.shiftschedule.app.ui.viewmodel.ShiftViewModel
import com.shiftschedule.app.util.DateUtils
import com.shiftschedule.app.util.LocalLang
import com.shiftschedule.app.util.monthLocale
import com.shiftschedule.app.util.tr
import java.time.LocalDate

@Composable
fun CalendarScreen(viewModel: ShiftViewModel) {
    val schedules by viewModel.allSchedules.collectAsState()
    val templates by viewModel.allTemplates.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val selectedScheduleId by viewModel.selectedScheduleId.collectAsState()
    val isLoaded by viewModel.isLoaded.collectAsState()
    val haptics = LocalHapticFeedback.current
    val lang = LocalLang.current
    var showCreateModal by remember { mutableStateOf(false) }
    var editSchedule by remember { mutableStateOf<Schedule?>(null) }
    var editDay by remember { mutableStateOf<LocalDate?>(null) }
    var whoWhereDate by remember { mutableStateOf<LocalDate?>(null) }
    var showSchedulePicker by remember { mutableStateOf(false) }
    var dragAmountX by remember { mutableStateOf(0f) }
    val selectedSchedule by remember(schedules, selectedScheduleId) {
        derivedStateOf { schedules.find { it.id == selectedScheduleId } ?: schedules.firstOrNull() }
    }
    if (!isLoaded) {
        CalendarSkeleton()
        return
    }
    if (!settings.hasCompletedOnboarding && schedules.isEmpty()) {
        OnboardingScreen(onCreateClick = { showCreateModal = true })
    } else {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { showCreateModal = true }) {
                    Icon(Icons.Filled.Add, contentDescription = tr("add_schedule"))
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 900.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    when {
                                        dragAmountX > 100f -> viewModel.previousMonth()
                                        dragAmountX < -100f -> viewModel.nextMonth()
                                    }
                                    dragAmountX = 0f
                                }
                            ) { change, dragAmount ->
                                dragAmountX += dragAmount
                                change.consume()
                            }
                        }
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); viewModel.previousMonth() }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = tr("prev_month"))
                        }
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = DateUtils.monthTitle(currentMonth, monthLocale()), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); viewModel.nextMonth() }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = tr("next_month"))
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        HeaderChip(tr("today")) { viewModel.goToday() }
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        HeaderChip((selectedSchedule?.name ?: tr("schedule_label")) + " ▾") { showSchedulePicker = true }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!viewModel.isTipSeen("calendar")) {
                        TipCard(text = tr("tip_calendar"), onClose = { viewModel.markTipSeen("calendar") }, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    selectedSchedule?.let { schedule ->
                        val todayShift = viewModel.getShiftForDate(schedule, LocalDate.now())
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Text(tr("today_label"), style = MaterialTheme.typography.titleMedium)
                            Text(text = todayShift?.let { it.emoji + " " + it.displayName(lang) } ?: tr("no_shift"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = todayShift?.color ?: MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        val stats = viewModel.getMonthStats(listOf(schedule.id), currentMonth)
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                                EmojiStat("☀️", stats["total_day"] ?: 0)
                                EmojiStat("🌙", stats["total_night"] ?: 0)
                                EmojiStat("🏠", stats["total_off"] ?: 0)
                                EmojiStat("🎉", stats["total_holiday"] ?: 0)
                                EmojiStat("🤒", stats["total_sick"] ?: 0)
                                EmojiStat("🌴", stats["total_vacation"] ?: 0)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    WeekHeader(weekStart = settings.weekStart, lang = lang)
                    Spacer(modifier = Modifier.height(8.dp))
                    AnimatedContent(
                        targetState = currentMonth,
                        transitionSpec = {
                            if (targetState.isAfter(initialState)) {
                                (slideInHorizontally(animationSpec = tween(300)) { it } + fadeIn(tween(300))) togetherWith
                                    (slideOutHorizontally(animationSpec = tween(300)) { -it } + fadeOut(tween(300)))
                            } else {
                                (slideInHorizontally(animationSpec = tween(300)) { -it } + fadeIn(tween(300))) togetherWith
                                    (slideOutHorizontally(animationSpec = tween(300)) { it } + fadeOut(tween(300)))
                            }
                        },
                        label = "month"
                    ) { month ->
                        val days = DateUtils.getDaysInMonth(month)
                        val firstDayOffset = DateUtils.getFirstDayOffset(month, settings.weekStart)
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
                                            val shift = selectedSchedule?.let { viewModel.getShiftForDate(it, date) }
                                            DayCell(
                                                day = date.dayOfMonth,
                                                shiftType = shift,
                                                isToday = date == today,
                                                isCurrentMonth = date.month == month.month,
                                                showEmoji = settings.showEmoji,
                                                modifier = Modifier.weight(1f),
                                                onClick = { editDay = date },
                                                onLongClick = { whoWhereDate = date }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
    if (showSchedulePicker) {
        AlertDialog(
            onDismissRequest = { showSchedulePicker = false },
            title = { Text(tr("select_schedule")) },
            text = {
                Column {
                    schedules.forEach { schedule ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { viewModel.selectSchedule(schedule.id); showSchedulePicker = false }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = schedule.id == selectedScheduleId, onClick = { viewModel.selectSchedule(schedule.id); showSchedulePicker = false })
                            Text(schedule.name, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                    TextButton(onClick = { showSchedulePicker = false; showCreateModal = true }) { Text(tr("add_schedule")) }
                }
            },
            confirmButton = { TextButton(onClick = { showSchedulePicker = false }) { Text(tr("close")) } }
        )
    }
    whoWhereDate?.let { date ->
        AlertDialog(
            onDismissRequest = { whoWhereDate = null },
            title = { Text(tr("who_where") + " · ${date.dayOfMonth}.${date.monthValue}") },
            text = {
                Column {
                    viewModel.getShiftsForDate(date).forEach { (schedule, shift) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(schedule.name, style = MaterialTheme.typography.bodyLarge)
                            Text(text = shift?.let { it.emoji + " " + it.displayName(lang) } ?: "—", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { whoWhereDate = null }) { Text(tr("close")) } }
        )
    }
    editDay?.let { date ->
        EditDayModal(
            schedules = schedules,
            date = date,
            currentShift = selectedSchedule?.let { viewModel.getShiftForDate(it, date) },
            onDismiss = { editDay = null },
            onSave = { schedule, type, range, days, cycle ->
                viewModel.updateDayException(schedule, date, type.code, range, days, cycle)
                editDay = null
            },
            onClear = { schedule ->
                viewModel.clearDayException(schedule, date)
                editDay = null
            }
        )
    }
    if (showCreateModal) {
        EditScheduleModal(
            initial = null,
            templates = templates,
            onDismiss = { showCreateModal = false },
            onSave = { schedule ->
                viewModel.addSchedule(schedule)
                viewModel.selectSchedule(viewModel.allSchedules.value.lastOrNull()?.id ?: schedule.id)
                viewModel.updateSettings(settings.copy(hasCompletedOnboarding = true))
                showCreateModal = false
            }
        )
    }
    editSchedule?.let { schedule ->
        EditScheduleModal(
            initial = schedule,
            templates = templates,
            onDismiss = { editSchedule = null },
            onSave = { updated ->
                viewModel.updateSchedule(updated)
                editSchedule = null
            }
        )
    }
}

@Composable
private fun EmojiStat(icon: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 16.sp)
        Text(value.toString(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HeaderChip(text: String, onClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surface).clickable { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); onClick() }.padding(horizontal = 12.dp, vertical = 6.dp)) {
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
