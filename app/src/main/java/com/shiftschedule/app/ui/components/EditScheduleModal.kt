package com.shiftschedule.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.data.model.ShiftType
import com.shiftschedule.app.data.model.Template
import com.shiftschedule.app.util.DateUtils
import com.shiftschedule.app.util.tr
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScheduleModal(initial: Schedule?, templates: List<Template>, defaultHourRate: Int = 0, defaultDayHours: Int = 8, defaultNightHours: Int = 16, onDismiss: () -> Unit, onSave: (Schedule) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var color by remember { mutableStateOf(initial?.color ?: "#6750A4") }
    var templateId by remember { mutableStateOf(initial?.templateId) }
    var startDate by remember { mutableStateOf(initial?.startDate?.let(DateUtils::tryParseDate) ?: LocalDate.now()) }
    var rate by remember { mutableStateOf(initial?.hourRate ?: defaultHourRate.coerceIn(0, 1_000_000)) }
    var dayHours by remember { mutableStateOf(initial?.dayHours ?: defaultDayHours.coerceIn(1, 24)) }
    var nightHours by remember { mutableStateOf(initial?.nightHours ?: defaultNightHours.coerceIn(1, 24)) }
    var datePicker by remember { mutableStateOf(false) }
    val colors = listOf("#6750A4", "#34C759", "#FF9500", "#FF3B30", "#AF52DE", "#00C7BE", "#FF2D55", "#5AC8FA", "#FFCC00", "#8E8E93", "#007AFF", "#FF6B35")
    val selectedTemplate = templates.firstOrNull { it.id == templateId }

    ModalBottomSheet(onDismissRequest = onDismiss, shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(if (initial == null) "Новый график" else "Настроить график", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text(if (initial == null) "Пара минут — и календарь будет считать всё сам." else "Изменения сохраняются только для этого графика.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            Spacer(Modifier.size(18.dp))

            OutlinedTextField(name, { name = it.take(40) }, label = { Text("Название") }, placeholder = { Text("Основная работа") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
            Spacer(Modifier.size(16.dp))
            Text("Цвет", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth().padding(top = 9.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) { colors.take(6).forEach { ColorChoice(it, color == it) { color = it } } }
            Row(Modifier.fillMaxWidth().padding(top = 9.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) { colors.drop(6).forEach { ColorChoice(it, color == it) { color = it } } }

            Spacer(Modifier.size(18.dp))
            Text("Ритм смен", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Выберите готовый цикл или оставьте ручной режим.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 3.dp, bottom = 8.dp))
            Surface(onClick = { templateId = null }, shape = RoundedCornerShape(16.dp), color = if (templateId == null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Text("✦", color = MaterialTheme.colorScheme.primary, fontSize = 18.sp); Column(Modifier.padding(start = 10.dp)) { Text("Без шаблона", fontWeight = FontWeight.Bold); Text("Смены меняются вручную", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
            templates.forEach { template ->
                val active = template.id == templateId
                Surface(onClick = { templateId = template.id }, shape = RoundedCornerShape(16.dp), color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Text(template.name, color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold, modifier = Modifier.size(width = 52.dp, height = 24.dp)); Column(Modifier.padding(start = 10.dp)) { Text(template.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); if (active) Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) { template.getPatternList().take(8).forEach { code -> ShiftType.fromCode(code)?.let { t -> Text(t.emoji, modifier = Modifier.background(t.color.copy(alpha = .15f), RoundedCornerShape(8.dp)).padding(4.dp)) } } } } }
                }
            }

            Spacer(Modifier.size(18.dp))
            Text("Расчёт", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(if (rate == 0) "" else rate.toString(), { rate = it.filter(Char::isDigit).take(7).toIntOrNull() ?: 0 }, label = { Text("₽/час") }, singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp))
                OutlinedTextField(dayHours.toString(), { it.filter(Char::isDigit).take(2).toIntOrNull()?.takeIf { n -> n in 1..24 }?.let { dayHours = it } }, label = { Text("День") }, singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp))
                OutlinedTextField(nightHours.toString(), { it.filter(Char::isDigit).take(2).toIntOrNull()?.takeIf { n -> n in 1..24 }?.let { nightHours = it } }, label = { Text("Ночь") }, singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp))
            }
            Spacer(Modifier.size(16.dp))
            Text("Начало цикла", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Surface(onClick = { datePicker = true }, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) { Text("${startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold); Text("Изменить", modifier = Modifier.weight(1f).padding(start = 10.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End, color = MaterialTheme.colorScheme.primary) } }
            Spacer(Modifier.size(20.dp))
            Button(onClick = { onSave(Schedule(id = initial?.id ?: 0, name = name.trim(), color = color, templateId = templateId, startDate = DateUtils.formatDate(startDate), isActive = initial?.isActive ?: true, exceptions = initial?.exceptions ?: emptyMap(), cycleShifts = initial?.cycleShifts ?: emptyMap(), hourRate = rate, dayHours = dayHours, nightHours = nightHours)) }, enabled = name.trim().isNotEmpty(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp)) { Text(if (initial == null) "Создать график" else "Сохранить", fontWeight = FontWeight.Bold) }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Отмена") }
            Spacer(Modifier.size(28.dp))
        }
    }

    if (datePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = startDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(onDismissRequest = { datePicker = false }, confirmButton = { TextButton(onClick = { state.selectedDateMillis?.let { startDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }; datePicker = false }) { Text("Готово") } }, dismissButton = { TextButton(onClick = { datePicker = false }) { Text("Отмена") } }) { DatePicker(state = state) }
    }
}

@Composable private fun ColorChoice(hex: String, selected: Boolean, onClick: () -> Unit) { Box(Modifier.size(38.dp).clip(CircleShape).background(Color(android.graphics.Color.parseColor(hex))).border(if (selected) 3.dp else 0.dp, if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent, CircleShape).clickable(onClick = onClick)) }
