package com.grid.pos.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.grid.pos.ui.family.ManageFamiliesView
import com.grid.pos.ui.Item.ManageItemsView
import com.grid.pos.ui.common.UIWebView
import com.grid.pos.ui.company.ManageCompaniesView
import com.grid.pos.ui.currency.ManageCurrenciesView
import com.grid.pos.ui.user.ManageUsersView
import com.grid.pos.ui.home.HomeView
import com.grid.pos.ui.home.components.CollectionsView
import com.grid.pos.ui.login.LoginView
import com.grid.pos.ui.pos.ManagePosView
import com.grid.pos.ui.table.ManageTablesView
import com.grid.pos.ui.theme.White
import com.grid.pos.ui.thirdParty.ManageThirdPartiesView

@Composable
fun AuthNavGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
        .background(color = White)
        .padding(0.dp)
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.background(color = White)
    ) {

        composable(route = "HomeView") { HomeView(navController) }
        composable(route = "LoginView") { LoginView(navController) }
        composable(route = "CollectionsView") { CollectionsView(navController) }
        composable(route = "ManageCurrenciesView") { ManageCurrenciesView(navController) }
        composable(route = "ManageCompaniesView") { ManageCompaniesView(navController) }
        composable(route = "ManageUsersView") { ManageUsersView(navController) }
        composable(route = "ManageFamiliesView") { ManageFamiliesView(navController) }
        composable(route = "ManageThirdPartiesView") { ManageThirdPartiesView(navController) }
        composable(route = "ManageItemsView") { ManageItemsView(navController) }
        composable(route = "ManagePosView") { ManagePosView(navController) }
        composable(route = "ManageTablesView") { ManageTablesView(navController) }
        composable(route = "UIWebView") { UIWebView(navController) }
    }
}