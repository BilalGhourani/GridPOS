package com.grid.pos.ui.purchase.components

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
import com.grid.pos.model.PurchaseItemModel
import com.grid.pos.ui.theme.GridPOSTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PurchaseDataGrid(
    purchaseItems: MutableList<PurchaseItemModel> = mutableListOf(),
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false,
    onEditQty: (Int, Double) -> Unit = { _, _ -> },
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
            PurchaseGridCell(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(color = Color.LightGray),
                purchaseItemModel = PurchaseItemModel(),
                isHeader = true,
                isLandscape = isLandscape,
                index = 0
            )
        }
        purchaseItems.toMutableList().forEachIndexed { index, purchaseItem ->
            item {
                val color = if (index % 2 == 0) Color.White else Color.LightGray
                PurchaseGridCell(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(color = color),
                    purchaseItemModel = purchaseItem,
                    isLandscape = isLandscape,
                    index = index,
                    onEditQty = { index, qty -> onEditQty.invoke(index, qty) },
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
        PurchaseDataGrid(
            purchaseItems = mutableListOf(
                PurchaseItemModel(),
                PurchaseItemModel(),
                PurchaseItemModel()
            ), isLandscape = true
        )
    }
}
