package com.grid.pos.ui.settings.setupReports

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.grid.pos.R
import com.grid.pos.model.ReportCountry
import com.grid.pos.model.ReportLanguage
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.SearchableDropdownMenuEx
import com.grid.pos.ui.common.UIImageButton
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupReportView(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    viewModel: SetupReportViewModel = hiltViewModel()
) {
    val state: SetupReportState by viewModel.state.collectAsState(SetupReportState())

    val context = LocalContext.current
    fun handleBack() {
        navController?.navigateUp()
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
                                text = "Setup Reports",
                                color = SettingsModel.textColor,
                                fontSize = 16.sp,
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
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(top = 240.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UIImageButton(
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(100.dp)
                            .padding(10.dp),
                        icon = R.drawable.add,
                        text = "Add ${viewModel.getReportType() ?: ""}",
                        iconSize = 60.dp,
                        isVertical = false
                    ) {
                        viewModel.addReport(context) {
                            navController?.navigateUp()
                        }
                    }
                }

                SearchableDropdownMenuEx(
                    items = Utils.getReportLanguages(true).toMutableList(),
                    modifier = Modifier.padding(
                        top = 120.dp,
                        start = 10.dp,
                        end = 10.dp
                    ),
                    placeholder = "Report Language",
                    label = state.language.value.ifEmpty { "Select Language" },
                    selectedId = state.language.code,
                    maxHeight = 290.dp
                ) { reportLan ->
                    viewModel.updateState(
                        state.copy(
                            language = (reportLan as ReportLanguage).language
                        )
                    )
                }
                SearchableDropdownMenuEx(
                    items = state.countries.toMutableList(),
                    modifier = Modifier.padding(
                        top = 15.dp,
                        start = 10.dp,
                        end = 10.dp
                    ),
                    onLoadItems = {
                        viewModel.fetchCountries()
                    },
                    placeholder = "Report Country",
                    label = state.country.ifEmpty { "Select Language" },
                    selectedId = state.country,
                    maxHeight = 290.dp
                ) { reportCountry ->
                    viewModel.updateState(
                        state.copy(
                            country = (reportCountry as ReportCountry).value
                        )
                    )
                }
            }
        }
    }
}