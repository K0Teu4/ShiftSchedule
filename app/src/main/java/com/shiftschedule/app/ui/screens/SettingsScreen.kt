package com.shiftschedule.app.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shiftschedule.app.BuildConfig
import com.shiftschedule.app.ui.components.AppHeader
import com.shiftschedule.app.ui.components.SectionLabel
import com.shiftschedule.app.ui.components.ShiftLegend
import com.shiftschedule.app.ui.components.SurfaceCard
import com.shiftschedule.app.ui.viewmodel.ShiftViewModel
import com.shiftschedule.app.util.tr
import kotlinx.coroutines.launch

private val themePreviews = listOf(
    "system" to (Color(0xFFE8E9EC) to Color(0xFF6B63D8)),
    "dark" to (Color(0xFF11161A) to Color(0xFF9B8CFF)),
    "light" to (Color(0xFFF7F8FA) to Color(0xFF5548D8))
)

@Composable
fun SettingsScreen(viewModel: ShiftViewModel) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var timeDialog by remember { mutableStateOf(false) }
    var languageDialog by remember { mutableStateOf(false) }
    var weekDialog by remember { mutableStateOf(false) }

    val notificationPermissionDeniedText = tr("notification_permission_denied")
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.updateSettings(settings.copy(notifications = true))
        else scope.launch { snackbar.showSnackbar(notificationPermissionDeniedText) }
    }

    val exportedText = tr("exported")
    val exportErrorText = tr("export_error")
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let {
            scope.launch {
                runCatching {
                    val json = viewModel.exportData()
                    context.contentResolver.openOutputStream(it)?.use { out -> out.write(json.toByteArray(Charsets.UTF_8)) }
                    snackbar.showSnackbar(exportedText)
                }.onFailure { snackbar.showSnackbar(exportErrorText) }
            }
        }
    }

    val importedText = tr("imported")
    val importBadText = tr("import_bad")
    val importErrorText = tr("import_error")
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            scope.launch {
                runCatching {
                    val json = context.contentResolver.openInputStream(it)?.use { input -> input.readBytes().toString(Charsets.UTF_8) } ?: error("empty")
                    if (viewModel.importData(json)) snackbar.showSnackbar(importedText) else snackbar.showSnackbar(importBadText)
                }.onFailure { snackbar.showSnackbar(importErrorText) }
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
            Spacer(Modifier.size(8.dp))
            AppHeader(tr("tab_settings"), tr("settings_subtitle"))
            Spacer(Modifier.size(12.dp))

            SettingGroup(tr("appearance"), Icons.Filled.Palette) {
                SectionLabel(tr("appearance_theme"))
                Row(Modifier.horizontalScroll(rememberScrollState()).padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    themePreviews.forEach { (id, colors) -> ThemeChip(id, colors.first, colors.second, settings.theme) { viewModel.updateSettings(settings.copy(theme = it)) } }
                }
                Spacer(Modifier.size(10.dp))
                ChoiceRow(tr("lang_title"), when (settings.lang) { "ru" -> tr("lang_ru"); "en" -> tr("lang_en"); else -> tr("lang_system") }, Icons.Filled.Language) { languageDialog = true }
                ChoiceRow(tr("week_start"), if (settings.weekStart == "sun") tr("sun") else tr("mon"), Icons.Filled.CalendarToday) { weekDialog = true }
            }

            SettingGroup(tr("notifications"), Icons.Filled.NotificationsNone) {
                val systemNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
                SwitchRow(tr("reminders"), tr("reminders_desc"), settings.notifications, Icons.Filled.NotificationsNone) { enabled ->
                    if (!enabled) {
                        viewModel.updateSettings(settings.copy(notifications = false))
                    } else if (Build.VERSION.SDK_INT >= 33 && !systemNotificationsEnabled) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.updateSettings(settings.copy(notifications = true))
                    }
                }
                if (settings.notifications) {
                    ChoiceRow(tr("change_time"), settings.reminderTime, Icons.Filled.Schedule) { timeDialog = true }
                    if (!systemNotificationsEnabled) {
                        Text(tr("notifications_blocked"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 6.dp))
                        OutlinedButton(onClick = { context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply { putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName) }) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(15.dp)) {
                            Text(tr("open_notification_settings"), maxLines = 1)
                        }
                    }
                }
            }

            SettingGroup(tr("shifts_and_colors"), Icons.Filled.Palette) {
                Text(tr("colors_hint"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.size(8.dp))
                SwitchRow(tr("emoji"), tr("emoji_desc"), settings.showEmoji, Icons.Filled.Brightness4) { viewModel.updateSettings(settings.copy(showEmoji = it)) }
                ShiftLegend(showEmoji = settings.showEmoji)
                Spacer(Modifier.size(8.dp))
                SwitchRow(tr("rf_holidays"), tr("rf_holidays_desc"), settings.rfHolidays, Icons.Filled.CalendarToday) { viewModel.updateSettings(settings.copy(rfHolidays = it)) }
            }

            SettingGroup(tr("data"), Icons.Filled.Backup) {
                Text(tr("data_desc"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { exportLauncher.launch("shiftweave-backup.json") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(15.dp)) { Text(tr("export"), maxLines = 1) }
                    Button(onClick = { importLauncher.launch(arrayOf("application/json", "**")) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(15.dp)) { Text(tr("import"), maxLines = 1) }
                }
            }

            SettingGroup(tr("settings_controls_title"), Icons.Filled.SettingsSuggest) {
                ControlLine(tr("ctrl_tap"))
                ControlLine(tr("ctrl_long"))
                ControlLine(tr("ctrl_swipe"))
                ControlLine(tr("ctrl_drag"))
                ControlLine(tr("ctrl_compare"))
            }

            SettingGroup(tr("app_info_title"), Icons.Filled.DataObject) {
                Text(tr("app_info_desc"), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.size(10.dp))
                Text(tr("app_license"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.size(8.dp))
                Text(tr("version_footer", BuildConfig.VERSION_NAME), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.size(96.dp))
        }
    }

    if (languageDialog) {
        ChoiceDialog(
            title = tr("lang_title"),
            values = listOf(tr("lang_system") to "system", tr("lang_ru") to "ru", tr("lang_en") to "en"),
            selected = settings.lang,
            onSelect = { value -> viewModel.updateSettings(settings.copy(lang = value)); languageDialog = false },
            onDismiss = { languageDialog = false }
        )
    }

    if (weekDialog) {
        ChoiceDialog(
            title = tr("week_start"),
            values = listOf(tr("mon") to "mon", tr("sun") to "sun"),
            selected = settings.weekStart,
            onSelect = { value -> viewModel.updateSettings(settings.copy(weekStart = value)); weekDialog = false },
            onDismiss = { weekDialog = false }
        )
    }

    if (timeDialog) {
        var value by remember(settings.reminderTime) { mutableStateOf(settings.reminderTime) }
        val valid = value.matches(Regex("^(?:[01]\\d|2[0-3]):[0-5]\\d$"))
        AlertDialog(
            onDismissRequest = { timeDialog = false },
            title = { Text(tr("time_dialog_title")) },
            text = {
                OutlinedTextField(
                    value = value,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }.take(4)
                        value = when {
                            digits.isEmpty() -> ""
                            digits.length <= 2 -> digits.take(2)
                            else -> {
                                val hours = digits.substring(0, 2).toIntOrNull()?.coerceAtMost(23) ?: 0
                                val minutes = digits.substring(2).toIntOrNull()?.coerceAtMost(59) ?: 0
                                "%02d:%02d".format(hours, minutes)
                            }
                        }
                    },
                    label = { Text(tr("time_label")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { if (!valid) Text(tr("time_invalid"), color = MaterialTheme.colorScheme.error) },
                    isError = value.isNotEmpty() && !valid,
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(enabled = valid, onClick = {
                    viewModel.updateSettings(settings.copy(reminderTime = value))
                    timeDialog = false
                }) { Text(tr("save")) }
            },
            dismissButton = { TextButton(onClick = { timeDialog = false }) { Text(tr("cancel")) } }
        )
    }
}

@Composable
private fun ChoiceDialog(title: String, values: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                values.forEach { (label, value) ->
                    Surface(onClick = { onSelect(value) }, shape = RoundedCornerShape(14.dp), color = if (value == selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
                        Text(label, Modifier.padding(14.dp), fontWeight = if (value == selected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(tr("cancel")) } }
    )
}

@Composable
private fun SettingGroup(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable () -> Unit) {
    SurfaceCard(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = .13f)) { Icon(icon, null, modifier = Modifier.padding(9.dp), tint = MaterialTheme.colorScheme.primary) }
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 10.dp), maxLines = 1)
            }
            Spacer(Modifier.size(10.dp))
            content()
        }
    }
}

@Composable
private fun ChoiceRow(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Column(Modifier.weight(1f).padding(start = 10.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, maxLines = 1)
            }
            Text("›", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
        }
        Switch(checked, onChange)
    }
}

@Composable
private fun ThemeChip(id: String, bg: Color, primary: Color, current: String, onSelect: (String) -> Unit) {
    val selected = current == id
    val label = tr("theme_${id}")
    Surface(onClick = { onSelect(id) }, shape = RoundedCornerShape(17.dp), color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(Modifier.padding(horizontal = 9.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(26.dp).clip(CircleShape).background(bg))
            Box(Modifier.size(12.dp).clip(CircleShape).background(primary).padding(start = 7.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 6.dp), fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, maxLines = 1)
        }
    }
}

@Composable
private fun ControlLine(text: String) {
    Text(text, Modifier.fillMaxWidth().padding(vertical = 5.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
