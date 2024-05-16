package com.grid.pos.ui.pos.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.Item.Item
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.SearchableDropdownMenu
import com.grid.pos.ui.theme.GridPOSTheme

@Composable
fun InvoiceFooterView(
    invoiceHeader: InvoiceHeader,
    items: MutableList<Item> = mutableListOf(),
    thirdParties: MutableList<ThirdParty> = mutableListOf(),
    invoiceHeaders: MutableList<InvoiceHeader> = mutableListOf(),
    modifier: Modifier = Modifier,
    onAddItem: () -> Unit = {},
    onAddThirdParty: () -> Unit = {},
    onItemSelected: (Item) -> Unit = {},
    onThirdPartySelected: (ThirdParty) -> Unit = {},
    onInvoiceSelected: (InvoiceHeader) -> Unit = {},
) {
    val currency = SettingsModel.currentCurrency ?: Currency()
    val curState = currency.currencyCode1 ?: ""
    val cur2State = currency.currencyCode2 ?: ""
    val curr1Decimal = currency.currencyName1Dec
    val curr2Decimal = currency.currencyName2Dec
    val taxState = String.format(
        "%.${curr1Decimal}f",
        invoiceHeader.invoiceHeadTaxAmt
    )
    val tax1State = String.format(
        "%.${curr1Decimal}f",
        invoiceHeader.invoiceHeadTax1Amt
    )
    val tax2State = String.format(
        "%.${curr1Decimal}f",
        invoiceHeader.invoiceHeadTax2Amt
    )
    val totalTaxState = String.format(
        "%.${curr1Decimal}f",
        invoiceHeader.invoiceHeadTotalTax
    )
    val totalState = String.format(
        "%.${curr1Decimal}f",
        invoiceHeader.invoiceHeadGrossAmount
    )
    val totalCur2State = String.format(
        "%.${curr2Decimal}f",
        invoiceHeader.invoiceHeadGrossAmount.times(currency.currencyRate)
    )

    val tableNoState = invoiceHeader.invoiceHeadTaName ?: ""
    var clientState by remember { mutableStateOf(invoiceHeader.invoiceHeadCashName ?: "Cash") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
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
                            text = "Tax: $taxState $curState",
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
                            text = "Tax1: $tax1State $curState",
                            color = SettingsModel.textColor
                        )
                    }
                }
                Row(
                    modifier = Modifier.wrapContentWidth(),
                    horizontalArrangement = Arrangement.Absolute.Left
                ) {
                    Text(
                        text = "Total: $totalState $curState",
                        color = SettingsModel.textColor
                    )
                }

                Row(
                    modifier = Modifier.wrapContentWidth(),
                    horizontalArrangement = Arrangement.Absolute.Left
                ) {
                    Text(
                        text = "Total: $totalCur2State $cur2State",
                        color = SettingsModel.textColor
                    )
                }

                SearchableDropdownMenu(items = items.toMutableList(),
                    modifier = Modifier.padding(
                        0.dp,
                        15.dp,
                        0.dp,
                        5.dp
                    ),
                    label = "Items",
                    leadingIcon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "add Item",
                            tint = Color.Black,
                            modifier = it
                        )
                    },
                    onLeadingIconClick = {
                        onAddItem.invoke()
                    }) { item ->
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
                            text = "Tax2: $tax2State $curState",
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
                            text = "Total Tax: $totalTaxState $curState",
                            color = SettingsModel.textColor
                        )
                    }
                }
                Row(
                    modifier = Modifier.wrapContentWidth(),
                    horizontalArrangement = Arrangement.Absolute.Left
                ) {
                    Text(
                        text = "Table Number: $tableNoState",
                        color = SettingsModel.textColor
                    )
                }

                Row(
                    modifier = Modifier.wrapContentWidth(),
                    horizontalArrangement = Arrangement.Absolute.Left
                ) {
                    Text(
                        text = "Client: $clientState",
                        color = SettingsModel.textColor
                    )
                }

                val defaultThirdParty =
                    if (invoiceHeader.invoiceHeadThirdPartyName.isNullOrEmpty()) {
                        thirdParties.firstOrNull { it.thirdPartyDefault }
                    } else {
                        thirdParties.firstOrNull {
                            it.thirdPartyId.equals(
                                invoiceHeader.invoiceHeadThirdPartyName,
                                ignoreCase = true
                            )
                        }
                    }
                defaultThirdParty?.let {
                    clientState = (it.thirdPartyName ?: "") + invoiceHeader.getCashName(" -")
                    onThirdPartySelected.invoke(it)
                }
                SearchableDropdownMenu(items = thirdParties.toMutableList(),
                    selectedId = defaultThirdParty?.thirdPartyId,
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
                            contentDescription = "Add Customer",
                            tint = Color.Black,
                            modifier = it
                        )
                    },
                    onLeadingIconClick = {
                        onAddThirdParty.invoke()
                    }) { thirdParty ->
                    thirdParty as ThirdParty
                    clientState =
                        (thirdParty.thirdPartyName ?: "") + invoiceHeader.getCashName(" -")
                    onThirdPartySelected.invoke(thirdParty)
                }
            }
        }
        SearchableDropdownMenu(
            items = invoiceHeaders.toMutableList(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    5.dp,
                    15.dp,
                    5.dp,
                    5.dp
                ),
            label = "Invoices"
        ) { invoiceHeader ->
            onInvoiceSelected.invoke(invoiceHeader as InvoiceHeader)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InvoiceFooterViewPreview() {
    GridPOSTheme {
        InvoiceFooterView(
            invoiceHeader = InvoiceHeader()
        )
    }
}
