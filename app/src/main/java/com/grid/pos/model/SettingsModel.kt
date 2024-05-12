package com.grid.pos.model

import androidx.compose.ui.graphics.Color
import com.grid.pos.data.Company.Company
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.User.User

object SettingsModel {
    var currentUserId: String? = null

    var firebaseApplicationId: String? = "1:337880577447:android:295a236f47063a5233b282"
    var firebaseApiKey: String? = "AIzaSyDSh65g8EqvGeyOviwCKmJh4jFD2iXQhYk"
    var firebaseProjectId: String? = "grids-app-8a2b7"
    var firebaseDbPath: String? = "https://grids-app-8a2b7-default-rtdb.europe-west1.firebasedatabase.app"
    var companyID: String? = null

    var loadFromRemote: Boolean = true
    var showTax: Boolean = false
    var showTax1: Boolean = false
    var showTax2: Boolean = false
    var showPriceInItemBtn: Boolean = false

    var buttonColor: Color = Color.Blue
    var buttonTextColor: Color = Color.White
    var topBarColor: Color = Color.White
    var backgroundColor: Color = Color.White
    var textColor: Color = Color.Black

    var currentCurrency: Currency? = null
    var currentUser: User? = null
    var currentCompany: Company? = null

    var companyAccessWarning: String = "You don't have access to this company!"
}
