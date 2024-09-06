package com.grid.pos.ui.settings.setupReports

import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.grid.pos.ActivityScopedViewModel
import com.grid.pos.App
import com.grid.pos.model.FileModel
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.FileUtils
import kotlinx.coroutines.launch
import java.io.File

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun ReportsListView(
        modifier: Modifier = Modifier,
        navController: NavController? = null,
        activityViewModel: ActivityScopedViewModel,
        viewModel: ReportsListViewModel = hiltViewModel()
) {
    val state: ReportsListState by viewModel.state.collectAsState(
        ReportsListState()
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var deletePopupState by remember { mutableStateOf(false) }
    val fileModelState = remember { mutableStateOf(FileModel()) }

    val context = LocalContext.current
    var isOptionPopupExpanded by remember { mutableStateOf(false) }
    var previewBottomSheetState by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    LaunchedEffect(deletePopupState) {
        activityViewModel.showPopup(deletePopupState,
            PopupModel().apply {
                onDismissRequest = {
                    deletePopupState = false
                }
                onConfirmation = {
                    deletePopupState = false
                    viewModel.deleteFile(fileModelState.value)
                }
                dialogText = "Are you sure you want to delete this file?"
                positiveBtnText = "Delete"
                negativeBtnText = "Close"
                height = 130.dp
            })
    }


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

    }

    fun handleBack() {
        if (isOptionPopupExpanded) {
            isOptionPopupExpanded = false
            return
        }
        if (deletePopupState) {
            deletePopupState = false
            return
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
                                text = "Files List",
                                color = SettingsModel.textColor,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        },
                        actions = {
                            IconButton(onClick = {
                                activityViewModel.isPaySlip = selectedTabIndex == 0
                                navController?.navigate("SetupReportView")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.AddCircleOutline,
                                    contentDescription = "Add new report",
                                    tint = SettingsModel.buttonColor
                                )
                            }
                        })
                }
            }) { padding ->
            Column(
                modifier = modifier.padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // TabRow with tabs
                TabRow(selectedTabIndex = selectedTabIndex,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    containerColor = SettingsModel.backgroundColor,
                    contentColor = SettingsModel.textColor,
                    indicator = { tabPositions ->
                        if (selectedTabIndex < tabPositions.size) {
                            Spacer(
                                Modifier
                                    .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                    .requiredHeight(3.dp)
                                    .requiredWidth(100.dp)
                                    .background(
                                        color = SettingsModel.buttonColor,
                                        shape = RoundedCornerShape(3.0.dp)
                                    )
                            )
                        }
                    },
                    divider = { }) {
                    viewModel.tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            selectedContentColor = SettingsModel.buttonColor,
                            unselectedContentColor = SettingsModel.textColor
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val dataArray = state.getFileModels(selectedTabIndex == 0)
                if (dataArray.isEmpty()) {
                    Spacer(modifier = Modifier.fillMaxHeight(.4f))
                    Text(
                        modifier = Modifier
                            .wrapContentSize(Alignment.Center)
                            .padding(horizontal = 10.dp),
                        text = "no reports found.",
                        color = SettingsModel.textColor,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )

                } else {
                    LazyColumn(
                        modifier = modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        dataArray.forEach { fileModel ->
                            item {
                                ReportListCell(modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .padding(horizontal = 10.dp),
                                    fileModel = fileModel,
                                    onSelect = {
                                        viewModel.selectFile(fileModel)
                                    },
                                    onOption = {
                                        fileModelState.value = fileModel
                                        isOptionPopupExpanded = !isOptionPopupExpanded
                                    })

                            }
                        }
                    }
                }
            }
        }

        if (isOptionPopupExpanded) {
            ModalBottomSheet(
                onDismissRequest = { isOptionPopupExpanded = false },
                sheetState = bottomSheetState,
                containerColor = Color.White,
                contentColor = Color.White,
                shape = RoundedCornerShape(
                    topStart = 15.dp,
                    topEnd = 15.dp
                ),
                dragHandle = null,
                scrimColor = Color.Black.copy(alpha = .5f),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(horizontal = 10.dp)
                        .background(color = Color.White)
                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 5.dp)
                            .clickable {
                                isOptionPopupExpanded = false
                                previewBottomSheetState = true
                            },
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Preview,
                            contentDescription = "Preview",
                            tint = SettingsModel.textColor
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            "Preview",
                            color = SettingsModel.textColor,
                            fontSize = 16.sp
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier
                            .height(1.dp)
                            .background(color = Color.LightGray)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 5.dp)
                            .clickable {
                                isOptionPopupExpanded = false
                            },
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SaveAlt,
                            contentDescription = "Download",
                            tint = SettingsModel.textColor
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            "Download",
                            color = SettingsModel.textColor,
                            fontSize = 16.sp
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier
                            .height(1.dp)
                            .background(color = Color.LightGray)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 5.dp)
                            .clickable {
                                isOptionPopupExpanded = false
                                if (fileModelState.value.selected) {
                                    viewModel.showError("cannot delete selected report!")
                                } else {
                                    deletePopupState = true
                                }
                            },
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            "Delete",
                            color = Color.Red,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
        if (previewBottomSheetState) {
            ModalBottomSheet(
                onDismissRequest = { previewBottomSheetState = false },
                sheetState = bottomSheetState,
                containerColor = Color.White,
                contentColor = Color.White,
                shape = RoundedCornerShape(
                    topStart = 15.dp,
                    topEnd = 15.dp
                ),
                dragHandle = null,
                scrimColor = Color.Black.copy(alpha = .5f)
            ) {
                AndroidView(modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(.8f)
                    .padding(top = 10.dp),
                    factory = {
                        WebView(context).apply {
                            val rootFile = File(
                                App.getInstance().filesDir,
                                "Reports"
                            )
                            val file = fileModelState.value.getFile(rootFile)
                            webViewClient = WebViewClient()
                            loadDataWithBaseURL(
                                null,
                                FileUtils.getFileContent(
                                    context,
                                    Uri.fromFile(file)
                                ),
                                "text/html",
                                "UTF-8",
                                null
                            )
                        }
                    }) {}
            }
        }
    }
}