package com.grid.pos.ui.stockInOut.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grid.pos.model.StockAdjItemModel
import com.grid.pos.ui.theme.GridPOSTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun itemDataGrid(
    stockAdjItems: MutableList<StockAdjItemModel> = mutableListOf(),
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false,
    onEdit: (Int) -> Unit = {},
    onRemove: (Int) -> Unit = {}
) {

    LazyColumn(
        modifier = if (isLandscape) {
            modifier
                .fillMaxWidth()
        } else {
            modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        },
        contentPadding = PaddingValues(0.dp)
    ) {
        stickyHeader {
            ItemGridCell(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(color = Color.LightGray),
                stockAdjItemModel = StockAdjItemModel(),
                isHeader = true,
                isLandscape = isLandscape,
                index = 0
            )
        }
        stockAdjItems.toMutableList().forEachIndexed { index, stockAdjItemModel ->
            item {
                val color = if (index % 2 == 0) Color.White else Color.LightGray
                ItemGridCell(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(color = color),
                    stockAdjItemModel = stockAdjItemModel,
                    isLandscape = isLandscape,
                    index = index,
                    onEdit = { onEdit.invoke(it) },
                    onRemove = { onRemove.invoke(it) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InvoiceBodyDetailsCellPreview() {
    GridPOSTheme {
        itemDataGrid(
            stockAdjItems = mutableListOf(
                StockAdjItemModel(),
                StockAdjItemModel(),
                StockAdjItemModel()
            ), isLandscape = true
        )
    }
}