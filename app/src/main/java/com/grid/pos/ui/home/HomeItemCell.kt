package com.grid.pos.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grid.pos.model.HomeItemModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.theme.homeLightBlue
import com.grid.pos.ui.theme.homeLightGreen
import com.grid.pos.ui.theme.homeLightPurple

@Composable
fun HomeItemCell(
        modifier: Modifier = Modifier,
        homeItemModel: HomeItemModel,
        onClick:(String) ->Unit
) {
    Button(modifier = modifier
        .width(120.dp)
        .wrapContentHeight()
        .padding(
            horizontal = 3.dp,
            vertical = 5.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
        ),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(15.dp),
        onClick = {
            onClick.invoke(homeItemModel.composable)
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
                RoundedCornerShape(15.dp)
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Image(
                modifier = Modifier.size(60.dp),
                painter = painterResource(homeItemModel.icon),
                contentDescription = "icon"
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = homeItemModel.title,
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