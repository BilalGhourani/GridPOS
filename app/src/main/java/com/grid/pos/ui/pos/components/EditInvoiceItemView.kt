package com.grid.pos.ui.pos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.pos.POSUtils
import com.grid.pos.utils.Utils

@Composable
fun EditInvoiceItemView(
    modifier: Modifier = Modifier,
    invoices: MutableList<InvoiceItemModel>,
    invHeader: InvoiceHeader,
    invoiceIndex: Int = 0,
    onSave: (InvoiceHeader, InvoiceItemModel) -> Unit = { _, _ -> },
    onClose: () -> Unit = {},
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val rDiscount1FocusRequester = remember { FocusRequester() }
    val rDiscount2FocusRequester = remember { FocusRequester() }
    val discount1FocusRequester = remember { FocusRequester() }
    val discount2FocusRequester = remember { FocusRequester() }
    val itemExtraNameFocusRequester = remember { FocusRequester() }
    val itemNoteFocusRequester = remember { FocusRequester() }
    val invoiceNoteFocusRequester = remember { FocusRequester() }
    val clientExtraNameRequester = remember { FocusRequester() }
    val taxFocusRequester = remember { FocusRequester() }
    val tax1FocusRequester = remember { FocusRequester() }
    val tax2FocusRequester = remember { FocusRequester() }

    var invoiceHeader = invHeader.copy()
    val invoiceItemAtIndex = invoices[invoiceIndex]
    val invoiceItemModel = InvoiceItemModel(
        invoice = invoiceItemAtIndex.invoice.copy(),
        invoiceItem = invoiceItemAtIndex.invoiceItem
    )

    val rDiscountVal = invoiceItemModel.invoice.invoiceDiscount
    val rDiscamtVal = invoiceItemModel.invoice.invoiceDiscamt * (rDiscountVal.div(100.0))
    val discountVal = invoiceHeader.invoiceHeadDiscount
    val discamtVal = invoiceHeader.invoiceHeadDiscountAmount * (discountVal.div(100.0))

    var price by remember {
        mutableStateOf(
            invoiceItemModel.invoice.invoicePrice.toString()
        )
    }
    var qty by remember {
        mutableIntStateOf(
            invoiceItemModel.invoice.invoiceQuantity.toInt()
        )
    }
    var rDiscount1 by remember {
        mutableStateOf(
            if (rDiscountVal > 0.0) rDiscountVal.toString() else ""
        )
    }
    var rDiscount2 by remember {
        mutableStateOf(
            if (rDiscamtVal > 0.0) rDiscamtVal.toString() else ""
        )
    }
    var discount1 by remember {
        mutableStateOf(
            if (discountVal > 0.0) discountVal.toString() else ""
        )
    }
    var discount2 by remember {
        mutableStateOf(
            if (discamtVal > 0.0) discamtVal.toString() else ""
        )
    }
    var itemExtraName by remember {
        mutableStateOf(
            invoiceItemModel.invoice.invoicExtraName ?: ""
        )
    }
    var itemNote by remember { mutableStateOf(invoiceItemModel.invoice.invoicNote ?: "") }
    var invoiceNote by remember { mutableStateOf(invoiceHeader.invoiceHeadNote ?: "") }
    var clientExtraName by remember { mutableStateOf(invoiceHeader.invoiceHeadCashName ?: "") }
    var taxState by remember {
        mutableStateOf(invoiceItemModel.invoice.invoiceTax.toString().takeIf { it != "0.0" } ?: "")
    }
    var tax1State by remember {
        mutableStateOf(invoiceItemModel.invoice.invoiceTax1.toString().takeIf { it != "0.0" } ?: "")
    }
    var tax2State by remember {
        mutableStateOf(invoiceItemModel.invoice.invoiceTax2.toString().takeIf { it != "0.0" } ?: "")
    }
    val curr1Decimal = SettingsModel.currentCurrency?.currencyName1Dec ?: 2
    val curr2Decimal = SettingsModel.currentCurrency?.currencyName2Dec ?: 2
    var isPercentageChanged by remember {
        mutableStateOf(false)
    }

    fun calculateItemDiscount() {
        val itemDiscount = rDiscount1.toDoubleOrNull() ?: 0.0
        val itemDiscountAmount = rDiscount2.toDoubleOrNull() ?: 0.0
        if (itemDiscount == 0.0 && itemDiscountAmount == 0.0) {
            return
        }
        val itemPrice = (invoiceItemModel.getPriceWithTax()).times(qty)
        if (isPercentageChanged) {
            rDiscount2 = String.format(
                "%.${curr1Decimal}f", (itemPrice.times(itemDiscount.div(100.0)))
            )
            invoiceItemModel.invoice.invoiceDiscamt = rDiscount2.toDoubleOrNull() ?: 0.0
        } else {
            rDiscount1 = String.format(
                "%.${curr1Decimal}f", ((itemDiscountAmount.div(itemPrice)).times(100.0))
            )
            invoiceItemModel.invoice.invoiceDiscount = rDiscount1.toDoubleOrNull() ?: 0.0
        }
    }

    fun calculateInvoiceDiscount() {
        if ((discount1.isEmpty() || discount1 == "0.0") && (discount2.isEmpty() || discount2 == "0.0")) {
            return
        }
        invoices[invoiceIndex] = invoiceItemModel
        invoiceHeader = POSUtils.refreshValues(invoices, invoiceHeader)
        if (isPercentageChanged) {
            discount2 = String.format("%.${curr2Decimal}f", invoiceHeader.invoiceHeadDiscountAmount)
        } else {
            discount1 = String.format("%.${curr1Decimal}f", invoiceHeader.invoiceHeadDiscount)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            OutlinedTextField(value = price, onValueChange = {
                price = Utils.getDoubleValue(
                    it, price
                )
                invoiceItemModel.invoice.invoicePrice = price.toDoubleOrNull() ?: 0.0
            }, modifier = Modifier
                .weight(1f)
                .onFocusChanged {
                    if (!it.hasFocus) {
                        calculateItemDiscount()
                        calculateInvoiceDiscount()
                    }
                }, shape = RoundedCornerShape(15.dp), label = {
                Text(
                    "Price", color = SettingsModel.textColor
                )
            }, keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
            ), keyboardActions = KeyboardActions(
                onNext = { rDiscount1FocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )
            OutlinedTextField(value = qty.toString(),
                onValueChange = {
                    qty = it.toInt()
                },
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.weight(1f),
                readOnly = true,
                label = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = Color.Transparent)
                    ) {
                        Text(
                            text = "Qty", modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center, color = SettingsModel.textColor
                        )
                    }
                },
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { /* Move focus to next field */ }),
                leadingIcon = {
                    IconButton(onClick = {
                        qty++
                        invoiceItemModel.invoice.invoiceQuantity = qty.toDouble()
                        calculateItemDiscount()
                        calculateInvoiceDiscount()
                    }) {
                        Icon(
                            Icons.Default.Add, contentDescription = "Increase quantity",
                            tint = SettingsModel.buttonColor
                        )
                    }
                },
                trailingIcon = {
                    IconButton(onClick = {
                        if (qty > 1) qty--
                        invoiceItemModel.invoice.invoiceQuantity = qty.toDouble()
                        calculateItemDiscount()
                        calculateInvoiceDiscount()
                    }) {
                        Icon(
                            Icons.Default.Remove, contentDescription = "Decrease quantity",
                            tint = SettingsModel.buttonColor
                        )
                    }
                })
        }
        Text(
            modifier = Modifier.padding(
                0.dp, 10.dp, 0.dp, 0.dp
            ), text = "Discount", style = TextStyle(
                textDecoration = TextDecoration.Underline, fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ), color = SettingsModel.textColor
        )
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "R. disc", modifier = Modifier
                    .width(60.dp)
                    .height(60.dp)
                    .wrapContentHeight(
                        align = Alignment.CenterVertically
                    ), color = SettingsModel.textColor
            )
            OutlinedTextField(value = rDiscount1, onValueChange = {
                rDiscount1 = Utils.getDoubleValue(
                    it, rDiscount1
                )
                isPercentageChanged = true
            }, placeholder = {
                Text(text = "0.0")
            }, modifier = Modifier
                .weight(1f)
                .onFocusChanged {
                    if (!it.hasFocus) {
                        calculateItemDiscount()
                        calculateInvoiceDiscount()
                    }
                }
                .focusRequester(rDiscount1FocusRequester), keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
            ), keyboardActions = KeyboardActions(
                onNext = { rDiscount2FocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )
            OutlinedTextField(value = rDiscount2, onValueChange = {
                rDiscount2 = Utils.getDoubleValue(
                    it, rDiscount2
                )
                isPercentageChanged = false
            }, placeholder = {
                Text(text = "0.0")
            }, modifier = Modifier
                .weight(1f)
                .onFocusChanged {
                    if (!it.hasFocus) {
                        calculateItemDiscount()
                        calculateInvoiceDiscount()
                    }
                }
                .focusRequester(rDiscount2FocusRequester), keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
            ), keyboardActions = KeyboardActions(
                onNext = { discount1FocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Disc", modifier = Modifier
                    .width(60.dp)
                    .height(60.dp)
                    .wrapContentHeight(
                        align = Alignment.CenterVertically
                    ), color = SettingsModel.textColor
            )
            OutlinedTextField(value = discount1, onValueChange = {
                discount1 = Utils.getDoubleValue(
                    it, discount1
                )
                isPercentageChanged = true
                invoiceHeader.invoiceHeadDiscount = discount1.toDoubleOrNull() ?: 0.0
            }, placeholder = {
                Text(text = "0.0")
            }, modifier = Modifier
                .weight(1f)
                .onFocusChanged {
                    if (!it.hasFocus) {
                        calculateInvoiceDiscount()
                    }
                }
                .focusRequester(discount1FocusRequester), keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
            ), keyboardActions = KeyboardActions(
                onNext = { discount2FocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )
            OutlinedTextField(value = discount2, onValueChange = {
                discount2 = Utils.getDoubleValue(
                    it, discount2
                )
                isPercentageChanged = false
                invoiceHeader.invoiceHeadDiscountAmount = discount2.toDoubleOrNull() ?: 0.0
            }, placeholder = {
                Text(text = "0.0")
            }, modifier = Modifier
                .weight(1f)
                .onFocusChanged {
                    if (!it.hasFocus) {
                        calculateInvoiceDiscount()
                    }
                }
                .focusRequester(discount2FocusRequester), keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
            ), keyboardActions = KeyboardActions(
                onNext = { clientExtraNameRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )
        }

        UITextField(modifier = Modifier.padding(10.dp), defaultValue = clientExtraName,
            label = "Client Exta Name", focusRequester = clientExtraNameRequester, onAction = {
                itemExtraNameFocusRequester.requestFocus()
            }) {
            clientExtraName = it
        }

        UITextField(modifier = Modifier.padding(10.dp), defaultValue = itemExtraName,
            label = "Item Extra Name", focusRequester = itemExtraNameFocusRequester,
            onAction = { invoiceNoteFocusRequester.requestFocus() }) {
            itemExtraName = it
        }

        UITextField(modifier = Modifier.padding(10.dp), defaultValue = invoiceNote,
            label = "Invoice Note", focusRequester = invoiceNoteFocusRequester,
            onAction = {
                itemNoteFocusRequester.requestFocus()
            }) {
            invoiceNote = it
        }

        UITextField(modifier = Modifier.padding(10.dp), defaultValue = itemNote,
            label = "Item Note", focusRequester = itemNoteFocusRequester,
            onAction = {
                if (SettingsModel.showTax) {
                    taxFocusRequester.requestFocus()
                } else if (SettingsModel.showTax1) {
                    tax1FocusRequester.requestFocus()
                } else if (SettingsModel.showTax2) {
                    tax2FocusRequester.requestFocus()
                } else {
                    keyboardController?.hide()
                }
            }) {
            itemNote = it
        }

        if (SettingsModel.showTax || SettingsModel.showTax1 || SettingsModel.showTax2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (SettingsModel.showTax) {
                    UITextField(modifier = Modifier
                        .weight(1f)
                        .padding(10.dp),
                        defaultValue = taxState, label = "Tax", keyboardType = KeyboardType.Decimal,
                        focusRequester = taxFocusRequester, onAction = {
                            if (SettingsModel.showTax1) {
                                tax1FocusRequester.requestFocus()
                            } else if (SettingsModel.showTax2) {
                                tax2FocusRequester.requestFocus()
                            } else {
                                keyboardController?.hide()
                            }
                        }, onFocusChanged = {
                            if (!it.hasFocus) {
                                calculateItemDiscount()
                                calculateInvoiceDiscount()
                            }
                        }) {
                        taxState = Utils.getDoubleValue(
                            it, taxState
                        )
                        invoiceItemModel.invoice.invoiceTax = taxState.toDoubleOrNull() ?: 0.0
                    }
                }
                if (SettingsModel.showTax1) {
                    UITextField(modifier = Modifier
                        .weight(1f)
                        .padding(10.dp),
                        defaultValue = tax1State, label = "Tax1",
                        keyboardType = KeyboardType.Decimal, focusRequester = tax1FocusRequester,
                        onAction = {
                            if (SettingsModel.showTax2) {
                                tax2FocusRequester.requestFocus()
                            } else {
                                keyboardController?.hide()
                            }
                        }, onFocusChanged = {
                            if (!it.hasFocus) {
                                calculateItemDiscount()
                                calculateInvoiceDiscount()
                            }
                        }) {
                        tax1State = Utils.getDoubleValue(
                            it, tax1State
                        )
                        invoiceItemModel.invoice.invoiceTax1 = tax1State.toDoubleOrNull() ?: 0.0
                    }
                }
                if (SettingsModel.showTax2) {
                    UITextField(modifier = Modifier
                        .weight(1f)
                        .padding(10.dp),
                        defaultValue = tax2State, label = "Tax2",
                        keyboardType = KeyboardType.Decimal, focusRequester = tax2FocusRequester,
                        imeAction = ImeAction.Done, onAction = { keyboardController?.hide() },
                        onFocusChanged = {
                            if (!it.hasFocus) {
                                calculateItemDiscount()
                                calculateInvoiceDiscount()
                            }
                        }) {
                        tax2State = Utils.getDoubleValue(
                            it, tax1State
                        )
                        invoiceItemModel.invoice.invoiceTax2 = tax2State.toDoubleOrNull() ?: 0.0
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UIButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(), text = "Save",
                shape = RoundedCornerShape(15.dp)
            ) {
                invoiceHeader.invoiceHeadNote = invoiceNote
                invoiceHeader.invoiceHeadCashName = clientExtraName
                invoiceHeader.invoiceHeadDiscount = discount1.toDoubleOrNull() ?: 0.0
                invoiceHeader.invoiceHeadDiscountAmount = discount2.toDoubleOrNull() ?: 0.0

                invoiceItemModel.invoice.invoicePrice =
                    price.toDoubleOrNull() ?: invoiceItemModel.invoiceItem.itemUnitPrice
                invoiceItemModel.invoice.invoiceTax = taxState.toDoubleOrNull() ?: 0.0
                invoiceItemModel.invoice.invoiceTax1 = tax1State.toDoubleOrNull() ?: 0.0
                invoiceItemModel.invoice.invoiceTax2 = tax2State.toDoubleOrNull() ?: 0.0
                invoiceItemModel.invoice.invoiceDiscount = rDiscount1.toDoubleOrNull() ?: 0.0
                invoiceItemModel.invoice.invoiceDiscamt = rDiscount2.toDoubleOrNull() ?: 0.0
                invoiceItemModel.invoice.invoiceQuantity = qty.toDouble()
                invoiceItemModel.invoice.invoicExtraName = itemExtraName
                invoiceItemModel.invoice.invoicNote = itemNote
                onSave.invoke(
                    invoiceHeader, invoiceItemModel
                )
            }

            UIButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(), text = "Clear",
                shape = RoundedCornerShape(15.dp)
            ) {
                price = ""
                qty = 1
                rDiscount1 = ""
                rDiscount2 = ""
                discount1 = ""
                discount2 = ""
                itemExtraName = ""
                itemNote = ""
                invoiceNote = ""
                clientExtraName = ""
            }

            UIButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(), text = "Close",
                shape = RoundedCornerShape(15.dp)
            ) {
                onClose.invoke()
            }
        }

    }
}