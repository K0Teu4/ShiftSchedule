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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shiftschedule.app.data.model.ShiftType
import com.shiftschedule.app.ui.theme.SharedDayOff

@Composable
fun SectorDayCell(
    day: Int,
    shifts: List<Pair<String, ShiftType?>>,
    isToday: Boolean,
    isCurrentMonth: Boolean,
    isSharedDayOff: Boolean,
    isHoliday: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val haptics = LocalHapticFeedback.current
    val holidayColor = Color(0xFFFF2D55)
    val hasShifts = shifts.any { it.second != null }
    val borderColor = when {
        isToday -> MaterialTheme.colorScheme.primary
        isSharedDayOff -> SharedDayOff
        isHoliday -> holidayColor
        else -> Color.Transparent
    }
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isCurrentMonth) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp))
            .border(2.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
    ) {
        when {
            isSharedDayOff -> Box(modifier = Modifier.fillMaxSize().background(SharedDayOff.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                Text("✦", color = SharedDayOff, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            hasShifts -> Row(modifier = Modifier.fillMaxSize()) {
                shifts.forEach { (_, shift) ->
                    Box(modifier = Modifier.weight(1f).fillMaxSize().background(shift?.color?.copy(alpha = 0.5f) ?: Color.Transparent))
                }
            }
            isHoliday -> Box(modifier = Modifier.fillMaxSize().background(holidayColor.copy(alpha = 0.3f)))
        }
        if (!isSharedDayOff) {
            Box(
                modifier = Modifier.align(Alignment.Center).size(26.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.toString(),
                    color = if (isCurrentMonth) Color.White else Color.White.copy(alpha = 0.35f),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
        if (isHoliday) {
            Text("🎉", fontSize = 10.sp, modifier = Modifier.align(Alignment.TopEnd).padding(2.dp))
        }
    }
}
