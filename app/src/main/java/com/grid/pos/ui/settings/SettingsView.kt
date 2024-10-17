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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.RemoveCircleOutline
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
import androidx.compose.ui.res.painterResource
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.grid.pos.ActivityScopedViewModel
import com.grid.pos.App
import com.grid.pos.R
import com.grid.pos.data.Company.Company
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.Language
import com.grid.pos.model.PopupModel
import com.grid.pos.model.ReportCountry
import com.grid.pos.model.ReportLanguage
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.ColorPickerPopup
import com.grid.pos.ui.common.SearchableDropdownMenuEx
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.common.UISwitch
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.reports.SalesReportsViewModel
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.ui.theme.LightGrey
import com.grid.pos.utils.DataStoreManager
import com.grid.pos.utils.Extension.toHexCode
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
        activityScopedViewModel: ActivityScopedViewModel,
        viewModel: SettingsViewModel = hiltViewModel()
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
    var sqlServerName by remember { mutableStateOf(SettingsModel.sqlServerName ?: "") }
    var sqlServerDbName by remember { mutableStateOf(SettingsModel.sqlServerDbName ?: "") }
    var sqlServerDbUser by remember { mutableStateOf(SettingsModel.sqlServerDbUser ?: "") }
    var sqlServerDbPassword by remember { mutableStateOf(SettingsModel.sqlServerDbPassword ?: "") }
    var sqlServerCompanyId by remember { mutableStateOf(SettingsModel.sqlServerCompanyId ?: "") }
    var isSqlServerWebDb by remember { mutableStateOf(SettingsModel.isSqlServerWebDb) }
    var passwordVisibility by remember { mutableStateOf(false) }

    val companies = remember { mutableStateListOf<Company>() }

    val firebaseApiKeyRequester = remember { FocusRequester() }
    val firebaseProjectIdRequester = remember { FocusRequester() }
    val firebaseDbPathRequester = remember { FocusRequester() }
    val companyIdRequester = remember { FocusRequester() }
    val sqlServerNameRequester = remember { FocusRequester() }
    val sqlServerDbNameRequester = remember { FocusRequester() }
    val sqlServerUserRequester = remember { FocusRequester() }
    val sqlServerPasswordRequester = remember { FocusRequester() }
    val sqlServerCmpIdRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var orientationType by remember { mutableStateOf(SettingsModel.orientationType) }
    var defaultReportCountry by remember { mutableStateOf(SettingsModel.defaultReportCountry) }
    var defaultReportLanguage by remember { mutableStateOf(SettingsModel.defaultReportLanguage) }
    var cashPrinterState by remember { mutableStateOf(SettingsModel.cashPrinter ?: "") }
    var showItemsInPOS by remember { mutableStateOf(SettingsModel.showItemsInPOS) }
    var showTax by remember { mutableStateOf(SettingsModel.showTax) }
    var showTax1 by remember { mutableStateOf(SettingsModel.showTax1) }
    var showTax2 by remember { mutableStateOf(SettingsModel.showTax2) }
    var showPriceInItemBtn by remember { mutableStateOf(SettingsModel.showPriceInItemBtn) }
    var autoPrintTickets by remember { mutableStateOf(SettingsModel.autoPrintTickets) }
    var showItemQtyAlert by remember { mutableStateOf(SettingsModel.showItemQtyAlert) }

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
            viewModel.fetchLocalCompanies { comps ->
                companies.addAll(comps)
                val selected = comps.firstOrNull { it.companyId.equals(localCompanyID) }
                localCompanyName = selected?.companyName ?: ""
            }
        }
    }
    LaunchedEffect(isLoading) {
        activityScopedViewModel.showLoading(isLoading)
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
                            SearchableDropdownMenuEx(items = Utils.connections,
                                modifier = Modifier.padding(10.dp),
                                enableSearch = false,
                                color = LightGrey,
                                placeholder = "Database",
                                label = connectionTypeState.ifEmpty { "Select Database" },
                                leadingIcon = {
                                    if (connectionTypeState == CONNECTION_TYPE.SQL_SERVER.key) {
                                        Icon(
                                            modifier = it.size(20.dp),
                                            painter = painterResource(R.drawable.refresh),
                                            contentDescription = "check connectivity",
                                            tint = Color.Black,
                                        )
                                    }
                                },
                                onLeadingIconClick = {
                                    isLoading = true
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val message = SQLServerWrapper.isConnectionSucceeded(
                                            sqlServerPath,
                                            sqlServerDbName,
                                            sqlServerName,
                                            sqlServerDbUser,
                                            sqlServerDbPassword
                                        )
                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                            activityScopedViewModel.showPopup(true,
                                                PopupModel().apply {
                                                    positiveBtnText = "Close"
                                                    negativeBtnText = null
                                                    dialogText = message
                                                })
                                        }
                                    }
                                }) { type ->
                                connectionTypeState = type.getName()
                            }
                            when (connectionTypeState) {
                                CONNECTION_TYPE.FIRESTORE.key -> {
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
                                }

                                CONNECTION_TYPE.SQL_SERVER.key -> {
                                    UITextField(modifier = Modifier.padding(10.dp),
                                        defaultValue = sqlServerPath,
                                        label = "SQL Server Path",
                                        placeHolder = "host:port",
                                        imeAction = ImeAction.Next,
                                        onAction = { sqlServerNameRequester.requestFocus() }) { path ->
                                        sqlServerPath = path
                                    }

                                    UITextField(modifier = Modifier.padding(10.dp),
                                        defaultValue = sqlServerName,
                                        label = "Sql Server Name",
                                        placeHolder = "Sql Server Name",
                                        focusRequester = sqlServerNameRequester,
                                        imeAction = ImeAction.Next,
                                        onAction = { sqlServerDbNameRequester.requestFocus() }) { name ->
                                        sqlServerName = name
                                    }
                                    UITextField(modifier = Modifier.padding(10.dp),
                                        defaultValue = sqlServerDbName,
                                        label = "Database Name",
                                        placeHolder = "Database Name",
                                        focusRequester = sqlServerDbNameRequester,
                                        imeAction = ImeAction.Next,
                                        onAction = { sqlServerUserRequester.requestFocus() }) { dbName ->
                                        sqlServerDbName = dbName
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
                                        imeAction = ImeAction.Next,
                                        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                                        onAction = {
                                            sqlServerCmpIdRequester.requestFocus()
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

                                    UITextField(modifier = Modifier.padding(10.dp),
                                        defaultValue = sqlServerCompanyId,
                                        label = "Company ID",
                                        placeHolder = "Company ID",
                                        focusRequester = sqlServerCmpIdRequester,
                                        imeAction = ImeAction.Done,
                                        onAction = { keyboardController?.hide() }) { compId ->
                                        sqlServerCompanyId = compId
                                    }

                                    UISwitch(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(60.dp)
                                            .padding(10.dp),
                                        checked = isSqlServerWebDb,
                                        text = "Sql Server Web Db",
                                        textColor = textColorState
                                    ) { isWebDb ->
                                        isSqlServerWebDb = isWebDb
                                    }
                                }

                                else -> {
                                    SearchableDropdownMenuEx(items = companies.toMutableList(),
                                        modifier = Modifier.padding(10.dp),
                                        enableSearch = false,
                                        color = LightGrey,
                                        placeholder = "Selected Company",
                                        label = localCompanyName.ifEmpty { "Select Company" }) { company ->
                                        company as Company
                                        localCompanyID = company.companyId
                                    }
                                }
                            }

                            UITextField(modifier = Modifier.padding(10.dp),
                                defaultValue = cashPrinterState,
                                label = "Cash Printer",
                                placeHolder = "ex. 127.0.0.1:9100",
                                focusRequester = sqlServerCmpIdRequester,
                                imeAction = ImeAction.Done,
                                onAction = { keyboardController?.hide() }) { cashPrinter ->
                                cashPrinterState = cashPrinter
                            }

                            UIButton(
                                modifier = Modifier
                                    .fillMaxWidth()
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
                                    if(SettingsModel.connectionType == CONNECTION_TYPE.SQL_SERVER.key){
                                        SQLServerWrapper.closeConnection()
                                    }
                                    SettingsModel.connectionType = connectionTypeState
                                    DataStoreManager.putString(
                                        DataStoreManager.DataStoreKeys.CONNECTION_TYPE.key,
                                        connectionTypeState
                                    )
                                    SettingsModel.cashPrinter = cashPrinterState
                                    DataStoreManager.putString(
                                        DataStoreManager.DataStoreKeys.CASH_PRINTER.key,
                                        cashPrinterState
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
                                            SettingsModel.sqlServerName = sqlServerName
                                            DataStoreManager.putString(
                                                DataStoreManager.DataStoreKeys.SQL_SERVER_NAME.key,
                                                sqlServerName
                                            )
                                            SettingsModel.sqlServerDbName = sqlServerDbName
                                            DataStoreManager.putString(
                                                DataStoreManager.DataStoreKeys.SQL_SERVER_DB_NAME.key,
                                                sqlServerDbName
                                            )
                                            SettingsModel.sqlServerDbUser = sqlServerDbUser
                                            DataStoreManager.putString(
                                                DataStoreManager.DataStoreKeys.SQL_SERVER_DB_USER.key,
                                                sqlServerDbUser
                                            )
                                            SettingsModel.sqlServerDbPassword = sqlServerDbPassword
                                            DataStoreManager.putString(
                                                DataStoreManager.DataStoreKeys.SQL_SERVER_DB_PASSWORD.key,
                                                sqlServerDbPassword
                                            )

                                            SettingsModel.sqlServerCompanyId = sqlServerCompanyId
                                            DataStoreManager.putString(
                                                DataStoreManager.DataStoreKeys.SQL_SERVER_COMPANY_ID.key,
                                                sqlServerCompanyId
                                            )

                                            SettingsModel.isSqlServerWebDb = isSqlServerWebDb
                                            DataStoreManager.putBoolean(
                                                DataStoreManager.DataStoreKeys.IS_SQL_SERVER_WEB_DB.key,
                                                isSqlServerWebDb
                                            )
                                            if(SettingsModel.connectionType == CONNECTION_TYPE.SQL_SERVER.key){
                                                SQLServerWrapper.openConnection()
                                            }
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
                        SearchableDropdownMenuEx(
                            items = Utils.orientations,
                            modifier = Modifier.padding(10.dp),
                            enableSearch = false,
                            color = LightGrey,
                            placeholder = "Orientation",
                            label = orientationType.ifEmpty { "Select Type" },
                        ) { type ->
                            orientationType = type.getName()
                            SettingsModel.orientationType = orientationType
                            CoroutineScope(Dispatchers.IO).launch {
                                DataStoreManager.putString(
                                    DataStoreManager.DataStoreKeys.ORIENTATION_TYPE.key,
                                    orientationType
                                )
                                activityScopedViewModel.changeAppOrientation(orientationType)
                            }
                        }

                        SearchableDropdownMenuEx(
                            items = Utils.getReportCountry().toMutableList(),
                            modifier = Modifier.padding(10.dp),
                            color = LightGrey,
                            placeholder = "Report Language",
                            label = defaultReportCountry,
                        ) { country ->
                            country as ReportCountry
                            defaultReportCountry = country.getName()
                            SettingsModel.defaultReportCountry = defaultReportCountry
                            CoroutineScope(Dispatchers.IO).launch {
                                DataStoreManager.putString(
                                    DataStoreManager.DataStoreKeys.REPORT_COUNTRY.key,
                                    defaultReportCountry
                                )
                            }
                        }

                        SearchableDropdownMenuEx(
                            items = Utils.getReportLanguages(false).toMutableList(),
                            modifier = Modifier.padding(10.dp),
                            color = LightGrey,
                            placeholder = "Report Language",
                            label = defaultReportLanguage,
                        ) { lang ->
                            lang as ReportLanguage
                            defaultReportLanguage = lang.getName()
                            SettingsModel.defaultReportLanguage = defaultReportLanguage
                            CoroutineScope(Dispatchers.IO).launch {
                                DataStoreManager.putString(
                                    DataStoreManager.DataStoreKeys.REPORT_LANGUAGE.key,
                                    defaultReportLanguage
                                )
                            }
                        }

                        UISwitch(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .padding(10.dp),
                            checked = showItemsInPOS,
                            text = "Show Items in POS",
                            textColor = textColorState
                        ) { showitems ->
                            showItemsInPOS = showitems
                            SettingsModel.showItemsInPOS = showItemsInPOS
                            CoroutineScope(Dispatchers.IO).launch {
                                DataStoreManager.putBoolean(
                                    DataStoreManager.DataStoreKeys.SHOW_ITEMS_IN_POS.key,
                                    showItemsInPOS
                                )
                            }
                        }

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

                        UISwitch(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .padding(10.dp),
                            checked = autoPrintTickets,
                            text = "Auto print tickets",
                            textColor = textColorState
                        ) { autoPrint ->
                            autoPrintTickets = autoPrint
                            SettingsModel.autoPrintTickets = autoPrint
                            CoroutineScope(Dispatchers.IO).launch {
                                DataStoreManager.putBoolean(
                                    DataStoreManager.DataStoreKeys.AUTO_PRINT_TICKETS.key,
                                    autoPrintTickets
                                )
                            }
                        }

                        UISwitch(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .padding(10.dp),
                            checked = showItemQtyAlert,
                            text = "Show Quantity Alert",
                            textColor = textColorState
                        ) { showAlert ->
                            showItemQtyAlert = showAlert
                            SettingsModel.showItemQtyAlert = showAlert
                            CoroutineScope(Dispatchers.IO).launch {
                                DataStoreManager.putBoolean(
                                    DataStoreManager.DataStoreKeys.SHOW_ITEM_QTY_ALERT.key,
                                    showItemQtyAlert
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                ) {
                    UIButton(
                        modifier = Modifier
                            .height(70.dp)
                            .weight(1f)
                            .padding(3.dp),
                        text = "Setup Reports",
                        buttonColor = buttonColorState,
                        textColor = buttonTextColorState
                    ) {
                        navController?.navigate("ReportsListView")
                    }
                    UIButton(
                        modifier = Modifier
                            .height(70.dp)
                            .weight(1f)
                            .padding(3.dp),
                        text = "Backup",
                        buttonColor = buttonColorState,
                        textColor = buttonTextColorState
                    ) {
                        navController?.navigate("BackupView")
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                ) {

                    UIButton(
                        modifier = Modifier
                            .height(70.dp)
                            .weight(1f)
                            .padding(3.dp),
                        text = "License",
                        buttonColor = buttonColorState,
                        textColor = buttonTextColorState
                    ) {
                        navController?.navigate("LicenseView")
                    }

                    if (isLoggedId) {
                        UIButton(
                            modifier = Modifier
                                .height(70.dp)
                                .weight(1f)
                                .padding(3.dp),
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
    }
}

enum class ColorPickerType(val key: String) {
    BUTTON_COLOR("BUTTON_COLOR"), BUTTON_TEXT_COLOR("BUTTON_TEXT_COLOR"), TOP_BAR_COLOR(
        "TOP_BAR_COLOR"
    ),
    BACKGROUND_COLOR("BACKGROUND_COLOR"), TEXT_COLOR("TEXT_COLOR")
}