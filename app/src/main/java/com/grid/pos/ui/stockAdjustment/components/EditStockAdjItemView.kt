package com.grid.pos.ui.stockAdjustment.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.grid.pos.data.stockHeaderAdjustment.StockHeaderAdjustment
import com.grid.pos.model.DivisionModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.StockAdjItemModel
import com.grid.pos.model.TransactionTypeModel
import com.grid.pos.ui.common.EditableDateInputField
import com.grid.pos.ui.common.SearchableDropdownMenuEx
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.stockAdjustment.StockAdjustmentState
import com.grid.pos.ui.stockAdjustment.StockAdjustmentViewModel
import com.grid.pos.utils.DateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun EditStockAdjItemView(
    modifier: Modifier = Modifier,
    stockAdjItemModel: StockAdjItemModel,
    stockHeaderAdjustment: StockHeaderAdjustment,
    triggerOnSave: Boolean,
    state: StockAdjustmentState,
    viewModel: StockAdjustmentViewModel = hiltViewModel(),
    source: String,
    onSave: (StockHeaderAdjustment, StockAdjItemModel) -> Unit = { _, _ -> }
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    val stockItemCostFocusRequester = remember { FocusRequester() }
    val stockItemReasonFocusRequester = remember { FocusRequester() }
    val stockInOutDateFocusRequester = remember { FocusRequester() }
    val stockHeadNoteFocusRequester = remember { FocusRequester() }
    val stockHeadTransNoFocusRequester = remember { FocusRequester() }

    var qtyState by remember { mutableIntStateOf(1) }
    var stockItemReasonState by remember { mutableStateOf("") }
    var stockItemCostState by remember { mutableStateOf("") }
    var stockItemDivState by remember { mutableStateOf("") }
    var stockHeadDateState by remember { mutableStateOf("") }
    var stockHeadValueDateState by remember { mutableStateOf("") }
    var stockHeadTtCodeState by remember { mutableStateOf("") }
    var stockHeadTransNoState by remember { mutableStateOf("") }
    var stockHeadDescState by remember { mutableStateOf("") }
    var stockHeadUserState by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (source.equals("stkadj", ignoreCase = true)) {
            qtyState = stockAdjItemModel.stockAdjustment.stockAdjQty?.toInt() ?: 0
            stockItemCostState = ""
        } else {
            qtyState = stockAdjItemModel.stockAdjustment.stockAdjRemQtyWa?.toInt() ?: 0
            stockItemCostState = stockAdjItemModel.stockAdjustment.stockAdjCost?.toString() ?: ""
        }

        stockItemReasonState = stockAdjItemModel.stockAdjustment.stockAdjReason ?: ""
        stockItemDivState = stockAdjItemModel.stockAdjustment.stockAdjDivName ?: ""

        stockHeadDateState = stockHeaderAdjustment.stockHADate
        stockHeadValueDateState = DateHelper.getDateInFormat(
            stockHeaderAdjustment.stockHAValueDate ?: Date(),
            "YYYY-MM-DD HH:mm:ss.SSS"
        )
        stockHeadTtCodeState = stockHeaderAdjustment.stockHATtCode ?: ""
        stockHeadTransNoState = stockHeaderAdjustment.stockHATransNo ?: ""
        stockHeadDescState = stockHeaderAdjustment.stockHADesc ?: ""
        stockHeadUserState =
            stockHeaderAdjustment.stockHAUserStamp ?: SettingsModel.currentUser?.userUsername
                    ?: ""

        if (state.transactionTypes.isEmpty()) {
            viewModel.fetchTransactionTypes(false)
        }
        if (state.divisions.isEmpty()) {
            viewModel.fetchDivisions(false)
        }
    }

    fun backAndSave() {
        val stockItemModel = stockAdjItemModel.copy()
        val stockHeaderInOutCopy = stockHeaderAdjustment.copy()
        if (source.equals("stkadj", ignoreCase = true)) {
            stockItemModel.stockAdjustment.stockAdjQty = qtyState.toDouble()
            stockItemModel.stockAdjustment.stockAdjRemQtyWa = null
            stockItemModel.stockAdjustment.stockAdjCost = null
        } else {
            stockItemModel.stockAdjustment.stockAdjQty = null
            stockItemModel.stockAdjustment.stockAdjRemQtyWa = qtyState.toDouble()
            stockItemModel.stockAdjustment.stockAdjCost = stockItemCostState.toDoubleOrNull()
        }

        stockItemModel.stockAdjustment.stockAdjReason = stockItemReasonState.ifEmpty { null }
        stockItemModel.stockAdjustment.stockAdjDivName = stockItemDivState.ifEmpty { null }

        stockHeaderInOutCopy.stockHADate = stockHeadDateState
        stockHeaderInOutCopy.stockHAValueDate =
            DateHelper.getDateFromString(stockHeadValueDateState, "yyyy-MM-dd HH:mm:ss.SSS")
        stockHeaderInOutCopy.stockHATtCode = stockHeadTtCodeState.ifEmpty { null }
        stockHeaderInOutCopy.stockHATransNo = stockHeadTransNoState.ifEmpty { null }
        stockHeaderInOutCopy.stockHADesc = stockHeadDescState.ifEmpty { null }
        onSave.invoke(stockHeaderInOutCopy, stockItemModel)
    }

    LaunchedEffect(key1 = triggerOnSave) {
        if (triggerOnSave) {
            backAndSave()
        }
    }

    BackHandler {
        backAndSave()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {})
            }
            .background(color = Color.Transparent)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = qtyState.toString(),
            onValueChange = {
                qtyState = it.toInt()
            },
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, top = 10.dp),
            readOnly = true,
            enabled = false,
            label = {
                Box(
                    modifier = Modifier
                        .wrapContentWidth()
                        .background(color = Color.Transparent)
                        .align(Alignment.CenterHorizontally),
                ) {
                    Text(
                        text = "Qty",
                        modifier = Modifier.wrapContentWidth(),
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
            keyboardActions = KeyboardActions(onNext = { stockInOutDateFocusRequester.requestFocus() }),
            leadingIcon = {
                IconButton(onClick = {
                    qtyState++
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
                    if (qtyState > 1) qtyState--
                }) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Decrease quantity",
                        tint = SettingsModel.buttonColor
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Black,
                focusedBorderColor = SettingsModel.buttonColor,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = SettingsModel.buttonColor,
                disabledTextColor = Color.Black,
                disabledBorderColor = SettingsModel.buttonColor,
            )
        )

        EditableDateInputField(modifier = Modifier.padding(horizontal = 10.dp),
            date = stockHeadDateState,
            label = "Date",
            focusRequester = stockInOutDateFocusRequester,
            onAction = {
                stockHeadNoteFocusRequester.requestFocus()
            }) {
            stockHeadDateState = it
        }

        EditableDateInputField(modifier = Modifier.padding(horizontal = 10.dp),
            date = stockHeadValueDateState,
            label = "Value Date",
            focusRequester = stockInOutDateFocusRequester,
            onAction = {
                stockHeadNoteFocusRequester.requestFocus()
            }) {
            stockHeadValueDateState = it
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
            selectedId = stockHeadTtCodeState
        ) { transType ->
            transType as TransactionTypeModel
            stockHeadTtCodeState = transType.transactionTypeId
        }

        UITextField(modifier = Modifier.padding(horizontal = 10.dp),
            defaultValue = stockHeadTransNoState,
            label = "Transaction No",
            placeHolder = "Transaction No",
            focusRequester = stockHeadTransNoFocusRequester,
            onAction = {
                stockHeadNoteFocusRequester.requestFocus()
            }) {
            stockHeadTransNoState = it
        }

        UITextField(
            modifier = Modifier.padding(horizontal = 10.dp),
            defaultValue = stockHeadDescState,
            label = "Description",
            placeHolder = "Description",
            maxLines = 3,
            focusRequester = stockHeadNoteFocusRequester,
            imeAction = ImeAction.None,
        ) {
            stockHeadDescState = it
        }

        UITextField(modifier = Modifier.padding(horizontal = 10.dp),
            defaultValue = stockHeadUserState,
            label = "User",
            readOnly = false,
            enabled = false,
            focusRequester = stockHeadNoteFocusRequester,
            onAction = {
                if (source.equals("stkadj", ignoreCase = true)) {
                    stockItemReasonFocusRequester.requestFocus()
                }else{
                    stockItemCostFocusRequester.requestFocus()
                }
            }) {
            stockHeadUserState = it
        }

        if (source.equals("QtyOnHand", ignoreCase = true)) {
            UITextField(modifier = Modifier.padding(horizontal = 10.dp),
                defaultValue = stockItemCostState,
                label = "Item Cost",
                placeHolder = "Item Cost",
                focusRequester = stockItemCostFocusRequester,
                onAction = {
                    stockItemReasonFocusRequester.requestFocus()
                }) {
                stockItemCostState = it
            }
        }

        UITextField(modifier = Modifier.padding(horizontal = 10.dp),
            defaultValue = stockItemReasonState,
            label = "Item Reason",
            placeHolder = "Item Reason",
            focusRequester = stockItemReasonFocusRequester,
            onAction = {
                keyboardController?.hide()
            }) {
            stockItemReasonState = it
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
            selectedId = stockItemDivState,
            leadingIcon = {
                if (stockItemDivState.isNotEmpty()) {
                    Icon(
                        Icons.Default.RemoveCircleOutline,
                        contentDescription = "remove division",
                        tint = Color.Black,
                        modifier = it
                    )
                }
            },
            onLeadingIconClick = {
                stockItemDivState = ""
            }
        ) { division ->
            division as DivisionModel
            stockItemDivState = division.divisionId
        }
    }

}