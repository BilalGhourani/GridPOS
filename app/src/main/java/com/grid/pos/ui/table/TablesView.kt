package com.grid.pos.ui.table

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.grid.pos.ActivityScopedViewModel
import com.grid.pos.MainActivity
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.User.User
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.LoadingIndicator
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.ui.user.ManageUsersState
import com.grid.pos.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    var tableNameState by remember { mutableStateOf("") }
    var clientsCountState by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(tablesState.warning) {
        if (!tablesState.warning.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.Main).launch {
                snackbarHostState.showSnackbar(
                    message = tablesState.warning!!,
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }

    fun handleBack() {
        if (tablesState.step > 1) {
            tablesState.step = 1
        } else {
            if (SettingsModel.currentUser?.userTableMode == true && SettingsModel.currentUser?.userPosMode == false) {
                mainActivity.finish()
            } else {
                navController?.popBackStack()
            }
        }
    }
    BackHandler {
        handleBack()
    }

    GridPOSTheme {
        Scaffold(
            containerColor = SettingsModel.backgroundColor,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            topBar = {
                Surface(shadowElevation = 3.dp, color = SettingsModel.backgroundColor) {
                    TopAppBar(
                        colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = SettingsModel.topBarColor),
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
                        })
                }
            }
        ) {
            Column(
                modifier = modifier.padding(it)
            ) {
                if (tablesState.step <= 1) {
                    UITextField(
                        modifier = Modifier.padding(10.dp),
                        defaultValue = tableNameState,
                        label = "Table Number",
                        maxLines = 3,
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
                            tabNo, tableNameState
                        )

                    }
                } else {
                    UITextField(
                        modifier = Modifier.padding(10.dp),
                        defaultValue = clientsCountState,
                        label = "Client Number",
                        maxLines = 3,
                        keyboardType = KeyboardType.Decimal,
                        placeHolder = "Enter Client Number",
                        imeAction = ImeAction.Done,
                        onAction = {
                            keyboardController?.hide()
                        }) { clients ->
                        clientsCountState = Utils.getDoubleValue(
                            clients, clientsCountState
                        )

                    }
                }
                UIButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(10.dp), text = "Submit"
                ) {
                    if (tablesState.step <= 1) {
                        viewModel.fetchInvoiceByTable(tableNameState)
                    } else {
                        tablesState.invoiceHeader.invoiceHeadTaName = tableNameState
                        tablesState.invoiceHeader.invoiceHeadClientsCount =
                            clientsCountState.toIntOrNull() ?: 1
                        activityScopedViewModel.posState.invoiceHeader = tablesState.invoiceHeader
                        activityScopedViewModel.isFromTable = true
                        navController?.navigate("PosView")
                    }

                }
            }
        }
        LoadingIndicator(
            show = tablesState.isLoading
        )

        if (tablesState.clear) {
            tablesState.invoiceHeader = InvoiceHeader()
            tablesState.step = 1
            tableNameState = ""
            clientsCountState = ""
            tablesState.clear = false
        }
    }
}