package com.grid.pos.ui.purchase

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.grid.pos.data.purchaseHeader.PurchaseHeader
import com.grid.pos.model.DivisionModel
import com.grid.pos.model.PurchaseItemModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.TransactionTypeModel
import com.grid.pos.ui.common.EditableDateInputField
import com.grid.pos.ui.common.SearchableDropdownMenuEx
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.pos.POSUtils
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun EditPurchaseItemView(
    modifier: Modifier = Modifier,
    purchaseItems: MutableList<PurchaseItemModel>,
    purHeader: PurchaseHeader,
    itemIndex: Int = 0,
    state: PurchaseState,
    viewModel: PurchaseViewModel,
    triggerOnSave: Boolean = false,
    onSave: (PurchaseHeader, PurchaseItemModel) -> Unit = { _, _ -> }
) {
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val rDiscount1FocusRequester = remember { FocusRequester() }
    val rDiscount2FocusRequester = remember { FocusRequester() }
    val discount1FocusRequester = remember { FocusRequester() }
    val discount2FocusRequester = remember { FocusRequester() }
    val itemExtraNameFocusRequester = remember { FocusRequester() }
    val itemNoteFocusRequester = remember { FocusRequester() }
    val purchaseNoteFocusRequester = remember { FocusRequester() }
    val purchaseHeadDateRequester = remember { FocusRequester() }
    val purchaseHeadValueDateRequester = remember { FocusRequester() }
    val purchaseHeadTransNoRequester = remember { FocusRequester() }
    val clientExtraNameRequester = remember { FocusRequester() }
    val taxFocusRequester = remember { FocusRequester() }
    val tax1FocusRequester = remember { FocusRequester() }
    val tax2FocusRequester = remember { FocusRequester() }

    var purchaseHeader = purHeader.copy()
    val purchaseItemModel = purchaseItems[itemIndex]

    val curr1Decimal = SettingsModel.currentCurrency?.currencyName1Dec ?: 2
    val curr2Decimal = SettingsModel.currentCurrency?.currencyName2Dec ?: 2

    var isFillingDefaults by remember { mutableStateOf(true) } // Flag to track default filling

    var price by remember { mutableStateOf("") }
    var qty by remember { mutableIntStateOf(1) }
    var rDiscount1 by remember { mutableStateOf("") }
    var rDiscount2 by remember { mutableStateOf("") }
    var discount1 by remember { mutableStateOf("") }
    var discount2 by remember { mutableStateOf("") }
    var itemNote by remember { mutableStateOf("") }
    var purchaseNote by remember { mutableStateOf("") }
    var clientExtraName by remember { mutableStateOf("") }
    var purchaseItemDivState by remember { mutableStateOf("") }
    var purchaseHeadDateState by remember { mutableStateOf("") }
    var purchaseHeadValueDateState by remember { mutableStateOf("") }
    var purchaseHeadTtCodeState by remember { mutableStateOf("") }
    var purchaseHeadTransNoState by remember { mutableStateOf("") }
    var purchaseHeadUserState by remember { mutableStateOf("") }
    var taxState by remember { mutableStateOf("") }
    var tax1State by remember { mutableStateOf("") }
    var tax2State by remember { mutableStateOf("") }
    var isPercentageChanged by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isFillingDefaults = true
        price = POSUtils.formatDouble(
            purchaseItemModel.purchase.purchasePrice ?: 0.0,
            curr1Decimal
        )
        qty = purchaseItemModel.purchase.purchaseQty?.toInt() ?: 1
        rDiscount1 = purchaseItemModel.purchase.purchaseDisc?.toString()
            .takeIf { it != "0.0" } ?: ""
        rDiscount2 =
            purchaseItemModel.purchase.purchaseDiscAmt?.toString().takeIf { it != "0.0" } ?: ""
        discount1 = purchaseHeader.purchaseHeaderDisc?.toString().takeIf { it != "0.0" } ?: ""
        discount2 = purchaseHeader.purchaseHeaderDiscAmt?.toString().takeIf { it != "0.0" } ?: ""
        itemNote = purchaseItemModel.purchase.purchaseNote ?: ""
        purchaseNote = purchaseHeader.purchaseHeaderNote ?: ""
        clientExtraName = purchaseHeader.purchaseHeaderCashName ?: ""
        taxState = purchaseItemModel.purchase.purchaseVat?.toString().takeIf { it != "0.0" } ?: ""
        tax1State = purchaseItemModel.purchase.purchaseTax1?.toString().takeIf { it != "0.0" } ?: ""
        tax2State = purchaseItemModel.purchase.purchaseTax2?.toString().takeIf { it != "0.0" } ?: ""

        purchaseItemDivState = purchaseItemModel.purchase.purchaseDivName ?: ""

        purchaseHeadTtCodeState = purchaseHeader.purchaseHeaderTtCode ?: ""
        purchaseHeadTransNoState = purchaseHeader.purchaseHeaderTransNo ?: ""
        purchaseHeadDateState = purchaseHeader.purchaseHeaderDate ?: DateHelper.getDateInFormat(
            format = "YYYY-MM-DD HH:mm:ss.SSS"
        )
        purchaseHeadValueDateState = DateHelper.getDateInFormat(
            purchaseHeader.purchaseHeaderValueDate ?: Date(),
            "YYYY-MM-DD HH:mm:ss.SSS"
        )
        purchaseHeadUserState =
            purchaseHeader.purchaseHeaderUserStamp ?: SettingsModel.currentUser?.userUsername
                    ?: ""

        isFillingDefaults = false
    }

    fun calculateItemDiscount() {
        if (isFillingDefaults) return
        purchaseItemModel.purchase.purchasePrice = price.toDoubleOrNull() ?: 0.0
        purchaseItemModel.purchase.purchaseQty = qty.toDouble()
        purchaseItemModel.purchase.purchaseVat = taxState.toDoubleOrNull() ?: 0.0
        purchaseItemModel.purchase.purchaseTax1 = tax1State.toDoubleOrNull() ?: 0.0
        purchaseItemModel.purchase.purchaseTax2 = tax2State.toDoubleOrNull() ?: 0.0
        val itemDiscount = rDiscount1.toDoubleOrNull() ?: 0.0
        val itemDiscountAmount = rDiscount2.toDoubleOrNull() ?: 0.0
        val itemPrice = ((price.toDoubleOrNull() ?: 0.0).times(qty))
        if (isPercentageChanged) {
            val disc = itemPrice.times(itemDiscount.times(0.01))
            rDiscount2 = String.format(
                "%,.${curr2Decimal}f",
                disc
            )
            purchaseItemModel.purchase.purchaseDisc = itemDiscount
            purchaseItemModel.purchase.purchaseDiscAmt = disc
        } else {
            val disc =
                if (itemPrice == 0.0) 0.0 else (itemDiscountAmount.div(itemPrice)).times(100.0)
            rDiscount1 = String.format(
                "%,.${curr1Decimal}f",
                disc
            )
            purchaseItemModel.purchase.purchaseDisc = disc
            purchaseItemModel.purchase.purchaseDiscAmt = itemDiscountAmount
        }
    }

    fun calculatePurchaseDiscount() {
        if (isFillingDefaults) return
        purchaseItems[itemIndex] = purchaseItemModel
        purchaseHeader = POSUtils.refreshValues(
            purchaseItems,
            purchaseHeader
        )
        if (isPercentageChanged) {
            purchaseHeader.purchaseHeaderDisc = discount1.toDoubleOrNull() ?: 0.0
            purchaseHeader.purchaseHeaderDiscAmt = purchaseHeader.purchaseHeaderNetAmt?.times(
                purchaseHeader.purchaseHeaderDisc?.times(0.01) ?: 0.0
            )?:0.0
            discount2 = if (purchaseHeader.purchaseHeaderDiscAmt == 0.0) "" else String.format(
                "%,.${curr2Decimal}f",
                purchaseHeader.purchaseHeaderDiscAmt?:0.0
            )
        } else {
            purchaseHeader.purchaseHeaderDiscAmt = discount2.toDoubleOrNull() ?: 0.0
            if (purchaseHeader.purchaseHeaderNetAmt == 0.0) {
                purchaseHeader.purchaseHeaderDisc = 0.0
            } else {
                purchaseHeader.purchaseHeaderDisc =
                    (purchaseHeader.purchaseHeaderDiscAmt?.div(
                        purchaseHeader.purchaseHeaderNetAmt ?: 0.0
                    ))?.times(
                        100.0
                    )
            }
            discount1 = String.format(
                "%,.${curr1Decimal}f",
                purchaseHeader.purchaseHeaderDisc
            )
        }
    }

    fun backAndSave() {
        purchaseHeader.purchaseHeaderNote = purchaseNote.ifEmpty { null }
        purchaseHeader.purchaseHeaderCashName = clientExtraName.ifEmpty { null }
        purchaseHeader.purchaseHeaderDisc = discount1.toDoubleOrNull() ?: 0.0
        purchaseHeader.purchaseHeaderDiscAmt = discount2.toDoubleOrNull() ?: 0.0

        purchaseHeader.purchaseHeaderDate = purchaseHeadDateState
        purchaseHeader.purchaseHeaderValueDate =
            DateHelper.getDateFromString(purchaseHeadValueDateState, "yyyy-MM-dd HH:mm:ss.SSS")
        purchaseHeader.purchaseHeaderTtCode = purchaseHeadTtCodeState.ifEmpty { null }
        purchaseHeader.purchaseHeaderTransNo = purchaseHeadTransNoState.ifEmpty { null }

        purchaseItemModel.purchase.purchasePrice =
            price.toDoubleOrNull() ?: purchaseItemModel.purchaseItem.itemUnitPrice
        purchaseItemModel.purchase.purchaseVat = taxState.toDoubleOrNull() ?: 0.0
        purchaseItemModel.purchase.purchaseTax1 = tax1State.toDoubleOrNull() ?: 0.0
        purchaseItemModel.purchase.purchaseTax2 = tax2State.toDoubleOrNull() ?: 0.0
        purchaseItemModel.purchase.purchaseDisc = rDiscount1.toDoubleOrNull() ?: 0.0
        purchaseItemModel.purchase.purchaseDiscAmt = rDiscount2.toDoubleOrNull() ?: 0.0
        purchaseItemModel.purchase.purchaseQty = qty.toDouble()
        purchaseItemModel.purchase.purchaseNote = itemNote.ifEmpty { null }
        purchaseItems[itemIndex] = purchaseItemModel
        purchaseHeader = POSUtils.refreshValues(
            purchaseItems,
            purchaseHeader
        )

        onSave.invoke(
            purchaseHeader,
            purchaseItemModel
        )
    }

    BackHandler {
        backAndSave()
    }

    LaunchedEffect(key1 = triggerOnSave) {
        if (triggerOnSave) {
            backAndSave()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {})
            },
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
                    purchaseItemModel.purchase.purchasePrice = price.toDoubleOrNull() ?: 0.0
                    calculateItemDiscount()
                    calculatePurchaseDiscount()
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
                    qty = it.toIntOrNull() ?: qty
                },
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.weight(1f),
                /*readOnly = true,*/
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
                keyboardActions = KeyboardActions(onNext = { rDiscount1FocusRequester.requestFocus() }),
                leadingIcon = {
                    IconButton(onClick = {
                        qty++
                        purchaseItemModel.purchase.purchaseQty = qty.toDouble()
                        calculateItemDiscount()
                        calculatePurchaseDiscount()
                    }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Increase quantity",
                            tint = SettingsModel.buttonColor
                        )
                    }
                },
                trailingIcon = {
                    IconButton(onClick = {
                        if (qty > 1) qty--
                        purchaseItemModel.purchase.purchaseQty = qty.toDouble()
                        calculateItemDiscount()
                        calculatePurchaseDiscount()
                    }) {
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
            OutlinedTextField(value = rDiscount1,
                onValueChange = {
                    rDiscount1 = Utils.getDoubleValue(
                        it,
                        rDiscount1
                    )
                    isPercentageChanged = true
                },
                placeholder = {
                    Text(text = "0.0")
                },
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged {
                        if (!it.hasFocus) {
                            calculateItemDiscount()
                            calculatePurchaseDiscount()
                        }
                    }
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
            OutlinedTextField(value = rDiscount2,
                onValueChange = {
                    rDiscount2 = Utils.getDoubleValue(
                        it,
                        rDiscount2
                    )
                    isPercentageChanged = false
                },
                placeholder = {
                    Text(text = "0.0")
                },
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged {
                        if (!it.hasFocus) {
                            calculateItemDiscount()
                            calculatePurchaseDiscount()
                        }
                    }
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
            OutlinedTextField(value = discount1,
                onValueChange = {
                    discount1 = Utils.getDoubleValue(
                        it,
                        discount1
                    )
                    isPercentageChanged = true
                    purchaseHeader.purchaseHeaderDisc = discount1.toDoubleOrNull() ?: 0.0
                },
                placeholder = {
                    Text(text = "0.0")
                },
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged {
                        if (!it.hasFocus) {
                            calculateItemDiscount()
                            calculatePurchaseDiscount()
                        }
                    }
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
            OutlinedTextField(value = discount2,
                onValueChange = {
                    discount2 = Utils.getDoubleValue(
                        it,
                        discount2
                    )
                    isPercentageChanged = false
                    purchaseHeader.purchaseHeaderDiscAmt = discount2.toDoubleOrNull() ?: 0.0
                },
                placeholder = {
                    Text(text = "0.0")
                },
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged {
                        if (!it.hasFocus) {
                            calculateItemDiscount()
                            calculatePurchaseDiscount()
                        }
                    }
                    .focusRequester(discount2FocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { clientExtraNameRequester.requestFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Black,
                    focusedBorderColor = SettingsModel.buttonColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )
        }

        UITextField(modifier = Modifier.padding(horizontal = 10.dp),
            defaultValue = clientExtraName,
            label = "Client Exta Name",
            focusRequester = clientExtraNameRequester,
            onAction = {
                itemExtraNameFocusRequester.requestFocus()
            }) {
            clientExtraName = it
        }

        UITextField(modifier = Modifier.padding(horizontal = 10.dp),
            defaultValue = purchaseNote,
            label = "Purchase Note",
            focusRequester = purchaseNoteFocusRequester,
            onAction = {
                itemNoteFocusRequester.requestFocus()
            }) {
            purchaseNote = it
        }

        UITextField(modifier = Modifier.padding(horizontal = 10.dp),
            defaultValue = itemNote,
            label = "Item Note",
            focusRequester = itemNoteFocusRequester,
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
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (SettingsModel.showTax) {
                    UITextField(modifier = Modifier
                        .weight(1f)
                        .padding(end = 3.dp),
                        defaultValue = taxState,
                        label = "Tax",
                        keyboardType = KeyboardType.Decimal,
                        focusRequester = taxFocusRequester,
                        onAction = {
                            if (SettingsModel.showTax1) {
                                tax1FocusRequester.requestFocus()
                            } else if (SettingsModel.showTax2) {
                                tax2FocusRequester.requestFocus()
                            } else {
                                keyboardController?.hide()
                            }
                        },
                        onFocusChanged = {
                            if (!it.hasFocus) {
                                calculateItemDiscount()
                                calculatePurchaseDiscount()
                            }
                        }) {
                        taxState = Utils.getDoubleValue(
                            it,
                            taxState
                        )
                        purchaseItemModel.purchase.purchaseVat = taxState.toDoubleOrNull() ?: 0.0
                    }
                }
                if (SettingsModel.showTax1) {
                    UITextField(modifier = Modifier
                        .weight(1f)
                        .padding(end = 3.dp),
                        defaultValue = tax1State,
                        label = "Tax1",
                        keyboardType = KeyboardType.Decimal,
                        focusRequester = tax1FocusRequester,
                        onAction = {
                            if (SettingsModel.showTax2) {
                                tax2FocusRequester.requestFocus()
                            } else {
                                keyboardController?.hide()
                            }
                        },
                        onFocusChanged = {
                            if (!it.hasFocus) {
                                calculateItemDiscount()
                                calculatePurchaseDiscount()
                            }
                        }) {
                        tax1State = Utils.getDoubleValue(
                            it,
                            tax1State
                        )
                        purchaseItemModel.purchase.purchaseTax1 = tax1State.toDoubleOrNull() ?: 0.0
                    }
                }
                if (SettingsModel.showTax2) {
                    UITextField(modifier = Modifier
                        .weight(1f),
                        defaultValue = tax2State,
                        label = "Tax2",
                        keyboardType = KeyboardType.Decimal,
                        focusRequester = tax2FocusRequester,
                        imeAction = ImeAction.Done,
                        onAction = { keyboardController?.hide() },
                        onFocusChanged = {
                            if (!it.hasFocus) {
                                calculateItemDiscount()
                                calculatePurchaseDiscount()
                            }
                        }) {
                        tax2State = Utils.getDoubleValue(
                            it,
                            tax1State
                        )
                        purchaseItemModel.purchase.purchaseTax2 = tax2State.toDoubleOrNull() ?: 0.0
                    }
                }
            }
        }

        EditableDateInputField(modifier = Modifier.padding(horizontal = 10.dp),
            date = purchaseHeadDateState,
            label = "Date",
            focusRequester = purchaseHeadDateRequester,
            onAction = {
                purchaseHeadValueDateRequester.requestFocus()
            }) {
            purchaseHeadDateState = it
        }

        EditableDateInputField(modifier = Modifier.padding(horizontal = 10.dp),
            date = purchaseHeadValueDateState,
            label = "Value Date",
            focusRequester = purchaseHeadValueDateRequester,
            onAction = {
                purchaseHeadTransNoRequester.requestFocus()
            }) {
            purchaseHeadValueDateState = it
        }

        SearchableDropdownMenuEx(
            items = state.transactionTypes.toMutableList(),
            modifier = Modifier
                .padding(horizontal = 10.dp),
            onLoadItems = {
                scope.launch(Dispatchers.IO) {
                    viewModel.fetchTransactionTypes()
                }
            },
            label = "Transaction Type",
            selectedId = purchaseHeadTtCodeState
        ) { transType ->
            transType as TransactionTypeModel
            purchaseHeadTtCodeState = transType.transactionTypeId
        }

        UITextField(modifier = Modifier.padding(horizontal = 10.dp),
            defaultValue = purchaseHeadTransNoState,
            label = "Transaction No",
            placeHolder = "Transaction No",
            focusRequester = purchaseHeadTransNoRequester,
            onAction = {
                keyboardController?.hide()
            }) {
            purchaseHeadTransNoState = it
        }

        UITextField(
            modifier = Modifier.padding(horizontal = 10.dp),
            defaultValue = purchaseHeadUserState,
            label = "User",
            readOnly = false,
            enabled = false) {
            purchaseHeadUserState = it
        }


        SearchableDropdownMenuEx(
            items = state.divisions.toMutableList(),
            modifier = Modifier
                .padding(horizontal = 10.dp),
            onLoadItems = {
                scope.launch(Dispatchers.IO) {
                    viewModel.fetchDivisions()
                }
            },
            label = "select item division",
            selectedId = purchaseItemDivState,
            leadingIcon = {
                if (purchaseItemDivState.isNotEmpty()) {
                    Icon(
                        Icons.Default.RemoveCircleOutline,
                        contentDescription = "remove division",
                        tint = Color.Black,
                        modifier = it
                    )
                }
            },
            onLeadingIconClick = {
                purchaseItemDivState = ""
            }
        ) { division ->
            division as DivisionModel
            purchaseItemDivState = division.divisionId
        }

    }
}

@Preview(showBackground = true)
@Composable
fun EditPurchaseItemViewPreview() {
    GridPOSTheme {
        EditPurchaseItemView(
            purchaseItems = mutableListOf(
                PurchaseItemModel(),
                PurchaseItemModel(),
                PurchaseItemModel()
            ),
            purHeader = PurchaseHeader(),
            itemIndex = 0,
            state = PurchaseState(),
            viewModel = hiltViewModel()
        )
    }
}