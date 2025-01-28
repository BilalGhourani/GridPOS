package com.grid.pos.ui.reports

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.grid.pos.BuildConfig
import com.grid.pos.R
import com.grid.pos.SharedViewModel
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.EditableDateInputField
import com.grid.pos.ui.common.UIImageButton
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.ui.theme.LightBlue
import com.grid.pos.ui.theme.LightGreen
import com.grid.pos.utils.DateHelper
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesReportsView(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    sharedViewModel: SharedViewModel,
    viewModel: SalesReportsViewModel = hiltViewModel()
) {
    val state by viewModel.reportsState.collectAsStateWithLifecycle()
    val context = LocalContext.current


    fun shareExcelSheet(action: String) {
        viewModel.reportFile?.let { file ->
            val shareIntent = Intent()
            shareIntent.setAction(action)
            val attachment = FileProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID,
                file
            )
            shareIntent.putExtra(
                Intent.EXTRA_STREAM,
                attachment
            )
            shareIntent.setType("application/vnd.ms-excel")

            sharedViewModel.startChooserActivity(
                Intent.createChooser(
                    shareIntent,
                    "send"
                )
            )
        }
    }

    var fromDateState by remember {
        mutableStateOf(
            DateHelper.getDateInFormat(
                DateHelper.editDate(Date(), 0, 0, 0),
                viewModel.dateFormat
            )
        )
    }
    var toDateState by remember {
        mutableStateOf(
            DateHelper.getDateInFormat(
                DateHelper.editDate(Date(), 23, 59, 59),
                viewModel.dateFormat
            )
        )
    }

    var isPopupVisible by remember { mutableStateOf(false) }
    var isBottomSheetVisible by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()


    LaunchedEffect(
        state.warning,
        state.isDone
    ) {
        state.warning?.value?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short,
                )
            }
        }

        if (state.isDone) {
            isBottomSheetVisible = true
            state.isDone = false
        }
    }

    LaunchedEffect(state.isLoading) {
        sharedViewModel.showLoading(state.isLoading)
    }

    fun handleBack() {
        if (state.isLoading) {
            isPopupVisible = true
        } else {
            viewModel.closeConnectionIfNeeded()
            viewModel.viewModelScope.cancel()
            navController?.navigateUp()
        }
    }

    LaunchedEffect(isPopupVisible) {
        sharedViewModel.showPopup(
            isPopupVisible,
            if (!isPopupVisible) null else PopupModel().apply {
                onDismissRequest = {
                    isPopupVisible = false
                }
                onConfirmation = {
                    state.isLoading = false
                    isPopupVisible = false
                    handleBack()
                }
                dialogText = "Are you sure you want to cancel the reports?"
                positiveBtnText = "Cancel"
                negativeBtnText = "Close"
            })
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
                                text = "Reports",
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
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                EditableDateInputField(
                    modifier = Modifier.padding(10.dp),
                    date = fromDateState,
                    dateTimeFormat = viewModel.dateFormat,
                    label = "From"
                ) { dateStr ->
                    val date = DateHelper.getDateFromString(dateStr, viewModel.dateFormat)
                    if (date.after(Date())) {
                        sharedViewModel.showPopup(
                            true, PopupModel(
                                dialogText = "From date should be today or before, please select again",
                                negativeBtnText = null
                            )
                        )
                    } else {
                        fromDateState = dateStr
                    }
                }


                EditableDateInputField(
                    modifier = Modifier.padding(10.dp),
                    date = toDateState,
                    dateTimeFormat = viewModel.dateFormat,
                    label = "To"
                ) { dateStr ->
                    toDateState = dateStr
                }

                UIImageButton(
                    modifier = Modifier
                        .wrapContentWidth()
                        .height(100.dp)
                        .padding(10.dp),
                    icon = R.drawable.add,
                    text = "Generate",
                    iconSize = 60.dp,
                    isVertical = false
                ) {
                    val from = DateHelper.getDateFromString(fromDateState, viewModel.dateFormat)
                    val to = DateHelper.getDateFromString(toDateState, viewModel.dateFormat)
                    viewModel.fetchInvoices(
                        from,
                        to
                    )
                }
            }
        }

        if (isBottomSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = {
                    isBottomSheetVisible = false
                },
                sheetState = bottomSheetState,
                containerColor = SettingsModel.backgroundColor,
                contentColor = SettingsModel.backgroundColor,
                shape = RoundedCornerShape(15.dp),
                dragHandle = null,
                scrimColor = Color.Black.copy(alpha = .5f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(.3f)
                        .padding(15.dp)
                ) {

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = SettingsModel.textColor)) {
                                append("Your ")
                            }

                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = LightGreen
                                )
                            ) {
                                append(viewModel.reportFile?.name ?: "Sales_Report.xlsx")
                            }

                            withStyle(style = SpanStyle(color = SettingsModel.textColor)) {
                                append(" hase been successfully generated, what would you like to do next?")
                            }
                        },

                        style = TextStyle(
                            textDecoration = TextDecoration.None,
                            fontWeight = FontWeight.Normal,
                            fontSize = 20.sp
                        ),
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    TextButton(
                        onClick = {
                            shareExcelSheet(Intent.ACTION_VIEW)
                        },
                        modifier = Modifier.wrapContentWidth(),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text(
                            "Open: view the report directly",
                            color = LightBlue,
                            style = TextStyle(
                                textDecoration = TextDecoration.None,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp
                            ),
                            textAlign = TextAlign.Start
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    TextButton(
                        onClick = {
                            shareExcelSheet(Intent.ACTION_SEND)
                        },
                        modifier = Modifier.wrapContentWidth(),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text(
                            "Share: send the report to others",
                            color = LightBlue,
                            style = TextStyle(
                                textDecoration = TextDecoration.None,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp
                            ),
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }

        }
    }
}