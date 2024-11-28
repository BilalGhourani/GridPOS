package com.grid.pos.ui.common

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.grid.pos.SharedViewModel
import com.grid.pos.model.ReportResult
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.PrinterUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UIWebView(
        modifier: Modifier = Modifier,
        navController: NavController? = null,
        sharedViewModel: SharedViewModel
) {
    val context = LocalContext.current
    val reportResultState = remember {
        mutableStateOf(
            if (sharedViewModel.reportsToPrint.isNotEmpty()) {
                sharedViewModel.reportsToPrint[0]
            } else {
                ReportResult(
                    false,
                    ""
                )
            }
        )
    }

    val webView = remember {
        WebView(context).apply {
            webViewClient = WebViewClient()
            loadDataWithBaseURL(
                null,
                reportResultState.value.htmlContent,
                "text/html",
                "UTF-8",
                null
            )
        }
    }

    fun handleBack() {
        if (sharedViewModel.isFromTable) {
            sharedViewModel.clearPosValues()
            navController?.popBackStack(
                "TablesView",
                false
            )
        } else {
            sharedViewModel.clearPosValues()
            navController?.navigateUp()
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
                                text = "Print",
                                color = SettingsModel.textColor,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        })
                }
            }) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                AndroidView(modifier = Modifier.weight(1f),
                    factory = { webView }) {}

                if (reportResultState.value.found) {
                    UIButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        text = "Print"
                    ) {
                        sharedViewModel.showLoading(true)
                        CoroutineScope(Dispatchers.Default).launch {
                            sharedViewModel.reportsToPrint.forEach {
                                PrinterUtils.printReport(
                                    context,
                                    it
                                )
                            }
                            withContext(Dispatchers.Main) {
                                sharedViewModel.showLoading(false)
                                handleBack()
                            }
                        }
                    }
                }
            }
        }
    }
}