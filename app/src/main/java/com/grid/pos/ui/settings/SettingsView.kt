package com.grid.pos.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.common.UIColorPicker
import com.grid.pos.ui.common.UISwitch
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.ui.theme.White
import com.grid.pos.utils.DataStoreManager
import com.grid.pos.utils.Extension.toHexCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    navController: NavController? = null,
    modifier: Modifier = Modifier
) {
    var buttonColorState by remember { mutableStateOf(SettingsModel.buttonColor) }
    var buttonTextColorState by remember { mutableStateOf(SettingsModel.buttonTextColor) }
    var backgroundColorState by remember { mutableStateOf(SettingsModel.buttonTextColor) }
    var textColorState by remember { mutableStateOf(SettingsModel.buttonTextColor) }
    var colorPickerType by remember { mutableStateOf(ColorPickerType.BUTTON_COLOR) }
    var isColorPickerShown by remember { mutableStateOf(false) }
    var loadFromRemote by remember { mutableStateOf(SettingsModel.loadFromRemote) }
    var hideTaxInputs by remember { mutableStateOf(SettingsModel.hideTaxInputs) }
    var showPriceInItemBtn by remember { mutableStateOf(SettingsModel.showPriceInItemBtn) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    GridPOSTheme {
        Scaffold(
            topBar = {
                Surface(shadowElevation = 3.dp, color = backgroundColorState) {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = { navController?.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        title = {
                            Text(
                                text = "Settings",
                                color = textColorState,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        })
                }
            }
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(it),
            ) {
                UIButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(10.dp),
                    text = "Button Color",
                    buttonColor = buttonColorState,
                    textColor = buttonTextColorState
                ) {
                    colorPickerType = ColorPickerType.BUTTON_COLOR
                    isColorPickerShown = true
                }

                UIButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(10.dp),
                    text = "Button Text Color",
                    buttonColor = buttonColorState,
                    textColor = buttonTextColorState
                ) {
                    colorPickerType = ColorPickerType.BUTTON_TEXT_COLOR
                    isColorPickerShown = true
                }

                UIButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(10.dp),
                    text = "Background Color",
                    buttonColor = buttonColorState,
                    textColor = buttonTextColorState
                ) {
                    colorPickerType = ColorPickerType.BACKGROUND_COLOR
                    isColorPickerShown = true
                }

                UIButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(10.dp),
                    text = "Text Color",
                    buttonColor = buttonColorState,
                    textColor = buttonTextColorState
                ) {
                    colorPickerType = ColorPickerType.TEXT_COLOR
                    isColorPickerShown = true
                }



                UISwitch(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(10.dp),
                    checked = loadFromRemote,
                    text = "Load From Remote",
                ) {
                    loadFromRemote = it
                    SettingsModel.loadFromRemote = it
                    CoroutineScope(Dispatchers.IO).launch {
                        DataStoreManager.putBoolean(
                            DataStoreManager.DataStoreKeys.LOAD_FROM_REMOTE.key,
                            it
                        )
                    }
                }

                UISwitch(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(10.dp),
                    checked = hideTaxInputs,
                    text = "Hide Tax Inputs",
                ) {
                    hideTaxInputs = it
                    SettingsModel.hideTaxInputs = it
                    CoroutineScope(Dispatchers.IO).launch {
                        DataStoreManager.putBoolean(
                            DataStoreManager.DataStoreKeys.HIDE_TAX_INPUTS.key,
                            it
                        )
                    }
                }

                UISwitch(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(10.dp),
                    checked = showPriceInItemBtn,
                    text = "Show Price in Item Button",
                ) {
                    showPriceInItemBtn = it
                    SettingsModel.showPriceInItemBtn = it
                    CoroutineScope(Dispatchers.IO).launch {
                        DataStoreManager.putBoolean(
                            DataStoreManager.DataStoreKeys.SHOW_PRICE_IN_ITEM_BTN.key,
                            it
                        )
                    }
                }
            }
        }
        if (isColorPickerShown) {
            ModalBottomSheet(
                onDismissRequest = { isColorPickerShown = false },
                sheetState = bottomSheetState,
                containerColor = Color.White,
                contentColor = White,
                shape = RectangleShape,
                dragHandle = null,
                scrimColor = Color.Black.copy(alpha = .5f),
                windowInsets = WindowInsets(0, 0, 0, 0)
            ) {
                UIColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f),
                    defaultColor = when (colorPickerType) {
                        ColorPickerType.BUTTON_COLOR -> buttonColorState
                        ColorPickerType.BUTTON_TEXT_COLOR -> buttonTextColorState
                        ColorPickerType.BACKGROUND_COLOR -> backgroundColorState
                        ColorPickerType.TEXT_COLOR -> textColorState
                    }
                ) {
                    when (colorPickerType) {
                        ColorPickerType.BUTTON_COLOR -> {
                            buttonColorState = it
                            SettingsModel.buttonColor = it
                            CoroutineScope(Dispatchers.IO).launch {
                                DataStoreManager.putString(
                                    DataStoreManager.DataStoreKeys.BUTTON_COLOR.key,
                                    it.toHexCode()
                                )
                            }
                        }

                        ColorPickerType.BUTTON_TEXT_COLOR -> {
                            buttonTextColorState = it
                            SettingsModel.buttonTextColor = it
                            CoroutineScope(Dispatchers.IO).launch {
                                DataStoreManager.putString(
                                    DataStoreManager.DataStoreKeys.BUTTON_TEXT_COLOR.key,
                                    it.toHexCode()
                                )
                            }
                        }

                        ColorPickerType.BACKGROUND_COLOR -> {
                            backgroundColorState = it
                            SettingsModel.backgroundColor = it
                            CoroutineScope(Dispatchers.IO).launch {
                                DataStoreManager.putString(
                                    DataStoreManager.DataStoreKeys.BACKGROUND_COLOR.key,
                                    it.toHexCode()
                                )
                            }
                        }

                        ColorPickerType.TEXT_COLOR -> {
                            textColorState = it
                            SettingsModel.textColor = it
                            CoroutineScope(Dispatchers.IO).launch {
                                DataStoreManager.putString(
                                    DataStoreManager.DataStoreKeys.TEXT_COLOR.key,
                                    it.toHexCode()
                                )
                            }
                        }
                    }
                    isColorPickerShown = false
                }
            }
        }
    }
}

enum class ColorPickerType(val key: String) {
    BUTTON_COLOR("BUTTON_COLOR"),
    BUTTON_TEXT_COLOR("BUTTON_TEXT_COLOR"),
    BACKGROUND_COLOR("BACKGROUND_COLOR"),
    TEXT_COLOR("TEXT_COLOR")
}