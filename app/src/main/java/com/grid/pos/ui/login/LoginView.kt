package com.grid.pos.ui.login

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.ToastModel
import com.grid.pos.ui.common.UIImageButton
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.GridPOSTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginView(
        modifier: Modifier = Modifier,
        navController: NavController? = null,
        sharedViewModel: SharedViewModel,
        viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.usersState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val passwordFocusRequester = remember { FocusRequester() }

    var usernameState by remember { mutableStateOf("") }
    var passwordState by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.openConnectionIfNeeded()
    }

    LaunchedEffect(state.warning) {
        state.warning?.value?.let { message ->
            scope.launch {
                if (state.needRegistration) {
                    sharedViewModel.isRegistering = true
                    sharedViewModel.showPopup(
                            true,
                            PopupModel(
                                    onConfirmation = {
                                        when (state.warningAction) {
                                            "Create" -> navController?.navigate("ManageUsersView")
                                            "Register" -> navController?.navigate("ManageCompaniesView")
                                            "Settings" -> navController?.navigate("SettingsView")
                                        }
                                        state.warning = null
                                        state.warningAction = null
                                    },
                                    dialogText = message,
                                    positiveBtnText = state.warningAction ?: "Register",
                                    negativeBtnText = "Cancel",
                                    cancelable = false
                            )
                    )
                } else {
                    sharedViewModel.isRegistering = false
                    sharedViewModel.showToastMessage(ToastModel(message = message,
                            actionButton = state.warningAction,
                            onActionClick = {
                                when (state.warningAction) {
                                    "Create" -> {
                                        state.warning = null
                                        state.warningAction = null
                                        navController?.navigate("ManageUsersView")
                                    }

                                    "Register" -> {
                                        state.warning = null
                                        state.warningAction = null
                                        navController?.navigate("ManageCompaniesView")
                                    }

                                    "Settings" -> {
                                        state.warning = null
                                        state.warningAction = null
                                        navController?.navigate("SettingsView")
                                    }
                                }
                            },
                            onDismiss = {
                                state.warning = null
                                state.warningAction = null
                            }))
                }
            }
        }
    }
    LaunchedEffect(state.isLoading) {
        sharedViewModel.showLoading(state.isLoading)
    }
    LaunchedEffect(state.needLicense) {
        if (state.needLicense) {
            state.needLicense = false
            navController?.navigate("LicenseView")
        }
    }
    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) {
            CoroutineScope(Dispatchers.IO).launch {
                sharedViewModel.activityState.value.isLoggedIn = true
                sharedViewModel.activityState.value.warning = null
                withContext(Dispatchers.Main) {
                    state.isLoading = true
                }
                sharedViewModel.initiateValues()
                withContext(Dispatchers.Main) {
                    state.isLoading = false
                    SettingsModel.currentUser?.let {
                        if (it.userPosMode && it.userTableMode) {
                            navController?.navigate("HomeView")
                        } else if (it.userPosMode) {
                            navController?.navigate("POSView")
                        } else if (it.userTableMode) {
                            navController?.navigate("TablesView")
                        } else {
                            navController?.navigate("HomeView")
                        }
                    }
                }
            }
        }
    }
    BackHandler {
        sharedViewModel.finish()
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
                                title = {
                                    Text(
                                            text = "Login",
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
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UITextField(modifier = Modifier.padding(10.dp),
                            defaultValue = usernameState,
                            label = "Username",
                            placeHolder = "Username",
                            onAction = { passwordFocusRequester.requestFocus() }) { username ->
                        usernameState = username
                    }

                    UITextField(modifier = Modifier.padding(10.dp),
                            defaultValue = passwordState,
                            label = "Password",
                            placeHolder = "Password",
                            focusRequester = passwordFocusRequester,
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                            onAction = {
                                keyboardController?.hide()
                                viewModel.login(
                                        context,
                                        usernameState.trim(),
                                        passwordState.trim()
                                )
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
                        passwordState = password
                    }
                    UIImageButton(
                            modifier = Modifier
                                    .wrapContentWidth()
                                    .height(100.dp)
                                    .padding(10.dp),
                            icon = R.drawable.login,
                            text = "Login",
                            iconSize = 60.dp,
                            isVertical = false
                    ) {
                        keyboardController?.hide()
                        viewModel.login(
                                context,
                                usernameState.trim(),
                                passwordState.trim()
                        )
                    }
                }
            }
        }
    }
}