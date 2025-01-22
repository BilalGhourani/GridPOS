package com.grid.pos.ui.stockInOut.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.grid.pos.data.stockHeadInOut.header.StockHeaderInOut
import com.grid.pos.data.stockHeaderAdjustment.StockHeaderAdjustment
import com.grid.pos.model.StockAdjItemModel
import com.grid.pos.model.StockInOutItemModel
import com.grid.pos.ui.common.UITextField
import com.grid.pos.utils.Utils

@Composable
fun EditStockInOutItemView(
    modifier: Modifier = Modifier,
    stockInOutItemModel: StockInOutItemModel,
    stockHeaderInOut: StockHeaderInOut,
    onSave: (StockInOutItemModel) -> Unit = { }
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val stockHeadDescFocusRequester = remember { FocusRequester() }
    val stockHeadNoteFocusRequester = remember { FocusRequester() }

    var qtyState by remember { mutableStateOf("") }
    var stockHeadDescState by remember { mutableStateOf("") }
    var stockHeadNoteState by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        qtyState = stockInOutItemModel.stockInOut.stockInOutQty.toString()
        stockHeadDescState = stockHeaderInOut.stockHeadInOutDesc ?: ""
        stockHeadNoteState = stockHeaderInOut.stockHeadInOutNote ?: ""
    }

    fun backAndSave() {
        val stockItemModel = stockInOutItemModel.copy()
        stockItemModel.stockInOut.stockInOutQty = qtyState.toDoubleOrNull() ?: 1.0
        stockHeaderInOut.stockHeadInOutDesc = stockHeadDescState.ifEmpty { null }
        stockHeaderInOut.stockHeadInOutNote = stockHeadNoteState.ifEmpty { null }
        onSave.invoke(stockItemModel)
    }

    BackHandler {
        backAndSave()
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

        UITextField(modifier = Modifier.padding(
            horizontal = 10.dp,
            vertical = 5.dp
        ),
            defaultValue = qtyState,
            label = "Quantity",
            keyboardType = KeyboardType.Decimal,
            placeHolder = "Enter Quantity",
            onAction = { stockHeadDescFocusRequester.requestFocus() }) { openQty ->
            qtyState = Utils.getDoubleValue(
                openQty,
                qtyState
            )
        }

        UITextField(modifier = Modifier.padding(10.dp),
            defaultValue = stockHeadDescState,
            label = "Description",
            focusRequester = stockHeadDescFocusRequester,
            onAction = {
                stockHeadNoteFocusRequester.requestFocus()
            }) {
            stockHeadDescState = it
        }

        UITextField(modifier = Modifier.padding(10.dp),
            defaultValue = stockHeadNoteState,
            label = "Note",
            focusRequester = stockHeadNoteFocusRequester,
            onAction = {
                keyboardController?.hide()
            }) {
            stockHeadNoteState = it
        }

    }
}