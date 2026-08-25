package com.shiftschedule.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shiftschedule.app.data.model.ShiftType
import com.shiftschedule.app.ui.theme.SharedDayOff
import com.shiftschedule.app.ui.theme.SharedDayWork
import com.shiftschedule.app.ui.theme.SharedNightWork
import com.shiftschedule.app.util.LocalLang

@Composable
fun SectorDayCell(
    day: Int,
    shifts: List<Pair<String, ShiftType?>>,
    isToday: Boolean,
    isCurrentMonth: Boolean,
    isSharedDayOff: Boolean,
    isSharedDayWork: Boolean = false,
    isSharedNightWork: Boolean = false,
    isHoliday: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val haptics = LocalHapticFeedback.current
    val lang = LocalLang.current
    val shape = RoundedCornerShape(13.dp)
    val hasShifts = shifts.any { it.second != null }
    val outline = when {
        isSharedDayWork && isSharedNightWork -> Brush.horizontalGradient(listOf(SharedDayWork, SharedNightWork))
        isSharedDayWork -> Brush.linearGradient(listOf(SharedDayWork, SharedDayWork))
        isSharedNightWork -> Brush.linearGradient(listOf(SharedNightWork, SharedNightWork))
        isSharedDayOff -> Brush.linearGradient(listOf(SharedDayOff, SharedDayOff))
        isToday -> Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary))
        else -> null
    }
    val outlineWidth = when {
        isSharedDayWork || isSharedNightWork || isSharedDayOff -> 3.dp
        isToday -> 1.5.dp
        else -> 0.dp
    }

    val description = buildString {
        append(day)
        shifts.forEach { (name, shift) ->
            append(", ").append(name).append(": ").append(shift?.displayName(lang) ?: "—")
        }
        if (isSharedDayWork) append(", ").append(if (lang == "en") "shared day" else "общий день")
        if (isSharedNightWork) append(", ").append(if (lang == "en") "shared night" else "общая ночь")
        if (isSharedDayOff) append(", ").append(if (lang == "en") "shared day off" else "общий выходной")
        if (isHoliday) append(", ").append(if (lang == "en") "public holiday" else "государственный праздник")
    }

    Box(
        modifier = modifier
            .aspectRatio(0.94f)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow, shape)
            .clickable {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        if (hasShifts) {
            Row(Modifier.fillMaxSize().clip(shape)) {
                shifts.forEach { (_, shift) ->
                    val fill = shift?.color ?: MaterialTheme.colorScheme.surfaceVariant
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(fill.copy(alpha = if (shift == null) 0.18f else 0.98f))
                    )
                }
            }
        } else if (isHoliday) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f)))
        }

        Box(
            modifier = Modifier
                .size(29.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                day.toString(),
                color = if (isCurrentMonth) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isToday || outline != null) FontWeight.ExtraBold else FontWeight.Bold
            )
        }

        if (isHoliday) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF4F87)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Star, null, tint = Color.White, modifier = Modifier.size(11.dp))
            }
        }

        if (outline != null && outlineWidth > 0.dp) {
            Box(Modifier.matchParentSize().border(outlineWidth, outline, shape))
        }
    }
}
