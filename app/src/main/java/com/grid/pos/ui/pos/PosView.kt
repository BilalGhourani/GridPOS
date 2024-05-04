package com.grid.pos.ui.pos

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
import com.grid.pos.ui.theme.White
import com.grid.pos.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosView(
    navController: NavController? = null,
    activityViewModel: ActivityScopedViewModel = ActivityScopedViewModel(),
    modifier: Modifier = Modifier,
    viewModel: POSViewModel = hiltViewModel()
) {
    val posState: POSState by viewModel.posState.collectAsState(activityViewModel.posState)
    var invoicesState = remember { posState.invoices }
    var isEditBottomSheetVisible by remember { mutableStateOf(false) }
    var isAddItemBottomSheetVisible by remember { mutableStateOf(false) }
    var isPayBottomSheetVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var orientation by remember { mutableStateOf(Configuration.ORIENTATION_PORTRAIT) }

    val configuration = LocalConfiguration.current
    val isTablet = Utils.isTablet(LocalConfiguration.current)
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(configuration) {
        snapshotFlow { configuration.orientation }
            .collect {
                orientation = it
                isEditBottomSheetVisible = false
                isAddItemBottomSheetVisible = false
                isPayBottomSheetVisible = false
            }
    }

    LaunchedEffect(posState.warning) {
        if (!posState.warning.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.Main).launch {
                snackbarHostState.showSnackbar(
                    message = posState.warning!!,
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }
    GridPOSTheme {
        Scaffold(
            containerColor=SettingsModel.backgroundColor,
            topBar = {
                Surface(shadowElevation = 3.dp, color = SettingsModel.backgroundColor) {
                    TopAppBar(
                        colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = SettingsModel.topBarColor),
                        navigationIcon = {
                            IconButton(onClick = { navController?.popBackStack() }) {
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
                        },
                       /* actions = {
                            IconButton(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(horizontal = 10.dp),
                                onClick = { navController?.popBackStack() }
                            ) {
                                Text(text = "Back", color = SettingsModel.textColor)
                            }
                        }*/)
                }
            }
        ) {
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
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                ) {
                    InvoiceHeaderDetails(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp),
                        onEdit = { isEditBottomSheetVisible = true },
                        onPay = { isPayBottomSheetVisible = true }
                    )

                    // Border stroke configuration
                    val borderStroke = BorderStroke(1.dp, Color.Black)

                    InvoiceBodyDetails(
                        invoices = invoicesState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Utils.getListHeight(invoicesState.size, 50))
                            .border(borderStroke),
                        isLandscape = isTablet || isLandscape,
                        onDismiss = { index ->
                            val invoices = invoicesState
                            invoices.removeAt(index)
                            posState.invoices = invoices
                            posState.refreshValues()
                            invoicesState = invoices
                            isAddItemBottomSheetVisible = false
                        }
                    )

                    InvoiceFooterView(
                        invoices = invoicesState,
                        invoiceHeader = posState.invoiceHeader,
                        items = posState.items,
                        thirdParties = posState.thirdParties,
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(250.dp),
                        onItemSelected = {
                            val invoiceItemModel = InvoiceItemModel()
                            invoiceItemModel.setItem(it)
                            val invoices = invoicesState
                            invoices.add(invoiceItemModel)
                            posState.invoices = invoices
                            posState.refreshValues()
                            invoicesState = invoices
                            isAddItemBottomSheetVisible = false
                        },
                        onThirdPartySelected = {
                            posState.invoiceHeader.invoiceHeadThirdPartyName = it.thirdPartyId
                        },
                    )
                }
            }
        }
    }
    if (isEditBottomSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { isEditBottomSheetVisible = false },
            sheetState = bottomSheetState,
            containerColor = SettingsModel.backgroundColor,
            contentColor = SettingsModel.backgroundColor,
            shape = RectangleShape,
            dragHandle = null,
            scrimColor = Color.Black.copy(alpha = .5f),
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            EditInvoiceHeaderView(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f),
                onAddCustomer = {
                    isEditBottomSheetVisible = false
                    navController?.navigate("ManageThirdPartiesView")
                },
                onAddItem = {
                    isAddItemBottomSheetVisible = true
                },
                onClose = {
                    isEditBottomSheetVisible = false
                }
            )
        }
    }

    if (isAddItemBottomSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { isAddItemBottomSheetVisible = false },
            sheetState = bottomSheetState,
            containerColor = SettingsModel.backgroundColor,
            contentColor = SettingsModel.backgroundColor,
            shape = RectangleShape,
            dragHandle = null,
            scrimColor = Color.Black.copy(alpha = .5f),
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            AddInvoiceItemView(
                categories = posState.families,
                items = posState.items,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
            ) { itemList ->
                val invoices = invoicesState
                itemList.forEach { item ->
                    val invoiceItemModel = InvoiceItemModel()
                    invoiceItemModel.setItem(item)
                    invoices.add(invoiceItemModel)
                }
                posState.invoices = invoices
                posState.refreshValues()
                invoicesState = invoices
                isAddItemBottomSheetVisible = false
                isEditBottomSheetVisible = false
            }
        }
    }

    if (isPayBottomSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { isPayBottomSheetVisible = false },
            sheetState = bottomSheetState,
            containerColor = SettingsModel.backgroundColor,
            contentColor = SettingsModel.backgroundColor,
            shape = RectangleShape,
            dragHandle = null,
            scrimColor = Color.Black.copy(alpha = .5f),
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            InvoiceCashView(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(if (isLandscape) 0.9f else 0.5f),
                onSave = {
                    // viewModel.saveInvoiceHeader(posState.invoiceHeader, posState.invoices)
                    activityViewModel.posState = posState
                    navController?.navigate("UIWebView")
                },
                onFinish = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PosViewPreview() {
    GridPOSTheme {
        PosView()
    }
}