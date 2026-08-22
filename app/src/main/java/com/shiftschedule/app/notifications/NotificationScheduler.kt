package com.shiftschedule.app.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    private const val WORK_NAME = "shift_notification"
    private const val LEGACY_WORK_NAME = "notification_worker"

    fun scheduleNext(context: Context, reminderTime: String) {
        val delay = calculateDelayMillis(reminderTime)
        val request = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancel(context: Context) {
        val manager = WorkManager.getInstance(context)
        manager.cancelUniqueWork(WORK_NAME)
        manager.cancelUniqueWork(LEGACY_WORK_NAME)
    }

    fun cancelLegacy(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(LEGACY_WORK_NAME)
    }

    private fun calculateDelayMillis(reminderTime: String): Long {
        val time = runCatching { LocalTime.parse(reminderTime) }.getOrElse { LocalTime.of(8, 0) }
        val now = LocalDateTime.now()
        var target = now.toLocalDate().atTime(time)
        if (!target.isAfter(now)) target = target.plusDays(1)
        return Duration.between(now, target).toMillis().coerceAtLeast(1_000L)
    }
}
