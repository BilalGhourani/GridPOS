package com.grid.pos.ui.pos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grid.pos.model.InvoiceItemModel

@Composable
fun InvoiceItemCell(
    modifier: Modifier = Modifier,
    invoiceItemModel: InvoiceItemModel,
    isHeader: Boolean = false,
    isLandscape: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val modifier = if (isLandscape) {
            Modifier
                .fillMaxHeight()
                .weight(1f)
                .wrapContentHeight(align = Alignment.CenterVertically)
        } else {

            Modifier
                .fillMaxHeight()
                .width(100.dp)
                .wrapContentHeight(align = Alignment.CenterVertically)
        }

        val dividerModifier = if (isLandscape) {
            Modifier
                .fillMaxHeight()
                .weight(.01f)
        } else {
            Modifier
                .fillMaxHeight()
                .width(1.dp)
        }
        val textStyle = TextStyle(
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp
        )
        Text(
            text = if (isHeader) "Item" else invoiceItemModel.getName(),
            modifier = if (isLandscape) {
                Modifier
                    .fillMaxHeight()
                    .weight(1.8f)
                    .wrapContentHeight(align = Alignment.CenterVertically)
            } else {
                Modifier
                    .fillMaxHeight()
                    .width(180.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically)
            },
            textAlign = TextAlign.Center,
            style = textStyle
        )
        Divider(
            color = Color.Black,
            modifier = dividerModifier
        )
        Text(
            text = if (isHeader) "Count" else invoiceItemModel.getQuantity().toString(),
            modifier = modifier,
            textAlign = TextAlign.Center,
            style = textStyle
        )
        Divider(
            color = Color.Black,
            modifier = dividerModifier
        )
        Text(
            text = if (isHeader) "Price" else invoiceItemModel.getPrice().toString(),
            modifier = modifier,
            textAlign = TextAlign.Center,
            style = textStyle
        )
        Divider(
            color = Color.Black,
            modifier = dividerModifier
        )
        Text(
            text = if (isHeader) "Dis%" else invoiceItemModel.getDiscount().toString(),
            modifier = modifier,
            textAlign = TextAlign.Center,
            style = textStyle
        )
        Divider(
            color = Color.Black,
            modifier = dividerModifier
        )
        Text(
            text = if (isHeader) "Tax" else invoiceItemModel.getTax().toString(),
            modifier = modifier,
            textAlign = TextAlign.Center,
            style = textStyle
        )
        Divider(
            color = Color.Black,
            modifier = dividerModifier
        )
        Text(
            text = if (isHeader) "Tax1" else invoiceItemModel.getTax1().toString(),
            modifier = modifier,
            textAlign = TextAlign.Center,
            style = textStyle
        )
        Divider(
            color = Color.Black,
            modifier = dividerModifier
        )
        Text(
            text = if (isHeader) "Tax2" else invoiceItemModel.getTax2().toString(),
            modifier = modifier,
            textAlign = TextAlign.Center,
            style = textStyle
        )
        Divider(
            color = Color.Black,
            modifier = dividerModifier
        )
        Text(
            text = if (isHeader) "Amount" else invoiceItemModel.getAmount().toString(),
            modifier = modifier,
            textAlign = TextAlign.Center,
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