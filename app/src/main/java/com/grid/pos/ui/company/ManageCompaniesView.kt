package com.grid.pos.ui.company

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.grid.pos.ActivityScopedViewModel
import com.grid.pos.R
import com.grid.pos.data.Company.Company
import com.grid.pos.interfaces.OnGalleryResult
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.SearchableDropdownMenuEx
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.common.UIImageButton
import com.grid.pos.ui.common.UISwitch
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.FileUtils
import com.grid.pos.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCompaniesView(
        modifier: Modifier = Modifier,
        navController: NavController? = null,
        activityScopedViewModel: ActivityScopedViewModel,
        viewModel: ManageCompaniesViewModel = hiltViewModel()
) {
    val state by viewModel.manageCompaniesState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val phoneFocusRequester = remember { FocusRequester() }
    val addressFocusRequester = remember { FocusRequester() }
    val countryFocusRequester = remember { FocusRequester() }
    val taxRegNoFocusRequester = remember { FocusRequester() }
    val taxFocusRequester = remember { FocusRequester() }
    val emailFocusRequester = remember { FocusRequester() }
    val webFocusRequester = remember { FocusRequester() }
    val logoFocusRequester = remember { FocusRequester() }
    val tax1FocusRequester = remember { FocusRequester() }
    val tax1RegNoFocusRequester = remember { FocusRequester() }
    val tax2FocusRequester = remember { FocusRequester() }
    val tax2RegNoFocusRequester = remember { FocusRequester() }

    var nameState by remember { mutableStateOf("") }
    var phoneState by remember { mutableStateOf("") }
    var addressState by remember { mutableStateOf("") }
    var countryState by remember { mutableStateOf("") }
    var taxRegnoState by remember { mutableStateOf("") }
    var taxState by remember { mutableStateOf("") }
    var upWithTaxState by remember { mutableStateOf(false) }
    //var printerState by remember { mutableStateOf("") }
    var emailState by remember { mutableStateOf("") }
    var webState by remember { mutableStateOf("") }
    var logoState by remember { mutableStateOf("") }
    var tax1State by remember { mutableStateOf("") }
    var tax1RegnoState by remember { mutableStateOf("") }
    var tax2State by remember { mutableStateOf("") }
    var tax2RegnoState by remember { mutableStateOf("") }

    var oldImage: String? = null

    LaunchedEffect(/*activityScopedViewModel.companies,*/
        activityScopedViewModel.currencies
    ) {
        viewModel.fillCachedCompanies(
            state.companies /*activityScopedViewModel.companies*/,
            activityScopedViewModel.currencies
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(state.warning) {
        state.warning?.value?.let { message ->
            scope.launch {
                if (state.actionLabel == "next" && activityScopedViewModel.isRegistering) {
                    activityScopedViewModel.showPopup(
                        true,
                        PopupModel(
                            onDismissRequest = {
                                activityScopedViewModel.isRegistering = false
                                navController?.navigateUp()
                            },
                            onConfirmation = {
                                navController?.navigateUp()
                                navController?.navigate("ManageUsersView")
                            },
                            dialogText = message,
                            positiveBtnText = "Continue",
                            negativeBtnText = "Close",
                            cancelable = false
                        )
                    )
                } else {
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
    }

    LaunchedEffect(state.isLoading) {
        activityScopedViewModel.showLoading(state.isLoading)
    }

    fun saveCompany() {
        oldImage?.let { old ->
            FileUtils.deleteFile(
                context,
                old
            )
        }
        val firstCurr = state.currencies.firstOrNull()
        state.selectedCompany.companyCurCodeTax = firstCurr?.currencyId
        viewModel.saveCompany(
            state.selectedCompany,
            activityScopedViewModel.isRegistering
        )
    }

    fun clear() {
        viewModel.currentCompany = Company()
        state.selectedCompany = Company()
        nameState = ""
        phoneState = ""
        addressState = ""
        countryState = ""
        taxRegnoState = ""
        taxState = ""
        upWithTaxState = false
        emailState = ""
        webState = ""
        logoState = ""
        tax1RegnoState = ""
        tax1State = ""
        tax2RegnoState = ""
        tax2State = ""
        viewModel.resetState()
    }

    var saveAndBack by remember { mutableStateOf(false) }
    fun handleBack() {
        if (state.isLoading) {
            return
        }
        if (state.selectedCompany.didChanged(
                viewModel.currentCompany
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
                        saveCompany()
                    }
                    dialogTitle = null
                    dialogText = "Do you want to save your changes"
                    positiveBtnText = "Save"
                    negativeBtnText = "Close"
                    icon = null
                })
            return
        }
        if (state.companies.isNotEmpty()) {
            activityScopedViewModel.companies = state.companies
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
                                text = "Manage Companies",
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
                        .fillMaxSize()
                        .padding(top = 90.dp),
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
                        onAction = { phoneFocusRequester.requestFocus() }) { name ->
                        nameState = name
                        state.selectedCompany.companyName = name
                    }

                    //phone
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = phoneState,
                        label = "Phone",
                        focusRequester = phoneFocusRequester,
                        placeHolder = "Enter Phone",
                        onAction = { addressFocusRequester.requestFocus() }) { phone ->
                        phoneState = phone
                        state.selectedCompany.companyPhone = phone
                    }

                    //address
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = addressState,
                        label = "Address",
                        maxLines = 3,
                        focusRequester = addressFocusRequester,
                        placeHolder = "Enter address",
                        onAction = {
                            countryFocusRequester.requestFocus()
                        }) { address ->
                        addressState = address
                        state.selectedCompany.companyAddress = address
                    }

                    //country
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = countryState,
                        label = "Country",
                        maxLines = 3,
                        focusRequester = countryFocusRequester,
                        placeHolder = "Enter country",
                        onAction = {
                            if (SettingsModel.showTax) {
                                taxRegNoFocusRequester.requestFocus()
                            } else if (SettingsModel.showTax1) {
                                tax1RegNoFocusRequester.requestFocus()
                            } else if (SettingsModel.showTax2) {
                                tax2RegNoFocusRequester.requestFocus()
                            } else {
                                emailFocusRequester.requestFocus()
                            }
                        }) { country ->
                        countryState = country
                        state.selectedCompany.companyCountry = countryState
                    }

                    /*SearchableDropdownMenuEx(
                        items = manageCompaniesState.printers.toMutableList(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        label = "Select Printer",
                        selectedId = printerState
                    ) { printer ->
                        printer as PosPrinter
                        printerState = printer.posPrinterId
                        manageCompaniesState.selectedCompany.companyPrinterId = printerState
                    }*/

                    //email
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = emailState,
                        label = "Email Address",
                        placeHolder = "Enter Email Address",
                        focusRequester = emailFocusRequester,
                        onAction = { webFocusRequester.requestFocus() }) { email ->
                        emailState = email
                        state.selectedCompany.companyEmail = email
                    }

                    //web
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = webState,
                        label = "Website",
                        placeHolder = "Enter Website",
                        focusRequester = webFocusRequester,
                        onAction = { logoFocusRequester.requestFocus() }) { web ->
                        webState = web
                        state.selectedCompany.companyWeb = web
                    }

                    //logo
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = logoState,
                        label = "Logo",
                        placeHolder = "Enter Logo",
                        focusRequester = logoFocusRequester,
                        imeAction = ImeAction.Done,
                        onAction = { keyboardController?.hide() },
                        trailingIcon = {
                            IconButton(onClick = {
                                activityScopedViewModel.launchGalleryPicker(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
                                    object : OnGalleryResult {
                                        override fun onGalleryResult(uris: List<Uri>) {
                                            if (uris.isNotEmpty()) {
                                                activityScopedViewModel.showLoading(true)
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    val internalPath = FileUtils.saveToExternalStorage(context = context,
                                                        parent = "company logo",
                                                        uris[0],
                                                        nameState.trim().replace(
                                                            " ",
                                                            "_"
                                                        ).ifEmpty { "item" })
                                                    withContext(Dispatchers.Main) {
                                                        activityScopedViewModel.showLoading(false)
                                                        if (internalPath != null) {
                                                            oldImage = logoState
                                                            logoState = internalPath
                                                            state.selectedCompany.companyLogo = logoState
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
                        }) { logo ->
                        logoState = logo
                        state.selectedCompany.companyLogo = logo
                    }

                    if (SettingsModel.showTax) {
                        //tax reg no
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue = taxRegnoState,
                            label = "Tax Reg. No",
                            focusRequester = taxRegNoFocusRequester,
                            placeHolder = "Enter Tax Reg. No",
                            onAction = { taxFocusRequester.requestFocus() }) { taxRegno ->
                            taxRegnoState = taxRegno
                            state.selectedCompany.companyTaxRegno = taxRegno
                        }

                        //tax
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue = taxState,
                            label = "Tax",
                            focusRequester = taxFocusRequester,
                            keyboardType = KeyboardType.Decimal,
                            placeHolder = "Enter Tax",
                            onAction = {
                                if (SettingsModel.showTax1) {
                                    tax1RegNoFocusRequester.requestFocus()
                                } else if (SettingsModel.showTax2) {
                                    tax2RegNoFocusRequester.requestFocus()
                                } else {
                                    emailFocusRequester.requestFocus()
                                }
                            }) { tax ->
                            taxState = Utils.getDoubleValue(
                                tax,
                                taxState
                            )
                            state.selectedCompany.companyTax = taxState.toDoubleOrNull() ?: 0.0
                        }
                    }
                    if (SettingsModel.showTax1) {
                        //tax1 reg no
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue = tax1RegnoState,
                            label = "Tax1 Reg. No",
                            placeHolder = "Enter Tax1 Reg. No",
                            focusRequester = tax1RegNoFocusRequester,
                            onAction = { tax1FocusRequester.requestFocus() }) { tax1Regno ->
                            tax1RegnoState = tax1Regno
                            state.selectedCompany.companyTax1Regno = tax1Regno
                        }

                        //tax1
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue = tax1State,
                            label = "Tax1",
                            keyboardType = KeyboardType.Decimal,
                            placeHolder = "Enter Tax1",
                            focusRequester = tax1FocusRequester,
                            onAction = {
                                if (SettingsModel.showTax2) {
                                    tax2RegNoFocusRequester.requestFocus()
                                } else {
                                    emailFocusRequester.requestFocus()
                                }
                            }) { tax1 ->
                            tax1State = Utils.getDoubleValue(
                                tax1,
                                tax1State
                            )
                            state.selectedCompany.companyTax1 = tax1State.toDoubleOrNull() ?: 0.0
                        }
                    }
                    if (SettingsModel.showTax2) {
                        //tax2 reg no
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue = tax2RegnoState,
                            label = "Tax2 Reg. No",
                            placeHolder = "Enter Tax2 Reg. No",
                            focusRequester = tax2RegNoFocusRequester,
                            onAction = { tax2FocusRequester.requestFocus() }) { tax2Regno ->
                            tax2RegnoState = tax2Regno
                            state.selectedCompany.companyTax2Regno = tax2Regno
                        }

                        //tax2
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue = tax2State,
                            label = "Tax2",
                            keyboardType = KeyboardType.Decimal,
                            placeHolder = "Enter Tax2",
                            focusRequester = tax2FocusRequester,
                            onAction = { emailFocusRequester.requestFocus() }) { tax2 ->
                            tax2State = Utils.getDoubleValue(
                                tax2,
                                tax2State
                            )
                            state.selectedCompany.companyTax2 = tax2State.toDoubleOrNull() ?: 0.0
                        }
                    }


                    UISwitch(
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        checked = upWithTaxState,
                        text = "Unit price with tax",
                    ) { unitPriceWithTax ->
                        upWithTaxState = unitPriceWithTax
                        state.selectedCompany.companyUpWithTax = unitPriceWithTax
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
                            saveCompany()
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
                            if (logoState.isNotEmpty()) {
                                FileUtils.deleteFile(
                                    context,
                                    logoState
                                )
                            }
                            viewModel.deleteSelectedCompany(
                                state.selectedCompany
                            )
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

                SearchableDropdownMenuEx(items = state.companies.toMutableList(),
                    modifier = Modifier.padding(
                        top = 15.dp,
                        start = 10.dp,
                        end = 10.dp
                    ),
                    label = "Select Company",
                    selectedId = state.selectedCompany.companyId,
                    onLoadItems = { viewModel.fetchCompanies() },
                    leadingIcon = {
                        if (state.selectedCompany.companyId.isNotEmpty()) {
                            Icon(
                                Icons.Default.RemoveCircleOutline,
                                contentDescription = "remove family",
                                tint = Color.Black,
                                modifier = it
                            )
                        }
                    },
                    onLeadingIconClick = {
                        clear()
                    }) { company ->
                    company as Company
                    viewModel.currentCompany = company.copy()
                    state.selectedCompany = company
                    nameState = company.companyName ?: ""
                    phoneState = company.companyPhone ?: ""
                    addressState = company.companyAddress ?: ""
                    countryState = company.companyCountry ?: ""
                    taxRegnoState = company.companyTaxRegno ?: ""
                    taxState = company.companyTax.toString()
                    upWithTaxState = company.companyUpWithTax
                    //printerState = company.companyPrinterId ?: ""
                    emailState = company.companyEmail ?: ""
                    webState = company.companyWeb ?: ""
                    logoState = company.companyLogo ?: ""
                    tax1RegnoState = company.companyTax1Regno ?: ""
                    tax1State = company.companyTax1.toString()
                    tax2RegnoState = company.companyTax2Regno ?: ""
                    tax2State = company.companyTax2.toString()
                }
            }
        }
    }
}