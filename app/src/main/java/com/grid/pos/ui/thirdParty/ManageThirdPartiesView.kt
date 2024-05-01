package com.grid.pos.ui.thirdParty

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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.grid.pos.data.Company.Company
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.ui.common.LoadingIndicator
import com.grid.pos.ui.common.SearchableDropdownMenu
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.Blue
import com.grid.pos.ui.theme.GridPOSTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ManageThirdPartiesView(
    navController: NavController? = null,
    modifier: Modifier = Modifier,
    viewModel: ManageThirdPartiesViewModel = hiltViewModel()
) {
    val manageThirdPartiesState: ManageThirdPartiesState by viewModel.manageThirdPartiesState.collectAsState(
        ManageThirdPartiesState()
    )
    val keyboardController = LocalSoftwareKeyboardController.current
    val phone1FocusRequester = remember { FocusRequester() }
    val phone2FocusRequester = remember { FocusRequester() }
    val addressFocusRequester = remember { FocusRequester() }

    var nameState by remember { mutableStateOf("") }
    var fnState by remember { mutableStateOf("") }
    var companyIdState by remember { mutableStateOf("") }
    var phone1State by remember { mutableStateOf("") }
    var phone2State by remember { mutableStateOf("") }
    var addressState by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(manageThirdPartiesState.warning) {
        if (!manageThirdPartiesState.warning.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.Main).launch {
                snackbarHostState.showSnackbar(
                    message = manageThirdPartiesState.warning!!,
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }
    GridPOSTheme {
        Scaffold(
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
                                text = "Manage Third Parties",
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
                            items = manageThirdPartiesState.thirdParties.toMutableList(),
                            modifier = Modifier.padding(10.dp),
                            label = nameState.ifEmpty { "Select Third Party" },
                        ) { thirdParty ->
                            thirdParty as ThirdParty
                            manageThirdPartiesState.selectedThirdParty = thirdParty
                            nameState = thirdParty.thirdPartyName ?: ""
                            fnState = thirdParty.thirdPartyFn ?: ""
                            companyIdState = thirdParty.thirdPartyCompId ?: ""
                            phone1State = thirdParty.thirdPartyPhone1 ?: ""
                            phone2State = thirdParty.thirdPartyPhone2 ?: ""
                            addressState = thirdParty.thirdPartyAddress ?: ""
                        }

                        //name
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = nameState,
                            label = "Name",
                            placeHolder = "Enter Name",
                            onAction = {
                                keyboardController?.hide()
                            }
                        ) { name ->
                            nameState = name
                            manageThirdPartiesState.selectedThirdParty.thirdPartyName = name
                        }

                        //company
                        SearchableDropdownMenu(
                            items = manageThirdPartiesState.companies.toMutableList(),
                            modifier = Modifier.padding(10.dp),
                            label = "Select Company",
                            selectedId = companyIdState
                        ) { company ->
                            company as Company
                            companyIdState = company.companyId
                            manageThirdPartiesState.selectedThirdParty.thirdPartyCompId =
                                companyIdState
                            phone1FocusRequester.requestFocus()
                        }

                        //phone1
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = phone1State,
                            label = "Phone1",
                            placeHolder = "Enter Phone1",
                            focusRequester = phone1FocusRequester,
                            onAction = { phone2FocusRequester.requestFocus() }
                        ) { phone1 ->
                            phone1State = phone1
                            manageThirdPartiesState.selectedThirdParty.thirdPartyPhone1 = phone1
                        }

                        //phone2
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = phone2State,
                            label = "Phone2",
                            placeHolder = "Enter Phone2",
                            focusRequester = phone2FocusRequester,
                            onAction = { addressFocusRequester.requestFocus() }
                        ) { phone2 ->
                            phone2State = phone2
                            manageThirdPartiesState.selectedThirdParty.thirdPartyPhone2 = phone2
                        }

                        //address
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = addressState,
                            label = "Address",
                            maxLines = 3,
                            placeHolder = "Enter address",
                            focusRequester = addressFocusRequester,
                            imeAction = ImeAction.Done,
                            onAction = { keyboardController?.hide() }
                        ) { address ->
                            addressState = address
                            manageThirdPartiesState.selectedThirdParty.thirdPartyAddress = address
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
                                viewModel.saveThirdParty()
                            }

                            UIButton(
                                modifier = Modifier
                                    .weight(.33f)
                                    .padding(3.dp),
                                text = "Delete"
                            ) {
                                viewModel.deleteSelectedThirdParty()
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
            show = manageThirdPartiesState.isLoading
        )
        if (manageThirdPartiesState.clear) {
            manageThirdPartiesState.selectedThirdParty = ThirdParty()
            manageThirdPartiesState.selectedThirdParty.thirdPartyCompId = ""
            nameState =  ""
            fnState =   ""
            companyIdState =   ""
            phone1State = ""
            phone2State =  ""
            addressState =   ""
            manageThirdPartiesState.clear = false
        }
    }
}