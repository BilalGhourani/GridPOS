package com.grid.pos.ui.pos

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.grid.pos.ui.pos.components.AddInvoiceItemView
import com.grid.pos.ui.pos.components.EditInvoiceHeaderView
import com.grid.pos.ui.pos.components.InvoiceBodyDetails
import com.grid.pos.ui.pos.components.InvoiceCashView
import com.grid.pos.ui.pos.components.InvoiceFooterView
import com.grid.pos.ui.pos.components.InvoiceHeaderDetails
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.ui.theme.White
import com.grid.pos.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosView(
    navController: NavController? = null,
    modifier: Modifier = Modifier,
    viewModel: POSViewModel = hiltViewModel()
) {
    val posState: POSState by viewModel.posState.collectAsState(POSState())
    var isEditBottomSheetVisible by remember { mutableStateOf(false) }
    var isAddItemBottomSheetVisible by remember { mutableStateOf(false) }
    var isPayBottomSheetVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var orientation by remember { mutableStateOf(Configuration.ORIENTATION_PORTRAIT) }

    val configuration = LocalConfiguration.current
    val isTablet = Utils.isTablet(LocalConfiguration.current)
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(configuration) {
        snapshotFlow { configuration.orientation }
            .collect { orientation = it }
    }

    LaunchedEffect(posState.warning) {
        if (!posState.warning.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.Main).launch {
                snackbarHostState.showSnackbar(
                    message = posState.warning!!,
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }
    GridPOSTheme {
        Scaffold(
            topBar = {
                Surface(shadowElevation = 3.dp, color = Color.White) {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = { navController?.navigateUp() }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        title = {
                            Text(
                                text = "POS",
                                color = Color.Black,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        },
                        actions = {
                            IconButton(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(horizontal = 10.dp),
                                onClick = { navController?.navigateUp() }
                            ) {
                                Text(text = "Back")
                            }
                        })
                }
            }
        ) {
            Surface(
                modifier = modifier
                    .wrapContentWidth()
                    .fillMaxHeight()
                    .padding(it)
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentWidth()
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                ) {
                    val height = configuration.screenHeightDp
                    InvoiceHeaderDetails(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp),
                        onEdit = { isEditBottomSheetVisible = true },
                        onAddItem = { isAddItemBottomSheetVisible = true },
                        onPay = { isPayBottomSheetVisible = true }
                    )

                    // Border stroke configuration
                    val borderStroke = BorderStroke(1.dp, Color.Black)

                    InvoiceBodyDetails(
                        invoices = Utils.getInvoiceModelFromList(posState.invoices),
                        modifier = Modifier
                            .wrapContentWidth()
                            .wrapContentHeight()
                            .defaultMinSize(minHeight = 130.dp)
                            .border(borderStroke)
                            .height(70.dp),
                        isLandscape = isTablet || isLandscape
                    )

                    InvoiceFooterView(
                        items = posState.items,
                        thirdParties = posState.thirdParties,
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(250.dp)
                            .height(70.dp),
                        onItemSelected = {},
                        onThirdPartySelected = {},
                    )
                }
            }
        }
    }
    if (isEditBottomSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { isEditBottomSheetVisible = false },
            sheetState = bottomSheetState,
            containerColor = Color.White,
            contentColor = White,
            shape = RectangleShape,
            dragHandle = null,
            scrimColor = Color.Black.copy(alpha = .5f),
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            EditInvoiceHeaderView(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
            )
        }
    }

    if (isAddItemBottomSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { isAddItemBottomSheetVisible = false },
            sheetState = bottomSheetState,
            containerColor = Color.White,
            contentColor = White,
            shape = RectangleShape,
            dragHandle = null,
            scrimColor = Color.Black.copy(alpha = .5f),
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            AddInvoiceItemView(
                categories = posState.families,
                items = posState.items,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
            ) {
                posState.invoices.add(Utils.getInvoiceFromItem(it))
            }
        }
    }

    if (isPayBottomSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { isPayBottomSheetVisible = false },
            sheetState = bottomSheetState,
            containerColor = Color.White,
            contentColor = White,
            shape = RectangleShape,
            dragHandle = null,
            scrimColor = Color.Black.copy(alpha = .5f),
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            InvoiceCashView(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f),
                onSave = { navController?.navigate("UIWebView") },
                onFinish = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PosViewPreview() {
    GridPOSTheme {
        PosView()
    }
}