package com.shiftschedule.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shiftschedule.app.data.local.SettingsDataStore
import com.shiftschedule.app.data.local.ShiftDatabase
import com.shiftschedule.app.data.model.AppSettings
import com.shiftschedule.app.data.model.BackupData
import com.shiftschedule.app.data.model.BackupValidator
import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.data.model.ShiftType
import com.shiftschedule.app.data.model.Template
import com.shiftschedule.app.data.repository.ShiftRepository
import com.shiftschedule.app.util.DateUtils
import com.shiftschedule.app.util.ListUtils
import com.shiftschedule.app.util.PatternUtils
import com.shiftschedule.app.util.Strings
import com.shiftschedule.app.domain.ShiftResolver
import com.shiftschedule.app.util.StatsUtils
import com.shiftschedule.app.widget.ShiftWidgetProvider
import com.shiftschedule.app.notifications.NotificationHelper
import com.shiftschedule.app.notifications.NotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class ShiftViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ShiftRepository(ShiftDatabase.getDatabase(application).shiftDao())
    private val settingsDataStore = SettingsDataStore(application)
    private val gson = com.google.gson.Gson()

    val allSchedules = repository.allSchedules.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allTemplates = repository.allTemplates.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val settings = settingsDataStore.settingsFlow.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _selectedScheduleId = MutableStateFlow<Int?>(null)
    val selectedScheduleId: StateFlow<Int?> = _selectedScheduleId.asStateFlow()

    private val _selectedCompareIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedCompareIds: StateFlow<Set<Int>> = _selectedCompareIds.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        viewModelScope.launch {
            val templates = repository.allTemplates.first()
            val existingBuiltInIds = templates.filter { it.isBuiltIn }.map { it.id }.toSet()
            Template.getBuiltInTemplates()
                .filter { it.id !in existingBuiltInIds }
                .forEach { repository.insertTemplate(it) }
            settingsDataStore.settingsFlow.first()
            _isLoaded.value = true
        }
    }

    private fun currentLang(): String = when (settings.value.lang) {
        "ru" -> "ru"
        "en" -> "en"
        else -> Strings.getSystemLanguage()
    }

    private fun copySuffix(): String = if (currentLang() == "en") " (copy)" else " (копия)"

    private fun refreshWidget() {
        try {
            val context = getApplication<Application>()
            val manager = android.appwidget.AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(android.content.ComponentName(context, ShiftWidgetProvider::class.java))
            if (ids.isNotEmpty()) {
                val intent = android.content.Intent(context, ShiftWidgetProvider::class.java).apply {
                    action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setSearchQuery(q: String) { _searchQuery.value = q }
    fun selectSchedule(id: Int?) { _selectedScheduleId.value = id }

    fun toggleCompareSchedule(id: Int) {
        val current = _selectedCompareIds.value.toMutableSet()
        if (current.contains(id)) { if (current.size > 1) current.remove(id) } else { current.add(id) }
        _selectedCompareIds.value = current
    }

    fun nextMonth() { _currentMonth.value = _currentMonth.value.plusMonths(1) }
    fun previousMonth() { _currentMonth.value = _currentMonth.value.minusMonths(1) }
    fun goToday() { _currentMonth.value = YearMonth.now() }
    fun nextYear() { _currentMonth.value = _currentMonth.value.plusYears(1) }
    fun previousYear() { _currentMonth.value = _currentMonth.value.minusYears(1) }

    fun addSchedule(schedule: Schedule, onCreated: (Int) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val cleanName = schedule.name.trim().replace(Regex("\\s+"), " ")
                if (cleanName.isEmpty()) return@launch
                val maxIndex = repository.getMaxScheduleSortIndex()
                val insertedId = repository.insertSchedule(
                    schedule.copy(id = 0, name = cleanName, sortIndex = maxIndex + 1)
                ).toInt()
                refreshWidget()
                onCreated(insertedId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteScheduleNow(schedule: Schedule) {
        repository.deleteSchedule(schedule)
        refreshWidget()
    }

    fun restoreSchedule(schedule: Schedule, onRestored: (Int) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val maxIndex = repository.getMaxScheduleSortIndex()
                val restoredId = repository.insertSchedule(
                    schedule.copy(id = 0, sortIndex = maxIndex + 1)
                ).toInt()
                refreshWidget()
                onRestored(restoredId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun duplicateSchedule(schedule: Schedule) {
        viewModelScope.launch {
            try {
                val maxIndex = repository.getMaxScheduleSortIndex()
                repository.insertSchedule(schedule.copy(id = 0, name = schedule.name + copySuffix(), sortIndex = maxIndex + 1))
                refreshWidget()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun duplicateTemplate(template: Template) {
        viewModelScope.launch {
            try {
                val maxIndex = repository.getMaxTemplateSortIndex()
                repository.insertTemplate(
                    template.copy(
                        id = 0,
                        name = template.name + copySuffix(),
                        isBuiltIn = false,
                        sortIndex = maxIndex + 1
                    )
                )
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun updateSchedule(schedule: Schedule) {
        viewModelScope.launch { try { repository.updateSchedule(schedule); refreshWidget() } catch (e: Exception) { e.printStackTrace() } }
    }

    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch {
            try { deleteScheduleNow(schedule) } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun reorderSchedules(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            try {
                val moved = ListUtils.move(allSchedules.value, fromIndex, toIndex)
                moved.forEachIndexed { index, schedule ->
                    if (schedule.sortIndex != index) repository.updateSchedule(schedule.copy(sortIndex = index))
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun addTemplate(template: Template, onCreated: (Int) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val cleanName = template.name.trim().replace(Regex("\\s+"), " ")
                if (cleanName.isEmpty()) return@launch
                val maxIndex = repository.getMaxTemplateSortIndex()
                val id = repository.insertTemplate(template.copy(name = cleanName, sortIndex = maxIndex + 1)).toInt()
                onCreated(id)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun updateTemplate(template: Template) {
        if (template.isBuiltIn) return
        viewModelScope.launch {
            try {
                repository.updateTemplate(template)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteTemplate(template: Template) {
        if (template.isBuiltIn) return
        viewModelScope.launch {
            try {
                allSchedules.value.filter { it.templateId == template.id }.forEach { schedule -> repository.updateSchedule(schedule.copy(templateId = null)) }
                repository.deleteTemplate(template)
                refreshWidget()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun reorderTemplates(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            try {
                val user = allTemplates.value.filter { !it.isBuiltIn }
                val moved = ListUtils.move(user, fromIndex, toIndex)
                moved.forEachIndexed { index, template ->
                    if (template.sortIndex != index) repository.updateTemplate(template.copy(sortIndex = index))
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun updateDayException(schedule: Schedule, date: LocalDate, shiftCode: String, applyRange: String, days: Int = 1, shiftCycle: Boolean = false) {
        viewModelScope.launch {
            try {
                val updated = if (shiftCode == "S" || shiftCode == "V") PatternUtils.applyPeriod(schedule, date, days, shiftCode, shiftCycle)
                else PatternUtils.applyChange(schedule, date, shiftCode, applyRange)
                repository.updateSchedule(updated)
                refreshWidget()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun clearDayException(schedule: Schedule, date: LocalDate) {
        viewModelScope.launch {
            try {
                val newExceptions = schedule.exceptions.toMutableMap()
                newExceptions.remove(DateUtils.formatDate(date))
                val newCycleShifts = schedule.cycleShifts.filter { (rawStart, days) ->
                    val cycleStart = DateUtils.tryParseDate(rawStart) ?: return@filter false
                    val cycleEnd = cycleStart.plusDays(days.toLong())
                    date.isBefore(cycleStart) || !date.isBefore(cycleEnd)
                }
                repository.updateSchedule(
                    schedule.copy(exceptions = newExceptions, cycleShifts = newCycleShifts)
                )
                refreshWidget()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun getShiftForDate(schedule: Schedule, date: LocalDate): ShiftType? {
        val template = schedule.templateId?.let { id -> allTemplates.value.firstOrNull { it.id == id } }
        return ShiftResolver.resolve(schedule, date, template)
    }

    fun getShiftsForDate(date: LocalDate): List<Pair<Schedule, ShiftType?>> {
        return allSchedules.value.map { schedule -> schedule to getShiftForDate(schedule, date) }
    }

    fun isTipSeen(id: String): Boolean = settings.value.seenTips.split(",").contains(id)

    fun markTipSeen(id: String) {
        val current = settings.value.seenTips
        val new = if (current.isBlank()) id else current + "," + id
        updateSettings(settings.value.copy(seenTips = new))
    }

    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            try {
                settingsDataStore.updateSettings(newSettings)
                if (newSettings.notifications) {
                    // ВСЕГДА отменяем старое и создаём заново —
                    // это чинит баг, когда после смены языка/времени напоминание не приходило
                    NotificationScheduler.cancel(getApplication())
                    NotificationScheduler.scheduleNext(getApplication(), newSettings.reminderTime)
                } else {
                    NotificationScheduler.cancel(getApplication())
                    NotificationHelper.cancelSummaryNotification(getApplication())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun exportData(): String {
        val data = BackupData(
            version = BackupData.CURRENT_VERSION,
            schedules = repository.allSchedules.first(),
            templates = repository.allTemplates.first()
        )
        return gson.toJson(data)
    }

    suspend fun importData(json: String): Boolean {
        return try {
            val root = com.google.gson.JsonParser.parseString(json).asJsonObject
            if (!root.has("version")) root.addProperty("version", 1)
            val data = gson.fromJson(root, BackupData::class.java) ?: return false
            if (BackupValidator.validate(data) != null) return false

            val templatesById = data.templates.associateBy { it.id }.toMutableMap()
            Template.getBuiltInTemplates().forEach { builtIn ->
                if (templatesById[builtIn.id]?.isBuiltIn != true) {
                    templatesById[builtIn.id] = builtIn
                }
            }
            val templates = templatesById.values.sortedWith(compareByDescending<Template> { it.isBuiltIn }.thenBy { it.sortIndex }.thenBy { it.name })

            repository.replaceAllData(data.schedules, templates)
            refreshWidget()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getMonthStats(scheduleIds: List<Int>, yearMonth: YearMonth): Map<String, Int> {
        return StatsUtils.monthStats(allSchedules.value, allTemplates.value.associateBy { it.id }, scheduleIds, yearMonth)
    }

    fun getYearStats(scheduleIds: List<Int>, year: Int): Map<String, Int> {
        return StatsUtils.yearStats(allSchedules.value, allTemplates.value.associateBy { it.id }, scheduleIds, year)
    }
}