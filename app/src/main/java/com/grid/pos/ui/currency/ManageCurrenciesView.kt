package com.grid.pos.ui.currency

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.grid.pos.SharedViewModel
import com.grid.pos.R
import com.grid.pos.data.currency.Currency
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.UIImageButton
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.pos.POSUtils
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun ManageCurrenciesView(
        modifier: Modifier = Modifier,
        navController: NavController? = null,
        sharedViewModel: SharedViewModel,
        viewModel: ManageCurrenciesViewModel = hiltViewModel()
) {
    val state by viewModel.manageCurrenciesState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val curName1FocusRequester = remember { FocusRequester() }
    val curName1DecFocusRequester = remember { FocusRequester() }
    val curCode2FocusRequester = remember { FocusRequester() }
    val curName2FocusRequester = remember { FocusRequester() }
    val curName2DecFocusRequester = remember { FocusRequester() }
    val rateFocusRequester = remember { FocusRequester() }

    var curCode1State by remember { mutableStateOf(state.selectedCurrency.currencyCode1 ?: "") }
    var curName1State by remember { mutableStateOf(state.selectedCurrency.currencyName1 ?: "") }
    var curName1DecState by remember {
        mutableStateOf(
            state.selectedCurrency.currencyName1Dec.toString()
        )
    }
    var curCode2State by remember { mutableStateOf(state.selectedCurrency.currencyCode2 ?: "") }
    var curName2State by remember { mutableStateOf(state.selectedCurrency.currencyName2 ?: "") }
    var curName2DecState by remember {
        mutableStateOf(
            state.selectedCurrency.currencyName2Dec.toString()
        )
    }
    var rateState by remember {
        mutableStateOf(
            POSUtils.formatDouble(state.selectedCurrency.currencyRate)
        )
    }

    var saveAndBack by remember { mutableStateOf(false) }
    fun handleBack() {
        if(state.isLoading){
            return
        }
        if (state.selectedCurrency.didChanged(
                viewModel.currentCurrency
            )
        ) {
            sharedViewModel.showPopup(true,
                PopupModel().apply {
                    onDismissRequest = {
                        viewModel.currentCurrency = Currency()
                        handleBack()
                    }
                    onConfirmation = {
                        saveAndBack = true
                        viewModel.saveCurrency()
                    }
                    dialogText = "Do you want to save your changes"
                    positiveBtnText = "Save"
                    negativeBtnText = "Close"
                })
            return
        }
        viewModel.closeConnectionIfNeeded()
        navController?.navigateUp()
    }
    BackHandler {
        handleBack()
    }

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
        sharedViewModel.showLoading(state.isLoading)
    }
    LaunchedEffect(state.isSaved) {
        if (state.isSaved && saveAndBack) {
            viewModel.currentCurrency = state.selectedCurrency
            handleBack()
        }
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
                                text = "Manage Currencies",
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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .padding(10.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            UITextField(modifier = Modifier
                                .fillMaxHeight()
                                .weight(.3f)
                                .padding(
                                    horizontal = 5.dp
                                ),
                                defaultValue = curCode1State,
                                label = "Cur1 Code",
                                placeHolder = "Code",
                                onAction = { curName1FocusRequester.requestFocus() }) { curCode1 ->
                                curCode1State = curCode1
                                state.selectedCurrency.currencyCode1 = curCode1
                            }

                            UITextField(modifier = Modifier
                                .fillMaxHeight()
                                .weight(.5f)
                                .padding(
                                    horizontal = 5.dp
                                ),
                                defaultValue = curName1State,
                                label = "Cur1 Name",
                                placeHolder = "Name",
                                focusRequester = curName1FocusRequester,
                                onAction = { curName1DecFocusRequester.requestFocus() }) { curName1 ->
                                curName1State = curName1
                                state.selectedCurrency.currencyName1 = curName1
                            }

                            UITextField(modifier = Modifier
                                .fillMaxHeight()
                                .weight(.2f)
                                .padding(
                                    horizontal = 5.dp
                                ),
                                defaultValue = curName1DecState,
                                keyboardType = KeyboardType.Decimal,
                                label = "Decimal",
                                focusRequester = curName1DecFocusRequester,
                                onAction = { curCode2FocusRequester.requestFocus() }) { curName1Dec ->
                                curName1DecState = Utils.getIntValue(
                                    curName1Dec,
                                    curName1DecState
                                )
                                state.selectedCurrency.currencyName1Dec = curName1DecState.toIntOrNull() ?: 0
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .padding(10.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            UITextField(modifier = Modifier
                                .fillMaxHeight()
                                .weight(.3f)
                                .padding(
                                    horizontal = 5.dp
                                ),
                                defaultValue = curCode2State,
                                label = "Cur2 Code",
                                placeHolder = "Code",
                                focusRequester = curCode2FocusRequester,
                                onAction = { curName2FocusRequester.requestFocus() }) { curCode2 ->
                                curCode2State = curCode2
                                state.selectedCurrency.currencyCode2 = curCode2
                            }

                            UITextField(modifier = Modifier
                                .fillMaxHeight()
                                .weight(.5f)
                                .padding(
                                    horizontal = 5.dp
                                ),
                                defaultValue = curName2State,
                                label = "Cur2 Name",
                                placeHolder = "Name",
                                focusRequester = curName2FocusRequester,
                                onAction = { curName2DecFocusRequester.requestFocus() }) { curName2 ->
                                curName2State = curName2
                                state.selectedCurrency.currencyName2 = curName2
                            }

                            UITextField(modifier = Modifier
                                .fillMaxHeight()
                                .weight(.2f)
                                .padding(
                                    horizontal = 5.dp
                                ),
                                defaultValue = curName2DecState,
                                keyboardType = KeyboardType.Decimal,
                                label = "Decimal",
                                focusRequester = curName2DecFocusRequester,
                                onAction = { rateFocusRequester.requestFocus() }) { curName2Dec ->
                                curName2DecState = Utils.getIntValue(
                                    curName2Dec,
                                    curName2DecState
                                )
                                state.selectedCurrency.currencyName2Dec = curName2DecState.toIntOrNull() ?: 0
                            }
                        }

                        UITextField(modifier = Modifier.padding(10.dp),
                            defaultValue = rateState,
                            keyboardType = KeyboardType.Decimal,
                            label = "Rate",
                            placeHolder = "Enter Rate",
                            focusRequester = rateFocusRequester,
                            imeAction = ImeAction.Done,
                            onAction = { keyboardController?.hide() }) { rateStr ->
                            rateState = Utils.getDoubleValue(
                                rateStr,
                                rateState
                            )
                            state.selectedCurrency.currencyRate = rateState.toDoubleOrNull() ?: 0.0
                        }


                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(10.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            UIImageButton(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(3.dp),
                                icon = R.drawable.save,
                                text = "Save"
                            ) {
                                viewModel.currentCurrency = state.selectedCurrency
                                viewModel.saveCurrency()
                            }

                            UIImageButton(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(3.dp),
                                icon = R.drawable.go_back,
                                text = "Close"
                            ) {
                                handleBack()
                            }
                        }
                    }
                }
            }
        }
        if (state.fillFields) {
            viewModel.currentCurrency = state.selectedCurrency
            curCode1State = state.selectedCurrency.currencyCode1 ?: ""
            curName1State = state.selectedCurrency.currencyName1 ?: ""
            curName1DecState = state.selectedCurrency.currencyName1Dec.toString()
            curCode2State = state.selectedCurrency.currencyCode2 ?: ""
            curName2State = state.selectedCurrency.currencyName2 ?: ""
            curName2DecState = state.selectedCurrency.currencyName2Dec.toString()
            rateState = POSUtils.formatDouble(state.selectedCurrency.currencyRate)
            state.fillFields = false
        }
    }
}
