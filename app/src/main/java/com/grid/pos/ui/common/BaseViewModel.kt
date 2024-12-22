package com.grid.pos.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.SettingsModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class BaseViewModel : ViewModel() {

    suspend fun openConnectionIfNeeded() {
        withContext(Dispatchers.IO) {
            if (SettingsModel.isConnectedToSqlServer()) {
                SQLServerWrapper.openConnection()
            }
        }
    }

    fun closeConnectionIfNeeded() {
        viewModelScope.launch(Dispatchers.IO) {
            if (SettingsModel.isConnectedToSqlServer()) {
                SQLServerWrapper.closeConnection()
            }
        }
    }

    fun <T : Any> convertToMutableList(any: Any?, clazz: Class<T>): MutableList<T> {
        return if (any is List<*>) {
            // Filter and cast items that match the provided class
            any.filterIsInstance(clazz).toMutableList()
        } else {
            mutableListOf() // Return an empty mutable list if the input is not a list
        }
    }
}