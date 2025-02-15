package com.grid.pos.ui.table

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.grid.pos.R
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.UserType
import com.grid.pos.ui.common.UIImageButton
import com.grid.pos.ui.common.UITextField
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun TablesView(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    viewModel: TablesViewModel = hiltViewModel()
) {
    val state by viewModel.tablesState.collectAsStateWithLifecycle()

    val keyboardController = LocalSoftwareKeyboardController.current
    val tableNameFocusRequester = remember { FocusRequester() }
    val clientsCountFocusRequester = remember { FocusRequester() }

    LaunchedEffect(key1 = Unit) {
        viewModel.fetchAllTables()
    }


    fun handleBack() {
        if (viewModel.isLoading()) {
            return
        }
        if (state.step > 1) {
            viewModel.updateState(
                state.copy(
                    step = 1
                )
            )
        } else {
            if (SettingsModel.getUserType() == UserType.TABLE) {
                viewModel.showPopup(
                    PopupModel().apply {
                        onConfirmation = {
                            viewModel.logout()
                            navController?.clearBackStack("LoginView")
                            navController?.navigate("LoginView")
                        }
                        dialogText = "Are you sure you want to logout?"
                        positiveBtnText = "Logout"
                        negativeBtnText = "Cancel"
                    })
            } else {
                navController?.navigateUp()
            }
        }
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
                modifier = modifier.padding(it),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                UITextField(modifier = Modifier.padding(10.dp),
                    defaultValue = state.tableName,
                    label = "Table Number",
                    enabled = state.step <= 1,
                    focusRequester = tableNameFocusRequester,
                    keyboardType = KeyboardType.Text,
                    placeHolder = "Enter Table Number",
                    onAction = {
                        viewModel.fetchInvoiceByTable(state.tableName)
                    },
                    trailingIcon = {
                        IconButton(enabled = state.step <= 1,
                            onClick = { }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = if (state.step <= 1) SettingsModel.buttonColor else Color.LightGray
                            )
                        }
                    }) { tabNo ->
                    viewModel.updateState(
                        state.copy(
                            tableName = tabNo
                        )
                    )
                    viewModel.filterTables(tabNo)
                }
                if (state.step > 1) {
                    clientsCountFocusRequester.requestFocus()
                    UITextField(modifier = Modifier.padding(10.dp),
                        defaultValue = state.clientCount,
                        onFocusChanged = { focusState ->
                            if (focusState.hasFocus) {
                                keyboardController?.show()
                            }
                        },
                        label = "Client Number",
                        focusRequester = clientsCountFocusRequester,
                        keyboardType = KeyboardType.Number,
                        placeHolder = "Enter Client Number",
                        imeAction = ImeAction.Done,
                        onAction = {
                            keyboardController?.hide()
                        }) { clients ->
                        viewModel.updateState(
                            state.copy(
                                clientCount = Utils.getIntValue(
                                    clients,
                                    state.clientCount
                                )
                            )
                        )
                    }
                } else {
                    tableNameFocusRequester.requestFocus()
                }
                UIImageButton(
                    modifier = Modifier
                        .wrapContentWidth()
                        .height(100.dp)
                        .padding(10.dp),
                    icon = if (state.step <= 1) R.drawable.search else R.drawable.next,
                    text = if (state.step <= 1) "Search for Table" else "Submit to POS",
                    iconSize = 60.dp,
                    isVertical = false
                ) {
                    if (state.step <= 1) {
                        viewModel.fetchInvoiceByTable(state.tableName)
                    } else {
                        if (state.clientCount.toIntOrNull() == null) {
                            viewModel.showWarning("Please enter client counts")
                            return@UIImageButton
                        }
                        viewModel.lockTableAndMoveToPos()
                    }
                }

                LazyColumn(
                    modifier = modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    stickyHeader {
                        Box(
                            modifier = modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(
                                    horizontal = 10.dp,
                                    vertical = 5.dp
                                ),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "Opened Tables",
                                maxLines = 1,
                                style = TextStyle(
                                    textDecoration = TextDecoration.None,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                textAlign = TextAlign.Start,
                                color = SettingsModel.textColor,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (state.tables.isNotEmpty()) {
                        state.tables.forEach { tableModel ->
                            item {
                                Box(
                                    modifier = modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .padding(
                                            horizontal = 10.dp,
                                            vertical = 5.dp
                                        )
                                        .background(
                                            color = Color.Transparent,
                                            RoundedCornerShape(15.dp)
                                        )
                                        .border(
                                            border = BorderStroke(
                                                1.dp,
                                                Color.Black
                                            ),
                                            shape = RoundedCornerShape(15.dp)
                                        )
                                        .clickable {
                                            viewModel.updateState(
                                                state.copy(
                                                    tableName = tableModel.table_name
                                                )
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tableModel.getName(),
                                        maxLines = 1,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center,
                                        color = SettingsModel.textColor,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    } else if (state.isLoadingTables) {
                        item {
                            Box(
                                modifier = modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "loading...",
                                    maxLines = 1,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    color = SettingsModel.textColor,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                    } else {
                        item {
                            Box(
                                modifier = modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "no tables found!",
                                    maxLines = 1,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    color = SettingsModel.textColor,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}