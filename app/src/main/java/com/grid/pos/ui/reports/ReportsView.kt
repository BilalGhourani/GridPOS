package com.grid.pos.ui.reports

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.grid.pos.ActivityScopedViewModel
import com.grid.pos.MainActivity
import com.grid.pos.R
import com.grid.pos.model.Event
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.LoadingIndicator
import com.grid.pos.ui.common.UIAlertDialog
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.pos.PopupState
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsView(
        navController: NavController? = null,
        activityViewModel: ActivityScopedViewModel,
        modifier: Modifier = Modifier,
        viewModel: ReportsViewModel = hiltViewModel()
) {
    val reportsState: ReportsState by viewModel.reportsState.collectAsState(
        ReportsState()
    )

    fun getDateFromState(time: Long): Date {
        return Calendar.getInstance().apply {
            timeInMillis = time
        }.time
    }

    val initialDate = Date()

    val toDateFocusRequester = remember { FocusRequester() }

    var datePickerPopupState by remember { mutableStateOf(DatePickerPopupState.FROM) }
    val fromDatePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate.time)
    val toDatePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate.time)
    var fromDateState by remember {
        mutableStateOf(
            Utils.getDateinFormat(
                getDateFromState(fromDatePickerState.selectedDateMillis!!),
                "yyyy-MM-dd"
            )
        )
    }
    var toDateState by remember {
        mutableStateOf(
            Utils.getDateinFormat(
                getDateFromState(toDatePickerState.selectedDateMillis!!),
                "yyyy-MM-dd"
            )
        )
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var isPopupVisible by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()


    LaunchedEffect(reportsState.warning) {
        reportsState.warning?.value?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }

    fun handleBack() {
        if (reportsState.isLoading) {
            isPopupVisible = true
        } else {
            navController?.popBackStack()
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
                    placeHolder = Utils.getDateinFormat(
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
                        Utils.editDate(
                            from,
                            0,
                            0,
                            0
                        ),
                        Utils.editDate(to)
                    )
                }
            }
        }

        // date picker component
        if (showDatePicker) {
            DatePickerDialog(onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (datePickerPopupState == DatePickerPopupState.FROM) {
                                val fromDate = getDateFromState(fromDatePickerState.selectedDateMillis!!)
                                if (fromDate.after(Date())) {
                                    viewModel.showError("From date should be today or before, please select again")
                                    showDatePicker = true
                                    return@TextButton
                                }
                                fromDateState = Utils.getDateinFormat(
                                    fromDate,
                                    "yyyy-MM-dd"
                                )
                            } else {
                                val toDate = getDateFromState(toDatePickerState.selectedDateMillis!!)
                                toDateState = Utils.getDateinFormat(
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
                DatePicker(state = if (datePickerPopupState == DatePickerPopupState.FROM) fromDatePickerState else toDatePickerState)
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
                negativeBtnText = "Dismiss",
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