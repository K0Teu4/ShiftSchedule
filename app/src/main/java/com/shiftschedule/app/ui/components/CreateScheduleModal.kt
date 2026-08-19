package com.shiftschedule.app.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.data.model.Template
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScheduleModal(
    templates: List<Template>,
    onDismiss: () -> Unit,
    onSave: (Schedule) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedTemplateId by remember { mutableStateOf<Int?>(null) }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var color by remember { mutableStateOf("#5856D6") }

    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val trimmedName = name.trim()
    val canSave = trimmedName.isNotBlank()

    val colorOptions = listOf(
        "#5856D6", "#34C759", "#FF9500", "#FF3B30", "#AF52DE", "#00C7BE"
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Новый график",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название") },
                placeholder = { Text("Например: Я, Жена, Петя") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (name.isNotEmpty() && trimmedName.isEmpty()) {
                Text(
                    "Имя не может состоять только из пробелов",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Цвет графика",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                colorOptions.forEach { c ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(c)))
                            .then(
                                if (c == color) Modifier.background(
                                    Color.Transparent,
                                    CircleShape
                                ) else Modifier
                            )
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(c)))
                                .size(30.dp)
                                .then(
                                    if (c == color) Modifier.padding(2.dp) else Modifier
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Шаблон",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedTemplateId == null,
                    onClick = { selectedTemplateId = null }
                )
                Text(
                    text = "Без шаблона (ручной)",
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            templates.forEach { template ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = template.id == selectedTemplateId,
                        onClick = { selectedTemplateId = template.id }
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = template.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                        Text(
                            text = template.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = startDate.format(dateFormatter),
                onValueChange = {},
                label = { Text("Дата начала") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.Button(
                    onClick = {
                        onSave(
                            Schedule(
                                name = trimmedName,
                                color = color,
                                templateId = selectedTemplateId,
                                startDate = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                            )
                        )
                    },
                    enabled = canSave
                ) {
                    Text("Создать")
                }
                androidx.compose.material3.OutlinedButton(onClick = onDismiss) {
                    Text("Отмена")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}