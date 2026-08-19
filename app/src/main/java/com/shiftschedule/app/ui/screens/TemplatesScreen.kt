package com.shiftschedule.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.data.model.ShiftType
import com.shiftschedule.app.data.model.Template
import com.shiftschedule.app.ui.components.EditScheduleModal
import com.shiftschedule.app.ui.components.ReorderState
import com.shiftschedule.app.ui.components.ScreenHeader
import com.shiftschedule.app.ui.components.SwipeToDelete
import com.shiftschedule.app.ui.components.TemplateEditorModal
import com.shiftschedule.app.ui.components.dragContainer
import com.shiftschedule.app.ui.components.rememberReorderState
import com.shiftschedule.app.ui.viewmodel.ShiftViewModel
import com.shiftschedule.app.util.tr
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun TemplatesScreen(viewModel: ShiftViewModel) {
    val templates by viewModel.allTemplates.collectAsState()
    val schedules by viewModel.allSchedules.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val itemHeight = with(density) { 96.dp.toPx() }

    var showCreateSchedule by remember { mutableStateOf(false) }
    var scheduleToEdit by remember { mutableStateOf<Schedule?>(null) }
    var scheduleToDelete by remember { mutableStateOf<Schedule?>(null) }
    var showCreateTemplate by remember { mutableStateOf(false) }
    var templateToEdit by remember { mutableStateOf<Template?>(null) }
    var templateToDelete by remember { mutableStateOf<Template?>(null) }

    val copyCreatedMsg = tr("copy_created")
    val undoMsg = tr("undo")
    val deletedTemplate = tr("deleted_schedule")

    val reorderSchedules = remember { ReorderState() }
    reorderSchedules.onMove = { from, to -> viewModel.reorderSchedules(from, to) }

    val reorderTemplates = remember { ReorderState() }
    reorderTemplates.onMove = { from, to -> viewModel.reorderTemplates(from, to) }

    val searchActive = query.isNotBlank()
    val filteredSchedules = if (searchActive) {
        schedules.filter { it.name.contains(query, ignoreCase = true) }
    } else {
        schedules
    }
    val filteredTemplates = if (searchActive) {
        templates.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
        }
    } else {
        templates
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            ScreenHeader(
                title = tr("templates_title"),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(tr("search_placeholder")) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = tr("search")) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Filled.Close, contentDescription = tr("clear"))
                        }
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (!viewModel.isTipSeen("templates")) {
                com.shiftschedule.app.ui.components.TipCard(
                    text = tr("tip_templates"),
                    onClose = { viewModel.markTipSeen("templates") },
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!searchActive) {
                    item { SectionTitle(tr("my_schedules")) }
                }

                if (filteredSchedules.isEmpty() && !searchActive) {
                    item {
                        EmptyCard(tr("no_schedules_yet"), tr("create_first")) { showCreateSchedule = true }
                    }
                } else {
                    itemsIndexed(filteredSchedules, key = { _, s -> "s${s.id}" }) { index, schedule ->
                        val templateName = templates.find { it.id == schedule.templateId }?.name
                        SwipeToDelete(onDismiss = { scheduleToDelete = schedule }) {
                            ScheduleCard(
                                schedule = schedule,
                                templateName = templateName,
                                modifier = Modifier.dragContainer(
                                    state = reorderSchedules,
                                    index = index,
                                    itemHeight = itemHeight,
                                    itemCount = filteredSchedules.size,
                                    enabled = !searchActive
                                ),
                                onEdit = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    scheduleToEdit = schedule
                                },
                                onCopy = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.duplicateSchedule(schedule)
                                    scope.launch { snackbarHostState.showSnackbar(copyCreatedMsg) }
                                },
                                onDelete = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    scheduleToDelete = schedule
                                }
                            )
                        }
                    }
                }

                if (!searchActive) {
                    item { SectionTitle(tr("built_in"), topPadding = true) }
                }
                itemsIndexed(
                    filteredTemplates.filter { it.isBuiltIn },
                    key = { _, t -> "b${t.id}" }
                ) { _, template ->
                    TemplateCard(
                        template = template,
                        onEdit = { templateToEdit = template },
                        onDelete = null
                    )
                }

                if (!searchActive) {
                    item { SectionTitle(tr("user_templates"), topPadding = true) }
                }
                val userTemplates = filteredTemplates.filter { !it.isBuiltIn }
                if (userTemplates.isEmpty() && !searchActive) {
                    item {
                        EmptyCard(tr("no_templates_yet"), tr("create_template")) { showCreateTemplate = true }
                    }
                } else {
                    itemsIndexed(userTemplates, key = { _, t -> "u${t.id}" }) { index, template ->
                        SwipeToDelete(onDismiss = { templateToDelete = template }) {
                            TemplateCard(
                                template = template,
                                modifier = Modifier.dragContainer(
                                    state = reorderTemplates,
                                    index = index,
                                    itemHeight = itemHeight,
                                    itemCount = userTemplates.size,
                                    enabled = !searchActive
                                ),
                                onEdit = { templateToEdit = template },
                                onDelete = { templateToDelete = template }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateSchedule) {
        EditScheduleModal(
            initial = null,
            templates = templates,
            onDismiss = { showCreateSchedule = false },
            onSave = { s ->
                viewModel.addSchedule(s)
                showCreateSchedule = false
            }
        )
    }

    scheduleToEdit?.let { s ->
        EditScheduleModal(
            initial = s,
            templates = templates,
            onDismiss = { scheduleToEdit = null },
            onSave = { updated ->
                viewModel.updateSchedule(updated)
                scheduleToEdit = null
            }
        )
    }

    scheduleToDelete?.let { s ->
        AlertDialog(
            onDismissRequest = { scheduleToDelete = null },
            title = { Text(tr("delete_schedule_q")) },
            text = { Text(tr("delete_schedule_text", s.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val toDelete = scheduleToDelete
                        scheduleToDelete = null
                        if (toDelete != null) {
                            scope.launch {
                                viewModel.deleteSchedule(toDelete)
                                val result = snackbarHostState.showSnackbar(
                                    String.format(deletedTemplate, toDelete.name),
                                    undoMsg
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.addSchedule(toDelete)
                                }
                            }
                        }
                    }
                ) { Text(tr("delete")) }
            },
            dismissButton = {
                TextButton(onClick = { scheduleToDelete = null }) { Text(tr("cancel")) }
            }
        )
    }

    if (showCreateTemplate) {
        TemplateEditorModal(
            initial = null,
            onDismiss = { showCreateTemplate = false },
            onSave = { t ->
                viewModel.addTemplate(t)
                showCreateTemplate = false
            }
        )
    }

    templateToEdit?.let { t ->
        TemplateEditorModal(
            initial = t,
            onDismiss = { templateToEdit = null },
            onSave = { updated ->
                viewModel.updateTemplate(updated)
                templateToEdit = null
            }
        )
    }

    templateToDelete?.let { t ->
        val affected = schedules.count { it.templateId == t.id }
        AlertDialog(
            onDismissRequest = { templateToDelete = null },
            title = { Text(tr("delete_template_q")) },
            text = {
                if (affected > 0) {
                    Text(tr("template_used", affected))
                } else {
                    Text(tr("template_deleted_text"))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTemplate(t)
                        templateToDelete = null
                    }
                ) { Text(tr("delete")) }
            },
            dismissButton = {
                TextButton(onClick = { templateToDelete = null }) { Text(tr("cancel")) }
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String, topPadding: Boolean = false) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = if (topPadding) 16.dp else 0.dp, bottom = 4.dp)
    )
}

@Composable
private fun EmptyCard(title: String, subtitle: String, onAction: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            OutlinedButton(onClick = onAction, modifier = Modifier.padding(top = 12.dp)) {
                Text("+ " + tr("create"))
            }
        }
    }
}

@Composable
private fun ScheduleCard(
    schedule: Schedule,
    templateName: String?,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(schedule.color)))
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    schedule.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    (templateName ?: tr("manual")) + " · " + tr("from") + " " + schedule.startDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onCopy) {
                Icon(Icons.Filled.ContentCopy, contentDescription = tr("copy"))
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = tr("edit"))
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = tr("delete"),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: Template,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        template.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        template.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = tr("edit"))
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = tr("delete"),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                template.getPatternList().forEach { code ->
                    val shiftType = ShiftType.values().find { it.code == code }
                    shiftType?.let { type ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(type.color.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = type.emoji, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}