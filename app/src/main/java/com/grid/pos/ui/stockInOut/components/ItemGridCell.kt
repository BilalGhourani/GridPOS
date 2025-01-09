package com.grid.pos.ui.stockInOut.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.grid.pos.model.StockAdjItemModel
import com.grid.pos.ui.theme.GridPOSTheme

@Composable
fun ItemGridCell(
    modifier: Modifier = Modifier,
    stockAdjItemModel: StockAdjItemModel,
    isHeader: Boolean = false,
    isLandscape: Boolean = false,
    index: Int,
    onEdit: (Int) -> Unit = {},
    onRemove: (Int) -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = {
                    onEdit.invoke(index)
                }, onLongPress = {
                    onEdit.invoke(index)
                })
            },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = if (isHeader) "Barcode" else stockAdjItemModel.stockAdjItem.itemBarcode ?: "~",
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
            },
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                fontSize = 16.sp
            ),
            color = SettingsModel.textColor
        )
        VerticalDivider(
            color = Color.Black,
            modifier = if (isLandscape) {
                Modifier
                    .weight(.1f)
                    .fillMaxHeight()
            } else {
                Modifier
                    .fillMaxHeight()
                    .width(1.dp)
            }
        )
        Text(
            text = if (isHeader) "Name" else stockAdjItemModel.getName(),
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
            },
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                fontSize = 16.sp
            ),
            color = SettingsModel.textColor
        )
        VerticalDivider(
            color = Color.Black,
            modifier = if (isLandscape) {
                Modifier
                    .weight(.1f)
                    .fillMaxHeight()
            } else {
                Modifier
                    .fillMaxHeight()
                    .width(1.dp)
            }
        )
        Text(
            text = if (isHeader) "Qty" else stockAdjItemModel.stockAdjustment.stockAdjQty.toString(),
            modifier = if (isLandscape) {
                Modifier
                    .weight(.8f)
                    .fillMaxHeight()
                    .wrapContentHeight(
                        align = Alignment.CenterVertically
                    )
            } else {
                Modifier
                    .fillMaxHeight()
                    .width(80.dp)
                    .wrapContentHeight(
                        align = Alignment.CenterVertically
                    )
            },
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                fontSize = 16.sp
            ),
            color = SettingsModel.textColor
        )
        VerticalDivider(
            color = Color.Black,
            modifier = if (isLandscape) {
                Modifier
                    .weight(.1f)
                    .fillMaxHeight()
            } else {
                Modifier
                    .fillMaxHeight()
                    .width(1.dp)
            }
        )
        Row(
            modifier = if (isLandscape) {
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
            },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isHeader) {
                Text(
                    text = "Actions",
                    modifier = modifier
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .padding(5.dp),
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 16.sp
                    ),
                    color = SettingsModel.textColor
                )
            } else {
                IconButton(modifier = Modifier.padding(start = 5.dp),
                    onClick = { onEdit.invoke(index) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = SettingsModel.buttonColor
                    )
                }
                IconButton(modifier = Modifier.padding(horizontal = 5.dp),
                    onClick = { onRemove.invoke(index) }) {
                    Icon(
                        imageVector = Icons.Default.RemoveCircle,
                        contentDescription = "Delete",
                        tint = SettingsModel.buttonColor
                    )
                }
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun InvoiceItemCellPreview() {
    GridPOSTheme {
        ItemGridCell(
            stockAdjItemModel = StockAdjItemModel(),
            index = 0,
            isLandscape = true
        )
    }
}