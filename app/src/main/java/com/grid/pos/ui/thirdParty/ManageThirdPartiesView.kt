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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.grid.pos.R
import com.grid.pos.SharedViewModel
import com.grid.pos.data.thirdParty.ThirdParty
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.ThirdPartyTypeModel
import com.grid.pos.model.ToastModel
import com.grid.pos.ui.common.SearchableDropdownMenuEx
import com.grid.pos.ui.common.UIImageButton
import com.grid.pos.ui.common.UISwitch
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.GridPOSTheme

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun ManageThirdPartiesView(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    sharedViewModel: SharedViewModel,
    viewModel: ManageThirdPartiesViewModel = hiltViewModel()
) {
    val state by viewModel.manageThirdPartiesState.collectAsStateWithLifecycle()
    val thirdParty = viewModel.thirdPartyState.collectAsState().value

    val keyboardController = LocalSoftwareKeyboardController.current
    val fnFocusRequester = remember { FocusRequester() }
    val phone1FocusRequester = remember { FocusRequester() }
    val phone2FocusRequester = remember { FocusRequester() }
    val addressFocusRequester = remember { FocusRequester() }

    LaunchedEffect(state.warning) {
        state.warning?.value?.let { message ->
            sharedViewModel.showToastMessage(ToastModel(message = message))
        }
    }

    LaunchedEffect(state.isLoading) {
        sharedViewModel.showLoading(state.isLoading)
    }

    var saveAndBack by remember { mutableStateOf(false) }
    fun handleBack() {
        if (state.isLoading) {
            return
        }
        if (thirdParty.didChanged(
                viewModel.currentThirdParty
            )
        ) {
            sharedViewModel.showPopup(true,
                PopupModel().apply {
                    onDismissRequest = {
                        viewModel.resetState()
                        handleBack()
                    }
                    onConfirmation = {
                        saveAndBack = true
                        viewModel.save()
                    }
                    dialogText = "Do you want to save your changes"
                    positiveBtnText = "Save"
                    negativeBtnText = "Close"
                })
            return
        }
        if (state.thirdParties.isNotEmpty()) {
            sharedViewModel.thirdParties = state.thirdParties
        }
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
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 175.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    //name
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = thirdParty.thirdPartyName?:"",
                        label = "Name",
                        placeHolder = "Enter Name",
                        onAction = {
                            fnFocusRequester.requestFocus()
                        }) { name ->
                        viewModel.updateThirdParty(
                            thirdParty.copy(
                                thirdPartyName = name.trim()
                            )
                        )
                    }

                    //financial number
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = thirdParty.thirdPartyFn?:"",
                        label = "Financial No.",
                        placeHolder = "Financial No.",
                        focusRequester = fnFocusRequester,
                        onAction = {
                            phone1FocusRequester.requestFocus()
                        }) { fn ->
                        viewModel.updateThirdParty(
                            thirdParty.copy(
                                thirdPartyFn = fn.trim()
                            )
                        )
                    }

                    //phone1
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = thirdParty.thirdPartyPhone1?:"",
                        label = "Phone1",
                        placeHolder = "Enter Phone1",
                        focusRequester = phone1FocusRequester,
                        onAction = { phone2FocusRequester.requestFocus() }) { phone1 ->
                        viewModel.updateThirdParty(
                            thirdParty.copy(
                                thirdPartyPhone1 = phone1.trim()
                            )
                        )
                    }

                    //phone2
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = thirdParty.thirdPartyPhone2 ?: "",
                        label = "Phone2",
                        placeHolder = "Enter Phone2",
                        focusRequester = phone2FocusRequester,
                        onAction = { addressFocusRequester.requestFocus() }) { phone2 ->
                        viewModel.updateThirdParty(
                            thirdParty.copy(
                                thirdPartyPhone2 = phone2.trim()
                            )
                        )
                    }

                    //address
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = thirdParty.thirdPartyAddress ?: "",
                        label = "Address",
                        maxLines = 2,
                        placeHolder = "Enter address",
                        focusRequester = addressFocusRequester,
                        imeAction = ImeAction.Done,
                        onAction = { keyboardController?.hide() }) { address ->
                        viewModel.updateThirdParty(
                            thirdParty.copy(
                                thirdPartyAddress = address.trim()
                            )
                        )
                    }

                    UISwitch(
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        checked = thirdParty.thirdPartyDefault,
                        enabled = state.enableIsDefault,
                        text = "POS Default",
                    ) { isDefault ->
                        viewModel.updateThirdParty(
                            thirdParty.copy(
                                thirdPartyDefault = isDefault
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
                            viewModel.save()
                        }

                        UIImageButton(
                            modifier = Modifier
                                .weight(.33f)
                                .padding(3.dp),
                            icon = R.drawable.delete,
                            text = "Delete"
                        ) {
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

                SearchableDropdownMenuEx(
                    items = state.thirdPartyTypes.toMutableList(),
                    modifier = Modifier.padding(
                        top = 100.dp,
                        start = 10.dp,
                        end = 10.dp
                    ),
                    selectedId = thirdParty.thirdPartyType
                ) { thirdPartyTypeModel ->
                    thirdPartyTypeModel as ThirdPartyTypeModel
                    viewModel.updateThirdParty(
                        thirdParty.copy(
                            thirdPartyType = thirdPartyTypeModel.thirdPartyType.type
                        )
                    )
                }

                SearchableDropdownMenuEx(items = state.thirdParties.toMutableList(),
                    modifier = Modifier.padding(
                        top = 15.dp,
                        start = 10.dp,
                        end = 10.dp
                    ),
                    label = "Select ThirdParty",
                    selectedId = thirdParty.thirdPartyId,
                    onLoadItems = { viewModel.fetchThirdParties() },
                    leadingIcon = {modifier->
                        if (thirdParty.thirdPartyId.isNotEmpty()) {
                            Icon(
                                Icons.Default.RemoveCircleOutline,
                                contentDescription = "remove family",
                                tint = Color.Black,
                                modifier = modifier
                            )
                        }
                    },
                    onLeadingIconClick = {
                        viewModel.resetState()
                    }) { thirdParty ->
                    thirdParty as ThirdParty
                    viewModel.currentThirdParty = thirdParty.copy()
                    viewModel.updateThirdParty(thirdParty.copy())
                }
            }
        }
    }
}