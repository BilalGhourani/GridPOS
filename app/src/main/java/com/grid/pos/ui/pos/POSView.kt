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
import com.grid.pos.data.invoiceHeader.InvoiceHeader
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POSView(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    viewModel: POSViewModel = hiltViewModel()
) {
    val state by viewModel.posState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(
        viewModel.shouldLoadInvoice(),
        viewModel.fetchItemsAgain(),
        viewModel.fetchThirdPartiesAgain()
    ) {
        if (viewModel.shouldLoadInvoice()) {
            viewModel.loadInvoiceFromTable()
        }
        if (viewModel.fetchItemsAgain()) {
            withContext(Dispatchers.IO) {
                viewModel.fetchItems()
            }
        }
        if (viewModel.fetchThirdPartiesAgain()) {
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

    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    fun handleBack() {
        if (viewModel.isLoading()) {
            return
        }
        if (isImeVisible) {
            keyboardController?.hide()
        } else {
            viewModel.handleBack { type, showPopup ->
                if (showPopup) {
                    val isBackPopup = type.equals("back", true)
                    viewModel.showPopup(
                        PopupModel().apply {
                            onDismissRequest = {
                                viewModel.popupState = null
                                viewModel.isSavePopupVisible.value = false
                            }
                            onConfirmation = {
                                if (isBackPopup) {
                                    viewModel.resetState()
                                    viewModel.logout()
                                    navController?.clearBackStack("LoginView")
                                    navController?.navigate("LoginView")
                                } else {
                                    if (!state.invoiceHeader.invoiceHeadTableId.isNullOrEmpty()) {
                                        viewModel.unLockTable()
                                    }
                                    state.invoiceItems.clear()
                                    state.invoiceHeader = InvoiceHeader()
                                    if (SettingsModel.getUserType() != UserType.POS) {
                                        handleBack()
                                    }
                                }
                            }
                            dialogText = if (isBackPopup) {
                                "Are you sure you want to logout?"
                            } else {
                                "Are you sure you want to discard current invoice?"
                            }
                            positiveBtnText = if (isBackPopup) {
                                "Logout"
                            } else {
                                "Discard"
                            }
                            negativeBtnText = "Cancel"
                        })
                } else {
                    navController?.navigateUp()
                }
            }
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
                                viewModel.showPopup(
                                    PopupModel().apply {
                                        onConfirmation = {
                                            viewModel.deleteInvoiceHeader {
                                                navController?.navigateUp()
                                            }
                                        }
                                        dialogText = "Are you sure you want to Delete this invoice?"
                                        positiveBtnText = "Delete"
                                        negativeBtnText = "Cancel"
                                    })
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
                                    viewModel.launchBarcodeScanner()
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
                                viewModel.needAddedData(true)
                                navController?.navigate(
                                    "ManageItemsView"
                                )
                            },
                            onAddThirdParty = {
                                viewModel.needAddedData(true)
                                navController?.navigate(
                                    "ManageThirdPartiesView"
                                )
                            },
                            onItemSelected = { item ->
                                scope.launch {
                                    viewModel.isInvoiceEdited = true
                                    var proceed = true
                                    if (item.itemRemQty <= 0) {
                                        proceed = SettingsModel.allowOutOfStockSale
                                        if (SettingsModel.showItemQtyAlert) {
                                            viewModel.showPopup(
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
                                            viewModel.updateRealItemPrice(item)
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
                                    viewModel.showPopup(
                                        PopupModel().apply {
                                            onConfirmation = {
                                                viewModel.loadInvoiceDetails(invoiceHeader)
                                            }
                                            dialogText =
                                                "Are you sure you want to discard current invoice?"
                                            positiveBtnText = "Discard"
                                            negativeBtnText = "Cancel"
                                        })
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
                            viewModel = viewModel,
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
                            ) || viewModel.currentInvoice?.didChanged(invHeader) == true
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
                AddInvoiceItemView(viewModel = viewModel,
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
                        viewModel.savePrintedNumber(context) {
                            navController?.navigate("UIWebView")
                        }
                    },
                    onSave = { change, receipt ->
                        state.posReceipt = receipt
                        state.invoiceHeader.invoiceHeadChange = change
                        viewModel.saveInvoiceHeader(
                            context = context,
                            print = false,
                            finish = !viewModel.isFromTable(),
                            proceedToPrint = false
                        ) {
                            navController?.navigateUp()
                        }
                    },
                    onSaveAndPrintOrder = { change, receipt ->
                        state.posReceipt = receipt
                        state.invoiceHeader.invoiceHeadChange = change
                        state.invoiceHeader.invoiceHeadPrinted += 1
                        viewModel.saveInvoiceHeader(
                            context = context,
                            print = true,
                            finish = state.invoiceHeader.isFinished(),
                            proceedToPrint = true
                        ) {
                            navController?.navigate("UIWebView")
                        }
                    },
                    onFinishAndPrint = { change, receipt ->
                        state.posReceipt = receipt
                        state.invoiceHeader.invoiceHeadChange = change
                        state.invoiceHeader.invoiceHeadPrinted += 1
                        viewModel.saveInvoiceHeader(
                            context = context,
                            print = true,
                            finish = true,
                            proceedToPrint = true
                        ) {
                            navController?.navigate("UIWebView")
                        }
                    },
                )
            }
        }
    }
}
