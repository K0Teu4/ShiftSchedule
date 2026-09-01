package com.shiftschedule.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shiftschedule.app.data.model.ShiftType
import com.shiftschedule.app.util.LocalLang
import com.shiftschedule.app.ui.theme.PublicHoliday

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
    val lang = LocalLang.current
    val accent = shiftType?.color
    val holidayColor = PublicHoliday
    val background = when {
        !isCurrentMonth -> MaterialTheme.colorScheme.surface.copy(alpha = 0.22f)
        accent != null -> accent.copy(alpha = 0.98f)
        isHoliday -> holidayColor.copy(alpha = 0.20f)
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }
    val borderColor = when {
        isToday -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .aspectRatio(0.92f)
            .background(background, RoundedCornerShape(15.dp))
            .border(if (borderColor == Color.Transparent) 0.dp else 1.5.dp, borderColor, RoundedCornerShape(15.dp))
            .combinedClickable(
                onClick = { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); onClick() },
                onLongClick = { haptics.performHapticFeedback(HapticFeedbackType.LongPress); onLongClick() }
            )
            .semantics {
                contentDescription = buildString {
                    append(day)
                    shiftType?.let { append(", ").append(it.displayName(lang)) }
                    if (isToday) append(", ").append(if (lang == "en") "today" else "сегодня")
                    if (isHoliday) append(", ").append(if (lang == "en") "public holiday" else "государственный праздник")
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(
                day.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (isCurrentMonth) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = .35f)
            )
            shiftType?.let { type ->
                if (showEmoji) {
                    Text(type.emoji, fontSize = 15.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
        if (isHoliday) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .size(20.dp)
                    .background(holidayColor, CircleShape)
                    .border(1.5.dp, Color.White.copy(alpha = 0.92f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Star, null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
fun WeekHeader(weekStart: String, lang: String = "ru", modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)) {
        com.shiftschedule.app.util.DateUtils.weekDayHeaders(weekStart, lang).forEach { day ->
            Text(day, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}