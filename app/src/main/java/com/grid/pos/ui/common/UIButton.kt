package com.grid.pos.ui.common

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.grid.pos.ui.theme.Blue
import com.grid.pos.ui.theme.White

@Composable
fun UIButton(
    modifier: Modifier = Modifier,
    text: String = "Submit",
    shape: Shape = ButtonDefaults.elevatedShape,
    buttonColor: Color = Blue,
    textColor: Color = White,
    textAlign: TextAlign = TextAlign.Center,
    textDecoration: TextDecoration = TextDecoration.None,
    fontWeight: FontWeight = FontWeight.Bold,
    fontSize: TextUnit = 16.sp,
    onClick: () -> Unit = {},
) {
    ElevatedButton(
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
        shape = shape,
        onClick = {
            onClick.invoke()
        }
    ) {
        Text(
            text = text,
            textAlign = textAlign,
            style = TextStyle(
                textDecoration = textDecoration,
                fontWeight = fontWeight,
                fontSize = fontSize
            ),
            color = textColor
        )
    }
}