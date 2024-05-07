package com.grid.pos.ui.pos.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.theme.GridPOSTheme

@Composable
fun InvoiceItemCell(modifier: Modifier = Modifier, invoiceItemModel: InvoiceItemModel,
                    isHeader: Boolean = false, isLandscape: Boolean = false, index: Int,
                    onEdit: (Int) -> Unit = {}, onRemove: (Int) -> Unit = {}) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap  = {
                    onEdit.invoke(index)
                },  onLongPress   = {
                    onEdit.invoke(index)
                })
            }, horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val modifier = if (isLandscape) {
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .wrapContentHeight(
                    align = Alignment.CenterVertically
                )
        } else {
            Modifier
                .fillMaxHeight()
                .width(100.dp)
                .wrapContentHeight(
                    align = Alignment.CenterVertically
                )
        }

        val dividerModifier = if (isLandscape) {
            Modifier
                .weight(.1f)
                .fillMaxHeight()
        } else {
            Modifier
                .fillMaxHeight()
                .width(1.dp)
        }
        val actionsModifier = if (isLandscape) {
            Modifier
                .weight(.7f)
                .fillMaxHeight()
                .wrapContentHeight(
                    align = Alignment.CenterVertically
                )
        } else {
            Modifier
                .fillMaxHeight()
                .width(70.dp)
                .wrapContentHeight(
                    align = Alignment.CenterVertically
                )
        }
        val textStyle = TextStyle(
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal, fontSize = 16.sp
        )
        Text(
            text = if (isHeader) "Item" else invoiceItemModel.getName(),
            modifier = if (isLandscape) {
                Modifier
                    .weight(1.8f)
                    .fillMaxHeight()
                    .wrapContentHeight(
                        align = Alignment.CenterVertically
                    )
            } else {
                Modifier
                    .fillMaxHeight()
                    .width(180.dp)
                    .wrapContentHeight(
                        align = Alignment.CenterVertically
                    )
            }, textAlign = TextAlign.Center, style = textStyle, color = SettingsModel.textColor
        )
        VerticalDivider(
            color = Color.Black, modifier = dividerModifier
        )
        Text(
            text = if (isHeader) "Count" else invoiceItemModel.getQuantity().toString(),
            modifier = modifier, textAlign = TextAlign.Center, style = textStyle,
            color = SettingsModel.textColor
        )
        VerticalDivider(
            color = Color.Black, modifier = dividerModifier
        )
        Text(
            text = if (isHeader) "Price" else invoiceItemModel.getPrice().toString(),
            modifier = modifier, textAlign = TextAlign.Center, style = textStyle,
            color = SettingsModel.textColor
        )
        VerticalDivider(
            color = Color.Black, modifier = dividerModifier
        )
        Text(
            text = if (isHeader) "Dis%" else invoiceItemModel.getDiscount().toString(),
            modifier = modifier, textAlign = TextAlign.Center, style = textStyle,
            color = SettingsModel.textColor
        )
        if (!SettingsModel.hideTaxInputs) {
            VerticalDivider(
                color = Color.Black, modifier = dividerModifier
            )
            Text(
                text = if (isHeader) "Tax" else invoiceItemModel.getTax().toString(),
                modifier = modifier, textAlign = TextAlign.Center, style = textStyle,
                color = SettingsModel.textColor
            )
            VerticalDivider(
                color = Color.Black, modifier = dividerModifier
            )
            Text(
                text = if (isHeader) "Tax1" else invoiceItemModel.getTax1().toString(),
                modifier = modifier, textAlign = TextAlign.Center, style = textStyle,
                color = SettingsModel.textColor
            )
            VerticalDivider(
                color = Color.Black, modifier = dividerModifier
            )
            Text(
                text = if (isHeader) "Tax2" else invoiceItemModel.getTax2().toString(),
                modifier = modifier, textAlign = TextAlign.Center, style = textStyle,
                color = SettingsModel.textColor
            )
        }
        VerticalDivider(
            modifier = dividerModifier, color = Color.Black
        )
        Text(
            text = if (isHeader) "Amount" else invoiceItemModel.getNetAmount().toString(),
            modifier = modifier, textAlign = TextAlign.Center, style = textStyle,
            color = SettingsModel.textColor
        )
        VerticalDivider(
            modifier = dividerModifier, color = Color.Black
        )
        if (isHeader) {
            Text(
                text = "Actions", modifier = actionsModifier, textAlign = TextAlign.Center,
                style = textStyle, color = SettingsModel.textColor
            )
        } else {
            IconButton(modifier = actionsModifier, onClick = { onRemove.invoke(index) }) {
                Icon(
                    imageVector = Icons.Default.RemoveCircle, contentDescription = "Delete",
                    tint = SettingsModel.buttonColor
                )
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun InvoiceItemCellPreview() {
    GridPOSTheme {
        InvoiceItemCell(invoiceItemModel = InvoiceItemModel(), index = 0, isLandscape = true)
    }
}