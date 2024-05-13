package com.grid.pos.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.ColorPickerPopup
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.common.UISwitch
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.ui.theme.LightGrey
import com.grid.pos.utils.DataStoreManager
import com.grid.pos.utils.Extension.toHexCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
        modifier: Modifier = Modifier,
        navController: NavController? = null
) {
    var firebaseApplicationId by remember {
        mutableStateOf(
            SettingsModel.firebaseApplicationId ?: ""
        )
    }
    var firebaseApiKey by remember { mutableStateOf(SettingsModel.firebaseApiKey ?: "") }
    var firebaseProjectId by remember { mutableStateOf(SettingsModel.firebaseProjectId ?: "") }
    var firebaseDbPath by remember { mutableStateOf(SettingsModel.firebaseDbPath ?: "") }
    var companyID by remember { mutableStateOf(SettingsModel.companyID ?: "") }
    var invoicePrinter by remember { mutableStateOf(SettingsModel.invoicePrinter ?: "") }

    val firebaseApiKeyRequester = remember { FocusRequester() }
    val firebaseProjectIdRequester = remember { FocusRequester() }
    val firebaseDbPathRequester = remember { FocusRequester() }
    val companyIdRequester = remember { FocusRequester() }
    val printerRequester = remember { FocusRequester() }

    var loadFromRemote by remember { mutableStateOf(SettingsModel.loadFromRemote) }
    var showTax by remember { mutableStateOf(SettingsModel.showTax) }
    var showTax1 by remember { mutableStateOf(SettingsModel.showTax1) }
    var showTax2 by remember { mutableStateOf(SettingsModel.showTax2) }
    var showPriceInItemBtn by remember { mutableStateOf(SettingsModel.showPriceInItemBtn) }

    var buttonColorState by remember { mutableStateOf(SettingsModel.buttonColor) }
    var buttonTextColorState by remember { mutableStateOf(SettingsModel.buttonTextColor) }
    var topBarColorState by remember { mutableStateOf(SettingsModel.topBarColor) }
    var backgroundColorState by remember { mutableStateOf(SettingsModel.backgroundColor) }
    var textColorState by remember { mutableStateOf(SettingsModel.textColor) }
    var colorPickerType by remember { mutableStateOf(ColorPickerType.BUTTON_COLOR) }
    var isColorPickerShown by remember { mutableStateOf(false) }

    var isFirebaseSectionExpanded by remember { mutableStateOf(false) }
    var isAppSettingsSectionExpanded by remember { mutableStateOf(false) }
    var isColorsSectionExpanded by remember { mutableStateOf(false) }

    val isLoggedId = !SettingsModel.currentUserId.isNullOrEmpty()

    GridPOSTheme {
        Scaffold(containerColor = SettingsModel.backgroundColor, topBar = {
            Surface(
                shadowElevation = 3.dp, color = backgroundColorState
            ) {
                TopAppBar(colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = SettingsModel.topBarColor
                ), navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back", tint = SettingsModel.buttonColor
                        )
                    }
                }, title = {
                    Text(
                        text = "Settings", color = textColorState, fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                })
            }
        }) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(it)
                    .verticalScroll(rememberScrollState()),
            ) {
                if (!isLoggedId) {
                    Card(
                        modifier = Modifier
                            .padding(10.dp)
                            .animateContentSize(),
                        shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(
                            containerColor = LightGrey,
                        )
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clickable {
                                    isFirebaseSectionExpanded = !isFirebaseSectionExpanded
                                }, horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Connectivity Settings", modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center, style = TextStyle(
                                    textDecoration = TextDecoration.None,
                                    fontWeight = FontWeight.SemiBold, fontSize = 16.sp
                                ), color = SettingsModel.textColor
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Icon(
                                Icons.Default.KeyboardArrowDown, null, Modifier
                                    .padding(16.dp)
                                    .size(20.dp)
                                    .align(
                                        Alignment.CenterVertically
                                    )
                                    .rotate(
                                        if (isFirebaseSectionExpanded) 180f else 0f
                                    ), tint = Color.Black
                            )
                        }

                        if (isFirebaseSectionExpanded) {
                            UITextField(modifier = Modifier.padding(10.dp),
                                defaultValue = firebaseApplicationId, label = "Application ID",
                                placeHolder = "Application ID",
                                onAction = { firebaseApiKeyRequester.requestFocus() }) { appId ->
                                firebaseApplicationId = appId
                            }

                            UITextField(modifier = Modifier.padding(10.dp),
                                defaultValue = firebaseApiKey, label = "Api Key",
                                placeHolder = "Api Key", focusRequester = firebaseApiKeyRequester,
                                onAction = { firebaseProjectIdRequester.requestFocus() }) { apiKey ->
                                firebaseApiKey = apiKey
                            }

                            UITextField(modifier = Modifier.padding(10.dp),
                                defaultValue = firebaseProjectId, label = "Project ID",
                                placeHolder = "Project ID",
                                focusRequester = firebaseProjectIdRequester,
                                onAction = { firebaseDbPathRequester.requestFocus() }) { projectID ->
                                firebaseProjectId = projectID
                            }

                            UITextField(modifier = Modifier.padding(10.dp),
                                defaultValue = firebaseDbPath, label = "Database Path",
                                placeHolder = "Database Path",
                                focusRequester = firebaseDbPathRequester,
                                onAction = { companyIdRequester.requestFocus() }) { dbPath ->
                                firebaseDbPath = dbPath
                            }

                            UITextField(
                                modifier = Modifier.padding(10.dp), defaultValue = companyID,
                                label = "Company ID", placeHolder = "Company ID",
                                focusRequester = companyIdRequester,
                                onAction = { printerRequester.requestFocus() }
                            ) { compId ->
                                companyID = compId
                            }

                            UITextField(
                                modifier = Modifier.padding(10.dp), defaultValue = invoicePrinter,
                                label = "Invoice Printer", placeHolder = "Printer name",
                                focusRequester = printerRequester, imeAction = ImeAction.Done
                            ) { printer ->
                                invoicePrinter = printer
                            }

                            UIButton(
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .height(60.dp)
                                    .padding(
                                        10.dp
                                    )
                                    .align(
                                        Alignment.CenterHorizontally
                                    ), text = "Save"
                            ) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    SettingsModel.firebaseApplicationId = firebaseApplicationId
                                    DataStoreManager.putString(
                                        DataStoreManager.DataStoreKeys.FIREBASE_APP_ID.key,
                                        firebaseApplicationId
                                    )
                                    SettingsModel.firebaseApiKey = firebaseApiKey
                                    DataStoreManager.putString(
                                        DataStoreManager.DataStoreKeys.FIREBASE_API_KEY.key,
                                        firebaseApiKey
                                    )
                                    SettingsModel.firebaseProjectId = firebaseProjectId
                                    DataStoreManager.putString(
                                        DataStoreManager.DataStoreKeys.FIREBASE_PROJECT_ID.key,
                                        firebaseProjectId
                                    )
                                    SettingsModel.firebaseDbPath = firebaseDbPath
                                    DataStoreManager.putString(
                                        DataStoreManager.DataStoreKeys.FIREBASE_DB_PATH.key,
                                        firebaseDbPath
                                    )
                                    SettingsModel.firebaseDbPath = firebaseDbPath
                                    DataStoreManager.putString(
                                        DataStoreManager.DataStoreKeys.FIREBASE_DB_PATH.key,
                                        firebaseDbPath
                                    )
                                    SettingsModel.companyID = companyID
                                    DataStoreManager.putString(
                                        DataStoreManager.DataStoreKeys.COMPANY_ID.key, companyID
                                    )

                                    SettingsModel.invoicePrinter = invoicePrinter
                                    DataStoreManager.putString(
                                        DataStoreManager.DataStoreKeys.INVOICE_PRINTER.key, invoicePrinter
                                    )
                                }
                            }
                        }
                    }
                }
                Card(
                    modifier = Modifier
                        .padding(10.dp)
                        .animateContentSize(),
                    shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(
                        containerColor = LightGrey,
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clickable {
                                isAppSettingsSectionExpanded = !isAppSettingsSectionExpanded
                            }, horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "App Settings", modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center, style = TextStyle(
                                textDecoration = TextDecoration.None,
                                fontWeight = FontWeight.SemiBold, fontSize = 16.sp
                            ), color = SettingsModel.textColor
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Icon(
                            Icons.Default.KeyboardArrowDown, null, Modifier
                                .padding(horizontal = 16.dp)
                                .size(20.dp)
                                .align(
                                    Alignment.CenterVertically
                                )
                                .rotate(
                                    if (isAppSettingsSectionExpanded) 180f else 0f
                                ), tint = Color.Black
                        )
                    }

                    if (isAppSettingsSectionExpanded) {
                        if (!isLoggedId) {
                            UISwitch(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .padding(10.dp),
                                checked = loadFromRemote, text = "Load From Remote",
                                textColor = textColorState
                            ) { isRemote ->
                                loadFromRemote = isRemote
                                SettingsModel.loadFromRemote = isRemote
                                CoroutineScope(Dispatchers.IO).launch {
                                    DataStoreManager.putBoolean(
                                        DataStoreManager.DataStoreKeys.LOAD_FROM_REMOTE.key,
                                        isRemote
                                    )
                                }
                            }
                        }
                        UISwitch(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .padding(10.dp),
                            checked = showTax, text = "Show Tax", textColor = textColorState
                        ) { showTx ->
                            showTax = showTx
                            SettingsModel.showTax = showTx
                            CoroutineScope(Dispatchers.IO).launch {
                                DataStoreManager.putBoolean(
                                    DataStoreManager.DataStoreKeys.SHOW_TAX.key, showTx
                                )
                            }
                        }

                        UISwitch(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .padding(10.dp),
                            checked = showTax1, text = "Show Tax1", textColor = textColorState
                        ) { showTx1 ->
                            showTax1 = showTx1
                            SettingsModel.showTax1 = showTx1
                            CoroutineScope(Dispatchers.IO).launch {
                                DataStoreManager.putBoolean(
                                    DataStoreManager.DataStoreKeys.SHOW_TAX1.key, showTx1
                                )
                            }
                        }

                        UISwitch(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .padding(10.dp),
                            checked = showTax2, text = "Show Tax2", textColor = textColorState
                        ) { showTx2 ->
                            showTax2 = showTx2
                            SettingsModel.showTax2 = showTx2
                            CoroutineScope(Dispatchers.IO).launch {
                                DataStoreManager.putBoolean(
                                    DataStoreManager.DataStoreKeys.SHOW_TAX2.key, showTx2
                                )
                            }
                        }

                        UISwitch(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .padding(10.dp),
                            checked = showPriceInItemBtn, text = "Show Price in Item Button",
                            textColor = textColorState
                        ) { showPrice ->
                            showPriceInItemBtn = showPrice
                            SettingsModel.showPriceInItemBtn = showPrice
                            CoroutineScope(Dispatchers.IO).launch {
                                DataStoreManager.putBoolean(
                                    DataStoreManager.DataStoreKeys.SHOW_PRICE_IN_ITEM_BTN.key,
                                    showPrice
                                )
                            }
                        }
                    }
                }
                Card(
                    modifier = Modifier
                        .padding(10.dp)
                        .animateContentSize(),
                    shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(
                        containerColor = LightGrey,
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clickable { isColorsSectionExpanded = !isColorsSectionExpanded },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Colors", modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center, style = TextStyle(
                                textDecoration = TextDecoration.None,
                                fontWeight = FontWeight.SemiBold, fontSize = 16.sp
                            ), color = SettingsModel.textColor
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Icon(
                            Icons.Default.KeyboardArrowDown, null, Modifier
                                .padding(16.dp)
                                .size(20.dp)
                                .align(
                                    Alignment.CenterVertically
                                )
                                .rotate(
                                    if (isColorsSectionExpanded) 180f else 0f
                                ), tint = Color.Black
                        )
                    }
                    if (isColorsSectionExpanded) {
                        UIButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp)
                                .padding(10.dp),
                            text = "Button Color", buttonColor = buttonColorState,
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
                            text = "Button Text Color", buttonColor = buttonColorState,
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
                            text = "Top Bar Color", buttonColor = buttonColorState,
                            textColor = buttonTextColorState
                        ) {
                            colorPickerType = ColorPickerType.TOP_BAR_COLOR
                            isColorPickerShown = true
                        }

                        UIButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp)
                                .padding(10.dp),
                            text = "Background Color", buttonColor = buttonColorState,
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
                            text = "Text Color", buttonColor = buttonColorState,
                            textColor = buttonTextColorState
                        ) {
                            colorPickerType = ColorPickerType.TEXT_COLOR
                            isColorPickerShown = true
                        }
                    }
                }
                if (isLoggedId) {
                    Spacer(modifier = Modifier.weight(1f))
                    UIButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .padding(10.dp),
                        text = "Logout", buttonColor = buttonColorState,
                        textColor = buttonTextColorState
                    ) {
                        CoroutineScope(Dispatchers.IO).launch {
                            DataStoreManager.removeKey(
                                DataStoreManager.DataStoreKeys.CURRENT_USER_ID.key
                            )
                        }
                        SettingsModel.currentUser = null
                        SettingsModel.currentUserId = null
                        navController?.clearBackStack("LoginView")
                        navController?.navigate("LoginView")
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = isColorPickerShown, enter = fadeIn(
                initialAlpha = 0.4f
            ), exit = fadeOut(
                animationSpec = tween(durationMillis = 250)
            )
        ) {
            Dialog(
                onDismissRequest = { isColorPickerShown = false },
            ) {
                ColorPickerPopup(defaultColor = when (colorPickerType) {
                    ColorPickerType.BUTTON_COLOR -> buttonColorState
                    ColorPickerType.BUTTON_TEXT_COLOR -> buttonTextColorState
                    ColorPickerType.BACKGROUND_COLOR -> backgroundColorState
                    ColorPickerType.TOP_BAR_COLOR -> topBarColorState
                    ColorPickerType.TEXT_COLOR -> textColorState
                }, onDismiss = { isColorPickerShown = false }, onSubmit = {
                    when (colorPickerType) {
                        ColorPickerType.BUTTON_COLOR -> {
                            buttonColorState = it
                            SettingsModel.buttonColor = it
                            CoroutineScope(Dispatchers.IO).launch {
                                DataStoreManager.putString(
                                    DataStoreManager.DataStoreKeys.BUTTON_COLOR.key, it.toHexCode()
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

                        ColorPickerType.TOP_BAR_COLOR -> {
                            topBarColorState = it
                            SettingsModel.topBarColor = it
                            CoroutineScope(Dispatchers.IO).launch {
                                DataStoreManager.putString(
                                    DataStoreManager.DataStoreKeys.TOP_BAR_COLOR.key, it.toHexCode()
                                )
                            }
                        }

                        ColorPickerType.TEXT_COLOR -> {
                            textColorState = it
                            SettingsModel.textColor = it
                            CoroutineScope(Dispatchers.IO).launch {
                                DataStoreManager.putString(
                                    DataStoreManager.DataStoreKeys.TEXT_COLOR.key, it.toHexCode()
                                )
                            }
                        }
                    }
                    isColorPickerShown = false
                })

            }
        }
    }
}

enum class ColorPickerType(val key: String) {
    BUTTON_COLOR("BUTTON_COLOR"), BUTTON_TEXT_COLOR("BUTTON_TEXT_COLOR"), TOP_BAR_COLOR(
        "TOP_BAR_COLOR"
    ),
    BACKGROUND_COLOR("BACKGROUND_COLOR"), TEXT_COLOR("TEXT_COLOR")
}