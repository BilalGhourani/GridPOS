package com.grid.pos.ui.license

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.grid.pos.R
import com.grid.pos.SharedViewModel
import com.grid.pos.interfaces.OnGalleryResult
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.ToastModel
import com.grid.pos.ui.common.UIImageButton
import com.grid.pos.ui.navigation.Screen
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.ui.theme.licenseErrorColor
import com.grid.pos.utils.Utils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseView(
        modifier: Modifier = Modifier,
        navController: NavController? = null,
        sharedViewModel: SharedViewModel,
        viewModel: LicenseViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val context = LocalContext.current

    val deviceIDState by remember { mutableStateOf(Utils.getDeviceID(context)) }
    val scope = rememberCoroutineScope()
    var isBackPressed by remember { mutableStateOf(false) }

    fun handleBack() {
        if (state.isLoading) {
            return
        }
        if (isBackPressed) {
            return
        }
        isBackPressed = true
        navController?.navigateUp()
    }


    LaunchedEffect(state.warning) {
        state.warning?.value?.let { message ->
            scope.launch {
                sharedViewModel.showToastMessage(ToastModel(message))
            }
        }
    }

    LaunchedEffect(state.isLoading) {
        sharedViewModel.showLoading(state.isLoading)
    }

    LaunchedEffect(state.isDone) {
        if (state.isDone) {
            scope.launch {
                handleBack()
            }
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
                            IconButton(onClick = { navController?.navigate(Screen.SettingsView.route) }) {
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // Add Image
                Image(
                    painter = painterResource(id = R.drawable.contact_administrator), // Replace with your image resource
                    contentDescription = "Illustration",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )


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

                Row(
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Text(
                        text = "Device ID: $deviceIDState",
                        color = SettingsModel.textColor,
                        modifier = Modifier.weight(1f),
                        style = TextStyle(
                            textDecoration = TextDecoration.None,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp
                        )
                    )
                    IconButton(onClick = {
                        val clipboard: ClipboardManager? = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                        val clip = ClipData.newPlainText(
                            "label",
                            deviceIDState
                        )
                        clipboard?.setPrimaryClip(clip)
                        viewModel.showWarning("Device ID copied to clipboard")
                    }) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy, // Replace with your icon resource
                            contentDescription = "Copy Icon",
                            modifier = Modifier.size(24.dp),
                            tint = SettingsModel.buttonColor
                        )
                    }
                }


                Spacer(modifier = Modifier.height(30.dp))

                UIImageButton(
                    modifier = Modifier
                        .wrapContentWidth()
                        .height(100.dp)
                        .padding(10.dp),
                    icon = R.drawable.pick_file,
                    text = "Select License File",
                    iconSize = 60.dp,
                    isVertical = false
                ) {
                    sharedViewModel.launchFilePicker("*/*",
                        object : OnGalleryResult {
                            override fun onGalleryResult(uris: List<Uri>) {
                                if (uris.isNotEmpty()) {
                                    viewModel.copyLicenseFile(
                                        context,
                                        uris[0]
                                    )
                                }
                            }
                        },
                        onPermissionDenied = {
                            viewModel.showWarning(
                                "Permission Denied",
                                "Settings"
                            )
                        })
                }

            }
        }
    }
}