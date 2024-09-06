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
}