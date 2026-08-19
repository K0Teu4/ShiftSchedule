package com.shiftschedule.app.data.model

data class AppSettings(
    val notifications: Boolean = true,
    val showEmoji: Boolean = true,
    val weekStart: String = "mon",
    val reminderTime: String = "08:00",
    val hasCompletedOnboarding: Boolean = false,
    val lastNotificationDate: String = "",
    val seenTips: String = "",
    val theme: String = "dark"
)