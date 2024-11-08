package com.grid.pos.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
        textColor: Color = SettingsModel.textColor,
        icon: Int = R.drawable.login,
        iconSize: Dp = 50.dp,
        shape: Shape = RoundedCornerShape(15.dp),
        isVertical: Boolean = true,
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
        if (isVertical) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        BorderStroke(
                            0.5.dp,
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
                Spacer(modifier = Modifier.height(10.dp))
                Image(
                    modifier = Modifier.size(iconSize),
                    painter = painterResource(icon),
                    contentDescription = "icon"
                )
                Spacer(modifier = Modifier.height(10.dp))
                if (!text.isNullOrEmpty()) {
                    Text(
                        text = text,
                        style = TextStyle(
                            textDecoration = TextDecoration.None,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        } else {
            Row(
                modifier = Modifier.border(
                    BorderStroke(
                        0.5.dp,
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.width(10.dp))
                Image(
                    modifier = Modifier
                        .size(iconSize)
                        .padding(vertical = 10.dp),
                    painter = painterResource(icon),
                    contentDescription = "icon"
                )
                Spacer(modifier = Modifier.width(10.dp))
                if (!text.isNullOrEmpty()) {
                    Text(
                        text = text,
                        style = TextStyle(
                            textDecoration = TextDecoration.None,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                }
            }
        }

    }
}