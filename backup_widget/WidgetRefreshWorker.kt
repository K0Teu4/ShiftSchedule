package com.shiftschedule.app.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class WidgetRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        var success = false
        try {
            ShiftWidgetProvider.updateAll(applicationContext)
            success = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            ShiftWidgetCompactProvider.updateAll(applicationContext)
            success = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return if (success) Result.success() else Result.retry()
    }
}