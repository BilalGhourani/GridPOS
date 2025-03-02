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
import com.grid.pos.ui.settings.setupReports.ReportsListView
import com.grid.pos.ui.settings.setupReports.SetupReportView
import com.grid.pos.ui.stockAdjustment.StockAdjustmentView
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

        composable(route = Screen.HomeView.route) {
            HomeView(
                navController = navController,
                sharedViewModel = sharedViewModel
            )
        }
        composable(route = Screen.LoginView.route) {
            LoginView(
                navController = navController
            )
        }
        composable(route = Screen.SettingsView.route) {
            SettingsView(
                navController = navController
            )
        }
        composable(route = Screen.ManageCurrenciesView.route) {
            ManageCurrenciesView(
                navController = navController
            )
        }
        composable(route = Screen.ManageCompaniesView.route) {
            ManageCompaniesView(
                navController = navController
            )
        }
        composable(route = Screen.ManageUsersView.route) {
            ManageUsersView(
                navController = navController
            )
        }
        composable(route = Screen.ManageFamiliesView.route) {
            ManageFamiliesView(
                navController = navController
            )
        }
        composable(route = Screen.ManageThirdPartiesView.route) {
            ManageThirdPartiesView(
                navController = navController
            )
        }
        composable(route = Screen.ManageItemsView.route) {
            ManageItemsView(
                navController = navController
            )
        }
        composable(route = Screen.POSView.route) {
            POSView(
                navController = navController
            )
        }
        composable(route = Screen.TablesView.route) {
            TablesView(
                navController = navController
            )
        }
        composable(route = Screen.UIWebView.route) {
            UIWebView(
                navController = navController,
                sharedViewModel = sharedViewModel
            )
        }
        composable(route = Screen.POSPrinterView.route) {
            POSPrinterView(
                navController = navController
            )
        }
        composable(route = Screen.SalesReportsView.route) {
            SalesReportsView(
                navController = navController
            )
        }
        composable(route = Screen.BackupView.route) {
            BackupView(
                navController = navController,
                sharedViewModel = sharedViewModel
            )
        }

        composable(route = Screen.ReportsListView.route) {
            ReportsListView(
                navController = navController
            )
        }

        composable(route = Screen.SetupReportView.route) {
            SetupReportView(
                navController = navController
            )
        }

        composable(route = Screen.AdjustmentView.route) {
            AdjustmentView(
                navController = navController
            )
        }

        composable(route = Screen.LicenseView.route) {
            LicenseView(
                navController = navController,
                sharedViewModel = sharedViewModel
            )
        }

        composable(route = Screen.PaymentsView.route) {
            PaymentsView(
                navController = navController
            )
        }

        composable(route = Screen.ReceiptsView.route) {
            ReceiptsView(
                navController = navController
            )
        }

        composable(route = Screen.ItemOpeningView.route) {
            ItemOpeningView(
                navController = navController
            )
        }

        composable(route = Screen.StockInOutView.route) {
            StockInOutView(
                navController = navController
            )
        }

        composable(route = Screen.StockAdjustmentView.route) {
            StockAdjustmentView(
                navController = navController
            )
        }
    }
}