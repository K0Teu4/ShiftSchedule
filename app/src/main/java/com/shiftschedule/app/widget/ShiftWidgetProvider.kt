package com.shiftschedule.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
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

private data class WidgetStyle(
    val titleSp: Float,
    val dateSp: Float,
    val lineSp: Float,
    val maxLines: Int,
    val datePattern: String,
    val fullNames: Boolean,
    val mergeHeader: Boolean,
    val compact: Boolean
)

private data class WidgetLine(
    val scheduleId: Int,
    val shiftText: String,
    val nameText: String
)

class ShiftWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) updateAppWidget(context, appWidgetManager, id)
        schedulePeriodicRefresh(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateAppWidget(context, appWidgetManager, appWidgetId)
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

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    companion object {
        private const val WORK_NAME = "widget_refresh"
        const val EXTRA_WIDGET_SCHEDULE_ID = "widget_schedule_id"

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
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        // Текст растёт вместе с виджетом: 4 ступени по высоте, 3 по ширине
        private fun styleFor(manager: AppWidgetManager, id: Int): WidgetStyle {
            val opts = manager.getAppWidgetOptions(id)
            val h = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 180)
            val w = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250)
            val wCompact = w < 170
            val wNarrow = w < 220
            val wWide = w >= 320
            val shortP = "d MMM, EEE"
            val midP = "d MMM, EEEE"
            val fullP = "d MMMM, EEEE"
            return when {
                h < 110 -> WidgetStyle(11f, 8f, 9f, 1, shortP, false, true, wCompact && h >= 95)
                h < 190 -> WidgetStyle(
                    13f, 10f, 11f, 2,
                    if (wNarrow) shortP else if (wWide) fullP else midP,
                    wWide, false, wCompact && h >= 95
                )
                h < 280 -> WidgetStyle(
                    15f, 11f, 11f, 3,
                    if (wNarrow) shortP else if (wWide) fullP else midP,
                    !wNarrow, false, false
                )
                else -> WidgetStyle(
                    if (wWide) 18f else 16f,
                    if (wWide) 13f else 12f,
                    if (wWide) 14f else 13f,
                    5,
                    if (wNarrow) shortP else fullP,
                    !wNarrow, false, false
                )
            }
        }

        private fun shortName(name: String): String {
            val trimmed = name.trim().replace(Regex("\\s+"), " ")
            if (trimmed.length <= 12) return trimmed
            val firstWord = trimmed.substringBefore(' ')
            if (firstWord.length in 1..12 && firstWord != trimmed) return firstWord
            return trimmed.take(11) + "…"
        }

        private fun applyStatics(
            views: RemoteViews,
            style: WidgetStyle,
            lang: String,
            light: Boolean,
            titleColor: Int,
            textColor: Int
        ) {
            views.setInt(
                R.id.widget_root, "setBackgroundResource",
                if (light) R.drawable.widget_bg_light else R.drawable.widget_bg_dark
            )
            views.setInt(
                R.id.widget_divider, "setBackgroundColor",
                if (light) 0x334A4A50.toInt() else 0x40505A64.toInt()
            )

            views.setTextColor(R.id.widget_today_title, titleColor)
            views.setTextColor(R.id.widget_tomorrow_title, titleColor)
            views.setFloat(R.id.widget_today_title, "setTextSize", style.titleSp)
            views.setFloat(R.id.widget_tomorrow_title, "setTextSize", style.titleSp)

            views.setTextColor(R.id.widget_today_date, textColor)
            views.setTextColor(R.id.widget_tomorrow_date, textColor)
            views.setFloat(R.id.widget_today_date, "setTextSize", style.dateSp)
            views.setFloat(R.id.widget_tomorrow_date, "setTextSize", style.dateSp)

            views.setTextColor(R.id.widget_today_empty, titleColor)
            views.setTextColor(R.id.widget_tomorrow_empty, titleColor)
            views.setFloat(R.id.widget_today_empty, "setTextSize", style.lineSp)
            views.setFloat(R.id.widget_tomorrow_empty, "setTextSize", style.lineSp)
            views.setInt(R.id.widget_today_empty, "setMaxLines", style.maxLines + 1)
            views.setInt(R.id.widget_tomorrow_empty, "setMaxLines", style.maxLines + 1)

            if (style.mergeHeader) {
                views.setViewVisibility(R.id.widget_today_date, View.GONE)
                views.setViewVisibility(R.id.widget_tomorrow_date, View.GONE)
            } else {
                views.setViewVisibility(R.id.widget_today_date, View.VISIBLE)
                views.setViewVisibility(R.id.widget_tomorrow_date, View.VISIBLE)
                views.setTextViewText(R.id.widget_today_title, Strings.raw(lang, "widget_today"))
                views.setTextViewText(R.id.widget_tomorrow_title, Strings.raw(lang, "widget_tomorrow"))
            }
        }

        fun updateAppWidget(context: Context, manager: AppWidgetManager, id: Int) {
            val style = styleFor(manager, id)
            val views = RemoteViews(
                context.packageName,
                if (style.compact) R.layout.widget_layout_compact else R.layout.widget_layout
            )

            val openIntent = Intent(context, MainActivity::class.java).apply {
                this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pending = PendingIntent.getActivity(
                context, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pending)

            // === СИНХРОННО: заглушка ===
            val langSync = Strings.getSystemLanguage()
            applyStatics(views, style, langSync, false, 0xFFF3F4F6.toInt(), 0xFFB9C0C7.toInt())
            if (style.mergeHeader) {
                views.setTextViewText(R.id.widget_today_title, Strings.raw(langSync, "widget_today") + " · ...")
                views.setTextViewText(R.id.widget_tomorrow_title, Strings.raw(langSync, "widget_tomorrow") + " · ...")
            } else {
                views.setTextViewText(R.id.widget_today_date, "...")
                views.setTextViewText(R.id.widget_tomorrow_date, "...")
            }
            views.setViewVisibility(R.id.widget_today_empty, View.GONE)
            views.setViewVisibility(R.id.widget_tomorrow_empty, View.GONE)
            views.setViewVisibility(R.id.widget_today_list, View.VISIBLE)
            views.setViewVisibility(R.id.widget_tomorrow_list, View.VISIBLE)
            views.removeAllViews(R.id.widget_today_list)
            views.removeAllViews(R.id.widget_tomorrow_list)
            manager.updateAppWidget(id, views)

            // === АСИНХРОННО: реальные данные ===
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

                    val titleColor: Int
                    val textColor: Int
                    if (lightWidget) {
                        titleColor = 0xFF17171A.toInt()
                        textColor = 0xFF4A4A50.toInt()
                    } else {
                        titleColor = 0xFFF3F4F6.toInt()
                        textColor = 0xFFB9C0C7.toInt()
                    }

                    applyStatics(views, style, lang, lightWidget, titleColor, textColor)

                    val today = LocalDate.now()
                    val tomorrow = today.plusDays(1)
                    val dateFormatter = DateTimeFormatter.ofPattern(style.datePattern, locale)
                    val headerDateFormatter = DateTimeFormatter.ofPattern("d MMM", locale)
                    val active = schedules.filter { it.isActive }
                    val primary = active.firstOrNull { it.isPrimary } ?: active.firstOrNull()
                    val visibleSchedules = if (style.compact || style.maxLines <= 1) {
                        listOfNotNull(primary)
                    } else {
                        active
                    }

                    val noSchedulesMsg = if (lang == "en")
                        "No schedules yet.\nCreate one in the app."
                    else
                        "Нет графиков.\nСоздайте в приложении."

                    if (style.mergeHeader) {
                        val primaryLabel = Strings.raw(lang, "widget_primary")
                        views.setTextViewText(
                            R.id.widget_today_title,
                            Strings.raw(lang, "widget_today") + " · " + primaryLabel
                        )
                        views.setTextViewText(
                            R.id.widget_tomorrow_title,
                            Strings.raw(lang, "widget_tomorrow") + " · " + primaryLabel
                        )
                    } else {
                        views.setTextViewText(R.id.widget_today_date, today.format(dateFormatter))
                        views.setTextViewText(R.id.widget_tomorrow_date, tomorrow.format(dateFormatter))
                    }

                    fun linesFor(date: LocalDate): List<WidgetLine> {
                        return visibleSchedules.map { schedule ->
                            val template = templates.find { it.id == schedule.templateId }
                            val shift = ShiftResolver.resolve(schedule, date, template)
                            val shiftText = if (shift != null) {
                                val marker = if (settings.showEmoji) shift.emoji + " " else ""
                                marker + shift.displayName(lang)
                            } else {
                                val manual = if (lang == "en") "manual" else "ручной"
                                "▪ " + manual
                            }
                            val nameText = if (style.fullNames) schedule.name else shortName(schedule.name)
                            WidgetLine(schedule.id, shiftText, nameText)
                        }
                    }

                    fun fillColumn(listId: Int, emptyId: Int, date: LocalDate) {
                        views.removeAllViews(listId)
                        val lines = linesFor(date)
                        if (lines.isEmpty()) {
                            views.setViewVisibility(listId, View.GONE)
                            views.setViewVisibility(emptyId, View.VISIBLE)
                            views.setTextViewText(emptyId, noSchedulesMsg)
                            return
                        }
                        views.setViewVisibility(emptyId, View.GONE)
                        views.setViewVisibility(listId, View.VISIBLE)
                        lines.take(style.maxLines).forEach { item ->
                            val line = RemoteViews(context.packageName, R.layout.widget_line)
                            val hiddenCount = (active.size - 1).coerceAtLeast(0)
                            val shiftLabel = if ((style.compact || style.maxLines <= 1) && hiddenCount > 0) {
                                val suffix = if (lang == "en") " · +$hiddenCount more" else " · +$hiddenCount ещё"
                                item.shiftText + suffix
                            } else {
                                item.shiftText
                            }
                            line.setTextViewText(R.id.line_shift, shiftLabel)
                            line.setTextViewText(R.id.line_name, item.nameText)
                            line.setTextColor(R.id.line_shift, titleColor)
                            line.setTextColor(R.id.line_name, textColor)
                            line.setFloat(R.id.line_shift, "setTextSize", style.lineSp)
                            line.setFloat(R.id.line_name, "setTextSize", style.lineSp)
                            val clickIntent = Intent(context, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                putExtra(EXTRA_WIDGET_SCHEDULE_ID, item.scheduleId)
                            }
                            val clickPending = PendingIntent.getActivity(
                                context,
                                item.scheduleId * 2 + if (date == LocalDate.now()) 0 else 1,
                                clickIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                            line.setOnClickPendingIntent(R.id.line_root, clickPending)
                            views.addView(listId, line)
                        }
                        val hidden = if (style.compact || style.maxLines <= 1) {
                            (active.size - lines.take(style.maxLines).size).coerceAtLeast(0)
                        } else {
                            (lines.size - style.maxLines).coerceAtLeast(0)
                        }
                        if (hidden > 0 && !style.mergeHeader) {
                            val more = RemoteViews(context.packageName, R.layout.widget_line)
                            val moreText = if (lang == "en") "+$hidden more schedules" else "+$hidden ещё графика"
                            more.setTextViewText(R.id.line_shift, moreText)
                            more.setTextViewText(R.id.line_name, "")
                            more.setTextColor(R.id.line_shift, textColor)
                            more.setFloat(R.id.line_shift, "setTextSize", style.lineSp)
                            views.addView(listId, more)
                        }
                    }

                    fillColumn(R.id.widget_today_list, R.id.widget_today_empty, today)
                    fillColumn(R.id.widget_tomorrow_list, R.id.widget_tomorrow_empty, tomorrow)

                    manager.updateAppWidget(id, views)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}