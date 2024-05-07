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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.Item.Item
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.SearchableDropdownMenu
import com.grid.pos.ui.theme.GridPOSTheme

@Composable
fun InvoiceFooterView(
    invoiceHeader: InvoiceHeader,
    currency: Currency,
    items: MutableList<Item> = mutableListOf(),
    thirdParties: MutableList<ThirdParty> = mutableListOf(),
    modifier: Modifier = Modifier,
    onItemSelected: (Item) -> Unit = {},
    onThirdPartySelected: (ThirdParty) -> Unit = {},
) {
    val curState by remember { mutableStateOf(currency.currencyCode1 ?: "") }
    val cur2State by remember { mutableStateOf(currency.currencyCode2 ?: "") }
    var taxState by remember { mutableStateOf("0.0") }
    var tax1State by remember { mutableStateOf("0.0") }
    var totalState by remember { mutableStateOf("0.0") }
    var totalCur2State by remember { mutableStateOf("0.0") }
    var tax2State by remember { mutableStateOf("0.0") }
    var totalTaxState by remember { mutableStateOf("0.0") }
    var tableNoState by remember { mutableStateOf("1") }
    var clientState by remember { mutableStateOf("Cash") }

    val tax = invoiceHeader.invoicHeadTaxAmt ?: 0.0
    val tax1 = invoiceHeader.invoicHeadTax1Amt ?: 0.0
    val tax2 = invoiceHeader.invoicHeadTax2Amt ?: 0.0
    val curr1Decimal = currency.currencyName1Dec ?: 2
    val curr2Decimal = currency.currencyName2Dec ?: 2
    taxState = String.format("%.${curr1Decimal}f", tax)
    tax1State = String.format("%.${curr1Decimal}f", tax1)
    tax2State = String.format("%.${curr1Decimal}f", tax2)
    totalTaxState = String.format("%.${curr1Decimal}f", tax + tax1 + tax2)
    totalState = String.format("%.${curr1Decimal}f", invoiceHeader.invoicHeadGrossmont ?: 0.0)
    totalCur2State = String.format("%.${curr2Decimal}f", totalState.toDouble().times(currency.currencyRate?.toDouble() ?: 0.0))

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
                    modifier = Modifier.wrapContentWidth(), horizontalArrangement = Arrangement.Absolute.Left
                ) {
                    Text(text = "Tax:", color = SettingsModel.textColor)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = taxState, color = SettingsModel.textColor)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = curState, color = SettingsModel.textColor)
                }

                Row(
                    modifier = Modifier.wrapContentWidth(), horizontalArrangement = Arrangement.Absolute.Left
                ) {
                    Text(text = "Tax1:", color = SettingsModel.textColor)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = tax1State, color = SettingsModel.textColor)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = curState, color = SettingsModel.textColor)
                }
            }
            Row(
                modifier = Modifier.wrapContentWidth(), horizontalArrangement = Arrangement.Absolute.Left
            ) {
                Text(text = "Total:", color = SettingsModel.textColor)
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = totalState, color = SettingsModel.textColor)
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = curState, color = SettingsModel.textColor)
            }

            Row(
                modifier = Modifier.wrapContentWidth(), horizontalArrangement = Arrangement.Absolute.Left
            ) {
                Text(text = "Total:", color = SettingsModel.textColor)
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = totalCur2State, color = SettingsModel.textColor)
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = cur2State, color = SettingsModel.textColor)
            }

            SearchableDropdownMenu(
                items = items.toMutableList(),
                modifier = Modifier.padding(0.dp, 15.dp, 0.dp, 5.dp),
                label = "Items",
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
                    modifier = Modifier.wrapContentWidth(), horizontalArrangement = Arrangement.Absolute.Left
                ) {
                    Text(text = "Tax2:", color = SettingsModel.textColor)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = tax2State, color = SettingsModel.textColor)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = curState, color = SettingsModel.textColor)
                }

                Row(
                    modifier = Modifier.wrapContentWidth(), horizontalArrangement = Arrangement.Absolute.Left
                ) {
                    Text(text = "Total Tax:", color = SettingsModel.textColor)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = totalTaxState, color = SettingsModel.textColor)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = curState, color = SettingsModel.textColor)
                }
            }
            Row(
                modifier = Modifier.wrapContentWidth(), horizontalArrangement = Arrangement.Absolute.Left
            ) {
                Text(text = "Table Number:", color = SettingsModel.textColor)
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = tableNoState, color = SettingsModel.textColor)
            }

            Row(
                modifier = Modifier.wrapContentWidth(), horizontalArrangement = Arrangement.Absolute.Left
            ) {
                Text(text = "Client:", color = SettingsModel.textColor)
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = clientState, color = SettingsModel.textColor)
            }

            SearchableDropdownMenu(
                items = thirdParties.toMutableList(),
                modifier = Modifier.padding(0.dp, 15.dp, 0.dp, 5.dp),
                label = "Customers",
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
        InvoiceFooterView(invoiceHeader = InvoiceHeader(), currency = Currency())
    }
}
