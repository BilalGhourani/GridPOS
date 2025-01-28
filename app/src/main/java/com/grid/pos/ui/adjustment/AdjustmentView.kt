package com.grid.pos.ui.adjustment

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.grid.pos.R
import com.grid.pos.SharedViewModel
import com.grid.pos.data.item.Item
import com.grid.pos.interfaces.OnBarcodeResult
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.EditableDateInputField
import com.grid.pos.ui.common.SearchableDropdownMenuEx
import com.grid.pos.ui.common.UIImageButton
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustmentView(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    sharedViewModel: SharedViewModel,
    viewModel: AdjustmentViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val keyboardController = LocalSoftwareKeyboardController.current


    var fromDateState by remember {
        mutableStateOf(
            DateHelper.getDateInFormat(
                DateHelper.editDate(Date(), 0, 0, 0),
                viewModel.dateFormat
            )
        )
    }
    var toDateState by remember {
        mutableStateOf(
            DateHelper.getDateInFormat(
                DateHelper.editDate(Date(), 23, 59, 59),
                viewModel.dateFormat
            )
        )
    }

    var barcodeSearchState by remember { mutableStateOf("") }
    var itemState by remember { mutableStateOf("") }
    var itemCostState by remember { mutableStateOf("") }
    var isPopupVisible by remember { mutableStateOf(false) }
    var collapseItemListState by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()


    LaunchedEffect(
        state.warning
    ) {
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
                        "Settings" -> sharedViewModel.openAppStorageSettings()
                    }
                }
            }
        }
    }

    LaunchedEffect(state.clear) {
        if (state.clear) {
            itemState = ""
            itemCostState = ""
            fromDateState = DateHelper.getDateInFormat(
                DateHelper.editDate(Date(), 0, 0, 0),
                viewModel.dateFormat
            )
            toDateState = DateHelper.getDateInFormat(
                DateHelper.editDate(Date(), 23, 59, 59),
                viewModel.dateFormat
            )
            isPopupVisible = false
            viewModel.resetState()
            keyboardController?.hide()
        }
    }

    LaunchedEffect(state.isLoading) {
        sharedViewModel.showLoading(
            state.isLoading,
            -1
        )
    }
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    fun handleBack() {
        if (state.isLoading) {
            isPopupVisible = true
        } else if (isImeVisible) {
            keyboardController?.hide()
        } else {
            viewModel.closeConnectionIfNeeded()
            viewModel.viewModelScope.cancel()
            navController?.navigateUp()
        }
    }

    LaunchedEffect(isPopupVisible) {
        sharedViewModel.showPopup(isPopupVisible,
            if (!isPopupVisible) null else PopupModel().apply {
                onDismissRequest = {
                    isPopupVisible = false
                }
                onConfirmation = {
                    state.isLoading = false
                    isPopupVisible = false
                    handleBack()
                }
                dialogText = "Are you sure you want to close?"
                positiveBtnText = "Cancel"
                negativeBtnText = "Close"
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
                    TopAppBar(colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = SettingsModel.topBarColor),
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
                                text = "Reports",
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
                        .fillMaxSize()
                        .padding(top = 90.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    UIImageButton(
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(100.dp)
                            .padding(10.dp),
                        icon = R.drawable.adjust_qty_cost,
                        text = "Adjust Remaining Quantity",
                        iconSize = 60.dp,
                        isVertical = false
                    ) {
                        if (state.selectedItem == null) {
                            viewModel.showError("select an Item at first!")
                            return@UIImageButton
                        }
                        viewModel.adjustRemainingQuantities(
                            state.selectedItem
                        )
                    }

                    UITextField(
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        defaultValue = itemCostState,
                        label = "Item Cost",
                        placeHolder = "Enter Cost",
                        keyboardType = KeyboardType.Decimal
                    ) { cost ->
                        itemCostState = Utils.getDoubleValue(
                            cost,
                            itemCostState
                        )
                    }

                    EditableDateInputField(
                        modifier = Modifier.padding(10.dp),
                        date = fromDateState,
                        dateTimeFormat = viewModel.dateFormat,
                        label = "From"
                    ) { dateStr ->
                        val date = DateHelper.getDateFromString(dateStr, viewModel.dateFormat)
                        if (date.after(Date())) {
                            sharedViewModel.showPopup(
                                true, PopupModel(
                                    dialogText = "From date should be today or before, please select again",
                                    negativeBtnText = null
                                )
                            )
                        } else {
                            fromDateState = dateStr
                        }
                    }

                    EditableDateInputField(
                        modifier = Modifier.padding(10.dp),
                        date = toDateState,
                        dateTimeFormat = viewModel.dateFormat,
                        label = "To"
                    ) { dateStr ->
                        toDateState = dateStr
                    }

                    UIImageButton(
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(100.dp)
                            .padding(10.dp),
                        icon = R.drawable.adjust_qty_cost,
                        text = "Update Item Cost",
                        iconSize = 60.dp,
                        isVertical = false
                    ) {
                        if (state.selectedItem == null) {
                            viewModel.showError("select an Item at first!")
                            return@UIImageButton
                        }
                        val from = DateHelper.getDateFromString(fromDateState, viewModel.dateFormat)
                        val to = DateHelper.getDateFromString(toDateState, viewModel.dateFormat)
                        viewModel.updateItemCost(
                            state.selectedItem!!,
                            itemCostState,
                            from,
                            to
                        )
                    }
                }

                SearchableDropdownMenuEx(items = state.items.toMutableList(),
                    modifier = Modifier.padding(
                        top = 15.dp,
                        start = 10.dp,
                        end = 10.dp
                    ),
                    label = "Select Item",
                    selectedId = itemState,
                    onLoadItems = { viewModel.fetchItems() },
                    leadingIcon = { mod ->
                        if (itemState.isNotEmpty()) {
                            Icon(
                                Icons.Default.RemoveCircleOutline,
                                contentDescription = "remove item",
                                tint = Color.Black,
                                modifier = mod
                            )
                        }
                    },
                    onLeadingIconClick = {
                        itemState = ""
                        state.selectedItem = null
                    },
                    collapseOnInit = collapseItemListState,
                    searchEnteredText = barcodeSearchState,
                    searchLeadingIcon = {
                        IconButton(onClick = {
                            collapseItemListState = false
                            sharedViewModel.launchBarcodeScanner(true,
                                null,
                                object : OnBarcodeResult {
                                    override fun OnBarcodeResult(barcodesList: List<Any>) {
                                        if (barcodesList.isNotEmpty()) {
                                            val resp = barcodesList[0]
                                            if (resp is String) {
                                                scope.launch(Dispatchers.Default) {
                                                    val item = state.items.firstOrNull { iterator ->
                                                        iterator.itemBarcode.equals(
                                                            resp,
                                                            ignoreCase = true
                                                        )
                                                    }
                                                    withContext(Dispatchers.Main) {
                                                        if (item != null) {
                                                            collapseItemListState = true
                                                            itemState = item.itemId
                                                            state.selectedItem = item
                                                        } else {
                                                            barcodeSearchState = resp
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                onPermissionDenied = {
                                    viewModel.showError(
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
                    }) { item ->
                    item as Item
                    itemState = item.itemId
                    state.selectedItem = item
                }
            }
        }
    }
}