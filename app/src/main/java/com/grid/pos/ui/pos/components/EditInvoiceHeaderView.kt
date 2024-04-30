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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.Blue
import com.grid.pos.ui.theme.GridPOSTheme

@Composable
fun EditInvoiceHeaderView(
    modifier: Modifier = Modifier
) {
    val rDiscount1FocusRequester = remember { FocusRequester() }
    val rDiscount2FocusRequester = remember { FocusRequester() }
    val discount1FocusRequester = remember { FocusRequester() }
    val discount2FocusRequester = remember { FocusRequester() }
    val clientExtraNameFocusRequester = remember { FocusRequester() }
    val itemNoteFocusRequester = remember { FocusRequester() }
    val invoiceNoteFocusRequester = remember { FocusRequester() }
    var price by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf(1) }
    var rDiscount1 by remember { mutableStateOf("") }
    var rDiscount2 by remember { mutableStateOf("") }
    var discount1 by remember { mutableStateOf("") }
    var discount2 by remember { mutableStateOf("") }
    var clientExtraName by remember { mutableStateOf("") }
    var itemNote by remember { mutableStateOf("") }
    var invoiceNote by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                modifier = Modifier.weight(1f),
                label = { Text("Price") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { rDiscount1FocusRequester.requestFocus() })
            )
            OutlinedTextField(
                value = qty.toString(),
                onValueChange = {
                    qty = it.toInt()
                },
                modifier = Modifier.weight(1f),
                readOnly = true,
                label = {
                    if (true) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = Color.Transparent)
                        ) {
                            Text(
                                text = "Qty",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
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
                        Icon(Icons.Default.Add, contentDescription = "Increase quantity")
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { if (qty > 1) qty-- }) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease quantity")
                    }
                }
            )
        }
        Text(
            text = "Discount",
            style = TextStyle(
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            color = Color.Black
        )
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(.7f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "R. disc",
                        modifier = Modifier
                            .width(60.dp)
                            .height(60.dp)
                            .wrapContentHeight(align = Alignment.CenterVertically),
                        color = Color.Black
                    )
                    OutlinedTextField(
                        value = rDiscount1,
                        onValueChange = { rDiscount1 = it },
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
                        keyboardActions = KeyboardActions(onNext = { rDiscount2FocusRequester.requestFocus() })
                    )
                    OutlinedTextField(
                        value = rDiscount2,
                        onValueChange = { rDiscount2 = it },
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
                        keyboardActions = KeyboardActions(onNext = { discount1FocusRequester.requestFocus() })
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
                            .wrapContentHeight(align = Alignment.CenterVertically),
                        color = Color.Black
                    )
                    OutlinedTextField(
                        value = discount1,
                        onValueChange = { discount1 = it },
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
                        keyboardActions = KeyboardActions(onNext = { discount2FocusRequester.requestFocus() })
                    )
                    OutlinedTextField(
                        value = discount2,
                        onValueChange = { discount2 = it },
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
                        keyboardActions = KeyboardActions(onNext = { clientExtraNameFocusRequester.requestFocus() })
                    )
                }
            }
            TextButton(
                modifier = Modifier
                    .weight(.2f)
                    .height(110.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                shape = RoundedCornerShape(
                    topStart = 15.dp,
                    topEnd = 15.dp,
                    bottomEnd = 15.dp,
                    bottomStart = 15.dp
                ),
                onClick = {

                }
            ) {
                Text("Void")
            }
        }


        UITextField(
            modifier = Modifier.padding(10.dp),
            defaultValue = clientExtraName,
            label = "Client Extra Name",
            maxLines = 2,
            focusRequester = clientExtraNameFocusRequester,
            onAction = { itemNoteFocusRequester.requestFocus() }
        ) {
            clientExtraName = it
        }

        UITextField(
            modifier = Modifier
                .padding(10.dp),
            defaultValue = itemNote,
            label = "Item Note",
            maxLines = 2,
            focusRequester = itemNoteFocusRequester,
            onAction = { invoiceNoteFocusRequester.requestFocus() }
        ) {
            itemNote = it
        }

        UITextField(
            modifier = Modifier
                .padding(10.dp),
            defaultValue = invoiceNote,
            label = "Invoice Note",
            maxLines = 2,
            focusRequester = itemNoteFocusRequester,
            imeAction = ImeAction.Done
        ) {
            invoiceNote = it
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ElevatedButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                onClick = {

                }
            ) {
                Text("Clear")
            }

            ElevatedButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                onClick = {

                }
            ) {
                Text("Close")
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