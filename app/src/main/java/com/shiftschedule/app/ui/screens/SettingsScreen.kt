package com.shiftschedule.app.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import com.shiftschedule.app.ui.components.AppHeader
import com.shiftschedule.app.ui.components.SectionLabel
import com.shiftschedule.app.ui.components.SurfaceCard
import com.shiftschedule.app.ui.viewmodel.ShiftViewModel
import com.shiftschedule.app.util.tr
import kotlinx.coroutines.launch

private val themePreviews = listOf(
    "dark" to (Color(0xFF111118) to Color(0xFF6C63FF)),
    "light" to (Color(0xFFF7F7FB) to Color(0xFF5A55D6)),
    "sepia" to (Color(0xFF33291D) to Color(0xFFD9A05B)),
    "dynamic" to (Color(0xFFEADDFF) to Color(0xFF6750A4))
)

@Composable
fun SettingsScreen(viewModel: ShiftViewModel) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var timeDialog by remember { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.updateSettings(settings.copy(notifications = true))
        } else {
            scope.launch { snackbar.showSnackbar("Разрешение на уведомления не выдано") }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri -> uri?.let { scope.launch { runCatching { val json = viewModel.exportData(); context.contentResolver.openOutputStream(it)?.use { out -> out.write(json.toByteArray()) }; snackbar.showSnackbar("Резервная копия сохранена") }.onFailure { snackbar.showSnackbar("Не удалось экспортировать данные") } } } }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let { scope.launch { runCatching { val json = context.contentResolver.openInputStream(it)?.use { input -> input.readBytes().toString(Charsets.UTF_8) } ?: error("empty"); if (viewModel.importData(json)) snackbar.showSnackbar("Данные восстановлены") else snackbar.showSnackbar("Файл не прошёл проверку") }.onFailure { snackbar.showSnackbar("Не удалось импортировать данные") } } } }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
            Spacer(Modifier.size(8.dp))
            AppHeader("Настройки", "Настройте приложение один раз — дальше оно работает само")
            Spacer(Modifier.size(16.dp))

            SettingGroup("Внешний вид", Icons.Filled.Palette) {
                SectionLabel("Тема")
                Row(Modifier.horizontalScroll(rememberScrollState()).padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    themePreviews.forEach { (id, colors) -> ThemeChip(id, colors.first, colors.second, settings.theme) { viewModel.updateSettings(settings.copy(theme = it)) } }
                }
                Spacer(Modifier.size(14.dp))
                ChoiceRow("Язык", when (settings.lang) { "ru" -> "Русский"; "en" -> "English"; else -> "Системный" }, Icons.Filled.Language) {
                    viewModel.updateSettings(settings.copy(lang = when (settings.lang) { "system" -> "ru"; "ru" -> "en"; else -> "system" }))
                }
                ChoiceRow("Начало недели", if (settings.weekStart == "sun") "Воскресенье" else "Понедельник", Icons.Filled.CalendarToday) { viewModel.updateSettings(settings.copy(weekStart = if (settings.weekStart == "mon") "sun" else "mon")) }
                SwitchRow("Показывать emoji", "Солнце, луна и другие обозначения в календаре", settings.showEmoji, Icons.Filled.Brightness4) { viewModel.updateSettings(settings.copy(showEmoji = it)) }
            }

            SettingGroup("Уведомления", Icons.Filled.NotificationsNone) {
                val systemNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
                SwitchRow("Напоминания", "Показывать ближайшую смену каждый день", settings.notifications, Icons.Filled.NotificationsNone) { enabled ->
                    if (!enabled) {
                        viewModel.updateSettings(settings.copy(notifications = false))
                    } else if (Build.VERSION.SDK_INT >= 33 && !NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.updateSettings(settings.copy(notifications = true))
                    }
                }
                if (settings.notifications) {
                    ChoiceRow("Время", settings.reminderTime, Icons.Filled.Schedule) { timeDialog = true }
                    if (!systemNotificationsEnabled) {
                        Text("Android заблокировал уведомления для приложения.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 6.dp))
                        OutlinedButton(
                            onClick = { context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply { putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName) }) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = RoundedCornerShape(15.dp)
                        ) { Text("Открыть настройки уведомлений") }
                    }
                }
            }

                    SettingGroup("Смены и цвета", Icons.Filled.Palette) {
                Text("Цвета сохраняют смысл даже без emoji.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.size(8.dp))
                com.shiftschedule.app.ui.components.ShiftLegend(showEmoji = false)
                Spacer(Modifier.size(8.dp))
                SwitchRow("Праздники РФ", "Подсвечивать государственные праздники в календаре", settings.rfHolidays, Icons.Filled.CalendarToday) { viewModel.updateSettings(settings.copy(rfHolidays = it)) }
            }

            SettingGroup("Данные", Icons.Filled.Backup) {
                Text("Резервная копия содержит графики, шаблоны и настройки расписания. Храните файл отдельно от телефона.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { exportLauncher.launch("shiftweave-backup.json") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(15.dp)) { Text("Экспорт") }
                    Button(onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(15.dp)) { Text("Импорт") }
                }
            }

            SettingGroup("Управление", Icons.Filled.SettingsSuggest) {
                ControlLine("Нажатие", "изменить смену в конкретный день")
                ControlLine("Удержание", "сравнить все графики на дату")
                ControlLine("Свайп", "перейти к соседнему месяцу")
                ControlLine("Перетаскивание", "изменить порядок графиков и шаблонов")
            }

            SettingGroup("О ShiftWeave", Icons.Filled.DataObject) {
                Text("Планировщик рабочих смен для тех, у кого жизнь не укладывается в стандартный понедельник–пятницу.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.size(10.dp))
                Text("Локальные данные · работает без интернета · JSON backup · несколько графиков · сравнение", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.size(10.dp))
                Text("Версия 1.0", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.size(96.dp))
        }
    }

    if (timeDialog) {
        var value by remember(settings.reminderTime) { mutableStateOf(settings.reminderTime) }
        AlertDialog(onDismissRequest = { timeDialog = false }, title = { Text("Время напоминания") }, text = { OutlinedTextField(value, { value = it.filter { c -> c.isDigit() || c == ':' }.take(5) }, label = { Text("HH:MM") }, singleLine = true) }, confirmButton = { TextButton(onClick = { val parts = value.split(":"); val h = parts.getOrNull(0)?.toIntOrNull(); val m = parts.getOrNull(1)?.toIntOrNull(); if (h != null && m != null && h in 0..23 && m in 0..59) { viewModel.updateSettings(settings.copy(reminderTime = "%02d:%02d".format(h, m))); timeDialog = false } }) { Text("Сохранить") } }, dismissButton = { TextButton(onClick = { timeDialog = false }) { Text("Отмена") } })
    }
}

@Composable private fun SettingGroup(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable () -> Unit) {
    SurfaceCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = .13f)) { Icon(icon, null, modifier = Modifier.padding(9.dp), tint = MaterialTheme.colorScheme.primary) }
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 10.dp))
            }
            Spacer(Modifier.size(10.dp)); content()
        }
    }
}

@Composable private fun ChoiceRow(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)); Column(Modifier.weight(1f).padding(start = 10.dp)) { Text(title, fontWeight = FontWeight.SemiBold); Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }; Text("›", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable private fun SwitchRow(title: String, subtitle: String, checked: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)); Column(Modifier.weight(1f).padding(horizontal = 10.dp)) { Text(title, fontWeight = FontWeight.SemiBold); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Switch(checked, onChange) }
}

@Composable private fun ThemeChip(id: String, bg: Color, primary: Color, current: String, onSelect: (String) -> Unit) {
    val selected = current == id
    Surface(onClick = { onSelect(id) }, shape = RoundedCornerShape(17.dp), color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(Modifier.padding(horizontal = 9.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { androidx.compose.foundation.layout.Box(Modifier.size(26.dp).clip(CircleShape).background(bg)); androidx.compose.foundation.layout.Box(Modifier.size(12.dp).clip(CircleShape).background(primary).padding(start = 7.dp)); Text(id.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 6.dp), fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) }
    }
}

@Composable private fun ControlLine(title: String, desc: String) { Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) { Text(title, Modifier.width(125.dp), fontWeight = FontWeight.SemiBold); Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
