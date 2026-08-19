import java.util.UUID
package com.shiftschedule.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DatePickerDefaults
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
import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.data.model.Template
import com.shiftschedule.app.util.DateUtils
import com.shiftschedule.app.util.tr
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScheduleModal(
    initial: Schedule?,
    templates: List<Template>,
    onDismiss: () -> Unit,
    onSave: (Schedule) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var color by remember { mutableStateOf(initial?.color ?: "#5856D6") }
    var templateId by remember { mutableStateOf(initial?.templateId) }
    var startDate by remember { mutableStateOf(initial?.startDate?.let { DateUtils.parseDate(it) } ?: LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val trimmed = name.trim()
    val canSave = trimmed.isNotBlank()
    val colorOptions = listOf(
        "#5856D6", "#34C759", "#FF9500", "#FF3B30", "#AF52DE", "#00C7BE",
        "#FF2D55", "#5AC8FA", "#FFCC00", "#8E8E93", "#007AFF", "#FF6B35"
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (initial == null) tr("new_schedule") else tr("edit_schedule"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(tr("name_label")) },
                placeholder = { Text(tr("placeholder_name")) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (name.isNotEmpty() && trimmed.isEmpty()) {
                Text(
                    tr("name_error"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(tr("color"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                colorOptions.take(6).forEach { c ->
                    val selected = c == color
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(c)))
                            .then(
                                if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                                else Modifier
                            )
                            .clickable { color = c }
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                colorOptions.drop(6).take(6).forEach { c ->
                    val selected = c == color
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(c)))
                            .then(
                                if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                                else Modifier
                            )
                            .clickable { color = c }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(tr("template"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { templateId = UUID.randomUUID().toString() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = templateId == null, onClick = { templateId = UUID.randomUUID().toString() })
                Text(
                    tr("no_template"),
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            templates.forEach { template ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { templateId = UUID.randomUUID().toString().id },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = templateId == template.id, onClick = { templateId = UUID.randomUUID().toString().id })
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(template.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            template.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(tr("start_date"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        onSave(
                            Schedule(
                                id = UUID.randomUUID().toString()?.id ?: 0,
                                name = trimmed,
                                color = color,
                                templateId = UUID.randomUUID().toString(),
                                startDate = DateUtils.formatDate(startDate),
                                isActive = initial?.isActive ?: true,
                                exceptions = initial?.exceptions ?: emptyMap(),
                                cycleShifts = initial?.cycleShifts ?: emptyMap()
                            )
                        )
                    },
                    enabled = canSave
                ) {
                    Text(if (initial == null) tr("create") else tr("save"))
                }
                OutlinedButton(onClick = onDismiss) { Text(tr("cancel")) }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )

        DatePickerDialog(
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                headlineContentColor = MaterialTheme.colorScheme.onSurface,
                weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                yearContentColor = MaterialTheme.colorScheme.onSurface,
                currentYearContentColor = MaterialTheme.colorScheme.primary,
                selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                dayContentColor = MaterialTheme.colorScheme.onSurface,
                selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                todayContentColor = MaterialTheme.colorScheme.primary,
                todayDateBorderColor = MaterialTheme.colorScheme.primary,
                dayInSelectionRangeContentColor = MaterialTheme.colorScheme.onPrimary,
                dayInSelectionRangeContainerColor = MaterialTheme.colorScheme.primary
            ),
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        startDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(tr("cancel"))
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    headlineContentColor = MaterialTheme.colorScheme.onSurface,
                    weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    yearContentColor = MaterialTheme.colorScheme.onSurface,
                    currentYearContentColor = MaterialTheme.colorScheme.primary,
                    selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                    selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                    dayContentColor = MaterialTheme.colorScheme.onSurface,
                    selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    todayContentColor = MaterialTheme.colorScheme.primary,
                    todayDateBorderColor = MaterialTheme.colorScheme.primary,
                    dividerColor = MaterialTheme.colorScheme.outline,
                    dateTextFieldColors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            )
        }
    }
}
