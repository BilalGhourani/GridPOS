package com.grid.pos.ui.item.opening

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
import androidx.compose.runtime.mutableIntStateOf
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
import com.grid.pos.R
import com.grid.pos.SharedViewModel
import com.grid.pos.data.item.Item
import com.grid.pos.interfaces.OnBarcodeResult
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.WarehouseModel
import com.grid.pos.ui.common.SearchableDropdownMenuEx
import com.grid.pos.ui.common.UIImageButton
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.pos.POSUtils
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun ItemOpeningView(
        modifier: Modifier = Modifier,
        navController: NavController? = null,
        sharedViewModel: SharedViewModel,
        viewModel: ItemOpeningViewModel = hiltViewModel()
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

    var currencyIndexState by remember { mutableIntStateOf(0) }
    var costState by remember { mutableStateOf("") }
    var costFirstState by remember { mutableStateOf("") }
    var costSecondState by remember { mutableStateOf("") }

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

    fun fillItemInputs(item: Item) {
        state.selectedItem = item
        warehouseState = item.itemWarehouse ?: ""
        locationState = item.itemLocation ?: ""
        openQtyState = (item.itemOpenQty.takeIf { it > 0.0 } ?: "").toString()

        currencyIndexState = viewModel.getSelectedCurrencyIndex(item.itemCurrencyId)
        costState = (item.itemOpenCost.takeIf { it > 0.0 } ?: "").toString()
        costFirstState = item.itemCostFirst?.toString() ?: ""
        costSecondState = item.itemCostSecond?.toString() ?: ""

    }

    fun clear() {
        state.selectedItem = null
        warehouseState = ""
        locationState = ""
        openQtyState = ""

        currencyIndexState = 0
        costState = ""
        costFirstState = ""
        costSecondState = ""
        viewModel.resetState()
    }

    fun handleBack() {
        if (state.isLoading) {
            return
        }
        navController?.navigateUp()
    }

    /*LaunchedEffect(
        state.clearCosts,
        state.clearWarehouseDetails
    ) {
        if (state.clearCosts) {
            if (warehouseState.isEmpty()) {
                clear()
            } else {
                costState = ""
                costFirstState = ""
                costSecondState = ""
            }
        }

        if (state.clearWarehouseDetails) {
            if (costState.isEmpty()) {
                clear()
            } else {
                warehouseState = ""
                locationState = ""
                openQtyState = ""
            }
        }
    }*/
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
                        text = "save warehouse details",
                        iconSize = 60.dp,
                        isVertical = false
                    ) {
                        viewModel.saveItemWarehouse(
                            state.selectedItem,
                            warehouseState,
                            locationState,
                            openQtyState
                        )
                    }

                    //cost
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = costState,
                        keyboardType = KeyboardType.Decimal,
                        label = "cost ${state.selectedItem?.itemCurrencyCode ?: ""}",
                        placeHolder = "Enter cost ${state.selectedItem?.itemCurrencyCode ?: ""}",
                        focusRequester = costFocusRequester,
                        onAction = { costFirstFocusRequester.requestFocus() }) { cost ->
                        costState = Utils.getDoubleValue(
                            cost,
                            costState
                        )

                        val costDouble = costState.toDoubleOrNull() ?: 0.0
                        if (currencyIndexState == 1) {
                            val second = costDouble.times(SettingsModel.currentCurrency?.currencyRate ?: 1.0)
                            costSecondState = POSUtils.formatDouble(
                                second,
                                SettingsModel.currentCurrency?.currencyName2Dec ?: 2
                            )
                            costFirstState = costState
                        } else if (currencyIndexState == 2) {
                            val first = costDouble.div(SettingsModel.currentCurrency?.currencyRate ?: 1.0)
                            costFirstState = POSUtils.formatDouble(
                                first,
                                SettingsModel.currentCurrency?.currencyName1Dec ?: 2
                            )
                            costSecondState = costState
                        }
                    }

                    //cost first
                    if (currencyIndexState != 1) {
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue = costFirstState,
                            label = "cost ${SettingsModel.currentCurrency?.currencyCode1 ?: ""}",
                            keyboardType = KeyboardType.Decimal,
                            placeHolder = "Enter cost ${SettingsModel.currentCurrency?.currencyCode1 ?: ""}",
                            focusRequester = costFirstFocusRequester,
                            onAction = { costSecondFocusRequester.requestFocus() }) { cost ->
                            costFirstState = Utils.getDoubleValue(
                                cost,
                                costFirstState
                            )
                        }
                    }

                    //cost second
                    if (currencyIndexState != 2) {
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue = costSecondState,
                            label = "cost ${SettingsModel.currentCurrency?.currencyCode2 ?: ""}",
                            focusRequester = costSecondFocusRequester,
                            keyboardType = KeyboardType.Decimal,
                            placeHolder = "Enter cost ${SettingsModel.currentCurrency?.currencyCode2 ?: ""}",
                            imeAction = ImeAction.Done,
                            onAction = {
                                keyboardController?.hide()
                            }) { openQty ->
                            costSecondState = Utils.getDoubleValue(
                                openQty,
                                costSecondState
                            )
                        }
                    }


                    UIImageButton(
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(100.dp)
                            .padding(10.dp),
                        icon = R.drawable.save,
                        text = "save item cost",
                        iconSize = 60.dp,
                        isVertical = false
                    ) {
                        viewModel.saveItemCosts(
                            state.selectedItem,
                            costState,
                            costFirstState,
                            costSecondState
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
                                                barcodeSearchState = resp
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
                    if (state.warehouses.isEmpty()) {
                        scope.launch(Dispatchers.IO) {
                            viewModel.fetchWarehouses()
                            withContext(Dispatchers.Main) {
                                fillItemInputs(item)
                            }
                        }
                    } else {
                        fillItemInputs(item)
                    }
                }
            }
        }
    }
}