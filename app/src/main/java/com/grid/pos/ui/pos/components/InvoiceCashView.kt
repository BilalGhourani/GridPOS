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
    val currency = posState.currency
    val posReceipt = posState.posReceipt.copy()

    val curr1Decimal = currency.currencyName1Dec
    val curr2Decimal = currency.currencyName2Dec
    val rate = currency.currencyRate
    val netTotal = invoiceHeader.invoiceHeadGrossAmount
    val cashTotalPaid1 = String.format("%.${curr1Decimal}f", netTotal)
    val cashTotalPaid2 = String.format(
        "%.${curr1Decimal}f", (netTotal).times(rate)
    )

    val curr1State by remember { mutableStateOf(currency.currencyName1 ?: "") }
    val curr2State by remember { mutableStateOf(currency.currencyName2 ?: "") }
    var cashCurr1Paid by remember { mutableStateOf("") }
    var cashCurr2Paid by remember { mutableStateOf("") }
    var cashCurr1Total by remember { mutableStateOf(cashTotalPaid1) }
    var cashCurr2Total by remember { mutableStateOf(cashTotalPaid2) }
    var creditCurr1Paid by remember { mutableStateOf("") }
    var creditCurr2Paid by remember { mutableStateOf("") }
    var creditCurr1Total by remember { mutableStateOf("") }
    var creditCurr2Total by remember { mutableStateOf("") }
    var debitCurr1Paid by remember { mutableStateOf("") }
    var debitCurr2Paid by remember { mutableStateOf("") }
    var debitCurr1Total by remember { mutableStateOf("") }
    var debitCurr2Total by remember { mutableStateOf("") }

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
                "", modifier = Modifier
                    .fillMaxHeight()
                    .weight(.10f)
            )

            Text(
                curr1State, modifier = Modifier
                    .fillMaxHeight()
                    .weight(.20f)
                    .wrapContentHeight(
                        align = Alignment.CenterVertically
                    )
                    .wrapContentWidth(align = Alignment.CenterHorizontally),
                color = SettingsModel.textColor
            )

            Text(
                curr2State, modifier = Modifier
                    .fillMaxHeight()
                    .weight(.20f)
                    .wrapContentHeight(
                        align = Alignment.CenterVertically
                    )
                    .wrapContentWidth(align = Alignment.CenterHorizontally),
                color = SettingsModel.textColor
            )

            Text(
                "", modifier = Modifier
                    .fillMaxHeight()
                    .weight(.25f)
            )

            Text(
                "", modifier = Modifier
                    .fillMaxHeight()
                    .weight(.25f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                "Cash", modifier = Modifier
                    .weight(.10f)
                    .fillMaxHeight()
                    .wrapContentHeight(
                        align = Alignment.CenterVertically
                    ), color = SettingsModel.textColor
            )
            OutlinedTextField(value = cashCurr1Paid, onValueChange = {
                cashCurr1Paid = Utils.getDoubleValue(it, cashCurr1Paid)
                val cashCurrTotal1 = if (cashCurr1Paid.isEmpty()) 0.0 else cashCurr1Paid.toDouble()
                val cashCurrTotal2 = if (cashCurr2Paid.isEmpty()) 0.0 else cashCurr2Paid.toDouble()
                creditCurr1Total = String.format(
                    "%.${curr1Decimal}f", cashCurrTotal1 + (cashCurrTotal2 / rate)
                )

                creditCurr2Total = String.format(
                    "%.${curr2Decimal}f", (cashCurrTotal1 * rate) + cashCurrTotal2
                )

                debitCurr1Total = String.format(
                    "%.${curr1Decimal}f", cashTotalPaid1.toDouble() - creditCurr1Total.toDouble()
                )

                debitCurr2Total = String.format(
                    "%.${curr2Decimal}f", cashTotalPaid2.toDouble() - creditCurr2Total.toDouble()
                )
            }, placeholder = {
                Text(text = "0.0")
            }, modifier = Modifier
                .fillMaxHeight()
                .weight(.20f), keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
            ), keyboardActions = KeyboardActions(
                onNext = { cashCurr2FocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )

            OutlinedTextField(value = cashCurr2Paid, onValueChange = {
                cashCurr2Paid = Utils.getDoubleValue(it, cashCurr2Paid)
                val cashCurrTotal1 = if (cashCurr1Paid.isEmpty()) 0.0 else cashCurr1Paid.toDouble()
                val cashCurrTotal2 = if (cashCurr2Paid.isEmpty()) 0.0 else cashCurr2Paid.toDouble()
                creditCurr1Total = String.format(
                    "%.${curr1Decimal}f", cashCurrTotal1 + (cashCurrTotal2 / rate)
                )

                creditCurr2Total = String.format(
                    "%.${curr2Decimal}f", (cashCurrTotal1 * rate) + cashCurrTotal2
                )

                debitCurr2Total = String.format(
                    "%.${curr2Decimal}f", cashTotalPaid2.toDouble() - creditCurr2Total.toDouble()
                )

                debitCurr1Total = String.format(
                    "%.${curr1Decimal}f", cashTotalPaid1.toDouble() - creditCurr1Total.toDouble()
                )

            }, placeholder = {
                Text(text = "0.0")
            }, modifier = Modifier
                .fillMaxHeight()
                .weight(.20f)
                .focusRequester(
                    cashCurr2FocusRequester
                ), keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
            ), keyboardActions = KeyboardActions(
                onNext = { cashTotal1FocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )
            OutlinedTextField(
                value = cashCurr1Total,
                onValueChange = { cashCurr1Total = Utils.getDoubleValue(it, cashCurr1Total) },
                placeholder = {
                    Text(text = "Total")
                }, modifier = Modifier
                    .fillMaxHeight()
                    .weight(.25f)
                    .focusRequester(
                        cashTotal1FocusRequester
                    ), keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                ), keyboardActions = KeyboardActions(
                    onNext = { cashTotal2FocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )

            OutlinedTextField(
                value = cashCurr2Total,
                onValueChange = { cashCurr2Total = Utils.getDoubleValue(it, cashCurr2Total) },
                placeholder = {
                    Text(text = "Total2")
                }, modifier = Modifier
                    .fillMaxHeight()
                    .weight(.25f)
                    .focusRequester(
                        cashTotal2FocusRequester
                    ), keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                ), keyboardActions = KeyboardActions(
                    onNext = { creditCurr1PaidFocusRequester.requestFocus() }),
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
            Text(
                "Credit", modifier = Modifier
                    .weight(.10f)
                    .fillMaxHeight()
                    .wrapContentHeight(
                        align = Alignment.CenterVertically
                    ), color = SettingsModel.textColor
            )
            OutlinedTextField(
                value = creditCurr1Paid,
                onValueChange = { creditCurr1Paid = Utils.getDoubleValue(it, creditCurr1Paid) },
                placeholder = {
                    Text(text = "0.0")
                }, modifier = Modifier
                    .weight(.20f)
                    .fillMaxHeight()
                    .focusRequester(
                        creditCurr1PaidFocusRequester
                    ), keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                ), keyboardActions = KeyboardActions(
                    onNext = { creditCurr2PaidFocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )

            OutlinedTextField(
                value = creditCurr2Paid,
                onValueChange = { creditCurr2Paid = Utils.getDoubleValue(it, creditCurr2Paid) },
                placeholder = {
                    Text(text = "0.0")
                }, modifier = Modifier
                    .weight(.20f)
                    .fillMaxHeight()
                    .focusRequester(
                        creditCurr2PaidFocusRequester
                    ), keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                ), keyboardActions = KeyboardActions(
                    onNext = { creditCurr1TotalFocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )
            OutlinedTextField(
                value = creditCurr1Total,
                onValueChange = { creditCurr1Total = Utils.getDoubleValue(it, creditCurr1Total) },
                placeholder = {
                    Text(text = "Paid")
                }, modifier = Modifier
                    .fillMaxHeight()
                    .weight(.25f)
                    .focusRequester(
                        creditCurr1TotalFocusRequester
                    ), keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                ), keyboardActions = KeyboardActions(
                    onNext = { creditCurr2TotalFocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )

            OutlinedTextField(
                value = creditCurr2Total,
                onValueChange = { creditCurr2Total = Utils.getDoubleValue(it, creditCurr2Total) },
                placeholder = {
                    Text(text = "Paid")
                }, modifier = Modifier
                    .fillMaxHeight()
                    .weight(.25f)
                    .focusRequester(
                        creditCurr2TotalFocusRequester
                    ), keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                ), keyboardActions = KeyboardActions(
                    onNext = { debitCurr1PaidFocusRequester.requestFocus() }),
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
            Text(
                "Debit", modifier = Modifier
                    .weight(.10f)
                    .fillMaxHeight()
                    .wrapContentHeight(
                        align = Alignment.CenterVertically
                    ), color = SettingsModel.textColor
            )
            OutlinedTextField(
                value = debitCurr1Paid,
                onValueChange = { debitCurr1Paid = Utils.getDoubleValue(it, debitCurr1Paid) },
                placeholder = {
                    Text(text = "0.0")
                }, modifier = Modifier
                    .fillMaxHeight()
                    .weight(.20f)
                    .focusRequester(
                        debitCurr1PaidFocusRequester
                    ), keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                ), keyboardActions = KeyboardActions(
                    onNext = { debitCurr2PaidFocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )

            OutlinedTextField(
                value = debitCurr2Paid,
                onValueChange = { debitCurr2Paid = Utils.getDoubleValue(it, debitCurr2Paid) },
                placeholder = {
                    Text(text = "0.0")
                }, modifier = Modifier
                    .fillMaxHeight()
                    .weight(.20f)
                    .focusRequester(
                        debitCurr2PaidFocusRequester
                    ), keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                ), keyboardActions = KeyboardActions(
                    onNext = { debitCurr1TotalFocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )
            OutlinedTextField(
                value = debitCurr1Total,
                onValueChange = { debitCurr1Total = Utils.getDoubleValue(it, debitCurr1Total) },
                placeholder = {
                    Text(text = "Change")
                }, modifier = Modifier
                    .fillMaxHeight()
                    .weight(.25f)
                    .focusRequester(
                        debitCurr1TotalFocusRequester
                    ), keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                ), keyboardActions = KeyboardActions(
                    onNext = { debitCurr2TotalFocusRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )

            OutlinedTextField(
                value = debitCurr2Total,
                onValueChange = { debitCurr2Total = Utils.getDoubleValue(it, debitCurr2Total) },
                placeholder = {
                    Text(text = "Change")
                }, modifier = Modifier
                    .fillMaxHeight()
                    .weight(.25f)
                    .focusRequester(
                        debitCurr2TotalFocusRequester
                    ), keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                ), keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
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
                .padding(0.dp, 15.dp, 0.dp, 0.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UIButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(), text = "Save & Print Order",
                shape = RoundedCornerShape(15.dp)
            ) {
                posReceipt.posReceiptCashAmount = cashTotalPaid1.toDoubleOrNull() ?: 0.0
                posReceipt.posReceiptCashAmount2 = cashTotalPaid2.toDoubleOrNull() ?: 0.0
                posReceipt.posReceiptDebitAmount = debitCurr1Paid.toDoubleOrNull() ?: 0.0
                posReceipt.posReceiptDebitAmount2 = debitCurr2Paid.toDoubleOrNull() ?: 0.0
                posReceipt.posReceiptCreditAmount = creditCurr1Paid.toDoubleOrNull() ?: 0.0
                posReceipt.posReceiptCreditAmount2 = creditCurr2Paid.toDoubleOrNull() ?: 0.0
                onSave.invoke(debitCurr1Total.toDoubleOrNull() ?: 0.0, posReceipt)
            }

            UIButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(), text = "Finish & Print",
                shape = RoundedCornerShape(15.dp)
            ) {
                posReceipt.posReceiptCashAmount = cashTotalPaid1.toDoubleOrNull() ?: 0.0
                posReceipt.posReceiptCashAmount2 = cashTotalPaid2.toDoubleOrNull() ?: 0.0
                posReceipt.posReceiptDebitAmount = debitCurr1Paid.toDoubleOrNull() ?: 0.0
                posReceipt.posReceiptDebitAmount2 = debitCurr2Paid.toDoubleOrNull() ?: 0.0
                posReceipt.posReceiptCreditAmount = creditCurr1Paid.toDoubleOrNull() ?: 0.0
                posReceipt.posReceiptCreditAmount2 = creditCurr2Paid.toDoubleOrNull() ?: 0.0
                onFinish.invoke(debitCurr1Total.toDoubleOrNull() ?: 0.0, posReceipt)
            }
        }
    }
}