package com.grid.pos.ui.currency

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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.grid.pos.data.Currency.Currency
import com.grid.pos.ui.common.LoadingIndicator
import com.grid.pos.ui.common.SearchableDropdownMenu
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.Blue
import com.grid.pos.ui.theme.GridPOSTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ManageCurrenciesView(
    navController: NavController? = null,
    modifier: Modifier = Modifier,
    viewModel: ManageCurrenciesViewModel = hiltViewModel()
) {
    val manageCurrenciesState: ManageCurrenciesState by viewModel.manageCurrenciesState.collectAsState(
        ManageCurrenciesState()
    )
    val keyboardController = LocalSoftwareKeyboardController.current
    val curName1FocusRequester = remember { FocusRequester() }
    val curCode2FocusRequester = remember { FocusRequester() }
    val curName2FocusRequester = remember { FocusRequester() }
    val rateFocusRequester = remember { FocusRequester() }

    var curCode1State by remember { mutableStateOf("") }
    var curName1State by remember { mutableStateOf("") }
    var curCode2State by remember { mutableStateOf("") }
    var curName2State by remember { mutableStateOf("") }
    var rateState by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(manageCurrenciesState.warning) {
        if (!manageCurrenciesState.warning.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.Main).launch {
                snackbarHostState.showSnackbar(
                    message = manageCurrenciesState.warning!!,
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }
    GridPOSTheme {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            topBar = {
                Surface(shadowElevation = 3.dp, color = Color.White) {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = { navController?.navigateUp() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        title = {
                            Text(
                                text = "Manage Currencies",
                                color = Color.Black,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        })
                }
            }
        ) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(it)
                    .background(color = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SearchableDropdownMenu(
                            items = manageCurrenciesState.currencies.toMutableList(),
                            modifier = Modifier.padding(10.dp),
                            label =
                            curName1State.ifEmpty { "Select Currency" },
                        ) { currency ->
                            currency as Currency
                            manageCurrenciesState.selectedCurrency = currency
                            curCode1State = currency.currencyCode1 ?: ""
                            curName1State = currency.currencyName1 ?: ""
                            curCode2State = currency.currencyCode2 ?: ""
                            curName2State = currency.currencyName2 ?: ""
                            rateState = currency.currencyRate ?: ""
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(10.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            UITextField(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .weight(.3f),
                                defaultValue = curCode1State,
                                label = "Cur1 Code",
                                placeHolder = "Code",
                                onAction = { curName1FocusRequester.requestFocus() }
                            ) { curCode1 ->
                                curCode1State = curCode1
                                manageCurrenciesState.selectedCurrency.currencyCode1 = curCode1
                            }

                            UITextField(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .weight(.6f),
                                defaultValue = curName1State,
                                label = "Cur1 Name",
                                placeHolder = "Name",
                                focusRequester = curName1FocusRequester,
                                onAction = { curCode2FocusRequester.requestFocus() }
                            ) { curName1 ->
                                curName1State = curName1
                                manageCurrenciesState.selectedCurrency.currencyName1 = curName1
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(10.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            UITextField(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .weight(.3f),
                                defaultValue = curCode2State,
                                label = "Cur2 Code",
                                placeHolder = "Code",
                                focusRequester = curCode2FocusRequester,
                                onAction = { curName2FocusRequester.requestFocus() }
                            ) { curCode2 ->
                                curCode2State = curCode2
                                manageCurrenciesState.selectedCurrency.currencyCode2 = curCode2
                            }

                            UITextField(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .weight(.6f),
                                defaultValue = curName2State,
                                label = "Cur2 Name",
                                placeHolder = "Name",
                                focusRequester = curName2FocusRequester,
                                onAction = { rateFocusRequester.requestFocus() }
                            ) { curName2 ->
                                curName2State = curName2
                                manageCurrenciesState.selectedCurrency.currencyName2 = curName2
                            }
                        }

                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = rateState,
                            keyboardType = KeyboardType.Decimal,
                            label = "Rate",
                            placeHolder = "Enter Rate",
                            focusRequester = rateFocusRequester,
                            imeAction = ImeAction.Done,
                            onAction = { keyboardController?.hide() }
                        ) { rateStr ->
                            rateState = rateStr
                            manageCurrenciesState.selectedCurrency.currencyRate = rateState
                        }


                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(10.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            ElevatedButton(
                                modifier = Modifier
                                    .weight(.33f)
                                    .padding(3.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                                onClick = { viewModel.saveCurrency(manageCurrenciesState.selectedCurrency) }
                            ) {
                                Text("Save")
                            }

                            ElevatedButton(
                                modifier = Modifier
                                    .weight(.33f)
                                    .padding(3.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                                onClick = { viewModel.deleteSelectedCurrency(manageCurrenciesState.selectedCurrency) }
                            ) {
                                Text("Delete")
                            }

                            ElevatedButton(
                                modifier = Modifier
                                    .weight(.33f)
                                    .padding(3.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                                onClick = { navController?.navigateUp() }
                            ) {
                                Text("Close")
                            }

                        }

                    }
                }
            }
        }
        LoadingIndicator(
            show = manageCurrenciesState.isLoading
        )
        if (manageCurrenciesState.clear) {
            manageCurrenciesState.selectedCurrency = Currency()
            curCode1State =  ""
            curName1State =  ""
            curCode2State =  ""
            curName2State = ""
            rateState =  ""
            manageCurrenciesState.clear = false
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ManageCurrenciesViewPreview() {
    GridPOSTheme {
        ManageCurrenciesView()
    }
}