package com.grid.pos.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.grid.pos.ActivityScopedViewModel
import com.grid.pos.ActivityState
import com.grid.pos.MainActivity
import com.grid.pos.R
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.UIAlertDialog
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.company.ManageCompaniesState
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun HomeView(
        modifier: Modifier = Modifier,
        navController: NavController? = null,
        mainActivity: MainActivity,
        activityViewModel: ActivityScopedViewModel?,
) {
    val activityState: ActivityState by activityViewModel!!.activityState.collectAsState(
        ActivityState()
    )
    var isLogoutPopupShown by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(
        true,
        activityState.isLoggedIn,
        activityState.warning
    ) {
        keyboardController?.hide()
        if (!activityState.isLoggedIn && !activityState.warning.isNullOrEmpty()) {
            isLogoutPopupShown=true
            activityState.warning=null
        }
    }
    BackHandler {
        mainActivity.finish()
    }
    GridPOSTheme {
        Scaffold(
            containerColor = SettingsModel.backgroundColor,
            topBar = {
                Surface(
                    shadowElevation = 3.dp,
                    color = SettingsModel.backgroundColor
                ) {
                    TopAppBar(colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = SettingsModel.topBarColor),
                        title = {
                            Text(
                                text = "Home",
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
                        UIButton(
                            modifier = Modifier
                                .width(120.dp)
                                .height(80.dp)
                                .padding(
                                    horizontal = 3.dp,
                                    vertical = 5.dp
                                ),
                            text = item.title,
                            shape = RoundedCornerShape(15.dp)
                        ) {
                            navController?.navigate(item.composable)
                        }
                    }
                }
            }
        }
        fun logout(){
            isLogoutPopupShown = false
            navController?.clearBackStack("LoginView")
            navController?.navigate("LoginView")
        }
        AnimatedVisibility(
            visible = isLogoutPopupShown,
            enter = fadeIn(
                initialAlpha = 0.4f
            ),
            exit = fadeOut(
                animationSpec = tween(durationMillis = 250)
            )
        ) {
            UIAlertDialog(
                onDismissRequest = { logout()},
                onConfirmation = {logout()},
                dialogTitle = "Alert.",
                dialogText = activityState.warning?:SettingsModel.companyAccessWarning,
                icon = Icons.Default.Info
            )
        }
    }
}