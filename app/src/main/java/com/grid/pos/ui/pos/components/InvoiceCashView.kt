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
import androidx.compose.foundation.rememberScrollState
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
import com.grid.pos.ui.theme.Blue

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

    var cashPaid by remember { mutableStateOf("0.0") }
    var cashTotal by remember { mutableStateOf("0.0") }
    var creditPaid by remember { mutableStateOf("0.0") }
    var creditTotal by remember { mutableStateOf("0.0") }
    var debitPaid by remember { mutableStateOf("0.0") }
    var debitTotal by remember { mutableStateOf("0.0") }

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
                "Cash",
                modifier = Modifier
                    .weight(.2f)
                    .height(60.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically),
                color = Color.Black
            )
            OutlinedTextField(
                value = cashPaid,
                onValueChange = { cashPaid = it },
                label = {
                    Text(text = "")
                },
                modifier = Modifier
                    .weight(.4f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { cashTotalFocusRequester.requestFocus() })
            )
            OutlinedTextField(
                value = cashTotal,
                onValueChange = { cashTotal = it },
                label = {
                    Text(text = "Total")
                },
                modifier = Modifier
                    .weight(.4f)
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
                color = Color.Black
            )
            OutlinedTextField(
                value = creditPaid,
                onValueChange = { creditPaid = it },
                label = {
                    Text(text = "")
                },
                modifier = Modifier
                    .weight(.4f)
                    .focusRequester(creditPaidFocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { creditTotalFocusRequester.requestFocus() })
            )
            OutlinedTextField(
                value = creditTotal,
                onValueChange = { creditTotal = it },
                label = {
                    Text(text = "Paid")
                },
                modifier = Modifier
                    .weight(.4f)
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
                color = Color.Black
            )
            OutlinedTextField(
                value = debitPaid,
                onValueChange = { debitPaid = it },
                label = {
                    Text(text = "")
                },
                modifier = Modifier
                    .weight(.4f)
                    .focusRequester(debitPaidFocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { debitTotalFocusRequester.requestFocus() })
            )
            OutlinedTextField(
                value = debitTotal,
                onValueChange = { debitTotal = it },
                label = {
                    Text(text = "Change")
                },
                modifier = Modifier
                    .weight(.4f)
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
                .height(60.dp)
                .padding(0.dp, 15.dp, 0.dp, 0.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ElevatedButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                onClick = {
                    onSave.invoke()
                }
            ) {
                Text("Save & Print Order")
            }

            ElevatedButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                onClick = {
                    onFinish.invoke()
                }
            ) {
                Text("Finish & Print")
            }
        }
    }
}