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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.grid.pos.R
import com.grid.pos.SharedViewModel
import com.grid.pos.data.company.Company
import com.grid.pos.interfaces.OnGalleryResult
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.ToastModel
import com.grid.pos.ui.common.SearchableDropdownMenuEx
import com.grid.pos.ui.common.UIImageButton
import com.grid.pos.ui.common.UISwitch
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.FileUtils
import com.grid.pos.utils.Utils

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCompaniesView(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    sharedViewModel: SharedViewModel,
    viewModel: ManageCompaniesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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


    LaunchedEffect(state.warning) {
        state.warning?.value?.let { message ->
            if (state.actionLabel == "next" && sharedViewModel.isRegistering) {
                sharedViewModel.showPopup(
                    true,
                    PopupModel(
                        onDismissRequest = {
                            sharedViewModel.isRegistering = false
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
                sharedViewModel.showToastMessage(ToastModel(
                    message = message,
                    actionButton = state.actionLabel,
                    onActionClick = {
                        when (state.actionLabel) {
                            "Settings" -> sharedViewModel.openAppStorageSettings()
                        }
                    }
                ))
            }
        }
    }

    LaunchedEffect(state.isLoading) {
        sharedViewModel.showLoading(state.isLoading)
    }

    fun saveCompany() {
        viewModel.oldImage?.let { old ->
            FileUtils.deleteFile(
                context,
                old
            )
        }
        val firstCurr = state.currencies.firstOrNull() ?: SettingsModel.currentCurrency
        state.company.companyCurCodeTax = firstCurr?.currencyId
        viewModel.save(
            sharedViewModel.isRegistering
        )
    }

    var saveAndBack by remember { mutableStateOf(false) }
    fun handleBack() {
        if (state.isLoading) {
            return
        }
        if (viewModel.isAnyChangeDone()) {
            sharedViewModel.showPopup(true,
                PopupModel().apply {
                    onDismissRequest = {
                        viewModel.resetState()
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
        viewModel.closeConnectionIfNeeded()
        navController?.navigateUp()
    }

    fun clearAndBack() {
        viewModel.resetState()
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
                        defaultValue = state.company.companyName ?: "",
                        label = "Name",
                        placeHolder = "Enter Name",
                        onAction = { phoneFocusRequester.requestFocus() }) { name ->
                        viewModel.updateCompany(
                            state.company.copy(
                                companyName = name
                            )
                        )
                    }

                    //phone
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = state.company.companyPhone ?: "",
                        label = "Phone",
                        focusRequester = phoneFocusRequester,
                        placeHolder = "Enter Phone",
                        onAction = { addressFocusRequester.requestFocus() }) { phone ->
                        viewModel.updateCompany(
                            state.company.copy(
                                companyPhone = phone
                            )
                        )
                    }

                    //address
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = state.company.companyAddress ?: "",
                        label = "Address",
                        focusRequester = addressFocusRequester,
                        placeHolder = "Enter address",
                        onAction = {
                            countryFocusRequester.requestFocus()
                        }) { address ->
                        viewModel.updateCompany(
                            state.company.copy(
                                companyAddress = address
                            )
                        )
                    }

                    //country
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = state.company.companyCountry ?: "",
                        label = "Country",
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
                        viewModel.updateCompany(
                            state.company.copy(
                                companyCountry = country
                            )
                        )
                    }

                    //email
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = state.company.companyEmail ?: "",
                        label = "Email Address",
                        placeHolder = "Enter Email Address",
                        focusRequester = emailFocusRequester,
                        onAction = { webFocusRequester.requestFocus() }) { email ->
                        viewModel.updateCompany(
                            state.company.copy(
                                companyEmail = email
                            )
                        )
                    }

                    //web
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = state.company.companyWeb ?: "",
                        label = "Website",
                        placeHolder = "Enter Website",
                        focusRequester = webFocusRequester,
                        onAction = { logoFocusRequester.requestFocus() }) { web ->
                        viewModel.updateCompany(
                            state.company.copy(
                                companyWeb = web
                            )
                        )
                    }

                    //logo
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = state.company.companyLogo ?: "",
                        label = "Logo",
                        placeHolder = "Enter Logo",
                        focusRequester = logoFocusRequester,
                        imeAction = ImeAction.Done,
                        onAction = { keyboardController?.hide() },
                        trailingIcon = {
                            IconButton(onClick = {
                                sharedViewModel.launchGalleryPicker(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
                                    object : OnGalleryResult {
                                        override fun onGalleryResult(uris: List<Uri>) {
                                            if (uris.isNotEmpty()) {
                                                sharedViewModel.copyToInternalStorage(
                                                    context,
                                                    uris[0],
                                                    "company logo",
                                                    (state.company.companyName ?: "company").trim()
                                                        .replace(
                                                            " ",
                                                            "_"
                                                        )
                                                ) { internalPath ->
                                                    if (internalPath != null) {
                                                        viewModel.oldImage = state.company.companyLogo
                                                        viewModel.updateCompany(
                                                            state.company.copy(
                                                                companyLogo = internalPath
                                                            )
                                                        )
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
                        viewModel.updateCompany(
                            state.company.copy(
                                companyLogo = logo
                            )
                        )
                    }

                    if (SettingsModel.showTax) {
                        //tax reg no
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue = state.company.companyTaxRegno ?: "",
                            label = "Tax Reg. No",
                            focusRequester = taxRegNoFocusRequester,
                            placeHolder = "Enter Tax Reg. No",
                            onAction = { taxFocusRequester.requestFocus() }) { taxRegno ->
                            viewModel.updateCompany(
                                state.company.copy(
                                    companyTaxRegno = taxRegno
                                )
                            )
                        }

                        //tax
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue =  state.company.companyTaxStr?:"",
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
                            val companyTax = tax.toDoubleOrNull() ?: state.company.companyTax
                            viewModel.updateCompany(
                                state.company.copy(
                                    companyTax = companyTax,
                                    companyTaxStr = Utils.getDoubleValue(tax,state.company.companyTaxStr?:"")
                                )
                            )
                        }
                    }
                    if (SettingsModel.showTax1) {
                        //tax1 reg no
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue = state.company.companyTax1Regno ?: "",
                            label = "Tax1 Reg. No",
                            placeHolder = "Enter Tax1 Reg. No",
                            focusRequester = tax1RegNoFocusRequester,
                            onAction = { tax1FocusRequester.requestFocus() }) { tax1Regno ->
                            viewModel.updateCompany(
                                state.company.copy(
                                    companyTax1Regno = tax1Regno
                                )
                            )
                        }

                        //tax1
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue = state.company.companyTax1Str?:"",
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
                            val companyTax1 = tax1.toDoubleOrNull() ?: state.company.companyTax1
                            viewModel.updateCompany(
                                state.company.copy(
                                    companyTax1 = companyTax1,
                                    companyTax1Str = Utils.getDoubleValue(tax1,state.company.companyTax1Str?:"")
                                )
                            )
                        }
                    }
                    if (SettingsModel.showTax2) {
                        //tax2 reg no
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue = state.company.companyTax2Regno ?: "",
                            label = "Tax2 Reg. No",
                            placeHolder = "Enter Tax2 Reg. No",
                            focusRequester = tax2RegNoFocusRequester,
                            onAction = { tax2FocusRequester.requestFocus() }) { tax2Regno ->
                            viewModel.updateCompany(
                                state.company.copy(
                                    companyTax2Regno = tax2Regno
                                )
                            )
                        }

                        //tax2
                        UITextField(modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                            defaultValue =  state.company.companyTax2Str?:"",
                            label = "Tax2",
                            keyboardType = KeyboardType.Decimal,
                            placeHolder = "Enter Tax2",
                            focusRequester = tax2FocusRequester,
                            onAction = { emailFocusRequester.requestFocus() }) { tax2 ->
                            val companyTax2 = tax2.toDoubleOrNull() ?: state.company.companyTax2
                            viewModel.updateCompany(
                                state.company.copy(
                                    companyTax2 = companyTax2,
                                    companyTax2Str = Utils.getDoubleValue(tax2,state.company.companyTax2Str?:"")
                                )
                            )
                        }
                    }


                    UISwitch(
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        checked = state.company.companyUpWithTax,
                        text = "Unit price with tax",
                    ) { unitPriceWithTax ->
                        viewModel.updateCompany(
                            state.company.copy(
                                companyUpWithTax = unitPriceWithTax
                            )
                        )
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
                            viewModel.oldImage?.let { old ->
                                FileUtils.deleteFile(
                                    context,
                                    old
                                )
                            }
                            if (!state.company.companyLogo.isNullOrEmpty()) {
                                FileUtils.deleteFile(
                                    context,
                                    state.company.companyLogo!!
                                )
                            }
                            viewModel.delete()
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
                    selectedId = state.company.companyId,
                    onLoadItems = { viewModel.fetchCompanies() },
                    leadingIcon = { mod ->
                        if (state.company.companyId.isNotEmpty()) {
                            Icon(
                                Icons.Default.RemoveCircleOutline,
                                contentDescription = "remove family",
                                tint = Color.Black,
                                modifier = mod
                            )
                        }
                    },
                    onLeadingIconClick = {
                        viewModel.resetState()
                    }) { company ->
                    company as Company
                    viewModel.currentCompany = company.copy(
                        companyTaxStr = company.companyTax.toString(),
                        companyTax1Str = company.companyTax1.toString(),
                        companyTax2Str = company.companyTax2.toString()
                    )
                    viewModel.updateCompany(viewModel.currentCompany.copy())
                }
            }
        }
    }
}