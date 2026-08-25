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
import com.shiftschedule.app.domain.ShiftResolver
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
                    // Создаём НОВЫЙ экземпляр DataStore для чтения свежих данных
                    val settingsDataStore = SettingsDataStore(context.applicationContext)
                    val settings = settingsDataStore.settingsFlow.first()
                    
                    val dao = ShiftDatabase.getDatabase(context).shiftDao()
                    val schedules = dao.getAllSchedules().first()
                    val templates = dao.getAllTemplates().first()

                    // ОПРЕДЕЛЯЕМ ЯЗЫК
                    val lang = when (settings.lang) {
                        "ru" -> "ru"
                        "en" -> "en"
                        else -> Strings.getSystemLanguage()
                    }
                    
                    // Отладка: можно раскомментировать для проверки
                    // android.util.Log.d("Widget", "Language from settings: ${settings.lang}, resolved: $lang")
                    
                    val locale = if (lang == "en") Locale.ENGLISH else Locale("ru")

                    val (bg, titleColor, textColor) = when (settings.theme) {
                        "light" -> Triple(0xE6FAF9F6.toInt(), 0xFF17171A.toInt(), 0xFF4A4A50.toInt())
                        "sand" -> Triple(0xE6FFF6E9.toInt(), 0xFF241A0E.toInt(), 0xFF6E5A43.toInt())
                        "sepia" -> Triple(0xE633291D.toInt(), 0xFFF4EAD9.toInt(), 0xFFD8CBB6.toInt())
                        "midnight" -> Triple(0xE60A0F16.toInt(), 0xFFE4F6FF.toInt(), 0xFFAAB8CE.toInt())
                        "ocean" -> Triple(0xE607404C.toInt(), 0xFFDFF6F9.toInt(), 0xFF9CCFD6.toInt())
                        "forest" -> Triple(0xE6142019.toInt(), 0xFFE8F3E9.toInt(), 0xFFA8BCA9.toInt())
                        "berry" -> Triple(0xE6221220.toInt(), 0xFFF6E9F4.toInt(), 0xFFC9B1C4.toInt())
                        "plum" -> Triple(0xE62A1439.toInt(), 0xFFF1E9FF.toInt(), 0xFFC5B8D8.toInt())
                        "graphite" -> Triple(0xE61A1C20.toInt(), 0xFFE8EAED.toInt(), 0xFFB9BDC5.toInt())
                        else -> Triple(0xD91A1A1A.toInt(), 0xFFFFFFFF.toInt(), 0xFFBFBFBF.toInt())
                    }

                    views.setInt(R.id.widget_root, "setBackgroundColor", bg)
                    views.setTextColor(R.id.widget_today_title, titleColor)
                    views.setTextColor(R.id.widget_today_date, textColor)
                    views.setTextColor(R.id.widget_today_status, titleColor)
                    views.setTextColor(R.id.widget_tomorrow_title, titleColor)
                    views.setTextColor(R.id.widget_tomorrow_date, textColor)
                    views.setTextColor(R.id.widget_tomorrow_status, titleColor)

                    val today = LocalDate.now()
                    val tomorrow = today.plusDays(1)
                    val dateFormatter = DateTimeFormatter.ofPattern("d MMMM, EEEE", locale)

                    fun buildStatusText(forDate: LocalDate): String {
                        val active = schedules.filter { it.isActive }
                        if (active.isEmpty()) return Strings.raw(lang, "no_schedules")
                        return buildString {
                            active.forEach { schedule ->
                                val template = templates.find { it.id == schedule.templateId }
                                val shift = ShiftResolver.resolve(schedule, forDate, template)
                                if (shift != null) {
                                    val marker = if (settings.showEmoji) shift.emoji + " " else ""
                                    append(marker + schedule.name + " — " + shift.displayName(lang))
                                } else {
                                    val manual = if (lang == "en") "manual" else "ручной"
                                    append("▪ " + schedule.name + " — " + manual)
                                }
                                append("\n")
                            }
                        }.trim()
                    }

                    views.setTextViewText(R.id.widget_today_title, Strings.raw(lang, "today"))
                    views.setTextViewText(R.id.widget_today_date, today.format(dateFormatter))
                    views.setTextViewText(R.id.widget_today_status, buildStatusText(today))

                    views.setTextViewText(R.id.widget_tomorrow_title, Strings.raw(lang, "tomorrow"))
                    views.setTextViewText(R.id.widget_tomorrow_date, tomorrow.format(dateFormatter))
                    views.setTextViewText(R.id.widget_tomorrow_status, buildStatusText(tomorrow))

                    manager.updateAppWidget(id, views)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}