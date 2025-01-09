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
import com.grid.pos.data.invoiceHeader.InvoiceHeader
import com.grid.pos.data.stockHeaderAdjustment.StockHeaderAdjustment
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.StockAdjItemModel
import com.grid.pos.ui.common.UITextField
import com.grid.pos.utils.Utils

@Composable
fun EditStockAdjustItemView(
    modifier: Modifier = Modifier,
    stockAdjItemModel: StockAdjItemModel,
    stockHeaderAdjustment: StockHeaderAdjustment,
    onSave: (StockAdjItemModel) -> Unit = { }
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val stockHeadDescFocusRequester = remember { FocusRequester() }

    var qtyState by remember { mutableStateOf("") }
    var stockHeadDescState by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        qtyState = stockAdjItemModel.stockAdjustment.stockAdjQty.toString()
        stockHeadDescState = stockHeaderAdjustment.stockHADesc ?: ""
    }

    fun backAndSave() {
        val stockItemModel = stockAdjItemModel.copy()
        stockItemModel.stockAdjustment.stockAdjQty = qtyState.toDoubleOrNull() ?: 1.0
        stockHeaderAdjustment.stockHADesc = stockHeadDescState.ifEmpty { null }
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
            label = "Client Exta Name",
            focusRequester = stockHeadDescFocusRequester,
            onAction = {
                keyboardController?.hide()
            }) {
            stockHeadDescState = it
        }

    }
}