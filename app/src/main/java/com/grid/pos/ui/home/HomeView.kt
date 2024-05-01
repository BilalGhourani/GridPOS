package com.grid.pos.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.theme.Blue
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun HomeView(
    navController: NavController? = null,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(true) {
        keyboardController?.hide()
    }
    GridPOSTheme {
        Scaffold(
            topBar = {
                Surface(shadowElevation = 3.dp, color = Color.White) {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = { navController?.navigateUp() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        title = {
                            Text(
                                text = "Home",
                                color = Color.Black,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        })
                }
            }
        ) {
            LazyVerticalGrid(
                modifier = modifier
                    .padding(it)
                    .padding(vertical = 30.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                columns = GridCells.Fixed(2)
            ) {
                Utils.homeSections.forEachIndexed { index, item ->
                    item {
                        Box( // Use a Box to achieve content alignment
                            modifier = Modifier
                                .width(120.dp)
                                .height(80.dp)
                                .padding(horizontal = 3.dp, vertical = 5.dp)
                                .background(color = SettingsModel.buttonColor, shape = RoundedCornerShape(15.dp))
                                .clickable { navController?.navigate(item.composable) },
                            contentAlignment = Alignment.Center // Center content within the Box
                        ) {
                            Text(
                                text = item.title,
                                color = SettingsModel.buttonTextColor,
                                modifier = Modifier
                                    .padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}