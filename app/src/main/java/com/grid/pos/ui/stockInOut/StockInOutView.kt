package com.grid.pos.ui.stockInOut

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.grid.pos.R
import com.grid.pos.SharedViewModel
import com.grid.pos.data.item.Item
import com.grid.pos.data.stockHeaderAdjustment.StockHeaderAdjustment
import com.grid.pos.interfaces.OnBarcodeResult
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.StockAdjItemModel
import com.grid.pos.model.ToastModel
import com.grid.pos.model.WarehouseModel
import com.grid.pos.ui.common.SearchableDropdownMenuEx
import com.grid.pos.ui.common.UIImageButton
import com.grid.pos.ui.pos.components.EditInvoiceItemView
import com.grid.pos.ui.stockInOut.components.EditStockAdjustItemView
import com.grid.pos.ui.stockInOut.components.itemDataGrid
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

    var fromWarehouseState by remember { mutableStateOf("") }
    var toWarehouseState by remember { mutableStateOf("") }

    var itemIndexToEdit by remember { mutableIntStateOf(-1) }
    var isEditBottomSheetVisible by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var orientation by remember { mutableIntStateOf(Configuration.ORIENTATION_PORTRAIT) }
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = Utils.isTablet(LocalConfiguration.current)
    val isDeviceLargerThan7Inches = Utils.isDeviceLargerThan7Inches(context)
    var isLandscape by remember { mutableStateOf(orientation == Configuration.ORIENTATION_LANDSCAPE) }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(state.warning) {
        state.warning?.value?.let { message ->
            scope.launch {
                sharedViewModel.showToastMessage(ToastModel(
                    message = message,
                    onActionClick = {
                        when (state.actionLabel) {
                            "Settings" -> sharedViewModel.openAppStorageSettings()
                        }
                    },
                    onDismiss = {
                        viewModel.showWarning(null, null)
                    }
                ))
            }
        }
    }

    LaunchedEffect(configuration) {
        snapshotFlow { configuration.orientation }.collect {
            isLandscape = it == Configuration.ORIENTATION_LANDSCAPE
            orientation = it
            isEditBottomSheetVisible = false
        }
    }

    LaunchedEffect(state.isLoading) {
        sharedViewModel.showLoading(state.isLoading)
    }

    fun clear() {
        fromWarehouseState = ""
        toWarehouseState = ""

        viewModel.resetState()
    }

    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    fun handleBack() {
        if (state.isLoading) {
            return
        }
        if (isImeVisible) {
            keyboardController?.hide()
        } else if (isEditBottomSheetVisible) {
            isEditBottomSheetVisible = false
        } else {
            navController?.navigateUp()
        }
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
                                text = "Stock In/Out",
                                color = SettingsModel.textColor,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        },
                        actions = {
                            IconButton(onClick = { navController?.navigate("SettingsView") }) {
                                Icon(
                                    painterResource(R.drawable.ic_settings),
                                    contentDescription = "Settings",
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
                        .padding(top = 270.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    itemDataGrid(
                        stockAdjItems = viewModel.items,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                Utils.getListHeight(
                                    viewModel.items.size,
                                    50
                                )
                            )
                            .border(
                                BorderStroke(
                                    1.dp,
                                    Color.Black
                                )
                            ),
                        isLandscape = isTablet || isDeviceLargerThan7Inches || isLandscape,
                        onEdit = { index ->
                            itemIndexToEdit = index
                            isEditBottomSheetVisible = true
                        },
                        onRemove = { index ->
                            val deletedRow = viewModel.items.removeAt(index)
                            if (!deletedRow.stockAdjustment.isNew()) {
                                deletedRow.isDeleted = true
                                state.itemsToDelete.add(deletedRow)
                            }
                        })
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(25.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(modifier = Modifier.size(25.dp),
                            onClick = {
                                sharedViewModel.launchBarcodeScanner(false,
                                    ArrayList(state.items),
                                    object : OnBarcodeResult {
                                        override fun OnBarcodeResult(barcodesList: List<Any>) {
                                            if (barcodesList.isNotEmpty()) {
                                                sharedViewModel.showLoading(true)
                                                scope.launch {
                                                    val map: Map<Item, Int> =
                                                        barcodesList.groupingBy { item -> item as Item }
                                                            .eachCount()

                                                    map.forEach { (item, count) ->
                                                        if (!item.itemBarcode.isNullOrEmpty()) {
                                                            withContext(Dispatchers.IO) {
                                                                sharedViewModel.updateRealItemPrice(
                                                                    item
                                                                )
                                                            }
                                                            val stockAdjItemModel =
                                                                StockAdjItemModel()
                                                            stockAdjItemModel.setItem(item)
                                                            stockAdjItemModel.stockAdjustment.stockAdjQty =
                                                                count.toDouble()
                                                            viewModel.items.add(stockAdjItemModel)
                                                        }
                                                    }
                                                    withContext(Dispatchers.Main) {
                                                        sharedViewModel.showLoading(false)
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

                        Text(
                            text = Utils.getItemsNumberStr(viewModel.items.size),
                            modifier = Modifier.wrapContentWidth(),
                            textAlign = TextAlign.End,
                            style = TextStyle(
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp
                            ),
                            color = SettingsModel.textColor
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

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
                        viewModel.save()
                    }
                }

                Row(
                    modifier = Modifier.padding(
                        top = 185.dp,
                        start = 10.dp,
                        end = 10.dp
                    )
                ) {
                    SearchableDropdownMenuEx(items = state.items.toMutableList(),
                        modifier = Modifier.weight(1f),
                        showSelected = false,
                        label = "Select Item",
                        onLoadItems = { viewModel.fetchItems() }) { item ->
                        item as Item
                        val stockAdjItemModel =
                            StockAdjItemModel()
                        stockAdjItemModel.setItem(item)
                        viewModel.items.add(stockAdjItemModel)
                    }
                }

                Row(
                    modifier = Modifier.padding(
                        top = 100.dp,
                        start = 10.dp,
                        end = 10.dp
                    )
                ) {
                    SearchableDropdownMenuEx(
                        items = state.warehouses.toMutableList(),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 3.dp),
                        onLoadItems = {
                            scope.launch(Dispatchers.IO) {
                                viewModel.fetchWarehouses()
                            }
                        },
                        label = "From Warehouse",
                        selectedId = fromWarehouseState
                    ) { warehouse ->
                        warehouse as WarehouseModel
                        fromWarehouseState = warehouse.getId()
                    }

                    SearchableDropdownMenuEx(
                        items = state.warehouses.toMutableList(),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 3.dp),
                        onLoadItems = {
                            scope.launch(Dispatchers.IO) {
                                viewModel.fetchWarehouses()
                            }
                        },
                        label = "To Warehouse",
                        selectedId = toWarehouseState
                    ) { warehouse ->
                        warehouse as WarehouseModel
                        toWarehouseState = warehouse.getId()
                    }
                }

                SearchableDropdownMenuEx(items = state.stockHeaderAdjustments.toMutableList(),
                    modifier = Modifier.padding(
                        top = 15.dp,
                        start = 10.dp,
                        end = 10.dp
                    ),
                    showSelected = false,
                    label = "Select Transfer",
                    onLoadItems = { viewModel.fetchTransfers() }) { stockHeaderAdjustment ->
                    stockHeaderAdjustment as StockHeaderAdjustment

                    fromWarehouseState = stockHeaderAdjustment.stockHAWaName ?: ""
                    toWarehouseState = stockHeaderAdjustment.stockHAWaName ?: ""
                    viewModel.loadTransferDetails(stockHeaderAdjustment)
                }
            }

            AnimatedVisibility(
                visible = isEditBottomSheetVisible,
                enter = fadeIn(
                    initialAlpha = 0.4f
                ),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = 250)
                )
            ) {
                EditStockAdjustItemView(
                    stockAdjItemModel = viewModel.items.get(itemIndexToEdit),
                    stockHeaderAdjustment = StockHeaderAdjustment(),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                        .background(
                            color = SettingsModel.backgroundColor
                        ),
                    onSave = { stockItemModel ->
                        viewModel.items[itemIndexToEdit] = stockItemModel
                        isEditBottomSheetVisible = false
                    })
            }
        }
    }
}