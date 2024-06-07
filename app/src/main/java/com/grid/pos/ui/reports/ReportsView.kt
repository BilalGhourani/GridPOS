package com.grid.pos.ui.reports

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.grid.pos.ActivityScopedViewModel
import com.grid.pos.BuildConfig
import com.grid.pos.R
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.LoadingIndicator
import com.grid.pos.ui.common.UIAlertDialog
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.ui.theme.LightBlue
import com.grid.pos.ui.theme.LightGreen
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Utils
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsView(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    activityViewModel: ActivityScopedViewModel,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val reportsState: ReportsState by viewModel.reportsState.collectAsState(
        ReportsState()
    )

    val context = LocalContext.current

    fun getDateFromState(time: Long): Date {
        return Calendar.getInstance().apply {
            timeInMillis = time
        }.time
    }

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

            activityViewModel.startChooserActivity(
                Intent.createChooser(
                    shareIntent,
                    "send"
                )
            )
        }
    }

    val initialDate = Date()

    val toDateFocusRequester = remember { FocusRequester() }

    var datePickerPopupState by remember { mutableStateOf(DatePickerPopupState.FROM) }
    val fromDatePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate.time)
    val toDatePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate.time)
    var fromDateState by remember {
        mutableStateOf(
            DateHelper.getDateInFormat(
                getDateFromState(fromDatePickerState.selectedDateMillis!!),
                "yyyy-MM-dd"
            )
        )
    }
    var toDateState by remember {
        mutableStateOf(
            DateHelper.getDateInFormat(
                getDateFromState(toDatePickerState.selectedDateMillis!!),
                "yyyy-MM-dd"
            )
        )
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var isPopupVisible by remember { mutableStateOf(false) }
    var isBottomSheetVisible by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()


    LaunchedEffect(
        reportsState.warning,
        reportsState.isDone
    ) {
        reportsState.warning?.value?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short,
                )
            }
        }

        if (reportsState.isDone) {
            isBottomSheetVisible = true
            reportsState.isDone = false
        }
    }

    fun handleBack() {
        if (reportsState.isLoading) {
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
                                text = "Tables",
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
                modifier = modifier.padding(it)
            ) {

                UITextField(modifier = Modifier.padding(10.dp),
                    defaultValue = fromDateState,
                    label = "From",
                    maxLines = 1,
                    readOnly = true,
                    keyboardType = KeyboardType.Text,
                    placeHolder = DateHelper.getDateInFormat(
                        initialDate,
                        "yyyy-MM-dd"
                    ),
                    onAction = {
                        toDateFocusRequester.requestFocus()
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            datePickerPopupState = DatePickerPopupState.FROM
                            showDatePicker = true
                        }) {
                            Icon(
                                imageVector = Icons.Filled.DateRange,
                                contentDescription = "From Date",
                                tint = SettingsModel.buttonColor
                            )
                        }
                    }) { from ->
                    fromDateState = from
                }

                UITextField(modifier = Modifier.padding(10.dp),
                    defaultValue = toDateState,
                    label = "To",
                    maxLines = 1,
                    readOnly = true,
                    keyboardType = KeyboardType.Text,
                    placeHolder = "yyyy-MM-dd",
                    onAction = {
                        toDateFocusRequester.requestFocus()
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            datePickerPopupState = DatePickerPopupState.TO
                            showDatePicker = true
                        }) {
                            Icon(
                                imageVector = Icons.Filled.DateRange,
                                contentDescription = "From Date",
                                tint = SettingsModel.buttonColor
                            )
                        }
                    }) { to ->
                    toDateState = to
                }

                UIButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(10.dp),
                    text = "Generate"
                ) {
                    val from = getDateFromState(fromDatePickerState.selectedDateMillis!!)
                    val to = getDateFromState(toDatePickerState.selectedDateMillis!!)
                    viewModel.fetchInvoices(
                        DateHelper.editDate(
                            from,
                            0,
                            0,
                            0
                        ),
                        DateHelper.editDate(to)
                    )
                }
            }
        }

        // date picker component
        if (showDatePicker) {
            DatePickerDialog(
                colors = DatePickerDefaults.colors(
                    containerColor = SettingsModel.backgroundColor
                ),
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (datePickerPopupState == DatePickerPopupState.FROM) {
                                val fromDate =
                                    getDateFromState(fromDatePickerState.selectedDateMillis!!)
                                if (fromDate.after(Date())) {
                                    viewModel.showError("From date should be today or before, please select again")
                                    showDatePicker = true
                                    return@TextButton
                                }
                                fromDateState = DateHelper.getDateInFormat(
                                    fromDate,
                                    "yyyy-MM-dd"
                                )
                            } else {
                                val toDate =
                                    getDateFromState(toDatePickerState.selectedDateMillis!!)
                                toDateState = DateHelper.getDateInFormat(
                                    toDate,
                                    "yyyy-MM-dd"
                                )
                            }
                            showDatePicker = false
                        },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text(
                            "Submit",
                            color = SettingsModel.textColor,
                            style = TextStyle(
                                textDecoration = TextDecoration.None,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp
                            )
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDatePicker = false
                        },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text(
                            "Cancel",
                            color = SettingsModel.textColor,
                            style = TextStyle(
                                textDecoration = TextDecoration.None,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp
                            )
                        )
                    }
                }) {
                DatePicker(
                    state = if (datePickerPopupState == DatePickerPopupState.FROM) fromDatePickerState else toDatePickerState,
                    colors = DatePickerDefaults.colors(
                        containerColor = SettingsModel.backgroundColor,
                        dayContentColor = SettingsModel.textColor,
                        currentYearContentColor = SettingsModel.textColor,
                        navigationContentColor = SettingsModel.textColor,
                        yearContentColor = SettingsModel.textColor,
                        weekdayContentColor = SettingsModel.textColor,
                        titleContentColor = SettingsModel.textColor,
                        headlineContentColor = SettingsModel.textColor,
                        subheadContentColor = SettingsModel.textColor,
                       // dayInSelectionRangeContentColor = SettingsModel.textColor,
                        selectedDayContainerColor = LightBlue,
                        selectedDayContentColor = Color.White,
                        selectedYearContainerColor = LightBlue,
                        selectedYearContentColor = Color.White,
                        todayContentColor = SettingsModel.textColor,
                        todayDateBorderColor = LightBlue
                    )
                )
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
                scrimColor = Color.Black.copy(alpha = .5f),
                windowInsets = WindowInsets(0, 0, 0, 0)
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
                                append(viewModel.reportFile?.name?:"Sales_Report.xlsx")
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

        AnimatedVisibility(
            visible = isPopupVisible,
            enter = fadeIn(
                initialAlpha = 0.4f
            ),
            exit = fadeOut(
                animationSpec = tween(durationMillis = 250)
            )
        ) {
            UIAlertDialog(
                onDismissRequest = {
                    isPopupVisible = false
                },
                onConfirmation = {
                    reportsState.isLoading = false
                    isPopupVisible = false
                    handleBack()
                },
                dialogTitle = "Alert.",
                dialogText = "Are you sure you want to cancel the reports?",
                positiveBtnText = "Cancel",
                negativeBtnText = "Close",
                icon = Icons.Default.Info,
                height = 230.dp
            )
        }
        LoadingIndicator(
            show = reportsState.isLoading
        )

        if (reportsState.clear) {
            reportsState.clear = false
        }
    }
}

enum class DatePickerPopupState(val key: String) {
    FROM("FROM"), TO("TO")
}