package com.grid.pos.ui.pos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.ui.common.SearchableDropdownMenu
import com.grid.pos.ui.theme.Blue
import com.grid.pos.utils.Utils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InvoiceBodyDetails(
    navController: NavController? = null,
    modifier: Modifier = Modifier
) {
    val header = InvoiceItemModel("Item", "Count", "Price", "Dis%", "Tax", "Tax1", "Tax2", "Amount")
    val invoiceItems = listOf(
        InvoiceItemModel("Chicken", "1", "150.00", "0.0", "0.0", "0.0", "0.0", "150.00"),
        InvoiceItemModel("Salad", "1", "150.00", "0.0", "0.0", "0.0", "0.0", "150.00"),
        InvoiceItemModel("Champo", "1", "150.00", "0.0", "0.0", "0.0", "0.0", "150.00"),
        InvoiceItemModel("Prince", "1", "150.00", "0.0", "0.0", "0.0", "0.0", "150.00"),
        InvoiceItemModel("Juice", "1", "150.00", "0.0", "0.0", "0.0", "0.0", "150.00"),
        InvoiceItemModel("Master chips", "1", "150.00", "0.0", "0.0", "0.0", "0.0", "150.00"),
        InvoiceItemModel("Mozarilla", "1", "150.00", "0.0", "0.0", "0.0", "0.0", "150.00"),
        InvoiceItemModel("Meat", "1", "150.00", "0.0", "0.0", "0.0", "0.0", "150.00"),
        InvoiceItemModel("Kabab", "1", "150.00", "0.0", "0.0", "0.0", "0.0", "150.00"),
    )

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(0.dp)
    ) {
        stickyHeader {
            InvoiceItem(
                modifier = modifier.height(50.dp).background(color = Color.LightGray),
                item = header,
                isHeader = true
            )
        }
        invoiceItems.forEachIndexed { index, invoiceItemModel ->
            item {
                val color = if (index % 2 == 0) Color.White else Color.LightGray
                InvoiceItem(
                    modifier = modifier.height(40.dp).background(color = color),
                    item = invoiceItemModel
                )
            }
        }
    }
}
