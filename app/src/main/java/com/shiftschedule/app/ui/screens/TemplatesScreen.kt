package com.shiftschedule.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.shiftschedule.app.ui.components.SwipeToDelete
import com.shiftschedule.app.ui.components.TemplateEditorModal
import com.shiftschedule.app.ui.components.dragContainer
import com.shiftschedule.app.ui.components.rememberReorderState
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
    var pendingTemplateId by remember { mutableStateOf<Int?>(null) }
    var editSchedule by remember { mutableStateOf<Schedule?>(null) }
    var deleteSchedule by remember { mutableStateOf<Schedule?>(null) }
    var deleteTemplate by remember { mutableStateOf<Template?>(null) }

    val filteredSchedules = schedules.filter { it.name.contains(query, true) }
    val filteredTemplates = templates.filter { it.name.contains(query, true) || it.description.contains(query, true) }
    val reorderState = rememberReorderState()
    val scheduleReorderState = rememberReorderState()
    val userTemplates = filteredTemplates.filter { !it.isBuiltIn }
    reorderState.onMove = { from, to -> if (query.isBlank()) viewModel.reorderTemplates(from, to) }
    scheduleReorderState.onMove = { from, to -> if (query.isBlank()) viewModel.reorderSchedules(from, to) }

    Scaffold { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).widthIn(max = 720.dp).padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Spacer(Modifier.height(6.dp)) }
            item {
                AppHeader(tr("templates_title"), tr("templates_subtitle"))
            }
            item {
                OutlinedTextField(value = query, onValueChange = viewModel::setSearchQuery, modifier = Modifier.fillMaxWidth(), placeholder = { Text(tr("search_placeholder")) }, leadingIcon = { Icon(Icons.Filled.Search, null) }, singleLine = true, shape = RoundedCornerShape(18.dp))
            }
            item { SectionLabel(tr("my_schedules"), action = tr("add_schedule"), onAction = { createSchedule = true }) }
            if (filteredSchedules.isEmpty()) {
                item { EmptyState(tr("no_schedules_yet"), tr("create_first"), tr("add_schedule"), { createSchedule = true }) }
            } else {
                items(filteredSchedules, key = { it.id }) { schedule ->
                    ScheduleCardNew(schedule, templates.firstOrNull { it.id == schedule.templateId }, lang, dragModifier = if (query.isBlank()) Modifier.dragContainer(scheduleReorderState, filteredSchedules.indexOfFirst { it.id == schedule.id }, 120f, filteredSchedules.size) else Modifier, onEdit = { editSchedule = schedule }, onCopy = { viewModel.duplicateSchedule(schedule) }, onDelete = { deleteSchedule = schedule })
                }
            }
            item { Spacer(Modifier.height(6.dp)); SectionLabel(tr("built_in")) }
            items(filteredTemplates.filter { it.isBuiltIn }, key = { "b${it.id}" }) { template ->
                TemplateCardNew(template, lang, settings.showEmoji, onUse = { pendingTemplateId = template.id; createSchedule = true }, onEdit = { editTemplate = template }, onCopy = { viewModel.duplicateTemplate(template) }, onDelete = null)
            }
            item { SectionLabel(tr("user_templates"), action = tr("create"), onAction = { createTemplate = true }) }
            if (userTemplates.isEmpty()) item { SurfaceCard { Column(Modifier.fillMaxWidth().padding(18.dp)) { Text(tr("create_template"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text(tr("template_example"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp)); Button(onClick = { createTemplate = true }, modifier = Modifier.padding(top = 12.dp), shape = RoundedCornerShape(14.dp)) { Text(tr("new_template")) } } } }
            else items(userTemplates, key = { "u${it.id}" }) { template ->
                SwipeToDelete(onDismiss = { deleteTemplate = template }) {
                    TemplateCardNew(
                        template, lang, settings.showEmoji,
                        dragModifier = if (query.isBlank()) Modifier.dragContainer(reorderState, userTemplates.indexOfFirst { it.id == template.id }, 170f, userTemplates.size) else Modifier,
                        onUse = { pendingTemplateId = template.id; createSchedule = true },
                        onEdit = { editTemplate = template },
                        onCopy = { viewModel.duplicateTemplate(template) },
                        onDelete = { deleteTemplate = template }
                    )
                }
            }
            item { Spacer(Modifier.height(90.dp)) }
        }
    }

    if (createTemplate) TemplateEditorModal(null, { createTemplate = false }) { template -> viewModel.addTemplate(template) { id -> pendingTemplateId = id; createTemplate = false; createSchedule = true } }
    editTemplate?.let { TemplateEditorModal(it, { editTemplate = null }) { viewModel.updateTemplate(it); editTemplate = null } }
    if (createSchedule) EditScheduleModal(
        initial = null,
        templates = templates,
        onDismiss = { createSchedule = false; pendingTemplateId = null },
        onSave = { viewModel.addSchedule(it) { viewModel.selectSchedule(it) }; createSchedule = false; pendingTemplateId = null },
        onCreateTemplate = { createSchedule = false; pendingTemplateId = null; createTemplate = true },
        initialTemplateId = pendingTemplateId,
        showEmoji = settings.showEmoji
    )
    editSchedule?.let { EditScheduleModal(it, templates, onDismiss = { editSchedule = null }, onSave = { viewModel.updateSchedule(it); editSchedule = null }) }
    deleteSchedule?.let { item -> AlertDialog(onDismissRequest = { deleteSchedule = null }, title = { Text(tr("delete_schedule_q")) }, text = { Text(tr("delete_schedule_text", item.name)) }, confirmButton = { TextButton(onClick = { viewModel.deleteSchedule(item); deleteSchedule = null }) { Text(tr("delete")) } }, dismissButton = { TextButton(onClick = { deleteSchedule = null }) { Text(tr("cancel")) } }) }
    deleteTemplate?.let { item -> AlertDialog(onDismissRequest = { deleteTemplate = null }, title = { Text(tr("delete_template_q")) }, text = { Text(tr("template_deleted_text")) }, confirmButton = { TextButton(onClick = { viewModel.deleteTemplate(item); deleteTemplate = null }) { Text(tr("delete")) } }, dismissButton = { TextButton(onClick = { deleteTemplate = null }) { Text(tr("cancel")) } }) }
}

@Composable
private fun ScheduleCardNew(schedule: Schedule, template: Template?, lang: String, dragModifier: Modifier = Modifier, onEdit: () -> Unit, onCopy: () -> Unit, onDelete: () -> Unit) {
    val accent = runCatching { Color(android.graphics.Color.parseColor(schedule.color)) }.getOrDefault(MaterialTheme.colorScheme.primary)
    SurfaceCard(dragModifier) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.layout.Box(Modifier.size(48.dp).clip(CircleShape).background(accent.copy(alpha = .18f)))
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(schedule.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(if (template != null) "${tr("rhythm")}: ${template.name}" else tr("manual"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            IconButton(onClick = onCopy) { Icon(Icons.Filled.ContentCopy, tr("copy")) }
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, tr("edit")) }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.DeleteOutline, tr("delete")) }
        }
    }
}

@Composable
private fun TemplateCardNew(template: Template, lang: String, showEmoji: Boolean, dragModifier: Modifier = Modifier, onUse: () -> Unit, onEdit: () -> Unit, onCopy: () -> Unit, onDelete: (() -> Unit)?) {
    SurfaceCard(dragModifier) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(template.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Text(template.displayDescription(lang), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (template.isBuiltIn) Text(tr("built_in_label"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 13.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                template.getPatternList().take(8).forEach { code ->
                    val type = ShiftType.fromCode(code)
                    if (type != null) androidx.compose.material3.Surface(shape = RoundedCornerShape(12.dp), color = type.color.copy(alpha = .16f)) {
                        if (showEmoji) Text(type.emoji, modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp))
                        else Box(Modifier.padding(horizontal = 12.dp, vertical = 11.dp).size(9.dp).clip(CircleShape).background(type.color))
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onUse, shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f)) { Text(tr("use")) }
                IconButton(onClick = onCopy) { Icon(Icons.Filled.ContentCopy, tr("copy")) }
                IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, tr("edit")) }
                if (onDelete != null) IconButton(onClick = onDelete) { Icon(Icons.Filled.DeleteOutline, tr("delete")) }
            }
        }
    }
}
