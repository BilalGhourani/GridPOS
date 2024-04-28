package com.grid.pos.ui.pos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePosView(
    navController: NavController? = null,
    modifier: Modifier = Modifier
) {
    var isEditBottomSheetVisible by remember { mutableStateOf(false) }
    var isPayBottomSheetVisible by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showCashBottomSheet by remember { mutableStateOf(false) }
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
                        })
                }
            }
        ) {

            /*if (Utils.isTablet(LocalConfiguration.current)) {
                // Compose your UI for tablets
            } else {
                // Compose your UI for phones or other non-tablet devices
            }*/
            Surface(
                modifier = modifier
                    .wrapContentWidth()
                    .fillMaxHeight()
                    .padding(it)
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(horizontal = 10.dp, vertical = 16.dp),
                ) {
                    InvoiceHeaderDetails(
                        navController = navController,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(.1f),
                        onEdit = { isEditBottomSheetVisible = true },
                        onPay = { isPayBottomSheetVisible = true }
                    )

                    // Border stroke configuration
                    val borderStroke = BorderStroke(1.dp, Color.Black)

                    InvoiceBodyDetails(
                        navController = navController,
                        modifier = Modifier
                            .wrapContentWidth()
                            .weight(.7f)
                            .border(borderStroke)
                    )

                    InvoiceFooterView(
                        navController = navController,
                        modifier = Modifier
                            .wrapContentWidth()
                            .weight(.2f)
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

    if (isPayBottomSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { isPayBottomSheetVisible = false },
            sheetState = bottomSheetState,
            containerColor = Color.Transparent,
            contentColor = White,
            shape = RectangleShape,
            dragHandle = null,
            scrimColor = Color.Black.copy(alpha = .5f),
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            EditInvoiceHeaderView()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ManagePosViewPreview() {
    GridPOSTheme {
        ManagePosView()
    }
}