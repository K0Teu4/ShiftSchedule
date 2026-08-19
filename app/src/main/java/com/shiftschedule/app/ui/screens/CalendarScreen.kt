package com.shiftschedule.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.shiftschedule.app.data.model.ShiftType
import com.shiftschedule.app.ui.components.CalendarSkeleton
import com.shiftschedule.app.ui.components.DayCell
import com.shiftschedule.app.ui.components.EditDayModal
import com.shiftschedule.app.ui.components.EditScheduleModal
import com.shiftschedule.app.ui.components.OnboardingScreen
import com.shiftschedule.app.ui.components.TipCard
import com.shiftschedule.app.ui.components.WeekHeader
import com.shiftschedule.app.ui.viewmodel.ShiftViewModel
import com.shiftschedule.app.util.DateUtils
import com.shiftschedule.app.util.monthLocale
import com.shiftschedule.app.util.tr
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: ShiftViewModel) {
    val schedules by viewModel.allSchedules.collectAsState()
    val templates by viewModel.allTemplates.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val selectedScheduleId by viewModel.selectedScheduleId.collectAsState()
    val isLoaded by viewModel.isLoaded.collectAsState()

    var showCreateModal by remember { mutableStateOf(false) }
    var showSchedulePicker by remember { mutableStateOf(false) }
    var editingDate by remember { mutableStateOf<LocalDate?>(null) }
    var whoWhereDate by remember { mutableStateOf<LocalDate?>(null) }
    var dragAmountX by remember { mutableStateOf(0f) }
    val haptics = LocalHapticFeedback.current

    val selectedSchedule by remember(schedules, selectedScheduleId) {
        derivedStateOf { schedules.find { it.id == selectedScheduleId } ?: schedules.firstOrNull() }
    }
    val today = LocalDate.now()

    LaunchedEffect(schedules) {
        if (selectedScheduleId == null && schedules.isNotEmpty()) {
            viewModel.selectSchedule(schedules.first().id)
        }
        if (schedules.isNotEmpty() && !settings.hasCompletedOnboarding) {
            viewModel.updateSettings(settings.copy(hasCompletedOnboarding = true))
        }
    }

    if (!isLoaded) {
        CalendarSkeleton()
        return
    }

    if (!settings.hasCompletedOnboarding && schedules.isEmpty()) {
        OnboardingScreen(onCreateClick = { showCreateModal = true })
        return
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); viewModel.previousMonth() }) {
                        Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = tr("prev_month"))
                    }
                    Text(
                        text = DateUtils.monthTitle(currentMonth, monthLocale()),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); viewModel.nextMonth() }) {
                        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = tr("next_month"))
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeaderChip(tr("today")) { viewModel.goToday() }
                    Spacer(modifier = Modifier.weight(1f))
                    if (schedules.isNotEmpty()) {
                        HeaderChip((selectedSchedule?.name ?: tr("schedule_label")) + " ▾") {
                            showSchedulePicker = true
                        }
                    }
                }

                selectedSchedule?.let { schedule ->
                    val todayShift = viewModel.getShiftForDate(schedule, today)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tr("today_label"), style = MaterialTheme.typography.titleMedium)
                                if (todayShift != null) {
                                    Text(
                                        text = (if (settings.showEmoji) todayShift.emoji else todayShift.letter) +
                                            " " + todayShift.displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        tr("no_shift"),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            val stats = viewModel.getMonthStats(listOf(schedule.id), currentMonth)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                StatChip(tr("day_shifts"), stats["total_day"] ?: 0, Color(0xFF34C759))
                                StatChip(tr("night_shifts"), stats["total_night"] ?: 0, Color(0xFF5856D6))
                                StatChip(tr("off_days"), stats["total_off"] ?: 0, Color(0xFFFF9500))
                            }
                        }
                    }
                }

                Text(
                    text = tr("hint_calendar"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )

                if (!viewModel.isTipSeen("calendar")) {
                    TipCard(
                        text = tr("tip_calendar"),
                        onClose = { viewModel.markTipSeen("calendar") },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateModal = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = tr("add_schedule"))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShiftType.values().forEach { type ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = if (settings.showEmoji) type.emoji else type.letter)
                        Text(
                            text = type.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            WeekHeader(
                weekStart = settings.weekStart,
                lang = androidx.compose.ui.platform.LocalContext.current.let { com.shiftschedule.app.util.LocalLang.current },
                modifier = Modifier.padding(vertical = 8.dp)
            )

            AnimatedContent(
                targetState = currentMonth,
                transitionSpec = {
                    if (targetState.isAfter(initialState)) {
                        (slideInHorizontally(initialOffsetX = { it / 4 }) + fadeIn()) togetherWith
                            (slideOutHorizontally(targetOffsetX = { -it / 4 }) + fadeOut())
                    } else {
                        (slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn()) togetherWith
                            (slideOutHorizontally(targetOffsetX = { it / 4 }) + fadeOut())
                    }
                },
                label = "monthTransition"
            ) { month ->
                val days = DateUtils.getDaysInMonth(month)
                val firstDayOffset = DateUtils.getFirstDayOffset(month, settings.weekStart)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(firstDayOffset) {
                        Box(modifier = Modifier.aspectRatio(1f))
                    }

                    items(days) { date ->
                        val shift = selectedSchedule?.let { viewModel.getShiftForDate(it, date) }

                        DayCell(
                            day = date.dayOfMonth,
                            shiftType = shift,
                            isToday = date == today,
                            isCurrentMonth = date.month == month.month,
                            showEmoji = settings.showEmoji,
                            isSharedDayOff = false,
                            onClick = { editingDate = date },
                            onLongClick = { whoWhereDate = date }
                        )
                    }
                }
            }
        }
    }

    if (showSchedulePicker) {
        ModalBottomSheet(onDismissRequest = { showSchedulePicker = false }) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    tr("select_schedule"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                schedules.forEach { schedule ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectSchedule(schedule.id)
                                showSchedulePicker = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(schedule.color)))
                        )
                        Text(
                            schedule.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (schedule.id == selectedScheduleId) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                        if (schedule.id == selectedScheduleId) {
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                "✓",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showCreateModal) {
        EditScheduleModal(
            initial = null,
            templates = templates,
            onDismiss = { showCreateModal = false },
            onSave = { schedule ->
                viewModel.addSchedule(schedule)
                showCreateModal = false
            }
        )
    }

    editingDate?.let { date ->
        EditDayModal(
            schedules = schedules,
            date = date,
            currentShift = selectedSchedule?.let { viewModel.getShiftForDate(it, date) },
            onDismiss = { editingDate = null },
            onSave = { sched, shift, range, days, shiftCycle ->
                viewModel.updateDayException(sched, date, shift.code, range, days, shiftCycle)
                editingDate = null
            },
            onClear = { sched ->
                viewModel.clearDayException(sched, date)
                editingDate = null
            }
        )
    }

    whoWhereDate?.let { date ->
        val list = viewModel.getShiftsForDate(date)
        AlertDialog(
            onDismissRequest = { whoWhereDate = null },
            title = { Text(tr("who_where") + " · ${date.dayOfMonth}.${date.monthValue}") },
            text = {
                Column {
                    list.forEach { (schedule, shift) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(schedule.name, style = MaterialTheme.typography.bodyLarge)
                            if (shift != null) {
                                Text(
                                    (if (settings.showEmoji) shift.emoji else shift.letter) + " " + shift.displayName,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            } else {
                                Text("—", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { whoWhereDate = null }) { Text(tr("close")) }
            }
        )
    }
}

@Composable
private fun HeaderChip(text: String, onClick: () -> Unit) {
    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable {
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onClick()
    }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun StatChip(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}