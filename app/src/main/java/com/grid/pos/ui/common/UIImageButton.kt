package com.grid.pos.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grid.pos.R
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.theme.homeLightBlue
import com.grid.pos.ui.theme.homeLightGreen
import com.grid.pos.ui.theme.homeLightPurple

@Composable
fun UIImageButton(
        modifier: Modifier = Modifier,
        text: String? = null,
        icon: Int = R.drawable.login,
        iconSize: Dp = 60.dp,
        shape: Shape = RoundedCornerShape(15.dp),
        enabled: Boolean = true,
        onClick: () -> Unit = {},
) {
    Button(modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
        ),
        enabled = enabled,
        contentPadding = PaddingValues(0.dp),
        shape = shape,
        onClick = {
            onClick.invoke()
        }) {
        Column(
            modifier = Modifier.border(
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            homeLightGreen,
                            homeLightPurple,
                            homeLightBlue
                        )
                    )
                ),
                shape
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(5.dp))
            Image(
                modifier = Modifier.size(iconSize),
                painter = painterResource(icon),
                contentDescription = "icon"
            )
            if (!text.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = text,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    style = TextStyle(
                        textDecoration = TextDecoration.None,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = SettingsModel.textColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
    }
}