package com.shiftschedule.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp

@Composable
fun SkeletonBox(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )
    Box(
        modifier = modifier.background(
            MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.35f),
            RoundedCornerShape(8.dp)
        )
    )
}

@Composable
fun CalendarSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(36.dp))
        Spacer(modifier = Modifier.height(12.dp))
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(84.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(7) {
                SkeletonBox(modifier = Modifier.weight(1f).height(16.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        repeat(5) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(7) {
                    SkeletonBox(modifier = Modifier.weight(1f).aspectRatio(1f))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}