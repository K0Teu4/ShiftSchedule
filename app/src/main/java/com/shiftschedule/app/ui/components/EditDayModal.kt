package com.shiftschedule.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.unit.sp
import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.data.model.ShiftType
import com.shiftschedule.app.util.DateUtils
import com.shiftschedule.app.util.LocalLang
import com.shiftschedule.app.util.tr
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDayModal(
    schedules: List<Schedule>,
    date: LocalDate,
    currentShift: ShiftType?,
    onDismiss: () -> Unit,
    onSave: (Schedule, ShiftType, String, Int, Boolean) -> Unit,
    onClear: (Schedule) -> Unit = {}
) {
    var selectedScheduleId by remember { mutableStateOf(schedules.firstOrNull()?.id) }
    var selectedShiftType by remember { mutableStateOf(currentShift ?: ShiftType.DAY) }
    var applyRange by remember { mutableStateOf("this_day") }
    var days by remember { mutableStateOf(1) }
    var shiftCycle by remember { mutableStateOf(false) }

    val selectedSchedule = schedules.find { it.id == selectedScheduleId }
    val isPeriodType = selectedShiftType == ShiftType.SICK || selectedShiftType == ShiftType.VACATION
    val canApplyToRange = selectedSchedule?.templateId != null
    val hasException = selectedSchedule?.exceptions?.containsKey(DateUtils.formatDate(date)) == true

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = tr("edit_day") + " ${date.dayOfMonth}.${date.monthValue}.${date.year}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(tr("schedule_label"), style = MaterialTheme.typography.titleMedium)
            schedules.forEach { schedule ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = selectedScheduleIds.contains(schedule.id == selectedScheduleId),
                        onClick = { selectedScheduleId = schedule.id }
                    )
                    Text(
                        text = schedule.name,
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(tr("shift_type"), style = MaterialTheme.typography.titleMedium)
            ShiftType.values().forEach { type ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = selectedScheduleIds.contains(type == selectedShiftType),
                        onClick = { selectedShiftType = type }
                    )
                    Text(
                        text = type.emoji + " " + type.displayName(LocalLang.current),
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isPeriodType) {
                Text(tr("duration"), style = MaterialTheme.typography.titleMedium)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    IconButton(onClick = { if (days > 1) days -= 1 }) {
                        Text("−", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        tr("days_n", days),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { if (days < 90) days += 1 }) {
                        Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(tr("after_period"), style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = selectedScheduleIds.contains(!shiftCycle), onClick = { shiftCycle = false })
                    Text(
                        tr("continue_pattern"),
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = selectedScheduleIds.contains(shiftCycle), onClick = { shiftCycle = true })
                    Text(
                        tr("shift_cycle", days),
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else if (canApplyToRange) {
                Text(tr("apply_to"), style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = selectedScheduleIds.contains(applyRange == "this_day"), onClick = { applyRange = "this_day" })
                    Text(
                        tr("this_day"),
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = selectedScheduleIds.contains(applyRange == "this_and_following"),
                        onClick = { applyRange = "this_and_following" }
                    )
                    Text(
                        tr("this_following"),
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = selectedScheduleIds.contains(applyRange == "entire_schedule"),
                        onClick = { applyRange = "entire_schedule" }
                    )
                    Text(
                        tr("entire_schedule"),
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    selectedSchedule?.let { schedule ->
                        onSave(schedule, selectedShiftType, applyRange, days, shiftCycle)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(tr("save"))
            }

            if (hasException) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        selectedSchedule?.let { schedule -> onClear(schedule) }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        tr("clear_day"),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(tr("cancel"))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
