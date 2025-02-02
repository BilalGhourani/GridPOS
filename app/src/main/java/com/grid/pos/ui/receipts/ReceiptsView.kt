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
import androidx.compose.runtime.collectAsState
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
import com.grid.pos.SharedViewModel
import com.grid.pos.data.receipt.Receipt
import com.grid.pos.data.thirdParty.ThirdParty
import com.grid.pos.model.CurrencyModel
import com.grid.pos.model.PaymentTypeModel
import com.grid.pos.model.PopupModel
import com.grid.pos.model.ReportResult
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.ToastModel
import com.grid.pos.ui.common.SearchableDropdownMenuEx
import com.grid.pos.ui.common.UIImageButton
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.GridPOSTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun ReceiptsView(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    sharedViewModel: SharedViewModel,
    viewModel: ReceiptsViewModel = hiltViewModel()
) {
    val state by viewModel.manageReceiptsState.collectAsStateWithLifecycle()
    val receipt = viewModel.receiptState.collectAsState().value

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val amountFocusRequester = remember { FocusRequester() }
    val descFocusRequester = remember { FocusRequester() }
    val noteFocusRequester = remember { FocusRequester() }

    val scope = rememberCoroutineScope()
    LaunchedEffect(state.warning) {
        state.warning?.value?.let { message ->
            sharedViewModel.showToastMessage(
                ToastModel(
                    message = message
                )
            )
        }
    }

    LaunchedEffect(state.isLoading) {
        sharedViewModel.showLoading(state.isLoading)
    }

    var saveAndBack by remember { mutableStateOf(false) }
    fun handleBack() {
        if (state.isLoading) {
            return
        }
        if (viewModel.isAnyChangeDone()) {
            sharedViewModel.showPopup(true,
                PopupModel().apply {
                    onDismissRequest = {
                        viewModel.resetState()
                        handleBack()
                    }
                    onConfirmation = {
                        saveAndBack = true
                        viewModel.save(context)
                    }
                    dialogText = "Do you want to save your changes"
                    positiveBtnText = "Save"
                    negativeBtnText = "Close"
                })
            return
        }
        if (state.thirdParties.isNotEmpty()) {
            sharedViewModel.thirdParties = state.thirdParties
        }
        navController?.navigateUp()
    }

    fun clearAndBack() {
        viewModel.resetState()
        if (saveAndBack) {
            handleBack()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.reportResult = ReportResult()
    }

    LaunchedEffect(
        state.clear,
        state.isSaved
    ) {
        if (state.isSaved) {
            state.isSaved = false
            sharedViewModel.reportsToPrint.clear()
            sharedViewModel.reportsToPrint.add(viewModel.reportResult)
            viewModel.resetState()
            navController?.navigate("UIWebView")
        }
        if (state.clear) {
            clearAndBack()
        }
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
                        selectedId = receipt.receiptType,
                        leadingIcon = { modifier ->
                            if (!receipt.receiptType.isNullOrEmpty()) {
                                Icon(
                                    Icons.Default.RemoveCircleOutline,
                                    contentDescription = "remove Type",
                                    tint = Color.Black,
                                    modifier = modifier
                                )
                            }
                        },
                        onLeadingIconClick = {
                            viewModel.updateReceipt(
                                receipt.copy(
                                    receiptType = null
                                )
                            )
                        }) { typeModel ->
                        typeModel as PaymentTypeModel
                        viewModel.updateReceipt(
                            receipt.copy(
                                receiptType = typeModel.type
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
                        selectedId = receipt.receiptCurrency,
                        leadingIcon = { modifier ->
                            if (!receipt.receiptCurrency.isNullOrEmpty()) {
                                Icon(
                                    Icons.Default.RemoveCircleOutline,
                                    contentDescription = "remove Currency",
                                    tint = Color.Black,
                                    modifier = modifier
                                )
                            }
                        },
                        onLeadingIconClick = {
                            viewModel.updateReceipt(
                                receipt.copy(
                                    receiptCurrency = null,
                                    receiptCurrencyCode = null
                                )
                            )
                            viewModel.currencyIndexState.intValue = 0
                        }) { currModel ->
                        currModel as CurrencyModel
                        viewModel.updateReceipt(
                            receipt.copy(
                                receiptCurrency = currModel.getId(),
                                receiptCurrencyCode = currModel.currencyCode
                            )
                        )
                        viewModel.currencyIndexState.intValue = viewModel.receiptState.value.getSelectedCurrencyIndex()
                    }

                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = receipt.getFormattedAmount(),
                        label = "Amount",
                        placeHolder = "Enter Amount",
                        focusRequester = amountFocusRequester,
                        onAction = {
                            descFocusRequester.requestFocus()
                        }) { amount ->
                        val receiptAmount = amount.toDoubleOrNull() ?: receipt.receiptAmount
                        var amountFirst = receipt.receiptAmountFirst
                        var amountSecond = receipt.receiptAmountSecond
                        if (viewModel.currencyIndexState.intValue == 1) {
                            amountSecond = receiptAmount.times(SettingsModel.currentCurrency?.currencyRate ?: 1.0)
                        } else if (viewModel.currencyIndexState.intValue == 2) {
                            amountFirst = receiptAmount.div(SettingsModel.currentCurrency?.currencyRate ?: 1.0)
                        }
                        viewModel.updateReceipt(
                            receipt.copy(
                                receiptAmount = receiptAmount,
                                receiptAmountFirst = amountFirst,
                                receiptAmountSecond = amountSecond
                            )
                        )
                    }

                    if (viewModel.currencyIndexState.intValue != 1) {
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue = receipt.getFormattedAmountFirst(),
                            label = "Amount ${SettingsModel.currentCurrency?.currencyCode1 ?: ""}",
                            placeHolder = "Enter Amount",
                            focusRequester = amountFocusRequester,
                            keyboardType = KeyboardType.Decimal,
                            onAction = {
                                descFocusRequester.requestFocus()
                            }) { amount ->
                            viewModel.updateReceipt(
                                receipt.copy(
                                    receiptAmountFirst = amount.toDoubleOrNull() ?: receipt.receiptAmountFirst
                                )
                            )
                        }
                    }

                    if (viewModel.currencyIndexState.intValue != 2) {
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue = receipt.getFormattedAmountSecond(),
                            label = "Amount ${SettingsModel.currentCurrency?.currencyCode2 ?: ""}",
                            placeHolder = "Enter Amount",
                            focusRequester = amountFocusRequester,
                            keyboardType = KeyboardType.Decimal,
                            onAction = {
                                descFocusRequester.requestFocus()
                            }) { amount ->
                            viewModel.updateReceipt(
                                receipt.copy(
                                    receiptAmountSecond = amount.toDoubleOrNull() ?: receipt.receiptAmountSecond
                                )
                            )
                        }
                    }

                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = receipt.receiptDesc?:"",
                        label = "Description",
                        placeHolder = "Enter Description",
                        focusRequester = descFocusRequester,
                        maxLines = 4,
                        imeAction = ImeAction.None,
                        onAction = { noteFocusRequester.requestFocus() }) { desc ->
                        viewModel.updateReceipt(
                            receipt.copy(
                                receiptDesc = desc.trim()
                            )
                        )
                    }

                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = receipt.receiptNote?:"",
                        label = "Note",
                        placeHolder = "Enter Note",
                        maxLines = 4,
                        focusRequester = noteFocusRequester,
                        imeAction = ImeAction.None,
                        onAction = { keyboardController?.hide() }) { note ->
                        viewModel.updateReceipt(
                            receipt.copy(
                                receiptNote = note.trim()
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
                            viewModel.save(context)
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
                    selectedId = receipt.receiptThirdParty,
                    onLoadItems = {
                        scope.launch(Dispatchers.IO) {
                            viewModel.fetchThirdParties()
                        }
                    },
                    leadingIcon = { modifier ->
                        if (!receipt.receiptThirdParty.isNullOrEmpty()) {
                            Icon(
                                Icons.Default.RemoveCircleOutline,
                                contentDescription = "remove ThirdParty",
                                tint = Color.Black,
                                modifier = modifier
                            )
                        }
                    },
                    onLeadingIconClick = {
                        viewModel.updateReceipt(
                            receipt.copy(
                                receiptThirdParty = null,
                                receiptThirdPartyName = null
                            )
                        )
                    }) { thirdParty ->
                    thirdParty as ThirdParty
                    viewModel.updateReceipt(
                        receipt.copy(
                            receiptThirdParty = thirdParty.thirdPartyId,
                            receiptThirdPartyName = thirdParty.thirdPartyName
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
                    selectedId = receipt.receiptId,
                    onLoadItems = {
                        viewModel.fetchReceipts()
                    },
                    leadingIcon = { modifier ->
                        if (receipt.receiptId.isNotEmpty()) {
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
                    viewModel.currencyIndexState.intValue = receipt.getSelectedCurrencyIndex()
                    viewModel.currentReceipt = receipt.copy()
                    viewModel.updateReceipt(receipt.copy())
                }
            }
        }
    }
}