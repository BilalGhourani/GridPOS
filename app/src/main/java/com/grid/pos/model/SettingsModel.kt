package com.grid.pos.model

import androidx.compose.ui.graphics.Color
import com.grid.pos.data.Company.Company
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.User.User

object SettingsModel {
    var currentUserId: String? = null

    var connectionType: String = CONNECTION_TYPE.LOCAL.key

    var firebaseApplicationId: String? = null
    var firebaseApiKey: String? = null
    var firebaseProjectId: String? = null
    var firebaseDbPath: String? = null
    var fireStoreCompanyID: String? = null
    var localCompanyID: String? = null
    var sqlServerPath: String? = null
    var sqlServerDbUser: String? = null
    var sqlServerDbPassword: String? = null
    var sqlServerCompanyId: String? = null
    var isSqlServerWebDb: Boolean = true

    var cashPrinter: String? = null

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

    var siTransactionType: String = "SI"
    var rsTransactionType: String = "RS"
    var defaultBranch: String? = null
    var defaultWarehouse: String? = null

    fun getUserType(): UserType {
        if (currentUser?.userPosMode == true && currentUser?.userTableMode == true) {
            return UserType.BOTH
        } else if (currentUser?.userPosMode == true) {
            return UserType.POS
        }
        return UserType.TABLE
    }

    fun isConnectedToSqlServer(): Boolean {
        return connectionType == CONNECTION_TYPE.LOCAL.key
    }

    fun isConnectedToSqlite(): Boolean {
        return connectionType == CONNECTION_TYPE.LOCAL.key
    }

    fun isConnectedToFireStore(): Boolean {
        return connectionType == CONNECTION_TYPE.FIRESTORE.key
    }

    fun getCompanyID(): String? {
        return if (connectionType == CONNECTION_TYPE.FIRESTORE.key) {
            fireStoreCompanyID
        } else if (connectionType == CONNECTION_TYPE.SQL_SERVER.key) {
            sqlServerCompanyId
        } else {
            localCompanyID
        }
    }

    fun getSqlServerDbPath(): String {
        return "jdbc:jtds:sqlserver://${sqlServerPath};encrypt=true"
    }

    fun getTransactionType(amount: Double): String {
        return if (amount > 0) siTransactionType else rsTransactionType
    }
}

enum class UserType(val key: String) {
    POS("POS"), TABLE("TABLE"), BOTH("BOTH")
}
