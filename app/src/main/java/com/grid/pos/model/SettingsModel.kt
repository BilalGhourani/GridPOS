package com.grid.pos.model

import androidx.compose.ui.graphics.Color

object SettingsModel {
    var currentUserId: String? = null

    var firebaseApplicationId: String? = null
    var firebaseApiKey: String? = null
    var firebaseProjectId: String? = null
    var firebaseDbPath: String? = null
    var companyID: String? = null

    var loadFromRemote: Boolean = true
    var hideTaxInputs: Boolean = false
    var showPriceInItemBtn: Boolean = false

    var buttonColor: Color = Color.Blue
    var buttonTextColor: Color = Color.White
    var topBarColor: Color = Color.White
    var backgroundColor: Color = Color.White
    var textColor: Color = Color.Black
}
