package com.grid.pos.ui.stockAdjustment.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.StockAdjItemModel
import com.grid.pos.ui.theme.GridPOSTheme

@Composable
fun StockAdjGridCell(
    modifier: Modifier = Modifier,
    stockAdjItem: StockAdjItemModel,
    isHeader: Boolean = false,
    isLandscape: Boolean = false,
    index: Int,
    onEditQty: (Int, Double) -> Unit = { _, _ -> },
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
            text = if (isHeader) "Barcode" else stockAdjItem.stockAdjItem.itemBarcode ?: "~",
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
            text = if (isHeader) "Name" else stockAdjItem.getName(),
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
        if (isHeader) {
            Text(
                text = "Qty",
                modifier = if (isLandscape) {
                    Modifier
                        .weight(1.6f)
                        .fillMaxHeight()
                        .wrapContentHeight(
                            align = Alignment.CenterVertically
                        )
                } else {
                    Modifier
                        .fillMaxHeight()
                        .width(160.dp)
                        .wrapContentHeight(
                            align = Alignment.CenterVertically
                        )
                },
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = SettingsModel.textColor
            )
        } else {
            TextField(
                value = stockAdjItem.stockAdjustment.stockAdjQty.toString(),
                onValueChange = {},
                modifier = if (isLandscape) {
                    Modifier
                        .fillMaxHeight()
                        .weight(1.6f)
                } else {
                    Modifier
                        .fillMaxHeight()
                        .width(160.dp)
                },
                readOnly = true,
                enabled = false,
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                leadingIcon = {
                    IconButton(onClick = {
                        val qty = stockAdjItem.stockAdjustment.stockAdjQty ?: 1.0
                        onEditQty.invoke(
                            index,
                            qty.plus(1.0)
                        )
                    }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Increase quantity",
                            tint = SettingsModel.buttonColor
                        )
                    }
                },
                trailingIcon = {
                    IconButton(onClick = {
                        val qty = stockAdjItem.stockAdjustment.stockAdjQty ?: 1.0
                        if (qty > 1) {
                            onEditQty.invoke(
                                index,
                                qty.minus(1.0)
                            )
                        }
                    }) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Decrease quantity",
                            tint = SettingsModel.buttonColor
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    focusedTextColor = SettingsModel.textColor,
                    unfocusedTextColor = SettingsModel.textColor,
                    disabledTextColor = SettingsModel.textColor,
                    cursorColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                )
            )
        }
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
                        fontWeight = FontWeight.Bold,
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
        StockAdjGridCell(
            stockAdjItem = StockAdjItemModel(),
            index = 0,
            isLandscape = true
        )
    }
}