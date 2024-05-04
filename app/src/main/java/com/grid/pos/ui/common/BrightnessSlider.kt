package com.grid.pos.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BrightnessSlider(
    progress: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Blue,
    thumbColor: Color = Color.LightGray,
    thumbSize:Dp=20.dp
) {
    Box(
        modifier = modifier.pointerInput(Unit) { // Empty lambda for initial setup
            detectDragGestures { change, dragAmount ->
                val newProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                onValueChange(newProgress)
            }
        }.
        background(color = backgroundColor, shape = RoundedCornerShape(10.dp)),
    ){
        Canvas(modifier = Modifier.fillMaxSize()) {
            val trackWidth = size.width
            val trackHeight = 5.dp.toPx()
            drawRect(
                color = backgroundColor,
                topLeft = Offset(0f, size.height / 2f - trackHeight / 2f),
                size = Size(trackWidth, trackHeight)
            )
            val thumbSize = thumbSize.toPx()
            val thumbPosition = (trackWidth * progress) - thumbSize / 2f
            drawCircle(
                color = thumbColor,
                center = Offset(thumbPosition + thumbSize / 2f, size.height / 2f),
                radius = thumbSize / 2f
            )
        }
    }
}