package com.grid.pos.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grid.pos.data.company.Company
import com.grid.pos.data.currency.Currency
import com.grid.pos.data.item.Item
import com.grid.pos.data.thirdParty.ThirdParty
import com.grid.pos.data.user.User

object SettingsModel {
    var connectionType: String = CONNECTION_TYPE.LOCAL.key

    var items: MutableList<Item>? = null
    var firebaseApplicationId: String? = null
    var firebaseApiKey: String? = null
    var firebaseProjectId: String? = null
    var firebaseDbPath: String? = null
    var fireStoreCompanyID: String? = null
    var localCompanyID: String? = null
    var sqlServerPath: String? = null
    var sqlServerName: String? = null
    var sqlServerDbName: String? = null
    var sqlServerDbUser: String? = null
    var sqlServerDbPassword: String? = null
    var sqlServerCompanyId: String? = null
    var isSqlServerWebDb: Boolean = true

    var cashPrinter: String? = null
    var defaultSaleInvoice: String = "SI"
    var defaultReturnSale: String = "RS"
    var defaultPayment: String = "PV"
    var defaultReceipt: String = "RV"
    var defaultLocalBranch: String? = null
    var defaultLocalWarehouse: String? = null

    var barcodePriceName: String? = null

    var orientationType: String = ORIENTATION_TYPE.DEVICE_SENSOR.key
    var defaultReportCountry: String = Country.DEFAULT.value
    var defaultReportLanguage: String = Language.ENGLISH.value
    var showItemsInPOS: Boolean = false
    var showTax: Boolean = false
    var showTax1: Boolean = false
    var showTax2: Boolean = false
    var showPriceInItemBtn: Boolean = false
    var autoPrintTickets: Boolean = false
    var showItemQtyAlert: Boolean = false
    var allowOutOfStockSale: Boolean = true
    var hideSecondCurrency: Boolean = false

    var buttonColor: Color = Color.Black
    var buttonTextColor: Color = Color.Black
    var topBarColor: Color = Color.White
    var backgroundColor: Color = Color.White
    var textColor: Color = Color.Black

    var currentCurrency: Currency? = null
    var currentUser: User? = null
    var currentCompany: Company? = null

    var companyAccessWarning: String = "You don't have access to this company!"

    var siTransactionType: String = "null"
    var rsTransactionType: String = "null"
    var defaultSqlServerBranch: String? = null
    var defaultSqlServerWarehouse: String? = null

    var posReceiptAccCashId: String? = null
    var posReceiptAccCash1Id: String? = null
    var posReceiptAccCreditId: String? = null
    var posReceiptAccCredit1Id: String? = null
    var posReceiptAccDebitId: String? = null
    var posReceiptAccDebit1Id: String? = null

    fun getUserType(): UserType {
        if (currentUser?.userPosMode == true && currentUser?.userTableMode == true) {
            return UserType.BOTH
        } else if (currentUser?.userPosMode == true) {
            return UserType.POS
        } else if (currentUser?.userTableMode == true) {
            return UserType.TABLE
        }
        return UserType.BOTH
    }

    fun isConnectedToSqlServer(): Boolean {
        return connectionType == CONNECTION_TYPE.SQL_SERVER.key
    }

    fun isConnectedToSqlite(): Boolean {
        return connectionType == CONNECTION_TYPE.LOCAL.key
    }

    fun isConnectedToFireStore(): Boolean {
        return connectionType == CONNECTION_TYPE.FIRESTORE.key
    }

    fun getCompanyID(): String? {
        return when (connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                fireStoreCompanyID
            }

            CONNECTION_TYPE.SQL_SERVER.key -> {
                sqlServerCompanyId
            }

            else -> {
                localCompanyID
            }
        }
    }

    private fun getSaleInvoiceType(): String {
        return if (connectionType == CONNECTION_TYPE.SQL_SERVER.key) {
            siTransactionType
        } else {
            defaultSaleInvoice
        }
    }

    private fun getReturnSaleType(): String {
        return if (connectionType == CONNECTION_TYPE.SQL_SERVER.key) {
            rsTransactionType
        } else {
            defaultReturnSale
        }
    }

    fun getTransactionType(amount: Double): String {
        return if (amount >= 0) getSaleInvoiceType() else getReturnSaleType()
    }

    fun getItemCellWidth(): Dp {
        return if (showPriceInItemBtn) 150.dp else 120.dp
    }
}

enum class UserType(val key: String) {
    POS("POS"), TABLE("TABLE"), BOTH("BOTH")
}
