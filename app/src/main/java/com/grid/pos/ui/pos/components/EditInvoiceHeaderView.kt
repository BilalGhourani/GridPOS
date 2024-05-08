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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils

@Composable
fun EditInvoiceHeaderView(
        modifier: Modifier = Modifier,
        invoiceItemModel: InvoiceItemModel = InvoiceItemModel(),
        invoiceHeader: InvoiceHeader = InvoiceHeader(),
        onSave: (InvoiceHeader, InvoiceItemModel) -> Unit = { _, _ -> },
        onClose: () -> Unit = {},
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val rDiscount1FocusRequester = remember { FocusRequester() }
    val rDiscount2FocusRequester = remember { FocusRequester() }
    val discount1FocusRequester = remember { FocusRequester() }
    val discount2FocusRequester = remember { FocusRequester() }
    val clientExtraNameFocusRequester = remember { FocusRequester() }
    val itemNoteFocusRequester = remember { FocusRequester() }
    val invoiceNoteFocusRequester = remember { FocusRequester() }
    val taxFocusRequester = remember { FocusRequester() }
    val tax1FocusRequester = remember { FocusRequester() }
    val tax2FocusRequester = remember { FocusRequester() }

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
    var rDiscount1 by remember { mutableStateOf("") }
    var rDiscount2 by remember { mutableStateOf("") }
    var discount1 by remember { mutableStateOf("") }
    var discount2 by remember { mutableStateOf("") }
    var clientExtraName by remember {
        mutableStateOf(
            invoiceItemModel.invoice.invoicExtraName ?: ""
        )
    }
    var itemNote by remember { mutableStateOf(invoiceItemModel.invoice.invoicNote ?: "") }
    var invoiceNote by remember { mutableStateOf(invoiceHeader.invoiceHeadNote ?: "") }
    var taxState by remember { mutableStateOf(invoiceItemModel.invoice.invoiceTax.toString()) }
    var tax1State by remember {
        mutableStateOf(
            invoiceItemModel.invoice.invoiceTax1.toString()
        )
    }
    var tax2State by remember {
        mutableStateOf(
            invoiceItemModel.invoice.invoiceTax2.toString()
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            OutlinedTextField(
                value = price,
                onValueChange = {
                    price = Utils.getDoubleValue(
                        it,
                        price
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(15.dp),
                label = {
                    Text(
                        "Price",
                        color = SettingsModel.textColor
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { rDiscount1FocusRequester.requestFocus() }),
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
                            text = "Qty",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = SettingsModel.textColor
                        )
                    }
                },
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { /* Move focus to next field */ }),
                leadingIcon = {
                    IconButton(onClick = { qty++ }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Increase quantity",
                            tint = SettingsModel.buttonColor
                        )
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { if (qty > 1) qty-- }) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Decrease quantity",
                            tint = SettingsModel.buttonColor
                        )
                    }
                })
        }
        Text(
            modifier = Modifier.padding(
                0.dp,
                10.dp,
                0.dp,
                0.dp
            ),
            text = "Discount",
            style = TextStyle(
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            color = SettingsModel.textColor
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "R. disc",
                modifier = Modifier
                    .width(60.dp)
                    .height(60.dp)
                    .wrapContentHeight(
                        align = Alignment.CenterVertically
                    ),
                color = SettingsModel.textColor
            )
            OutlinedTextField(
                value = rDiscount1,
                onValueChange = {
                    rDiscount1 = Utils.getDoubleValue(
                        it,
                        rDiscount1
                    )
                },
                placeholder = {
                    Text(text = "0.0")
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(rDiscount1FocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { rDiscount2FocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )
            OutlinedTextField(
                value = rDiscount2,
                onValueChange = {
                    rDiscount2 = Utils.getDoubleValue(
                        it,
                        rDiscount2
                    )
                },
                placeholder = {
                    Text(text = "0.0")
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(rDiscount2FocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { discount1FocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Disc",
                modifier = Modifier
                    .width(60.dp)
                    .height(60.dp)
                    .wrapContentHeight(
                        align = Alignment.CenterVertically
                    ),
                color = SettingsModel.textColor
            )
            OutlinedTextField(
                value = discount1,
                onValueChange = {
                    discount1 = Utils.getDoubleValue(
                        it,
                        discount1
                    )
                },
                placeholder = {
                    Text(text = "0.0")
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(discount1FocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { discount2FocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )
            OutlinedTextField(
                value = discount2,
                onValueChange = {
                    discount2 = Utils.getDoubleValue(
                        it,
                        discount2
                    )
                },
                placeholder = {
                    Text(text = "0.0")
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(discount2FocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { clientExtraNameFocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )
        }


        UITextField(modifier = Modifier.padding(10.dp),
            defaultValue = clientExtraName,
            label = "Client Extra Name",
            focusRequester = clientExtraNameFocusRequester,
            onAction = { itemNoteFocusRequester.requestFocus() }) {
            clientExtraName = it
        }

        UITextField(modifier = Modifier.padding(10.dp),
            defaultValue = itemNote,
            label = "Item Note",
            focusRequester = itemNoteFocusRequester,
            onAction = { invoiceNoteFocusRequester.requestFocus() }) {
            itemNote = it
        }

        UITextField(modifier = Modifier.padding(10.dp),
            defaultValue = invoiceNote,
            label = "Invoice Note",
            focusRequester = itemNoteFocusRequester,
            onAction = { taxFocusRequester.requestFocus() }) {
            invoiceNote = it
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
                        defaultValue = taxState,
                        label = "Tax",
                        keyboardType = KeyboardType.Decimal,
                        focusRequester = taxFocusRequester,
                        onAction = { tax1FocusRequester.requestFocus() }) {
                        taxState = Utils.getDoubleValue(
                            it,
                            taxState
                        )
                    }
                }
                if (SettingsModel.showTax1) {
                    UITextField(modifier = Modifier
                        .weight(1f)
                        .padding(10.dp),
                        defaultValue = tax1State,
                        label = "Tax1",
                        keyboardType = KeyboardType.Decimal,
                        focusRequester = tax1FocusRequester,
                        onAction = { tax2FocusRequester.requestFocus() }) {
                        tax1State = Utils.getDoubleValue(
                            it,
                            taxState
                        )
                    }
                }
                if (SettingsModel.showTax2) {
                    UITextField(modifier = Modifier
                        .weight(1f)
                        .padding(10.dp),
                        defaultValue = tax2State,
                        label = "Tax2",
                        keyboardType = KeyboardType.Decimal,
                        focusRequester = tax2FocusRequester,
                        imeAction = ImeAction.Done,
                        onAction = { keyboardController?.hide() }) {
                        tax2State = Utils.getDoubleValue(
                            it,
                            taxState
                        )
                    }
                }
            }
        }/* Row(
             modifier = Modifier
                 .fillMaxWidth()
                 .height(60.dp)
                 .fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
         ) {
             UIButton(
                 modifier = Modifier
                     .weight(1f)
                     .fillMaxHeight(), text = "Add Customer", shape = RoundedCornerShape(15.dp)
             ) {
                 onAddCustomer.invoke()
             }

             UIButton(
                 modifier = Modifier
                     .weight(1f)
                     .fillMaxHeight(), text = "Add Item", shape = RoundedCornerShape(15.dp)
             ) {
                 onAddItem.invoke()
             }
         }*/

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
                    .fillMaxHeight(),
                text = "Save",
                shape = RoundedCornerShape(15.dp)
            ) {
                invoiceHeader.invoiceHeadNote = invoiceNote

                invoiceItemModel.invoice.invoicePrice = price.toDoubleOrNull() ?: invoiceItemModel.invoiceItem.itemUnitPrice
                invoiceItemModel.invoice.invoiceTax = taxState.toDoubleOrNull() ?: 0.0
                invoiceItemModel.invoice.invoiceTax1 = tax1State.toDoubleOrNull() ?: 0.0
                invoiceItemModel.invoice.invoiceTax2 = tax2State.toDoubleOrNull() ?: 0.0
                invoiceItemModel.invoice.invoiceQuantity = qty.toDouble()
                invoiceItemModel.invoice.invoicExtraName = clientExtraName
                invoiceItemModel.invoice.invoicNote = itemNote
                onSave.invoke(
                    invoiceHeader,
                    invoiceItemModel
                )
            }

            UIButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = "Clear",
                shape = RoundedCornerShape(15.dp)
            ) {
                price = ""
                qty = 1
                rDiscount1 = ""
                rDiscount2 = ""
                discount1 = ""
                discount2 = ""
                clientExtraName = ""
                itemNote = ""
                invoiceNote = ""
            }

            UIButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = "Close",
                shape = RoundedCornerShape(15.dp)
            ) {
                onClose.invoke()
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun EditInvoiceHeaderViewPreview() {
    GridPOSTheme {
        EditInvoiceHeaderView()
    }
}