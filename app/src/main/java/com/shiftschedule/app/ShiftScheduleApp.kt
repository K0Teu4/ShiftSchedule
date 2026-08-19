package com.shiftschedule.app

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.shiftschedule.app.notifications.NotificationWorker
import java.util.concurrent.TimeUnit

class ShiftScheduleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        setupNotificationWorker()
    }

    private fun setupNotificationWorker() {
        val request = PeriodicWorkRequestBuilder<NotificationWorker>(15, TimeUnit.MINUTES).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "notification_worker",
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }
}