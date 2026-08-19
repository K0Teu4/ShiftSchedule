package com.shiftschedule.app.ui.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput

class ReorderState {
    var draggingIndex by mutableStateOf<Int?>(null)
        private set
    var dragOffset by mutableStateOf(0f)
        private set

    var onMove: ((Int, Int) -> Unit)? = null

    fun startDrag(index: Int) {
        draggingIndex = index
        dragOffset = 0f
    }

    fun updateDrag(delta: Float, itemCount: Int, itemHeight: Float) {
        val currentIndex = draggingIndex ?: return
        dragOffset += delta
        val offsetItems = (dragOffset / itemHeight).toInt()
        if (offsetItems != 0) {
            val targetIndex = (currentIndex + offsetItems).coerceIn(0, itemCount - 1)
            if (targetIndex != currentIndex) {
                onMove?.invoke(currentIndex, targetIndex)
                draggingIndex = targetIndex
                dragOffset -= offsetItems * itemHeight
            }
        }
    }

    fun endDrag() {
        draggingIndex = null
        dragOffset = 0f
    }
}

@Composable
fun rememberReorderState(): ReorderState = remember { ReorderState() }

fun Modifier.dragContainer(
    state: ReorderState,
    index: Int,
    itemHeight: Float,
    itemCount: Int,
    enabled: Boolean = true
): Modifier = composed {
    if (!enabled) return@composed this
    this
        .pointerInput(index, itemCount) {
            detectDragGesturesAfterLongPress(
                onDragStart = { state.startDrag(index) },
                onDragEnd = { state.endDrag() },
                onDragCancel = { state.endDrag() }
            ) { change, dragAmount ->
                change.consume()
                state.updateDrag(dragAmount.y, itemCount, itemHeight)
            }
        }
        .graphicsLayer {
            val isDragging = state.draggingIndex == index
            translationY = if (isDragging) state.dragOffset else 0f
            scaleX = if (isDragging) 1.03f else 1f
            scaleY = if (isDragging) 1.03f else 1f
            alpha = if (isDragging) 0.9f else 1f
        }
}