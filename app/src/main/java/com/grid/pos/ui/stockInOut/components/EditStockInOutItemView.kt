package com.grid.pos.ui.stockInOut.components

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
import com.grid.pos.data.stockHeaderInOut.StockHeaderInOut
import com.grid.pos.model.DivisionModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.StockInOutItemModel
import com.grid.pos.model.TransactionTypeModel
import com.grid.pos.ui.common.EditableDateInputField
import com.grid.pos.ui.common.SearchableDropdownMenuEx
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.stockInOut.StockInOutState
import com.grid.pos.ui.stockInOut.StockInOutViewModel
import com.grid.pos.utils.DateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun EditStockInOutItemView(
    modifier: Modifier = Modifier,
    stockInOutItemModel: StockInOutItemModel,
    stockHeaderInOut: StockHeaderInOut,
    triggerOnSave: Boolean,
    state: StockInOutState,
    viewModel: StockInOutViewModel = hiltViewModel(),
    onSave: (StockHeaderInOut, StockInOutItemModel) -> Unit = { _, _ -> }
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    val stockItemNoteFocusRequester = remember { FocusRequester() }
    val stockInOutDateFocusRequester = remember { FocusRequester() }
    val stockHeadNoteFocusRequester = remember { FocusRequester() }
    val stockHeadTransNoFocusRequester = remember { FocusRequester() }

    var qtyState by remember { mutableIntStateOf(1) }
    var stockItemNoteState by remember { mutableStateOf("") }
    var stockItemDivState by remember { mutableStateOf("") }
    var stockHeadDateState by remember { mutableStateOf("") }
    var stockHeadValueDateState by remember { mutableStateOf("") }
    var stockHeadTtCodeState by remember { mutableStateOf("") }
    var stockHeadTransNoState by remember { mutableStateOf("") }
    var stockHeadNoteState by remember { mutableStateOf("") }
    var stockHeadUserState by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        qtyState = stockInOutItemModel.stockInOut.stockInOutQty.toInt()
        stockItemNoteState = stockInOutItemModel.stockInOut.stockInOutNote ?: ""
        stockItemDivState = stockInOutItemModel.stockInOut.stockInOutDivName ?: ""

        stockHeadDateState = stockHeaderInOut.stockHeadInOutDate ?: DateHelper.getDateInFormat(
            format = "YYYY-MM-DD HH:mm:ss.SSS"
        )
        stockHeadValueDateState = DateHelper.getDateInFormat(
            stockHeaderInOut.stockHeadInOutValueDate ?: Date(),
            "YYYY-MM-DD HH:mm:ss.SSS"
        )
        stockHeadTtCodeState = stockHeaderInOut.stockHeadInOutTtCode ?: ""
        stockHeadTransNoState = stockHeaderInOut.stockHeadInOutTransNo ?: ""
        stockHeadNoteState = stockHeaderInOut.stockHeadInOutNote ?: ""
        stockHeadUserState =
            stockHeaderInOut.stockHeadInOutUserStamp ?: SettingsModel.currentUser?.userUsername
                    ?: ""

        if (state.transactionTypes.isEmpty()) {
            viewModel.fetchTransactionTypes(false)
        }
        if (state.divisions.isEmpty()) {
            viewModel.fetchDivisions(false)
        }
    }

    fun backAndSave() {
        val stockItemModel = stockInOutItemModel.copy()
        val stockHeaderInOutCopy = stockHeaderInOut.copy()
        stockItemModel.stockInOut.stockInOutQty = qtyState.toDouble()
        stockItemModel.stockInOut.stockInOutNote = stockItemNoteState.ifEmpty { null }
        stockItemModel.stockInOut.stockInOutDivName = stockItemDivState.ifEmpty { null }

        stockHeaderInOutCopy.stockHeadInOutDate = stockHeadDateState
        stockHeaderInOutCopy.stockHeadInOutValueDate =
            DateHelper.getDateFromString(stockHeadValueDateState, "yyyy-MM-dd HH:mm:ss.SSS")
        stockHeaderInOutCopy.stockHeadInOutTtCode = stockHeadTtCodeState.ifEmpty { null }
        stockHeaderInOutCopy.stockHeadInOutTransNo = stockHeadTransNoState.ifEmpty { null }
        stockHeaderInOutCopy.stockHeadInOutNote = stockHeadNoteState.ifEmpty { null }
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
                qtyState = it.toIntOrNull() ?: qtyState
            },
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, top = 10.dp),
            /*readOnly = true,
            enabled = false,*/
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
            keyboardActions = KeyboardActions(onNext = { stockHeadTransNoFocusRequester.requestFocus() }),
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

        UITextField(modifier = Modifier.padding(horizontal = 10.dp),
            defaultValue = stockHeadNoteState,
            label = "Transfer Note",
            placeHolder = "Transfer Note",
            focusRequester = stockHeadNoteFocusRequester,
            onAction = {
                stockItemNoteFocusRequester.requestFocus()
            }) {
            stockHeadNoteState = it
        }

        UITextField(modifier = Modifier.padding(horizontal = 10.dp),
            defaultValue = stockHeadUserState,
            label = "User",
            readOnly = false,
            enabled = false,
            focusRequester = stockHeadNoteFocusRequester,
            onAction = {
                stockItemNoteFocusRequester.requestFocus()
            }) {
            stockHeadUserState = it
        }

        UITextField(modifier = Modifier.padding(horizontal = 10.dp),
            defaultValue = stockItemNoteState,
            label = "Item Note",
            placeHolder = "Item Note",
            focusRequester = stockItemNoteFocusRequester,
            onAction = {
                keyboardController?.hide()
            }) {
            stockItemNoteState = it
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