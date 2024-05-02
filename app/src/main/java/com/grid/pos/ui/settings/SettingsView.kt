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
    var isForText by remember { mutableStateOf(false) }
    var isColorPickerShown by remember { mutableStateOf(false) }
    var loadFromRemote by remember { mutableStateOf(SettingsModel.loadFromRemote) }
    var hideTaxInputs by remember { mutableStateOf(SettingsModel.hideTaxInputs) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    GridPOSTheme {
        Scaffold(
            topBar = {
                Surface(shadowElevation = 3.dp, color = Color.White) {
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
                                color = Color.Black,
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
                        .height(60.dp)
                        .padding(10.dp),
                    text = "Button Color",
                    buttonColor = buttonColorState,
                    textColor = buttonTextColorState
                ) {
                    isForText = false
                    isColorPickerShown = true
                }

                UIButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(10.dp),
                    text = "Button Text Color",
                    buttonColor = buttonColorState,
                    textColor = buttonTextColorState
                ) {
                    isForText = true
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
                    defaultColor = if (isForText) {
                        buttonTextColorState
                    } else {
                        buttonColorState
                    }
                ) {
                    if (isForText) {
                        buttonTextColorState = it
                        SettingsModel.buttonTextColor = it
                        CoroutineScope(Dispatchers.IO).launch {
                            DataStoreManager.putString(
                                DataStoreManager.DataStoreKeys.BUTTON_TEXT_COLOR.key,
                                it.toHexCode()
                            )
                        }
                    } else {
                        buttonColorState = it
                        SettingsModel.buttonColor = it
                        CoroutineScope(Dispatchers.IO).launch {
                            DataStoreManager.putString(
                                DataStoreManager.DataStoreKeys.BUTTON_COLOR.key,
                                it.toHexCode()
                            )
                        }
                    }
                    isColorPickerShown = false
                }
            }
        }
    }
}