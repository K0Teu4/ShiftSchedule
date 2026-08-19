package com.shiftschedule.app.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.shiftschedule.app.data.local.SettingsDataStore
import com.shiftschedule.app.data.local.ShiftDatabase
import com.shiftschedule.app.util.PatternUtils
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val settingsDataStore = SettingsDataStore(context)
        val settings = settingsDataStore.settingsFlow.first()

        if (!settings.notifications) return Result.success()

        val now = LocalTime.now()
        val parts = settings.reminderTime.split(":")
        val targetHour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val targetMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val timePassed = now.hour > targetHour || (now.hour == targetHour && now.minute >= targetMinute)
        if (!timePassed) return Result.success()

        val today = LocalDate.now()
        if (settings.lastNotificationDate == today.toString()) return Result.success()

        val dao = ShiftDatabase.getDatabase(context).shiftDao()
        val schedules = dao.getAllSchedules().first().filter { it.isActive }
        val templates = dao.getAllTemplates().first()

        if (schedules.isEmpty()) return Result.success()

        val tomorrow = today.plusDays(1)

        val shifts = schedules.map { schedule ->
            val template = templates.find { it.id == schedule.templateId }
            schedule to PatternUtils.getShiftForDate(schedule, tomorrow, template)
        }

        val lines = shifts.mapNotNull { (schedule, shift) ->
            if (shift != null) schedule.name + " — " + shift.emoji + " " + shift.displayName else null
        }

        if (lines.isEmpty()) return Result.success()

        val sharedOff = schedules.size >= 2 &&
            shifts.all { (_, shift) -> shift?.code == "O" }

        val title = if (sharedOff) "✦ Завтра общий выходной!" else "Смены завтра"

        NotificationHelper.createNotificationChannel(context)
        NotificationHelper.showSummaryNotification(
            context,
            title,
            lines.joinToString("\n")
        )

        settingsDataStore.updateSettings(settings.copy(lastNotificationDate = today.toString()))

        return Result.success()
    }
}