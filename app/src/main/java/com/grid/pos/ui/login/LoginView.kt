package com.grid.pos.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.grid.pos.R
import com.grid.pos.ui.common.LoadingIndicator
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.Blue
import com.grid.pos.ui.theme.GridPOSTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun LoginView(
    navController: NavController? = null,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val loginState: LoginState by viewModel.usersState.collectAsState(LoginState())
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val passwordFocusRequester = remember { FocusRequester() }
    LaunchedEffect(loginState.warning, loginState.isLoggedIn) {
        if (!loginState.warning.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.Main).launch {
                snackbarHostState.showSnackbar(
                    message = loginState.warning!!,
                    duration = SnackbarDuration.Short,
                )
            }
        }
        if (loginState.isLoggedIn) {
            navController?.navigate("HomeView")
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
                        /*navigationIcon = {
                            IconButton(onClick = { navController?.navigateUp() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },*/
                        title = {
                            Text(
                                text = "Login",
                                color = Color.Black,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        },
                        actions = {
                            IconButton(
                                onClick = { navController?.navigate("SettingsView") }
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_settings),
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }
            }
        ) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(it)
                    .background(color = Color.Transparent)
            ) {
                var usernameState by remember { mutableStateOf("") }
                var passwordState by remember { mutableStateOf("") }
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = usernameState,
                            label = "Username",
                            placeHolder = "Username",
                            onAction = { passwordFocusRequester.requestFocus() }
                        ) {
                            usernameState = it
                        }

                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = passwordState,
                            label = "Password",
                            placeHolder = "Password",
                            focusRequester = passwordFocusRequester,
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                            onAction = {
                                keyboardController?.hide()
                                viewModel.login(usernameState, passwordState)
                            }
                        ) {
                            passwordState = it
                        }

                        ElevatedButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .padding(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue),
                            onClick = {
                                keyboardController?.hide()
                                viewModel.login(usernameState, passwordState)
                            }
                        ) {
                            Text("Log In")
                        }
                    }
                }
            }
        }
        LoadingIndicator(
            show = loginState.isLoading
        )
    }
}