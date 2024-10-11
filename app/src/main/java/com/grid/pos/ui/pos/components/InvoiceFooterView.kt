package com.grid.pos.ui.pos.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.Item.Item
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.SearchableDropdownMenuEx
import com.grid.pos.ui.theme.GridPOSTheme

@Composable
fun InvoiceFooterView(
    invoiceHeader: InvoiceHeader,
    items: MutableList<Item> = mutableListOf(),
    thirdParties: MutableList<ThirdParty> = mutableListOf(),
    invoiceHeaders: MutableList<InvoiceHeader> = mutableListOf(),
    modifier: Modifier = Modifier,
    isFromTable: Boolean = false,
    onLoadClients: () -> Unit = {},
    onLoadInvoices: () -> Unit = {},
    onLoadItems: () -> Unit = {},
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

    val totalState = String.format(
        "%,.${curr1Decimal}f",
        invoiceHeader.invoiceHeadTotal
    )
    val totalCur2State = String.format(
        "%,.${curr2Decimal}f",
        invoiceHeader.invoiceHeadTotal1
    )

    val tableNoState = invoiceHeader.invoiceHeadTaName ?: ""
    var clientState by remember { mutableStateOf(invoiceHeader.invoiceHeadCashName ?: "") }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        if (!isFromTable) {
            SearchableDropdownMenuEx(
                items = invoiceHeaders.toMutableList(),
                showSelected = false,
                selectedId = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        5.dp,
                        180.dp,
                        5.dp,
                        5.dp
                    ),
                label = "Invoices",
                onLoadItems = { onLoadInvoices.invoke() },
            ) { invoiceHeader ->
                onInvoiceSelected.invoke(invoiceHeader as InvoiceHeader)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .padding(horizontal = 5.dp),
                maxLines = 1,
                text = "$totalState $curState",
                color = SettingsModel.textColor
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .padding(horizontal = 5.dp),
                maxLines = 1,
                text = "$totalCur2State $cur2State",
                color = SettingsModel.textColor
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight()
                        .padding(horizontal = 5.dp),
                    maxLines = 1,
                    text = clientState,
                    color = SettingsModel.textColor
                )

                Text(
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight()
                        .padding(horizontal = 5.dp),
                    maxLines = 1,
                    text = tableNoState,
                    color = SettingsModel.textColor
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(5.dp),
            ) {
                SearchableDropdownMenuEx(items = items.toMutableList(),
                    showSelected = false,
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight()
                        .padding(end = 5.dp),
                    label = "Items",
                    onLoadItems = {
                        onLoadItems.invoke()
                    },
                    leadingIcon = {
                        if (SettingsModel.connectionType != CONNECTION_TYPE.SQL_SERVER.key) {
                            Icon(
                                Icons.Default.AddCircleOutline,
                                contentDescription = "add Item",
                                tint = Color.Black,
                                modifier = it
                            )
                        }
                    },
                    onLeadingIconClick = {
                        onAddItem.invoke()
                    }) { item ->
                    onItemSelected.invoke(item as Item)
                }

                val selectedThirdParty =
                    if (invoiceHeader.invoiceHeadThirdPartyName.isNullOrEmpty()) {
                        SettingsModel.defaultThirdParty
                            ?: thirdParties.firstOrNull { it.thirdPartyDefault }
                    } else {
                        thirdParties.firstOrNull {
                            it.thirdPartyId.equals(
                                invoiceHeader.invoiceHeadThirdPartyName,
                                ignoreCase = true
                            )
                        } ?: SettingsModel.defaultThirdParty
                    }
                selectedThirdParty?.let {
                    clientState = it.thirdPartyName ?: ""
                    invoiceHeader.invoiceHeadThirdPartyNewName = it.thirdPartyName
                    onThirdPartySelected.invoke(it)
                } ?: run {
                    clientState = ""
                    invoiceHeader.invoiceHeadThirdPartyNewName = null
                }
                SearchableDropdownMenuEx(
                    items = thirdParties.toMutableList(),
                    selectedId = selectedThirdParty?.thirdPartyId,
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight()
                    ,
                    label = "Customers",
                    onLoadItems = { onLoadClients.invoke() },
                    leadingIcon = {
                        if (SettingsModel.connectionType != CONNECTION_TYPE.SQL_SERVER.key) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = "Add Customer",
                                tint = Color.Black,
                                modifier = it
                            )
                        }
                    },
                    onLeadingIconClick = {
                        onAddThirdParty.invoke()
                    }) { thirdParty ->
                    thirdParty as ThirdParty
                    clientState = thirdParty.thirdPartyName ?: ""
                    onThirdPartySelected.invoke(thirdParty)
                }

            }

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
