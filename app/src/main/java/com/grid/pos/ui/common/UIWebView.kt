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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.grid.pos.ActivityScopedViewModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.UserType
import com.grid.pos.ui.theme.GridPOSTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UIWebView(
        modifier: Modifier = Modifier,
        navController: NavController? = null,
        activityViewModel: ActivityScopedViewModel
) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            webViewClient = WebViewClient()
            loadDataWithBaseURL(
                null,
                activityViewModel.getInvoiceReceiptHtmlContent(context),
                "text/html",
                "UTF-8",
                null
            )
        }
    }

    fun handleBack() {
        activityViewModel.clearPosValues()
        if (SettingsModel.getUserType() == UserType.TABLE) {
            navController?.popBackStack(
                "TablesView",
                false
            )
        } else {
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

                UIButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    text = "Print"
                ) {
                    activityViewModel.print(context)/*val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                    val jobName = "webpage_" + System.currentTimeMillis()
                    val printAdapter = webView.createPrintDocumentAdapter(jobName)

                    // Define Print Attributes (optional)
                    val printAttributes = PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4*//*getMediaSize()*//*)
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS).build()
                    printManager.print(
                        jobName,
                        printAdapter,
                        printAttributes
                    )*/
                }
            }
        }
    }
}