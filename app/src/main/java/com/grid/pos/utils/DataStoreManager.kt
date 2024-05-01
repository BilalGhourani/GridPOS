package com.grid.pos.utils

import android.content.Context
import androidx.compose.ui.graphics.Color
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
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "GRID_POS_DATA_STORE")

    suspend fun initValues() {
        initSettingsModel()
    }

    suspend fun putString(key: String, value: String) {
        val preferencesKey = stringPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    suspend fun putInt(key: String, value: Int) {
        val preferencesKey = intPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    suspend fun putLong(key: String, value: Long) {
        val preferencesKey = longPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    suspend fun putBoolean(key: String, value: Boolean) {
        val preferencesKey = booleanPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    suspend fun getString(key: String): String {
        return try {
            val preferencesKey = stringPreferencesKey(key)
            val preferences = context.dataStore.data.first()
            var value = preferences[preferencesKey]
            return value ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    suspend fun getInt(key: String): Int? {
        return try {
            val preferencesKey = intPreferencesKey(key)
            val preferences = context.dataStore.data.first()
            var value = preferences[preferencesKey]
            return value ?: -1
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }

    suspend fun getLong(key: String): Long? {
        return try {
            val preferencesKey = longPreferencesKey(key)
            val preferences = context.dataStore.data.first()
            return preferences[preferencesKey]
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getBoolean(key: String): Boolean? {
        return try {
            val preferencesKey = booleanPreferencesKey(key)
            val preferences = context.dataStore.data.first()
            var value = preferences[preferencesKey]
            return value ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
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
        val buttonColor = getLong(DataStoreKeys.BUTTON_COLOR.key)
        val buttonTextColor = getLong(DataStoreKeys.BUTTON_TEXT_COLOR.key)
        if (buttonColor?.isNullOrZero() == false) {
            SettingsModel.buttonColor = Color(buttonColor)
        }

        if (buttonTextColor?.isNullOrZero() == false) {
            SettingsModel.buttonTextColor = Color(buttonTextColor)
        }
        SettingsModel.loadFromRemote = getBoolean(DataStoreKeys.LOAD_FROM_REMOTE.key) == true

    }


    enum class DataStoreKeys(val key: String) {
        BUTTON_COLOR("BUTTON_COLOR"),
        BUTTON_TEXT_COLOR("BUTTON_TEXT_COLOR"),
        LOAD_FROM_REMOTE("LOAD_FROM_REMOTE"),
    }
}