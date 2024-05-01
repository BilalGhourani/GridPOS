package com.grid.pos.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red

@Composable
fun UIColorPicker(
    modifier: Modifier = Modifier,
    defaultColor: Color = Color.Blue,
    onSelect: (Color) -> Unit = {}
) {
    val rgb = defaultColor.toArgb()
    // State variables for RGBA values
    val alpha = rememberSaveable { mutableFloatStateOf(rgb.alpha.toFloat() / 255) }
    val red = rememberSaveable { mutableFloatStateOf(rgb.red.toFloat() / 255) }
    val green = rememberSaveable { mutableFloatStateOf(rgb.green.toFloat() / 255) }
    val blue = rememberSaveable { mutableFloatStateOf(rgb.blue.toFloat() / 255) }

// Derived state for the color based on RGBA values
    val color by remember {
        derivedStateOf {
            Color(red.value, green.value, blue.value, alpha.value)
        }
    }

// UI layout using Scaffold and Column
    Column(modifier = modifier.padding(5.dp)) {
        // Display the current color in a Box with a MaterialTheme shape
        Row {
            Box(
                modifier = Modifier
                    .padding(10.dp, 0.dp)
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(color, shape = MaterialTheme.shapes.large)
            )
        }

        // Sliders for adjusting RGBA values
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            ColorSlider("A", alpha, color.copy(1f))
            ColorSlider("R", red, Color.Red)
            ColorSlider("G", green, Color.Green)
            ColorSlider("B", blue, Color.Blue)
            UIButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 10.dp),
                text = "Submit"
            ) {
                onSelect.invoke(color)
            }
        }
    }
}
