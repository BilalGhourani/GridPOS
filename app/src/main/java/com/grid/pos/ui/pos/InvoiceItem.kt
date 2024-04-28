package com.grid.pos.ui.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grid.pos.model.InvoiceItemModel

@Composable
fun InvoiceItem(modifier: Modifier = Modifier, item: InvoiceItemModel, isHeader: Boolean = false) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val modifier = Modifier
            .fillMaxHeight()
            .weight(1f)
            .wrapContentHeight(align = Alignment.CenterVertically)
        val textStyle = TextStyle(
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp
        )
        Text(
            text = item.name,
            modifier = modifier,
            textAlign = TextAlign.Center,
            style = textStyle
        )
        Divider(
            color = Color.Black,
            modifier = Modifier
                .fillMaxHeight()
                .weight(.01f)
        )
        Text(
            text = item.count, modifier = modifier, textAlign = TextAlign.Center,
            style = textStyle
        )
        Divider(
            color = Color.Black,
            modifier = Modifier
                .fillMaxHeight()
                .weight(.01f)
        )
        Text(
            text = item.price, modifier = modifier, textAlign = TextAlign.Center,
            style = textStyle
        )
        Divider(
            color = Color.Black,
            modifier = Modifier
                .fillMaxHeight()
                .weight(.01f)
        )
        Text(
            text = item.discount, modifier = modifier, textAlign = TextAlign.Center,
            style = textStyle
        )
        Divider(
            color = Color.Black,
            modifier = Modifier
                .fillMaxHeight()
                .weight(.01f)
        )
        Text(
            text = item.tax, modifier = modifier, textAlign = TextAlign.Center,
            style = textStyle
        )
        Divider(
            color = Color.Black,
            modifier = Modifier
                .fillMaxHeight()
                .weight(.01f)
        )
        Text(
            text = item.tax1, modifier = modifier, textAlign = TextAlign.Center,
            style = textStyle
        )
        Divider(
            color = Color.Black,
            modifier = Modifier
                .fillMaxHeight()
                .weight(.01f)
        )
        Text(
            text = item.tax2, modifier = modifier, textAlign = TextAlign.Center,
            style = textStyle
        )
        Divider(
            color = Color.Black,
            modifier = Modifier
                .fillMaxHeight()
                .weight(.01f)
        )
        Text(
            text = item.amount, modifier = modifier, textAlign = TextAlign.Center,
            style = textStyle
        )
    }
}

@Composable
fun lineDivider(
    thickness: Dp = 1.dp,
    color: Color = Color.Black
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(thickness)
            .background(color)
    )
}