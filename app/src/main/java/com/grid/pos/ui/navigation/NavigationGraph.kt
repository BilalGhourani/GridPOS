package com.grid.pos.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.grid.pos.ActivityScopedViewModel
import com.grid.pos.MainActivity
import com.grid.pos.ui.common.UIWebView
import com.grid.pos.ui.company.ManageCompaniesView
import com.grid.pos.ui.currency.ManageCurrenciesView
import com.grid.pos.ui.family.ManageFamiliesView
import com.grid.pos.ui.home.HomeView
import com.grid.pos.ui.item.ManageItemsView
import com.grid.pos.ui.login.LoginView
import com.grid.pos.ui.pos.POSView
import com.grid.pos.ui.posPrinter.POSPrinterView
import com.grid.pos.ui.reports.ReportsView
import com.grid.pos.ui.settings.SettingsView
import com.grid.pos.ui.table.TablesView
import com.grid.pos.ui.theme.White
import com.grid.pos.ui.thirdParty.ManageThirdPartiesView
import com.grid.pos.ui.user.ManageUsersView

@Composable
fun AuthNavGraph(
        navController: NavHostController,
        mainActivity: MainActivity,
        activityViewModel: ActivityScopedViewModel,
        startDestination: String,
        modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.background(color = White)
    ) {

        composable(route = "HomeView") {
            HomeView(
                navController = navController,
                mainActivity = mainActivity,
                activityViewModel = activityViewModel
            )
        }
        composable(route = "LoginView") {
            LoginView(
                navController = navController,
                activityScopedViewModel = activityViewModel,
                mainActivity = mainActivity
            )
        }
        composable(route = "SettingsView") {
            SettingsView(
                navController = navController,
                activityScopedViewModel = activityViewModel
            )
        }
        composable(route = "ManageCurrenciesView") {
            ManageCurrenciesView(
                navController = navController
            )
        }
        composable(route = "ManageCompaniesView") {
            ManageCompaniesView(
                navController = navController,
                activityScopedViewModel = activityViewModel
            )
        }
        composable(route = "ManageUsersView") {
            ManageUsersView(
                navController = navController,
                activityScopedViewModel = activityViewModel
            )
        }
        composable(route = "ManageFamiliesView") {
            ManageFamiliesView(
                navController = navController,
                mainActivity = mainActivity,
                activityScopedViewModel = activityViewModel,
            )
        }
        composable(route = "ManageThirdPartiesView") {
            ManageThirdPartiesView(
                navController = navController,
                activityScopedViewModel = activityViewModel
            )
        }
        composable(route = "ManageItemsView") {
            ManageItemsView(
                navController = navController,
                activityScopedViewModel = activityViewModel,
                mainActivity = mainActivity
            )
        }
        composable(route = "POSView") {
            POSView(
                navController = navController,
                activityViewModel = activityViewModel,
                mainActivity = mainActivity
            )
        }
        composable(route = "TablesView") {
            TablesView(
                navController = navController,
                activityScopedViewModel = activityViewModel,
                mainActivity = mainActivity
            )
        }
        composable(route = "UIWebView") {
            UIWebView(
                navController = navController,
                activityViewModel = activityViewModel
            )
        }
        composable(route = "POSPrinterView") {
            POSPrinterView(
                navController = navController,
                activityScopedViewModel = activityViewModel
            )
        }
        composable(route = "ReportsView") {
            ReportsView(
                navController = navController,
                mainActivity = mainActivity
            )
        }
    }
}