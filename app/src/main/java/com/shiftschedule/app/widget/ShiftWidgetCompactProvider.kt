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

class ShiftWidgetCompactProvider : AppWidgetProvider() {

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
        private const val WORK_NAME = "widget_compact_refresh"

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, ShiftWidgetCompactProvider::class.java)
            )
            for (id in ids) updateAppWidget(context, manager, id)
        }

        fun schedulePeriodicRefresh(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(15, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun updateAppWidget(context: Context, manager: AppWidgetManager, id: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout_compact)

            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pending = PendingIntent.getActivity(
                context, 100 + id, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pending)

            // === СИНХРОННОЕ ОБНОВЛЕНИЕ-ЗАГЛУШКА ===
            val langSync = Strings.getSystemLanguage()
            views.setTextViewText(R.id.widget_today_title, Strings.raw(langSync, "widget_today"))
            views.setTextViewText(R.id.widget_tomorrow_title, Strings.raw(langSync, "widget_tomorrow"))
            views.setTextViewText(R.id.widget_today_date, "...")
            views.setTextViewText(R.id.widget_tomorrow_date, "...")
            views.setTextViewText(R.id.widget_today_status, "")
            views.setTextViewText(R.id.widget_tomorrow_status, "")
            views.setInt(R.id.widget_root, "setBackgroundColor", 0xE61A2025.toInt())
            views.setTextColor(R.id.widget_today_title, 0xFFF3F4F6.toInt())
            views.setTextColor(R.id.widget_tomorrow_title, 0xFFF3F4F6.toInt())
            views.setTextColor(R.id.widget_today_date, 0xFFB9C0C7.toInt())
            views.setTextColor(R.id.widget_tomorrow_date, 0xFFB9C0C7.toInt())
            views.setTextColor(R.id.widget_today_status, 0xFFF3F4F6.toInt())
            views.setTextColor(R.id.widget_tomorrow_status, 0xFFF3F4F6.toInt())
            manager.updateAppWidget(id, views)

            // === АСИНХРОННОЕ ОБНОВЛЕНИЕ С РЕАЛЬНЫМИ ДАННЫМИ ===
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val settings = SettingsDataStore(context).settingsFlow.first()
                    val dao = ShiftDatabase.getDatabase(context).shiftDao()
                    val schedules = dao.getAllSchedules().first()
                    val templates = dao.getAllTemplates().first()

                    val lang = when (settings.lang) {
                        "ru" -> "ru"
                        "en" -> "en"
                        else -> Strings.getSystemLanguage()
                    }
                    val locale = if (lang == "en") Locale.ENGLISH else Locale("ru")

                    val uiMode = context.resources.configuration.uiMode
                    val nightMask = android.content.res.Configuration.UI_MODE_NIGHT_MASK
                    val nightNo = android.content.res.Configuration.UI_MODE_NIGHT_NO
                    val systemLight = (uiMode and nightMask) == nightNo
                    val lightWidget = settings.theme == "light" || (settings.theme == "system" && systemLight)

                    val bg: Int
                    val titleColor: Int
                    val textColor: Int
                    if (lightWidget) {
                        bg = 0xE6FAF9F6.toInt()
                        titleColor = 0xFF17171A.toInt()
                        textColor = 0xFF4A4A50.toInt()
                    } else {
                        bg = 0xE61A2025.toInt()
                        titleColor = 0xFFF3F4F6.toInt()
                        textColor = 0xFFB9C0C7.toInt()
                    }

                    views.setInt(R.id.widget_root, "setBackgroundColor", bg)
                    views.setInt(R.id.widget_today_panel, "setBackgroundColor", bg)
                    views.setInt(R.id.widget_tomorrow_panel, "setBackgroundColor", bg)
                    views.setTextColor(R.id.widget_today_title, titleColor)
                    views.setTextColor(R.id.widget_tomorrow_title, titleColor)
                    views.setTextColor(R.id.widget_today_date, textColor)
                    views.setTextColor(R.id.widget_tomorrow_date, textColor)
                    views.setTextColor(R.id.widget_today_status, titleColor)
                    views.setTextColor(R.id.widget_tomorrow_status, titleColor)

                    val today = LocalDate.now()
                    val tomorrow = today.plusDays(1)
                    val dateFormatter = DateTimeFormatter.ofPattern("d MMM, EEE", locale)
                    val active = schedules.filter { it.isActive }

                    val noSchedulesMsg = if (lang == "en") "No schedules yet.\nCreate one in the app." else "Нет графиков.\nСоздайте в приложении."

                    val todayStatus = buildStatusForDate(active, templates, settings, lang, today, noSchedulesMsg)
                    val tomorrowStatus = buildStatusForDate(active, templates, settings, lang, tomorrow, noSchedulesMsg)

                    views.setTextViewText(R.id.widget_today_title, Strings.raw(lang, "widget_today"))
                    views.setTextViewText(R.id.widget_tomorrow_title, Strings.raw(lang, "widget_tomorrow"))
                    views.setTextViewText(R.id.widget_today_date, today.format(dateFormatter))
                    views.setTextViewText(R.id.widget_tomorrow_date, tomorrow.format(dateFormatter))
                    views.setTextViewText(R.id.widget_today_status, todayStatus)
                    views.setTextViewText(R.id.widget_tomorrow_status, tomorrowStatus)

                    manager.updateAppWidget(id, views)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun buildStatusForDate(
            active: List<com.shiftschedule.app.data.model.Schedule>,
            templates: List<com.shiftschedule.app.data.model.Template>,
            settings: com.shiftschedule.app.data.model.AppSettings,
            lang: String,
            date: LocalDate,
            noSchedulesMsg: String
        ): String {
            if (active.isEmpty()) return noSchedulesMsg
            val sb = StringBuilder()
            val maxItems = 2
            active.take(maxItems).forEachIndexed { index, schedule ->
                val template = templates.find { it.id == schedule.templateId }
                val shift = ShiftResolver.resolve(schedule, date, template)
                if (index > 0) sb.append("\n")
                if (shift != null) {
                    val marker = if (settings.showEmoji) shift.emoji + " " else ""
                    sb.append(marker).append(shift.displayName(lang))
                } else {
                    val manual = if (lang == "en") "manual" else "\u0440\u0443\u0447\u043d\u043e\u0439"
                    sb.append("\u25aa ").append(manual)
                }
            }
            if (active.size > maxItems) sb.append("\n\u2026")
            return sb.toString()
        }
    }
}