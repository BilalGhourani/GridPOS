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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.grid.pos.data.stockHeadInOut.header.StockHeaderInOut
import com.grid.pos.data.stockHeaderAdjustment.StockHeaderAdjustment
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.StockAdjItemModel
import com.grid.pos.model.StockInOutItemModel
import com.grid.pos.ui.common.UITextField
import com.grid.pos.utils.Utils

@Composable
fun EditStockInOutItemView(
    modifier: Modifier = Modifier,
    stockInOutItemModel: StockInOutItemModel,
    stockHeaderInOut: StockHeaderInOut,
    triggerOnSave: Boolean,
    onSave: (StockHeaderInOut, StockInOutItemModel) -> Unit = { _, _ -> }
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val stockHeadDescFocusRequester = remember { FocusRequester() }
    val stockHeadNoteFocusRequester = remember { FocusRequester() }

    var qtyState by remember { mutableIntStateOf(1) }
    var stockHeadDescState by remember { mutableStateOf("") }
    var stockHeadNoteState by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        qtyState = stockInOutItemModel.stockInOut.stockInOutQty.toInt()
        stockHeadDescState = stockHeaderInOut.stockHeadInOutDesc ?: ""
        stockHeadNoteState = stockHeaderInOut.stockHeadInOutNote ?: ""
    }

    fun backAndSave() {
        val stockItemModel = stockInOutItemModel.copy()
        val stockHeaderInOutCopy = stockHeaderInOut.copy()
        stockItemModel.stockInOut.stockInOutQty = qtyState.toDouble()
        stockHeaderInOutCopy.stockHeadInOutDesc = stockHeadDescState.ifEmpty { null }
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
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {})
            },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        OutlinedTextField(value = qtyState.toString(),
            onValueChange = {
                qtyState = it.toInt()
            },
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            readOnly = true,
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
            keyboardActions = KeyboardActions(onNext = { /* Move focus to next field */ }),
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
            })

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