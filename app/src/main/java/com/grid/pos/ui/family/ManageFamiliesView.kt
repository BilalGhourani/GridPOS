package com.grid.pos.ui.family

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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.grid.pos.data.Company.Company
import com.grid.pos.data.Family.Family
import com.grid.pos.ui.common.LoadingIndicator
import com.grid.pos.ui.common.SearchableDropdownMenu
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.GridPOSTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageFamiliesView(
    navController: NavController? = null,
    modifier: Modifier = Modifier,
    viewModel: ManageFamiliesViewModel = hiltViewModel()
) {
    val manageFamiliesState: ManageFamiliesState by viewModel.manageFamiliesState.collectAsState(
        ManageFamiliesState()
    )
    var nameState by remember { mutableStateOf("") }
    var companyIdState by remember { mutableStateOf("") }
    GridPOSTheme {
        Scaffold(
            topBar = {
                Surface(shadowElevation = 3.dp, color = Color.White) {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = { navController?.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        title = {
                            Text(
                                text = "Manage Families",
                                color = Color.Black,
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
                            if (nameState.isNotEmpty()) nameState else "Select Family",
                        ) {
                            it as Family
                            manageFamiliesState.selectedFamily = it
                            nameState = it.familyName ?: ""
                            companyIdState = it.familyCompanyId ?: ""
                        }

                        UITextField(
                            modifier = Modifier.padding(10.dp),
                            defaultValue = nameState,
                            label = "Name",
                            placeHolder = "Enter Name"
                        ) {
                            nameState = it
                            manageFamiliesState.selectedFamily.familyName = it
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