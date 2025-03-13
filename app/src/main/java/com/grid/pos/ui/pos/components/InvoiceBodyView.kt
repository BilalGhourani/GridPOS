package com.grid.pos.ui.pos.components

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
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.ui.theme.GridPOSTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InvoiceBodyDetails(
    invoices: MutableList<InvoiceItemModel> = mutableListOf(),
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
            InvoiceItemCell(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(color = Color.LightGray),
                invoiceItemModel = InvoiceItemModel(),
                isHeader = true,
                isLandscape = isLandscape,
                index = 0
            )
        }
        invoices.toMutableList().forEachIndexed { index, invoiceItemModel ->
            item {
                val color = if (index % 2 == 0) Color.White else Color.LightGray
                InvoiceItemCell(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(color = color),
                    invoiceItemModel = invoiceItemModel,
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
        InvoiceBodyDetails(
            invoices = mutableListOf(
                InvoiceItemModel(),
                InvoiceItemModel(),
                InvoiceItemModel(),
                InvoiceItemModel(),
                InvoiceItemModel()
            ), isLandscape = true
        )
    }
}
