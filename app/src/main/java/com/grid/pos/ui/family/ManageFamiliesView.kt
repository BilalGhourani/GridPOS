package com.grid.pos.ui.family

import android.net.Uri
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.grid.pos.MainActivity
import com.grid.pos.data.Family.Family
import com.grid.pos.interfaces.OnGalleryResult
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.LoadingIndicator
import com.grid.pos.ui.common.SearchableDropdownMenu
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun ManageFamiliesView(
        navController: NavController? = null,
        modifier: Modifier = Modifier,
        mainActivity: MainActivity,
        viewModel: ManageFamiliesViewModel = hiltViewModel()
) {
    val manageFamiliesState: ManageFamiliesState by viewModel.manageFamiliesState.collectAsState(
        ManageFamiliesState()
    )
    val keyboardController = LocalSoftwareKeyboardController.current
    val imageFocusRequester = remember { FocusRequester() }

    var nameState by remember { mutableStateOf("") }
    var imageState by remember { mutableStateOf("") }
    GridPOSTheme {
        Scaffold(containerColor = SettingsModel.backgroundColor, topBar = {
            Surface(shadowElevation = 3.dp, color = SettingsModel.backgroundColor) {
                TopAppBar(colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = SettingsModel.topBarColor
                ), navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back", tint = SettingsModel.buttonColor
                        )
                    }
                }, title = {
                    Text(
                        text = "Manage Families", color = SettingsModel.textColor, fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
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
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter
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
                            label = nameState.ifEmpty { "Select Family" },
                        ) { family ->
                            family as Family
                            manageFamiliesState.selectedFamily = family
                            nameState = family.familyName ?: ""
                            imageState = family.familyImage ?: ""
                        }

                        UITextField(modifier = Modifier.padding(10.dp), defaultValue = nameState,
                            label = "Name", placeHolder = "Enter Name",
                            onAction = { imageFocusRequester.requestFocus() }) { name ->
                            nameState = name
                            manageFamiliesState.selectedFamily.familyName = name
                        }

                        UITextField(modifier = Modifier.padding(10.dp), defaultValue = imageState,
                            label = "Image", placeHolder = "Image",
                            focusRequester = imageFocusRequester, imeAction = ImeAction.Done,
                            onAction = { keyboardController?.hide() }, trailingIcon = {
                                IconButton(onClick = {
                                    mainActivity.launchGalleryPicker(
                                        mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
                                        object : OnGalleryResult {
                                            override fun onGalleryResult(uris: List<Uri>) {
                                                if (uris.isNotEmpty()) {
                                                    manageFamiliesState.isLoading = true
                                                    CoroutineScope(Dispatchers.IO).launch {
                                                        val file = File(uris[0].path)
                                                        val internalPath = Utils.saveToInternalStorage(
                                                            context = mainActivity,
                                                            parent = "family", file,
                                                            nameState.ifEmpty { file.name })
                                                        withContext(Dispatchers.Main) {
                                                            manageFamiliesState.isLoading = false
                                                            if (internalPath != null) {
                                                                imageState = internalPath
                                                                manageFamiliesState.selectedFamily.familyImage = imageState
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                        })
                                }) {
                                    Icon(
                                        Icons.Default.Image, contentDescription = "Image",
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
                                    .padding(3.dp), text = "Save"
                            ) {
                                viewModel.saveFamily()
                            }

                            UIButton(
                                modifier = Modifier
                                    .weight(.33f)
                                    .padding(3.dp), text = "Delete"
                            ) {
                                viewModel.deleteSelectedFamily()
                            }

                            UIButton(
                                modifier = Modifier
                                    .weight(.33f)
                                    .padding(3.dp), text = "Close"
                            ) {
                                navController?.popBackStack()
                            }
                        }

                    }
                }
            }
        }
        LoadingIndicator(
            show = manageFamiliesState.isLoading
        )
        if (manageFamiliesState.clear) {
            manageFamiliesState.selectedFamily = Family()
            manageFamiliesState.selectedFamily.familyCompanyId = ""
            nameState = ""
            manageFamiliesState.clear = false
        }
    }
}