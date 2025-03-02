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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.navigation.NavController
import com.grid.pos.R
import com.grid.pos.data.item.Item
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.EditableDateInputField
import com.grid.pos.ui.common.SearchableDropdownMenuEx
import com.grid.pos.ui.common.UIImageButton
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.navigation.Screen
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Utils
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustmentView(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    viewModel: AdjustmentViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val keyboardController = LocalSoftwareKeyboardController.current
    var collapseItemListState by remember { mutableStateOf(false) }
    var isBackPressed by remember { mutableStateOf(false) }

    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    fun handleBack() {
        if (isImeVisible) {
            keyboardController?.hide()
            return
        }
        viewModel.checkAndBack {
            if (!isBackPressed) {
                isBackPressed = true
                navController?.navigateUp()
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
                            IconButton(onClick = { navController?.navigate(Screen.SettingsView.route) }) {
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
                        keyboardController?.hide()
                        viewModel.adjustRemainingQuantities()
                    }

                    UITextField(
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        defaultValue = state.itemCostString,
                        label = "Item Cost",
                        placeHolder = "Enter Cost",
                        keyboardType = KeyboardType.Decimal
                    ) { cost ->
                        val costStr = Utils.getDoubleValue(
                            cost,
                            state.itemCostString
                        )
                        viewModel.updateState(
                            state.copy(
                                itemCostString = costStr
                            )
                        )
                    }

                    EditableDateInputField(
                        modifier = Modifier.padding(10.dp),
                        date = state.fromDateString,
                        dateTimeFormat = viewModel.dateFormat,
                        label = "From"
                    ) { dateStr ->
                        val date = DateHelper.getDateFromString(dateStr, viewModel.dateFormat)
                        if (date.after(Date())) {
                            viewModel.showPopup(
                                PopupModel(
                                    dialogText = "From date should be today or before, please select again",
                                    negativeBtnText = null
                                )
                            )
                        } else {
                            viewModel.updateState(
                                state.copy(
                                    fromDateString = dateStr
                                )
                            )
                        }
                    }

                    EditableDateInputField(
                        modifier = Modifier.padding(10.dp),
                        date = state.toDateString,
                        dateTimeFormat = viewModel.dateFormat,
                        label = "To"
                    ) { dateStr ->
                        viewModel.updateState(
                            state.copy(
                                toDateString = dateStr
                            )
                        )
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
                        viewModel.updateItemCost()
                    }
                }

                SearchableDropdownMenuEx(items = state.items.toMutableList(),
                    modifier = Modifier.padding(
                        top = 15.dp,
                        start = 10.dp,
                        end = 10.dp
                    ),
                    label = "Select Item",
                    selectedId = state.selectedItem?.itemId,
                    onLoadItems = { viewModel.fetchItems() },
                    leadingIcon = { mod ->
                        if (state.selectedItem?.itemId?.isNotEmpty() == true) {
                            Icon(
                                Icons.Default.RemoveCircleOutline,
                                contentDescription = "remove item",
                                tint = Color.Black,
                                modifier = mod
                            )
                        }
                    },
                    onLeadingIconClick = {
                        viewModel.updateState(
                            state.copy(
                                selectedItem = null
                            )
                        )
                    },
                    collapseOnInit = collapseItemListState,
                    searchEnteredText = state.barcodeSearchedKey,
                    searchLeadingIcon = {
                        IconButton(onClick = {
                            collapseItemListState = false
                            viewModel.launchBarcodeScanner {
                                collapseItemListState = true
                            }
                        }) {
                            Icon(
                                Icons.Default.QrCode2,
                                contentDescription = "Barcode",
                                tint = SettingsModel.buttonColor
                            )
                        }
                    }) { item ->
                    item as Item
                    viewModel.updateState(
                        state.copy(
                            selectedItem = item
                        )
                    )
                }
            }
        }
    }
}