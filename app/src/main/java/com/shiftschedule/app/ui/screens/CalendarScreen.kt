package com.shiftschedule.app.ui.screens

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.data.model.ShiftType
import com.shiftschedule.app.ui.components.AppHeader
import com.shiftschedule.app.ui.components.DayCell
import com.shiftschedule.app.ui.components.EditDayModal
import com.shiftschedule.app.ui.components.EditScheduleModal
import com.shiftschedule.app.ui.components.EmptyState
import com.shiftschedule.app.ui.components.SectionLabel
import com.shiftschedule.app.ui.components.ShiftHeroCard
import com.shiftschedule.app.ui.components.ShiftStatPill
import com.shiftschedule.app.ui.components.SurfaceCard
import com.shiftschedule.app.ui.components.TemplateEditorModal
import com.shiftschedule.app.ui.components.WeekHeader
import com.shiftschedule.app.ui.components.OnboardingScreen
import com.shiftschedule.app.ui.viewmodel.ShiftViewModel
import com.shiftschedule.app.util.DateUtils
import com.shiftschedule.app.util.LocalLang
import com.shiftschedule.app.util.monthLocale
import com.shiftschedule.app.util.tr
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(viewModel: ShiftViewModel) {
    val schedules by viewModel.allSchedules.collectAsState()
    val templates by viewModel.allTemplates.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val selectedId by viewModel.selectedScheduleId.collectAsState()
    val loaded by viewModel.isLoaded.collectAsState()
    val lang = LocalLang.current

    var showCreate by remember { mutableStateOf(false) }
    var showCreateTemplate by remember { mutableStateOf(false) }
    var showPicker by remember { mutableStateOf(false) }
    var editDay by remember { mutableStateOf<LocalDate?>(null) }
    var whoWhere by remember { mutableStateOf<LocalDate?>(null) }

    val selected by remember(schedules, selectedId) { derivedStateOf { schedules.firstOrNull { it.id == selectedId } ?: schedules.firstOrNull() } }

    LaunchedEffect(schedules, selectedId) {
        if (schedules.isEmpty()) viewModel.selectSchedule(null)
        else if (selectedId == null || schedules.none { it.id == selectedId }) viewModel.selectSchedule(schedules.first().id)
    }

    if (!loaded) return
    if (!settings.hasCompletedOnboarding && schedules.isEmpty()) {
        OnboardingScreen(onCreateClick = { showCreate = true })
        if (showCreate) {
            EditScheduleModal(null, templates, onDismiss = { showCreate = false }, onSave = { schedule ->
                viewModel.addSchedule(schedule) { viewModel.selectSchedule(it) }
                viewModel.updateSettings(settings.copy(hasCompletedOnboarding = true))
                showCreate = false
            }, onCreateTemplate = { showCreate = false; showCreateTemplate = true }, showEmoji = settings.showEmoji)
        }
        return
    }

    Scaffold(
        floatingActionButton = { FloatingActionButton(onClick = { showCreate = true }, shape = RoundedCornerShape(20.dp)) { Icon(Icons.Filled.Add, tr("add_schedule")) } }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).widthIn(max = 720.dp).padding(horizontal = 18.dp),
            verticalArrangement = spacedBy(14.dp)
        ) {
            item { Spacer(Modifier.height(6.dp)) }
            item {
                AppHeader(
                    title = "ShiftWeave",
                    subtitle = DateUtils.monthTitle(currentMonth, monthLocale()),
                    action = {
                        Surface(onClick = { showPicker = true }, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.widthIn(max = 150.dp)) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    selected?.name ?: tr("schedule_label"),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(Icons.Filled.ExpandMore, null, modifier = Modifier.padding(start = 2.dp))
                            }
                        }
                    }
                )
            }
            if (selected != null) {
                item {
                    val today = LocalDate.now()
                    val todayShift = viewModel.getShiftForDate(selected!!, today)
                    val next = (1..120).asSequence().map { today.plusDays(it.toLong()) }.map { it to viewModel.getShiftForDate(selected!!, it) }.firstOrNull { it.second == ShiftType.DAY || it.second == ShiftType.NIGHT }
                    ShiftHeroCard(
                        dateLabel = "Сегодня · ${today.dayOfMonth}.${today.monthValue}",
                        shift = todayShift,
                        secondaryLabel = next?.let { "Следующая · ${it.first.dayOfMonth}.${it.first.monthValue} · ${it.second?.displayName(lang)}" } ?: "Следующая смена не найдена",
                        showEmoji = settings.showEmoji
                    )
                }
                item {
                    val stats = viewModel.getMonthStats(listOf(selected!!.id), currentMonth)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = spacedBy(8.dp)) {
                        ShiftStatPill(ShiftType.DAY, (stats["total_day"] ?: 0).toString(), settings.showEmoji, Modifier.weight(1f))
                        ShiftStatPill(ShiftType.NIGHT, (stats["total_night"] ?: 0).toString(), settings.showEmoji, Modifier.weight(1f))
                        ShiftStatPill(ShiftType.OFF, (stats["total_off"] ?: 0).toString(), settings.showEmoji, Modifier.weight(1f))
                    }
                }
                item {
                    val stats = viewModel.getMonthStats(listOf(selected!!.id), currentMonth)
                    SurfaceCard {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Итоги месяца", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${(stats["total_day"] ?: 0) + (stats["total_night"] ?: 0)} рабочих смен", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                            }
                            Text("${stats["total_off"] ?: 0} выходных", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                item { EmptyState(tr("no_schedules_yet"), tr("create_first"), tr("add_schedule"), { showCreate = true }) }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.previousMonth() }) { Icon(Icons.Filled.ChevronLeft, tr("prev_month")) }
                    Text(DateUtils.monthTitle(currentMonth, monthLocale()), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    IconButton(
                        onClick = { viewModel.goToday() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                            Icon(Icons.Filled.Today, "Сегодня", modifier = Modifier.padding(9.dp))
                        }
                    }
                    IconButton(onClick = { viewModel.nextMonth() }) { Icon(Icons.Filled.ChevronRight, tr("next_month")) }
                }
            }
            item { WeekHeader(settings.weekStart, lang) }
            item {
                val days = DateUtils.getDaysInMonth(currentMonth)
                val offset = DateUtils.getFirstDayOffset(currentMonth, settings.weekStart)
                val cells = buildList<LocalDate?> { repeat(offset) { add(null) }; addAll(days); while (size % 7 != 0) add(null) }
                Column(
                    modifier = Modifier.pointerInput(currentMonth) {
                        var totalDrag = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { totalDrag = 0f },
                            onHorizontalDrag = { change, amount -> change.consume(); totalDrag += amount },
                            onDragEnd = {
                                when {
                                    totalDrag < -80f -> viewModel.nextMonth()
                                    totalDrag > 80f -> viewModel.previousMonth()
                                }
                            }
                        )
                    },
                    verticalArrangement = spacedBy(6.dp)
                ) {
                    cells.chunked(7).forEach { week ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = spacedBy(6.dp)) {
                            week.forEach { date ->
                                if (date == null) Box(Modifier.weight(1f).height(58.dp)) else DayCell(
                                    day = date.dayOfMonth,
                                    shiftType = selected?.let { viewModel.getShiftForDate(it, date) },
                                    isToday = date == LocalDate.now(),
                                    isCurrentMonth = true,
                                    showEmoji = settings.showEmoji,
                                    isHoliday = settings.rfHolidays && com.shiftschedule.app.util.RuHolidays.isHoliday(date),
                                    modifier = Modifier.weight(1f),
                                    onClick = { editDay = date },
                                    onLongClick = { whoWhere = date }
                                )
                            }
                        }
                    }
                }
            }
            item { ShiftLegend(showEmoji = settings.showEmoji) }
            item { Spacer(Modifier.height(84.dp)) }
        }
    }

    if (showPicker) {
        ModalBottomSheet(onDismissRequest = { showPicker = false }) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text("Выберите график", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(12.dp))
                schedules.forEach { schedule ->
                    Surface(onClick = { viewModel.selectSchedule(schedule.id); showPicker = false }, shape = RoundedCornerShape(18.dp), color = if (schedule.id == selectedId) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(schedule.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            if (schedule.id == selectedId) Text("Активен", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }

    editDay?.let { date ->
        EditDayModal(schedules, selectedId, date, selected?.let { viewModel.getShiftForDate(it, date) }, { editDay = null }, { schedule, type, range, days, cycle -> viewModel.updateDayException(schedule, date, type.code, range, days, cycle); editDay = null }, { schedule -> viewModel.clearDayException(schedule, date); editDay = null })
    }
    whoWhere?.let { date ->
        ModalBottomSheet(onDismissRequest = { whoWhere = null }) {
            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                Text("Графики · ${date.dayOfMonth}.${date.monthValue}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(12.dp))
                viewModel.getShiftsForDate(date).forEach { (schedule, shift) ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(schedule.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text(shift?.let { "${it.emoji} ${it.displayName(lang)}" } ?: "—", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
    if (showCreate) {
        EditScheduleModal(null, templates, onDismiss = { showCreate = false }, onSave = { schedule ->
            viewModel.addSchedule(schedule) { viewModel.selectSchedule(it) }
            viewModel.updateSettings(settings.copy(hasCompletedOnboarding = true))
            showCreate = false
        }, onCreateTemplate = { showCreate = false; showCreateTemplate = true }, showEmoji = settings.showEmoji)
    }
    if (showCreateTemplate) {
        TemplateEditorModal(null, onDismiss = { showCreateTemplate = false }, onSave = { viewModel.addTemplate(it); showCreateTemplate = false; showCreate = true })
    }
}
