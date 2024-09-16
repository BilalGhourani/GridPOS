package com.grid.pos.ui.table

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.grid.pos.ActivityScopedViewModel
import com.grid.pos.R
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.UserType
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.settings.setupReports.ReportListCell
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TablesView(
        modifier: Modifier = Modifier,
        navController: NavController? = null,
        activityScopedViewModel: ActivityScopedViewModel,
        viewModel: TablesViewModel = hiltViewModel()
) {
    val state by viewModel.tablesState.collectAsStateWithLifecycle()

    val keyboardController = LocalSoftwareKeyboardController.current

    val tableNameFocusRequester = remember { FocusRequester() }
    val clientsCountFocusRequester = remember { FocusRequester() }

    var tableNameState by remember { mutableStateOf("") }
    var clientsCountState by remember { mutableStateOf("") }
    var stepState by remember { mutableIntStateOf(1) }

    var isPopupVisible by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit) {
        viewModel.fetchAllTables()
    }

    fun moveToPos() {
        state.invoiceHeader.invoiceHeadTaName = tableNameState
        state.invoiceHeader.invoiceHeadClientsCount = clientsCountState.toIntOrNull() ?: 1
        activityScopedViewModel.invoiceHeader = state.invoiceHeader
        activityScopedViewModel.shouldLoadInvoice = true
        activityScopedViewModel.isFromTable = true
        state.clear = true
        stepState = 1
        state.step = 1
        navController?.navigate("POSView")
    }

    LaunchedEffect(
        stepState,
        state.moveToPos
    ) {
        if (stepState == 1) {
            tableNameFocusRequester.requestFocus()
        } else {
            clientsCountFocusRequester.requestFocus()
        }
        if (state.moveToPos) {
            moveToPos()
            state.moveToPos = false
        }
    }
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

    LaunchedEffect(isPopupVisible) {
        activityScopedViewModel.showPopup(isPopupVisible,
            if (!isPopupVisible) null else PopupModel().apply {
                onDismissRequest = {
                    isPopupVisible = false
                }
                onConfirmation = {
                    isPopupVisible = false

                    activityScopedViewModel.logout()
                    navController?.clearBackStack("LoginView")
                    navController?.navigate("LoginView")
                }
                dialogText = "Are you sure you want to logout?"
                positiveBtnText = "Logout"
                negativeBtnText = "Cancel"
                height = 100.dp
            })
    }

    LaunchedEffect(state.step) {
        stepState = state.step
    }

    fun handleBack() {
        if (stepState > 1) {
            stepState = 1
            state.step = 1
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
                UITextField(modifier = Modifier.padding(10.dp),
                    defaultValue = tableNameState,
                    label = "Table Number",
                    maxLines = 3,
                    enabled = stepState <= 1,
                    focusRequester = tableNameFocusRequester,
                    keyboardType = KeyboardType.Decimal,
                    placeHolder = "Enter Table Number",
                    onAction = {
                        viewModel.fetchInvoiceByTable(tableNameState)
                    },
                    trailingIcon = {
                        IconButton(enabled = stepState <= 1,
                            onClick = { }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = if (stepState <= 1) SettingsModel.buttonColor else Color.LightGray
                            )
                        }
                    }) { tabNo ->
                    tableNameState = Utils.getDoubleValue(
                        tabNo,
                        tableNameState
                    )

                }
                if (stepState > 1) {
                    UITextField(modifier = Modifier.padding(10.dp),
                        defaultValue = clientsCountState,
                        onFocusChanged = { focusState ->
                            if (focusState.hasFocus) {
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
                        moveToPos()
                    }
                }

                LazyColumn(
                    modifier = modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    state.tables.forEach { tableModel ->
                        item {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .padding(
                                        horizontal = 10.dp,
                                        vertical = 5.dp
                                    )
                                    .border(
                                        border = BorderStroke(
                                            1.dp,
                                            Color.Black
                                        ),
                                        shape = RoundedCornerShape(15.dp)
                                    )
                                    .clickable {
                                        tableNameState = tableModel.table_name
                                    },
                                text = tableModel.getName(),
                                maxLines = 1,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                color = SettingsModel.textColor,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }


        if (state.clear) {
            state.invoiceHeader = InvoiceHeader()
            stepState = 1
            tableNameState = ""
            clientsCountState = ""
            state.clear = false
        }
    }
}