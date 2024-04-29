package com.grid.pos.ui.pos.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.grid.pos.data.Family.Family
import com.grid.pos.data.Invoice.Invoice
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.utils.Utils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InvoiceBodyDetails(
    invoices: MutableList<InvoiceItemModel> = mutableListOf(),
    modifier: Modifier = Modifier
) {
    val header = InvoiceItemModel("Item", "Count", "Price", "Dis%", "Tax", "Tax1", "Tax2", "Amount")
/*    val invoiceItems = listOf(
        InvoiceItemModel("Chicken", "1", "150.00", "0.0", "0.0", "0.0", "0.0", "150.00"),
        InvoiceItemModel("Salad", "1", "150.00", "0.0", "0.0", "0.0", "0.0", "150.00"),
        InvoiceItemModel("Champo", "1", "150.00", "0.0", "0.0", "0.0", "0.0", "150.00"),
        InvoiceItemModel("Prince", "1", "150.00", "0.0", "0.0", "0.0", "0.0", "150.00"),
        InvoiceItemModel("Juice", "1", "150.00", "0.0", "0.0", "0.0", "0.0", "150.00"),
        InvoiceItemModel("Master chips", "1", "150.00", "0.0", "0.0", "0.0", "0.0", "150.00"),
        InvoiceItemModel("Mozarilla", "1", "150.00", "0.0", "0.0", "0.0", "0.0", "150.00"),
        InvoiceItemModel("Meat", "1", "150.00", "0.0", "0.0", "0.0", "0.0", "150.00"),
        InvoiceItemModel("Kabab", "1", "150.00", "0.0", "0.0", "0.0", "0.0", "150.00"),
    )*/

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(0.dp)
    ) {
        stickyHeader {
            InvoiceItemCell(
                modifier = Modifier
                    .height(50.dp)
                    .background(color = Color.LightGray),
                item = header,
                isHeader = true
            )
        }
        invoices.forEachIndexed { index, invoiceItemModel ->
            item {
                val color = if (index % 2 == 0) Color.White else Color.LightGray
                InvoiceItemCell(
                    modifier = Modifier
                        .height(40.dp)
                        .background(color = color),
                    item = invoiceItemModel
                )
            }
        }
    }
}
