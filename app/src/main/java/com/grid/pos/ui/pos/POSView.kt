package com.grid.pos.ui.pos

import android.annotation.SuppressLint
import android.content.res.Configuration
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.grid.pos.ActivityScopedViewModel
import com.grid.pos.R
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.PosReceipt.PosReceipt
import com.grid.pos.interfaces.OnBarcodeResult
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.UserType
import com.grid.pos.ui.pos.components.AddInvoiceItemView
import com.grid.pos.ui.pos.components.EditInvoiceItemView
import com.grid.pos.ui.pos.components.InvoiceBodyDetails
import com.grid.pos.ui.pos.components.InvoiceCashView
import com.grid.pos.ui.pos.components.InvoiceFooterView
import com.grid.pos.ui.pos.components.InvoiceHeaderDetails
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.PrinterUtils
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
        activityViewModel: ActivityScopedViewModel,
        viewModel: POSViewModel = hiltViewModel()
) {
    val state by viewModel.posState.collectAsStateWithLifecycle()
    val invoicesState = remember { mutableStateListOf<InvoiceItemModel>() }
    val invoiceHeaderState = remember { mutableStateOf(activityViewModel.invoiceHeader) }
    var itemIndexToEdit by remember { mutableIntStateOf(-1) }
    var isInvoiceEdited by remember { mutableStateOf(false) }
    var proceedToPrint by remember { mutableStateOf(false) }
    var isEditBottomSheetVisible by remember { mutableStateOf(false) }
    var isAddItemBottomSheetVisible by remember { mutableStateOf(false) }
    var isPayBottomSheetVisible by remember { mutableStateOf(false) }
    var isSavePopupVisible by remember { mutableStateOf(false) }
    var popupState by remember { mutableStateOf(PopupState.BACK_PRESSED) }
    val snackbarHostState = remember { SnackbarHostState() }
    var orientation by remember { mutableIntStateOf(Configuration.ORIENTATION_PORTRAIT) }
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = Utils.isTablet(LocalConfiguration.current)
    val isDeviceLargerThan7Inches = Utils.isDeviceLargerThan7Inches(context)
    var isLandscape by remember { mutableStateOf(orientation == Configuration.ORIENTATION_LANDSCAPE) }
    val keyboardController = LocalSoftwareKeyboardController.current

    fun selectInvoice(invoiceHeader: InvoiceHeader) {
        if (invoiceHeader.invoiceHeadId.isNotEmpty()) {
            invoiceHeaderState.value = invoiceHeader
            viewModel.loadInvoiceDetails(invoiceHeader,
                onSuccess = { receipt, invoiceItems ->
                    invoicesState.clear()
                    invoicesState.addAll(invoiceItems)
                    invoiceHeaderState.value = POSUtils.refreshValues(
                        invoiceItems,
                        invoiceHeader
                    )
                    activityViewModel.initialInvoiceItemModels = invoiceItems
                    activityViewModel.invoiceItemModels = invoiceItems
                    activityViewModel.posReceipt = receipt
                    isInvoiceEdited = false
                })
        } else {
            state.isLoading = false
        }
    }

    LaunchedEffect(
        activityViewModel.items
    ) {
        state.items = activityViewModel.items.toMutableList()
    }

    LaunchedEffect(
        activityViewModel.families
    ) {
        state.families = activityViewModel.families.toMutableList()
    }
    LaunchedEffect(
        activityViewModel.thirdParties
    ) {
        if (SettingsModel.connectionType == CONNECTION_TYPE.LOCAL.key) {
            state.thirdParties = activityViewModel.thirdParties.toMutableList()
        }
    }
    LaunchedEffect(
        activityViewModel.invoiceHeaders
    ) {
        if (SettingsModel.connectionType == CONNECTION_TYPE.LOCAL.key) {
            state.invoiceHeaders = activityViewModel.invoiceHeaders.toMutableList()
        }
    }

    LaunchedEffect(key1 = Unit) {
        if (activityViewModel.shouldLoadInvoice) {
            activityViewModel.shouldLoadInvoice = false
            selectInvoice(invoiceHeaderState.value)
        } else if (invoicesState.isEmpty() && activityViewModel.invoiceItemModels.isNotEmpty()) {
            invoicesState.addAll(activityViewModel.invoiceItemModels)
        }
    }

    LaunchedEffect(configuration) {
        snapshotFlow { configuration.orientation }.collect {
            isLandscape = it == Configuration.ORIENTATION_LANDSCAPE
            orientation = it
            isEditBottomSheetVisible = false
            isAddItemBottomSheetVisible = false
            isPayBottomSheetVisible = false
        }
    }

    LaunchedEffect(state.isLoading) {
        activityViewModel.showLoading(state.isLoading)
    }

    val scope = rememberCoroutineScope()
    LaunchedEffect(state.warning) {
        state.warning?.value?.let { message ->
            scope.launch {
                val snackBarResult = snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short,
                    actionLabel = state.actionLabel
                )
                when (snackBarResult) {
                    SnackbarResult.Dismissed -> {}
                    SnackbarResult.ActionPerformed -> when (state.actionLabel) {
                        "Settings" -> activityViewModel.openAppStorageSettings()
                    }
                }
            }
        }
    }
    fun clear() {
        activityViewModel.clearPosValues()
        proceedToPrint = true
        invoicesState.clear()
        invoiceHeaderState.value = activityViewModel.invoiceHeader
    }

    fun cashLoadedData() {
        activityViewModel.invoiceHeaders = state.invoiceHeaders.toMutableList()
        activityViewModel.thirdParties = state.thirdParties.toMutableList()
        activityViewModel.families = state.families.toMutableList()
        activityViewModel.items = state.items.toMutableList()
    }

    LaunchedEffect(
        state.isSaved,
        state.isDeleted
    ) {
        if (state.isSaved) {
            isPayBottomSheetVisible = false
            if (proceedToPrint) {
                activityViewModel.invoiceItemModels = invoicesState
                activityViewModel.deletedInvoiceItems = state.itemsToDelete
                activityViewModel.invoiceHeader = invoiceHeaderState.value
                cashLoadedData()
                navController?.navigate("UIWebView")
            } else if (SettingsModel.autoPrintTickets) {
                state.isLoading = true
                val invoices = invoicesState.filter { it.invoice.isNew() || it.shouldPrint }
                    .toMutableList()
                invoices.addAll(state.itemsToDelete)
                if (invoices.isNotEmpty()) {
                    scope.launch {
                        withContext(Dispatchers.Default) {
                            activityViewModel.invoiceHeader = invoiceHeaderState.value
                            cashLoadedData()
                            PrinterUtils.printTickets(
                                context = context,
                                invoiceHeader = invoiceHeaderState.value,
                                invoiceItemModels = invoices,
                                printers = activityViewModel.printers
                            )
                        }
                        withContext(Dispatchers.Main) {
                            state.isLoading = false
                            clear()
                            navController?.navigateUp()
                        }
                    }
                } else {
                    clear()
                    navController?.navigateUp()
                }
            } else {
                clear()
                navController?.navigateUp()
            }
            state.isSaved = false
        }
        if (state.isDeleted) {
            state.invoiceHeaders.remove(invoiceHeaderState.value)
            activityViewModel.invoiceHeaders = state.invoiceHeaders
            clear()
            state.isDeleted = false
            if (activityViewModel.isFromTable) {
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
        } else if (isAddItemBottomSheetVisible) {
            isAddItemBottomSheetVisible = false
        } else if (isEditBottomSheetVisible) {
            isEditBottomSheetVisible = false
        } else if (isPayBottomSheetVisible) {
            isPayBottomSheetVisible = false
        } else if (invoicesState.isNotEmpty()) {
            popupState = PopupState.DISCARD_INVOICE
            isSavePopupVisible = true
        } else {
            if (!invoiceHeaderState.value.invoiceHeadTableId.isNullOrEmpty()) {
                viewModel.unLockTable(
                    invoiceHeaderState.value.invoiceHeadId,
                    invoiceHeaderState.value.invoiceHeadTableId!!,
                    invoiceHeaderState.value.invoiceHeadTableType
                )
            }
            activityViewModel.clearPosValues()
            cashLoadedData()
            if (SettingsModel.getUserType() == UserType.POS) {
                popupState = PopupState.BACK_PRESSED
                isSavePopupVisible = true
            } else {
                navController?.navigateUp()
            }
        }
    }
    LaunchedEffect(isSavePopupVisible) {
        activityViewModel.showPopup(isSavePopupVisible,
            if (!isSavePopupVisible) null else PopupModel().apply {
                onDismissRequest = {
                    isSavePopupVisible = false
                }
                onConfirmation = {
                    isSavePopupVisible = false
                    if(popupState == PopupState.DISCARD_INVOICE){
                        if (!invoiceHeaderState.value.invoiceHeadTableId.isNullOrEmpty()) {
                            viewModel.unLockTable(
                                invoiceHeaderState.value.invoiceHeadId,
                                invoiceHeaderState.value.invoiceHeadTableId!!,
                                invoiceHeaderState.value.invoiceHeadTableType
                            )
                        }
                    }
                    if (popupState != PopupState.DELETE_INVOICE) {
                        invoicesState.clear()
                        invoiceHeaderState.value = InvoiceHeader()
                        activityViewModel.posReceipt = PosReceipt()
                        activityViewModel.invoiceHeader = invoiceHeaderState.value
                        activityViewModel.invoiceItemModels.clear()
                        activityViewModel.shouldLoadInvoice = false
                    }
                    when (popupState) {
                        PopupState.BACK_PRESSED -> {
                            activityViewModel.logout()
                            navController?.clearBackStack("LoginView")
                            navController?.navigate("LoginView")
                        }

                        PopupState.DISCARD_INVOICE -> {
                            if (SettingsModel.getUserType() != UserType.POS) {
                                handleBack()
                            }
                        }

                        PopupState.CHANGE_INVOICE -> {
                            activityViewModel.pendingInvHeadState?.let {
                                selectInvoice(it)
                            }
                        }

                        PopupState.DELETE_INVOICE -> {
                            viewModel.deleteInvoiceHeader(
                                invoiceHeaderState.value,
                                activityViewModel.posReceipt,
                                invoicesState.toMutableList()
                            )
                        }
                    }
                }
                dialogText = when (popupState) {
                    PopupState.BACK_PRESSED -> "Are you sure you want to logout?"
                    PopupState.DELETE_INVOICE -> "Are you sure you want to Delete this invoice?"
                    else -> "Are you sure you want to discard current invoice?"
                }
                positiveBtnText = when (popupState) {
                    PopupState.BACK_PRESSED -> "Logout"
                    PopupState.DELETE_INVOICE -> "Delete"
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
                            if (!isEditBottomSheetVisible && !isAddItemBottomSheetVisible && !isPayBottomSheetVisible) {
                                IconButton(onClick = {
                                    handleBack()
                                }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = SettingsModel.buttonColor
                                    )
                                }
                            }
                        },
                        title = {
                            Text(
                                text = if (isEditBottomSheetVisible) "Edit Item" else if (isAddItemBottomSheetVisible) "Add Items" else if (isPayBottomSheetVisible) "Pay" else "POS",
                                color = SettingsModel.textColor,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        },
                        actions = {
                            if (!isEditBottomSheetVisible && !isAddItemBottomSheetVisible && !isPayBottomSheetVisible) {
                                IconButton(onClick = {
                                    activityViewModel.invoiceItemModels = invoicesState
                                    activityViewModel.deletedInvoiceItems = state.itemsToDelete
                                    activityViewModel.invoiceHeader = invoiceHeaderState.value
                                    cashLoadedData()
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
                        modifier = (if (isLandscape && SettingsModel.showItemsInPOS) Modifier.fillMaxWidth(
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
                            isPayEnabled = invoicesState.size > 0,
                            isDeleteEnabled = !invoiceHeaderState.value.isNew(),
                            onAddItem = {
                                if (state.items.isEmpty()) {
                                    viewModel.loadFamiliesAndItems()
                                }
                                isAddItemBottomSheetVisible = true
                            },
                            onPay = {
                                isPayBottomSheetVisible = true
                            },
                            onDelete = {
                                popupState = PopupState.DELETE_INVOICE
                                isSavePopupVisible = true
                            })

                        // Border stroke configuration
                        val borderStroke = BorderStroke(
                            1.dp,
                            Color.Black
                        )

                        InvoiceBodyDetails(invoices = invoicesState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(
                                    Utils.getListHeight(
                                        invoicesState.size,
                                        50
                                    )
                                )
                                .border(borderStroke),
                            isLandscape = isTablet || isDeviceLargerThan7Inches || isLandscape,
                            onEdit = { index ->
                                itemIndexToEdit = index
                                isEditBottomSheetVisible = true
                            },
                            onRemove = { index ->
                                val deletedRow = invoicesState.removeAt(index)
                                if (!deletedRow.invoice.isNew()) {
                                    deletedRow.isDeleted = true
                                    state.itemsToDelete.add(deletedRow)
                                }
                                activityViewModel.invoiceItemModels = invoicesState
                                invoiceHeaderState.value = POSUtils.refreshValues(
                                    activityViewModel.invoiceItemModels,
                                    invoiceHeaderState.value
                                )
                            })
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(25.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(modifier = Modifier.size(25.dp),
                                onClick = {
                                    activityViewModel.launchBarcodeScanner(false,
                                        ArrayList(state.items),
                                        object : OnBarcodeResult {
                                            override fun OnBarcodeResult(barcodesList: List<String>) {
                                                if (barcodesList.isNotEmpty()) {
                                                    scope.launch {
                                                        val map: Map<String, Int> = barcodesList.groupingBy { barcode -> barcode }
                                                            .eachCount()
                                                        val barcodes = barcodesList.joinToString(",")
                                                        val items = state.items.filter { item ->
                                                            item.itemBarcode?.let { barcode ->
                                                                barcodes.contains(
                                                                    barcode,
                                                                    ignoreCase = true
                                                                )
                                                            } ?: false
                                                        }
                                                        items.forEach { itm ->
                                                            val count = itm.itemBarcode?.let { barcode -> map[barcode] } ?: 1
                                                            for (i in 0 until count) {
                                                                withContext(Dispatchers.IO) {
                                                                    itm.itemRealUnitPrice = activityViewModel.updateRealItemPrice(itm)
                                                                }
                                                                val invoiceItemModel = InvoiceItemModel()
                                                                invoiceItemModel.setItem(itm)
                                                                invoiceItemModel.shouldPrint = true
                                                                invoicesState.add(invoiceItemModel)
                                                                activityViewModel.invoiceItemModels = invoicesState
                                                                invoiceHeaderState.value = POSUtils.refreshValues(
                                                                    activityViewModel.invoiceItemModels,
                                                                    invoiceHeaderState.value
                                                                )
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

                            Text(
                                text = "Discount: ${invoiceHeaderState.value.invoiceHeadDiscount}%",
                                modifier = Modifier.wrapContentWidth(),
                                textAlign = TextAlign.End,
                                style = TextStyle(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp
                                ),
                                color = SettingsModel.textColor
                            )

                            Text(
                                text = Utils.getItemsNumberStr(invoicesState),
                                modifier = Modifier.wrapContentWidth(),
                                textAlign = TextAlign.End,
                                style = TextStyle(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp
                                ),
                                color = SettingsModel.textColor
                            )
                        }


                        InvoiceFooterView(invoiceHeader = invoiceHeaderState.value,
                            items = state.items,
                            thirdParties = state.thirdParties.toMutableList(),
                            invoiceHeaders = state.invoiceHeaders,
                            modifier = Modifier
                                .wrapContentWidth()
                                .wrapContentHeight(),
                            isFromTable = activityViewModel.isFromTable,
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
                                activityViewModel.invoiceItemModels = invoicesState
                                activityViewModel.deletedInvoiceItems = state.itemsToDelete
                                activityViewModel.invoiceHeader = invoiceHeaderState.value
                                cashLoadedData()
                                state.items.clear()
                                navController?.navigate(
                                    "ManageItemsView"
                                )
                            },
                            onAddThirdParty = {
                                activityViewModel.invoiceItemModels = invoicesState
                                activityViewModel.deletedInvoiceItems = state.itemsToDelete
                                activityViewModel.invoiceHeader = invoiceHeaderState.value
                                cashLoadedData()
                                state.thirdParties.clear()
                                navController?.navigate(
                                    "ManageThirdPartiesView"
                                )
                            },
                            onItemSelected = { item ->
                                scope.launch {
                                    isInvoiceEdited = true
                                    var proceed = true
                                    if (item.itemRemQty <= 0) {
                                        proceed = SettingsModel.allowOutOfStockSale
                                        if (SettingsModel.showItemQtyAlert) {
                                            activityViewModel.showPopup(
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
                                            item.itemRealUnitPrice = activityViewModel.updateRealItemPrice(item)
                                        }
                                        val invoiceItemModel = InvoiceItemModel()
                                        invoiceItemModel.setItem(item)
                                        invoiceItemModel.shouldPrint = true
                                        invoicesState.add(invoiceItemModel)
                                        activityViewModel.invoiceItemModels = invoicesState
                                        invoiceHeaderState.value = POSUtils.refreshValues(
                                            activityViewModel.invoiceItemModels,
                                            invoiceHeaderState.value
                                        )
                                        isAddItemBottomSheetVisible = false
                                    }
                                }
                            },
                            onThirdPartySelected = { thirdParty ->
                                isInvoiceEdited = isInvoiceEdited || invoiceHeaderState.value.invoiceHeadThirdPartyName != thirdParty.thirdPartyId
                                state.selectedThirdParty = thirdParty
                                invoiceHeaderState.value.invoiceHeadThirdPartyName = thirdParty.thirdPartyId
                            },
                            onInvoiceSelected = { invoiceHeader ->
                                if (invoicesState.isNotEmpty()) {
                                    activityViewModel.pendingInvHeadState = invoiceHeader
                                    popupState = PopupState.CHANGE_INVOICE
                                    isSavePopupVisible = true
                                } else {
                                    selectInvoice(invoiceHeader)
                                }
                            })
                    }
                    if (isLandscape && SettingsModel.showItemsInPOS) {
                        if (state.items.isEmpty()) {
                            viewModel.loadFamiliesAndItems()
                        }
                        AddInvoiceItemView(
                            activityViewModel = activityViewModel,
                            categories = state.families,
                            items = state.items,
                            notifyDirectly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                        ) { itemList ->
                            isInvoiceEdited = true
                            val invoices = mutableListOf<InvoiceItemModel>()
                            itemList.forEach { item ->
                                val invoiceItemModel = InvoiceItemModel()
                                invoiceItemModel.setItem(item)
                                invoiceItemModel.shouldPrint = true
                                invoices.add(invoiceItemModel)
                            }
                            invoicesState.addAll(invoices)
                            activityViewModel.invoiceItemModels = invoicesState
                            invoiceHeaderState.value = POSUtils.refreshValues(
                                activityViewModel.invoiceItemModels,
                                invoiceHeaderState.value
                            )
                            isAddItemBottomSheetVisible = false
                        }
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
                EditInvoiceItemView(invoices = invoicesState.toMutableList(),
                    invHeader = invoiceHeaderState.value,
                    invoiceIndex = itemIndexToEdit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                        .background(
                            color = SettingsModel.backgroundColor
                        ),
                    onSave = { invHeader, itemModel ->
                        val isChanged = !itemModel.invoice.isNew() && activityViewModel.initialInvoiceItemModels[itemIndexToEdit].invoice.didChanged(itemModel.invoice) || (activityViewModel.pendingInvHeadState ?: activityViewModel.invoiceHeader).didChanged(invHeader)
                        isInvoiceEdited = isInvoiceEdited || isChanged/* itemModel.shouldPrint = activityViewModel.isInvoiceItemQtyChanged(
                            itemModel.invoice.invoiceId,
                            itemModel.invoice.invoiceQuantity
                        )*/
                        invoicesState[itemIndexToEdit] = itemModel
                        activityViewModel.invoiceItemModels = invoicesState
                        invoiceHeaderState.value = invHeader
                        activityViewModel.invoiceHeader = invHeader
                        isEditBottomSheetVisible = false
                    },
                    onClose = {
                        isEditBottomSheetVisible = false
                    })
            }

            AnimatedVisibility(
                isAddItemBottomSheetVisible,
                enter = fadeIn(
                    initialAlpha = 0.4f
                ),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = 250)
                )
            ) {
                AddInvoiceItemView(activityViewModel = activityViewModel,
                    categories = state.families,
                    items = state.items,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                        .background(
                            color = SettingsModel.backgroundColor
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {})
                        }) { itemList ->
                    isInvoiceEdited = true
                    val invoices = mutableListOf<InvoiceItemModel>()
                    itemList.forEach { item ->
                        val invoiceItemModel = InvoiceItemModel()
                        invoiceItemModel.setItem(item)
                        invoiceItemModel.shouldPrint = true
                        invoices.add(invoiceItemModel)
                    }
                    invoicesState.addAll(invoices)
                    activityViewModel.invoiceItemModels = invoicesState
                    invoiceHeaderState.value = POSUtils.refreshValues(
                        activityViewModel.invoiceItemModels,
                        invoiceHeaderState.value
                    )
                    activityViewModel.invoiceHeader = invoiceHeaderState.value
                    isAddItemBottomSheetVisible = false
                }
            }

            AnimatedVisibility(
                visible = isPayBottomSheetVisible,
                enter = fadeIn(
                    initialAlpha = 0.4f
                ),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = 250)
                )
            ) {
                InvoiceCashView(
                    invoiceHeader = invoiceHeaderState.value,
                    posReceipt = activityViewModel.posReceipt,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                        .background(
                            color = SettingsModel.backgroundColor
                        ),
                    onPrint = {
                        if (isInvoiceEdited) {
                            viewModel.showWarning(
                                "Save your changes at first!",
                                ""
                            )
                        } else if (invoiceHeaderState.value.isNew()) {
                            viewModel.showWarning(
                                "Save invoice at first!",
                                ""
                            )
                        } else {
                            proceedToPrint = true
                            invoicesState.forEach {
                                it.shouldPrint = true
                            }
                            invoiceHeaderState.value.invoiceHeadPrinted += 1
                            viewModel.savePrintedNumber(
                                invoiceHeaderState.value
                            )
                        }
                    },
                    onSave = { change, receipt ->
                        activityViewModel.posReceipt = receipt
                        invoiceHeaderState.value.invoiceHeadChange = change
                        proceedToPrint = false
                        viewModel.saveInvoiceHeader(
                            invoiceHeader = invoiceHeaderState.value,
                            posReceipt = activityViewModel.posReceipt,
                            invoiceItems = activityViewModel.invoiceItemModels,
                            print = false,
                            finish = !activityViewModel.isFromTable
                        )
                    },
                    onSaveAndPrintOrder = { change, receipt ->
                        activityViewModel.posReceipt = receipt
                        invoiceHeaderState.value.invoiceHeadChange = change
                        invoiceHeaderState.value.invoiceHeadPrinted += 1
                        proceedToPrint = true
                        viewModel.saveInvoiceHeader(
                            invoiceHeader = invoiceHeaderState.value,
                            posReceipt = activityViewModel.posReceipt,
                            invoiceItems = activityViewModel.invoiceItemModels,
                            print = true,
                            finish = invoiceHeaderState.value.isFinished()
                        )
                    },
                    onFinishAndPrint = { change, receipt ->
                        activityViewModel.posReceipt = receipt
                        invoiceHeaderState.value.invoiceHeadChange = change
                        invoiceHeaderState.value.invoiceHeadPrinted += 1
                        proceedToPrint = true
                        viewModel.saveInvoiceHeader(
                            invoiceHeader = invoiceHeaderState.value,
                            posReceipt = activityViewModel.posReceipt,
                            invoiceItems = activityViewModel.invoiceItemModels,
                            print = true,
                            finish = true
                        )
                    },
                )
            }
        }
    }
}

enum class PopupState(val key: String) {
    BACK_PRESSED("BACK_PRESSEF"), DISCARD_INVOICE("DISCARD_INVOICE"), CHANGE_INVOICE("CHANGE_INVOICE"), DELETE_INVOICE(
        "DELETE_INVOICE"
    )
}
