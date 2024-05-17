package com.grid.pos.model

import androidx.compose.ui.graphics.Color
import com.grid.pos.data.Company.Company
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.User.User

object SettingsModel {
    var currentUserId: String? = null

    var firebaseApplicationId: String? = null
    var firebaseApiKey: String? = null
    var firebaseProjectId: String? = null
    var firebaseDbPath: String? = null
    var companyID: String? = null
    var invoicePrinter: String? = null

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
