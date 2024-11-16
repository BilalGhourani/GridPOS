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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.grid.pos.ActivityScopedViewModel
import com.grid.pos.R
import com.grid.pos.data.Payment.Payment
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.model.CurrencyModel
import com.grid.pos.model.PaymentTypeModel
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.ThirdPartyType
import com.grid.pos.model.ThirdPartyTypeModel
import com.grid.pos.ui.common.SearchableDropdownMenuEx
import com.grid.pos.ui.common.UIImageButton
import com.grid.pos.ui.common.UISwitch
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun PaymentsView(
        modifier: Modifier = Modifier,
        navController: NavController? = null,
        activityScopedViewModel: ActivityScopedViewModel,
        viewModel: PaymentsViewModel = hiltViewModel()
) {
    val state by viewModel.paymentsState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val amountFocusRequester = remember { FocusRequester() }
    val descFocusRequester = remember { FocusRequester() }
    val noteFocusRequester = remember { FocusRequester() }

    var typeState by remember { mutableStateOf(viewModel.getDefaultType()) }
    var thirdPartyState by remember { mutableStateOf("") }
    var currencyState by remember { mutableStateOf(SettingsModel.currentCurrency?.currencyCode1 ?: "") }
    var amountState by remember { mutableStateOf("") }
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
        viewModel.currentPayment = Payment()
        state.selectedPayment = Payment()
        thirdPartyState = ""
        typeState = viewModel.getDefaultType()
        currencyState = SettingsModel.currentCurrency?.currencyCode1 ?: ""
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
        if (state.selectedPayment.didChanged(
                viewModel.currentPayment
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
                        viewModel.savePayment(state.selectedPayment)
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
    LaunchedEffect(state.clear) {
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
                    SearchableDropdownMenuEx(
                        items = viewModel.paymentTypes,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        enableSearch = false,
                        label = "Select Type",
                        selectedId = typeState
                    ) { typeModel ->
                        typeModel as PaymentTypeModel
                        currencyState = typeModel.type
                        state.selectedPayment.paymentType = typeModel.type
                    }

                    if (state.currencies.isNotEmpty()) {
                        SearchableDropdownMenuEx(
                            items = state.currencies.toMutableList(),
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 5.dp
                            ),
                            onLoadItems = {
                                viewModel.fetchCurrencies()
                            },
                            enableSearch = SettingsModel.isConnectedToSqlServer(),
                            label = "Select Currency",
                            selectedId = currencyState
                        ) { currModel ->
                            currModel as CurrencyModel
                            currencyState = currModel.getId()
                            state.selectedPayment.paymentCurrency = currencyState
                        }
                    }

                    //financial number
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
                        state.selectedPayment.paymentAmount = amountState.toDoubleOrNull() ?: 0.0
                        state.selectedPayment.calculateAmountFirstAndSecond()
                    }

                    //phone1
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
                        state.selectedPayment.paymentDesc = desc
                    }

                    //phone2
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
                        state.selectedPayment.paymentNote = note
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
                            viewModel.savePayment(state.selectedPayment)
                        }

                        UIImageButton(
                            modifier = Modifier
                                .weight(.33f)
                                .padding(3.dp),
                            icon = R.drawable.delete,
                            text = "Delete"
                        ) {
                            viewModel.deleteSelectedPayment()
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
                    onLoadItems = { viewModel.fetchThirdParties() },
                    leadingIcon = {
                        if (thirdPartyState.isNotEmpty()) {
                            Icon(
                                Icons.Default.RemoveCircleOutline,
                                contentDescription = "remove ThirdParty",
                                tint = Color.Black,
                                modifier = it
                            )
                        }
                    },
                    onLeadingIconClick = {
                        thirdPartyState = ""
                        state.selectedPayment.paymentThirdParty = null
                        state.selectedPayment.paymentThirdPartyName = null
                    }) { thirdParty ->
                    thirdParty as ThirdParty
                    thirdPartyState = thirdParty.thirdPartyId
                    state.selectedPayment.paymentThirdParty = thirdPartyState
                    state.selectedPayment.paymentThirdPartyName = thirdParty.thirdPartyName
                }

                SearchableDropdownMenuEx(items = state.payments.toMutableList(),
                    modifier = Modifier.padding(
                        top = 15.dp,
                        start = 10.dp,
                        end = 10.dp
                    ),
                    label = "Select Payment",
                    selectedId = state.selectedPayment.paymentId,
                    onLoadItems = { viewModel.fetchPayments() },
                    leadingIcon = {
                        if (state.selectedPayment.paymentId.isNotEmpty()) {
                            Icon(
                                Icons.Default.RemoveCircleOutline,
                                contentDescription = "remove payment",
                                tint = Color.Black,
                                modifier = it
                            )
                        }
                    },
                    onLeadingIconClick = {
                        clear()
                    }) { payment ->
                    payment as Payment
                    viewModel.currentPayment = payment.copy()
                    state.selectedPayment = payment
                    thirdPartyState = payment.paymentThirdParty ?: ""
                    typeState = payment.paymentType ?: ""
                    currencyState = payment.paymentCurrency ?: SettingsModel.currentCurrency?.currencyCode1 ?: ""
                    amountState = payment.paymentAmount.toString()
                    descriptionState = payment.paymentDesc ?: ""
                    noteState = payment.paymentNote ?: ""
                }
            }
        }
    }
}