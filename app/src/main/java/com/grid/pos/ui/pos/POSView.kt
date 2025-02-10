package com.grid.pos.ui.pos

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
import com.grid.pos.data.invoiceHeader.InvoiceHeader
import com.grid.pos.data.item.Item
import com.grid.pos.data.posReceipt.PosReceipt
import com.grid.pos.interfaces.OnBarcodeResult
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.PopupModel
import com.grid.pos.model.PopupState
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.ToastModel
import com.grid.pos.model.UserType
import com.grid.pos.ui.pos.components.AddInvoiceItemView
import com.grid.pos.ui.pos.components.EditInvoiceItemView
import com.grid.pos.ui.pos.components.InvoiceBodyDetails
import com.grid.pos.ui.pos.components.InvoiceCashView
import com.grid.pos.ui.pos.components.InvoiceFooterView
import com.grid.pos.ui.pos.components.InvoiceHeaderDetails
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POSView(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    sharedViewModel: SharedViewModel,
    viewModel: POSViewModel = hiltViewModel()
) {
    val state by viewModel.posState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(
        sharedViewModel.shouldLoadInvoice,
        sharedViewModel.fetchItemsAgain,
        sharedViewModel.fetchThirdPartiesAgain
    ) {
        if (sharedViewModel.shouldLoadInvoice && sharedViewModel.tempInvoiceHeader != null) {
            sharedViewModel.shouldLoadInvoice = false
            viewModel.loadInvoiceDetails(sharedViewModel.tempInvoiceHeader!!.copy())
            sharedViewModel.tempInvoiceHeader = null
        }
        if (sharedViewModel.fetchItemsAgain) {
            sharedViewModel.fetchItemsAgain = false
            withContext(Dispatchers.IO) {
                viewModel.fetchItems()
            }
        }
        if (sharedViewModel.fetchThirdPartiesAgain) {
            sharedViewModel.fetchThirdPartiesAgain = false
            withContext(Dispatchers.IO) {
                viewModel.fetchThirdParties(false)
            }
        }
    }

    LaunchedEffect(configuration) {
        snapshotFlow { configuration.orientation }.collect {
            viewModel.isTablet = Utils.isTablet(configuration)
            viewModel.updateState(
                state.copy(
                    orientation = it,
                )
            )

        }
    }

    LaunchedEffect(state.isLoading) {
        sharedViewModel.showLoading(state.isLoading)
    }

    LaunchedEffect(state.warning) {
        state.warning?.value?.let { message ->
            sharedViewModel.showToastMessage(
                ToastModel(
                    message = message,
                    actionButton = state.actionLabel,
                    onActionClick = {
                        when (state.actionLabel) {
                            "Settings" -> sharedViewModel.openAppStorageSettings()
                        }
                    }
                )
            )
        }
    }

    LaunchedEffect(
        state.isSaved
    ) {
        if (state.isSaved) {
            state.isSaved = false
            viewModel.isPayBottomSheetVisible.value = false
            if (viewModel.proceedToPrint) {
                state.invoiceItems.clear()
                state.invoiceHeader = InvoiceHeader()
                state.posReceipt = PosReceipt()
                sharedViewModel.reportsToPrint.clear()
                sharedViewModel.reportsToPrint.addAll(viewModel.reportResults)
                navController?.navigate("UIWebView")
            } else if (SettingsModel.autoPrintTickets) {
                sharedViewModel.showLoading(true)
                viewModel.prepareAutoPrint(context) {
                    sharedViewModel.showLoading(false)
                    viewModel.resetState()
                    if (sharedViewModel.isFromTable) {
                        sharedViewModel.isFromTable = false
                        navController?.navigateUp()
                    }
                }
            } else {
                viewModel.resetState()
                if (sharedViewModel.isFromTable) {
                    sharedViewModel.isFromTable = false
                    navController?.navigateUp()
                }
            }
            sharedViewModel.showLoading(false)
        }
    }

    LaunchedEffect(
        state.isDeleted
    ) {
        if (state.isDeleted) {
            viewModel.resetState()
            if (sharedViewModel.isFromTable) {
                sharedViewModel.isFromTable = false
                navController?.navigateUp()
            }
        }
    }
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    fun handleBack() {
        if (state.isLoading) {
            return
        }
        if (isImeVisible) {
            keyboardController?.hide()
        } else if (viewModel.isAddItemBottomSheetVisible.value) {
            viewModel.isAddItemBottomSheetVisible.value = false
        } else if (viewModel.isEditBottomSheetVisible.value) {
            viewModel.isEditBottomSheetVisible.value = false
        } else if (viewModel.isPayBottomSheetVisible.value) {
            viewModel.isPayBottomSheetVisible.value = false
        } else if (state.invoiceItems.isNotEmpty()) {
            viewModel.isSavePopupVisible.value = true
            viewModel.popupState = PopupState.DISCARD_CHANGES
        } else {
            if (!state.invoiceHeader.invoiceHeadTableId.isNullOrEmpty()) {
                viewModel.unLockTable()
            }
            if (SettingsModel.getUserType() == UserType.POS) {
                viewModel.isSavePopupVisible.value = true
                viewModel.popupState = PopupState.BACK_PRESSED
            } else {
                sharedViewModel.isFromTable = false
                navController?.navigateUp()
            }
        }
    }
    LaunchedEffect(viewModel.isSavePopupVisible.value) {
        sharedViewModel.showPopup(viewModel.isSavePopupVisible.value,
            if (!viewModel.isSavePopupVisible.value) null else PopupModel().apply {
                onDismissRequest = {
                    viewModel.popupState = null
                    viewModel.isSavePopupVisible.value = false
                }
                onConfirmation = {
                    viewModel.isSavePopupVisible.value = false
                    when (viewModel.popupState) {
                        PopupState.BACK_PRESSED -> {
                            viewModel.resetState()
                            sharedViewModel.logout()
                            navController?.clearBackStack("LoginView")
                            navController?.navigate("LoginView")
                        }

                        PopupState.DISCARD_CHANGES -> {
                            if (!state.invoiceHeader.invoiceHeadTableId.isNullOrEmpty()) {
                                viewModel.unLockTable()
                            }
                            state.invoiceItems.clear()
                            state.invoiceHeader = InvoiceHeader()
                            if (SettingsModel.getUserType() != UserType.POS) {
                                handleBack()
                            }
                        }

                        PopupState.CHANGE_ITEM -> {
                            //viewModel.resetState()
                            sharedViewModel.tempInvoiceHeader?.let {
                                viewModel.loadInvoiceDetails(it)
                            }
                        }

                        PopupState.DELETE_ITEM -> {
                            viewModel.deleteInvoiceHeader()
                        }

                        else -> {
                            viewModel.resetState()
                        }
                    }
                }
                dialogText = when (viewModel.popupState) {
                    PopupState.BACK_PRESSED -> "Are you sure you want to logout?"
                    PopupState.DELETE_ITEM -> "Are you sure you want to Delete this invoice?"
                    else -> "Are you sure you want to discard current invoice?"
                }
                positiveBtnText = when (viewModel.popupState) {
                    PopupState.BACK_PRESSED -> "Logout"
                    PopupState.DELETE_ITEM -> "Delete"
                    else -> "Discard"
                }
                negativeBtnText = "Cancel"
            })
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
                                if (viewModel.isAddItemBottomSheetVisible.value || viewModel.isEditBottomSheetVisible.value) {
                                    viewModel.triggerSaveCallback.value = true
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
                                text = if (viewModel.isEditBottomSheetVisible.value) "Edit Item" else if (viewModel.isAddItemBottomSheetVisible.value) "Add Items" else if (viewModel.isPayBottomSheetVisible.value) "Pay" else "POS",
                                color = SettingsModel.textColor,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        },
                        actions = {
                            if (!viewModel.isAnyPopupShown()) {
                                IconButton(onClick = {
                                    navController?.navigate("SettingsView")
                                }) {
                                    Icon(
                                        painterResource(R.drawable.ic_settings),
                                        contentDescription = "Back",
                                        tint = SettingsModel.buttonColor
                                    )
                                }
                            }
                        })
                }
            }) {
            Surface(
                modifier = modifier
                    .wrapContentWidth()
                    .fillMaxHeight()
                    .padding(it),
                color = SettingsModel.backgroundColor
            ) {
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = (if (state.isLandscape && SettingsModel.showItemsInPOS) Modifier.fillMaxWidth(
                            .6f
                        )
                        else Modifier.fillMaxWidth())
                            .verticalScroll(
                                rememberScrollState()
                            )
                            .padding(
                                horizontal = 10.dp,
                                vertical = 10.dp
                            ),
                    ) {
                        InvoiceHeaderDetails(modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp),
                            isPayEnabled = state.invoiceItems.isNotEmpty(),
                            isDeleteEnabled = !state.invoiceHeader.isNew(),
                            onAddItem = {
                                if (state.items.isEmpty()) {
                                    viewModel.loadFamiliesAndItems()
                                }
                                viewModel.isAddItemBottomSheetVisible.value = true
                            },
                            onPay = {
                                viewModel.isPayBottomSheetVisible.value = true
                            },
                            onDelete = {
                                viewModel.popupState = PopupState.DELETE_ITEM
                                viewModel.isSavePopupVisible.value = true
                            })

                        // Border stroke configuration
                        val borderStroke = BorderStroke(
                            1.dp,
                            Color.Black
                        )

                        InvoiceBodyDetails(invoices = state.invoiceItems,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(
                                    Utils.getListHeight(
                                        state.invoiceItems.size,
                                        50
                                    )
                                )
                                .border(borderStroke),
                            isLandscape = viewModel.isTablet || viewModel.isDeviceLargerThan7Inches || state.isLandscape,
                            onEdit = { index ->
                                viewModel.selectedItemIndex = index
                                viewModel.isEditBottomSheetVisible.value = true
                            },
                            onRemove = { index ->
                                val invItems = state.invoiceItems.toMutableList()
                                val deletedRow = invItems.removeAt(index)
                                viewModel.updateState(
                                    state.copy(
                                        invoiceItems = invItems,
                                        invoiceHeader = POSUtils.refreshValues(
                                            invItems,
                                            state.invoiceHeader
                                        )
                                    )
                                )
                                if (!deletedRow.invoice.isNew()) {
                                    deletedRow.isDeleted = true
                                    viewModel.itemsToDelete.add(deletedRow)
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
                                    scope.launch(Dispatchers.Main) {
                                        if (state.items.isEmpty()) {
                                            sharedViewModel.showLoading(true)
                                            viewModel.fetchItems()
                                        }
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
                                                            val invoiceItems =
                                                                state.invoiceItems.toMutableList()
                                                            map.forEach { (item, count) ->
                                                                if (!item.itemBarcode.isNullOrEmpty()) {
                                                                    withContext(Dispatchers.IO) {
                                                                        sharedViewModel.updateRealItemPrice(
                                                                            item
                                                                        )
                                                                    }
                                                                    val invoiceItemModel =
                                                                        InvoiceItemModel()
                                                                    invoiceItemModel.setItem(item)
                                                                    invoiceItemModel.shouldPrint =
                                                                        true
                                                                    invoiceItemModel.invoice.invoiceQuantity =
                                                                        count.toDouble()
                                                                    invoiceItems.add(
                                                                        invoiceItemModel
                                                                    )
                                                                }
                                                            }
                                                            withContext(Dispatchers.Main) {
                                                                viewModel.updateState(
                                                                    state.copy(
                                                                        invoiceItems = invoiceItems,
                                                                        invoiceHeader = POSUtils.refreshValues(
                                                                            invoiceItems,
                                                                            state.invoiceHeader
                                                                        )
                                                                    )
                                                                )
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
                                    }
                                }) {
                                Icon(
                                    Icons.Default.QrCode2,
                                    contentDescription = "Barcode",
                                    tint = SettingsModel.buttonColor
                                )
                            }

                            Text(
                                text = "Discount: ${state.invoiceHeader.invoiceHeadDiscount}%",
                                modifier = Modifier.wrapContentWidth(),
                                textAlign = TextAlign.End,
                                style = TextStyle(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp
                                ),
                                color = SettingsModel.textColor
                            )

                            Text(
                                text = Utils.getItemsNumberStr(state.invoiceItems.size),
                                modifier = Modifier.wrapContentWidth(),
                                textAlign = TextAlign.End,
                                style = TextStyle(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp
                                ),
                                color = SettingsModel.textColor
                            )
                        }


                        InvoiceFooterView(
                            state, viewModel,
                            modifier = Modifier
                                .wrapContentWidth()
                                .wrapContentHeight(),
                            isFromTable = sharedViewModel.isFromTable,
                            onLoadClients = { viewModel.fetchThirdParties() },
                            onLoadInvoices = {
                                if (state.thirdParties.isEmpty()) {
                                    viewModel.fetchThirdParties()
                                }
                                viewModel.fetchInvoices()
                            },
                            onLoadItems = {
                                viewModel.loadFamiliesAndItems()
                            },
                            onAddItem = {
                                navController?.navigate(
                                    "ManageItemsView"
                                )
                                sharedViewModel.fetchItemsAgain = true
                            },
                            onAddThirdParty = {
                                navController?.navigate(
                                    "ManageThirdPartiesView"
                                )
                                sharedViewModel.fetchThirdPartiesAgain = true
                            },
                            onItemSelected = { item ->
                                scope.launch {
                                    viewModel.isInvoiceEdited = true
                                    var proceed = true
                                    if (item.itemRemQty <= 0) {
                                        proceed = SettingsModel.allowOutOfStockSale
                                        if (SettingsModel.showItemQtyAlert) {
                                            sharedViewModel.showPopup(
                                                true,
                                                PopupModel(
                                                    dialogText = "Not enough stock available for ${item.itemName}. Please adjust the quantity.",
                                                    positiveBtnText = "Close",
                                                    negativeBtnText = null
                                                )
                                            )
                                        }
                                    }
                                    if (proceed) {
                                        withContext(Dispatchers.IO) {
                                            sharedViewModel.updateRealItemPrice(item)
                                        }
                                        val invoiceItemModel = InvoiceItemModel()
                                        invoiceItemModel.setItem(item)
                                        invoiceItemModel.shouldPrint = true
                                        val invoiceItems = state.invoiceItems.toMutableList()
                                        invoiceItems.add(invoiceItemModel)
                                        viewModel.updateState(
                                            state.copy(
                                                invoiceItems = invoiceItems,
                                                invoiceHeader = POSUtils.refreshValues(
                                                    invoiceItems,
                                                    state.invoiceHeader
                                                )
                                            )
                                        )
                                        viewModel.isAddItemBottomSheetVisible.value = false
                                    }
                                }
                            },
                            onThirdPartySelected = { thirdParty ->
                                viewModel.isInvoiceEdited =
                                    viewModel.isInvoiceEdited || state.invoiceHeader.invoiceHeadThirdPartyName != thirdParty.thirdPartyId
                                viewModel.updateState(
                                    state.copy(
                                        selectedThirdParty = thirdParty,
                                        invoiceHeader = state.invoiceHeader.copy(
                                            invoiceHeadThirdPartyName = thirdParty.thirdPartyId
                                        )
                                    )
                                )
                            },
                            onInvoiceSelected = { invoiceHeader ->
                                if (state.invoiceItems.isNotEmpty()) {
                                    sharedViewModel.tempInvoiceHeader = invoiceHeader
                                    viewModel.popupState = PopupState.CHANGE_ITEM
                                    viewModel.isSavePopupVisible.value = true
                                } else {
                                    viewModel.loadInvoiceDetails(invoiceHeader)
                                }
                            })
                    }
                    if (state.isLandscape && SettingsModel.showItemsInPOS) {
                        if (state.items.isEmpty()) {
                            viewModel.loadFamiliesAndItems()
                        }
                        AddInvoiceItemView(
                            sharedViewModel = sharedViewModel,
                            categories = state.families,
                            items = state.items,
                            notifyDirectly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                        ) { itemList ->
                            viewModel.isInvoiceEdited = true
                            val invoices = state.invoiceItems.toMutableList()
                            itemList.forEach { item ->
                                val invoiceItemModel = InvoiceItemModel()
                                invoiceItemModel.setItem(item)
                                invoiceItemModel.shouldPrint = true
                                invoices.add(invoiceItemModel)
                            }
                            viewModel.updateState(
                                state.copy(
                                    invoiceItems = invoices,
                                    invoiceHeader = POSUtils.refreshValues(
                                        invoices,
                                        state.invoiceHeader
                                    )
                                )
                            )
                            viewModel.isAddItemBottomSheetVisible.value = false
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = viewModel.isEditBottomSheetVisible.value,
                enter = fadeIn(
                    initialAlpha = 0.4f
                ),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = 250)
                )
            ) {
                EditInvoiceItemView(invoices = state.invoiceItems.toMutableList(),
                    invHeader = state.invoiceHeader,
                    invoiceIndex = viewModel.selectedItemIndex,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                        .background(
                            color = SettingsModel.backgroundColor
                        ),
                    triggerOnSave = viewModel.triggerSaveCallback.value,
                    onSave = { invHeader, itemModel ->
                        val isChanged =
                            !itemModel.invoice.isNew() && viewModel.invoiceItemModels[viewModel.selectedItemIndex].invoice.didChanged(
                                itemModel.invoice
                            ) || sharedViewModel.tempInvoiceHeader?.didChanged(invHeader) == true
                        viewModel.isInvoiceEdited = viewModel.isInvoiceEdited || isChanged
                        val invoiceItems = state.invoiceItems.toMutableList()
                        invoiceItems[viewModel.selectedItemIndex] = itemModel
                        viewModel.updateState(
                            state.copy(
                                invoiceItems = invoiceItems,
                                invoiceHeader = invHeader
                            )
                        )
                        viewModel.isEditBottomSheetVisible.value = false
                        viewModel.triggerSaveCallback.value = false
                    })
            }

            AnimatedVisibility(
                viewModel.isAddItemBottomSheetVisible.value,
                enter = fadeIn(
                    initialAlpha = 0.4f
                ),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = 250)
                )
            ) {
                AddInvoiceItemView(sharedViewModel = sharedViewModel,
                    categories = state.families,
                    items = state.items,
                    triggerOnSave = viewModel.triggerSaveCallback.value,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                        .background(
                            color = SettingsModel.backgroundColor
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {})
                        }) { itemList ->
                    viewModel.isInvoiceEdited = true
                    val invoices = state.invoiceItems.toMutableList()
                    itemList.forEach { item ->
                        val invoiceItemModel = InvoiceItemModel()
                        invoiceItemModel.setItem(item)
                        invoiceItemModel.shouldPrint = true
                        invoices.add(invoiceItemModel)
                    }
                    viewModel.updateState(
                        state.copy(
                            invoiceItems = invoices,
                            invoiceHeader = POSUtils.refreshValues(
                                invoices,
                                state.invoiceHeader
                            )
                        )
                    )
                    viewModel.isAddItemBottomSheetVisible.value = false
                    viewModel.triggerSaveCallback.value = false
                }
            }

            AnimatedVisibility(
                visible = viewModel.isPayBottomSheetVisible.value,
                enter = fadeIn(
                    initialAlpha = 0.4f
                ),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = 250)
                )
            ) {
                InvoiceCashView(
                    invoiceHeader = state.invoiceHeader,
                    posReceipt = state.posReceipt,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                        .background(
                            color = SettingsModel.backgroundColor
                        ),
                    onPrint = {
                        if (viewModel.isInvoiceEdited) {
                            viewModel.showWarning(
                                "Save your changes at first!",
                                ""
                            )
                        } else if (state.invoiceHeader.isNew()) {
                            viewModel.showWarning(
                                "Save invoice at first!",
                                ""
                            )
                        } else {
                            viewModel.proceedToPrint = true
                            state.invoiceItems.forEach { invoiceItemModel ->
                                invoiceItemModel.shouldPrint = true
                            }
                            state.invoiceHeader.invoiceHeadPrinted += 1
                            viewModel.savePrintedNumber(context)
                        }
                    },
                    onSave = { change, receipt ->
                        state.posReceipt = receipt
                        state.invoiceHeader.invoiceHeadChange = change
                        viewModel.proceedToPrint = false
                        viewModel.saveInvoiceHeader(
                            context = context,
                            print = false,
                            finish = !sharedViewModel.isFromTable
                        )
                    },
                    onSaveAndPrintOrder = { change, receipt ->
                        state.posReceipt = receipt
                        state.invoiceHeader.invoiceHeadChange = change
                        state.invoiceHeader.invoiceHeadPrinted += 1
                        viewModel.proceedToPrint = true
                        viewModel.saveInvoiceHeader(
                            context = context,
                            print = true,
                            finish = state.invoiceHeader.isFinished()
                        )
                    },
                    onFinishAndPrint = { change, receipt ->
                        state.posReceipt = receipt
                        state.invoiceHeader.invoiceHeadChange = change
                        state.invoiceHeader.invoiceHeadPrinted += 1
                        viewModel.proceedToPrint = true
                        viewModel.saveInvoiceHeader(
                            context = context,
                            print = true,
                            finish = true
                        )
                    },
                )
            }
        }
    }
}
