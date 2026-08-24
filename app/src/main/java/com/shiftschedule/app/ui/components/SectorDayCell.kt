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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shiftschedule.app.data.model.ShiftType
import com.shiftschedule.app.ui.theme.SharedDayOff
import com.shiftschedule.app.ui.theme.SharedDayWork
import com.shiftschedule.app.ui.theme.SharedNightWork

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
    val holidayColor = Color(0xFFFF2D55)
    val shape = RoundedCornerShape(12.dp)
    val hasShifts = shifts.any { it.second != null }
    // A common working day is only a match when every selected schedule has
    // the SAME working shift. Day + Night is intentionally not highlighted.
    val sharedWorkColor = when {
        isSharedDayWork -> SharedDayWork
        isSharedNightWork -> SharedNightWork
        else -> Color.Transparent
    }

    // Priority is deliberately explicit: today remains visible even when the
    // date is also a common workday/common day off.
    val outerBorder = when {
        sharedWorkColor != Color.Transparent -> sharedWorkColor
        isToday -> MaterialTheme.colorScheme.primary
        isSharedDayOff -> SharedDayOff
        isHoliday -> holidayColor
        else -> Color.Transparent
    }

    val description = buildString {
        append(day)
        shifts.forEach { (name, shift) ->
            append(", ").append(name).append(": ").append(shift?.displayName ?: "нет смены")
        }
        if (isSharedDayWork) append(", общий дневной рабочий день")
        if (isSharedNightWork) append(", общий ночной рабочий день")
        if (isSharedDayOff) append(", общий выходной")
    }

    Box(
        modifier = modifier
            .aspectRatio(0.94f)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow, shape)
            .border(if (outerBorder == Color.Transparent) 0.dp else 2.dp, outerBorder, shape)
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
                    val fill = shift?.color ?: MaterialTheme.colorScheme.surface
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(fill.copy(alpha = if (shift == null) 0.08f else 0.34f))
                    )
                }
            }
        } else if (isHoliday) {
            Box(Modifier.fillMaxSize().background(holidayColor.copy(alpha = 0.16f)))
        }

        // The outer contour is the only common-work marker. Day and night use
        // different colors; a mixed Day + Night combination gets no contour.
        if (sharedWorkColor != Color.Transparent) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(3.dp)
                    .border(2.dp, sharedWorkColor.copy(alpha = 0.98f), shape)
            )
        }

        // Common days off keep the date number visible. The previous version
        // replaced the number with a star, which made the calendar harder to read.
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.toString(),
                color = if (isCurrentMonth) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isToday || sharedWorkColor != Color.Transparent || isSharedDayOff) FontWeight.ExtraBold else FontWeight.Medium
            )
        }

        if (isToday && sharedWorkColor != Color.Transparent) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(6.dp)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.95f), RoundedCornerShape(8.dp))
            )
        }

        if (isSharedDayOff) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(SharedDayOff)
            )
        }

        if (isHoliday) {
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(holidayColor)
            )
        }
    }
}
