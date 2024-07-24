package com.grid.pos.ui.posPrinter

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
import androidx.navigation.NavController
import com.grid.pos.ActivityScopedViewModel
import com.grid.pos.R
import com.grid.pos.data.PosPrinter.PosPrinter
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.LoadingIndicator
import com.grid.pos.ui.common.SearchableDropdownMenu
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun POSPrinterView(
        modifier: Modifier = Modifier,
        navController: NavController? = null,
        activityScopedViewModel: ActivityScopedViewModel,
        viewModel: POSPrinterViewModel = hiltViewModel()
) {
    val posPrinterState: POSPrinterState by viewModel.posPrinterState.collectAsState(
        POSPrinterState()
    )
    viewModel.fillCachedPrinters(activityScopedViewModel.printers)
    val keyboardController = LocalSoftwareKeyboardController.current

    val hostFocusRequester = remember { FocusRequester() }
    val portFocusRequester = remember { FocusRequester() }
    val typeFocusRequester = remember { FocusRequester() }

    var nameState by remember { mutableStateOf("") }
    var hostState by remember { mutableStateOf("") }
    var portState by remember { mutableStateOf("") }
    var typeState by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()



    LaunchedEffect(posPrinterState.warning) {
        posPrinterState.warning?.value?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short,
                    actionLabel = posPrinterState.actionLabel
                )
            }
        }
    }

    fun handleBack() {
        if (posPrinterState.printers.isNotEmpty()) {
            activityScopedViewModel.printers = posPrinterState.printers
        }
        navController?.navigateUp()
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
                                text = "Printers",
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
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SearchableDropdownMenu(
                            items = posPrinterState.printers.toMutableList(),
                            modifier = Modifier.padding(10.dp),
                            label = nameState.ifEmpty { "Select Printer" },
                        ) { printer ->
                            printer as PosPrinter
                            posPrinterState.selectedPrinter = printer
                            nameState = printer.posPrinterName ?: ""
                            hostState = printer.posPrinterHost
                            portState = printer.posPrinterPort.toString()
                            typeState = printer.posPrinterType ?: ""
                        }

                        UITextField(modifier = Modifier.padding(10.dp),
                            defaultValue = nameState,
                            label = "Name",
                            placeHolder = "Enter Name",
                            onAction = { hostFocusRequester.requestFocus() }) { name ->
                            nameState = name
                            posPrinterState.selectedPrinter.posPrinterName = nameState
                        }

                        UITextField(modifier = Modifier.padding(10.dp),
                            defaultValue = hostState,
                            label = "Host",
                            placeHolder = "ex:127.0.0.1",
                            onAction = { portFocusRequester.requestFocus() }) { host ->
                            hostState = host
                            posPrinterState.selectedPrinter.posPrinterHost = hostState
                        }

                        UITextField(modifier = Modifier.padding(10.dp),
                            defaultValue = portState,
                            label = "Port",
                            placeHolder = "ex:9100",
                            onAction = { typeFocusRequester.requestFocus() }) { port ->
                            portState = Utils.getIntValue(
                                port,
                                portState
                            )
                            posPrinterState.selectedPrinter.posPrinterPort = portState.toIntOrNull() ?: -1
                        }

                        UITextField(modifier = Modifier.padding(10.dp),
                            defaultValue = typeState,
                            label = "Type",
                            placeHolder = "Enter Type",
                            imeAction = ImeAction.Done,
                            onAction = { keyboardController?.hide() }) { type ->
                            typeState = type
                            posPrinterState.selectedPrinter.posPrinterType = typeState
                        }



                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(10.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            UIButton(
                                modifier = Modifier
                                    .weight(.33f)
                                    .padding(3.dp),
                                text = "Save"
                            ) {
                                viewModel.savePrinter(posPrinterState.selectedPrinter)
                            }

                            UIButton(
                                modifier = Modifier
                                    .weight(.33f)
                                    .padding(3.dp),
                                text = "Delete"
                            ) {
                                viewModel.deleteSelectedPrinter(posPrinterState.selectedPrinter)
                            }

                            UIButton(
                                modifier = Modifier
                                    .weight(.33f)
                                    .padding(3.dp),
                                text = "Close"
                            ) {
                                handleBack()
                            }
                        }

                    }
                }
            }
        }
        LoadingIndicator(
            show = posPrinterState.isLoading
        )
        if (posPrinterState.clear) {
            posPrinterState.selectedPrinter = PosPrinter()
            posPrinterState.selectedPrinter.posPrinterCompId = ""
            nameState = ""
            hostState = ""
            portState = ""
            typeState = ""
            posPrinterState.clear = false
        }
    }
}