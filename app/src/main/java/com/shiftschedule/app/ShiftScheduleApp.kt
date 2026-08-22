package com.shiftschedule.app

import android.app.Application
import com.shiftschedule.app.data.local.SettingsDataStore
import com.shiftschedule.app.notifications.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ShiftScheduleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        com.shiftschedule.app.notifications.NotificationHelper.createNotificationChannel(this)
        NotificationScheduler.cancelLegacy(this)
        setupNotificationWorker()
    }

    private fun setupNotificationWorker() {
        CoroutineScope(Dispatchers.Default).launch {
            val settings = SettingsDataStore(this@ShiftScheduleApp).settingsFlow.first()
            if (settings.notifications) {
                NotificationScheduler.scheduleNext(this@ShiftScheduleApp, settings.reminderTime)
            } else {
                NotificationScheduler.cancel(this@ShiftScheduleApp)
            }
        }
    }
}
