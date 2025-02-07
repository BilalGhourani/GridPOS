package com.grid.pos.ui.item

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.grid.pos.R
import com.grid.pos.SharedViewModel
import com.grid.pos.data.family.Family
import com.grid.pos.data.item.Item
import com.grid.pos.data.posPrinter.PosPrinter
import com.grid.pos.interfaces.OnBarcodeResult
import com.grid.pos.interfaces.OnGalleryResult
import com.grid.pos.model.CurrencyModel
import com.grid.pos.model.ItemGroupModel
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.ToastModel
import com.grid.pos.ui.common.ColorPickerPopup
import com.grid.pos.ui.common.SearchableDropdownMenuEx
import com.grid.pos.ui.common.UIImageButton
import com.grid.pos.ui.common.UISwitch
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.pos.POSUtils
import com.grid.pos.ui.settings.ColorPickerType
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Extension.toHexCode
import com.grid.pos.utils.FileUtils
import com.grid.pos.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun ManageItemsView(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    sharedViewModel: SharedViewModel,
    viewModel: ManageItemsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val unitPriceFocusRequester = remember { FocusRequester() }
    val taxFocusRequester = remember { FocusRequester() }
    val tax1FocusRequester = remember { FocusRequester() }
    val tax2FocusRequester = remember { FocusRequester() }
    val barcodeFocusRequester = remember { FocusRequester() }
    val openCostFocusRequester = remember { FocusRequester() }
    val openQtyFocusRequester = remember { FocusRequester() }
    val btnColorFocusRequester = remember { FocusRequester() }
    val btnTextColorFocusRequester = remember { FocusRequester() }
    val imageFocusRequester = remember { FocusRequester() }

    var collapseItemListState by remember { mutableStateOf(false) }
    var barcodeSearchState by remember { mutableStateOf("") }
    var colorPickerType by remember { mutableStateOf(ColorPickerType.BUTTON_COLOR) }
    var isColorPickerShown by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

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

    LaunchedEffect(state.isLoading) {
        sharedViewModel.showLoading(state.isLoading)
    }

    fun fillItemInputs(item: Item) {
        sharedViewModel.showLoading(true)
        viewModel.selectionPrerequisite {
            viewModel.currentITem = item.copy(
                itemOpenQtyStr = item.itemOpenQty.toString(),
                itemOpenCostStr = item.itemOpenCost.toString(),
                itemUnitPriceStr = item.itemUnitPrice.toString(),
                itemTaxStr = item.itemTax.toString(),
                itemTax1Str = item.itemTax1.toString(),
                itemTax2Str = item.itemTax2.toString(),
            )
            viewModel.updateItem(viewModel.currentITem.copy())
            sharedViewModel.showLoading(false)
        }
    }

    fun saveItem() {
        viewModel.oldImage?.let { old ->
            FileUtils.deleteFile(
                context,
                old
            )
        }
        viewModel.save()
    }


    var saveAndBack by remember { mutableStateOf(false) }
    fun handleBack() {
        if (state.isLoading) {
            return
        }
        if (viewModel.isAnyChangeDone()) {
            sharedViewModel.showPopup(true,
                PopupModel().apply {
                    onDismissRequest = {
                        viewModel.resetState()
                        handleBack()
                    }
                    onConfirmation = {
                        saveAndBack = true
                        saveItem()
                    }
                    dialogText = "Do you want to save your changes"
                    positiveBtnText = "Save"
                    negativeBtnText = "Close"
                })
            return
        }
        navController?.navigateUp()
    }

    fun clearAndBack() {
        viewModel.resetState()
        if (saveAndBack) {
            handleBack()
        }
    }
    LaunchedEffect(state.clear) {
        if (state.clear) {
            clearAndBack()
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
                                text = "Manage Items",
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
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(top = 90.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    //name
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = state.item.itemName ?: "",
                        label = "Name",
                        placeHolder = "Enter Name",
                        onAction = { unitPriceFocusRequester.requestFocus() }) { name ->
                        viewModel.updateItem(
                            state.item.copy(
                                itemName = name
                            )
                        )
                    }

                    //unitPrice
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = state.item.itemUnitPriceStr ?: "",
                        label = "Unit Price",
                        focusRequester = unitPriceFocusRequester,
                        keyboardType = KeyboardType.Decimal,
                        placeHolder = "Enter Unit Price",
                        onAction = {
                            if (SettingsModel.showTax) {
                                taxFocusRequester.requestFocus()
                            } else if (SettingsModel.showTax1) {
                                tax1FocusRequester.requestFocus()
                            } else if (SettingsModel.showTax2) {
                                tax2FocusRequester.requestFocus()
                            } else {
                                barcodeFocusRequester.requestFocus()
                            }
                        }) { unitPrice ->
                        viewModel.updateItem(
                            state.item.copy(
                                itemUnitPrice = unitPrice.toDoubleOrNull()
                                    ?: state.item.itemUnitPrice,
                                itemUnitPriceStr = Utils.getDoubleValue(
                                    unitPrice,
                                    state.item.itemUnitPriceStr ?: ""
                                )
                            )
                        )
                    }

                    if (SettingsModel.showTax) {
                        //tax
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue = state.item.itemTaxStr ?: "",
                            label = "Tax",
                            focusRequester = taxFocusRequester,
                            keyboardType = KeyboardType.Decimal,
                            placeHolder = "Enter Tax",
                            onAction = {
                                if (SettingsModel.showTax1) {
                                    tax1FocusRequester.requestFocus()
                                } else if (SettingsModel.showTax2) {
                                    tax2FocusRequester.requestFocus()
                                } else {
                                    barcodeFocusRequester.requestFocus()
                                }
                            }) { tax ->
                            viewModel.updateItem(
                                state.item.copy(
                                    itemTax = tax.toDoubleOrNull() ?: state.item.itemTax,
                                    itemTaxStr = Utils.getDoubleValue(
                                        tax,
                                        state.item.itemTaxStr ?: ""
                                    )
                                )
                            )
                        }
                    }
                    if (SettingsModel.showTax1) {
                        //tax1
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue = state.item.itemTax1Str ?: "",
                            label = "Tax1",
                            focusRequester = tax1FocusRequester,
                            keyboardType = KeyboardType.Decimal,
                            placeHolder = "Enter Tax1",
                            onAction = {
                                if (SettingsModel.showTax2) {
                                    tax2FocusRequester.requestFocus()
                                } else {
                                    barcodeFocusRequester.requestFocus()
                                }
                            }) { tax1 ->
                            viewModel.updateItem(
                                state.item.copy(
                                    itemTax1 = tax1.toDoubleOrNull() ?: state.item.itemTax1,
                                    itemTax1Str = Utils.getDoubleValue(
                                        tax1,
                                        state.item.itemTax1Str ?: ""
                                    )
                                )
                            )
                        }
                    }
                    if (SettingsModel.showTax2) {
                        //tax2
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue = state.item.itemTax2Str ?: "",
                            label = "Tax2",
                            focusRequester = tax2FocusRequester,
                            keyboardType = KeyboardType.Decimal,
                            placeHolder = "Enter Tax2",
                            onAction = { barcodeFocusRequester.requestFocus() }) { tax2 ->
                            viewModel.updateItem(
                                state.item.copy(
                                    itemTax2 = tax2.toDoubleOrNull() ?: state.item.itemTax2,
                                    itemTax2Str = Utils.getDoubleValue(
                                        tax2,
                                        state.item.itemTax2Str ?: ""
                                    )
                                )
                            )
                        }
                    }
                    //barcode
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = state.item.itemBarcode ?: "",
                        label = "Barcode",
                        placeHolder = "Enter Barcode",
                        focusRequester = barcodeFocusRequester,
                        onAction = {
                            if (viewModel.shouldDisableCostAndQty()) {
                                btnColorFocusRequester.requestFocus()
                            } else {
                                openCostFocusRequester.requestFocus()
                            }
                        },
                        leadingIcon = {
                            IconButton(onClick = {
                                sharedViewModel.showLoading(true)
                                scope.launch(Dispatchers.IO) {
                                    val barcode = if (state.item.itemBarcode.isNullOrEmpty()) {
                                        viewModel.generateBarcode()
                                    } else {
                                        state.item.itemBarcode
                                    }
                                    if (!barcode.isNullOrEmpty()) {
                                        withContext(Dispatchers.Main) {
                                            viewModel.updateItem(
                                                state.item.copy(
                                                    itemBarcode = barcode
                                                )
                                            )
                                        }
                                        val reportResult = viewModel.prepareItemBarcodeReport(
                                            context,
                                            state.item
                                        )
//                                        PrinterUtils.printReport(
//                                            context,
//                                            reportResult
//                                        )
                                        withContext(Dispatchers.Main) {
                                            sharedViewModel.showLoading(false)
                                            sharedViewModel.reportsToPrint.clear()
                                            sharedViewModel.reportsToPrint.add(reportResult)
                                            navController?.navigate("UIWebView")
                                        }
                                    }
                                }

                            }) {
                                Icon(
                                    painterResource(R.drawable.generate_barcode),
                                    contentDescription = "Generate barcode",
                                    tint = SettingsModel.buttonColor
                                )
                            }
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                sharedViewModel.launchBarcodeScanner(true,
                                    ArrayList(state.items),
                                    object : OnBarcodeResult {
                                        override fun OnBarcodeResult(barcodesList: List<Any>) {
                                            if (barcodesList.isNotEmpty()) {
                                                val barcode = barcodesList[0]
                                                if (barcode is String) {
                                                    viewModel.updateItem(
                                                        state.item.copy(
                                                            itemBarcode = barcode
                                                        )
                                                    )
                                                    if (viewModel.shouldDisableCostAndQty()) {
                                                        btnColorFocusRequester.requestFocus()
                                                    } else {
                                                        openCostFocusRequester.requestFocus()
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
                        }) { barcode ->
                        viewModel.updateItem(
                            state.item.copy(
                                itemBarcode = barcode
                            )
                        )
                    }

                    //open cost
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = state.item.itemOpenCostStr ?: "",
                        keyboardType = KeyboardType.Decimal,
                        label = "Open cost",
                        placeHolder = "Enter Open cost",
                        enabled = !viewModel.shouldDisableCostAndQty(),
                        focusRequester = openCostFocusRequester,
                        onAction = { openQtyFocusRequester.requestFocus() }) { openCost ->
                        viewModel.updateItem(
                            state.item.copy(
                                itemOpenCost = openCost.toDoubleOrNull() ?: state.item.itemOpenCost,
                                itemOpenCostStr = Utils.getDoubleValue(
                                    openCost,
                                    state.item.itemOpenCostStr ?: ""
                                )
                            )
                        )
                    }

                    //open quantity
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = state.item.itemOpenQtyStr ?: "",
                        label = "Open Qty",
                        enabled = !viewModel.shouldDisableCostAndQty(),
                        keyboardType = KeyboardType.Decimal,
                        placeHolder = "Enter Open Qty",
                        focusRequester = openQtyFocusRequester,
                        onAction = { btnColorFocusRequester.requestFocus() }) { openQty ->
                        viewModel.updateItem(
                            state.item.copy(
                                itemOpenQty = openQty.toDoubleOrNull() ?: state.item.itemOpenQty,
                                itemOpenQtyStr = Utils.getDoubleValue(
                                    openQty,
                                    state.item.itemOpenQtyStr ?: ""
                                )
                            )
                        )
                    }

                    //Rem quantity
                    UITextField(
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        defaultValue = state.item.itemRemQty.toString(),
                        enabled = false,
                        label = "Remaining Qty",
                        keyboardType = KeyboardType.Decimal,
                        placeHolder = "Remaining Qty"
                    ) { remQty ->
                        viewModel.updateItem(
                            state.item.copy(
                                itemRemQty = remQty.toDoubleOrNull() ?: state.item.itemRemQty
                            )
                        )
                    }
                    if (state.isConnectingToSQLServer) {
                        SearchableDropdownMenuEx(
                            items = state.groups.toMutableList(),
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 5.dp
                            ),
                            label = "Select Group",
                            selectedId = state.item.itemGroup
                        ) { group ->
                            group as ItemGroupModel
                            viewModel.updateItem(
                                state.item.copy(
                                    itemGroup = group.getId()
                                )
                            )
                        }
                    }

                    SearchableDropdownMenuEx(
                        items = state.currencies.toMutableList(),
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        label = "Select Currency",
                        selectedId = state.item.itemCurrencyId
                    ) { currModel ->
                        currModel as CurrencyModel
                        viewModel.updateItem(
                            state.item.copy(
                                itemCurrencyId = currModel.getId(),
                                itemCurrencyCode = currModel.currencyCode
                            )
                        )
                    }

                    SearchableDropdownMenuEx(items = state.families.toMutableList(),
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        label = "Select Family",
                        selectedId = state.item.itemFaId,
                        onLoadItems = {
                            viewModel.fetchFamilies()
                        },
                        leadingIcon = { modifier ->
                            if (!state.item.itemFaId.isNullOrEmpty()) {
                                Icon(
                                    Icons.Default.RemoveCircleOutline,
                                    contentDescription = "remove family",
                                    tint = Color.Black,
                                    modifier = modifier
                                )
                            }
                        },
                        onLeadingIconClick = {
                            viewModel.updateItem(
                                state.item.copy(
                                    itemFaId = null
                                )
                            )
                        }) { family ->
                        family as Family
                        viewModel.updateItem(
                            state.item.copy(
                                itemFaId = family.familyId
                            )
                        )
                    }

                    //Button color
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = state.item.itemBtnColor ?: "",
                        label = "Button color",
                        placeHolder = "Enter Button color",
                        focusRequester = btnColorFocusRequester,
                        onAction = { btnTextColorFocusRequester.requestFocus() },
                        trailingIcon = {
                            IconButton(onClick = {
                                colorPickerType = ColorPickerType.BUTTON_COLOR
                                isColorPickerShown = true
                            }) {
                                Icon(
                                    Icons.Default.ColorLens,
                                    contentDescription = "color",
                                    tint = SettingsModel.buttonColor
                                )
                            }
                        }) { btnColor ->
                        viewModel.updateItem(
                            state.item.copy(
                                itemBtnColor = btnColor
                            )
                        )
                    }

                    //Button text color
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = state.item.itemBtnTextColor ?: "",
                        label = "Button Text color",
                        placeHolder = "Enter Button Text color",
                        focusRequester = btnTextColorFocusRequester,
                        onAction = { imageFocusRequester.requestFocus() },
                        trailingIcon = {
                            IconButton(onClick = {
                                colorPickerType = ColorPickerType.BUTTON_TEXT_COLOR
                                isColorPickerShown = true
                            }) {
                                Icon(
                                    Icons.Default.ColorLens,
                                    contentDescription = "color",
                                    tint = SettingsModel.buttonColor
                                )
                            }
                        }) { btnTextColor ->
                        viewModel.updateItem(
                            state.item.copy(
                                itemBtnTextColor = btnTextColor
                            )
                        )
                    }

                    SearchableDropdownMenuEx(items = state.printers.toMutableList(),
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        label = "Select Printer",
                        selectedId = state.item.itemPrinter,
                        onLoadItems = {
                            viewModel.fetchPrinters()
                        },
                        leadingIcon = { modifier ->
                            if (!state.item.itemPrinter.isNullOrEmpty()) {
                                Icon(
                                    Icons.Default.RemoveCircleOutline,
                                    contentDescription = "remove printer",
                                    tint = Color.Black,
                                    modifier = modifier
                                )
                            }
                        },
                        onLeadingIconClick = {
                            viewModel.updateItem(
                                state.item.copy(
                                    itemPrinter = null
                                )
                            )
                        }) { printer ->
                        printer as PosPrinter
                        viewModel.updateItem(
                            state.item.copy(
                                itemPrinter = printer.posPrinterId
                            )
                        )
                    }

                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = state.item.itemImage ?: "",
                        label = "Image",
                        placeHolder = "Image",
                        focusRequester = imageFocusRequester,
                        imeAction = ImeAction.Done,
                        onAction = { keyboardController?.hide() },
                        trailingIcon = {
                            IconButton(onClick = {
                                sharedViewModel.launchGalleryPicker(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
                                    object : OnGalleryResult {
                                        override fun onGalleryResult(uris: List<Uri>) {
                                            if (uris.isNotEmpty()) {
                                                sharedViewModel.copyToInternalStorage(
                                                    context = context,
                                                    uri = uris[0],
                                                    parent = "item",
                                                    fileName = ((state.item.itemName
                                                        ?: "item").trim()
                                                        .replace(
                                                            " ",
                                                            "_"
                                                        ))
                                                ) { internalPath ->
                                                    viewModel.oldImage = state.item.itemImage
                                                    viewModel.updateItem(
                                                        state.item.copy(
                                                            itemImage = internalPath
                                                        )
                                                    )
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
                                    Icons.Default.Image,
                                    contentDescription = "Image",
                                    tint = SettingsModel.buttonColor
                                )
                            }
                        }) { img ->
                        viewModel.updateItem(
                            state.item.copy(
                                itemImage = img
                            )
                        )
                    }

                    UISwitch(
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        checked = state.item.itemPos,
                        text = "Item POS",
                    ) { isItemPOS ->
                        viewModel.updateItem(
                            state.item.copy(
                                itemPos = isItemPOS
                            )
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(
                                horizontal = 10.dp,
                                vertical = 5.dp
                            ),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        UIImageButton(
                            modifier = Modifier
                                .weight(.33f)
                                .padding(3.dp),
                            icon = R.drawable.save,
                            text = "Save"
                        ) {
                            saveItem()
                        }

                        UIImageButton(
                            modifier = Modifier
                                .weight(.33f)
                                .padding(3.dp),
                            icon = R.drawable.delete,
                            text = "Delete"
                        ) {
                            viewModel.oldImage?.let { old ->
                                FileUtils.deleteFile(
                                    context,
                                    old
                                )
                            }
                            if (!state.item.itemImage.isNullOrEmpty()) {
                                FileUtils.deleteFile(
                                    context,
                                    state.item.itemImage!!
                                )
                            }
                            viewModel.delete()
                        }

                        UIImageButton(
                            modifier = Modifier
                                .weight(.33f)
                                .padding(3.dp),
                            icon = R.drawable.go_back,
                            text = "Close"
                        ) {
                            handleBack()
                        }
                    }
                }

                SearchableDropdownMenuEx(items = state.items.toMutableList(),
                    modifier = Modifier.padding(
                        top = 15.dp,
                        start = 10.dp,
                        end = 10.dp
                    ),
                    label = "Select Item",
                    selectedId = state.item.itemId,
                    onLoadItems = { viewModel.fetchItems() },
                    leadingIcon = { modifier ->
                        if (state.item.itemId.isNotEmpty()) {
                            Icon(
                                Icons.Default.RemoveCircleOutline,
                                contentDescription = "remove family",
                                tint = Color.Black,
                                modifier = modifier
                            )
                        }
                    },
                    onLeadingIconClick = {
                        viewModel.resetState()
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
                                                    val barcodeItem =
                                                        state.items.firstOrNull { iterator ->
                                                            iterator.itemBarcode.equals(
                                                                resp,
                                                                ignoreCase = true
                                                            )
                                                        }
                                                    withContext(Dispatchers.Main) {
                                                        if (barcodeItem != null) {
                                                            collapseItemListState = true
                                                            fillItemInputs(barcodeItem)
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
                    }) { item ->
                    item as Item
                    fillItemInputs(item)
                }
            }
        }

        AnimatedVisibility(
            visible = isColorPickerShown,
            enter = fadeIn(
                initialAlpha = 0.4f
            ),
            exit = fadeOut(
                animationSpec = tween(durationMillis = 250)
            )
        ) {
            Dialog(
                onDismissRequest = { isColorPickerShown = false },
            ) {
                ColorPickerPopup(defaultColor = when (colorPickerType) {
                    ColorPickerType.BUTTON_COLOR -> Color.Red
                    ColorPickerType.BUTTON_TEXT_COLOR -> Color.White
                    else -> Color.Blue
                },
                    onDismiss = { isColorPickerShown = false },
                    onSubmit = {
                        when (colorPickerType) {
                            ColorPickerType.BUTTON_COLOR -> {
                                viewModel.updateItem(
                                    state.item.copy(
                                        itemBtnColor = it.toHexCode()
                                    )
                                )
                            }

                            ColorPickerType.BUTTON_TEXT_COLOR -> {
                                viewModel.updateItem(
                                    state.item.copy(
                                        itemBtnTextColor = it.toHexCode()
                                    )
                                )
                            }

                            else -> {}
                        }
                        isColorPickerShown = false
                    })

            }
        }
    }
}