package com.shiftschedule.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shiftschedule.app.ui.components.ScreenHeader
import com.shiftschedule.app.ui.viewmodel.ShiftViewModel
import com.shiftschedule.app.util.tr
import kotlinx.coroutines.launch

private val themePreviews = mapOf(
    "dark" to (Color(0xFF1A1A1A) to Color(0xFF5856D6)),
    "light" to (Color(0xFFFAF9F6) to Color(0xFF4F46E5)),
    "sepia" to (Color(0xFF33291D) to Color(0xFFD9A05B)),
    "midnight" to (Color(0xFF000000) to Color(0xFF00E5FF)),
    "ocean" to (Color(0xFF07404C) to Color(0xFF22B8CF)),
    "forest" to (Color(0xFF142019) to Color(0xFF7BC46A)),
    "berry" to (Color(0xFF221220) to Color(0xFFD985C7)),
    "sand" to (Color(0xFFFFF6E9) to Color(0xFFB26B1F)),
    "plum" to (Color(0xFF2A1439) to Color(0xFFB388FF)),
    "graphite" to (Color(0xFF1A1C20) to Color(0xFFC9CCD3))
)

@Composable
fun SettingsScreen(viewModel: ShiftViewModel) {
    val settings by viewModel.settings.collectAsState()
    var showTimeDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val exportedMsg = tr("exported")
    val exportErrMsg = tr("export_error")
    val importedMsg = tr("imported")
    val importBadMsg = tr("import_bad")
    val importErrMsg = tr("import_error")

    val exportLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val json = viewModel.exportData()
                    context.contentResolver.openOutputStream(it)?.use { os -> os.write(json.toByteArray(Charsets.UTF_8)) }
                    snackbarHostState.showSnackbar(exportedMsg)
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar(exportErrMsg)
                }
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val json = context.contentResolver.openInputStream(it)?.use { input -> input.readBytes().toString(Charsets.UTF_8) } ?: return@launch
                    val ok = viewModel.importData(json)
                    snackbarHostState.showSnackbar(if (ok) importedMsg else importBadMsg)
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar(importErrMsg)
                }
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp).verticalScroll(rememberScrollState())) {
            ScreenHeader(title = tr("tab_settings"), modifier = Modifier.padding(bottom = 12.dp))

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(tr("appearance_theme"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                        themePreviews.forEach { (id, pair) ->
                            ThemeDot(id, tr("theme_$id"), pair.first, pair.second, settings.theme) { viewModel.updateSettings(settings.copy(theme = it)) }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(tr("notifications"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tr("reminders"), style = MaterialTheme.typography.bodyLarge)
                            Text(tr("reminders_desc"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = settings.notifications, onCheckedChange = { viewModel.updateSettings(settings.copy(notifications = it)) })
                    }
                    if (settings.notifications) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(tr("time_dialog_title") + ": " + settings.reminderTime, style = MaterialTheme.typography.bodyMedium)
                        OutlinedButton(onClick = { showTimeDialog = true }) { Text(tr("change_time")) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(tr("display"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tr("emoji"), style = MaterialTheme.typography.bodyLarge)
                            Text(tr("emoji_desc"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = settings.showEmoji, onCheckedChange = { viewModel.updateSettings(settings.copy(showEmoji = it)) })
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(tr("week_start"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { viewModel.updateSettings(settings.copy(weekStart = "mon")) }) {
                        RadioButton(selected = settings.weekStart == "mon", onClick = { viewModel.updateSettings(settings.copy(weekStart = "mon")) })
                        Text(tr("mon"), modifier = Modifier.padding(start = 8.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { viewModel.updateSettings(settings.copy(weekStart = "sun")) }) {
                        RadioButton(selected = settings.weekStart == "sun", onClick = { viewModel.updateSettings(settings.copy(weekStart = "sun")) })
                        Text(tr("sun"), modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(tr("data"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    Text(tr("data_desc"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { exportLauncher.launch("shift-schedule-backup.json") }) { Text(tr("export")) }
                        OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) }) { Text(tr("import")) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(tr("settings_controls_title"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    ControlRow("👆", tr("ctrl_tap"))
                    ControlRow("👥", tr("ctrl_long"))
                    ControlRow("↔️", tr("ctrl_swipe"))
                    ControlRow("✊", tr("ctrl_drag"))
                    ControlRow("⧉", tr("ctrl_copy"))
                    ControlRow("🆚", tr("ctrl_compare"))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Shift Schedule", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(tr("app_info_desc"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    AboutBullet(tr("about_1"))
                    AboutBullet(tr("about_2"))
                    AboutBullet(tr("about_3"))
                    AboutBullet(tr("about_4"))
                    AboutBullet(tr("about_5"))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(tr("app_license"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Версия 1.0 · Сделано с ❤️",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )
        }
    }

    if (showTimeDialog) {
        val parts = settings.reminderTime.split(":")
        var hours by remember { mutableStateOf(parts.getOrNull(0) ?: "08") }
        var minutes by remember { mutableStateOf(parts.getOrNull(1) ?: "00") }
        AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            title = { Text(tr("time_dialog_title")) },
            text = {
                Column {
                    Text(tr("time_dialog_hint"), style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        OutlinedTextField(value = hours, onValueChange = { if (it.length <= 2 && it.all { ch -> ch.isDigit() }) hours = it }, label = { Text("0-23") }, singleLine = true, modifier = Modifier.width(80.dp))
                        Text(":", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 8.dp))
                        OutlinedTextField(value = minutes, onValueChange = { if (it.length <= 2 && it.all { ch -> ch.isDigit() }) minutes = it }, label = { Text("0-59") }, singleLine = true, modifier = Modifier.width(80.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val h = hours.toIntOrNull(); val m = minutes.toIntOrNull()
                        if (h != null && m != null && h in 0..23 && m in 0..59) {
                            viewModel.updateSettings(settings.copy(reminderTime = String.format("%02d:%02d", h, m)))
                            showTimeDialog = false
                        }
                    },
                    enabled = (hours.toIntOrNull() ?: -1) in 0..23 && (minutes.toIntOrNull() ?: -1) in 0..59
                ) { Text(tr("save")) }
            },
            dismissButton = { TextButton(onClick = { showTimeDialog = false }) { Text(tr("cancel")) } }
        )
    }
}

@Composable
private fun ThemeDot(id: String, label: String, bg: Color, pr: Color, current: String, onSelect: (String) -> Unit) {
    val selected = current == id
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 14.dp).clickable { onSelect(id) }) {
        Box(
            modifier = Modifier.size(46.dp).clip(CircleShape).background(bg).then(if (selected) Modifier.border(3.dp, pr, CircleShape) else Modifier.border(1.dp, Color(0x33888888), CircleShape)),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(pr))
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun ControlRow(icon: String, text: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 18.sp, modifier = Modifier.size(28.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun AboutBullet(text: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text("•", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
    }
}
