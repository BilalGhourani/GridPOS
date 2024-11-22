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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.grid.pos.ActivityScopedViewModel
import com.grid.pos.R
import com.grid.pos.data.Receipt.Receipt
import com.grid.pos.data.ThirdParty.ThirdParty
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
        activityScopedViewModel: ActivityScopedViewModel,
        viewModel: ReceiptsViewModel = hiltViewModel()
) {
    val state by viewModel.receiptsState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val amountFocusRequester = remember { FocusRequester() }
    val descFocusRequester = remember { FocusRequester() }
    val noteFocusRequester = remember { FocusRequester() }

    var typeState by remember { mutableStateOf("") }
    var thirdPartyState by remember { mutableStateOf("") }
    var currencyState by remember { mutableStateOf("") }
    var currencyIndexState by remember { mutableIntStateOf(0) }
    var amountState by remember { mutableStateOf("") }
    var amountFirstState by remember { mutableStateOf("") }
    var amountSecondsState by remember { mutableStateOf("") }
    var descriptionState by remember { mutableStateOf("") }
    var noteState by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(state.warning) {
        state.warning?.value?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }

    LaunchedEffect(state.isLoading) {
        activityScopedViewModel.showLoading(state.isLoading)
    }

    fun clear() {
        viewModel.currentReceipt = Receipt()
        state.selectedReceipt = Receipt()
        thirdPartyState = ""
        typeState = viewModel.getDefaultType()
        currencyState = ""
        currencyIndexState = 0
        amountState = ""
        descriptionState = ""
        noteState = ""
        state.clear = false
    }

    var saveAndBack by remember { mutableStateOf(false) }
    fun handleBack() {
        if (state.isLoading) {
            return
        }
        if (state.selectedReceipt.didChanged(
                viewModel.currentReceipt
            )
        ) {
            activityScopedViewModel.showPopup(true,
                PopupModel().apply {
                    onDismissRequest = {
                        clear()
                        handleBack()
                    }
                    onConfirmation = {
                        saveAndBack = true
                        viewModel.saveReceipt(
                            context,
                            state.selectedReceipt
                        )
                    }
                    dialogText = "Do you want to save your changes"
                    positiveBtnText = "Save"
                    negativeBtnText = "Close"
                })
            return
        }
        if (state.thirdParties.isNotEmpty()) {
            activityScopedViewModel.thirdParties = state.thirdParties
        }
        navController?.navigateUp()
    }

    fun clearAndBack() {
        clear()
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
            activityScopedViewModel.reportsToPrint.clear()
            activityScopedViewModel.reportsToPrint.add(viewModel.reportResult)
            clear()
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
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
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
                        selectedId = typeState,
                        leadingIcon = { modifier ->
                            if (typeState.isNotEmpty()) {
                                Icon(
                                    Icons.Default.RemoveCircleOutline,
                                    contentDescription = "remove Type",
                                    tint = Color.Black,
                                    modifier = modifier
                                )
                            }
                        },
                        onLeadingIconClick = {
                            typeState = ""
                            state.selectedReceipt.receiptType = null
                        }) { typeModel ->
                        typeModel as PaymentTypeModel
                        typeState = typeModel.type
                        state.selectedReceipt.receiptType = typeModel.type
                    }

                    SearchableDropdownMenuEx(items = state.currencies.toMutableList(),
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        enableSearch = SettingsModel.isConnectedToSqlServer(),
                        label = "Select Currency",
                        selectedId = currencyState,
                        leadingIcon = { modifier ->
                            if (currencyState.isNotEmpty()) {
                                Icon(
                                    Icons.Default.RemoveCircleOutline,
                                    contentDescription = "remove Currency",
                                    tint = Color.Black,
                                    modifier = modifier
                                )
                            }
                        },
                        onLeadingIconClick = {
                            currencyState = ""
                            state.selectedReceipt.receiptCurrency = null
                            state.selectedReceipt.receiptCurrencyCode = null
                            currencyIndexState = 0
                        }) { currModel ->
                        currModel as CurrencyModel
                        currencyState = currModel.getId()
                        state.selectedReceipt.receiptCurrency = currencyState
                        state.selectedReceipt.receiptCurrencyCode = currModel.currencyCode
                        currencyIndexState = state.selectedReceipt.getSelectedCurrencyIndex()
                    }

                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = amountState,
                        label = "Amount",
                        placeHolder = "Enter Amount",
                        focusRequester = amountFocusRequester,
                        onAction = {
                            descFocusRequester.requestFocus()
                        }) { amount ->
                        amountState = Utils.getDoubleValue(
                            amount,
                            amountState
                        )
                        state.selectedReceipt.receiptAmount = amountState.toDoubleOrNull() ?: 0.0

                        if (currencyIndexState == 1) {
                            val second = state.selectedReceipt.receiptAmount.times(SettingsModel.currentCurrency?.currencyRate ?: 1.0)
                            amountSecondsState = POSUtils.formatDouble(
                                second,
                                SettingsModel.currentCurrency?.currencyName2Dec ?: 2
                            )
                            state.selectedReceipt.receiptAmountSecond = second
                        } else if (currencyIndexState == 2) {
                            val first = state.selectedReceipt.receiptAmount.div(SettingsModel.currentCurrency?.currencyRate ?: 1.0)
                            amountFirstState = POSUtils.formatDouble(
                                first,
                                SettingsModel.currentCurrency?.currencyName1Dec ?: 2
                            )
                            state.selectedReceipt.receiptAmountFirst = first
                        }
                    }

                    if (currencyIndexState != 1) {
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue = amountFirstState,
                            label = "Amount ${SettingsModel.currentCurrency?.currencyCode1 ?: ""}",
                            placeHolder = "Enter Amount",
                            focusRequester = amountFocusRequester,
                            keyboardType = KeyboardType.Decimal,
                            onAction = {
                                descFocusRequester.requestFocus()
                            }) { amount ->
                            amountFirstState = Utils.getDoubleValue(
                                amount,
                                amountFirstState
                            )
                            state.selectedReceipt.receiptAmountFirst = amountFirstState.toDoubleOrNull() ?: 0.0
                        }
                    }

                    if (currencyIndexState != 2) {
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue = amountSecondsState,
                            label = "Amount ${SettingsModel.currentCurrency?.currencyCode2 ?: ""}",
                            placeHolder = "Enter Amount",
                            focusRequester = amountFocusRequester,
                            keyboardType = KeyboardType.Decimal,
                            onAction = {
                                descFocusRequester.requestFocus()
                            }) { amount ->
                            amountSecondsState = Utils.getDoubleValue(
                                amount,
                                amountSecondsState
                            )
                            state.selectedReceipt.receiptAmountSecond = amountSecondsState.toDoubleOrNull() ?: 0.0
                        }
                    }

                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = descriptionState,
                        label = "Description",
                        placeHolder = "Enter Description",
                        focusRequester = descFocusRequester,
                        maxLines = 4,
                        imeAction = ImeAction.None,
                        onAction = { noteFocusRequester.requestFocus() }) { desc ->
                        descriptionState = desc
                        state.selectedReceipt.receiptDesc = desc
                    }

                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = noteState,
                        label = "Note",
                        placeHolder = "Enter Note",
                        maxLines = 4,
                        focusRequester = noteFocusRequester,
                        imeAction = ImeAction.None,
                        onAction = { keyboardController?.hide() }) { note ->
                        noteState = note
                        state.selectedReceipt.receiptNote = note
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
                            viewModel.saveReceipt(
                                context,
                                state.selectedReceipt
                            )
                        }

                        UIImageButton(
                            modifier = Modifier
                                .weight(.33f)
                                .padding(3.dp),
                            icon = R.drawable.delete,
                            text = "Delete"
                        ) {
                            viewModel.deleteSelectedReceipt()
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
                    selectedId = thirdPartyState,
                    onLoadItems = {
                        scope.launch(Dispatchers.IO) {
                            viewModel.fetchThirdParties()
                        }
                    },
                    leadingIcon = { modifier ->
                        if (thirdPartyState.isNotEmpty()) {
                            Icon(
                                Icons.Default.RemoveCircleOutline,
                                contentDescription = "remove ThirdParty",
                                tint = Color.Black,
                                modifier = modifier
                            )
                        }
                    },
                    onLeadingIconClick = {
                        thirdPartyState = ""
                        state.selectedReceipt.receiptThirdParty = null
                        state.selectedReceipt.receiptThirdPartyName = null
                    }) { thirdParty ->
                    thirdParty as ThirdParty
                    thirdPartyState = thirdParty.thirdPartyId
                    state.selectedReceipt.receiptThirdParty = thirdPartyState
                    state.selectedReceipt.receiptThirdPartyName = thirdParty.thirdPartyName
                }

                SearchableDropdownMenuEx(items = state.receipts.toMutableList(),
                    modifier = Modifier.padding(
                        top = 15.dp,
                        start = 10.dp,
                        end = 10.dp
                    ),
                    label = "Select Receipt",
                    selectedId = state.selectedReceipt.receiptId,
                    onLoadItems = {
                        viewModel.fetchReceipts()
                    },
                    leadingIcon = { modifier ->
                        if (state.selectedReceipt.receiptId.isNotEmpty()) {
                            Icon(
                                Icons.Default.RemoveCircleOutline,
                                contentDescription = "remove Receipt",
                                tint = Color.Black,
                                modifier = modifier
                            )
                        }
                    },
                    onLeadingIconClick = {
                        clear()
                    }) { receipt ->
                    receipt as Receipt
                    receipt.receiptCurrencyCode = viewModel.getCurrencyCode(receipt.receiptCurrency)
                    viewModel.currentReceipt = receipt.copy()
                    state.selectedReceipt = receipt
                    thirdPartyState = receipt.receiptThirdParty ?: ""
                    typeState = receipt.receiptType ?: ""
                    currencyState = receipt.receiptCurrency ?: ""
                    currencyIndexState = receipt.getSelectedCurrencyIndex()
                    amountState = receipt.receiptAmount.toString()
                    amountFirstState = receipt.receiptAmountFirst.toString()
                    amountSecondsState = receipt.receiptAmountSecond.toString()
                    descriptionState = receipt.receiptDesc ?: ""
                    noteState = receipt.receiptNote ?: ""
                }
            }
        }
    }
}