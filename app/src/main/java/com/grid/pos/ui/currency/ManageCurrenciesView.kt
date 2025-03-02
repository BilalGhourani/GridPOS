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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.grid.pos.R
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.UIImageButton
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.navigation.Screen
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun ManageCurrenciesView(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    viewModel: ManageCurrenciesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val keyboardController = LocalSoftwareKeyboardController.current
    val curName1FocusRequester = remember { FocusRequester() }
    val curName1DecFocusRequester = remember { FocusRequester() }
    val curCode2FocusRequester = remember { FocusRequester() }
    val curName2FocusRequester = remember { FocusRequester() }
    val curName2DecFocusRequester = remember { FocusRequester() }
    val rateFocusRequester = remember { FocusRequester() }
    var isBackPressed by remember { mutableStateOf(false) }

    fun handleBack() {
        if (viewModel.isLoading()) {
            return
        }
        if (isBackPressed) {
            return
        }
        isBackPressed = true
        viewModel.checkChanges {
            navController?.navigateUp()
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
                                text = "Manage Currencies",
                                color = SettingsModel.textColor,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        },
                        actions = {
                            IconButton(onClick = { navController?.navigate(Screen.SettingsView.route) }) {
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
                                defaultValue = state.currency.currencyCode1 ?: "",
                                label = "Cur1 Code",
                                placeHolder = "Code",
                                onAction = { curName1FocusRequester.requestFocus() }) { curCode1 ->
                                viewModel.updateState(
                                    state.copy(
                                        currency = state.currency.copy(
                                            currencyCode1 = curCode1
                                        )
                                    )
                                )
                            }

                            UITextField(modifier = Modifier
                                .fillMaxHeight()
                                .weight(.5f)
                                .padding(
                                    horizontal = 5.dp
                                ),
                                defaultValue = state.currency.currencyName1 ?: "",
                                label = "Cur1 Name",
                                placeHolder = "Name",
                                focusRequester = curName1FocusRequester,
                                onAction = { curName1DecFocusRequester.requestFocus() }) { curName1 ->
                                viewModel.updateState(
                                    state.copy(
                                        currency =
                                        state.currency.copy(
                                            currencyName1 = curName1
                                        )
                                    )
                                )
                            }

                            UITextField(modifier = Modifier
                                .fillMaxHeight()
                                .weight(.2f)
                                .padding(
                                    horizontal = 5.dp
                                ),
                                defaultValue = state.currencyName1DecStr,
                                keyboardType = KeyboardType.Decimal,
                                label = "Decimal",
                                focusRequester = curName1DecFocusRequester,
                                onAction = { curCode2FocusRequester.requestFocus() }) { curName1Dec ->
                                viewModel.updateState(
                                    state.copy(
                                        currency = state.currency.copy(
                                            currencyName1Dec = curName1Dec.toIntOrNull()
                                                ?: state.currency.currencyName1Dec
                                        ),
                                        currencyName1DecStr = Utils.getIntValue(
                                            curName1Dec,
                                            state.currencyName1DecStr
                                        )
                                    )
                                )
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
                                defaultValue = state.currency.currencyCode2 ?: "",
                                label = "Cur2 Code",
                                placeHolder = "Code",
                                focusRequester = curCode2FocusRequester,
                                onAction = { curName2FocusRequester.requestFocus() }) { curCode2 ->
                                viewModel.updateState(
                                    state.copy(
                                        currency = state.currency.copy(
                                            currencyCode2 = curCode2
                                        )
                                    )
                                )
                            }

                            UITextField(modifier = Modifier
                                .fillMaxHeight()
                                .weight(.5f)
                                .padding(
                                    horizontal = 5.dp
                                ),
                                defaultValue = state.currency.currencyName2 ?: "",
                                label = "Cur2 Name",
                                placeHolder = "Name",
                                focusRequester = curName2FocusRequester,
                                onAction = { curName2DecFocusRequester.requestFocus() }) { curName2 ->
                                viewModel.updateState(
                                    state.copy(
                                        currency = state.currency.copy(
                                            currencyName2 = curName2
                                        )
                                    )
                                )
                            }

                            UITextField(modifier = Modifier
                                .fillMaxHeight()
                                .weight(.2f)
                                .padding(
                                    horizontal = 5.dp
                                ),
                                defaultValue = state.currencyName2DecStr,
                                keyboardType = KeyboardType.Decimal,
                                label = "Decimal",
                                focusRequester = curName2DecFocusRequester,
                                onAction = { rateFocusRequester.requestFocus() }) { curName2Dec ->
                                viewModel.updateState(
                                    state.copy(
                                        currency = state.currency.copy(
                                            currencyName2Dec = curName2Dec.toIntOrNull()
                                                ?: state.currency.currencyName2Dec,
                                        ),
                                        currencyName2DecStr = Utils.getIntValue(
                                            curName2Dec,
                                            state.currencyName2DecStr
                                        )
                                    )
                                )
                            }
                        }

                        UITextField(modifier = Modifier.padding(10.dp),
                            defaultValue = state.currencyRateStr,
                            keyboardType = KeyboardType.Decimal,
                            label = "Rate",
                            placeHolder = "Enter Rate",
                            focusRequester = rateFocusRequester,
                            imeAction = ImeAction.Done,
                            onAction = { keyboardController?.hide() }) { rateStr ->
                            viewModel.updateState(
                                state.copy(
                                    currency = state.currency.copy(
                                        currencyRate = rateStr.toDoubleOrNull()
                                            ?: state.currency.currencyRate
                                    ),
                                    currencyRateStr = Utils.getDoubleValue(
                                        rateStr,
                                        state.currencyRateStr
                                    )
                                )

                            )
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
    }
}
