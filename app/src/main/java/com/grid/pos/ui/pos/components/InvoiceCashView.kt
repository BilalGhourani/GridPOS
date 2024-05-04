package com.grid.pos.ui.pos.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.UIButton

@Composable
fun InvoiceCashView(
    modifier: Modifier,
    onSave: () -> Unit = {},
    onFinish: () -> Unit = {},
) {
    val cashTotalFocusRequester = remember { FocusRequester() }
    val creditPaidFocusRequester = remember { FocusRequester() }
    val creditTotalFocusRequester = remember { FocusRequester() }
    val debitPaidFocusRequester = remember { FocusRequester() }
    val debitTotalFocusRequester = remember { FocusRequester() }

    var curr1State by remember { mutableStateOf("USD") }
    var curr2State by remember { mutableStateOf("LIRA") }
    var cashCurr1Paid by remember { mutableStateOf("") }
    var cashCurr2Paid by remember { mutableStateOf("") }
    var cashCurr1Total by remember { mutableStateOf("") }
    var cashCurr2Total by remember { mutableStateOf("") }
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
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "",
                modifier = Modifier
                    .weight(.2f)
            )

            Text(
                curr1State,
                modifier = Modifier
                    .weight(.2f)
                    .wrapContentHeight(align = Alignment.CenterVertically)
                    .wrapContentWidth(align = Alignment.CenterHorizontally),
                color = SettingsModel.textColor
            )

            Text(
                curr2State,
                modifier = Modifier
                    .weight(.2f)
                    .wrapContentHeight(align = Alignment.CenterVertically)
                    .wrapContentWidth(align = Alignment.CenterHorizontally),
                color = SettingsModel.textColor
            )

            Text(
                "",
                modifier = Modifier
                    .weight(.2f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Cash",
                modifier = Modifier
                    .weight(.2f)
                    .height(60.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically),
                color = SettingsModel.textColor
            )
            OutlinedTextField(
                value = cashCurr1Paid,
                onValueChange = { cashCurr1Paid = it },
                placeholder = {
                    Text(text = "0.0")
                },
                modifier = Modifier
                    .padding(top = 8.dp)
                    .weight(.2f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { cashTotalFocusRequester.requestFocus() })
            )

            OutlinedTextField(
                value = cashCurr2Paid,
                onValueChange = { cashCurr2Paid = it },
                placeholder = {
                    Text(text = "0.0")
                },
                modifier = Modifier
                    .padding(top = 8.dp)
                    .weight(.2f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { cashTotalFocusRequester.requestFocus() })
            )
            OutlinedTextField(
                value = cashCurr1Total,
                onValueChange = { cashCurr1Total = it },
                label = {
                    Text(text = "Total", color = SettingsModel.textColor)
                },
                placeholder = {
                    Text(text = "0.0")
                },
                modifier = Modifier
                    .weight(.2f)
                    .focusRequester(cashTotalFocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { creditPaidFocusRequester.requestFocus() })
            )

            OutlinedTextField(
                value = cashCurr2Total,
                onValueChange = { cashCurr2Total = it },
                label = {
                    Text(text = "Total2", color = SettingsModel.textColor)
                },
                placeholder = {
                    Text(text = "0.0")
                },
                modifier = Modifier
                    .weight(.2f)
                    .focusRequester(cashTotalFocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { creditPaidFocusRequester.requestFocus() })
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Credit",
                modifier = Modifier
                    .weight(.2f)
                    .height(60.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically),
                color = SettingsModel.textColor
            )
            OutlinedTextField(
                value = creditCurr1Paid,
                onValueChange = { creditCurr1Paid = it },
                placeholder = {
                    Text(text = "0.0")
                },
                modifier = Modifier
                    .weight(.2f)
                    .padding(top = 8.dp)
                    .focusRequester(creditPaidFocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { creditTotalFocusRequester.requestFocus() })
            )

            OutlinedTextField(
                value = creditCurr2Paid,
                onValueChange = { creditCurr2Paid = it },
                placeholder = {
                    Text(text = "0.0")
                },
                modifier = Modifier
                    .weight(.2f)
                    .padding(top = 8.dp)
                    .focusRequester(creditPaidFocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { creditTotalFocusRequester.requestFocus() })
            )
            OutlinedTextField(
                value = creditCurr1Total,
                onValueChange = { creditCurr1Total = it },
                label = {
                    Text(text = "Paid", color = SettingsModel.textColor)
                },
                placeholder = {
                    Text(text = "0.0")
                },
                modifier = Modifier
                    .weight(.2f)
                    .focusRequester(creditTotalFocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { debitPaidFocusRequester.requestFocus() })
            )

            OutlinedTextField(
                value = creditCurr2Total,
                onValueChange = { creditCurr2Total = it },
                label = {
                    Text(text = "Paid", color = SettingsModel.textColor)
                },
                placeholder = {
                    Text(text = "0.0")
                },
                modifier = Modifier
                    .weight(.2f)
                    .focusRequester(creditTotalFocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { debitPaidFocusRequester.requestFocus() })
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Debit",
                modifier = Modifier
                    .weight(.2f)
                    .height(60.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically),
                color = SettingsModel.textColor
            )
            OutlinedTextField(
                value = debitCurr1Paid,
                onValueChange = { debitCurr1Paid = it },
                placeholder = {
                    Text(text = "0.0")
                },
                modifier = Modifier
                    .weight(.2f)
                    .padding(top = 8.dp)
                    .focusRequester(debitPaidFocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { debitTotalFocusRequester.requestFocus() })
            )

            OutlinedTextField(
                value = debitCurr2Paid,
                onValueChange = { debitCurr2Paid = it },
                placeholder = {
                    Text(text = "0.0")
                },
                modifier = Modifier
                    .weight(.2f)
                    .padding(top = 8.dp)
                    .focusRequester(debitPaidFocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { debitTotalFocusRequester.requestFocus() })
            )
            OutlinedTextField(
                value = debitCurr1Total,
                onValueChange = { debitCurr1Total = it },
                label = {
                    Text(text = "Change", color = SettingsModel.textColor)
                },
                placeholder = {
                    Text(text = "0.0")
                },
                modifier = Modifier
                    .weight(.2f)
                    .focusRequester(debitTotalFocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { creditPaidFocusRequester.requestFocus() })
            )

            OutlinedTextField(
                value = debitCurr2Total,
                onValueChange = { debitCurr2Total = it },
                label = {
                    Text(text = "Change", color = SettingsModel.textColor)
                },
                placeholder = {
                    Text(text = "0.0")
                },
                modifier = Modifier
                    .weight(.2f)
                    .focusRequester(debitTotalFocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { creditPaidFocusRequester.requestFocus() })
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
                    .fillMaxHeight(),
                text = "Save & Print Order",
                shape = RoundedCornerShape(15.dp)
            ) {
                onSave.invoke()
            }

            UIButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = "Finish & Print",
                shape = RoundedCornerShape(15.dp)
            ) {
                onFinish.invoke()
            }
        }
    }
}