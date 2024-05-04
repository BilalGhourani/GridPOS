package com.grid.pos.ui.table

import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.theme.GridPOSTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTablesView(
    navController: NavController? = null,
    modifier: Modifier = Modifier
) {
    GridPOSTheme {
        Scaffold(
            containerColor=SettingsModel.backgroundColor,
            topBar = {
                Surface(shadowElevation = 3.dp, color = SettingsModel.backgroundColor) {
                    TopAppBar(
                        colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = SettingsModel.topBarColor),
                        navigationIcon = {
                            IconButton(onClick = { navController?.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = SettingsModel.buttonColor
                                )
                            }
                        },
                        title = {
                            Text(
                                text = "Manage Tables",
                                color = SettingsModel.textColor,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        })
                }
            }
        ) {
            Text(
                text = "Coming Soon",
                color = SettingsModel.textColor,
                modifier = Modifier.fillMaxSize()
                    .padding(it),
                textAlign = TextAlign.Center
            )
        }
    }
}