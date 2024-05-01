package com.grid.pos.ui.pos.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grid.pos.data.Item.Item
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.SearchableDropdownMenu
import com.grid.pos.ui.theme.GridPOSTheme

@Composable
fun InvoiceFooterView(
    invoices: MutableList<InvoiceItemModel> = mutableListOf(),
    items: MutableList<Item> = mutableListOf(),
    thirdParties: MutableList<ThirdParty> = mutableListOf(),
    modifier: Modifier = Modifier,
    onItemSelected: (Item) -> Unit = {},
    onThirdPartySelected: (ThirdParty) -> Unit = {},
) {
    var curState by remember { mutableStateOf("USD") }
    var cur2State by remember { mutableStateOf("L.L.") }
    var taxState by remember { mutableStateOf("0.0") }
    var tax1State by remember { mutableStateOf("0.0") }
    var totalState by remember { mutableStateOf("0.0") }
    var totalCur2State by remember { mutableStateOf("0.0") }
    var tax2State by remember { mutableStateOf("0.0") }
    var totalTaxState by remember { mutableStateOf("0.0") }
    var tableNoState by remember { mutableStateOf("1") }
    var clientState by remember { mutableStateOf("Cash") }

    if (invoices.isNotEmpty()) {
        var tax = 0.0
        var tax1 = 0.0
        var tax2 = 0.0
        var total = 0.0
        invoices.forEach {
            tax += it.getTax()
            tax1 += it.getTax1()
            tax2 += it.getTax2()
            total += it.getAmount()
        }
        taxState = String.format("%.2f", tax)
        tax1State = String.format("%.2f", tax1)
        tax2State = String.format("%.2f", tax2)
        totalTaxState = String.format("%.2f", tax + tax1 + tax2)
        totalState = String.format("%.2f", total)
        totalCur2State = totalState
    } else {
        taxState = "0.0"
        tax1State = "0.0"
        tax2State = "0.0"
        totalTaxState = "0.0"
        totalState = "0.0"
        totalCur2State = totalState
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .wrapContentHeight()
                .padding(5.dp)
        ) {
            if (!SettingsModel.hideTaxInputs) {
                Row(
                    modifier = Modifier.wrapContentWidth(),
                    horizontalArrangement = Arrangement.Absolute.Left
                ) {
                    Text(text = "Tax:")
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = taxState)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = curState)
                }

                Row(
                    modifier = Modifier.wrapContentWidth(),
                    horizontalArrangement = Arrangement.Absolute.Left
                ) {
                    Text(text = "Tax1:")
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = tax1State)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = curState)
                }
            }
            Row(
                modifier = Modifier.wrapContentWidth(),
                horizontalArrangement = Arrangement.Absolute.Left
            ) {
                Text(text = "Total:")
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = totalState)
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = curState)
            }

            Row(
                modifier = Modifier.wrapContentWidth(),
                horizontalArrangement = Arrangement.Absolute.Left
            ) {
                Text(text = "Total:")
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = totalCur2State)
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = cur2State)
            }

            SearchableDropdownMenu(
                items = items.toMutableList(),
                modifier = Modifier
                    .padding(10.dp, 15.dp, 10.dp, 5.dp),
                label = "Search Items",
            ) { item ->
                onItemSelected.invoke(item as Item)
            }

        }
        Column(
            modifier = Modifier
                .weight(1f)
                .wrapContentHeight()
                .padding(5.dp)
        ) {
            if (!SettingsModel.hideTaxInputs) {
                Row(
                    modifier = Modifier.wrapContentWidth(),
                    horizontalArrangement = Arrangement.Absolute.Left
                ) {
                    Text(text = "Tax2:")
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = tax2State)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = curState)
                }

                Row(
                    modifier = Modifier.wrapContentWidth(),
                    horizontalArrangement = Arrangement.Absolute.Left
                ) {
                    Text(text = "Total Tax:")
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = totalTaxState)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = curState)
                }
            }
            Row(
                modifier = Modifier.wrapContentWidth(),
                horizontalArrangement = Arrangement.Absolute.Left
            ) {
                Text(text = "Table Number:")
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = tableNoState)
            }

            Row(
                modifier = Modifier.wrapContentWidth(),
                horizontalArrangement = Arrangement.Absolute.Left
            ) {
                Text(text = "Client:")
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = clientState)
            }

            SearchableDropdownMenu(
                items = thirdParties.toMutableList(),
                modifier = Modifier
                    .padding(10.dp, 15.dp, 10.dp, 5.dp),
                label = "Customer Search",
            ) { thirdParty ->
                onThirdPartySelected.invoke(thirdParty as ThirdParty)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InvoiceFooterViewPreview() {
    GridPOSTheme {
        InvoiceFooterView()
    }
}
