package com.grid.pos.ui.company

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
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.grid.pos.data.Company.Company
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.Family.Family
import com.grid.pos.ui.common.LoadingIndicator
import com.grid.pos.ui.common.SearchableDropdownMenu
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.Blue
import com.grid.pos.ui.theme.GridPOSTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ManageCompaniesView(
    navController: NavController? = null,
    modifier: Modifier = Modifier,
    viewModel: ManageCompaniesViewModel = hiltViewModel()
) {
    val manageCompaniesState: ManageCompaniesState by viewModel.manageCompaniesState.collectAsState(
        ManageCompaniesState()
    )
    val keyboardController = LocalSoftwareKeyboardController.current
    val phoneFocusRequester = remember { FocusRequester() }
    val addressFocusRequester = remember { FocusRequester() }
    val taxRegNoFocusRequester = remember { FocusRequester() }
    val taxFocusRequester = remember { FocusRequester() }
    val emailFocusRequester = remember { FocusRequester() }
    val webFocusRequester = remember { FocusRequester() }
    val logoFocusRequester = remember { FocusRequester() }
    val tax1FocusRequester = remember { FocusRequester() }
    val upWithTaxFocusRequester = remember { FocusRequester() }
    val tax1RegNoFocusRequester = remember { FocusRequester() }
    val tax2FocusRequester = remember { FocusRequester() }
    val tax2RegNoFocusRequester = remember { FocusRequester() }

    var nameState by remember { mutableStateOf("") }
    var phoneState by remember { mutableStateOf("") }
    var addressState by remember { mutableStateOf("") }
    var taxRegnoState by remember { mutableStateOf("") }
    var taxState by remember { mutableStateOf("") }
    var curCodeTaxState by remember { mutableStateOf("") }
    var curUpWithTaxState by remember { mutableStateOf("") }
    var emailState by remember { mutableStateOf("") }
    var webState by remember { mutableStateOf("") }
    var logoState by remember { mutableStateOf("") }
    var ssState by remember { mutableStateOf(false) }
    var tax1State by remember { mutableStateOf("") }
    var tax1RegnoState by remember { mutableStateOf("") }
    var tax2State by remember { mutableStateOf("") }
    var tax2RegnoState by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(manageCompaniesState.warning) {
        if (!manageCompaniesState.warning.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.Main).launch {
                snackbarHostState.showSnackbar(
                    message = manageCompaniesState.warning!!,
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
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        title = {
                            Text(
                                text = "Manage Companies",
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
                            items = manageCompaniesState.companies.toMutableList(),
                            modifier = Modifier.padding(10.dp),
                            label = nameState.ifEmpty { "Select Company" },
                        ) { company ->
                            company as Company
                            manageCompaniesState.selectedCompany = company
                            nameState = company.companyName ?: ""
                            phoneState = company.companyPhone ?: ""
                            addressState = company.companyAddress ?: ""
                            taxRegnoState = company.companyTaxRegno ?: ""
                            taxState = company.companyTax ?: ""
                            curCodeTaxState = company.companyCurCodeTax ?: ""
                            curUpWithTaxState = company.companyUpWithTax ?: ""
                            emailState = company.companyEmail ?: ""
                            webState = company.companyWeb ?: ""
                            logoState = company.companyLogo ?: ""
                            ssState = company.companySS
                            tax1RegnoState = company.companyTax1Regno ?: ""
                            tax1State = company.companyTax1 ?: ""
                            tax2RegnoState = company.companyTax2Regno ?: ""
                            tax2State = company.companyTax2 ?: ""
                        }

                        //name
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = nameState,
                            label = "Name",
                            placeHolder = "Enter Name",
                            onAction = { phoneFocusRequester.requestFocus() }
                        ) { name ->
                            nameState = name
                            manageCompaniesState.selectedCompany.companyName = name
                        }

                        //phone
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = phoneState,
                            label = "Phone",
                            focusRequester = phoneFocusRequester,
                            placeHolder = "Enter Phone",
                            onAction = { addressFocusRequester.requestFocus() }
                        ) { phone ->
                            phoneState = phone
                            manageCompaniesState.selectedCompany.companyPhone = phone
                        }

                        //address
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = addressState,
                            label = "Address",
                            maxLines = 3,
                            focusRequester = addressFocusRequester,
                            placeHolder = "Enter address",
                            onAction = { taxRegNoFocusRequester.requestFocus() }
                        ) { address ->
                            addressState = address
                            manageCompaniesState.selectedCompany.companyAddress = address
                        }

                        //tax reg no
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = taxRegnoState,
                            label = "Tax Reg. No",
                            focusRequester = taxRegNoFocusRequester,
                            placeHolder = "Enter Tax Reg. No",
                            onAction = { taxFocusRequester.requestFocus() }
                        ) { taxRegno ->
                            taxRegnoState = taxRegno
                            manageCompaniesState.selectedCompany.companyTaxRegno = taxRegno
                        }

                        //tax
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = taxState,
                            label = "Tax",
                            focusRequester = taxFocusRequester,
                            keyboardType = KeyboardType.Decimal,
                            placeHolder = "Enter Tax",
                            onAction = { upWithTaxFocusRequester.requestFocus() }
                        ) { tax ->
                            taxState = tax
                            manageCompaniesState.selectedCompany.companyTax = tax
                        }

                        SearchableDropdownMenu(
                            items = manageCompaniesState.currencies.toMutableList(),
                            modifier = Modifier.padding(10.dp),
                            label = "Select Tax Currency",
                            selectedId = curCodeTaxState
                        ) { currency ->
                            currency as Currency
                            curCodeTaxState = currency.currencyId
                            manageCompaniesState.selectedCompany.companyCurCodeTax =
                                currency.currencyId
                        }

                        //tax1 reg no
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = curUpWithTaxState,
                            label = "Up With Tax",
                            placeHolder = "Enter Up With Tax",
                            focusRequester = upWithTaxFocusRequester,
                            onAction = { tax1RegNoFocusRequester.requestFocus() }
                        ) { upWithTax ->
                            curUpWithTaxState = upWithTax
                            manageCompaniesState.selectedCompany.companyUpWithTax = upWithTax
                        }

                        //tax1 reg no
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = tax1RegnoState,
                            label = "Tax1 Reg. No",
                            placeHolder = "Enter Tax1 Reg. No",
                            focusRequester = tax1RegNoFocusRequester,
                            onAction = { tax1FocusRequester.requestFocus() }
                        ) { tax1Regno ->
                            tax1RegnoState = tax1Regno
                            manageCompaniesState.selectedCompany.companyTax1Regno = tax1Regno
                        }

                        //tax1
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = tax1State,
                            label = "Tax1",
                            keyboardType = KeyboardType.Decimal,
                            placeHolder = "Enter Tax1",
                            focusRequester = tax1FocusRequester,
                            onAction = { tax2RegNoFocusRequester.requestFocus() }
                        ) { tax1 ->
                            tax1State = tax1
                            manageCompaniesState.selectedCompany.companyTax1 = tax1
                        }

                        //tax2 reg no
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = tax2RegnoState,
                            label = "Tax2 Reg. No",
                            placeHolder = "Enter Tax2 Reg. No",
                            focusRequester = tax2RegNoFocusRequester,
                            onAction = { tax2FocusRequester.requestFocus() }
                        ) { tax2Regno ->
                            tax2RegnoState = tax2Regno
                            manageCompaniesState.selectedCompany.companyTax2Regno = tax2Regno
                        }

                        //tax2
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = tax2State,
                            label = "Tax2",
                            keyboardType = KeyboardType.Decimal,
                            placeHolder = "Enter Tax2",
                            focusRequester = tax2FocusRequester,
                            onAction = { emailFocusRequester.requestFocus() }
                        ) { tax2 ->
                            tax2State = tax2
                            manageCompaniesState.selectedCompany.companyTax2 = tax2
                        }

                        //email
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = emailState,
                            label = "Email Address",
                            placeHolder = "Enter Email Address",
                            focusRequester = emailFocusRequester,
                            onAction = { webFocusRequester.requestFocus() }
                        ) { email ->
                            emailState = email
                            manageCompaniesState.selectedCompany.companyEmail = email
                        }

                        //web
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = webState,
                            label = "Website",
                            placeHolder = "Enter Website",
                            focusRequester = webFocusRequester,
                            onAction = { logoFocusRequester.requestFocus() }
                        ) { web ->
                            webState = web
                            manageCompaniesState.selectedCompany.companyWeb = web
                        }

                        //logo
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = logoState,
                            label = "Logo",
                            placeHolder = "Enter Logo",
                            focusRequester = logoFocusRequester,
                            imeAction = ImeAction.Done,
                            onAction = { keyboardController?.hide() }
                        ) { logo ->
                            logoState = logo
                            manageCompaniesState.selectedCompany.companyLogo = logo
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(10.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            ElevatedButton(
                                modifier = Modifier
                                    .weight(.33f)
                                    .padding(3.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                                onClick = { viewModel.saveCompany(manageCompaniesState.selectedCompany) }
                            ) {
                                Text("Save")
                            }

                            ElevatedButton(
                                modifier = Modifier
                                    .weight(.33f)
                                    .padding(3.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                                onClick = { viewModel.deleteSelectedCompany(manageCompaniesState.selectedCompany) }
                            ) {
                                Text("Delete")
                            }

                            ElevatedButton(
                                modifier = Modifier
                                    .weight(.33f)
                                    .padding(3.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                                onClick = { navController?.navigateUp() }
                            ) {
                                Text("Close")
                            }

                        }

                    }
                }
            }
        }
        LoadingIndicator(
            show = manageCompaniesState.isLoading
        )
        if (manageCompaniesState.clear) {
            manageCompaniesState.selectedCompany = Company()
            manageCompaniesState.selectedCompany.companyCurCodeTax = ""
            nameState = ""
            phoneState = ""
            addressState = ""
            taxRegnoState = ""
            taxState = ""
            curCodeTaxState = ""
            curUpWithTaxState = ""
            emailState = ""
            webState = ""
            logoState = ""
            ssState = false
            tax1RegnoState = ""
            tax1State = ""
            tax2RegnoState = ""
            tax2State = ""
            manageCompaniesState.clear = false
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ManageCompaniesViewPreview() {
    GridPOSTheme {
        ManageCompaniesView()
    }
}