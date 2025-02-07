package com.grid.pos.ui.payments

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
import com.grid.pos.data.payment.Payment
import com.grid.pos.data.thirdParty.ThirdParty
import com.grid.pos.model.CurrencyModel
import com.grid.pos.model.PaymentTypeModel
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.ToastModel
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
fun PaymentsView(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    sharedViewModel: SharedViewModel,
    viewModel: PaymentsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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
        navController?.navigateUp()
    }

    fun clearAndBack() {
        viewModel.resetState()
        if (saveAndBack) {
            handleBack()
        }
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
                                text = "Payments",
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
                    SearchableDropdownMenuEx(items = viewModel.paymentTypes,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        enableSearch = false,
                        label = "Select Type",
                        selectedId = state.payment.paymentType,
                        leadingIcon = { modifier ->
                            if (!state.payment.paymentType.isNullOrEmpty()) {
                                Icon(
                                    Icons.Default.RemoveCircleOutline,
                                    contentDescription = "remove Type",
                                    tint = Color.Black,
                                    modifier = modifier
                                )
                            }
                        },
                        onLeadingIconClick = {
                            viewModel.updatePayment(
                                state.payment.copy(
                                    paymentType = null
                                )
                            )
                        }) { typeModel ->
                        typeModel as PaymentTypeModel
                        viewModel.updatePayment(
                            state.payment.copy(
                                paymentType = typeModel.type
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
                        selectedId = state.payment.paymentCurrency,
                        leadingIcon = { modifier ->
                            if (!state.payment.paymentCurrency.isNullOrEmpty()) {
                                Icon(
                                    Icons.Default.RemoveCircleOutline,
                                    contentDescription = "remove Currency",
                                    tint = Color.Black,
                                    modifier = modifier
                                )
                            }
                        },
                        onLeadingIconClick = {
                            viewModel.updatePayment(
                                state.payment.copy(
                                    paymentCurrency = null,
                                    paymentCurrencyCode = null
                                ),
                                currIndex = 0
                            )
                        }) { currModel ->
                        currModel as CurrencyModel
                        viewModel.updatePayment(
                            state.payment.copy(
                                paymentCurrency = currModel.getId(),
                                paymentCurrencyCode = currModel.currencyCode
                            ),
                            currIndex = state.payment.getSelectedCurrencyIndex()
                        )
                    }

                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = state.payment.paymentAmountStr ?: "",
                        label = "Amount",
                        placeHolder = "Enter Amount",
                        focusRequester = amountFocusRequester,
                        keyboardType = KeyboardType.Decimal,
                        onAction = {
                            descFocusRequester.requestFocus()
                        }) { amount ->
                        val amountStr = Utils.getDoubleValue(
                            amount,
                            state.payment.paymentAmountStr ?: ""
                        )
                        val paymentAmount = amountStr.toDoubleOrNull() ?: 0.0
                        var amountFirst = state.payment.paymentAmountFirst
                        var amountSecond = state.payment.paymentAmountSecond
                        if (state.currencyIndex == 1) {
                            amountSecond = paymentAmount.times(
                                SettingsModel.currentCurrency?.currencyRate ?: 1.0
                            )
                        } else if (state.currencyIndex == 2) {
                            amountFirst = paymentAmount.div(
                                SettingsModel.currentCurrency?.currencyRate ?: 1.0
                            )
                        }
                        viewModel.updatePayment(
                            state.payment.copy(
                                paymentAmount = paymentAmount,
                                paymentAmountStr = amountStr,
                                paymentAmountFirst = amountFirst,
                                paymentAmountFirstStr = POSUtils.formatDouble(
                                    amountFirst,
                                    SettingsModel.currentCurrency?.currencyName1Dec ?: 2
                                ),
                                paymentAmountSecond = amountSecond,
                                paymentAmountSecondStr = POSUtils.formatDouble(
                                    amountSecond,
                                    SettingsModel.currentCurrency?.currencyName2Dec ?: 2
                                )
                            )
                        )
                    }

                    if (state.currencyIndex != 1) {
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue = state.payment.paymentAmountFirstStr ?: "",
                            label = "Amount ${SettingsModel.currentCurrency?.currencyCode1 ?: ""}",
                            placeHolder = "Enter Amount",
                            focusRequester = amountFocusRequester,
                            keyboardType = KeyboardType.Decimal,
                            onAction = {
                                descFocusRequester.requestFocus()
                            }) { amount ->
                            val amountFirst =
                                amount.toDoubleOrNull() ?: state.payment.paymentAmountFirst
                            viewModel.updatePayment(
                                state.payment.copy(
                                    paymentAmountFirst = amountFirst,
                                    paymentAmountFirstStr = Utils.getDoubleValue(
                                        amount,
                                        state.payment.paymentAmountFirstStr ?: ""
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
                            defaultValue = state.payment.paymentAmountSecondStr ?: "",
                            label = "Amount ${SettingsModel.currentCurrency?.currencyCode2 ?: ""}",
                            placeHolder = "Enter Amount",
                            focusRequester = amountFocusRequester,
                            keyboardType = KeyboardType.Decimal,
                            onAction = {
                                descFocusRequester.requestFocus()
                            }) { amount ->
                            val amountSecond =
                                amount.toDoubleOrNull() ?: state.payment.paymentAmountSecond
                            viewModel.updatePayment(
                                state.payment.copy(
                                    paymentAmountSecond = amountSecond,
                                    paymentAmountSecondStr = Utils.getDoubleValue(
                                        amount,
                                        state.payment.paymentAmountSecondStr ?: ""
                                    )
                                )
                            )
                        }
                    }

                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = state.payment.paymentDesc ?: "",
                        label = "Description",
                        placeHolder = "Enter Description",
                        focusRequester = descFocusRequester,
                        maxLines = 4,
                        imeAction = ImeAction.None,
                        onAction = { noteFocusRequester.requestFocus() }) { desc ->
                        viewModel.updatePayment(
                            state.payment.copy(
                                paymentDesc = desc
                            )
                        )
                    }

                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = state.payment.paymentNote ?: "",
                        label = "Note",
                        placeHolder = "Enter Note",
                        maxLines = 4,
                        focusRequester = noteFocusRequester,
                        imeAction = ImeAction.None,
                        onAction = { keyboardController?.hide() }) { note ->
                        viewModel.updatePayment(
                            state.payment.copy(
                                paymentNote = note
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
                    selectedId = state.payment.paymentThirdParty,
                    onLoadItems = {
                        scope.launch(Dispatchers.IO) {
                            viewModel.fetchThirdParties()
                        }
                    },
                    leadingIcon = { modifier ->
                        if (!state.payment.paymentThirdParty.isNullOrEmpty()) {
                            Icon(
                                Icons.Default.RemoveCircleOutline,
                                contentDescription = "remove ThirdParty",
                                tint = Color.Black,
                                modifier = modifier
                            )
                        }
                    },
                    onLeadingIconClick = {
                        viewModel.updatePayment(
                            state.payment.copy(
                                paymentThirdParty = null,
                                paymentThirdPartyName = null
                            )
                        )
                    }) { thirdParty ->
                    thirdParty as ThirdParty
                    viewModel.updatePayment(
                        state.payment.copy(
                            paymentThirdParty = thirdParty.thirdPartyId,
                            paymentThirdPartyName = thirdParty.thirdPartyName
                        )
                    )
                }

                SearchableDropdownMenuEx(items = state.payments.toMutableList(),
                    modifier = Modifier.padding(
                        top = 15.dp,
                        start = 10.dp,
                        end = 10.dp
                    ),
                    label = "Select Payment",
                    selectedId = state.payment.paymentId,
                    onLoadItems = { viewModel.fetchPayments() },
                    leadingIcon = { modifier ->
                        if (state.payment.paymentId.isNotEmpty()) {
                            Icon(
                                Icons.Default.RemoveCircleOutline,
                                contentDescription = "remove payment",
                                tint = Color.Black,
                                modifier = modifier
                            )
                        }
                    },
                    onLeadingIconClick = {
                        viewModel.resetState()
                    }) { payment ->
                    payment as Payment
                    payment.paymentAmountStr = POSUtils.formatDouble(
                        payment.paymentAmount,
                        SettingsModel.currentCurrency?.currencyName1Dec ?: 2
                    )
                    payment.paymentAmountFirstStr = POSUtils.formatDouble(
                        payment.paymentAmountFirst,
                        SettingsModel.currentCurrency?.currencyName1Dec ?: 2
                    )
                    payment.paymentAmountSecondStr = POSUtils.formatDouble(
                        payment.paymentAmountSecond,
                        SettingsModel.currentCurrency?.currencyName2Dec ?: 2
                    )
                    payment.paymentCurrencyCode = viewModel.getCurrencyCode(payment.paymentCurrency)
                    viewModel.currentPayment = payment.copy()
                    viewModel.updatePayment(payment.copy(), payment.getSelectedCurrencyIndex())
                }
            }
        }
    }
}