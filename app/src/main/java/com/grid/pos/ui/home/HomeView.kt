package com.grid.pos.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.grid.pos.SharedViewModel
import com.grid.pos.ActivityState
import com.grid.pos.R
import com.grid.pos.model.Event
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun HomeView(
        modifier: Modifier = Modifier,
        navController: NavController? = null,
        sharedViewModel: SharedViewModel,
) {
    val activityState: ActivityState by sharedViewModel.activityState.collectAsState(
        ActivityState()
    )
    val context = LocalContext.current
    var isLogoutPopupShown by remember { mutableStateOf(false) }
    var columnCount by remember { mutableIntStateOf(Utils.getColumnCount(context)) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val configuration = LocalConfiguration.current
    LaunchedEffect(configuration) {
        snapshotFlow { configuration.orientation }.collect {
            columnCount = Utils.getColumnCount(context)
        }
    }

    LaunchedEffect(
        true,
        activityState.isLoggedIn,
        activityState.warning
    ) {
        keyboardController?.hide()
        activityState.warning?.value?.let {
            if (it.isNotEmpty()) {
                isLogoutPopupShown = true
            }
        }
    }

    fun logout() {
        isLogoutPopupShown = false
        sharedViewModel.logout()
        navController?.clearBackStack("LoginView")
        navController?.navigate("LoginView")
    }
    LaunchedEffect(isLogoutPopupShown) {
        sharedViewModel.showPopup(isLogoutPopupShown,
            if (!isLogoutPopupShown) null else PopupModel().apply {
                onDismissRequest = {
                    isLogoutPopupShown = false
                    activityState.warning = null
                    if (activityState.forceLogout) logout()
                }
                onConfirmation = {
                    isLogoutPopupShown = false
                    activityState.warning = null
                    logout()
                }
                dialogText = activityState.warning?.value ?: SettingsModel.companyAccessWarning
            })
    }
    fun askToLogout() {
        if (!isLogoutPopupShown) {
            activityState.warning = Event("Are you sure you want to logout?")
            activityState.forceLogout = false
            isLogoutPopupShown = true
        }
    }
    BackHandler {
        askToLogout()
    }
    GridPOSTheme {
        Scaffold(containerColor = SettingsModel.backgroundColor,
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
            LazyColumn(
                modifier = modifier
                    .padding(it)
                    .fillMaxSize()
            ) {
                Utils.getHomeList().forEach { category ->
                    item {
                        HomeCategoryCell(homeCategoryModel = category,
                            columnCount = Utils.getColumnCount(context),
                            onClick = { destination ->
                                if (destination == "logout") {
                                    askToLogout()
                                } else {
                                    navController?.navigate(destination)
                                }
                            })
                    }
                }
            }
        }
    }
}