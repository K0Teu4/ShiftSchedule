package com.shiftschedule.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shiftschedule.app.data.model.ShiftType
import com.shiftschedule.app.util.DateUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DayCell(
    day: Int,
    shiftType: ShiftType?,
    isToday: Boolean,
    isCurrentMonth: Boolean,
    showEmoji: Boolean,
    isHoliday: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val haptics = LocalHapticFeedback.current
    val holidayTint = Color(0xFFFFB6C1).copy(alpha = 0.35f)
    val targetBg = when {
        !isCurrentMonth -> MaterialTheme.colorScheme.background
        isHoliday && shiftType == null -> holidayTint
        isHoliday && shiftType != null -> shiftType.color.copy(alpha = 0.35f).let { Color((it.red + holidayTint.red)/2, (it.green + holidayTint.green)/2, (it.blue + holidayTint.blue)/2) }
        shiftType != null -> shiftType.color.copy(alpha = 0.25f)
        else -> MaterialTheme.colorScheme.surface
    }
    val bgColor by animateColorAsState(targetValue = targetBg, animationSpec = tween(300), label = "dayBg")
    val holidayColor = Color(0xFFFF2D55)
    val borderColor = when {
        isToday -> MaterialTheme.colorScheme.primary
        isHoliday -> holidayColor
        else -> Color.Transparent
    }
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor, RoundedCornerShape(10.dp))
            .border(2.dp, borderColor, RoundedCornerShape(10.dp))
            .semantics {
                contentDescription = buildString {
                    append(day.toString())
                    if (shiftType != null) append(", " + shiftType.displayName)
                    if (isHoliday) append(", holiday")
                    if (isToday) append(", today")
                }
            }
            .combinedClickable(
                onClick = { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); onClick() },
                onLongClick = { haptics.performHapticFeedback(HapticFeedbackType.LongPress); onLongClick() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrentMonth) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
            )
            if (shiftType != null || isHoliday) {
                Text(
                    text = buildString {
                        if (shiftType != null) append(if (showEmoji) shiftType.emoji else shiftType.displayName.take(1))
                        if (isHoliday) append(" 🎉")
                    }.trim(),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun WeekHeader(weekStart: String, lang: String = "ru", modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        DateUtils.weekDayHeaders(weekStart, lang).forEach { day ->
            Text(text = day, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        }
    }
}

