package com.grid.pos.ui.settings

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.grid.pos.ActivityScopedViewModel
import com.grid.pos.App
import com.grid.pos.data.Company.Company
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.ColorPickerPopup
import com.grid.pos.ui.common.LoadingIndicator
import com.grid.pos.ui.common.SearchableDropdownMenu
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.common.UISwitch
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.ui.theme.LightGrey
import com.grid.pos.utils.DataStoreManager
import com.grid.pos.utils.Extension.toHexCode
import com.grid.pos.utils.FileUtils
import com.grid.pos.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
        modifier: Modifier = Modifier,
        navController: NavController? = null,
        activityScopedViewModel: ActivityScopedViewModel
) {
    var firebaseApplicationId by remember {
        mutableStateOf(
            SettingsModel.firebaseApplicationId ?: ""
        )
    }
    var connectionTypeState by remember { mutableStateOf(SettingsModel.connectionType) }
    var firebaseApiKey by remember { mutableStateOf(SettingsModel.firebaseApiKey ?: "") }
    var firebaseProjectId by remember { mutableStateOf(SettingsModel.firebaseProjectId ?: "") }
    var firebaseDbPath by remember { mutableStateOf(SettingsModel.firebaseDbPath ?: "") }
    var fireStoreCompanyID by remember { mutableStateOf(SettingsModel.fireStoreCompanyID ?: "") }
    var localCompanyID by remember { mutableStateOf(SettingsModel.localCompanyID ?: "") }
    var localCompanyName by remember { mutableStateOf("") }
    var sqlServerPath by remember { mutableStateOf(SettingsModel.sqlServerPath ?: "") }
    var sqlServerDbUser by remember { mutableStateOf(SettingsModel.sqlServerDbUser ?: "") }
    var sqlServerDbPassword by remember { mutableStateOf(SettingsModel.sqlServerDbPassword ?: "") }
    var passwordVisibility by remember { mutableStateOf(false) }

    val companies = remember { mutableStateListOf<Company>() }

    val firebaseApiKeyRequester = remember { FocusRequester() }
    val firebaseProjectIdRequester = remember { FocusRequester() }
    val firebaseDbPathRequester = remember { FocusRequester() }
    val companyIdRequester = remember { FocusRequester() }
    val sqlServerUserRequester = remember { FocusRequester() }
    val sqlServerPasswordRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

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

    var isLoading by remember { mutableStateOf(false) }
    val isLoggedId = activityScopedViewModel.isLoggedIn()

    fun handleBack() {
        navController?.navigateUp()
    }
    BackHandler {
        handleBack()
    }

    LaunchedEffect(Unit) {
        if (companies.isEmpty()) {
            activityScopedViewModel.getLocalCompanies { comps ->
                companies.addAll(comps)
                val selected = comps.firstOrNull { it.companyId.equals(localCompanyID) }
                localCompanyName = selected?.companyName ?: ""
            }
        }
    }

    GridPOSTheme {
        Scaffold(containerColor = SettingsModel.backgroundColor,
            topBar = {
                Surface(
                    shadowElevation = 3.dp,
                    color = backgroundColorState
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
                                text = "Settings",
                                color = textColorState,
                                fontSize = 16.sp,
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
                        shape = RoundedCornerShape(15.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = LightGrey,
                        )
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clickable {
                                    isFirebaseSectionExpanded = !isFirebaseSectionExpanded
                                },
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Connectivity Settings",
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    textDecoration = TextDecoration.None,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                ),
                                color = SettingsModel.textColor
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                null,
                                Modifier
                                    .padding(16.dp)
                                    .size(20.dp)
                                    .align(
                                        Alignment.CenterVertically
                                    )
                                    .rotate(
                                        if (isFirebaseSectionExpanded) 180f else 0f
                                    ),
                                tint = Color.Black
                            )
                        }

                        if (isFirebaseSectionExpanded) {
                            SearchableDropdownMenu(
                                items = Utils.connections,
                                modifier = Modifier.padding(10.dp),
                                enableSearch = false,
                                color = LightGrey,
                                label = connectionTypeState.ifEmpty { "Select Type" },
                            ) { type ->
                                connectionTypeState = type.getName()
                            }
                            if (connectionTypeState == CONNECTION_TYPE.FIRESTORE.key) {
                                UITextField(modifier = Modifier.padding(10.dp),
                                    defaultValue = firebaseApplicationId,
                                    label = "Application ID",
                                    placeHolder = "Application ID",
                                    onAction = { firebaseApiKeyRequester.requestFocus() }) { appId ->
                                    firebaseApplicationId = appId
                                }

                                UITextField(modifier = Modifier.padding(10.dp),
                                    defaultValue = firebaseApiKey,
                                    label = "Api Key",
                                    placeHolder = "Api Key",
                                    focusRequester = firebaseApiKeyRequester,
                                    onAction = { firebaseProjectIdRequester.requestFocus() }) { apiKey ->
                                    firebaseApiKey = apiKey
                                }

                                UITextField(modifier = Modifier.padding(10.dp),
                                    defaultValue = firebaseProjectId,
                                    label = "Project ID",
                                    placeHolder = "Project ID",
                                    focusRequester = firebaseProjectIdRequester,
                                    onAction = { firebaseDbPathRequester.requestFocus() }) { projectID ->
                                    firebaseProjectId = projectID
                                }

                                UITextField(modifier = Modifier.padding(10.dp),
                                    defaultValue = firebaseDbPath,
                                    label = "Database Path",
                                    placeHolder = "Database Path",
                                    focusRequester = firebaseDbPathRequester,
                                    onAction = { companyIdRequester.requestFocus() }) { dbPath ->
                                    firebaseDbPath = dbPath
                                }

                                UITextField(modifier = Modifier.padding(10.dp),
                                    defaultValue = fireStoreCompanyID,
                                    label = "Company ID",
                                    placeHolder = "Company ID",
                                    focusRequester = companyIdRequester,
                                    onAction = { keyboardController?.hide() }) { compId ->
                                    fireStoreCompanyID = compId
                                }
                            } else if (connectionTypeState == CONNECTION_TYPE.SQL_SERVER.key) {
                                UITextField(modifier = Modifier.padding(10.dp),
                                    defaultValue = sqlServerPath,
                                    label = "SQL Server Path",
                                    placeHolder = "host:port/dbname",
                                    imeAction = ImeAction.Next,
                                    onAction = { sqlServerUserRequester.requestFocus() }) { path ->
                                    sqlServerPath = path
                                }

                                UITextField(modifier = Modifier.padding(10.dp),
                                    defaultValue = sqlServerDbUser,
                                    label = "Database User",
                                    placeHolder = "user",
                                    focusRequester = sqlServerUserRequester,
                                    imeAction = ImeAction.Next,
                                    onAction = { sqlServerPasswordRequester.requestFocus() }) { user ->
                                    sqlServerDbUser = user
                                }

                                UITextField(modifier = Modifier.padding(10.dp),
                                    defaultValue = sqlServerDbPassword,
                                    label = "Database Password",
                                    placeHolder = "password",
                                    focusRequester = sqlServerUserRequester,
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done,
                                    visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                                    onAction = {
                                        keyboardController?.hide()
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                                            Icon(
                                                imageVector = if (passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                                contentDescription = if (passwordVisibility) "Hide password" else "Show password",
                                                tint = SettingsModel.buttonColor
                                            )
                                        }
                                    }) { password ->
                                    sqlServerDbPassword = password
                                }
                            } else {
                                SearchableDropdownMenu(items = companies.toMutableList(),
                                    modifier = Modifier.padding(10.dp),
                                    enableSearch = false,
                                    color = LightGrey,
                                    label = localCompanyName.ifEmpty { "Select Company" }) { company ->
                                    company as Company
                                    localCompanyID = company.companyId
                                }
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
                                    ),
                                text = "Save"
                            ) {
                                isLoading = true
                                CoroutineScope(Dispatchers.IO).launch {
                                    SettingsModel.connectionType = connectionTypeState
                                    DataStoreManager.putString(
                                        DataStoreManager.DataStoreKeys.CONNECTION_TYPE.key,
                                        connectionTypeState
                                    )
                                    when (connectionTypeState) {
                                        CONNECTION_TYPE.FIRESTORE.key -> {
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
                                            SettingsModel.fireStoreCompanyID = fireStoreCompanyID
                                            DataStoreManager.putString(
                                                DataStoreManager.DataStoreKeys.FIRESTORE_COMPANY_ID.key,
                                                fireStoreCompanyID
                                            )

                                            if (SettingsModel.isConnectedToFireStore()) {
                                                App.getInstance().initFirebase()
                                            }
                                        }

                                        CONNECTION_TYPE.SQL_SERVER.key -> {
                                            SettingsModel.sqlServerPath = sqlServerPath
                                            DataStoreManager.putString(
                                                DataStoreManager.DataStoreKeys.SQL_SERVER_PATH.key,
                                                sqlServerPath
                                            )
                                            SettingsModel.sqlServerDbUser = sqlServerDbUser
                                            DataStoreManager.putString(
                                                DataStoreManager.DataStoreKeys.SQL_SERVER_DB_USER.key,
                                                sqlServerDbUser
                                            )
                                            SettingsModel.sqlServerDbPassword = sqlServerDbPassword
                                            DataStoreManager.putString(
                                                DataStoreManager.DataStoreKeys.SQL_SERVER__DB_PASSWORD.key,
                                                sqlServerDbPassword
                                            )
                                        }

                                        CONNECTION_TYPE.LOCAL.key -> {
                                            SettingsModel.localCompanyID = localCompanyID
                                            DataStoreManager.putString(
                                                DataStoreManager.DataStoreKeys.LOCAL_COMPANY_ID.key,
                                                localCompanyID
                                            )
                                        }
                                    }
                                    delay(1000L)
                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                    }
                                }
                            }
                        }
                    }
                }
                Card(
                    modifier = Modifier
                        .padding(10.dp)
                        .animateContentSize(),
                    shape = RoundedCornerShape(15.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = LightGrey,
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clickable {
                                isAppSettingsSectionExpanded = !isAppSettingsSectionExpanded
                            },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "App Settings",
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                textDecoration = TextDecoration.None,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            color = SettingsModel.textColor
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            null,
                            Modifier
                                .padding(horizontal = 16.dp)
                                .size(20.dp)
                                .align(
                                    Alignment.CenterVertically
                                )
                                .rotate(
                                    if (isAppSettingsSectionExpanded) 180f else 0f
                                ),
                            tint = Color.Black
                        )
                    }

                    if (isAppSettingsSectionExpanded) {
                        UISwitch(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .padding(10.dp),
                            checked = showTax,
                            text = "Show Tax",
                            textColor = textColorState
                        ) { showTx ->
                            showTax = showTx
                            SettingsModel.showTax = showTx
                            CoroutineScope(Dispatchers.IO).launch {
                                DataStoreManager.putBoolean(
                                    DataStoreManager.DataStoreKeys.SHOW_TAX.key,
                                    showTx
                                )
                            }
                        }

                        UISwitch(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .padding(10.dp),
                            checked = showTax1,
                            text = "Show Tax1",
                            textColor = textColorState
                        ) { showTx1 ->
                            showTax1 = showTx1
                            SettingsModel.showTax1 = showTx1
                            CoroutineScope(Dispatchers.IO).launch {
                                DataStoreManager.putBoolean(
                                    DataStoreManager.DataStoreKeys.SHOW_TAX1.key,
                                    showTx1
                                )
                            }
                        }

                        UISwitch(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .padding(10.dp),
                            checked = showTax2,
                            text = "Show Tax2",
                            textColor = textColorState
                        ) { showTx2 ->
                            showTax2 = showTx2
                            SettingsModel.showTax2 = showTx2
                            CoroutineScope(Dispatchers.IO).launch {
                                DataStoreManager.putBoolean(
                                    DataStoreManager.DataStoreKeys.SHOW_TAX2.key,
                                    showTx2
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
                    shape = RoundedCornerShape(15.dp),
                    colors = CardDefaults.cardColors(
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
                            text = "Colors",
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                textDecoration = TextDecoration.None,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            color = SettingsModel.textColor
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            null,
                            Modifier
                                .padding(16.dp)
                                .size(20.dp)
                                .align(
                                    Alignment.CenterVertically
                                )
                                .rotate(
                                    if (isColorsSectionExpanded) 180f else 0f
                                ),
                            tint = Color.Black
                        )
                    }
                    if (isColorsSectionExpanded) {
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
                            text = "Top Bar Color",
                            buttonColor = buttonColorState,
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
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                UIButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(10.dp),
                    text = "App Settings",
                    buttonColor = buttonColorState,
                    textColor = buttonTextColorState
                ) {
                    activityScopedViewModel.openAppStorageSettings()
                }
                UIButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(10.dp),
                    text = "Backup",
                    buttonColor = buttonColorState,
                    textColor = buttonTextColorState
                ) {
                    navController?.navigate("BackupView")
                }
                if (isLoggedId) {
                    UIButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .padding(10.dp),
                        text = "Logout",
                        buttonColor = buttonColorState,
                        textColor = buttonTextColorState
                    ) {
                        CoroutineScope(Dispatchers.IO).launch {
                            DataStoreManager.removeKey(
                                DataStoreManager.DataStoreKeys.CURRENT_USER_ID.key
                            )
                        }
                        activityScopedViewModel.logout()
                        navController?.clearBackStack("LoginView")
                        navController?.navigate("LoginView")
                    }
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
                    ColorPickerType.BUTTON_COLOR -> buttonColorState
                    ColorPickerType.BUTTON_TEXT_COLOR -> buttonTextColorState
                    ColorPickerType.BACKGROUND_COLOR -> backgroundColorState
                    ColorPickerType.TOP_BAR_COLOR -> topBarColorState
                    ColorPickerType.TEXT_COLOR -> textColorState
                },
                    onDismiss = { isColorPickerShown = false },
                    onSubmit = {
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

                            ColorPickerType.TOP_BAR_COLOR -> {
                                topBarColorState = it
                                SettingsModel.topBarColor = it
                                CoroutineScope(Dispatchers.IO).launch {
                                    DataStoreManager.putString(
                                        DataStoreManager.DataStoreKeys.TOP_BAR_COLOR.key,
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
                    })

            }
        }
        LoadingIndicator(
            show = isLoading
        )
    }
}

enum class ColorPickerType(val key: String) {
    BUTTON_COLOR("BUTTON_COLOR"), BUTTON_TEXT_COLOR("BUTTON_TEXT_COLOR"), TOP_BAR_COLOR(
        "TOP_BAR_COLOR"
    ),
    BACKGROUND_COLOR("BACKGROUND_COLOR"), TEXT_COLOR("TEXT_COLOR")
}