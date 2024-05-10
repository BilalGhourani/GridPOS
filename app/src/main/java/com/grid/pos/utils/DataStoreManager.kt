package com.grid.pos.utils

import android.content.Context
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
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Extension.isNullOrZero
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

object DataStoreManager {
    private val context = App.getInstance().applicationContext
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
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    suspend fun removeKey(
            key: String
    ) {
        val preferencesKey = stringPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences.remove(preferencesKey)
        }
    }

    suspend fun putInt(
            key: String,
            value: Int
    ) {
        val preferencesKey = intPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    suspend fun putLong(
            key: String,
            value: Long
    ) {
        val preferencesKey = longPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    suspend fun putBoolean(
            key: String,
            value: Boolean
    ) {
        val preferencesKey = booleanPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    suspend fun getString(
            key: String,
            fallback: String = ""
    ): String {
        return try {
            val preferencesKey = stringPreferencesKey(key)
            val preferences = context.dataStore.data.first()
            var value = preferences[preferencesKey]
            return value ?: fallback
        } catch (e: Exception) {
            e.printStackTrace()
            fallback
        }
    }

    suspend fun getInt(
            key: String,
            fallback: Int = -1
    ): Int {
        return try {
            val preferencesKey = intPreferencesKey(key)
            val preferences = context.dataStore.data.first()
            var value = preferences[preferencesKey]
            return value ?: fallback
        } catch (e: Exception) {
            e.printStackTrace()
            fallback
        }
    }

    suspend fun getLong(
            key: String,
            fallback: Long = -1L
    ): Long {
        return try {
            val preferencesKey = longPreferencesKey(key)
            val preferences = context.dataStore.data.first()
            return preferences[preferencesKey] ?: fallback
        } catch (e: Exception) {
            e.printStackTrace()
            fallback
        }
    }

    suspend fun getBoolean(
            key: String,
            fallback: Boolean = false
    ): Boolean {
        return try {
            val preferencesKey = booleanPreferencesKey(key)
            val preferences = context.dataStore.data.first()
            var value = preferences[preferencesKey]
            return value ?: fallback
        } catch (e: Exception) {
            e.printStackTrace()
            fallback
        }
    }

    suspend fun getValueByKey(key: Preferences.Key<*>): Any? {
        val value = context.dataStore.data.map {
            it[key]
        }
        return value.firstOrNull()
    }

    suspend fun deleteAll() {
        context.dataStore.edit {
            it.clear()
        }
    }

    suspend fun initSettingsModel() {
        SettingsModel.currentUserId = getString(DataStoreKeys.CURRENT_USER_ID.key)
        val buttonColor = getString(DataStoreKeys.BUTTON_COLOR.key)
        val buttonTextColor = getString(DataStoreKeys.BUTTON_TEXT_COLOR.key)
        val topBarColor = getString(DataStoreKeys.TOP_BAR_COLOR.key)
        val backgroundColor = getString(DataStoreKeys.BACKGROUND_COLOR.key)
        val textColor = getString(DataStoreKeys.TEXT_COLOR.key)
        if (buttonColor?.isNullOrEmpty() == false) {
            SettingsModel.buttonColor = Color(buttonColor.toColorInt())
        }

        if (buttonTextColor?.isNullOrEmpty() == false) {
            SettingsModel.buttonTextColor = Color(buttonTextColor.toColorInt())
        }

        if (topBarColor?.isNullOrEmpty() == false) {
            SettingsModel.topBarColor = Color(topBarColor.toColorInt())
        }

        if (backgroundColor?.isNullOrEmpty() == false) {
            SettingsModel.backgroundColor = Color(backgroundColor.toColorInt())
        }

        if (textColor?.isNullOrEmpty() == false) {
            SettingsModel.textColor = Color(textColor.toColorInt())
        }
        SettingsModel.loadFromRemote = getBoolean(DataStoreKeys.LOAD_FROM_REMOTE.key, true) == true
        SettingsModel.showTax = getBoolean(DataStoreKeys.SHOW_TAX.key, false) == true
        SettingsModel.showTax1 = getBoolean(DataStoreKeys.SHOW_TAX1.key, false) == true
        SettingsModel.showTax2 = getBoolean(DataStoreKeys.SHOW_TAX2.key, false) == true
        SettingsModel.showPriceInItemBtn = getBoolean(DataStoreKeys.SHOW_PRICE_IN_ITEM_BTN.key, false) == true

        SettingsModel.firebaseApplicationId = getString(DataStoreKeys.FIREBASE_APP_ID.key)
        SettingsModel.firebaseApiKey = getString(DataStoreKeys.FIREBASE_API_KEY.key)
        SettingsModel.firebaseProjectId = getString(DataStoreKeys.FIREBASE_PROJECT_ID.key)
        SettingsModel.firebaseDbPath = getString(DataStoreKeys.FIREBASE_DB_PATH.key)
        SettingsModel.companyID = getString(
            DataStoreKeys.COMPANY_ID.key, ""
        ).ifEmpty { "b446ad20-506f-40e1-83e5-022c748f39c0" }
    }

    enum class DataStoreKeys(val key: String) {
        CURRENT_USER_ID("CURRENT_USER_ID"),

        FIREBASE_APP_ID("FIREBASE_APP_ID"), FIREBASE_API_KEY(
            "FIREBASE_API_KEY"
        ),
        FIREBASE_PROJECT_ID("FIREBASE_PROJECT_ID"), FIREBASE_DB_PATH(
            "FIREBASE_DB_PATH"
        ),
        COMPANY_ID("COMPANY_ID"),

        LOAD_FROM_REMOTE("LOAD_FROM_REMOTE"), SHOW_TAX("SHOW_TAX"), SHOW_TAX1(
            "SHOW_TAX1"
        ),
        SHOW_TAX2("SHOW_TAX2"), SHOW_PRICE_IN_ITEM_BTN("SHOW_PRICE_IN_ITEM_BTN"),

        BUTTON_COLOR("BUTTON_COLOR"), BUTTON_TEXT_COLOR("BUTTON_TEXT_COLOR"), BACKGROUND_COLOR(
            "BACKGROUND_COLOR"
        ),
        TOP_BAR_COLOR("TOP_BAR_COLOR"), TEXT_COLOR("TEXT_COLOR"),
    }
}