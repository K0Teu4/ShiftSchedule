package com.shiftschedule.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.shiftschedule.app.data.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    private object Keys {
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
        val SHOW_EMOJI = booleanPreferencesKey("show_emoji")
        val WEEK_START = stringPreferencesKey("week_start")
        val REMINDER_TIME = stringPreferencesKey("reminder_time")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val LAST_NOTIFICATION = stringPreferencesKey("last_notification_date")
        val SEEN_TIPS = stringPreferencesKey("seen_tips")
        val THEME = stringPreferencesKey("theme")
        val LANG = stringPreferencesKey("lang")
        val RF_HOLIDAYS = booleanPreferencesKey("rf_holidays")
        val HOUR_RATE = intPreferencesKey("hour_rate")
        val DAY_HOURS = intPreferencesKey("day_hours")
        val NIGHT_HOURS = intPreferencesKey("night_hours")
        val LAST_SEEN_VERSION = stringPreferencesKey("last_seen_version")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            notifications = preferences[Keys.NOTIFICATIONS] ?: true,
            showEmoji = preferences[Keys.SHOW_EMOJI] ?: true,
            weekStart = preferences[Keys.WEEK_START] ?: "mon",
            reminderTime = preferences[Keys.REMINDER_TIME] ?: "08:00",
            hasCompletedOnboarding = preferences[Keys.HAS_COMPLETED_ONBOARDING] ?: false,
            lastNotificationDate = preferences[Keys.LAST_NOTIFICATION] ?: "",
            seenTips = preferences[Keys.SEEN_TIPS] ?: "",
            theme = preferences[Keys.THEME] ?: "dark",
            lang = preferences[Keys.LANG] ?: "system",
            rfHolidays = preferences[Keys.RF_HOLIDAYS] ?: true,
            hourRate = preferences[Keys.HOUR_RATE] ?: 0,
            dayHours = preferences[Keys.DAY_HOURS] ?: 8,
            nightHours = preferences[Keys.NIGHT_HOURS] ?: 16,
            lastSeenVersion = preferences[Keys.LAST_SEEN_VERSION] ?: ""
        )
    }

    suspend fun updateSettings(settings: AppSettings) {
        context.dataStore.edit { preferences ->
            preferences[Keys.NOTIFICATIONS] = settings.notifications
            preferences[Keys.SHOW_EMOJI] = settings.showEmoji
            preferences[Keys.WEEK_START] = settings.weekStart
            preferences[Keys.REMINDER_TIME] = settings.reminderTime
            preferences[Keys.HAS_COMPLETED_ONBOARDING] = settings.hasCompletedOnboarding
            preferences[Keys.LAST_NOTIFICATION] = settings.lastNotificationDate
            preferences[Keys.SEEN_TIPS] = settings.seenTips
            preferences[Keys.THEME] = settings.theme
            preferences[Keys.LANG] = settings.lang
            preferences[Keys.RF_HOLIDAYS] = settings.rfHolidays
            preferences[Keys.HOUR_RATE] = settings.hourRate
            preferences[Keys.DAY_HOURS] = settings.dayHours
            preferences[Keys.NIGHT_HOURS] = settings.nightHours
            preferences[Keys.LAST_SEEN_VERSION] = settings.lastSeenVersion
        }
    }
}
