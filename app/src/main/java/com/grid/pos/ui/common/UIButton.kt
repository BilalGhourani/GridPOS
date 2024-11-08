package com.grid.pos.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.theme.homeLightBlue
import com.grid.pos.ui.theme.homeLightGreen
import com.grid.pos.ui.theme.homeLightPurple

@Composable
fun UIButton(
        modifier: Modifier = Modifier,
        text: String = "Submit",
        shape: Shape = RoundedCornerShape(15.dp),
        textColor: Color = SettingsModel.buttonTextColor,
        textAlign: TextAlign = TextAlign.Center,
        textDecoration: TextDecoration = TextDecoration.None,
        fontWeight: FontWeight = FontWeight.Bold,
        fontSize: TextUnit = 16.sp,
        enabled: Boolean = true,
        onClick: () -> Unit = {},
) {
    OutlinedButton(modifier = modifier.alpha(if (enabled) 1f else .5f),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        shape = shape,
        enabled = enabled,
        border = BorderStroke(
            0.5.dp,
            Brush.linearGradient(
                colors = listOf(
                    homeLightGreen,
                    homeLightPurple,
                    homeLightBlue
                )
            )
        ),
        contentPadding = PaddingValues(0.dp),
        onClick = {
            onClick.invoke()
        }) {
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