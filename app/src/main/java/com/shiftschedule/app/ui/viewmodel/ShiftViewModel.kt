package com.shiftschedule.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shiftschedule.app.data.local.SettingsDataStore
import com.shiftschedule.app.data.local.ShiftDatabase
import com.shiftschedule.app.data.model.AppSettings
import com.shiftschedule.app.data.model.BackupData
import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.data.model.ShiftType
import com.shiftschedule.app.data.model.Template
import com.shiftschedule.app.data.repository.ShiftRepository
import com.shiftschedule.app.util.DateUtils
import com.shiftschedule.app.util.ListUtils
import com.shiftschedule.app.util.PatternUtils
import com.shiftschedule.app.util.StatsUtils
import com.shiftschedule.app.widget.ShiftWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

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
            if (templates.none { it.isBuiltIn }) {
                Template.getBuiltInTemplates().forEach { repository.insertTemplate(it) }
            }
            settingsDataStore.settingsFlow.first()
            _isLoaded.value = true
        }
    }

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

    fun addSchedule(schedule: Schedule) {
        viewModelScope.launch {
            try {
                val cleanName = schedule.name.trim().replace(Regex("\\s+"), " ")
                if (cleanName.isEmpty()) return@launch
                val maxIndex = repository.getMaxScheduleSortIndex()
                repository.insertSchedule(schedule.copy(name = cleanName, sortIndex = maxIndex + 1))
                refreshWidget()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun duplicateSchedule(schedule: Schedule) {
        viewModelScope.launch {
            try {
                val maxIndex = repository.getMaxScheduleSortIndex()
                repository.insertSchedule(schedule.copy(id = 0, name = schedule.name + " (копия)", sortIndex = maxIndex + 1))
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
                        name = template.name + " (копия)",
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
        viewModelScope.launch { try { repository.deleteSchedule(schedule); refreshWidget() } catch (e: Exception) { e.printStackTrace() } }
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

    fun addTemplate(template: Template) {
        viewModelScope.launch {
            try {
                val cleanName = template.name.trim().replace(Regex("\\s+"), " ")
                if (cleanName.isEmpty()) return@launch
                val maxIndex = repository.getMaxTemplateSortIndex()
                repository.insertTemplate(template.copy(name = cleanName, sortIndex = maxIndex + 1))
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun updateTemplate(template: Template) {
        viewModelScope.launch { try { repository.updateTemplate(template) } catch (e: Exception) { e.printStackTrace() } }
    }

    fun deleteTemplate(template: Template) {
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
                repository.updateSchedule(schedule.copy(exceptions = newExceptions))
                refreshWidget()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun getShiftForDate(schedule: Schedule, date: LocalDate): ShiftType? {
        val template = schedule.templateId?.let { id -> allTemplates.value.firstOrNull { it.id == id } }
        return PatternUtils.getShiftForDate(schedule, date, template)
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
        viewModelScope.launch { try { settingsDataStore.updateSettings(newSettings) } catch (e: Exception) { e.printStackTrace() } }
    }

    suspend fun exportData(): String {
        val data = BackupData(version = 1, schedules = repository.allSchedules.first(), templates = repository.allTemplates.first())
        return gson.toJson(data)
    }

    suspend fun importData(json: String): Boolean {
        return try {
            val data = gson.fromJson(json, BackupData::class.java) ?: return false
            repository.deleteAllSchedules()
            repository.deleteAllTemplates()
            data.schedules.forEach { repository.insertSchedule(it) }
            data.templates.forEach { repository.insertTemplate(it) }
            if (data.templates.none { it.isBuiltIn }) Template.getBuiltInTemplates().forEach { repository.insertTemplate(it) }
            refreshWidget()
            true
        } catch (e: Exception) { false }
    }

    fun getMonthStats(scheduleIds: List<Int>, yearMonth: YearMonth): Map<String, Int> {
        return StatsUtils.monthStats(allSchedules.value, allTemplates.value.associateBy { it.id }, scheduleIds, yearMonth)
    }

    fun getYearStats(scheduleIds: List<Int>, year: Int): Map<String, Int> {
        return StatsUtils.yearStats(allSchedules.value, allTemplates.value.associateBy { it.id }, scheduleIds, year)
    }
}



