package com.example.ironvault

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt

@Composable
fun PatternLock(
    size: Int = 3,
    onPatternComplete: (List<Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    val dotSize = 20.dp
    val spacing = 60.dp
    
    val selectedPoints = remember { mutableStateListOf<Int>() }
    val currentLineEnd = remember { mutableStateOf<Offset?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    
    val density = LocalDensity.current
    val dotSizePx = with(density) { dotSize.toPx() }
    val spacingPx = with(density) { spacing.toPx() }
    
    val totalSize = (size - 1) * spacingPx + dotSizePx * 2
    
    fun getPointPosition(index: Int): Offset {
        val row = index / size
        val col = index % size
        return Offset(
            x = col * spacingPx + dotSizePx,
            y = row * spacingPx + dotSizePx
        )
    }
    
    fun getPointAt(offset: Offset): Int? {
        for (i in 0 until size * size) {
            val point = getPointPosition(i)
            val distance = sqrt((offset.x - point.x) * (offset.x - point.x) + (offset.y - point.y) * (offset.y - point.y))
            if (distance < dotSizePx * 1.5f) {
                return i
            }
        }
        return null
    }
    
    Box(
        modifier = modifier
            .size(with(density) { totalSize.toDp() })
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        selectedPoints.clear()
                        getPointAt(offset)?.let { 
                            if (!selectedPoints.contains(it)) selectedPoints.add(it)
                        }
                    },
                    onDrag = { change, _ ->
                        currentLineEnd.value = change.position
                        getPointAt(change.position)?.let { point ->
                            if (!selectedPoints.contains(point)) {
                                selectedPoints.add(point)
                            }
                        }
                    },
                    onDragEnd = {
                        isDragging = false
                        currentLineEnd.value = null
                        if (selectedPoints.size >= 4) {
                            onPatternComplete(selectedPoints.toList())
                        }
                        selectedPoints.clear()
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Рисуем линии между выбранными точками
            for (i in 0 until selectedPoints.size - 1) {
                val start = getPointPosition(selectedPoints[i])
                val end = getPointPosition(selectedPoints[i + 1])
                drawLine(
                    color = Color(0xFF00D9FF),
                    start = start,
                    end = end,
                    strokeWidth = 8f,
                    cap = StrokeCap.Round
                )
            }
            
            // Линия к текущей позиции пальца
            if (isDragging && selectedPoints.isNotEmpty() && currentLineEnd.value != null) {
                val lastPoint = getPointPosition(selectedPoints.last())
                drawLine(
                    color = Color(0xFF00D9FF).copy(alpha = 0.5f),
                    start = lastPoint,
                    end = currentLineEnd.value!!,
                    strokeWidth = 8f,
                    cap = StrokeCap.Round
                )
            }
            
            // Рисуем точки
            for (i in 0 until size * size) {
                val position = getPointPosition(i)
                val isSelected = selectedPoints.contains(i)
                
                // Внешний круг (если выбрана)
                if (isSelected) {
                    drawCircle(
                        color = Color(0xFF00D9FF).copy(alpha = 0.2f),
                        radius = dotSizePx * 1.5f,
                        center = position
                    )
                }
                
                // Внутренний круг
                drawCircle(
                    color = if (isSelected) Color(0xFF00D9FF) else Color(0xFF3A3F5C),
                    radius = dotSizePx,
                    center = position
                )
            }
        }
    }
}
