package com.shiftschedule.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.data.model.ShiftType
import com.shiftschedule.app.util.DateUtils
import com.shiftschedule.app.util.LocalLang
import com.shiftschedule.app.util.tr
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDayModal(schedules: List<Schedule>, selectedScheduleId: Int?, date: LocalDate, currentShift: ShiftType?, onDismiss: () -> Unit, onSave: (Schedule, ShiftType, String, Int, Boolean) -> Unit, onClear: (Schedule) -> Unit = {}) {
    var selectedIds by remember(selectedScheduleId, schedules) { mutableStateOf(selectedScheduleId?.takeIf { id -> schedules.any { it.id == id } }?.let { setOf(it) } ?: schedules.firstOrNull()?.let { setOf(it.id) } ?: emptySet()) }
    var shift by remember { mutableStateOf(currentShift ?: ShiftType.DAY) }
    var range by remember { mutableStateOf("this_day") }
    var days by remember { mutableStateOf(1) }
    var cycle by remember { mutableStateOf(false) }
    val period = shift == ShiftType.SICK || shift == ShiftType.VACATION
    val hasException = schedules.any { it.id in selectedIds && it.exceptions.containsKey(DateUtils.formatDate(date)) }

    ModalBottomSheet(onDismissRequest = onDismiss, shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text("${date.dayOfMonth}.${date.monthValue}.${date.year}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text("Изменение смены", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 3.dp))
            Spacer(Modifier.padding(top = 12.dp))
            Text("Графики", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            schedules.forEach { schedule ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = schedule.id in selectedIds, onCheckedChange = { checked -> selectedIds = if (checked) selectedIds + schedule.id else if (selectedIds.size > 1) selectedIds - schedule.id else selectedIds }); Text(schedule.name, Modifier.padding(start = 6.dp), fontWeight = FontWeight.SemiBold) }
            }
            Spacer(Modifier.padding(top = 12.dp))
            Text("Смена", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            ShiftType.values().chunked(2).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { row.forEach { type -> Surface(onClick = { shift = type }, shape = RoundedCornerShape(16.dp), color = if (type == shift) type.color.copy(alpha = .18f) else MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.weight(1f)) { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Text(type.emoji); Text(type.displayName(LocalLang.current), Modifier.padding(start = 7.dp), fontWeight = if (type == shift) FontWeight.Bold else FontWeight.Medium) } } } } }
            Spacer(Modifier.padding(top = 14.dp))
            if (period) {
                Text("Продолжительность", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = { if (days > 1) days-- }) { Text("−") }; Text("$days дн.", Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold); IconButton(onClick = { if (days < 90) days++ }) { Text("+") } }
                Text("После периода", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 5.dp, bottom = 6.dp))
                Choice("Продолжить текущий цикл", !cycle) { cycle = false }
                Choice("Сдвинуть цикл на $days дн.", cycle) { cycle = true }
            } else if (schedules.any { it.templateId != null }) {
                Text("Применить", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Choice("Только этот день", range == "this_day") { range = "this_day" }
                Choice("Этот и следующие", range == "this_and_following") { range = "this_and_following" }
                Choice("Весь график", range == "entire_schedule") { range = "entire_schedule" }
            }
            Spacer(Modifier.padding(top = 14.dp))
            Button(onClick = { selectedIds.forEach { id -> schedules.firstOrNull { it.id == id }?.let { onSave(it, shift, range, days, cycle) } }; onDismiss() }, enabled = selectedIds.isNotEmpty(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp)) { Text("Сохранить", fontWeight = FontWeight.Bold) }
            if (hasException) OutlinedButton(onClick = { selectedIds.forEach { id -> schedules.firstOrNull { it.id == id }?.let(onClear) }; onDismiss() }, modifier = Modifier.fillMaxWidth().padding(top = 7.dp), shape = RoundedCornerShape(17.dp)) { Text("Сбросить изменение", color = MaterialTheme.colorScheme.error) }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Отмена") }
            Spacer(Modifier.padding(bottom = 24.dp))
        }
    }
}

@Composable private fun Choice(label: String, selected: Boolean, onClick: () -> Unit) { Surface(onClick = onClick, shape = RoundedCornerShape(14.dp), color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Text(if (selected) "●" else "○", color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant); Text(label, Modifier.padding(start = 9.dp), fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) } } }
