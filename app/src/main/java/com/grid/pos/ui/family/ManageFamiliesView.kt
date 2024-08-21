package com.grid.pos.ui.family

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.grid.pos.ActivityScopedViewModel
import com.grid.pos.R
import com.grid.pos.data.Family.Family
import com.grid.pos.interfaces.OnGalleryResult
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.SearchableDropdownMenu
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun ManageFamiliesView(
        modifier: Modifier = Modifier,
        navController: NavController? = null,
        activityScopedViewModel: ActivityScopedViewModel,
        viewModel: ManageFamiliesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val manageFamiliesState: ManageFamiliesState by viewModel.manageFamiliesState.collectAsState(
        ManageFamiliesState()
    )
    viewModel.fillCachedFamilies(activityScopedViewModel.families)
    val keyboardController = LocalSoftwareKeyboardController.current
    val imageFocusRequester = remember { FocusRequester() }

    var nameState by remember { mutableStateOf("") }
    var imageState by remember { mutableStateOf("") }
    var oldImage: String? = null

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(manageFamiliesState.warning) {
        manageFamiliesState.warning?.value?.let { message ->
            scope.launch {
                val snackBarResult = snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short,
                    actionLabel = manageFamiliesState.actionLabel
                )
                when (snackBarResult) {
                    SnackbarResult.Dismissed -> {}
                    SnackbarResult.ActionPerformed -> when (manageFamiliesState.actionLabel) {
                        "Settings" -> activityScopedViewModel.openAppStorageSettings()
                    }
                }
            }
        }
    }

    LaunchedEffect(manageFamiliesState.isLoading) {
        activityScopedViewModel.showLoading(manageFamiliesState.isLoading)
    }

    fun saveFamily() {
        oldImage?.let { old ->
            FileUtils.deleteFile(
                context,
                old
            )
        }
        viewModel.saveFamily(manageFamiliesState.selectedFamily)
    }

    var saveAndBack by remember { mutableStateOf(false) }
    fun handleBack() {
        if (viewModel.currentFamily != null && manageFamiliesState.selectedFamily.didChanged(
                viewModel.currentFamily!!
            )
        ) {
            activityScopedViewModel.showPopup(true,
                PopupModel().apply {
                    onDismissRequest = {
                        viewModel.currentFamily = null
                        handleBack()
                    }
                    onConfirmation = {
                        saveAndBack = true
                        saveFamily()
                    }
                    dialogTitle = "Alert."
                    dialogText = "Do you want to save your changes"
                    positiveBtnText = "Save"
                    negativeBtnText = "Close"
                    icon = Icons.Default.Info
                })
            return
        }
        if (manageFamiliesState.families.isNotEmpty()) {
            activityScopedViewModel.families = manageFamiliesState.families
        }
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
                                text = "Manage Families",
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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SearchableDropdownMenu(
                            items = manageFamiliesState.families.toMutableList(),
                            modifier = Modifier.padding(10.dp),
                            label = "Select Family",
                            selectedId = manageFamiliesState.selectedFamily.familyId
                        ) { family ->
                            family as Family
                            viewModel.currentFamily = family.copy()
                            manageFamiliesState.selectedFamily = family
                            nameState = family.familyName ?: ""
                            imageState = family.familyImage ?: ""
                        }

                        UITextField(modifier = Modifier.padding(10.dp),
                            defaultValue = nameState,
                            label = "Name",
                            placeHolder = "Enter Name",
                            onAction = { imageFocusRequester.requestFocus() }) { name ->
                            nameState = name
                            manageFamiliesState.selectedFamily.familyName = name
                        }

                        UITextField(modifier = Modifier.padding(10.dp),
                            defaultValue = imageState,
                            label = "Image",
                            placeHolder = "Image",
                            focusRequester = imageFocusRequester,
                            imeAction = ImeAction.Done,
                            onAction = { keyboardController?.hide() },
                            trailingIcon = {
                                IconButton(onClick = {
                                    activityScopedViewModel.launchGalleryPicker(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
                                        object : OnGalleryResult {
                                            override fun onGalleryResult(uris: List<Uri>) {
                                                if (uris.isNotEmpty()) {
                                                    manageFamiliesState.isLoading = true
                                                    CoroutineScope(Dispatchers.IO).launch {
                                                        val internalPath = FileUtils.saveToExternalStorage(context = context,
                                                            parent = "family",
                                                            uris[0],
                                                            nameState.trim().replace(
                                                                " ",
                                                                "_"
                                                            ).ifEmpty { "family" })
                                                        withContext(Dispatchers.Main) {
                                                            manageFamiliesState.isLoading = false
                                                            if (internalPath != null) {
                                                                oldImage = imageState
                                                                imageState = internalPath
                                                                manageFamiliesState.selectedFamily.familyImage = imageState
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
                            imageState = img
                            manageFamiliesState.selectedFamily.familyImage = img
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(10.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            UIButton(
                                modifier = Modifier
                                    .weight(.33f)
                                    .padding(3.dp),
                                text = "Save"
                            ) {
                                viewModel.currentFamily = null
                                saveFamily()
                            }

                            UIButton(
                                modifier = Modifier
                                    .weight(.33f)
                                    .padding(3.dp),
                                text = "Delete"
                            ) {
                                oldImage?.let { old ->
                                    FileUtils.deleteFile(
                                        context,
                                        old
                                    )
                                }
                                if (imageState.isNotEmpty()) {
                                    FileUtils.deleteFile(
                                        context,
                                        imageState
                                    )
                                }
                                viewModel.deleteSelectedFamily(manageFamiliesState.selectedFamily)
                            }

                            UIButton(
                                modifier = Modifier
                                    .weight(.33f)
                                    .padding(3.dp),
                                text = "Close"
                            ) {
                                handleBack()
                            }
                        }

                    }
                }
            }
        }
        if (manageFamiliesState.clear) {
            manageFamiliesState.selectedFamily = Family()
            manageFamiliesState.selectedFamily.familyCompanyId = ""
            nameState = ""
            imageState = ""
            manageFamiliesState.clear = false
            if (saveAndBack) {
                viewModel.currentFamily = null
                handleBack()
            }
        }
    }
}