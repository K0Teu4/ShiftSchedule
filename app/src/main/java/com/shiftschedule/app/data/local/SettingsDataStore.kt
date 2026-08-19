package com.shiftschedule.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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
            theme = preferences[Keys.THEME] ?: "dark"
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
        }
    }
}