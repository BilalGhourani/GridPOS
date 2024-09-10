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
import com.grid.pos.utils.Utils
import kotlinx.coroutines.launch

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
                    activityViewModel.invoiceItemModels = invoiceItems
                    activityViewModel.posReceipt = receipt
                })
        } else {
            state.isLoading = false
        }
    }

    LaunchedEffect(key1 = Unit) {
        if (state.items.isEmpty()) {
            state.items.addAll(activityViewModel.items)
        }
        if (state.families.isEmpty()) {
            state.families.addAll(activityViewModel.families)
        }
        if (state.thirdParties.isEmpty()) {
            state.thirdParties.addAll(activityViewModel.thirdParties)
        }
        if (state.invoiceHeaders.isEmpty()) {
            state.invoiceHeaders.addAll(activityViewModel.invoiceHeaders)
        }

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
        activityViewModel.invoiceItemModels.clear()
        activityViewModel.invoiceHeader = InvoiceHeader()
        activityViewModel.posReceipt = PosReceipt()
        activityViewModel.shouldPrintInvoice = true
        activityViewModel.shouldLoadInvoice = false
        activityViewModel.pendingInvHeadState = null
        invoicesState.clear()
        invoiceHeaderState.value = activityViewModel.invoiceHeader
    }

    LaunchedEffect(
        state.isSaved,
        state.isDeleted
    ) {
        if (state.isSaved) {
            isPayBottomSheetVisible = false
            if (activityViewModel.shouldPrintInvoice) {
                activityViewModel.invoiceItemModels = invoicesState
                activityViewModel.invoiceHeader = invoiceHeaderState.value
                navController?.navigate("UIWebView")
            } else if (activityViewModel.isFromTable) {
                navController?.navigateUp()
            } else {
                clear()
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
            activityViewModel.clearPosValues()
            if (SettingsModel.getUserType() == UserType.POS) {
                popupState = PopupState.BACK_PRESSED
                isSavePopupVisible = true
            } else {
                viewModel.closeConnectionIfNeeded()
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
                height = 150.dp
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
                                    activityViewModel.invoiceHeader = invoiceHeaderState.value
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
                            onAddItem = { isAddItemBottomSheetVisible = true },
                            onPay = { isPayBottomSheetVisible = true },
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
                                state.itemsToDelete.add(invoicesState.removeAt(index))
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
                                                            val invoiceItemModel = InvoiceItemModel()
                                                            invoiceItemModel.setItem(itm)
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
                            onAddItem = {
                                activityViewModel.invoiceItemModels = invoicesState
                                activityViewModel.invoiceHeader = invoiceHeaderState.value
                                navController?.navigate(
                                    "ManageItemsView"
                                )
                                state.items.clear()
                            },
                            onAddThirdParty = {
                                activityViewModel.invoiceItemModels = invoicesState
                                activityViewModel.invoiceHeader = invoiceHeaderState.value
                                navController?.navigate(
                                    "ManageThirdPartiesView"
                                )
                                state.thirdParties.clear()
                            },
                            onItemSelected = { item ->
                                val invoiceItemModel = InvoiceItemModel()
                                invoiceItemModel.setItem(item)
                                invoicesState.add(invoiceItemModel)
                                activityViewModel.invoiceItemModels = invoicesState
                                invoiceHeaderState.value = POSUtils.refreshValues(
                                    activityViewModel.invoiceItemModels,
                                    invoiceHeaderState.value
                                )
                                isAddItemBottomSheetVisible = false
                            },
                            onThirdPartySelected = { thirdParty ->
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
                        AddInvoiceItemView(
                            categories = state.families,
                            items = state.items,
                            notifyDirectly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                        ) { itemList ->
                            val invoices = mutableListOf<InvoiceItemModel>()
                            itemList.forEach { item ->
                                val invoiceItemModel = InvoiceItemModel()
                                invoiceItemModel.setItem(item)
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
                AddInvoiceItemView(categories = state.families,
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
                    val invoices = mutableListOf<InvoiceItemModel>()
                    itemList.forEach { item ->
                        val invoiceItemModel = InvoiceItemModel()
                        invoiceItemModel.setItem(item)
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
                    onSave = { change, receipt ->
                        activityViewModel.posReceipt = receipt
                        invoiceHeaderState.value.invoiceHeadChange = change
                        activityViewModel.shouldPrintInvoice = false
                        viewModel.saveInvoiceHeader(
                            invoiceHeader = invoiceHeaderState.value,
                            posReceipt = activityViewModel.posReceipt,
                            invoiceItems = activityViewModel.invoiceItemModels,
                            finish = !activityViewModel.isFromTable
                        )
                    },
                    onSaveAndPrintOrder = { change, receipt ->
                        activityViewModel.posReceipt = receipt
                        invoiceHeaderState.value.invoiceHeadChange = change
                        activityViewModel.shouldPrintInvoice = true
                        viewModel.saveInvoiceHeader(
                            invoiceHeader = invoiceHeaderState.value,
                            posReceipt = activityViewModel.posReceipt,
                            invoiceItems = activityViewModel.invoiceItemModels,
                            finish = false
                        )
                    },
                    onFinishAndPrint = { change, receipt ->
                        activityViewModel.posReceipt = receipt
                        invoiceHeaderState.value.invoiceHeadChange = change
                        activityViewModel.shouldPrintInvoice = true
                        viewModel.saveInvoiceHeader(
                            invoiceHeader = invoiceHeaderState.value,
                            posReceipt = activityViewModel.posReceipt,
                            invoiceItems = activityViewModel.invoiceItemModels,
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
