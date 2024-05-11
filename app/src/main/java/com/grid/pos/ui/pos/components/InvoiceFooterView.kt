package com.grid.pos.ui.pos.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.Item.Item
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.SearchableDropdownMenu
import com.grid.pos.ui.theme.GridPOSTheme

@Composable
fun InvoiceFooterView(
        navController: NavController? = null,
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

    val tax = invoiceHeader.invoiceHeadTaxAmt
    val tax1 = invoiceHeader.invoiceHeadTax1Amt
    val tax2 = invoiceHeader.invoiceHeadTax2Amt
    val total = invoiceHeader.invoiceHeadTotal
    val curr1Decimal = currency.currencyName1Dec
    val curr2Decimal = currency.currencyName2Dec
    taxState = String.format(
        "%.${curr1Decimal}f",
        tax
    )
    tax1State = String.format(
        "%.${curr1Decimal}f",
        tax1
    )
    tax2State = String.format(
        "%.${curr1Decimal}f",
        tax2
    )
    totalTaxState = String.format(
        "%.${curr1Decimal}f",
        tax + tax1 + tax2
    )
    val netTotal = (total - (tax + tax1 + tax2)).times(invoiceHeader.invoiceHeadDiscount.div(100.0))
    totalState = String.format(
        "%.${curr1Decimal}f",
        netTotal
    )
    totalCur2State = String.format(
        "%.${curr2Decimal}f",
        netTotal.times(currency.currencyRate)
    )

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
            if (SettingsModel.showTax) {
                Row(
                    modifier = Modifier.wrapContentWidth(),
                    horizontalArrangement = Arrangement.Absolute.Left
                ) {
                    Text(
                        text = "Tax: ${taxState} ${curState}",
                        color = SettingsModel.textColor
                    )
                }
            }
            if (SettingsModel.showTax1) {
                Row(
                    modifier = Modifier.wrapContentWidth(),
                    horizontalArrangement = Arrangement.Absolute.Left
                ) {
                    Text(
                        text = "Tax1: ${tax1State} ${curState}",
                        color = SettingsModel.textColor
                    )
                }
            }
            Row(
                modifier = Modifier.wrapContentWidth(),
                horizontalArrangement = Arrangement.Absolute.Left
            ) {
                Text(
                    text = "Total: ${totalState} ${curState}",
                    color = SettingsModel.textColor
                )
            }

            Row(
                modifier = Modifier.wrapContentWidth(),
                horizontalArrangement = Arrangement.Absolute.Left
            ) {
                Text(
                    text = "Total: ${totalCur2State} ${cur2State}",
                    color = SettingsModel.textColor
                )
            }

            SearchableDropdownMenu(
                items = items.toMutableList(),
                modifier = Modifier.padding(
                    0.dp,
                    15.dp,
                    0.dp,
                    5.dp
                ),
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
            if (SettingsModel.showTax2) {
                Row(
                    modifier = Modifier.wrapContentWidth(),
                    horizontalArrangement = Arrangement.Absolute.Left
                ) {
                    Text(
                        text = "Tax2: ${tax2State} ${curState}",
                        color = SettingsModel.textColor
                    )
                }
            }
            if (SettingsModel.showTax || SettingsModel.showTax1 || SettingsModel.showTax2) {
                Row(
                    modifier = Modifier.wrapContentWidth(),
                    horizontalArrangement = Arrangement.Absolute.Left
                ) {
                    Text(
                        text = "Total Tax: ${totalTaxState} ${curState}",
                        color = SettingsModel.textColor
                    )
                }
            }
            Row(
                modifier = Modifier.wrapContentWidth(),
                horizontalArrangement = Arrangement.Absolute.Left
            ) {
                Text(
                    text = "Table Number: ${tableNoState}",
                    color = SettingsModel.textColor
                )
            }

            Row(
                modifier = Modifier.wrapContentWidth(),
                horizontalArrangement = Arrangement.Absolute.Left
            ) {
                Text(
                    text = "Client: ${clientState}",
                    color = SettingsModel.textColor
                )
            }

            SearchableDropdownMenu(items = thirdParties.toMutableList(),
                modifier = Modifier.padding(
                    0.dp,
                    15.dp,
                    0.dp,
                    5.dp
                ),
                label = "Customers",
                leadingIcon = {
                    Icon(
                        Icons.Default.PersonAdd,
                        contentDescription = "Increase quantity",
                        tint = Color.Black,
                        modifier = it
                    )
                },
                onLeadingIconClick = { navController?.navigate("ManageThirdPartiesView") }) { thirdParty ->
                onThirdPartySelected.invoke(thirdParty as ThirdParty)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InvoiceFooterViewPreview() {
    GridPOSTheme {
        InvoiceFooterView(
            invoiceHeader = InvoiceHeader(),
            currency = Currency()
        )
    }
}
