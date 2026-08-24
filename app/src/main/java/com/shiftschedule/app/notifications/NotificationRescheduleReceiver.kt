package com.shiftschedule.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shiftschedule.app.data.local.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val settings = SettingsDataStore(context.applicationContext).settingsFlow.first()
                if (settings.notifications) NotificationScheduler.scheduleNext(context.applicationContext, settings.reminderTime)
                else NotificationScheduler.cancel(context.applicationContext)
            } finally {
                pending.finish()
            }
        }
    }
}
