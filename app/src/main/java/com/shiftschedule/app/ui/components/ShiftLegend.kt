package com.shiftschedule.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shiftschedule.app.data.model.ShiftType
import com.shiftschedule.app.util.LocalLang

@Composable
fun ShiftLegend(showEmoji: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(ShiftType.DAY, Color(0xFFF59E0B), showEmoji)
        LegendItem(ShiftType.NIGHT, Color(0xFF6366F1), showEmoji)
        LegendItem(ShiftType.OFF, Color(0xFF10B981), showEmoji)
    }
}

@Composable
private fun LegendItem(type: ShiftType, color: Color, showEmoji: Boolean) {
    val lang = LocalLang.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Text(
            text = if (showEmoji) "${type.emoji} ${type.displayName(lang)}" else type.displayName(lang),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}