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
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.grid.pos.data.item.Item
import com.grid.pos.data.stockHeadInOut.header.StockHeaderInOut
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.StockInOutItemModel
import com.grid.pos.model.WarehouseModel
import com.grid.pos.ui.common.SearchableDropdownMenuEx
import com.grid.pos.ui.common.UIImageButton
import com.grid.pos.ui.stockInOut.components.EditStockInOutItemView
import com.grid.pos.ui.stockInOut.components.itemDataGrid
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun StockInOutView(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    viewModel: StockInOutViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val stockHeaderInOut = viewModel.stockHeaderInOutState.collectAsState().value

    var triggerSaveCallback by remember { mutableStateOf(false) }
    var isEditBottomSheetVisible by remember { mutableStateOf(false) }

    var showDeleteButton by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var orientation by remember { mutableIntStateOf(Configuration.ORIENTATION_PORTRAIT) }
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = Utils.isTablet(LocalConfiguration.current)
    val isDeviceLargerThan7Inches = Utils.isDeviceLargerThan7Inches(context)
    var isLandscape by remember { mutableStateOf(orientation == Configuration.ORIENTATION_LANDSCAPE) }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(configuration) {
        snapshotFlow { configuration.orientation }.collect {
            isLandscape = it == Configuration.ORIENTATION_LANDSCAPE
            orientation = it
            isEditBottomSheetVisible = false
        }
    }

    fun clear() {
        showDeleteButton = false
        viewModel.resetState()
    }

    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    fun handleBack() {
        if (viewModel.isLoading()) {
            return
        }
        if (isImeVisible) {
            keyboardController?.hide()
        } else if (isEditBottomSheetVisible) {
            isEditBottomSheetVisible = false
        } else if (viewModel.items.isNotEmpty()) {
            viewModel.showPopup(
                PopupModel().apply {
                    onConfirmation = {
                        viewModel.resetState()
                        handleBack()
                    }
                    dialogText = "Are you sure you want to discard current transfer?"
                    positiveBtnText = "Discard"
                    negativeBtnText = "Cancel"
                })
        } else {
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
                            IconButton(onClick = {
                                if (isEditBottomSheetVisible) {
                                    triggerSaveCallback = true
                                } else {
                                    handleBack()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = SettingsModel.buttonColor
                                )
                            }
                        },
                        title = {
                            Text(
                                text = if (isEditBottomSheetVisible) "Edit Item" else "Stock In/Out",
                                color = SettingsModel.textColor,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        },
                        actions = {
                            if (!isEditBottomSheetVisible) {
                                IconButton(onClick = { navController?.navigate("SettingsView") }) {
                                    Icon(
                                        painterResource(R.drawable.ic_settings),
                                        contentDescription = "Settings",
                                        tint = SettingsModel.buttonColor
                                    )
                                }
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
                        stockInOutItems = viewModel.items,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                Utils.getListHeight(
                                    viewModel.items.size,
                                    50
                                )
                            )
                            .padding(horizontal = 10.dp)
                            .border(
                                BorderStroke(
                                    1.dp,
                                    Color.Black
                                )
                            ),
                        isLandscape = isTablet || isDeviceLargerThan7Inches || isLandscape,
                        onEdit = { index ->
                            viewModel.selectedItemIndex = index
                            isEditBottomSheetVisible = true
                        },
                        onRemove = { index ->
                            val deletedRow = viewModel.items.removeAt(index)
                            if (!deletedRow.stockInOut.isNew()) {
                                deletedRow.isDeleted = true
                                viewModel.deletedItems.add(deletedRow)
                            }
                        })
                    Text(
                        text = Utils.getItemsNumberStr(viewModel.items.size),
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = 10.dp),
                        textAlign = TextAlign.End,
                        style = TextStyle(
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        ),
                        color = SettingsModel.textColor
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
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

                        if (showDeleteButton) {
                            UIImageButton(
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .height(100.dp)
                                    .padding(10.dp),
                                icon = R.drawable.delete,
                                text = "delete",
                                iconSize = 60.dp,
                                isVertical = false
                            ) {
                                viewModel.showPopup(
                                    PopupModel().apply {
                                        onConfirmation = {
                                            viewModel.delete()
                                        }
                                        dialogText =
                                            "Are you sure you want to Delete this transfer?"
                                        positiveBtnText = "Delete"
                                        negativeBtnText = "Cancel"
                                    })
                            }
                        }
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
                        onLoadItems = {
                            scope.launch(Dispatchers.IO) {
                                viewModel.fetchItems()
                            }
                        },
                        leadingIcon = { modifier ->
                            Icon(
                                Icons.Default.QrCode2,
                                contentDescription = "Barcode",
                                tint = SettingsModel.buttonColor,
                                modifier = modifier
                            )
                        }, onLeadingIconClick = {
                            viewModel.launchBarcodeScanner()
                        }) { item ->
                        item as Item
                        val stockInOutItem =
                            StockInOutItemModel()
                        stockInOutItem.setItem(item)
                        viewModel.items.add(stockInOutItem)
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
                        selectedId = stockHeaderInOut.stockHeadInOutWaName
                    ) { warehouse ->
                        warehouse as WarehouseModel
                        viewModel.updateStockHeaderInOut(
                            stockHeaderInOut.copy(
                                stockHeadInOutWaName = warehouse.getId()
                            )
                        )
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
                        selectedId = stockHeaderInOut.stockHeadInOutWaTpName,
                    ) { warehouse ->
                        warehouse as WarehouseModel
                        viewModel.updateStockHeaderInOut(
                            stockHeaderInOut.copy(
                                stockHeadInOutWaTpName = warehouse.getId()
                            )
                        )
                    }
                }

                SearchableDropdownMenuEx(items = state.stockHeaderInOutList.toMutableList(),
                    modifier = Modifier.padding(
                        top = 15.dp,
                        start = 10.dp,
                        end = 10.dp
                    ),
                    label = "Select Transfer",
                    onLoadItems = { viewModel.fetchTransfers() },
                    selectedId = stockHeaderInOut.stockHeadInOutId,
                    leadingIcon = { modifier ->
                        if (stockHeaderInOut.stockHeadInOutId.isNotEmpty()) {
                            Icon(
                                Icons.Default.RemoveCircleOutline,
                                contentDescription = "reset transfer",
                                tint = Color.Black,
                                modifier = modifier
                            )
                        }
                    },
                    onLeadingIconClick = {
                        clear()
                    }
                ) { stockHeaderInOut ->
                    stockHeaderInOut as StockHeaderInOut
                    if (viewModel.items.isNotEmpty()) {
                        viewModel.showPopup(
                            PopupModel().apply {
                                onConfirmation = {
                                    viewModel.resetState()
                                    showDeleteButton = true
                                    viewModel.loadTransferDetails(stockHeaderInOut)
                                }
                                dialogText = "Are you sure you want to discard current transfer?"
                                positiveBtnText = "Discard"
                                negativeBtnText = "Cancel"
                            })
                    } else {
                        showDeleteButton = true
                        viewModel.loadTransferDetails(stockHeaderInOut)
                    }
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
                EditStockInOutItemView(
                    stockInOutItemModel = viewModel.items[viewModel.selectedItemIndex],
                    stockHeaderInOut = stockHeaderInOut,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                        .background(
                            color = SettingsModel.backgroundColor
                        ),
                    triggerOnSave = triggerSaveCallback,
                    state = state,
                    viewModel = viewModel,
                    onSave = { stockHeaderInOut, stockItemModel ->
                        viewModel.items[viewModel.selectedItemIndex] = stockItemModel
                        viewModel.updateStockHeaderInOut(stockHeaderInOut)
                        isEditBottomSheetVisible = false
                        triggerSaveCallback = false
                    })
            }
        }
    }
}