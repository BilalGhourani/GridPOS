package com.grid.pos.ui.table

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.grid.pos.ActivityScopedViewModel
import com.grid.pos.MainActivity
import com.grid.pos.R
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.UserType
import com.grid.pos.ui.common.LoadingIndicator
import com.grid.pos.ui.common.UIAlertDialog
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TablesView(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    activityScopedViewModel: ActivityScopedViewModel,
    mainActivity: MainActivity,
    viewModel: TablesViewModel = hiltViewModel()
) {
    val tablesState: TablesState by viewModel.tablesState.collectAsState(
        TablesState()
    )

    val keyboardController = LocalSoftwareKeyboardController.current

    val tableNameFocusRequester = remember { FocusRequester() }
    val clientsCountFocusRequester = remember { FocusRequester() }

    var tableNameState by remember { mutableStateOf("") }
    var clientsCountState by remember { mutableStateOf("") }
    var stepState by remember { mutableStateOf(1) }

    var isPopupVisible by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(stepState) {
        if (stepState == 1) {
            tableNameFocusRequester.requestFocus()
        } else {
            clientsCountFocusRequester.requestFocus()
        }
    }
    LaunchedEffect(tablesState.warning) {
        tablesState.warning?.value?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }

    fun handleBack() {
        if (stepState > 1) {
            stepState = 1
        } else {
            if (SettingsModel.getUserType() == UserType.TABLE) {
                isPopupVisible = true
            } else {
                navController?.navigateUp()
            }
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
                    TopAppBar(colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = SettingsModel.topBarColor),
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
                                text = "Tables",
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
            Column(
                modifier = modifier.padding(it)
            ) {
                if (stepState <= 1) {
                    UITextField(modifier = Modifier.padding(10.dp),
                        defaultValue = tableNameState,
                        onFocusChanged = {
                            if (it.hasFocus) {
                                keyboardController?.show()
                            }
                        },
                        label = "Table Number",
                        maxLines = 3,
                        focusRequester = tableNameFocusRequester,
                        keyboardType = KeyboardType.Decimal,
                        placeHolder = "Enter Table Number",
                        onAction = {
                            viewModel.fetchInvoiceByTable(tableNameState)
                        },
                        trailingIcon = {
                            IconButton(onClick = { }) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "Search",
                                    tint = SettingsModel.buttonColor
                                )
                            }
                        }) { tabNo ->
                        tableNameState = Utils.getDoubleValue(
                            tabNo,
                            tableNameState
                        )

                    }
                } else {
                    UITextField(modifier = Modifier.padding(10.dp),
                        defaultValue = clientsCountState,
                        onFocusChanged = {
                            if (it.hasFocus) {
                                keyboardController?.show()
                            }
                        },
                        label = "Client Number",
                        maxLines = 3,
                        focusRequester = clientsCountFocusRequester,
                        keyboardType = KeyboardType.Decimal,
                        placeHolder = "Enter Client Number",
                        imeAction = ImeAction.Done,
                        onAction = {
                            keyboardController?.hide()
                        }) { clients ->
                        clientsCountState = Utils.getDoubleValue(
                            clients,
                            clientsCountState
                        )
                    }
                }
                UIButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(10.dp),
                    text = "Submit"
                ) {
                    if (stepState <= 1) {
                        viewModel.fetchInvoiceByTable(tableNameState)
                    } else {
                        if (clientsCountState.toIntOrNull() == null) {
                            viewModel.showError("Please enter client counts")
                            return@UIButton
                        }
                        tablesState.invoiceHeader.invoiceHeadTaName = tableNameState
                        tablesState.invoiceHeader.invoiceHeadClientsCount =
                            clientsCountState.toIntOrNull() ?: 1
                        activityScopedViewModel.invoiceHeader = tablesState.invoiceHeader
                        activityScopedViewModel.shouldLoadInvoice = true
                        activityScopedViewModel.isFromTable = true
                        tablesState.clear = true
                        navController?.navigate("POSView")
                    }

                }
            }
        }

        AnimatedVisibility(
            visible = isPopupVisible,
            enter = fadeIn(
                initialAlpha = 0.4f
            ),
            exit = fadeOut(
                animationSpec = tween(durationMillis = 250)
            )
        ) {
            UIAlertDialog(
                onDismissRequest = {
                    isPopupVisible = false
                },
                onConfirmation = {
                    isPopupVisible = false

                    activityScopedViewModel.logout()
                    navController?.clearBackStack("LoginView")
                    navController?.navigate("LoginView")
                },
                dialogTitle = "Alert.",
                dialogText = "Are you sure you want to logout?",
                positiveBtnText = "Logout",
                negativeBtnText = "Cancel",
                icon = Icons.Default.Info
            )
        }

        LoadingIndicator(
            show = tablesState.isLoading
        )

        if (tablesState.clear) {
            tablesState.invoiceHeader = InvoiceHeader()
            stepState = 1
            tableNameState = ""
            clientsCountState = ""
            tablesState.clear = false
        }
    }
}