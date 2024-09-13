package com.grid.pos.ui.reports

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.grid.pos.ActivityScopedViewModel
import com.grid.pos.BuildConfig
import com.grid.pos.R
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.ui.theme.LightBlue
import com.grid.pos.ui.theme.LightGreen
import com.grid.pos.utils.DateHelper
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsView(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    activityViewModel: ActivityScopedViewModel,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val state by viewModel.reportsState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val currentTime = Calendar.getInstance()
    currentTime.timeZone = TimeZone.getDefault()
    val initialDate = currentTime.time
    val dateFormat = "yyyy-MM-dd HH:mm"

    fun getDateFromState(
        time: Long,
        timePickerState: TimePickerState
    ): Date {
        val date = currentTime.apply {
            timeInMillis = time
        }.time
        return DateHelper.editDate(
            date,
            timePickerState.hour,
            timePickerState.minute,
            0
        )
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

    var datePickerPopupState by remember { mutableStateOf(DatePickerPopupState.FROM) }
    val fromDatePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate.time)
    val fromTimePickerState = rememberTimePickerState(
        initialHour = 0,
        initialMinute = 0,
        is24Hour = true,
    )
    val toDatePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate.time)
    val toTimePickerState = rememberTimePickerState(
        initialHour = 23,
        initialMinute = 59,
        is24Hour = true,
    )
    var fromDateState by remember {
        mutableStateOf(
            DateHelper.getDateInFormat(
                getDateFromState(
                    fromDatePickerState.selectedDateMillis!!,
                    fromTimePickerState
                ),
                dateFormat
            )
        )
    }
    var toDateState by remember {
        mutableStateOf(
            DateHelper.getDateInFormat(
                getDateFromState(
                    toDatePickerState.selectedDateMillis!!,
                    toTimePickerState
                ),
                dateFormat
            )
        )
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
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
        activityViewModel.showLoading(state.isLoading)
    }

    fun handleBack() {
        if (state.isLoading) {
            isPopupVisible = true
        } else {
            viewModel.closeConnectionIfNeeded()
            navController?.navigateUp()
        }
    }

    LaunchedEffect(isPopupVisible) {
        activityViewModel.showPopup(isPopupVisible, if (!isPopupVisible) null else PopupModel().apply {
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
            height = 130.dp
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
                        dateFormat
                    ),
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
                    placeHolder = dateFormat,
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
                    val from = getDateFromState(
                        fromDatePickerState.selectedDateMillis!!,
                        fromTimePickerState
                    )
                    val to = getDateFromState(
                        toDatePickerState.selectedDateMillis!!,
                        toTimePickerState
                    )
                    viewModel.fetchInvoices(
                        from,
                        to
                    )
                }
            }
        }

        // date picker component
        if (showDatePicker) {
            DatePickerDialog(colors = DatePickerDefaults.colors(
                containerColor = SettingsModel.backgroundColor
            ),
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (datePickerPopupState == DatePickerPopupState.FROM) {
                                val fromDate = getDateFromState(
                                    fromDatePickerState.selectedDateMillis!!,
                                    fromTimePickerState
                                )
                                if (fromDate.after(Date())) {
                                    viewModel.showError("From date should be today or before, please select again")
                                    showDatePicker = true
                                    return@TextButton
                                }
                                fromDateState = DateHelper.getDateInFormat(
                                    fromDate,
                                    dateFormat
                                )
                            } else {
                                val toDate = getDateFromState(
                                    toDatePickerState.selectedDateMillis!!,
                                    toTimePickerState
                                )
                                toDateState = DateHelper.getDateInFormat(
                                    toDate,
                                    dateFormat
                                )
                            }
                            showDatePicker = false
                            showTimePicker = true
                        },
                        modifier = Modifier.padding(horizontal = 8.dp),
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
                        modifier = Modifier.padding(horizontal = 8.dp),
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
        if (showTimePicker) {
            Dialog(onDismissRequest = { showTimePicker = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(.6f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SettingsModel.backgroundColor,
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        TimePicker(
                            state = if (datePickerPopupState == DatePickerPopupState.FROM) fromTimePickerState else toTimePickerState,
                        )
                        Row {
                            TextButton(
                                onClick = {
                                    showTimePicker = false
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

                            TextButton(
                                onClick = {
                                    if (datePickerPopupState == DatePickerPopupState.FROM) {
                                        val fromDate = getDateFromState(
                                            fromDatePickerState.selectedDateMillis!!,
                                            fromTimePickerState
                                        )
                                        fromDateState = DateHelper.getDateInFormat(
                                            fromDate,
                                            dateFormat
                                        )
                                    } else {
                                        val toDate = getDateFromState(
                                            toDatePickerState.selectedDateMillis!!,
                                            toTimePickerState
                                        )
                                        toDateState = DateHelper.getDateInFormat(
                                            toDate,
                                            dateFormat
                                        )
                                    }
                                    showTimePicker = false
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
                        }

                    }
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

        if (state.clear) {
            state.clear = false
        }
    }
}

enum class DatePickerPopupState(val key: String) {
    FROM("FROM"), TO("TO")
}