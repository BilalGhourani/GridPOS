package com.grid.pos.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.ui.theme.White
import com.grid.pos.utils.Utils

@Composable
fun ColorPickerPopup(
    modifier: Modifier = Modifier,
    defaultColor: Color = Color.Blue,
    onDismiss: () -> Unit = {},
    onSubmit: (Color) -> Unit = {},
) {
    val hex = listOf(defaultColor.red, defaultColor.green, defaultColor.blue)
    var hueState by remember { mutableStateOf(defaultColor.value.toFloat()) }
    var saturation by remember { mutableStateOf(1 - (hex.min() / hex.max())) }
    var brightness by remember { mutableStateOf(hex.sum() / (3 * 255) * 100) }
    var buttonColorState by remember { mutableStateOf(defaultColor) }
    val configuration = LocalConfiguration.current
    Box(
        modifier = modifier
            .width((configuration.screenWidthDp * 0.8).dp)
            .height((configuration.screenHeightDp * 0.35).dp)
            .background(color = White, shape = RoundedCornerShape(15.dp)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Color Picker",
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                style = TextStyle(
                    textDecoration = TextDecoration.None,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = SettingsModel.textColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(25.dp))
            HueBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                defaultColor = defaultColor
            ) {
                hueState = it
                buttonColorState = Utils.floatToColor(
                    hue = hueState,
                    saturation = saturation,
                    brightness = brightness
                )
            }
            Spacer(modifier = Modifier.height(25.dp))

            // saturation slider
            BrightnessSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(15.dp),
                progress = saturation,
                backgroundColor = buttonColorState,
                thumbSize = 25.dp,
                /*thumbColor = buttonColorState,*/
                onValueChange = { newValue ->
                    saturation = newValue
                    buttonColorState = Utils.floatToColor(
                        hue = hueState,
                        saturation = saturation,
                        brightness = brightness
                    )
                })
            Spacer(modifier = Modifier.height(25.dp))
            // Brightness slider
            BrightnessSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(15.dp),
                progress = brightness,
                backgroundColor = buttonColorState,
                thumbSize = 25.dp,
                /*thumbColor = buttonColorState,*/
                onValueChange = { newValue ->
                    brightness = newValue
                    buttonColorState = Utils.floatToColor(
                        hue = hueState,
                        saturation = saturation,
                        brightness = brightness
                    )
                }
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { onDismiss.invoke() }) {
                    Text("Dismiss", color = SettingsModel.textColor)
                }

                TextButton(onClick = {
                    onSubmit.invoke(buttonColorState)
                }) {
                    Text("Submit", color = Color.Black)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ColorPickerPopupPreview() {
    GridPOSTheme {
        ColorPickerPopup()
    }
}