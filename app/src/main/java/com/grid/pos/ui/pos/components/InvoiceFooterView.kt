package com.grid.pos.ui.pos.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.unit.dp
import com.grid.pos.data.currency.Currency
import com.grid.pos.data.invoiceHeader.InvoiceHeader
import com.grid.pos.data.item.Item
import com.grid.pos.data.thirdParty.ThirdParty
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.SearchableDropdownMenuEx
import com.grid.pos.ui.pos.POSState
import com.grid.pos.ui.pos.POSViewModel

@Composable
fun InvoiceFooterView(
    state: POSState,
    viewModel: POSViewModel,
    modifier: Modifier = Modifier,
    onAddItem: () -> Unit = {},
    onAddThirdParty: () -> Unit = {},
    onItemSelected: (Item) -> Unit = {},
    onThirdPartySelected: (ThirdParty) -> Unit = {},
    onInvoiceSelected: (InvoiceHeader) -> Unit = {},
) {
    val currency = SettingsModel.currentCurrency ?: Currency()

    var clientState by remember { mutableStateOf(state.invoiceHeader.invoiceHeadCashName ?: "") }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        if (!viewModel.isFromTable()) {
            SearchableDropdownMenuEx(
                items = state.invoiceHeaders.toMutableList(),
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
                onLoadItems = {
                    if (viewModel.state.value.thirdParties.isEmpty()) {
                        viewModel.fetchThirdParties()
                    }
                    viewModel.fetchInvoices()
                },
                onNoSearchResultsFound = { key ->
                    viewModel.searchForInvoices(key)
                },
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
                text = String.format(
                    "%,.${currency.currencyName1Dec}f %s",
                    state.invoiceHeader.invoiceHeadTotal,
                    currency.currencyCode1 ?: ""
                ),
                color = SettingsModel.textColor
            )
            if (!SettingsModel.hideSecondCurrency) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .padding(horizontal = 5.dp),
                    maxLines = 1,
                    text = String.format(
                        "%,.${currency.currencyName2Dec}f %s",
                        state.invoiceHeader.invoiceHeadTotal1,
                        currency.currencyCode2 ?: ""
                    ),
                    color = SettingsModel.textColor
                )
            }

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
                    text = state.invoiceHeader.invoiceHeadTaName ?: "",
                    color = SettingsModel.textColor
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(5.dp),
            ) {
                SearchableDropdownMenuEx(items = state.items.toMutableList(),
                    showSelected = false,
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight()
                        .padding(end = 5.dp),
                    label = "Items",
                    onLoadItems = {
                        viewModel.loadFamiliesAndItems()
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
                    if (state.invoiceHeader.invoiceHeadThirdPartyName.isNullOrEmpty()) {
                        viewModel.defaultThirdParty
                            ?: state.thirdParties.firstOrNull { it.thirdPartyDefault }
                    } else {
                        state.thirdParties.firstOrNull {
                            it.thirdPartyId.equals(
                                state.invoiceHeader.invoiceHeadThirdPartyName,
                                ignoreCase = true
                            )
                        } ?: viewModel.defaultThirdParty
                    }
                selectedThirdParty?.let {
                    clientState = it.thirdPartyName ?: ""
                    state.invoiceHeader.invoiceHeadThirdPartyNewName = it.thirdPartyName
                    onThirdPartySelected.invoke(it)
                } ?: run {
                    clientState = ""
                    state.invoiceHeader.invoiceHeadThirdPartyNewName = null
                }
                SearchableDropdownMenuEx(items = state.thirdParties.toMutableList(),
                    selectedId = selectedThirdParty?.thirdPartyId,
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight(),
                    label = "Customers",
                    onLoadItems = { viewModel.fetchThirdParties() },
                    leadingIcon = {
                        if (!viewModel.isFromTable()) {
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
