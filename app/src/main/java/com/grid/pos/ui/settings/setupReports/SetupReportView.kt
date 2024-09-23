package com.grid.pos.ui.settings.setupReports

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.grid.pos.ActivityScopedViewModel
import com.grid.pos.interfaces.OnGalleryResult
import com.grid.pos.model.Event
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.SearchableDropdownMenuEx
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.FileUtils
import com.grid.pos.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupReportView(
        modifier: Modifier = Modifier,
        navController: NavController? = null,
        activityViewModel: ActivityScopedViewModel,
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var warning by remember { mutableStateOf(Event("")) }
    var action by remember { mutableStateOf("") }

    var countryState by remember { mutableStateOf("Default") }
    var languageState by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun addReport(reportType: String) {
        if (languageState.isEmpty()) {
            warning = Event("Please select a language!")
            return
        }
        activityViewModel.launchFilePicker("text/html",
            object : OnGalleryResult {
                override fun onGalleryResult(uris: List<Uri>) {
                    if (uris.isNotEmpty()) {
                        isLoading = true
                        CoroutineScope(Dispatchers.IO).launch {
                            FileUtils.saveToInternalStorage(
                                context,
                                "Reports/$countryState",
                                uris[0],
                                "$reportType-$languageState.html"
                            )
                            withContext(Dispatchers.Main) {
                                isLoading = false
                                warning = Event("$reportType has been added successfully")
                                action = ""
                            }
                        }
                    } else {
                        CoroutineScope(Dispatchers.Main).launch {
                            isLoading = false
                            warning = Event("Failed to add $reportType")
                            action = ""
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
    LaunchedEffect(isLoading) {
        activityViewModel.showLoading(isLoading)
    }
    fun handleBack() {
        navController?.navigateUp()
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
                                text = "Setup Reports",
                                color = SettingsModel.textColor,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        })
                }
            }) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(it)
                    .verticalScroll(rememberScrollState()),
            ) {

                UITextField(
                    modifier = Modifier.padding(10.dp),
                    defaultValue = countryState,
                    enabled = false,
                    label = "Country",
                    placeHolder = "Country",
                ) { country ->
                    countryState = country
                }

                SearchableDropdownMenuEx(items = Utils.reportLanguages,
                    modifier = Modifier.padding(10.dp),
                    enableSearch = false,
                    label = languageState.ifEmpty { "Select Language" },
                    selectedId = languageState,
                    leadingIcon = { modifier ->
                        if (languageState.isNotEmpty()) {
                            Icon(
                                Icons.Default.RemoveCircleOutline,
                                contentDescription = "remove family",
                                tint = Color.Black,
                                modifier = modifier
                            )
                        }
                    },
                    onLeadingIconClick = {
                        languageState = ""
                    }) { type ->
                    languageState = type.getName()
                }

                Spacer(modifier = Modifier.height(20.dp))

                UIButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(10.dp),
                    text = if (activityViewModel.isPaySlip) "Add Payslip" else "Add PayTicket",
                    buttonColor = SettingsModel.buttonColor,
                    textColor = SettingsModel.buttonTextColor
                ) {
                    addReport(if (activityViewModel.isPaySlip) "Payslip" else "PayTicket")
                }
            }
        }
    }
}