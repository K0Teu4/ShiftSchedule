package com.shiftschedule.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.shiftschedule.app.data.model.ShiftType
import com.shiftschedule.app.data.model.Template
import com.shiftschedule.app.ui.components.AppHeader
import com.shiftschedule.app.ui.components.EditScheduleModal
import com.shiftschedule.app.ui.components.EmptyState
import com.shiftschedule.app.ui.components.SectionLabel
import com.shiftschedule.app.ui.components.SurfaceCard
import com.shiftschedule.app.ui.components.TemplateEditorModal
import com.shiftschedule.app.ui.viewmodel.ShiftViewModel
import com.shiftschedule.app.util.LocalLang
import com.shiftschedule.app.util.tr

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(viewModel: ShiftViewModel) {
    val schedules by viewModel.allSchedules.collectAsState()
    val templates by viewModel.allTemplates.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val lang = LocalLang.current

    var createTemplate by remember { mutableStateOf(false) }
    var editTemplate by remember { mutableStateOf<Template?>(null) }
    var createSchedule by remember { mutableStateOf(false) }
    var editSchedule by remember { mutableStateOf<Schedule?>(null) }
    var deleteSchedule by remember { mutableStateOf<Schedule?>(null) }
    var deleteTemplate by remember { mutableStateOf<Template?>(null) }

    val filteredSchedules = schedules.filter { it.name.contains(query, true) }
    val filteredTemplates = templates.filter { it.name.contains(query, true) || it.description.contains(query, true) }

    Scaffold(floatingActionButton = { FloatingActionButton(onClick = { createTemplate = true }, shape = RoundedCornerShape(20.dp)) { Icon(Icons.Filled.Add, tr("new_template")) } }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).widthIn(max = 720.dp).padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Spacer(Modifier.height(6.dp)) }
            item {
                AppHeader("Шаблоны", "Соберите ритм смен один раз — календарь повторит его автоматически")
            }
            item {
                OutlinedTextField(value = query, onValueChange = viewModel::setSearchQuery, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Поиск графика или шаблона") }, leadingIcon = { Icon(Icons.Filled.Search, null) }, singleLine = true, shape = RoundedCornerShape(18.dp))
            }
            item { SectionLabel("Мои графики", action = "Добавить", onAction = { createSchedule = true }) }
            if (filteredSchedules.isEmpty()) {
                item { EmptyState("Пока нет графиков", "Создайте первый — он появится в календаре и статистике.", "Создать график", { createSchedule = true }) }
            } else {
                items(filteredSchedules, key = { it.id }) { schedule ->
                    ScheduleCardNew(schedule, templates.firstOrNull { it.id == schedule.templateId }, lang, onEdit = { editSchedule = schedule }, onCopy = { viewModel.duplicateSchedule(schedule) }, onDelete = { deleteSchedule = schedule })
                }
            }
            item { Spacer(Modifier.height(6.dp)); SectionLabel("Готовые шаблоны", action = "Все встроенные") }
            items(filteredTemplates.filter { it.isBuiltIn }, key = { "b${it.id}" }) { template ->
                TemplateCardNew(template, lang, onUse = { createSchedule = true }, onEdit = { editTemplate = template }, onCopy = { viewModel.duplicateTemplate(template) }, onDelete = null)
            }
            item { SectionLabel("Мои шаблоны", action = "Создать", onAction = { createTemplate = true }) }
            val userTemplates = filteredTemplates.filter { !it.isBuiltIn }
            if (userTemplates.isEmpty()) item { SurfaceCard { Column(Modifier.fillMaxWidth().padding(18.dp)) { Text("Создайте свой цикл", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text("Например: 2 день → 2 ночь → 2 выходных.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp)); Button(onClick = { createTemplate = true }, modifier = Modifier.padding(top = 12.dp), shape = RoundedCornerShape(14.dp)) { Text("Новый шаблон") } } } }
            else items(userTemplates, key = { "u${it.id}" }) { template -> TemplateCardNew(template, lang, onUse = { createSchedule = true }, onEdit = { editTemplate = template }, onCopy = { viewModel.duplicateTemplate(template) }, onDelete = { deleteTemplate = template }) }
            item { Spacer(Modifier.height(90.dp)) }
        }
    }

    if (createTemplate) TemplateEditorModal(null, { createTemplate = false }) { viewModel.addTemplate(it); createTemplate = false }
    editTemplate?.let { TemplateEditorModal(it, { editTemplate = null }) { viewModel.updateTemplate(it); editTemplate = null } }
    if (createSchedule) EditScheduleModal(null, templates, settings.hourRate, settings.dayHours, settings.nightHours, { createSchedule = false }) { viewModel.addSchedule(it) { viewModel.selectSchedule(it) }; createSchedule = false }
    editSchedule?.let { EditScheduleModal(it, templates, onDismiss = { editSchedule = null }) { viewModel.updateSchedule(it); editSchedule = null } }
    deleteSchedule?.let { item -> AlertDialog(onDismissRequest = { deleteSchedule = null }, title = { Text("Удалить график?") }, text = { Text("${item.name} будет удалён. Смены-исключения тоже исчезнут.") }, confirmButton = { TextButton(onClick = { viewModel.deleteSchedule(item); deleteSchedule = null }) { Text("Удалить") } }, dismissButton = { TextButton(onClick = { deleteSchedule = null }) { Text("Отмена") } }) }
    deleteTemplate?.let { item -> AlertDialog(onDismissRequest = { deleteTemplate = null }, title = { Text("Удалить шаблон?") }, text = { Text("Графики, которые используют его, останутся, но перестанут быть привязаны к шаблону.") }, confirmButton = { TextButton(onClick = { viewModel.deleteTemplate(item); deleteTemplate = null }) { Text("Удалить") } }, dismissButton = { TextButton(onClick = { deleteTemplate = null }) { Text("Отмена") } }) }
}

@Composable
private fun ScheduleCardNew(schedule: Schedule, template: Template?, lang: String, onEdit: () -> Unit, onCopy: () -> Unit, onDelete: () -> Unit) {
    val accent = runCatching { Color(android.graphics.Color.parseColor(schedule.color)) }.getOrDefault(MaterialTheme.colorScheme.primary)
    SurfaceCard {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.layout.Box(Modifier.size(48.dp).clip(CircleShape).background(accent.copy(alpha = .18f)))
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(schedule.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(template?.name ?: "Ручной график", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${schedule.dayHours}ч день · ${schedule.nightHours}ч ночь", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 3.dp))
            }
            IconButton(onClick = onCopy) { Icon(Icons.Filled.ContentCopy, "Копировать") }
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, "Изменить") }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.DeleteOutline, "Удалить") }
        }
    }
}

@Composable
private fun TemplateCardNew(template: Template, lang: String, onUse: () -> Unit, onEdit: () -> Unit, onCopy: () -> Unit, onDelete: (() -> Unit)?) {
    SurfaceCard {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(template.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Text(template.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (template.isBuiltIn) Text("Встроенный", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Row(Modifier.fillMaxWidth().padding(top = 13.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                template.getPatternList().take(8).forEach { code ->
                    val type = ShiftType.fromCode(code)
                    if (type != null) androidx.compose.material3.Surface(shape = RoundedCornerShape(12.dp), color = type.color.copy(alpha = .16f)) { Text(type.emoji, modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp)) }
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onUse, shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f)) { Text("Использовать") }
                IconButton(onClick = onCopy) { Icon(Icons.Filled.ContentCopy, "Копировать") }
                IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, "Изменить") }
                if (onDelete != null) IconButton(onClick = onDelete) { Icon(Icons.Filled.DeleteOutline, "Удалить") }
            }
        }
    }
}
