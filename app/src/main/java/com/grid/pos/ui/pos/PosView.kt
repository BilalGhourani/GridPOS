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
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.pos.components.AddInvoiceItemView
import com.grid.pos.ui.pos.components.EditInvoiceHeaderView
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
fun PosView(
        modifier: Modifier = Modifier,
        navController: NavController? = null,
        activityViewModel: ActivityScopedViewModel,
        viewModel: POSViewModel = hiltViewModel()
) {
    val posState: POSState by viewModel.posState.collectAsState(activityViewModel.posState)
    var invoicesState = remember { mutableStateListOf<InvoiceItemModel>() }
    var invoiceHeaderState = remember { mutableStateOf(posState.invoiceHeader) }
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

    LaunchedEffect(configuration) {
        snapshotFlow { configuration.orientation }.collect {
            orientation = it
            isEditBottomSheetVisible = false
            isAddItemBottomSheetVisible = false
            isPayBottomSheetVisible = false
        }
    }

    LaunchedEffect(
        posState.warning, posState.isSaved
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
            activityViewModel.posState = posState
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
            navController?.popBackStack()
        }
    }
    BackHandler {
        handleBack()
    }
    GridPOSTheme {
        Scaffold(containerColor = SettingsModel.backgroundColor, snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }, topBar = {
            Surface(
                shadowElevation = 3.dp, color = SettingsModel.backgroundColor
            ) {
                TopAppBar(colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = SettingsModel.topBarColor
                ), navigationIcon = {
                    IconButton(onClick = {
                        handleBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back", tint = SettingsModel.buttonColor
                        )
                    }
                }, title = {
                    Text(
                        text = "POS", color = SettingsModel.textColor, fontSize = 16.sp,
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
                        .wrapContentWidth()
                        .fillMaxWidth()
                        .verticalScroll(
                            rememberScrollState()
                        )
                        .padding(
                            horizontal = 10.dp, vertical = 10.dp
                        ),
                ) {
                    InvoiceHeaderDetails(modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp),
                        onAddItem = { isAddItemBottomSheetVisible = true },
                        onPay = { isPayBottomSheetVisible = true })

                    // Border stroke configuration
                    val borderStroke = BorderStroke(
                        1.dp, Color.Black
                    )

                    InvoiceBodyDetails(invoices = invoicesState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                Utils.getListHeight(
                                    invoicesState.size, 50
                                )
                            )
                            .border(borderStroke), isLandscape = isTablet || isLandscape,
                        onEdit = { index ->
                            itemIndexToEdit = index
                            isEditBottomSheetVisible = true
                        }, onRemove = { index ->
                            invoicesState.removeAt(index)
                            posState.invoices = invoicesState
                            invoiceHeaderState.value = posState.refreshValues()
                        })

                    InvoiceFooterView(
                        navController = navController,
                        invoiceHeader = invoiceHeaderState.value,
                        currency = posState.currency,
                        items = posState.items,
                        thirdParties = posState.thirdParties,
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(250.dp),
                        onItemSelected = { item ->
                            val invoiceItemModel = InvoiceItemModel()
                            invoiceItemModel.setItem(item)
                            invoicesState.add(invoiceItemModel)
                            posState.invoices = invoicesState
                            invoiceHeaderState.value = posState.refreshValues()
                            isAddItemBottomSheetVisible = false
                        },
                        onThirdPartySelected = { thirdParty ->
                            posState.selectedThirdParty = thirdParty
                            posState.invoiceHeader.invoiceHeadThirdPartyName = thirdParty.thirdPartyId
                        },
                    )
                }
            }
            AnimatedVisibility(
                visible = isEditBottomSheetVisible, enter = fadeIn(
                    initialAlpha = 0.4f
                ), exit = fadeOut(
                    animationSpec = tween(durationMillis = 250)
                )
            ) {
                EditInvoiceHeaderView(posState = posState, invoiceIndex = itemIndexToEdit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                        .background(
                            color = SettingsModel.backgroundColor
                        ), onSave = { invHeader, itemModel ->
                        invoicesState[itemIndexToEdit] = itemModel
                        posState.invoices = invoicesState
                        posState.invoiceHeader = invHeader
                        invoiceHeaderState.value = posState.refreshValues()
                        isEditBottomSheetVisible = false
                    }, onClose = {
                        isEditBottomSheetVisible = false
                    })
            }

            AnimatedVisibility(
                isAddItemBottomSheetVisible, enter = fadeIn(
                    initialAlpha = 0.4f
                ), exit = fadeOut(
                    animationSpec = tween(durationMillis = 250)
                )
            ) {
                AddInvoiceItemView(
                    categories = posState.families, items = posState.items,
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
                    posState.invoices = invoicesState
                    invoiceHeaderState.value = posState.refreshValues()
                    isAddItemBottomSheetVisible = false
                }
            }
            AnimatedVisibility(
                visible = isPayBottomSheetVisible, enter = fadeIn(
                    initialAlpha = 0.4f
                ), exit = fadeOut(
                    animationSpec = tween(durationMillis = 250)
                )
            ) {
                InvoiceCashView(
                    invoiceHeader = invoiceHeaderState.value,
                    currency = posState.currency,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                        .background(
                            color = SettingsModel.backgroundColor
                        ),
                    onSave = { change ->
                        posState.invoiceHeader.invoiceHeadChange = change
                        var cashName = posState.invoiceHeader.invoiceHeadCashName ?: ""
                        cashName = (posState.selectedThirdParty.thirdPartyName ?: "") + " ${cashName}"
                        posState.invoiceHeader.invoiceHeadCashName = cashName.ifEmpty { null }
                        invoiceHeaderState.value = posState.invoiceHeader
                        viewModel.saveInvoiceHeader(
                            posState.invoiceHeader, posState.invoices
                        )
                    },
                    onFinish = { change ->
                        posState.invoiceHeader.invoiceHeadChange = change
                        var cashName = posState.invoiceHeader.invoiceHeadCashName ?: ""
                        cashName = (posState.selectedThirdParty.thirdPartyName ?: "") + " ${cashName}"
                        posState.invoiceHeader.invoiceHeadCashName = cashName.ifEmpty { null }
                        invoiceHeaderState.value = posState.invoiceHeader
                        viewModel.saveInvoiceHeader(
                            posState.invoiceHeader, posState.invoices
                        )
                    },
                )
            }
        }
    }
}
