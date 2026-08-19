package com.shiftschedule.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.shiftschedule.app.MainActivity
import com.shiftschedule.app.R
import com.shiftschedule.app.data.local.SettingsDataStore
import com.shiftschedule.app.data.local.ShiftDatabase
import com.shiftschedule.app.util.PatternUtils
import com.shiftschedule.app.util.Strings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

class ShiftWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) updateAppWidget(context, appWidgetManager, id)
        schedulePeriodicRefresh(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> updateAll(context)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        schedulePeriodicRefresh(context)
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, ShiftWidgetProvider::class.java)
            )
            for (id in ids) updateAppWidget(context, manager, id)
        }

        fun schedulePeriodicRefresh(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(15, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "widget_refresh",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun updateAppWidget(context: Context, manager: AppWidgetManager, id: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pending = PendingIntent.getActivity(
                context, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pending)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val settings = SettingsDataStore(context).settingsFlow.first()
                    val dao = ShiftDatabase.getDatabase(context).shiftDao()
                    val schedules = dao.getAllSchedules().first()
                    val templates = dao.getAllTemplates().first()

                    val lang = Strings.getSystemLanguage()
                    val locale = if (lang == "en") Locale.ENGLISH else Locale("ru")

                    val (bg, titleColor, textColor) = when (settings.theme) {
                        "light" -> Triple(0xE6FAF9F6.toInt(), 0xFF17171A.toInt(), 0xFF4A4A50.toInt())
                        "sepia" -> Triple(0xE633291D.toInt(), 0xFFF4EAD9.toInt(), 0xFFD8CBB6.toInt())
                        "midnight" -> Triple(0xE60B1220.toInt(), 0xFFE6EEFA.toInt(), 0xFFAAB8CE.toInt())
                        else -> Triple(0xD91A1A1A.toInt(), 0xFFFFFFFF.toInt(), 0xFFBFBFBF.toInt())
                    }

                    views.setInt(R.id.widget_root, "setBackgroundColor", bg)
                    views.setTextColor(R.id.widget_title, titleColor)
                    views.setTextColor(R.id.widget_date, textColor)
                    views.setTextColor(R.id.widget_status, titleColor)

                    val today = LocalDate.now()
                    val dateFormatter = DateTimeFormatter.ofPattern("d MMMM, EEEE", locale)

                    val status = buildString {
                        val active = schedules.filter { it.isActive }
                        if (active.isEmpty()) {
                            append(Strings.raw(lang, "no_schedules"))
                        } else {
                            active.forEach { schedule ->
                                val template = templates.find { it.id == schedule.templateId }
                                val shift = PatternUtils.getShiftForDate(schedule, today, template)
                                if (shift != null) {
                                    append(shift.emoji + " " + schedule.name + " — " + shift.displayName)
                                } else {
                                    val manual = if (lang == "en") "manual" else "ручной"
                                    append("▪ " + schedule.name + " — " + manual)
                                }
                                append("\n")
                            }
                        }
                    }

                    views.setTextViewText(R.id.widget_title, Strings.raw(lang, "widget_title"))
                    views.setTextViewText(R.id.widget_date, today.format(dateFormatter))
                    views.setTextViewText(R.id.widget_status, status.trim())

                    manager.updateAppWidget(id, views)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}