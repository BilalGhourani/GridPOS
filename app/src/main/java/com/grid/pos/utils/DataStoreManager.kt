package com.grid.pos.utils

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.grid.pos.App
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.Country
import com.grid.pos.model.Language
import com.grid.pos.model.ORIENTATION_TYPE
import com.grid.pos.model.SettingsModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

object DataStoreManager {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "GRID_POS_DATA_STORE"
    )

    suspend fun initValues() {
        initSettingsModel()
    }

    suspend fun putString(
        key: String,
        value: String
    ) {
        val preferencesKey = stringPreferencesKey(key)
        App.getInstance().applicationContext.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    suspend fun removeKey(
        key: String
    ) {
        val preferencesKey = stringPreferencesKey(key)
        App.getInstance().applicationContext.dataStore.edit { preferences ->
            preferences.remove(preferencesKey)
        }
    }

    suspend fun putInt(
        key: String,
        value: Int
    ) {
        val preferencesKey = intPreferencesKey(key)
        App.getInstance().applicationContext.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    suspend fun putLong(
        key: String,
        value: Long
    ) {
        val preferencesKey = longPreferencesKey(key)
        App.getInstance().applicationContext.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    suspend fun putBoolean(
        key: String,
        value: Boolean
    ) {
        val preferencesKey = booleanPreferencesKey(key)
        App.getInstance().applicationContext.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    suspend fun getString(
        key: String,
        fallback: String = ""
    ): String {
        return try {
            val preferencesKey = stringPreferencesKey(key)
            val preferences = App.getInstance().applicationContext.dataStore.data.first()
            val value = preferences[preferencesKey]
            return value ?: fallback
        } catch (e: Exception) {
            Log.e(
                "exception",
                e.message.toString()
            )
            fallback
        }
    }

    suspend fun getInt(
        key: String,
        fallback: Int = -1
    ): Int {
        return try {
            val preferencesKey = intPreferencesKey(key)
            val preferences = App.getInstance().applicationContext.dataStore.data.first()
            val value = preferences[preferencesKey]
            return value ?: fallback
        } catch (e: Exception) {
            Log.e(
                "exception",
                e.message.toString()
            )
            fallback
        }
    }

    suspend fun getLong(
        key: String,
        fallback: Long = -1L
    ): Long {
        return try {
            val preferencesKey = longPreferencesKey(key)
            val preferences = App.getInstance().applicationContext.dataStore.data.first()
            return preferences[preferencesKey] ?: fallback
        } catch (e: Exception) {
            Log.e(
                "exception",
                e.message.toString()
            )
            fallback
        }
    }

    suspend fun getBoolean(
        key: String,
        fallback: Boolean = false
    ): Boolean {
        return try {
            val preferencesKey = booleanPreferencesKey(key)
            val preferences = App.getInstance().applicationContext.dataStore.data.first()
            val value = preferences[preferencesKey]
            return value ?: fallback
        } catch (e: Exception) {
            Log.e(
                "exception",
                e.message.toString()
            )
            fallback
        }
    }

    suspend fun getValueByKey(key: Preferences.Key<*>): Any? {
        val value = App.getInstance().applicationContext.dataStore.data.map {
            it[key]
        }
        return value.firstOrNull()
    }

    suspend fun deleteAll() {
        App.getInstance().applicationContext.dataStore.edit {
            it.clear()
        }
    }

    private suspend fun initSettingsModel() {
        val buttonColor = getString(DataStoreKeys.BUTTON_COLOR.key)
        val buttonTextColor = getString(DataStoreKeys.BUTTON_TEXT_COLOR.key)
        val topBarColor = getString(DataStoreKeys.TOP_BAR_COLOR.key)
        val backgroundColor = getString(DataStoreKeys.BACKGROUND_COLOR.key)
        val textColor = getString(DataStoreKeys.TEXT_COLOR.key)

        if (buttonColor.isNotEmpty()) {
            SettingsModel.buttonColor = Color(buttonColor.toColorInt())
        }

        if (buttonTextColor.isNotEmpty()) {
            SettingsModel.buttonTextColor = Color(buttonTextColor.toColorInt())
        }

        if (topBarColor.isNotEmpty()) {
            SettingsModel.topBarColor = Color(topBarColor.toColorInt())
        }

        if (backgroundColor.isNotEmpty()) {
            SettingsModel.backgroundColor = Color(backgroundColor.toColorInt())
        }

        if (textColor.isNotEmpty()) {
            SettingsModel.textColor = Color(textColor.toColorInt())
        }
        SettingsModel.connectionType = getString(
            DataStoreKeys.CONNECTION_TYPE.key,
            CONNECTION_TYPE.LOCAL.key
        )
        SettingsModel.showItemsInPOS = getBoolean(
            DataStoreKeys.SHOW_ITEMS_IN_POS.key,
            false
        ) == true
        SettingsModel.orientationType = getString(
            DataStoreKeys.ORIENTATION_TYPE.key,
            ORIENTATION_TYPE.DEVICE_SENSOR.key
        )
        SettingsModel.defaultReportCountry = getString(
            DataStoreKeys.REPORT_COUNTRY.key,
            Country.DEFAULT.value
        )
        SettingsModel.defaultReportLanguage = getString(
            DataStoreKeys.REPORT_LANGUAGE.key,
            Language.ENGLISH.value
        )
        SettingsModel.showTax = getBoolean(
            DataStoreKeys.SHOW_TAX.key,
            false
        ) == true
        SettingsModel.showTax1 = getBoolean(
            DataStoreKeys.SHOW_TAX1.key,
            false
        ) == true
        SettingsModel.showTax2 = getBoolean(
            DataStoreKeys.SHOW_TAX2.key,
            false
        ) == true
        SettingsModel.showPriceInItemBtn = getBoolean(
            DataStoreKeys.SHOW_PRICE_IN_ITEM_BTN.key,
            false
        ) == true

        SettingsModel.autoPrintTickets = getBoolean(
            DataStoreKeys.AUTO_PRINT_TICKETS.key,
            false
        ) == true
        SettingsModel.showItemQtyAlert = getBoolean(
            DataStoreKeys.SHOW_ITEM_QTY_ALERT.key,
            false
        ) == true
        SettingsModel.allowOutOfStockSale = getBoolean(
            DataStoreKeys.ALLOW_OUT_OF_STOCK_SALE.key,
            true
        ) == true
        SettingsModel.hideSecondCurrency = getBoolean(
            DataStoreKeys.HIDE_SECOND_CURRENCY.key,
            false
        ) == true

        SettingsModel.firebaseApplicationId = getString(DataStoreKeys.FIREBASE_APP_ID.key)
        SettingsModel.firebaseApiKey = getString(DataStoreKeys.FIREBASE_API_KEY.key)
        SettingsModel.firebaseProjectId = getString(DataStoreKeys.FIREBASE_PROJECT_ID.key)
        SettingsModel.firebaseDbPath = getString(DataStoreKeys.FIREBASE_DB_PATH.key)/*if (BuildConfig.DEBUG) {
            SettingsModel.firebaseApplicationId = SettingsModel.firebaseApplicationId!!.ifEmpty { "1:337880577447:android:295a236f47063a5233b282" }
            SettingsModel.firebaseApiKey = SettingsModel.firebaseApiKey!!.ifEmpty { "AIzaSyDSh65g8EqvGeyOviwCKmJh4jFD2iXQhYk" }
            SettingsModel.firebaseProjectId = SettingsModel.firebaseProjectId!!.ifEmpty { "grids-app-8a2b7" }
            SettingsModel.firebaseDbPath = SettingsModel.firebaseDbPath!!.ifEmpty { "https://grids-app-8a2b7-default-rtdb.europe-west1.firebasedatabase.app" }
        }*/


        SettingsModel.fireStoreCompanyID = getString(
            DataStoreKeys.FIRESTORE_COMPANY_ID.key,
            ""
        )

        SettingsModel.localCompanyID = getString(
            DataStoreKeys.LOCAL_COMPANY_ID.key,
            ""
        )

        SettingsModel.sqlServerPath = getString(
            DataStoreKeys.SQL_SERVER_PATH.key,
            ""
        )
        SettingsModel.sqlServerName = getString(
            DataStoreKeys.SQL_SERVER_NAME.key,
            ""
        )
        SettingsModel.sqlServerDbName = getString(
            DataStoreKeys.SQL_SERVER_DB_NAME.key,
            ""
        )
        SettingsModel.sqlServerDbUser = getString(
            DataStoreKeys.SQL_SERVER_DB_USER.key,
            ""
        )
        SettingsModel.sqlServerDbPassword = getString(
            DataStoreKeys.SQL_SERVER_DB_PASSWORD.key,
            ""
        )
        SettingsModel.sqlServerCompanyId = getString(
            DataStoreKeys.SQL_SERVER_COMPANY_ID.key,
            ""
        )

        SettingsModel.isSqlServerWebDb = getBoolean(
            DataStoreKeys.IS_SQL_SERVER_WEB_DB.key,
            true
        )

        SettingsModel.cashPrinter = getString(
            DataStoreKeys.CASH_PRINTER.key,
            ""
        ).ifEmpty { null }
        SettingsModel.defaultSaleInvoice = getString(
            DataStoreKeys.DEFAULT_SALE_INVOICE.key,
            "SI"
        )
        SettingsModel.defaultReturnSale = getString(
            DataStoreKeys.DEFAULT_RETURN_SALE.key,
            "RS"
        )
        SettingsModel.defaultPayment = getString(
            DataStoreKeys.DEFAULT_PAYMENT.key,
            ""
        )
        SettingsModel.defaultReceipt = getString(
            DataStoreKeys.DEFAULT_RECEIPT.key,
            ""
        )
        SettingsModel.defaultLocalBranch = getString(
            DataStoreKeys.DEFAULT_BRANCH.key,
            ""
        ).ifEmpty { null }
        SettingsModel.defaultLocalWarehouse = getString(
            DataStoreKeys.DEFAULT_WAREHOUSE.key,
            ""
        ).ifEmpty { null }
        SettingsModel.barcodePriceName = getString(
            DataStoreKeys.BARCODE_PRICE_NAME.key,
            ""
        ).ifEmpty { null }
    }

    enum class DataStoreKeys(val key: String) {
        FIREBASE_APP_ID("FIREBASE_APP_ID"), FIREBASE_API_KEY(
            "FIREBASE_API_KEY"
        ),
        FIREBASE_PROJECT_ID("FIREBASE_PROJECT_ID"), FIREBASE_DB_PATH(
            "FIREBASE_DB_PATH"
        ),
        FIRESTORE_COMPANY_ID("FIRESTORE_COMPANY_ID"), LOCAL_COMPANY_ID("LOCAL_COMPANY_ID"),

        SQL_SERVER_PATH("SQL_SERVER_PATH"), SQL_SERVER_NAME("SQL_SERVER_NAME"), SQL_SERVER_DB_NAME("SQL_SERVER_DB_NAME"),
        SQL_SERVER_DB_USER("SQL_SERVER_DB_USER"), SQL_SERVER_DB_PASSWORD("SQL_SERVER_DB_PASSWORD"), SQL_SERVER_COMPANY_ID(
            "SQL_SERVER_COMPANY_ID"
        ),
        IS_SQL_SERVER_WEB_DB("IS_SQL_SERVER_WEB_DB"),


        CASH_PRINTER("CASH_PRINTER"), DEFAULT_SALE_INVOICE("DEFAULT_SSALE_INVOICE"), DEFAULT_RETURN_SALE(
            "DEFAULT_RETURN_SALE"
        ),
        DEFAULT_BRANCH("DEFAULT_BRANCH"), DEFAULT_WAREHOUSE("DEFAULT_WAREHOUSE"), BARCODE_PRICE_NAME(
            "BARCODE_PRICE_NAME"
        ),
        DEFAULT_PAYMENT("DEFAULT_PAYMENT"), DEFAULT_RECEIPT("DEFAULT_RECEIPT"),

        CONNECTION_TYPE("CONNECTION_TYPE"), SHOW_ITEMS_IN_POS("SHOW_ITEMS_IN_POS"), ORIENTATION_TYPE(
            "ORIENTATION_TYPE"
        ),
        REPORT_LANGUAGE("REPORT_LANGUAGE"), REPORT_COUNTRY("REPORT_COUNTRY"), SHOW_TAX("SHOW_TAX"), SHOW_TAX1(
            "SHOW_TAX1"
        ),
        SHOW_TAX2("SHOW_TAX2"), SHOW_PRICE_IN_ITEM_BTN("SHOW_PRICE_IN_ITEM_BTN"), AUTO_PRINT_TICKETS(
            "AUTO_PRINT_TICKETS"
        ),
        SHOW_ITEM_QTY_ALERT("SHOW_ITEM_QTY_ALERT"), ALLOW_OUT_OF_STOCK_SALE("ALLOW_OUT_OF_STOCK_SALE"), HIDE_SECOND_CURRENCY(
            "HIDE_SECOND_CURRENCY"
        ),

        BUTTON_COLOR("BUTTON_COLOR"), BUTTON_TEXT_COLOR("BUTTON_TEXT_COLOR"), BACKGROUND_COLOR(
            "BACKGROUND_COLOR"
        ),
        TOP_BAR_COLOR("TOP_BAR_COLOR"), TEXT_COLOR("TEXT_COLOR"),
    }
}