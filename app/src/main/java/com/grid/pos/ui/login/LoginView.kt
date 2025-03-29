package com.grid.pos.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.grid.pos.App
import com.grid.pos.R
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.UIImageButton
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.navigation.Screen
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.ui.theme.homeLightBlue
import com.grid.pos.utils.Constants

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginView(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val passwordFocusRequester = remember { FocusRequester() }

    var passwordVisibility by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.openConnectionIfNeeded()
    }
    BackHandler {
        viewModel.backPressed()
    }
    GridPOSTheme {
        Scaffold(containerColor = SettingsModel.backgroundColor,
            topBar = {
                Surface(
                    shadowElevation = 0.dp,
                    color = SettingsModel.backgroundColor
                ) {
                    TopAppBar(colors = TopAppBarDefaults.mediumTopAppBarColors(
                        containerColor = SettingsModel.topBarColor
                    ),
                        title = {},
                        actions = {
                            TextButton(onClick = {
                                navController?.navigate(Screen.SettingsView.route)
                            }) {
                                Text(text = "Settings", color = homeLightBlue)
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
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(modifier = Modifier.height(24.dp))

                    // Title
                    Text(
                        text = "Grids POS",
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            fontSize = 35.sp,
                            color = homeLightBlue,
                        ),
                        modifier = Modifier
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    UITextField(modifier = Modifier,
                        defaultValue = viewModel.usernameState.value,
                        label = "Username",
                        placeHolder = "Username",
                        onAction = { passwordFocusRequester.requestFocus() }) { username ->
                        viewModel.usernameState.value = username
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    UITextField(modifier = Modifier,
                        defaultValue = viewModel.passwordState.value,
                        label = "Password",
                        placeHolder = "Password",
                        focusRequester = passwordFocusRequester,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        onAction = {
                            keyboardController?.hide()
                            viewModel.login(context) { destination ->
                                navController?.navigate(destination)
                            }
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
                        viewModel.passwordState.value = password
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    UIImageButton(
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(100.dp),
                        icon = R.drawable.login,
                        text = "Login",
                        iconSize = 60.dp,
                        isVertical = false
                    ) {
                        keyboardController?.hide()
                        viewModel.login(context) { destination ->
                            navController?.navigate(destination)
                        }
                    }

                    if (Constants.PLAY_STORE_VERSION) {

                        Spacer(modifier = Modifier.height(16.dp))

                        // Register label
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Don't have an account?",
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    fontWeight = FontWeight.Normal,
                                    fontStyle = FontStyle.Normal,
                                    fontSize = 16.sp,
                                    color = SettingsModel.textColor,
                                )
                            )
                            TextButton(onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(App.getInstance().getConfigValue("registration_url")))
                                context.startActivity(intent)
                            }) {
                                Text(
                                    text = "Register",
                                    textAlign = TextAlign.Center,
                                    style = TextStyle(
                                        fontWeight = FontWeight.SemiBold,
                                        fontStyle = FontStyle.Normal,
                                        fontSize = 16.sp,
                                        color = homeLightBlue,
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}