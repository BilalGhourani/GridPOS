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
import com.grid.pos.ActivityScopedViewModel
import com.grid.pos.R
import com.grid.pos.data.Family.Family
import com.grid.pos.data.Item.Item
import com.grid.pos.data.PosPrinter.PosPrinter
import com.grid.pos.interfaces.OnBarcodeResult
import com.grid.pos.interfaces.OnGalleryResult
import com.grid.pos.model.CurrencyModel
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.ColorPickerPopup
import com.grid.pos.ui.common.SearchableDropdownMenuEx
import com.grid.pos.ui.common.UIImageButton
import com.grid.pos.ui.common.UISwitch
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.settings.ColorPickerType
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Extension.toHexCode
import com.grid.pos.utils.FileUtils
import com.grid.pos.utils.Utils
import kotlinx.coroutines.CoroutineScope
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
        activityScopedViewModel: ActivityScopedViewModel,
        viewModel: ManageItemsViewModel = hiltViewModel()
) {
    val state by viewModel.manageItemsState.collectAsStateWithLifecycle()

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

    var nameState by remember { mutableStateOf("") }
    var unitPriceState by remember { mutableStateOf("") }
    var taxState by remember { mutableStateOf(SettingsModel.currentCompany?.companyTax.toString()) }
    var tax1State by remember {
        mutableStateOf(
            SettingsModel.currentCompany?.companyTax1.toString()
        )
    }
    var tax2State by remember {
        mutableStateOf(
            SettingsModel.currentCompany?.companyTax2.toString()
        )
    }
    var barcodeState by remember { mutableStateOf("") }
    var openCostState by remember { mutableStateOf("") }
    var openQtyState by remember { mutableStateOf("") }
    var remQtyState by remember { mutableStateOf("") }
    var itemCurrState by remember { mutableStateOf(SettingsModel.currentCurrency?.currencyCode1 ?: "") }
    var familyIdState by remember { mutableStateOf("") }
    var btnColorState by remember { mutableStateOf("") }
    var btnTextColorState by remember { mutableStateOf("") }
    var printerState by remember { mutableStateOf("") }
    var imageState by remember { mutableStateOf("") }
    var itemPOSState by remember { mutableStateOf(false) }

    var oldImage: String? = null

    var colorPickerType by remember { mutableStateOf(ColorPickerType.BUTTON_COLOR) }
    var isColorPickerShown by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    /* LaunchedEffect(
         activityScopedViewModel.items,
         activityScopedViewModel.families
     ) {
         viewModel.fillCachedItems(
             activityScopedViewModel.items,
             activityScopedViewModel.families
         )
     }*/

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
                        "Settings" -> activityScopedViewModel.openAppStorageSettings()
                    }
                }
            }
        }
    }

    LaunchedEffect(state.isLoading) {
        activityScopedViewModel.showLoading(state.isLoading)
    }

    fun saveItem() {
        oldImage?.let { old ->
            FileUtils.deleteFile(
                context,
                old
            )
        }
        if (SettingsModel.showTax && state.selectedItem.itemTax == 0.0) {
            state.selectedItem.itemTax = taxState.toDoubleOrNull() ?: 0.0
        }
        if (SettingsModel.showTax1 && state.selectedItem.itemTax1 == 0.0) {
            state.selectedItem.itemTax1 = tax1State.toDoubleOrNull() ?: 0.0
        }
        if (SettingsModel.showTax2 && state.selectedItem.itemTax2 == 0.0) {
            state.selectedItem.itemTax2 = tax2State.toDoubleOrNull() ?: 0.0
        }
        state.selectedItem.itemUnitPrice = Utils.roundDoubleValue(
            state.selectedItem.itemUnitPrice,
            SettingsModel.currentCurrency?.currencyName1Dec
        )
        state.selectedItem.itemOpenCost = Utils.roundDoubleValue(
            state.selectedItem.itemOpenCost,
            SettingsModel.currentCurrency?.currencyName1Dec
        )
        state.selectedItem.itemPos = itemPOSState
        viewModel.saveItem(state.selectedItem)
    }

    fun clear() {
        viewModel.currentITem = Item()
        state.selectedItem = Item()
        nameState = ""
        unitPriceState = ""
        taxState = SettingsModel.currentCompany?.companyTax.toString()
        tax1State = SettingsModel.currentCompany?.companyTax1.toString()
        tax2State = SettingsModel.currentCompany?.companyTax2.toString()
        barcodeState = ""
        openCostState = ""
        openQtyState = ""
        remQtyState = ""
        itemCurrState = SettingsModel.currentCurrency?.currencyCode1 ?: ""
        familyIdState = ""
        btnColorState = ""
        btnTextColorState = ""
        printerState = ""
        imageState = ""
        itemPOSState = false
        state.clear = false
    }

    var saveAndBack by remember { mutableStateOf(false) }
    fun handleBack() {
        if (state.isLoading) {
            return
        }
        if (state.selectedItem.didChanged(
                viewModel.currentITem
            )
        ) {
            activityScopedViewModel.showPopup(true,
                PopupModel().apply {
                    onDismissRequest = {
                        clear()
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
        if (state.items.isNotEmpty()) {
            activityScopedViewModel.items = state.items
        }
        viewModel.closeConnectionIfNeeded()
        navController?.navigateUp()
    }

    fun clearAndBack() {
        clear()
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
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(top = 90.dp)
                        .verticalScroll(
                            rememberScrollState()
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    //name
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = nameState,
                        label = "Name",
                        placeHolder = "Enter Name",
                        onAction = { unitPriceFocusRequester.requestFocus() }) { name ->
                        nameState = name
                        state.selectedItem.itemName = name
                    }

                    //unitPrice
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = unitPriceState,
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
                        unitPriceState = Utils.getDoubleValue(
                            unitPrice,
                            unitPriceState
                        )
                        state.selectedItem.itemUnitPrice = unitPriceState.toDoubleOrNull() ?: 0.0
                    }

                    if (SettingsModel.showTax) {
                        //tax
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue = taxState,
                            label = "Tax",
                            maxLines = 3,
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
                            taxState = Utils.getDoubleValue(
                                tax,
                                taxState
                            )
                            state.selectedItem.itemTax = taxState.toDoubleOrNull() ?: 0.0
                        }
                    }
                    if (SettingsModel.showTax1) {
                        //tax1
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue = tax1State,
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
                            tax1State = Utils.getDoubleValue(
                                tax1,
                                tax1State
                            )
                            state.selectedItem.itemTax1 = tax1State.toDoubleOrNull() ?: 0.0
                        }
                    }
                    if (SettingsModel.showTax2) {
                        //tax2
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue = tax2State,
                            label = "Tax2",
                            focusRequester = tax2FocusRequester,
                            keyboardType = KeyboardType.Decimal,
                            placeHolder = "Enter Tax2",
                            onAction = { barcodeFocusRequester.requestFocus() }) { tax2 ->
                            tax2State = Utils.getDoubleValue(
                                tax2,
                                tax2State
                            )
                            state.selectedItem.itemTax2 = tax2State.toDoubleOrNull() ?: 0.0
                        }
                    }
                    //barcode
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = barcodeState,
                        label = "Barcode",
                        placeHolder = "Enter Barcode",
                        focusRequester = barcodeFocusRequester,
                        onAction = { openCostFocusRequester.requestFocus() },
                        trailingIcon = {
                            IconButton(onClick = {
                                activityScopedViewModel.launchBarcodeScanner(true,
                                    ArrayList(state.items),
                                    object : OnBarcodeResult {
                                        override fun OnBarcodeResult(barcodesList: List<String>) {
                                            if (barcodesList.isNotEmpty()) {
                                                barcodeState = barcodesList[0]
                                                state.selectedItem.itemBarcode = barcodeState
                                                openCostFocusRequester.requestFocus()
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
                        barcodeState = barcode
                        state.selectedItem.itemBarcode = barcode
                    }

                    //open cost
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = openCostState,
                        keyboardType = KeyboardType.Decimal,
                        label = "Open cost",
                        placeHolder = "Enter Open cost",
                        focusRequester = openCostFocusRequester,
                        onAction = { openQtyFocusRequester.requestFocus() }) { openCost ->
                        openCostState = Utils.getDoubleValue(
                            openCost,
                            openCostState
                        )
                        state.selectedItem.itemOpenCost = openCostState.toDoubleOrNull() ?: 0.0
                    }

                    //open quantity
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = openQtyState,
                        label = "Open Qty",
                        keyboardType = KeyboardType.Decimal,
                        placeHolder = "Enter Open Qty",
                        focusRequester = openQtyFocusRequester,
                        onAction = { btnColorFocusRequester.requestFocus() }) { openQty ->
                        openQtyState = Utils.getDoubleValue(
                            openQty,
                            openQtyState
                        )
                        state.selectedItem.itemOpenQty = openQtyState.toDoubleOrNull() ?: 0.0
                    }

                    //Rem quantity
                    UITextField(
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        defaultValue = remQtyState,
                        readOnly = true,
                        label = "Remaining Qty",
                        keyboardType = KeyboardType.Decimal,
                        placeHolder = "Remaining Qty"
                    ) { openQty ->
                        remQtyState = Utils.getDoubleValue(
                            openQty,
                            remQtyState
                        )
                        state.selectedItem.itemRemQty = remQtyState.toDoubleOrNull() ?: 0.0
                    }

                    if (state.currencies.isNotEmpty()) {
                        SearchableDropdownMenuEx(
                            items = state.currencies.toMutableList(),
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 5.dp
                            ),
                            onLoadItems = {
                                viewModel.fetchCurrencies()
                            },
                            label = "Select Currency",
                            selectedId = itemCurrState
                        ) { currModel ->
                            currModel as CurrencyModel
                            itemCurrState = currModel.getId()
                            state.selectedItem.itemCurrencyId = itemCurrState
                        }
                    }

                    SearchableDropdownMenuEx(items = state.families.toMutableList(),
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        label = "Select Family",
                        selectedId = familyIdState,
                        leadingIcon = { modifier ->
                            if (familyIdState.isNotEmpty()) {
                                Icon(
                                    Icons.Default.RemoveCircleOutline,
                                    contentDescription = "remove family",
                                    tint = Color.Black,
                                    modifier = modifier
                                )
                            }
                        },
                        onLeadingIconClick = {
                            familyIdState = ""
                            state.selectedItem.itemFaId = null
                        }) { family ->
                        family as Family
                        familyIdState = family.familyId
                        state.selectedItem.itemFaId = familyIdState
                    }

                    //Button color
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = btnColorState,
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
                        btnColorState = btnColor
                        state.selectedItem.itemBtnColor = btnColor
                    }

                    //Button text color
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = btnTextColorState,
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
                        btnTextColorState = btnTextColor
                        state.selectedItem.itemBtnTextColor = btnTextColor
                    }

                    SearchableDropdownMenuEx(items = state.printers.toMutableList(),
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        label = "Select Printer",
                        selectedId = printerState,
                        leadingIcon = { modifier ->
                            if (printerState.isNotEmpty()) {
                                Icon(
                                    Icons.Default.RemoveCircleOutline,
                                    contentDescription = "remove printer",
                                    tint = Color.Black,
                                    modifier = modifier
                                )
                            }
                        },
                        onLeadingIconClick = {
                            printerState = ""
                            state.selectedItem.itemPrinter = null
                        }) { printer ->
                        printer as PosPrinter
                        printerState = printer.posPrinterId
                        state.selectedItem.itemPrinter = printerState
                    }

                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = imageState,
                        label = "Image",
                        placeHolder = "Image",
                        focusRequester = imageFocusRequester,
                        imeAction = ImeAction.Done,
                        onAction = { keyboardController?.hide() },
                        trailingIcon = {
                            IconButton(onClick = {
                                activityScopedViewModel.launchGalleryPicker(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
                                    object : OnGalleryResult {
                                        override fun onGalleryResult(uris: List<Uri>) {
                                            if (uris.isNotEmpty()) {
                                                state.isLoading = true
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    val internalPath = FileUtils.saveToExternalStorage(context = context,
                                                        parent = "item",
                                                        uris[0],
                                                        nameState.trim().replace(
                                                            " ",
                                                            "_"
                                                        ).ifEmpty { "item" })
                                                    withContext(Dispatchers.Main) {
                                                        state.isLoading = false
                                                        if (internalPath != null) {
                                                            oldImage = imageState
                                                            imageState = internalPath
                                                            state.selectedItem.itemImage = imageState
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
                                    Icons.Default.Image,
                                    contentDescription = "Image",
                                    tint = SettingsModel.buttonColor
                                )
                            }
                        }) { img ->
                        imageState = img
                        state.selectedItem.itemImage = img
                    }

                    UISwitch(
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        checked = itemPOSState,
                        text = "Item POS",
                    ) { isItemPOS ->
                        itemPOSState = isItemPOS
                        state.selectedItem.itemPos = isItemPOS
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
                            oldImage?.let { old ->
                                FileUtils.deleteFile(
                                    context,
                                    old
                                )
                            }
                            if (imageState.isNotEmpty()) {
                                FileUtils.deleteFile(
                                    context,
                                    imageState
                                )
                            }
                            viewModel.deleteSelectedItem(state.selectedItem)
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
                    selectedId = state.selectedItem.itemId,
                    onLoadItems = { viewModel.fetchItems() },
                    leadingIcon = { modifier ->
                        if (state.selectedItem.itemId.isNotEmpty()) {
                            Icon(
                                Icons.Default.RemoveCircleOutline,
                                contentDescription = "remove family",
                                tint = Color.Black,
                                modifier = modifier
                            )
                        }
                    },
                    onLeadingIconClick = {
                        clear()
                    }) { item ->
                    item as Item
                    viewModel.currentITem = item.copy()
                    state.selectedItem = item
                    nameState = item.itemName ?: ""
                    unitPriceState = item.itemUnitPrice.toString()
                    taxState = item.itemTax.toString()
                    tax1State = item.itemTax1.toString()
                    tax2State = item.itemTax2.toString()
                    barcodeState = item.itemBarcode ?: ""
                    openCostState = item.itemOpenCost.toString()
                    openQtyState = item.itemOpenQty.toString()
                    remQtyState = item.itemRemQty.toString()
                    itemCurrState = item.itemCurrencyId ?: SettingsModel.currentCurrency?.currencyCode1 ?: ""
                    familyIdState = item.itemFaId ?: ""
                    btnColorState = item.itemBtnColor ?: ""
                    btnTextColorState = item.itemBtnTextColor ?: ""
                    printerState = item.itemPrinter ?: ""
                    itemPOSState = item.itemPos
                    imageState = item.itemImage ?: ""
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
                                btnColorState = it.toHexCode()
                                state.selectedItem.itemBtnColor = btnColorState
                            }

                            ColorPickerType.BUTTON_TEXT_COLOR -> {
                                btnTextColorState = it.toHexCode()
                                state.selectedItem.itemBtnTextColor = btnTextColorState
                            }

                            else -> {}
                        }
                        isColorPickerShown = false
                    })

            }
        }
    }
}