package com.shiftschedule.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shiftschedule.app.data.model.ShiftType

@Composable
fun AppHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    action: (@Composable RowScope.() -> Unit)? = null
) {
    // A vertical header is deliberately used when an action exists. This prevents
    // long Russian titles/subtitles from colliding with the schedule selector on
    // small screens.
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            if (action != null) action.invoke(this)
        }
        subtitle?.let {
            Spacer(Modifier.height(3.dp))
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

@Composable
fun SectionLabel(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) {
            androidx.compose.material3.TextButton(onClick = onAction) { Text(action) }
        }
    }
}

@Composable
fun SurfaceCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = { content() }
    )
}

@Composable
fun ShiftHeroCard(
    dateLabel: String,
    shift: ShiftType?,
    secondaryLabel: String,
    showEmoji: Boolean = true,
    modifier: Modifier = Modifier
) {
    val accent = shift?.color ?: MaterialTheme.colorScheme.primary
    val container = Color(
        red = (accent.red * 0.22f + MaterialTheme.colorScheme.surface.red * 0.78f).coerceIn(0f, 1f),
        green = (accent.green * 0.22f + MaterialTheme.colorScheme.surface.green * 0.78f).coerceIn(0f, 1f),
        blue = (accent.blue * 0.22f + MaterialTheme.colorScheme.surface.blue * 0.78f).coerceIn(0f, 1f),
        alpha = 1f
    )
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(dateLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (showEmoji) {
                            Text(shift?.emoji ?: "—", fontSize = 28.sp)
                            Spacer(Modifier.width(10.dp))
                        }
                        Text(
                            shift?.displayName ?: "Нет смены",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (shift != null) accent else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                // Navigation to today is kept in the month toolbar to avoid duplicate controls.

            }
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(secondaryLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                if (shift != null) {
                    Icon(Icons.Filled.ArrowForward, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun ShiftStatPill(shift: ShiftType, value: String, showEmoji: Boolean, modifier: Modifier = Modifier, label: String = shift.displayName) {
    Surface(modifier = modifier, shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            if (showEmoji) Text(shift.emoji, fontSize = 16.sp)
            else Box(Modifier.size(10.dp).clip(CircleShape).background(shift.color))
            Spacer(Modifier.width(7.dp))
            Column {
                Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold)
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}

@Composable
fun ShiftLegend(showEmoji: Boolean = true, modifier: Modifier = Modifier) {
    SurfaceCard(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text("Обозначения", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            // Two columns are more stable on narrow phones than the old three-column
            // grid, where long labels could overlap or become clipped.
            val types = ShiftType.values().toList()
            types.chunked(2).forEachIndexed { index, row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { type -> LegendItem(type, showEmoji, Modifier.weight(1f)) }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
                if (index < (types.size + 1) / 2 - 1) Spacer(Modifier.height(9.dp))
            }
        }
    }
}

@Composable
private fun LegendItem(type: ShiftType, showEmoji: Boolean, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        if (showEmoji) {
            Text(type.emoji, fontSize = 14.sp)
        } else {
            Box(Modifier.size(10.dp).clip(CircleShape).background(type.color))
        }
        Text(
            type.displayName,
            Modifier.padding(start = 7.dp),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun ColorDot(color: Color, selected: Boolean, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(38.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
        if (selected) {
            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color.White))
        }
    }
}

@Composable
fun EmptyState(title: String, description: String, action: String, onAction: () -> Unit, modifier: Modifier = Modifier) {
    SurfaceCard(modifier) {
        Column(modifier = Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)) {
                Icon(Icons.Filled.LightMode, null, modifier = Modifier.padding(16.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(14.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(18.dp))
            androidx.compose.material3.Button(onClick = onAction, shape = RoundedCornerShape(16.dp)) { Text(action) }
        }
    }
}

@Composable
fun CompactCloseButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) { Icon(Icons.Filled.Close, null) }
}
