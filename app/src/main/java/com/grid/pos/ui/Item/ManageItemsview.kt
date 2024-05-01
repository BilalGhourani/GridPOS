package com.grid.pos.ui.Item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
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
import com.grid.pos.data.Family.Family
import com.grid.pos.data.Item.Item
import com.grid.pos.data.PosPrinter.PosPrinter
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.LoadingIndicator
import com.grid.pos.ui.common.SearchableDropdownMenu
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.common.UISwitch
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ManageItemsView(
    navController: NavController? = null,
    modifier: Modifier = Modifier,
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

    var nameState by remember { mutableStateOf("") }
    var unitPriceState by remember { mutableStateOf("") }
    var taxState by remember { mutableStateOf("") }
    var tax1State by remember { mutableStateOf("") }
    var tax2State by remember { mutableStateOf("") }
    var barcodeState by remember { mutableStateOf("") }
    var openCostState by remember { mutableStateOf("") }
    var openQtyState by remember { mutableStateOf("") }
    var familyIdState by remember { mutableStateOf("") }
    var btnColorState by remember { mutableStateOf("") }
    var btnTextColorState by remember { mutableStateOf("") }
    var posPrinterState by remember { mutableStateOf("") }
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
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            topBar = {
                Surface(shadowElevation = 3.dp, color = Color.White) {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = { navController?.navigateUp() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        title = {
                            Text(
                                text = "Manage Items",
                                color = Color.Black,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        })
                }
            }
        ) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(it)
                    .background(color = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
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
                            unitPriceState = item.itemUnitPrice ?: ""
                            taxState = item.itemTax ?: ""
                            tax1State = item.itemTax1 ?: ""
                            tax2State = item.itemTax2 ?: ""
                            barcodeState = item.itemBarcode ?: ""
                            openCostState = item.itemOpenCost ?: ""
                            openQtyState = item.itemOpenQty ?: ""
                            familyIdState = item.itemFaId ?: ""
                            btnColorState = item.itemBtnColor ?: ""
                            btnTextColorState = item.itemBtnTextColor ?: ""
                            posPrinterState = item.itemPosPrinter ?: ""
                            itemPOSState = item.itemPos
                        }

                        //name
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = nameState,
                            label = "Name",
                            placeHolder = "Enter Name",
                            onAction = { unitPriceFocusRequester.requestFocus() }
                        ) { name ->
                            nameState = name
                            manageItemsState.selectedItem.itemName = name
                        }

                        //unitPrice
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = unitPriceState,
                            label = "Unit Price",
                            focusRequester = unitPriceFocusRequester,
                            keyboardType = KeyboardType.Decimal,
                            placeHolder = "Enter Unit Price",
                            onAction = { taxFocusRequester.requestFocus() }
                        ) { unitPrice ->
                            unitPriceState = unitPrice
                            manageItemsState.selectedItem.itemUnitPrice = unitPrice
                        }

                        //tax
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = taxState,
                            label = "Tax",
                            maxLines = 3,
                            focusRequester = taxFocusRequester,
                            keyboardType = KeyboardType.Decimal,
                            placeHolder = "Enter Tax",
                            onAction = { tax1FocusRequester.requestFocus() }
                        ) { tax ->
                            taxState = tax
                            manageItemsState.selectedItem.itemTax = tax
                        }

                        //tax1
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = tax1State,
                            label = "Tax1",
                            focusRequester = tax1FocusRequester,
                            keyboardType = KeyboardType.Decimal,
                            placeHolder = "Enter Tax1",
                            onAction = { tax2FocusRequester.requestFocus() }
                        ) { tax1 ->
                            tax1State = tax1
                            manageItemsState.selectedItem.itemTax1 = tax1
                        }

                        //tax2
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = tax2State,
                            label = "Tax2",
                            focusRequester = tax2FocusRequester,
                            keyboardType = KeyboardType.Decimal,
                            placeHolder = "Enter Tax2",
                            onAction = { barcodeFocusRequester.requestFocus() }
                        ) { tax2 ->
                            tax2State = tax2
                            manageItemsState.selectedItem.itemTax2 = tax2
                        }

                        //barcode
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = barcodeState,
                            label = "Barcode",
                            placeHolder = "Enter Barcode",
                            focusRequester = barcodeFocusRequester,
                            onAction = { openCostFocusRequester.requestFocus() }
                        ) { barcode ->
                            barcodeState = barcode
                            manageItemsState.selectedItem.itemBarcode = barcode
                        }

                        //open cost
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = openCostState,
                            label = "Open cost",
                            placeHolder = "Enter Open cost",
                            focusRequester = openCostFocusRequester,
                            onAction = { openQtyFocusRequester.requestFocus() }
                        ) { openCost ->
                            openCostState = openCost
                            manageItemsState.selectedItem.itemOpenCost = openCost
                        }

                        //open quantity
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = openQtyState,
                            label = "Open Qty",
                            keyboardType = KeyboardType.Decimal,
                            placeHolder = "Enter Open Qty",
                            focusRequester = openQtyFocusRequester,
                            onAction = { btnColorFocusRequester.requestFocus() }
                        ) { openQty ->
                            openQtyState = openQty
                            manageItemsState.selectedItem.itemOpenQty = openQty
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
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = btnColorState,
                            label = "Button color",
                            placeHolder = "Enter Button color",
                            focusRequester = btnColorFocusRequester,
                            onAction = { btnTextColorFocusRequester.requestFocus() }
                        ) { btnColor ->
                            btnColorState = btnColor
                            manageItemsState.selectedItem.itemBtnColor = btnColor
                        }

                        //Button text color
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = btnTextColorState,
                            label = "Button Text color",
                            placeHolder = "Enter Button Text color",
                            focusRequester = btnTextColorFocusRequester,
                            imeAction = ImeAction.Done,
                            onAction = { keyboardController?.hide() }
                        ) { btnTextColor ->
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

                        UISwitch(
                            modifier = Modifier.padding(10.dp),
                            checked = itemPOSState,
                            text = "Item POS",
                        ) {
                            itemPOSState = it
                            manageItemsState.selectedItem.itemPos = it
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
                                navController?.navigateUp()
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