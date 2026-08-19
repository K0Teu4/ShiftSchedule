package com.shiftschedule.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.shiftschedule.app.MainActivity
import com.shiftschedule.app.R
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

class ShiftWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}

internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    val views = RemoteViews(context.packageName, R.layout.widget_layout)

    val openIntent = Intent(context, MainActivity::class.java)
    val pending = PendingIntent.getActivity(
        context,
        0,
        openIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.widget_root, pending)

    CoroutineScope(Dispatchers.IO).launch {
        val dao = ShiftDatabase.getDatabase(context).shiftDao()
        val today = LocalDate.now()
        val schedules = dao.getAllSchedules().first()
        val templates = dao.getAllTemplates().first()

        val lang = Strings.getSystemLanguage()
        val locale = if (lang == "en") Locale.ENGLISH else Locale("ru")
        val dateFormatter = DateTimeFormatter.ofPattern("d MMMM, EEEE", locale)

        val noSchedulesText = if (lang == "en") "No schedules" else "Нет графиков"
        val titleText = if (lang == "en") "Shifts today" else "Смены сегодня"

        val lines = buildString {
            schedules.filter { it.isActive }.forEach { schedule ->
                val template = templates.find { it.id == schedule.templateId }
                val shift = PatternUtils.getShiftForDate(schedule, today, template)
                if (shift != null) {
                    append(shift.emoji + " " + schedule.name + " — " + shift.displayName)
                } else {
                    val manualText = if (lang == "en") "manual" else "ручной"
                    append("▪ " + schedule.name + " — " + manualText)
                }
                append("\n")
            }
        }

        views.setTextViewText(R.id.widget_title, titleText)
        views.setTextViewText(R.id.widget_date, today.format(dateFormatter))
        views.setTextViewText(R.id.widget_status, if (lines.isEmpty()) noSchedulesText else lines.trim())

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}