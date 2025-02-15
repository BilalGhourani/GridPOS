package com.grid.pos.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.grid.pos.SharedViewModel
import com.grid.pos.ui.adjustment.AdjustmentView
import com.grid.pos.ui.common.UIWebView
import com.grid.pos.ui.company.ManageCompaniesView
import com.grid.pos.ui.currency.ManageCurrenciesView
import com.grid.pos.ui.family.ManageFamiliesView
import com.grid.pos.ui.home.HomeView
import com.grid.pos.ui.item.ManageItemsView
import com.grid.pos.ui.item.opening.ItemOpeningView
import com.grid.pos.ui.license.LicenseView
import com.grid.pos.ui.login.LoginView
import com.grid.pos.ui.payments.PaymentsView
import com.grid.pos.ui.pos.POSView
import com.grid.pos.ui.posPrinter.POSPrinterView
import com.grid.pos.ui.receipts.ReceiptsView
import com.grid.pos.ui.reports.SalesReportsView
import com.grid.pos.ui.settings.BackupView
import com.grid.pos.ui.settings.SettingsView
import com.grid.pos.ui.settings.setupReports.SetupReportView
import com.grid.pos.ui.settings.setupReports.ReportsListView
import com.grid.pos.ui.stockInOut.StockInOutView
import com.grid.pos.ui.table.TablesView
import com.grid.pos.ui.theme.White
import com.grid.pos.ui.thirdParty.ManageThirdPartiesView
import com.grid.pos.ui.user.ManageUsersView

@Composable
fun AuthNavGraph(
        navController: NavHostController,
        sharedViewModel: SharedViewModel,
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
                sharedViewModel = sharedViewModel
            )
        }
        composable(route = "LoginView") {
            LoginView(
                navController = navController
            )
        }
        composable(route = "SettingsView") {
            SettingsView(
                navController = navController
            )
        }
        composable(route = "ManageCurrenciesView") {
            ManageCurrenciesView(
                navController = navController
            )
        }
        composable(route = "ManageCompaniesView") {
            ManageCompaniesView(
                navController = navController
            )
        }
        composable(route = "ManageUsersView") {
            ManageUsersView(
                navController = navController
            )
        }
        composable(route = "ManageFamiliesView") {
            ManageFamiliesView(
                navController = navController
            )
        }
        composable(route = "ManageThirdPartiesView") {
            ManageThirdPartiesView(
                navController = navController
            )
        }
        composable(route = "ManageItemsView") {
            ManageItemsView(
                navController = navController
            )
        }
        composable(route = "POSView") {
            POSView(
                navController = navController,
                sharedViewModel = sharedViewModel
            )
        }
        composable(route = "TablesView") {
            TablesView(
                navController = navController
            )
        }
        composable(route = "UIWebView") {
            UIWebView(
                navController = navController,
                sharedViewModel = sharedViewModel
            )
        }
        composable(route = "POSPrinterView") {
            POSPrinterView(
                navController = navController
            )
        }
        composable(route = "ReportsView") {
            SalesReportsView(
                navController = navController
            )
        }
        composable(route = "BackupView") {
            BackupView(
                navController = navController,
                sharedViewModel = sharedViewModel
            )
        }

        composable(route = "ReportsListView") {
            ReportsListView(
                navController = navController
            )
        }

        composable(route = "SetupReportView") {
            SetupReportView(
                navController = navController
            )
        }

        composable(route = "AdjustmentView") {
            AdjustmentView(
                navController = navController
            )
        }

        composable(route = "LicenseView") {
            LicenseView(
                navController = navController,
                sharedViewModel = sharedViewModel
            )
        }

        composable(route = "PaymentsView") {
            PaymentsView(
                navController = navController,
                sharedViewModel = sharedViewModel
            )
        }

        composable(route = "ReceiptsView") {
            ReceiptsView(
                navController = navController,
                sharedViewModel = sharedViewModel
            )
        }

        composable(route = "ItemOpeningView") {
            ItemOpeningView(
                navController = navController
            )
        }

        composable(route = "StockInOutView") {
            StockInOutView(
                navController = navController
            )
        }
    }
}