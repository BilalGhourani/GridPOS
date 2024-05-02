package com.grid.pos.ui.common

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.grid.pos.ActivityScopedViewModel
import com.grid.pos.ui.theme.Blue
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UIWebView(
    navController: NavController? = null,
    activityViewModel: ActivityScopedViewModel = ActivityScopedViewModel(),
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            webViewClient = WebViewClient()
            val htmlContent = activityViewModel.getHtmlContent(context)
            if (htmlContent.isNotEmpty()) {
                loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            } else {
                loadUrl(activityViewModel.getHtmlContent(context, Utils.getDefaultReceipt()))
            }
        }
    }

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
                                text = "Print",
                                color = Color.Black,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        })
                }
            }
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                AndroidView(
                    modifier = Modifier.weight(1f),
                    factory = { webView }
                ) {
                }

                UIButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    text = "Print"
                ) {
                    Utils.printWebPage(webView, context)
                }
            }
        }
    }
}