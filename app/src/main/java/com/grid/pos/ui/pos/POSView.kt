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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.grid.pos.ActivityScopedViewModel
import com.grid.pos.MainActivity
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.PosReceipt.PosReceipt
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.pos.components.AddInvoiceItemView
import com.grid.pos.ui.pos.components.EditInvoiceItemView
import com.grid.pos.ui.pos.components.InvoiceBodyDetails
import com.grid.pos.ui.pos.components.InvoiceCashView
import com.grid.pos.ui.pos.components.InvoiceFooterView
import com.grid.pos.ui.pos.components.InvoiceHeaderDetails
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POSView(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    activityViewModel: ActivityScopedViewModel,
    mainActivity: MainActivity,
    viewModel: POSViewModel = hiltViewModel()
) {
    val posState: POSState by viewModel.posState.collectAsState(POSState())
    val invoicesState = remember { mutableStateListOf<InvoiceItemModel>() }
    val invoiceHeaderState = remember { mutableStateOf(activityViewModel.invoiceHeader) }
    var itemIndexToEdit by remember { mutableIntStateOf(-1) }
    var isEditBottomSheetVisible by remember { mutableStateOf(false) }
    var isAddItemBottomSheetVisible by remember { mutableStateOf(false) }
    var isPayBottomSheetVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var orientation by remember { mutableIntStateOf(Configuration.ORIENTATION_PORTRAIT) }
    val configuration = LocalConfiguration.current
    val isTablet = Utils.isTablet(LocalConfiguration.current)
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE
    val keyboardController = LocalSoftwareKeyboardController.current
    if (activityViewModel.isFromTable) {
        posState.isLoading = true
        activityViewModel.isFromTable = false
        if (!invoiceHeaderState.value.invoiceHeadId.isNullOrEmpty()) {
            viewModel.loadInvoiceDetails(invoiceHeaderState.value,
                onSuccess = { receipt, invoiceItems ->
                    posState.isLoading = false
                    invoicesState.clear()
                    invoicesState.addAll(invoiceItems)
                    activityViewModel.invoiceItemModels = invoiceItems
                    activityViewModel.posReceipt = receipt
                })
        }
    } else if (invoicesState.isEmpty() && activityViewModel.invoiceItemModels.isNotEmpty()) {
        invoicesState.addAll(activityViewModel.invoiceItemModels)
    }

    if (posState.items.isEmpty()) {
        posState.items.addAll(activityViewModel.items)
    }
    if (posState.families.isEmpty()) {
        posState.families.addAll(activityViewModel.families)
    }
    if (posState.thirdParties.isEmpty()) {
        posState.thirdParties.addAll(activityViewModel.thirdParties)
    }
    if (posState.invoiceHeaders.isEmpty()) {
        posState.invoiceHeaders.addAll(activityViewModel.invoiceHeaders)
    }

    LaunchedEffect(configuration) {
        snapshotFlow { configuration.orientation }.collect {
            orientation = it
            isEditBottomSheetVisible = false
            isAddItemBottomSheetVisible = false
            isPayBottomSheetVisible = false
        }
    }

    LaunchedEffect(
        posState.warning,
        posState.isSaved
    ) {
        if (!posState.warning.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.Main).launch {
                snackbarHostState.showSnackbar(
                    message = posState.warning!!,
                    duration = SnackbarDuration.Short,
                )
            }
        }
        if (posState.isSaved) {
            activityViewModel.invoiceItemModels = invoicesState
            activityViewModel.invoiceHeader = invoiceHeaderState.value
            isPayBottomSheetVisible = false
            navController?.navigate("UIWebView")
            posState.isSaved = false
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
        } else {
            activityViewModel.invoiceItemModels = mutableListOf()
            activityViewModel.invoiceHeader = InvoiceHeader()
            activityViewModel.posReceipt = PosReceipt()
            if (SettingsModel.currentUser?.userPosMode == true && SettingsModel.currentUser?.userTableMode == false) {
                mainActivity.finish()
            } else {
                navController?.popBackStack()
            }
        }
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
                            IconButton(onClick = {
                                handleBack()
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
                                text = "POS",
                                color = SettingsModel.textColor,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
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
                        onAddItem = { isAddItemBottomSheetVisible = true },
                        onPay = { isPayBottomSheetVisible = true })

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
                        isLandscape = isTablet || isLandscape,
                        onEdit = { index ->
                            itemIndexToEdit = index
                            isEditBottomSheetVisible = true
                        },
                        onRemove = { index ->
                            invoicesState.removeAt(index)
                            activityViewModel.invoiceItemModels = invoicesState
                            invoiceHeaderState.value = POSUtils.refreshValues(
                                activityViewModel.invoiceItemModels,
                                invoiceHeaderState.value
                            )
                        })

                    InvoiceFooterView(invoiceHeader = invoiceHeaderState.value,
                        items = posState.items,
                        thirdParties = posState.thirdParties.toMutableList(),
                        invoiceHeaders = posState.invoiceHeaders,
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(350.dp),
                        onAddItem = {
                            activityViewModel.invoiceItemModels = invoicesState
                            activityViewModel.invoiceHeader = invoiceHeaderState.value
                            navController?.navigate(
                                "ManageItemsView"
                            )
                            posState.items.clear()
                        },
                        onAddThirdParty = {
                            activityViewModel.invoiceItemModels = invoicesState
                            activityViewModel.invoiceHeader = invoiceHeaderState.value
                            navController?.navigate(
                                "ManageThirdPartiesView"
                            )
                            posState.thirdParties.clear()
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
                            posState.selectedThirdParty = thirdParty
                            invoiceHeaderState.value.invoiceHeadThirdPartyName =
                                thirdParty.thirdPartyId
                        },
                        onInvoiceSelected = { invoiceHeader ->
                            invoiceHeaderState.value = invoiceHeader
                            posState.isLoading = true
                            viewModel.loadInvoiceDetails(invoiceHeader,
                                onSuccess = { receipt, invoiceItems ->
                                    posState.isLoading = false
                                    invoicesState.clear()
                                    invoicesState.addAll(invoiceItems)
                                    activityViewModel.invoiceItemModels = invoiceItems
                                    activityViewModel.posReceipt = receipt
                                })
                        })
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
                        invoiceHeaderState.value = POSUtils.refreshValues(
                            activityViewModel.invoiceItemModels,
                            invHeader
                        )
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
                AddInvoiceItemView(
                    categories = posState.families,
                    items = posState.items,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                        .background(
                            color = SettingsModel.backgroundColor
                        )
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
                        var cashName = invoiceHeaderState.value.getCashName()
                        val thirdPartyName = posState.selectedThirdParty.thirdPartyName ?: ""
                        cashName =
                            if (cashName.contains(thirdPartyName)) cashName else thirdPartyName + cashName
                        invoiceHeaderState.value.invoiceHeadCashName = cashName.ifEmpty { null }
                        viewModel.saveInvoiceHeader(
                            invoiceHeaderState.value,
                            activityViewModel.posReceipt,
                            activityViewModel.invoiceItemModels
                        )
                    },
                    onFinish = { change, receipt ->
                        activityViewModel.posReceipt = receipt
                        invoiceHeaderState.value.invoiceHeadChange = change
                        var cashName = invoiceHeaderState.value.getCashName()
                        val thirdPartyName = posState.selectedThirdParty.thirdPartyName ?: ""
                        cashName =
                            if (cashName.contains(thirdPartyName)) cashName else thirdPartyName + cashName
                        invoiceHeaderState.value.invoiceHeadCashName = cashName.ifEmpty { null }
                        viewModel.saveInvoiceHeader(
                            invoiceHeaderState.value,
                            activityViewModel.posReceipt,
                            activityViewModel.invoiceItemModels,
                            true
                        )
                    },
                )
            }
        }
    }
}