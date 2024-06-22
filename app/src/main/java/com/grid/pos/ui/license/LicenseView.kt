package com.grid.pos.ui.license

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.grid.pos.ActivityScopedViewModel
import com.grid.pos.R
import com.grid.pos.interfaces.OnGalleryResult
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.UserType
import com.grid.pos.ui.common.LoadingIndicator
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.ui.theme.LightBlue
import com.grid.pos.ui.theme.deviceIDColor
import com.grid.pos.ui.theme.licenseErrorColor
import com.grid.pos.utils.FileUtils
import com.grid.pos.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseView(
        modifier: Modifier = Modifier,
        navController: NavController? = null,
        activityScopedViewModel: ActivityScopedViewModel,
        viewModel: LicenseViewModel = hiltViewModel()
) {
    val state: LicenseState by viewModel.state.collectAsState(
        LicenseState()
    )
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val deviceIDState by remember { mutableStateOf(Utils.getDeviceID(context)) }
    var fileState by remember { mutableStateOf("") }
    var isPopupVisible by remember { mutableStateOf(false) }

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

    fun handleBack() {
        if (SettingsModel.getUserType() == UserType.TABLE) {
            isPopupVisible = true
        } else {
            navController?.navigateUp()
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
                    TopAppBar(colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = SettingsModel.topBarColor),
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
                                text = "License",
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
            Column(
                modifier = modifier.padding(it),
                verticalArrangement = Arrangement.Center
            ) {

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    text = "Local server is not responding, contact your system administrator",
                    style = TextStyle(
                        textDecoration = TextDecoration.None,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp
                    ),
                    color = licenseErrorColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    text = "Your Device ID ",
                    color = SettingsModel.textColor,
                    style = TextStyle(
                        textDecoration = TextDecoration.None,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    text = deviceIDState,
                    color = deviceIDColor,
                    style = TextStyle(
                        textDecoration = TextDecoration.None,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(3.dp))

                TextButton(
                    onClick = {
                        val clipboard: ClipboardManager? = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                        val clip = ClipData.newPlainText(
                            "label",
                            deviceIDState
                        )
                        clipboard?.setPrimaryClip(clip)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text(
                        "Copy",
                        color = LightBlue,
                        style = TextStyle(
                            textDecoration = TextDecoration.None,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                UITextField(modifier = Modifier.padding(horizontal = 10.dp),
                    defaultValue = fileState,
                    label = "License File",
                    readOnly = true,
                    placeHolder = "Browse your License File",
                    imeAction = ImeAction.Done,
                    onAction = { keyboardController?.hide() },
                    trailingIcon = {
                        IconButton(onClick = {
                            activityScopedViewModel.launchFilePicker(object : OnGalleryResult {
                                override fun onGalleryResult(uris: List<Uri>) {
                                    if (uris.isNotEmpty()) {
                                        state.isLoading = true
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val path = FileUtils.saveToExternalStorage(
                                                context = context,
                                                parent = "licenses",
                                                uris[0],
                                                "license"
                                            )
                                            withContext(Dispatchers.Main) {
                                                state.isLoading = false
                                                if (path != null) {
                                                    fileState = path
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
                    }) { img ->
                    fileState = img
                }

            }
        }

        LoadingIndicator(
            show = state.isLoading
        )
    }
}