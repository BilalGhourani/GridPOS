package com.grid.pos.ui.stockInOut

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.grid.pos.R
import com.grid.pos.SharedViewModel
import com.grid.pos.data.item.Item
import com.grid.pos.interfaces.OnBarcodeResult
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.WarehouseModel
import com.grid.pos.ui.common.SearchableDropdownMenuEx
import com.grid.pos.ui.common.UIImageButton
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun StockInOutView(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    sharedViewModel: SharedViewModel,
    viewModel: StockInOutViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val keyboardController = LocalSoftwareKeyboardController.current

    val openQtyFocusRequester = remember { FocusRequester() }
    val costFocusRequester = remember { FocusRequester() }
    val costFirstFocusRequester = remember { FocusRequester() }
    val costSecondFocusRequester = remember { FocusRequester() }

    var barcodeSearchState by remember { mutableStateOf("") }

    var warehouseState by remember { mutableStateOf("") }
    var locationState by remember { mutableStateOf("") }
    var openQtyState by remember { mutableStateOf("") }

    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.warning) {
        state.warning?.value?.let { message ->
            scope.launch {
                val snackBarResult = snackBarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short,
                    actionLabel = state.actionLabel
                )
                when (snackBarResult) {
                    SnackbarResult.Dismissed -> {}
                    SnackbarResult.ActionPerformed -> when (state.actionLabel) {
                        "Settings" -> sharedViewModel.openAppStorageSettings()
                    }
                }
            }
        }
    }

    LaunchedEffect(state.isLoading) {
        sharedViewModel.showLoading(state.isLoading)
    }

    fun fillItemInputsNow(item: Item) {
        state.selectedItem = item
        warehouseState = item.itemWarehouse ?: ""
        locationState = item.itemLocation ?: ""
        openQtyState = (item.itemOpenQty.takeIf { it > 0.0 } ?: "").toString()

    }

    fun fillItemInputs(item: Item) {
        if (state.warehouses.isEmpty()) {
            scope.launch(Dispatchers.IO) {
                viewModel.fetchWarehouses()
                withContext(Dispatchers.Main) {
                    fillItemInputsNow(item)
                }
            }
        } else {
            fillItemInputsNow(item)
        }
    }

    fun clear() {
        state.selectedItem = null
        warehouseState = ""
        locationState = ""
        openQtyState = ""

        viewModel.resetState()
    }

    fun handleBack() {
        if (state.isLoading) {
            return
        }
        navController?.navigateUp()
    }

    LaunchedEffect(
        state.clear
    ) {
        if (state.clear) {
            clear()
        }
    }
    BackHandler {
        handleBack()
    }
    GridPOSTheme {
        Scaffold(containerColor = SettingsModel.backgroundColor,
            snackbarHost = {
                SnackbarHost(hostState = snackBarHostState)
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
                                text = "Item Opening",
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
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(top = 175.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    //Location
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = locationState,
                        label = "Location",
                        placeHolder = "Enter Location",
                        onAction = { openQtyFocusRequester.requestFocus() }) { location ->
                        locationState = location
                    }

                    //open quantity
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = openQtyState,
                        label = "Open Quantity",
                        focusRequester = openQtyFocusRequester,
                        keyboardType = KeyboardType.Decimal,
                        placeHolder = "Enter open quantity",
                        onAction = {
                            costFocusRequester.requestFocus()
                        }) { quantity ->
                        openQtyState = Utils.getDoubleValue(
                            quantity,
                            openQtyState
                        )
                    }

                    UIImageButton(
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(100.dp)
                            .padding(10.dp),
                        icon = R.drawable.save,
                        text = "save",
                        iconSize = 60.dp,
                        isVertical = false
                    ) {
                        viewModel.save(
                            state.selectedItem,
                            warehouseState,
                            locationState,
                            openQtyState
                        )
                    }
                }

                SearchableDropdownMenuEx(
                    items = state.warehouses.toMutableList(),
                    modifier = Modifier.padding(
                        top = 100.dp,
                        start = 10.dp,
                        end = 10.dp
                    ),
                    onLoadItems = { viewModel.fetchWarehouses() },
                    label = "Select Warehouse",
                    selectedId = warehouseState
                ) { warehouse ->
                    warehouse as WarehouseModel
                    warehouseState = warehouse.getId()
                }

                SearchableDropdownMenuEx(items = state.items.toMutableList(),
                    modifier = Modifier.padding(
                        top = 15.dp,
                        start = 10.dp,
                        end = 10.dp
                    ),
                    label = "Select Item",
                    selectedId = state.selectedItem?.itemId,
                    onLoadItems = { viewModel.fetchItems() },
                    leadingIcon = { modifier ->
                        if (!state.selectedItem?.itemId.isNullOrEmpty()) {
                            Icon(
                                Icons.Default.RemoveCircleOutline,
                                contentDescription = "reset Item",
                                tint = Color.Black,
                                modifier = modifier
                            )
                        }
                    },
                    onLeadingIconClick = {
                        clear()
                    },
                    searchEnteredText = barcodeSearchState,
                    searchLeadingIcon = {
                        IconButton(onClick = {
                            sharedViewModel.launchBarcodeScanner(true,
                                null,
                                object : OnBarcodeResult {
                                    override fun OnBarcodeResult(barcodesList: List<Any>) {
                                        if (barcodesList.isNotEmpty()) {
                                            val resp = barcodesList[0]
                                            if (resp is String) {
                                                scope.launch(Dispatchers.Default) {
                                                    val item = state.items.firstOrNull {
                                                        it.itemBarcode.equals(
                                                            resp,
                                                            ignoreCase = true
                                                        )
                                                    }
                                                    withContext(Dispatchers.Main) {
                                                        if (item != null) {
                                                            fillItemInputs(item)
                                                        } else {
                                                            barcodeSearchState = resp
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                onPermissionDenied = {
                                    viewModel.showWarning(
                                        "Permission Denied",
                                        "Settings"
                                    )
                                })
                        }) {
                            Icon(
                                Icons.Default.QrCode2,
                                contentDescription = "Barcode",
                                tint = SettingsModel.buttonColor
                            )
                        }
                    }) { item ->
                    item as Item
                    fillItemInputs(item)
                }
            }
        }
    }
}