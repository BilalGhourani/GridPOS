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
import com.grid.pos.data.item.Item
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.WarehouseModel
import com.grid.pos.ui.common.SearchableDropdownMenuEx
import com.grid.pos.ui.common.UIImageButton
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.pos.POSUtils
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun ItemOpeningView(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    viewModel: ItemOpeningViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val keyboardController = LocalSoftwareKeyboardController.current

    val openQtyFocusRequester = remember { FocusRequester() }
    val costFocusRequester = remember { FocusRequester() }
    val costFirstFocusRequester = remember { FocusRequester() }
    val costSecondFocusRequester = remember { FocusRequester() }

    var collapseItemListState by remember { mutableStateOf(false) }
    var isBackPressed by remember { mutableStateOf(false) }

    fun handleBack() {
        if (viewModel.isLoading()) {
            return
        }
        if (isBackPressed) {
            return
        }
        isBackPressed = true
        navController?.navigateUp()
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
                        defaultValue = state.locationState,
                        label = "Location",
                        placeHolder = "Enter Location",
                        onAction = { openQtyFocusRequester.requestFocus() }) { location ->
                        viewModel.updateState(
                            state.copy(
                                locationState = location
                            )
                        )
                    }

                    //open quantity
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = state.openQtyState,
                        label = "Open Quantity",
                        focusRequester = openQtyFocusRequester,
                        keyboardType = KeyboardType.Decimal,
                        placeHolder = "Enter open quantity",
                        onAction = {
                            costFocusRequester.requestFocus()
                        }) { quantity ->
                        viewModel.updateState(
                            state.copy(
                                openQtyState = Utils.getDoubleValue(
                                    quantity,
                                    state.openQtyState
                                )
                            )
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
                        viewModel.saveItemWarehouse()
                    }

                    //cost
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = state.costState,
                        keyboardType = KeyboardType.Decimal,
                        label = "cost ${state.selectedItem?.itemCurrencyCode ?: ""}",
                        placeHolder = "Enter cost ${state.selectedItem?.itemCurrencyCode ?: ""}",
                        focusRequester = costFocusRequester,
                        onAction = { costFirstFocusRequester.requestFocus() }) { cost ->
                        val costState = Utils.getDoubleValue(
                            cost,
                            state.costState
                        )
                        var costFirstState = state.costFirstState
                        var costSecondState = state.costSecondState
                        val costDouble = costState.toDoubleOrNull() ?: 0.0
                        if (state.currencyIndexState == 1) {
                            val second =
                                costDouble.times(SettingsModel.currentCurrency?.currencyRate ?: 1.0)
                            costSecondState = POSUtils.formatDouble(
                                second,
                                SettingsModel.currentCurrency?.currencyName2Dec ?: 2
                            )
                            costFirstState = costState
                        } else if (state.currencyIndexState == 2) {
                            val first =
                                costDouble.div(SettingsModel.currentCurrency?.currencyRate ?: 1.0)
                            costFirstState = POSUtils.formatDouble(
                                first,
                                SettingsModel.currentCurrency?.currencyName1Dec ?: 2
                            )
                            costSecondState = costState
                        }
                        viewModel.updateState(
                            state.copy(
                                costState = costState,
                                costFirstState = costFirstState,
                                costSecondState = costSecondState
                            )
                        )
                    }

                    //cost first
                    if (state.currencyIndexState != 1) {
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue = state.costFirstState,
                            label = "cost ${SettingsModel.currentCurrency?.currencyCode1 ?: ""}",
                            keyboardType = KeyboardType.Decimal,
                            placeHolder = "Enter cost ${SettingsModel.currentCurrency?.currencyCode1 ?: ""}",
                            focusRequester = costFirstFocusRequester,
                            onAction = { costSecondFocusRequester.requestFocus() }) { cost ->
                            viewModel.updateState(
                                state.copy(
                                    costFirstState = Utils.getDoubleValue(
                                        cost,
                                        state.costFirstState
                                    )
                                )
                            )
                        }
                    }

                    //cost second
                    if (state.currencyIndexState != 2) {
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue = state.costSecondState,
                            label = "cost ${SettingsModel.currentCurrency?.currencyCode2 ?: ""}",
                            focusRequester = costSecondFocusRequester,
                            keyboardType = KeyboardType.Decimal,
                            placeHolder = "Enter cost ${SettingsModel.currentCurrency?.currencyCode2 ?: ""}",
                            imeAction = ImeAction.Done,
                            onAction = {
                                keyboardController?.hide()
                            }) { openQty ->
                            viewModel.updateState(
                                state.copy(
                                    costSecondState = Utils.getDoubleValue(
                                        openQty,
                                        state.costSecondState
                                    )
                                )
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
                        viewModel.saveItemCosts()
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
                    selectedId = state.warehouseState
                ) { warehouse ->
                    warehouse as WarehouseModel
                    viewModel.updateState(
                        state.copy(
                            warehouseState = warehouse.getId()
                        )
                    )
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
                        viewModel.resetState()
                    },
                    collapseOnInit = collapseItemListState,
                    searchEnteredText = state.barcodeSearchState,
                    searchLeadingIcon = {
                        IconButton(onClick = {
                            collapseItemListState = false
                            viewModel.launchBarcodeScanner {
                                collapseItemListState = true
                            }
                        }) {
                            Icon(
                                Icons.Default.QrCode2,
                                contentDescription = "Barcode",
                                tint = SettingsModel.buttonColor
                            )
                        }
                    }) { item ->
                    item as Item
                    viewModel.selectItem(item)
                }
            }
        }
    }
}