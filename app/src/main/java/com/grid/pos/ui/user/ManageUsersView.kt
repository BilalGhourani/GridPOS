package com.grid.pos.ui.user

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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.grid.pos.R
import com.grid.pos.SharedViewModel
import com.grid.pos.data.user.User
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.ToastModel
import com.grid.pos.ui.common.SearchableDropdownMenuEx
import com.grid.pos.ui.common.UIImageButton
import com.grid.pos.ui.common.UISwitch
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Extension.decryptCBC

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun ManageUsersView(
    navController: NavController? = null,
    modifier: Modifier = Modifier,
    sharedViewModel: SharedViewModel,
    viewModel: ManageUsersViewModel = hiltViewModel()
) {
    val state by viewModel.manageUsersState.collectAsStateWithLifecycle()
    val user = viewModel.userState.collectAsState().value

    val keyboardController = LocalSoftwareKeyboardController.current
    val usernameFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }

    var passwordVisibility by remember { mutableStateOf(false) }

    LaunchedEffect(state.warning) {
        state.warning?.value?.let { message ->
            if (state.action == "done" && sharedViewModel.isRegistering) {
                sharedViewModel.isRegistering = false
                sharedViewModel.showPopup(
                    true,
                    PopupModel(
                        onDismissRequest = {
                            navController?.navigateUp()
                        },
                        onConfirmation = {
                            navController?.navigateUp()
                        },
                        dialogText = message,
                        positiveBtnText = "Login",
                        negativeBtnText = null,
                        cancelable = false
                    )
                )
            } else {
                sharedViewModel.showToastMessage(
                    ToastModel(
                        message = message
                    )
                )
            }
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
        if (user.didChanged(
                viewModel.currentUser
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
                        viewModel.save(
                            sharedViewModel.isRegistering
                        )
                    }
                    dialogText = "Do you want to save your changes"
                    positiveBtnText = "Save"
                    negativeBtnText = "Close"
                })
            return
        }
        if (state.users.isNotEmpty()) {
            sharedViewModel.users = state.users
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
                                text = "Manage Users",
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
            }) { it ->
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(it)
                    .background(color = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 90.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = user.userName ?: "",
                        label = "Name",
                        placeHolder = "Enter Name",
                        onAction = { usernameFocusRequester.requestFocus() }) { name ->
                        viewModel.updateUser(
                            user.copy(
                                userName = name.trim()
                            )
                        )
                    }

                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = user.userUsername ?: "",
                        label = "Username",
                        placeHolder = "Enter Username",
                        focusRequester = usernameFocusRequester,
                        onAction = { passwordFocusRequester.requestFocus() }) { username ->
                        viewModel.updateUser(
                            user.copy(
                                userUsername = username.trim()
                            )
                        )
                    }

                    UITextField(modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                        defaultValue = user.userPassword ?: "",
                        label = "Password",
                        placeHolder = "Enter Password",
                        focusRequester = passwordFocusRequester,
                        keyboardType = KeyboardType.Password,
                        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        imeAction = ImeAction.Done,
                        onAction = { keyboardController?.hide() },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                                Icon(
                                    imageVector = if (passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (passwordVisibility) "Hide password" else "Show password",
                                    tint = SettingsModel.buttonColor
                                )
                            }
                        }) { password ->
                        viewModel.updateUser(
                            user.copy(
                                userPassword = password.trim()
                            )
                        )
                    }

                    UISwitch(
                        modifier = Modifier.padding(
                            horizontal = 15.dp
                        ),
                        checked = user.userPosMode,
                        text = "Enable POS Mode",
                    ) { isPOSMode ->
                        viewModel.updateUser(
                            user.copy(
                                userPosMode = isPOSMode
                            )
                        )
                    }

                    UISwitch(
                        modifier = Modifier.padding(
                            horizontal = 15.dp
                        ),
                        checked = user.userTableMode,
                        text = "Enable Table Mode",
                    ) { isTableMode ->
                        viewModel.updateUser(
                            user.copy(
                                userTableMode = isTableMode
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
                            viewModel.save(
                                sharedViewModel.isRegistering
                            )
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
                SearchableDropdownMenuEx(items = state.users.toMutableList(),
                    modifier = Modifier.padding(
                        top = 15.dp,
                        start = 10.dp,
                        end = 10.dp
                    ),
                    label = "Select User",
                    selectedId = user.userId,
                    onLoadItems = { viewModel.fetchUsers() },
                    leadingIcon = {
                        if (user.userId.isNotEmpty()) {
                            Icon(
                                Icons.Default.RemoveCircleOutline,
                                contentDescription = "remove family",
                                tint = Color.Black,
                                modifier = it
                            )
                        }
                    },
                    onLeadingIconClick = {
                        viewModel.resetState()
                    }) { selectedUser ->
                    selectedUser as User
                    viewModel.currentUser = selectedUser.copy()
                    viewModel.updateUser(
                        selectedUser.copy(
                            userPassword = selectedUser.userPassword?.decryptCBC() ?: ""
                        )
                    )
                }
            }
        }
    }
}