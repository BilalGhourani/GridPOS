package com.grid.pos.ui.item

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.grid.pos.MainActivity
import com.grid.pos.data.Family.Family
import com.grid.pos.data.Item.Item
import com.grid.pos.data.PosPrinter.PosPrinter
import com.grid.pos.interfaces.OnGalleryResult
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.LoadingIndicator
import com.grid.pos.ui.common.SearchableDropdownMenu
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.common.UISwitch
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageItemsView(
        modifier: Modifier = Modifier,
        navController: NavController? = null,
        mainActivity: MainActivity,
        viewModel: ManageItemsViewModel = hiltViewModel()
) {
    val manageItemsState: ManageItemsState by viewModel.manageItemsState.collectAsState(
        ManageItemsState()
    )
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
    var tax1State by remember { mutableStateOf(SettingsModel.currentCompany?.companyTax1.toString()) }
    var tax2State by remember { mutableStateOf(SettingsModel.currentCompany?.companyTax2.toString()) }
    var barcodeState by remember { mutableStateOf("") }
    var openCostState by remember { mutableStateOf("") }
    var openQtyState by remember { mutableStateOf("") }
    var familyIdState by remember { mutableStateOf("") }
    var btnColorState by remember { mutableStateOf("") }
    var btnTextColorState by remember { mutableStateOf("") }
    var posPrinterState by remember { mutableStateOf("") }
    var imageState by remember { mutableStateOf(manageItemsState.selectedItem.itemImage ?: "") }
    var itemPOSState by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(manageItemsState.warning) {
        if (!manageItemsState.warning.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.Main).launch {
                snackbarHostState.showSnackbar(
                    message = manageItemsState.warning!!,
                    duration = SnackbarDuration.Short,
                )
            }
        }
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
                                text = "Manage Items",
                                color = SettingsModel.textColor,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
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
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(
                                rememberScrollState()
                            )
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SearchableDropdownMenu(
                            items = manageItemsState.items.toMutableList(),
                            modifier = Modifier.padding(10.dp),
                            label = nameState.ifEmpty { "Select Item" },
                        ) { item ->
                            item as Item
                            manageItemsState.selectedItem = item
                            nameState = item.itemName ?: ""
                            unitPriceState = item.itemUnitPrice.toString()
                            taxState = item.itemTax.toString()
                            tax1State = item.itemTax1.toString()
                            tax2State = item.itemTax2.toString()
                            barcodeState = item.itemBarcode ?: ""
                            openCostState = item.itemOpenCost.toString()
                            openQtyState = item.itemOpenQty.toString()
                            familyIdState = item.itemFaId ?: ""
                            btnColorState = item.itemBtnColor ?: ""
                            btnTextColorState = item.itemBtnTextColor ?: ""
                            posPrinterState = item.itemPosPrinter ?: ""
                            itemPOSState = item.itemPos
                        }

                        //name
                        UITextField(modifier = Modifier.padding(10.dp),
                            defaultValue = nameState,
                            label = "Name",
                            placeHolder = "Enter Name",
                            onAction = { unitPriceFocusRequester.requestFocus() }) { name ->
                            nameState = name
                            manageItemsState.selectedItem.itemName = name
                        }

                        //unitPrice
                        UITextField(modifier = Modifier.padding(10.dp),
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
                            manageItemsState.selectedItem.itemUnitPrice = unitPriceState.toDoubleOrNull() ?: 0.0
                        }

                        if (SettingsModel.showTax) {
                            //tax
                            UITextField(modifier = Modifier.padding(10.dp),
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
                                manageItemsState.selectedItem.itemTax = taxState.toDoubleOrNull() ?: 0.0
                            }
                        }
                        if (SettingsModel.showTax1) {
                            //tax1
                            UITextField(modifier = Modifier.padding(10.dp),
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
                                manageItemsState.selectedItem.itemTax1 = tax1State.toDoubleOrNull() ?: 0.0
                            }
                        }
                        if (SettingsModel.showTax2) {
                            //tax2
                            UITextField(modifier = Modifier.padding(10.dp),
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
                                manageItemsState.selectedItem.itemTax2 = tax2State.toDoubleOrNull() ?: 0.0
                            }
                        }
                        //barcode
                        UITextField(modifier = Modifier.padding(10.dp),
                            defaultValue = barcodeState,
                            label = "Barcode",
                            placeHolder = "Enter Barcode",
                            focusRequester = barcodeFocusRequester,
                            onAction = { openCostFocusRequester.requestFocus() }) { barcode ->
                            barcodeState = barcode
                            manageItemsState.selectedItem.itemBarcode = barcode
                        }

                        //open cost
                        UITextField(modifier = Modifier.padding(10.dp),
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
                            manageItemsState.selectedItem.itemOpenCost = openCostState.toDoubleOrNull() ?: 0.0
                        }

                        //open quantity
                        UITextField(modifier = Modifier.padding(10.dp),
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
                            manageItemsState.selectedItem.itemOpenQty = openQtyState.toDoubleOrNull() ?: 0.0
                        }

                        SearchableDropdownMenu(
                            items = manageItemsState.families.toMutableList(),
                            modifier = Modifier.padding(10.dp),
                            label = "Select Family",
                            selectedId = familyIdState
                        ) { family ->
                            family as Family
                            familyIdState = family.familyId
                            manageItemsState.selectedItem.itemFaId = familyIdState
                        }

                        //Button color
                        UITextField(modifier = Modifier.padding(10.dp),
                            defaultValue = btnColorState,
                            label = "Button color",
                            placeHolder = "Enter Button color",
                            focusRequester = btnColorFocusRequester,
                            onAction = { btnTextColorFocusRequester.requestFocus() }) { btnColor ->
                            btnColorState = btnColor
                            manageItemsState.selectedItem.itemBtnColor = btnColor
                        }

                        //Button text color
                        UITextField(modifier = Modifier.padding(10.dp),
                            defaultValue = btnTextColorState,
                            label = "Button Text color",
                            placeHolder = "Enter Button Text color",
                            focusRequester = btnTextColorFocusRequester,
                            onAction = { imageFocusRequester.requestFocus() }) { btnTextColor ->
                            btnTextColorState = btnTextColor
                            manageItemsState.selectedItem.itemBtnTextColor = btnTextColor
                        }

                        SearchableDropdownMenu(
                            items = manageItemsState.printers.toMutableList(),
                            modifier = Modifier.padding(10.dp),
                            label = "Select Printer",
                            selectedId = posPrinterState
                        ) { printer ->
                            printer as PosPrinter
                            posPrinterState = printer.posPrinterId
                            manageItemsState.selectedItem.itemPosPrinter = posPrinterState
                        }

                        UITextField(modifier = Modifier.padding(10.dp),
                            defaultValue = imageState,
                            label = "Image",
                            placeHolder = "Image",
                            focusRequester = imageFocusRequester,
                            imeAction = ImeAction.Done,
                            onAction = { keyboardController?.hide() },
                            trailingIcon = {
                                IconButton(onClick = {
                                    mainActivity.launchGalleryPicker(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
                                        object : OnGalleryResult {
                                            override fun onGalleryResult(uris: List<Uri>) {
                                                if (uris.isNotEmpty()) {
                                                    imageState = uris[0].toString()
                                                    manageItemsState.selectedItem.itemImage = imageState
                                                }
                                            }

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
                            manageItemsState.selectedItem.itemImage = img
                        }

                        UISwitch(
                            modifier = Modifier.padding(10.dp),
                            checked = itemPOSState,
                            text = "Item POS",
                        ) { isItemPOS ->
                            itemPOSState = isItemPOS
                            manageItemsState.selectedItem.itemPos = isItemPOS
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(10.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            UIButton(
                                modifier = Modifier
                                    .weight(.33f)
                                    .padding(3.dp),
                                text = "Save"
                            ) {
                                viewModel.saveItem(manageItemsState.selectedItem)
                            }

                            UIButton(
                                modifier = Modifier
                                    .weight(.33f)
                                    .padding(3.dp),
                                text = "Delete"
                            ) {
                                viewModel.deleteSelectedItem(manageItemsState.selectedItem)
                            }

                            UIButton(
                                modifier = Modifier
                                    .weight(.33f)
                                    .padding(3.dp),
                                text = "Close"
                            ) {
                                navController?.popBackStack()
                            }
                        }
                    }
                }
            }
        }
        LoadingIndicator(
            show = manageItemsState.isLoading
        )

        if (manageItemsState.clear) {
            manageItemsState.selectedItem = Item()
            manageItemsState.selectedItem.itemCompId = ""
            manageItemsState.selectedItem.itemFaId = ""
            nameState = ""
            unitPriceState = ""
            taxState = ""
            tax1State = ""
            tax2State = ""
            barcodeState = ""
            openCostState = ""
            openQtyState = ""
            familyIdState = ""
            btnColorState = ""
            btnTextColorState = ""
            posPrinterState = ""
            manageItemsState.clear = false
        }
    }
}