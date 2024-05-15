package com.grid.pos.ui.pos.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.PosReceipt.PosReceipt
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.pos.POSState
import com.grid.pos.utils.Utils

@Composable
fun InvoiceCashView(
        modifier: Modifier,
        posState: POSState,
        onSave: (Double, PosReceipt) -> Unit = { _, _ -> },
        onFinish: (Double, PosReceipt) -> Unit = { _, _ -> },
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val cashCurr2FocusRequester = remember { FocusRequester() }
    val cashTotal1FocusRequester = remember { FocusRequester() }
    val cashTotal2FocusRequester = remember { FocusRequester() }
    val creditCurr1PaidFocusRequester = remember { FocusRequester() }
    val creditCurr2PaidFocusRequester = remember { FocusRequester() }
    val creditCurr1TotalFocusRequester = remember { FocusRequester() }
    val creditCurr2TotalFocusRequester = remember { FocusRequester() }
    val debitCurr1PaidFocusRequester = remember { FocusRequester() }
    val debitCurr2PaidFocusRequester = remember { FocusRequester() }
    val debitCurr1TotalFocusRequester = remember { FocusRequester() }
    val debitCurr2TotalFocusRequester = remember { FocusRequester() }

    val invoiceHeader = posState.invoiceHeader
    val currency = SettingsModel.currentCurrency ?: Currency()
    val posReceipt = posState.posReceipt.copy()

    val curr1Decimal = currency.currencyName1Dec
    val curr2Decimal = currency.currencyName2Dec
    val rate = currency.currencyRate
    val netTotal = invoiceHeader.invoiceHeadGrossAmount
    val cashTotalPaid1 = String.format(
        "%.${curr1Decimal}f",
        netTotal
    )
    val cashTotalPaid2 = String.format(
        "%.${curr1Decimal}f",
        (netTotal).times(rate)
    )

    val curr1State by remember { mutableStateOf(currency.currencyCode1 ?: "") }
    val curr2State by remember { mutableStateOf(currency.currencyCode2 ?: "") }
    var cashCurr1Paid by remember {
        mutableStateOf(
            if (posReceipt.posReceiptCash > 0.0) String.format(
                "%.${curr1Decimal}f",
                posReceipt.posReceiptCash
            ) else ""
        )
    }
    var cashCurr2Paid by remember {
        mutableStateOf(
            if (posReceipt.posReceiptCashs > 0.0) String.format(
                "%.${curr2Decimal}f",
                posReceipt.posReceiptCashs
            ) else ""
        )
    }
    var cashCurr1Total by remember { mutableStateOf(cashTotalPaid1) }
    var cashCurr2Total by remember { mutableStateOf(cashTotalPaid2) }
    var creditCurr1Paid by remember {
        mutableStateOf(
            if (posReceipt.posReceiptCredit > 0.0) String.format(
                "%.${curr1Decimal}f",
                posReceipt.posReceiptCredit
            ) else ""
        )
    }
    var creditCurr2Paid by remember {
        mutableStateOf(
            if (posReceipt.posReceiptCredits > 0.0) String.format(
                "%.${curr2Decimal}f",
                posReceipt.posReceiptCredits
            ) else ""
        )
    }
    var totalCurr1Paid by remember { mutableStateOf("0.0") }
    var totalCurr2Paid by remember { mutableStateOf("0.0") }
    var debitCurr1Paid by remember {
        mutableStateOf(
            if (posReceipt.posReceiptDebit > 0.0) String.format(
                "%.${curr1Decimal}f",
                posReceipt.posReceiptDebit
            ) else ""
        )
    }
    var debitCurr2Paid by remember {
        mutableStateOf(
            if (posReceipt.posReceiptDebits > 0.0) String.format(
                "%.${curr2Decimal}f",
                posReceipt.posReceiptDebits
            ) else ""
        )
    }
    var changeCurr1 by remember { mutableStateOf("0.0") }
    var changeCurr2 by remember { mutableStateOf("0.0") }

    fun calculateTotal() {
        val cashPaid1 = cashCurr1Paid.toDoubleOrNull() ?: 0.0
        val cashPaid2 = cashCurr2Paid.toDoubleOrNull() ?: 0.0
        val creditPaid1 = creditCurr1Paid.toDoubleOrNull() ?: 0.0
        val creditPaid2 = creditCurr2Paid.toDoubleOrNull() ?: 0.0
        val debitPaid1 = debitCurr1Paid.toDoubleOrNull() ?: 0.0
        val debitPaid2 = debitCurr2Paid.toDoubleOrNull() ?: 0.0

        val total = cashPaid1 + creditPaid1 + debitPaid1
        val total2 = cashPaid2 + creditPaid2 + debitPaid2
        val totalPaid1 = total + total2.div(rate)
        val totalPaid2 = totalPaid1.times(rate)

        totalCurr1Paid = String.format(
            "%.${curr1Decimal}f",
            totalPaid1
        )
        totalCurr2Paid = String.format(
            "%.${curr2Decimal}f",
            totalPaid2
        )
        changeCurr1 = String.format(
            "%.${curr1Decimal}f",
            (cashCurr1Total.toDoubleOrNull() ?: 0.0) - totalPaid1
        )
        changeCurr2 = String.format(
            "%.${curr2Decimal}f",
            (cashCurr2Total.toDoubleOrNull() ?: 0.0) - totalPaid2
        )
    }

    calculateTotal()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            Text(
                curr1State,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(.25f)
                    .wrapContentHeight(
                        align = Alignment.CenterVertically
                    )
                    .wrapContentWidth(align = Alignment.CenterHorizontally),
                color = SettingsModel.textColor
            )

            Text(
                curr2State,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(.25f)
                    .wrapContentHeight(
                        align = Alignment.CenterVertically
                    )
                    .wrapContentWidth(align = Alignment.CenterHorizontally),
                color = SettingsModel.textColor
            )

            Text(
                curr1State,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(.25f)
                    .wrapContentHeight(
                        align = Alignment.CenterVertically
                    )
                    .wrapContentWidth(align = Alignment.CenterHorizontally),
                color = SettingsModel.textColor
            )

            Text(
                curr2State,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(.25f)
                    .wrapContentHeight(
                        align = Alignment.CenterVertically
                    )
                    .wrapContentWidth(align = Alignment.CenterHorizontally),
                color = SettingsModel.textColor
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            OutlinedTextField(value = cashCurr1Paid,
                onValueChange = {
                    cashCurr1Paid = Utils.getDoubleValue(
                        it,
                        cashCurr1Paid
                    )
                    calculateTotal()
                },
                label = {
                    Text(text = "Cash")
                },
                placeholder = {
                    Text(text = "0.0")
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(.25f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { cashCurr2FocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )

            OutlinedTextField(value = cashCurr2Paid,
                onValueChange = {
                    cashCurr2Paid = Utils.getDoubleValue(
                        it,
                        cashCurr2Paid
                    )
                    calculateTotal()
                },
                label = {
                    Text(text = "Cash")
                },
                placeholder = {
                    Text(text = "0.0")
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(.25f)
                    .focusRequester(
                        cashCurr2FocusRequester
                    ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { cashTotal1FocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )
            OutlinedTextField(
                value = cashCurr1Total,
                onValueChange = {
                    cashCurr1Total = Utils.getDoubleValue(
                        it,
                        cashCurr1Total
                    )
                },
                readOnly = true,
                label = {
                    Text(text = "Total")
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(.25f)
                    .focusRequester(
                        cashTotal1FocusRequester
                    ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { cashTotal2FocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )

            OutlinedTextField(
                value = cashCurr2Total,
                onValueChange = {
                    cashCurr2Total = Utils.getDoubleValue(
                        it,
                        cashCurr2Total
                    )
                },
                readOnly = true,
                label = {
                    Text(text = "Total")
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(.25f)
                    .focusRequester(
                        cashTotal2FocusRequester
                    ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { creditCurr1PaidFocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            OutlinedTextField(value = creditCurr1Paid,
                onValueChange = {
                    creditCurr1Paid = Utils.getDoubleValue(
                        it,
                        creditCurr1Paid
                    )
                    calculateTotal()
                },
                label = {
                    Text(text = "Credit")
                },
                placeholder = {
                    Text(text = "0.0")
                },
                modifier = Modifier
                    .weight(.25f)
                    .fillMaxHeight()
                    .focusRequester(
                        creditCurr1PaidFocusRequester
                    ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { creditCurr2PaidFocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )

            OutlinedTextField(value = creditCurr2Paid,
                onValueChange = {
                    creditCurr2Paid = Utils.getDoubleValue(
                        it,
                        creditCurr2Paid
                    )
                    calculateTotal()
                },
                label = {
                    Text(text = "Credit")
                },
                placeholder = {
                    Text(text = "0.0")
                },
                modifier = Modifier
                    .weight(.25f)
                    .fillMaxHeight()
                    .focusRequester(
                        creditCurr2PaidFocusRequester
                    ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { creditCurr1TotalFocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )
            OutlinedTextField(
                value = totalCurr1Paid,
                onValueChange = {
                    totalCurr1Paid = Utils.getDoubleValue(
                        it,
                        totalCurr1Paid
                    )
                },
                readOnly = true,
                label = {
                    Text(text = "Paid")
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(.25f)
                    .focusRequester(
                        creditCurr1TotalFocusRequester
                    ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { creditCurr2TotalFocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )

            OutlinedTextField(
                value = totalCurr2Paid,
                onValueChange = {
                    totalCurr2Paid = Utils.getDoubleValue(
                        it,
                        totalCurr2Paid
                    )
                },
                readOnly = true,
                label = {
                    Text(text = "Paid")
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(.25f)
                    .focusRequester(
                        creditCurr2TotalFocusRequester
                    ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { debitCurr1PaidFocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            OutlinedTextField(value = debitCurr1Paid,
                onValueChange = {
                    debitCurr1Paid = Utils.getDoubleValue(
                        it,
                        debitCurr1Paid
                    )
                    calculateTotal()
                },
                label = {
                    Text(text = "Debit")
                },
                placeholder = {
                    Text(text = "0.0")
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(.25f)
                    .focusRequester(
                        debitCurr1PaidFocusRequester
                    ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { debitCurr2PaidFocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )

            OutlinedTextField(value = debitCurr2Paid,
                onValueChange = {
                    debitCurr2Paid = Utils.getDoubleValue(
                        it,
                        debitCurr2Paid
                    )
                    calculateTotal()
                },
                label = {
                    Text(text = "Debit")
                },
                placeholder = {
                    Text(text = "0.0")
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(.25f)
                    .focusRequester(
                        debitCurr2PaidFocusRequester
                    ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { debitCurr1TotalFocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )
            OutlinedTextField(
                value = changeCurr1,
                onValueChange = {
                    changeCurr1 = Utils.getDoubleValue(
                        it,
                        changeCurr1
                    )
                },
                readOnly = true,
                label = {
                    Text(text = "Change")
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(.25f)
                    .focusRequester(
                        debitCurr1TotalFocusRequester
                    ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { debitCurr2TotalFocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )

            OutlinedTextField(
                value = changeCurr2,
                onValueChange = {
                    changeCurr2 = Utils.getDoubleValue(
                        it,
                        changeCurr2
                    )
                },
                readOnly = true,
                label = {
                    Text(text = "Change")
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(.25f)
                    .focusRequester(
                        debitCurr2TotalFocusRequester
                    ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(
                    0.dp,
                    15.dp,
                    0.dp,
                    0.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UIButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = "Save & Print Order",
                shape = RoundedCornerShape(15.dp)
            ) {
                posReceipt.posReceiptCash = cashTotalPaid1.toDoubleOrNull() ?: 0.0
                posReceipt.posReceiptCashs = cashTotalPaid2.toDoubleOrNull() ?: 0.0
                posReceipt.posReceiptDebit = debitCurr1Paid.toDoubleOrNull() ?: 0.0
                posReceipt.posReceiptDebits = debitCurr2Paid.toDoubleOrNull() ?: 0.0
                posReceipt.posReceiptCredit = creditCurr1Paid.toDoubleOrNull() ?: 0.0
                posReceipt.posReceiptCredits = creditCurr2Paid.toDoubleOrNull() ?: 0.0
                onSave.invoke(
                    changeCurr1.toDoubleOrNull() ?: 0.0,
                    posReceipt
                )
            }

            UIButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = "Finish & Print",
                shape = RoundedCornerShape(15.dp)
            ) {
                posReceipt.posReceiptCash = cashTotalPaid1.toDoubleOrNull() ?: 0.0
                posReceipt.posReceiptCashs = cashTotalPaid2.toDoubleOrNull() ?: 0.0
                posReceipt.posReceiptDebit = debitCurr1Paid.toDoubleOrNull() ?: 0.0
                posReceipt.posReceiptDebits = debitCurr2Paid.toDoubleOrNull() ?: 0.0
                posReceipt.posReceiptCredit = creditCurr1Paid.toDoubleOrNull() ?: 0.0
                posReceipt.posReceiptCredits = creditCurr2Paid.toDoubleOrNull() ?: 0.0
                onFinish.invoke(
                    changeCurr1.toDoubleOrNull() ?: 0.0,
                    posReceipt
                )
            }
        }
    }
}