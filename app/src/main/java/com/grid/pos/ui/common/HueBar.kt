package com.grid.pos.ui.common

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toRect
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Extension.emitDragGesture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun HueBar(
        modifier: Modifier = Modifier,
        defaultColor: Color = Color.Red,
        onChange: (Float) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val interactionSource = remember {
        MutableInteractionSource()
    }

    val defaultHue = remember(defaultColor) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(
            defaultColor.toArgb(),
            hsv
        )
        hsv[0] // Hue value
    }

    val mySize = remember { mutableStateOf(IntSize.Zero) }
    val pressOffset = remember {
        mutableStateOf(
            Offset(0.2f, 0.0f)) }
    LaunchedEffect(key1 = mySize) {
        pressOffset.value = Offset(defaultHue / 360f * mySize.value.width, 0f)
    }


    Canvas(modifier = modifier
        .clip(RoundedCornerShape(50))
        .emitDragGesture(interactionSource)
        .onGloballyPositioned { coordinates ->
            // Get the size of the composable in pixels
            mySize.value = coordinates.size
        }) {
        val drawScopeSize = size
        val bitmap = Bitmap.createBitmap(
            size.width.toInt(),
            size.height.toInt(),
            Bitmap.Config.ARGB_8888
        )
        val hueCanvas = android.graphics.Canvas(bitmap)
        val huePanel = RectF(
            0f,
            0f,
            bitmap.width.toFloat(),
            bitmap.height.toFloat()
        )
        val hueColors = IntArray((huePanel.width()).toInt())
        var hue = 0f
        for (i in hueColors.indices) {
            hueColors[i] = android.graphics.Color.HSVToColor(
                floatArrayOf(
                    hue,
                    1f,
                    1f
                )
            )
            hue += 360f / hueColors.size
        }
        val linePaint = Paint()
        linePaint.strokeWidth = 0F
        for (i in hueColors.indices) {
            linePaint.color = hueColors[i]
            hueCanvas.drawLine(
                i.toFloat(),
                0F,
                i.toFloat(),
                huePanel.bottom,
                linePaint
            )
        }
        drawBitmap(
            bitmap = bitmap,
            panel = huePanel
        )
        fun pointToHue(pointX: Float): Float {
            val width = huePanel.width()
            val x = when {
                pointX < huePanel.left -> 0F
                pointX > huePanel.right -> width
                else -> pointX - huePanel.left
            }
            return x * 360f / width
        }

        scope.collectForPress(interactionSource) { pressPosition ->
            val pressPos = pressPosition.x.coerceIn(0f..drawScopeSize.width)
            pressOffset.value = Offset(
                pressPos,
                0f
            )
            val selectedHue = pointToHue(pressPos)
            onChange(selectedHue)
        }

        drawCircle(
            Color.White,
            radius = size.height / 2,
            center = Offset(
                pressOffset.value.x,
                size.height / 2
            ),
            style = Stroke(
                width = 2.dp.toPx()
            )
        )
    }
}

fun CoroutineScope.collectForPress(
        interactionSource: InteractionSource,
        setOffset: (Offset) -> Unit
) {
    launch {
        interactionSource.interactions.collect { interaction ->
            (interaction as? PressInteraction.Press)?.pressPosition?.let(setOffset)
        }
    }
}

private fun DrawScope.drawBitmap(
        bitmap: Bitmap,
        panel: RectF
) {
    drawIntoCanvas {
        it.nativeCanvas.drawBitmap(
            bitmap,
            null,
            panel.toRect(),
            null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HueBarPreview() {
    GridPOSTheme {
        HueBar()
    }
}