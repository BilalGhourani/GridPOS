package com.grid.pos.ui.family

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.grid.pos.MainActivity
import com.grid.pos.R
import com.grid.pos.data.Company.Company
import com.grid.pos.data.Family.Family
import com.grid.pos.interfaces.OnGalleryResult
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.LoadingIndicator
import com.grid.pos.ui.common.SearchableDropdownMenu
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils

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
    var nameState by remember { mutableStateOf("") }
    var companyIdState by remember { mutableStateOf("") }
    var imageState by remember {
        mutableStateOf(
            Uri.parse(
                manageFamiliesState.selectedFamily.familyImage ?: ""
            )
        )
    }
    GridPOSTheme {
        Scaffold(
            containerColor = SettingsModel.backgroundColor,
            topBar = {
                Surface(shadowElevation = 3.dp, color = SettingsModel.backgroundColor) {
                    TopAppBar(
                        colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = SettingsModel.topBarColor),
                        navigationIcon = {
                            IconButton(onClick = { navController?.popBackStack() }) {
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
                        })
                }
            }
        ) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(it)
                    .background(color = Color.Transparent)
            ) {
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
                        SearchableDropdownMenu(
                            items = manageFamiliesState.families.toMutableList(),
                            modifier = Modifier.padding(10.dp),
                            label =
                            nameState.ifEmpty { "Select Family" },
                        ) { family ->
                            family as Family
                            manageFamiliesState.selectedFamily = family
                            nameState = family.familyName ?: ""
                            companyIdState = family.familyCompanyId ?: ""
                            imageState = Uri.parse(family.familyImage ?: "")
                            /* if (!family.familyImage.isNullOrEmpty()) {
                                 imageState =
                                     Uri.parse(viewModel.getDownloadUrl(family.familyImage ?: ""))
                             } else {
                                 imageState = Uri.parse("")
                             }*/
                        }

                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = nameState,
                            label = "Name",
                            placeHolder = "Enter Name"
                        ) {name->
                            nameState = name
                            manageFamiliesState.selectedFamily.familyName = name
                        }

                        SearchableDropdownMenu(
                            items = manageFamiliesState.companies.toMutableList(),
                            modifier = Modifier.padding(10.dp),
                            label = "Select Company",
                            selectedId = companyIdState
                        ) { company ->
                            company as Company
                            companyIdState = company.companyId
                            manageFamiliesState.selectedFamily.familyCompanyId =
                                company.companyId
                        }
                        val width = LocalConfiguration.current.screenWidthDp * 0.8
                        GlideImage(
                            model = imageState,
                            loading = placeholder(R.drawable.placeholder),
                            failure = placeholder(R.drawable.placeholder),
                            contentScale = ContentScale.Crop,
                            contentDescription = "Family Image",
                            modifier = Modifier
                                .width(width.dp)
                                .height(width.dp)
                                .padding(vertical = 10.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .clickable {
                                    mainActivity.launchGalleryPicker(
                                        mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
                                        object : OnGalleryResult {
                                            override fun onGalleryResult(uris: List<Uri>) {
                                                if (uris.isNotEmpty()) {
                                                    imageState = uris[0]
                                                    manageFamiliesState.selectedFamily.familyImage =
                                                        imageState.toString()
                                                    val id = Utils.generateRandomUuidString()
                                                    manageFamiliesState.selectedFamily.familyId = id

                                                    viewModel.uploadImage(id, imageState)
                                                }
                                            }

                                        }
                                    )
                                },
                        )

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
                                viewModel.saveFamily()
                            }

                            UIButton(
                                modifier = Modifier
                                    .weight(.33f)
                                    .padding(3.dp),
                                text = "Delete"
                            ) {
                                viewModel.deleteSelectedFamily()
                            }

                            UIButton(
                                modifier = Modifier
                                    .weight(.33f)
                                    .padding(3.dp),
                                text = "Close"
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
            companyIdState = ""
            manageFamiliesState.clear = false
        }
    }
}