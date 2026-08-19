package com.shiftschedule.app.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

object Haptics {
    fun tap(h: HapticFeedback) = h.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    fun confirm(h: HapticFeedback) = h.performHapticFeedback(HapticFeedbackType.LongPress)
    fun long(h: HapticFeedback) = h.performHapticFeedback(HapticFeedbackType.LongPress)
}