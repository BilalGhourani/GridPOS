package com.grid.pos.ui.thirdParty

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.RemoveCircleOutline
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.grid.pos.ActivityScopedViewModel
import com.grid.pos.R
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.ThirdPartyType
import com.grid.pos.model.ThirdPartyTypeModel
import com.grid.pos.ui.common.SearchableDropdownMenuEx
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.common.UIImageButton
import com.grid.pos.ui.common.UISwitch
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun ManageThirdPartiesView(
        modifier: Modifier = Modifier,
        navController: NavController? = null,
        activityScopedViewModel: ActivityScopedViewModel,
        viewModel: ManageThirdPartiesViewModel = hiltViewModel()
) {
    val state by viewModel.manageThirdPartiesState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val fnFocusRequester = remember { FocusRequester() }
    val phone1FocusRequester = remember { FocusRequester() }
    val phone2FocusRequester = remember { FocusRequester() }
    val addressFocusRequester = remember { FocusRequester() }

    val typeListState = remember { mutableStateListOf<ThirdPartyTypeModel>() }
    var typeState by remember { mutableStateOf("") }
    var nameState by remember { mutableStateOf("") }
    var fnState by remember { mutableStateOf("") }
    var phone1State by remember { mutableStateOf("") }
    var phone2State by remember { mutableStateOf("") }
    var addressState by remember { mutableStateOf("") }
    var isDefaultState by remember { mutableStateOf(false) }

    /*  LaunchedEffect(activityScopedViewModel.thirdParties) {
          viewModel.fillCachedThirdParties(activityScopedViewModel.thirdParties)
      }*/

    LaunchedEffect(Unit) {
        typeListState.addAll(Utils.getThirdPartyTypeModels())
        typeState = ThirdPartyType.RECEIVALBE.type
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(state.warning) {
        state.warning?.value?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }

    LaunchedEffect(state.isLoading) {
        activityScopedViewModel.showLoading(state.isLoading)
    }

    fun clear() {
        viewModel.currentThirdParty = ThirdParty()
        state.selectedThirdParty = ThirdParty()
        typeState = ThirdPartyType.RECEIVALBE.type
        nameState = ""
        fnState = ""
        phone1State = ""
        phone2State = ""
        addressState = ""
        isDefaultState = false
        state.clear = false
    }

    var saveAndBack by remember { mutableStateOf(false) }
    fun handleBack() {
        if (state.isLoading) {
            return
        }
        if (state.selectedThirdParty.didChanged(
                viewModel.currentThirdParty
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
                        viewModel.saveThirdParty(state.selectedThirdParty)
                    }
                    dialogText = "Do you want to save your changes"
                    positiveBtnText = "Save"
                    negativeBtnText = "Close"
                    height = 100.dp
                })
            return
        }
        if (state.thirdParties.isNotEmpty()) {
            activityScopedViewModel.thirdParties = state.thirdParties
        }
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
                                text = "Manage Third Parties",
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
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 175.dp)
                        .verticalScroll(rememberScrollState()),
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
                        onAction = {
                            fnFocusRequester.requestFocus()
                        }) { name ->
                        nameState = name
                        state.selectedThirdParty.thirdPartyName = name
                    }

                    //financial number
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = fnState,
                        label = "Financial No.",
                        placeHolder = "Financial No.",
                        focusRequester = fnFocusRequester,
                        onAction = {
                            phone1FocusRequester.requestFocus()
                        }) { fn ->
                        fnState = fn
                        state.selectedThirdParty.thirdPartyFn = fn
                    }

                    //phone1
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = phone1State,
                        label = "Phone1",
                        placeHolder = "Enter Phone1",
                        focusRequester = phone1FocusRequester,
                        onAction = { phone2FocusRequester.requestFocus() }) { phone1 ->
                        phone1State = phone1
                        state.selectedThirdParty.thirdPartyPhone1 = phone1
                    }

                    //phone2
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = phone2State,
                        label = "Phone2",
                        placeHolder = "Enter Phone2",
                        focusRequester = phone2FocusRequester,
                        onAction = { addressFocusRequester.requestFocus() }) { phone2 ->
                        phone2State = phone2
                        state.selectedThirdParty.thirdPartyPhone2 = phone2
                    }

                    //address
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = addressState,
                        label = "Address",
                        maxLines = 3,
                        placeHolder = "Enter address",
                        focusRequester = addressFocusRequester,
                        imeAction = ImeAction.Done,
                        onAction = { keyboardController?.hide() }) { address ->
                        addressState = address
                        state.selectedThirdParty.thirdPartyAddress = address
                    }

                    UISwitch(
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        checked = isDefaultState,
                        enabled = state.enableIsDefault,
                        text = "POS Default",
                    ) { isDefault ->
                        isDefaultState = isDefault
                        state.selectedThirdParty.thirdPartyDefault = isDefaultState
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
                            if (state.selectedThirdParty.thirdPartyType.isNullOrEmpty()) {
                                state.selectedThirdParty.thirdPartyType = typeState
                            }
                            viewModel.saveThirdParty(state.selectedThirdParty)
                        }

                        UIImageButton(
                            modifier = Modifier
                                .weight(.33f)
                                .padding(3.dp),
                            icon = R.drawable.delete,
                            text = "Delete"
                        ) {
                            viewModel.deleteSelectedThirdParty()
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

                SearchableDropdownMenuEx(
                    items = typeListState.toMutableList(),
                    modifier = Modifier.padding(
                        top = 100.dp,
                        start = 10.dp,
                        end = 10.dp
                    ),
                    selectedId = typeState
                ) { thirdPartyTypeModel ->
                    thirdPartyTypeModel as ThirdPartyTypeModel
                    typeState = thirdPartyTypeModel.thirdPartyType.type
                    state.selectedThirdParty.thirdPartyType = typeState
                }

                SearchableDropdownMenuEx(items = state.thirdParties.toMutableList(),
                    modifier = Modifier.padding(
                        top = 15.dp,
                        start = 10.dp,
                        end = 10.dp
                    ),
                    label = "Select Third Party",
                    selectedId = state.selectedThirdParty.thirdPartyId,
                    onLoadItems = { viewModel.fetchThirdParties() },
                    leadingIcon = {
                        if (state.selectedThirdParty.thirdPartyId.isNotEmpty()) {
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
                    }) { thirdParty ->
                    thirdParty as ThirdParty
                    viewModel.currentThirdParty = thirdParty.copy()
                    state.selectedThirdParty = thirdParty
                    typeState = thirdParty.thirdPartyType ?: ""
                    nameState = thirdParty.thirdPartyName ?: ""
                    fnState = thirdParty.thirdPartyFn ?: ""
                    phone1State = thirdParty.thirdPartyPhone1 ?: ""
                    phone2State = thirdParty.thirdPartyPhone2 ?: ""
                    addressState = thirdParty.thirdPartyAddress ?: ""
                    isDefaultState = thirdParty.thirdPartyDefault
                }
            }
        }
    }
}