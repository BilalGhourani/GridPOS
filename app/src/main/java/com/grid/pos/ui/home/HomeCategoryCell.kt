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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import com.grid.pos.model.HomeCategoryModel
import com.grid.pos.model.HomeItemModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.theme.homeLightBlue
import com.grid.pos.ui.theme.homeLightGreen
import com.grid.pos.ui.theme.homeLightPurple

@Composable
fun HomeCategoryCell(
        modifier: Modifier = Modifier,
        homeCategoryModel: HomeCategoryModel,
        columnCount: Int,
        onClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.padding(start = 8.dp, top = 8.dp),
            text = homeCategoryModel.title,
            color = SettingsModel.textColor,
            style = TextStyle(
                textDecoration = TextDecoration.None,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            textAlign = TextAlign.Start
        )
        LazyVerticalGrid(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(
                    min = 130.dp,
                    max = 500.dp
                ),
            columns = GridCells.Fixed(columnCount)
        ) {
            homeCategoryModel.items.forEach { item ->
                item {
                    HomeItemCell(
                        modifier = Modifier,
                        homeItemModel = item,
                        onClick = onClick
                    )
                }
            }
        }
    }
}