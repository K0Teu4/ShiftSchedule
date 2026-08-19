package com.shiftschedule.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shiftschedule.app.data.model.ShiftType
import com.shiftschedule.app.ui.theme.SharedDayOff
import com.shiftschedule.app.ui.theme.TextSecondary

@Composable
fun SectorDayCell(
    day: Int,
    shifts: List<Pair<String, ShiftType?>>,
    isToday: Boolean,
    isCurrentMonth: Boolean,
    isSharedDayOff: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val haptics = LocalHapticFeedback.current

    val borderColor = when {
        isSharedDayOff -> SharedDayOff
        isToday -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }

    val codes = shifts.map { it.second?.code }.distinct()
    val singleShift = if (codes.size == 1) shifts.firstOrNull()?.second else null

    val baseColor = when {
        !isCurrentMonth -> MaterialTheme.colorScheme.background
        singleShift != null -> singleShift.color.copy(alpha = 0.8f)
        else -> MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(baseColor)
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        if (isCurrentMonth && singleShift == null && shifts.isNotEmpty() && shifts.size <= 4) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                if (shifts.size == 2) {
                    shifts[0].second?.let { shift ->
                        drawRect(
                            color = shift.color.copy(alpha = 0.8f),
                            topLeft = Offset.Zero,
                            size = Size(width / 2, height)
                        )
                    }
                    shifts[1].second?.let { shift ->
                        drawRect(
                            color = shift.color.copy(alpha = 0.8f),
                            topLeft = Offset(width / 2, 0f),
                            size = Size(width / 2, height)
                        )
                    }
                } else if (shifts.size == 3) {
                    val partHeight = height / 3
                    shifts.forEachIndexed { index, pair ->
                        pair.second?.let { shift ->
                            drawRect(
                                color = shift.color.copy(alpha = 0.8f),
                                topLeft = Offset(0f, index * partHeight),
                                size = Size(width, partHeight)
                            )
                        }
                    }
                } else if (shifts.size == 4) {
                    val halfWidth = width / 2
                    val halfHeight = height / 2
                    shifts[0].second?.let { shift ->
                        drawRect(
                            color = shift.color.copy(alpha = 0.8f),
                            topLeft = Offset.Zero,
                            size = Size(halfWidth, halfHeight)
                        )
                    }
                    shifts[1].second?.let { shift ->
                        drawRect(
                            color = shift.color.copy(alpha = 0.8f),
                            topLeft = Offset(halfWidth, 0f),
                            size = Size(halfWidth, halfHeight)
                        )
                    }
                    shifts[2].second?.let { shift ->
                        drawRect(
                            color = shift.color.copy(alpha = 0.8f),
                            topLeft = Offset(0f, halfHeight),
                            size = Size(halfWidth, halfHeight)
                        )
                    }
                    shifts[3].second?.let { shift ->
                        drawRect(
                            color = shift.color.copy(alpha = 0.8f),
                            topLeft = Offset(halfWidth, halfHeight),
                            size = Size(halfWidth, halfHeight)
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .size(22.dp)
                .background(Color(0xB3000000), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = if (isCurrentMonth) Color.White else TextSecondary,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )
        }

        if (isSharedDayOff) {
            Text(
                text = "✦",
                style = MaterialTheme.typography.bodyMedium,
                color = SharedDayOff,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
            )
        }
    }
}