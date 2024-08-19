package com.grid.pos.ui.settings

import android.app.backup.BackupManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.grid.pos.ActivityScopedViewModel
import com.grid.pos.App
import com.grid.pos.R
import com.grid.pos.di.AppModule
import com.grid.pos.interfaces.OnGalleryResult
import com.grid.pos.model.Event
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.LoadingIndicator
import com.grid.pos.ui.common.UIAlertDialog
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupView(
        modifier: Modifier = Modifier,
        navController: NavController? = null,
        activityViewModel: ActivityScopedViewModel,
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var warning by remember { mutableStateOf(Event("")) }
    var action by remember { mutableStateOf("") }
    var popupMessage by remember { mutableStateOf(Event("")) }
    var isPopupShown by remember { mutableStateOf(false) }
    var isRestoreWarningPopup by remember { mutableStateOf(false) }
    var shouldKill by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun restoreDbNow() {
        activityViewModel.launchFilePicker(object : OnGalleryResult {
            override fun onGalleryResult(uris: List<Uri>) {
                if (uris.isNotEmpty()) {
                    isLoading = true
                    CoroutineScope(Dispatchers.IO).launch {
                        FileUtils.restore(
                            context,
                            uris[0]
                        )
                        withContext(Dispatchers.Main) {
                            isLoading = false
                            popupMessage = Event("To complete the backup restoration process, the application needs to be restarted. Please close and reopen the app.")
                            delay(250L)
                            shouldKill = true
                            isPopupShown = true
                            isRestoreWarningPopup = false
                        }
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        isLoading = false
                        popupMessage = Event("Failed to restore your data.")
                        delay(250L)
                        shouldKill = false
                        isPopupShown = true
                        isRestoreWarningPopup = false
                    }
                }
            }
        },
            onPermissionDenied = {
                warning = Event("Permission Denied")
                action = "Settings"
            })
    }

    LaunchedEffect(warning) {
        if (warning.value.isNotEmpty()) {
            scope.launch {
                val snackBarResult = snackbarHostState.showSnackbar(
                    message = warning.value,
                    duration = SnackbarDuration.Short,
                    actionLabel = action
                )
                when (snackBarResult) {
                    SnackbarResult.Dismissed -> {}
                    SnackbarResult.ActionPerformed -> when (action) {
                        "Settings" -> activityViewModel.openAppStorageSettings()
                    }
                }
            }
        }
    }
    BackHandler {
        navController?.navigateUp()
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
                        title = {
                            Text(
                                text = "Backup & Restore",
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
                modifier = modifier
                    .fillMaxSize()
                    .padding(it)
                    .verticalScroll(rememberScrollState()),
            ) {
                UIButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(10.dp),
                    text = "Backup",
                    buttonColor = SettingsModel.buttonColor,
                    textColor = SettingsModel.buttonTextColor
                ) {
                    isLoading = true
                    CoroutineScope(Dispatchers.IO).launch {
                        FileUtils.backup()
                        withContext(Dispatchers.Main) {
                            isLoading = false
                            popupMessage = Event("Your data has been backed up successfully.")
                            delay(250L)
                            shouldKill = false
                            isPopupShown = true
                            isRestoreWarningPopup = false
                        }
                    }
                }
                UIButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(10.dp),
                    text = "Restore",
                    buttonColor = SettingsModel.buttonColor,
                    textColor = SettingsModel.buttonTextColor
                ) {
                    popupMessage = Event("Are you sure you want to restore, this will overwrite completely your current db and cannot be restored, continue?")
                    shouldKill = false
                    isPopupShown = true
                    isRestoreWarningPopup = true

                }
            }
        }
        AnimatedVisibility(
            visible = isPopupShown,
            enter = fadeIn(
                initialAlpha = 0.4f
            ),
            exit = fadeOut(
                animationSpec = tween(durationMillis = 250)
            )
        ) {
            UIAlertDialog(
                onDismissRequest = {
                    isPopupShown = false
                },
                onConfirmation = {
                    isPopupShown = false
                    if (isRestoreWarningPopup) {
                        restoreDbNow()
                    } else if (shouldKill) {
                        App.getInstance().killTheApp()
                    }
                },
                dialogTitle = "Alert.",
                dialogText = popupMessage.value,
                icon = Icons.Default.Info,
                positiveBtnText = if (isRestoreWarningPopup) "Restore" else if (shouldKill) "Close" else "Ok",
                negativeBtnText = if (isRestoreWarningPopup) "Cancel" else null,
                height = if (shouldKill || isRestoreWarningPopup) 250.dp else 200.dp
            )
        }
        LoadingIndicator(
            show = isLoading
        )
    }
}