package com.grid.pos.ui.receipts

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.grid.pos.R
import com.grid.pos.data.receipt.Receipt
import com.grid.pos.data.thirdParty.ThirdParty
import com.grid.pos.model.CurrencyModel
import com.grid.pos.model.PaymentTypeModel
import com.grid.pos.model.PopupModel
import com.grid.pos.model.ReportResult
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.SearchableDropdownMenuEx
import com.grid.pos.ui.common.UIImageButton
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.pos.POSUtils
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun ReceiptsView(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    viewModel: ReceiptsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val amountFocusRequester = remember { FocusRequester() }
    val descFocusRequester = remember { FocusRequester() }
    val noteFocusRequester = remember { FocusRequester() }
    var isBackPressed by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    fun handleBack() {
        if (viewModel.isLoading()) {
            return
        }
        if (isBackPressed) {
            return
        }
        isBackPressed = true
        if (viewModel.isAnyChangeDone()) {
            viewModel.showPopup(
                PopupModel().apply {
                    onDismissRequest = {
                        viewModel.resetState()
                        handleBack()
                    }
                    onConfirmation = {
                        viewModel.save(context){ destination ->
                            navController?.navigate(destination)
                        }
                    }
                    dialogText = "Do you want to save your changes"
                    positiveBtnText = "Save"
                    negativeBtnText = "Close"
                    cancelable = false
                })
            return
        }
        navController?.navigateUp()
    }

    LaunchedEffect(Unit) {
        viewModel.reportResult = ReportResult()
    }

    BackHandler {
        handleBack()
    }
    GridPOSTheme {
        Scaffold(containerColor = SettingsModel.backgroundColor,
            topBar = {
                Surface(
                    shadowElevation = 3.dp,
                    color = SettingsModel.backgroundColor
                ) {
                    TopAppBar(colors = TopAppBarDefaults.mediumTopAppBarColors(
                        containerColor = SettingsModel.topBarColor
                    ),
                        navigationIcon = {
                            IconButton(onClick = { handleBack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = SettingsModel.buttonColor
                                )
                            }
                        },
                        title = {
                            Text(
                                text = "Receipts",
                                color = SettingsModel.textColor,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        },
                        actions = {
                            IconButton(onClick = { navController?.navigate("SettingsView") }) {
                                Icon(
                                    painterResource(R.drawable.ic_settings),
                                    contentDescription = "Back",
                                    tint = SettingsModel.buttonColor
                                )
                            }
                        })
                }
            }) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(it)
                    .background(color = Color.Transparent)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 175.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SearchableDropdownMenuEx(items = viewModel.receiptTypes,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        enableSearch = false,
                        label = "Select Type",
                        selectedId = state.receipt.receiptType,
                        leadingIcon = { modifier ->
                            if (!state.receipt.receiptType.isNullOrEmpty()) {
                                Icon(
                                    Icons.Default.RemoveCircleOutline,
                                    contentDescription = "remove Type",
                                    tint = Color.Black,
                                    modifier = modifier
                                )
                            }
                        },
                        onLeadingIconClick = {
                            viewModel.updateState(
                                state.copy(
                                    receipt = state.receipt.copy(
                                        receiptType = null
                                    )
                                )

                            )
                        }) { typeModel ->
                        typeModel as PaymentTypeModel
                        viewModel.updateState(
                            state.copy(
                                receipt = state.receipt.copy(
                                    receiptType = typeModel.type
                                )
                            )

                        )
                    }

                    SearchableDropdownMenuEx(items = state.currencies.toMutableList(),
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        enableSearch = SettingsModel.isConnectedToSqlServer(),
                        label = "Select Currency",
                        selectedId = state.receipt.receiptCurrency,
                        leadingIcon = { modifier ->
                            if (!state.receipt.receiptCurrency.isNullOrEmpty()) {
                                Icon(
                                    Icons.Default.RemoveCircleOutline,
                                    contentDescription = "remove Currency",
                                    tint = Color.Black,
                                    modifier = modifier
                                )
                            }
                        },
                        onLeadingIconClick = {
                            viewModel.updateState(
                                state.copy(
                                    receipt = state.receipt.copy(
                                        receiptCurrency = null,
                                        receiptCurrencyCode = null
                                    ),
                                    currencyIndex = 0
                                )

                            )
                        }) { currModel ->
                        currModel as CurrencyModel
                        viewModel.updateState(
                            state.copy(
                                receipt = state.receipt.copy(
                                    receiptCurrency = currModel.getId(),
                                    receiptCurrencyCode = currModel.currencyCode
                                ),
                                currencyIndex = state.receipt.getSelectedCurrencyIndex(currModel.getId())
                            )

                        )
                    }

                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = state.receiptAmountStr,
                        label = "Amount",
                        placeHolder = "Enter Amount",
                        focusRequester = amountFocusRequester,
                        onAction = {
                            descFocusRequester.requestFocus()
                        }) { amount ->
                        val amountStr = Utils.getDoubleValue(
                            amount,
                            state.receiptAmountStr
                        )
                        val receiptAmount = amountStr.toDoubleOrNull() ?: 0.0
                        var amountFirst = state.receipt.receiptAmountFirst
                        var amountSecond = state.receipt.receiptAmountSecond
                        if (state.currencyIndex == 1) {
                            amountSecond = receiptAmount.times(
                                SettingsModel.currentCurrency?.currencyRate ?: 1.0
                            )
                        } else if (state.currencyIndex == 2) {
                            amountFirst = receiptAmount.div(
                                SettingsModel.currentCurrency?.currencyRate ?: 1.0
                            )
                        }
                        val amountFirstStr = if (amountFirst == 0.0) "" else POSUtils.formatDouble(
                            amountFirst,
                            SettingsModel.currentCurrency?.currencyName1Dec ?: 2
                        )
                        val amountSecondStr =
                            if (amountSecond == 0.0) "" else POSUtils.formatDouble(
                                amountSecond,
                                SettingsModel.currentCurrency?.currencyName2Dec ?: 2
                            )
                        viewModel.updateState(
                            state.copy(
                                receipt = state.receipt.copy(
                                    receiptAmount = receiptAmount,
                                    receiptAmountFirst = amountFirst,
                                    receiptAmountSecond = amountSecond,
                                ),
                                receiptAmountStr = amountStr,
                                receiptAmountFirstStr = amountFirstStr,
                                receiptAmountSecondStr = amountSecondStr
                            )

                        )
                    }

                    if (state.currencyIndex != 1) {
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue = state.receiptAmountFirstStr,
                            label = "Amount ${SettingsModel.currentCurrency?.currencyCode1 ?: ""}",
                            placeHolder = "Enter Amount",
                            focusRequester = amountFocusRequester,
                            keyboardType = KeyboardType.Decimal,
                            onAction = {
                                descFocusRequester.requestFocus()
                            }) { amount ->
                            val amountFirst =
                                amount.toDoubleOrNull() ?: state.receipt.receiptAmountFirst
                            viewModel.updateState(
                                state.copy(
                                    receipt = state.receipt.copy(
                                        receiptAmountFirst = amountFirst
                                    ),
                                    receiptAmountFirstStr = Utils.getDoubleValue(
                                        amount,
                                        state.receiptAmountFirstStr
                                    )
                                )

                            )
                        }
                    }

                    if (state.currencyIndex != 2) {
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue = state.receiptAmountSecondStr,
                            label = "Amount ${SettingsModel.currentCurrency?.currencyCode2 ?: ""}",
                            placeHolder = "Enter Amount",
                            focusRequester = amountFocusRequester,
                            keyboardType = KeyboardType.Decimal,
                            onAction = {
                                descFocusRequester.requestFocus()
                            }) { amount ->
                            val amountSecond =
                                amount.toDoubleOrNull() ?: state.receipt.receiptAmountSecond
                            viewModel.updateState(
                                state.copy(
                                    receipt = state.receipt.copy(
                                        receiptAmountSecond = amountSecond
                                    ),
                                    receiptAmountSecondStr = Utils.getDoubleValue(
                                        amount,
                                        state.receiptAmountSecondStr
                                    )
                                )

                            )
                        }
                    }

                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = state.receipt.receiptDesc ?: "",
                        label = "Description",
                        placeHolder = "Enter Description",
                        focusRequester = descFocusRequester,
                        maxLines = 4,
                        imeAction = ImeAction.None,
                        onAction = { noteFocusRequester.requestFocus() }) { desc ->
                        viewModel.updateState(
                            state.copy(
                                receipt = state.receipt.copy(
                                    receiptDesc = desc
                                )
                            )

                        )
                    }

                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = state.receipt.receiptNote ?: "",
                        label = "Note",
                        placeHolder = "Enter Note",
                        maxLines = 4,
                        focusRequester = noteFocusRequester,
                        imeAction = ImeAction.None,
                        onAction = { keyboardController?.hide() }) { note ->
                        viewModel.updateState(
                            state.copy(
                                receipt = state.receipt.copy(
                                    receiptNote = note
                                )
                            )

                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(
                                horizontal = 10.dp,
                                vertical = 5.dp
                            ),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        UIImageButton(
                            modifier = Modifier
                                .weight(.33f)
                                .padding(3.dp),
                            icon = R.drawable.save,
                            text = "Save"
                        ) {
                            viewModel.save(context){ destination ->
                                navController?.navigate(destination)
                            }
                        }

                        UIImageButton(
                            modifier = Modifier
                                .weight(.33f)
                                .padding(3.dp),
                            icon = R.drawable.delete,
                            text = "Delete"
                        ) {
                            viewModel.delete()
                        }

                        UIImageButton(
                            modifier = Modifier
                                .weight(.33f)
                                .padding(3.dp),
                            icon = R.drawable.go_back,
                            text = "Close"
                        ) {
                            handleBack()
                        }
                    }
                }

                SearchableDropdownMenuEx(items = state.thirdParties.toMutableList(),
                    modifier = Modifier.padding(
                        top = 100.dp,
                        start = 10.dp,
                        end = 10.dp
                    ),
                    label = "Select ThirdParty",
                    selectedId = state.receipt.receiptThirdParty,
                    onLoadItems = {
                        scope.launch(Dispatchers.IO) {
                            viewModel.fetchThirdParties()
                        }
                    },
                    leadingIcon = { modifier ->
                        if (!state.receipt.receiptThirdParty.isNullOrEmpty()) {
                            Icon(
                                Icons.Default.RemoveCircleOutline,
                                contentDescription = "remove ThirdParty",
                                tint = Color.Black,
                                modifier = modifier
                            )
                        }
                    },
                    onLeadingIconClick = {
                        viewModel.updateState(
                            state.copy(
                                receipt = state.receipt.copy(
                                    receiptThirdParty = null,
                                    receiptThirdPartyName = null
                                )
                            )
                        )
                    }) { thirdParty ->
                    thirdParty as ThirdParty
                    viewModel.updateState(
                        state.copy(
                            receipt = state.receipt.copy(
                                receiptThirdParty = thirdParty.thirdPartyId,
                                receiptThirdPartyName = thirdParty.thirdPartyName
                            )
                        )
                    )
                }

                SearchableDropdownMenuEx(items = state.receipts.toMutableList(),
                    modifier = Modifier.padding(
                        top = 15.dp,
                        start = 10.dp,
                        end = 10.dp
                    ),
                    label = "Select Receipt",
                    selectedId = state.receipt.receiptId,
                    onLoadItems = {
                        viewModel.fetchReceipts()
                    },
                    leadingIcon = { modifier ->
                        if (state.receipt.receiptId.isNotEmpty()) {
                            Icon(
                                Icons.Default.RemoveCircleOutline,
                                contentDescription = "remove Receipt",
                                tint = Color.Black,
                                modifier = modifier
                            )
                        }
                    },
                    onLeadingIconClick = {
                        viewModel.resetState()
                    }) { receipt ->
                    receipt as Receipt
                    receipt.receiptCurrencyCode = viewModel.getCurrencyCode(receipt.receiptCurrency)
                    viewModel.currentReceipt = receipt.copy()
                    viewModel.updateState(
                        state.copy(
                            receipt = receipt.copy(),
                            currencyIndex = receipt.getSelectedCurrencyIndex(),
                            receiptAmountStr = POSUtils.formatDouble(
                                receipt.receiptAmount,
                                SettingsModel.currentCurrency?.currencyName1Dec ?: 2
                            ),
                            receiptAmountFirstStr = POSUtils.formatDouble(
                                receipt.receiptAmountFirst,
                                SettingsModel.currentCurrency?.currencyName1Dec ?: 2
                            ),
                            receiptAmountSecondStr = POSUtils.formatDouble(
                                receipt.receiptAmountSecond,
                                SettingsModel.currentCurrency?.currencyName2Dec ?: 2
                            )
                        )
                    )
                }
            }
        }
    }
}