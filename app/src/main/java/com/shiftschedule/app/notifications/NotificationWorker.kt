package com.shiftschedule.app.notifications

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.shiftschedule.app.data.local.SettingsDataStore
import com.shiftschedule.app.data.local.ShiftDatabase
import com.shiftschedule.app.domain.ShiftResolver
import com.shiftschedule.app.util.Strings
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

        if (!settings.notifications) {
            NotificationScheduler.cancel(context)
            return Result.success()
        }

        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationScheduler.scheduleNext(context, settings.reminderTime)
            return Result.success()
        }

        val targetTime = runCatching { LocalTime.parse(settings.reminderTime) }
            .getOrElse { LocalTime.of(8, 0) }
        val now = LocalTime.now()
        val today = LocalDate.now()
        val timePassed = !now.isBefore(targetTime)

        if (!timePassed) {
            NotificationScheduler.scheduleNext(context, settings.reminderTime)
            return Result.success()
        }

        if (settings.lastNotificationDate == today.toString()) {
            NotificationScheduler.scheduleNext(context, settings.reminderTime)
            return Result.success()
        }

        val dao = ShiftDatabase.getDatabase(context).shiftDao()
        val schedules = dao.getAllSchedules().first().filter { it.isActive }
        val templates = dao.getAllTemplates().first()

        if (schedules.isEmpty()) {
            NotificationScheduler.scheduleNext(context, settings.reminderTime)
            return Result.success()
        }

        val tomorrow = today.plusDays(1)
        val lang = when (settings.lang) {
            "ru" -> "ru"
            "en" -> "en"
            else -> Strings.getSystemLanguage()
        }
        val shifts = schedules.map { schedule ->
            val template = templates.find { it.id == schedule.templateId }
            schedule to ShiftResolver.resolve(schedule, tomorrow, template)
        }

        val lines = shifts.mapNotNull { (schedule, shift) ->
            shift?.let {
                val marker = if (settings.showEmoji) it.emoji + " " else ""
                schedule.name + " — " + marker + it.displayName(lang)
            }
        }

        if (lines.isNotEmpty()) {
            val sharedOff = schedules.size >= 2 && shifts.all { (_, shift) -> shift?.code == "O" }
            val title = if (sharedOff) Strings.raw(lang, "notification_shared_off") else Strings.raw(lang, "notification_title")
            NotificationHelper.showSummaryNotification(context, title, lines.joinToString("\n"))
            settingsDataStore.updateSettings(settings.copy(lastNotificationDate = today.toString()))
        }

        NotificationScheduler.scheduleNext(context, settings.reminderTime)
        return Result.success()
    }
}
