package com.grid.pos.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.SharedViewModel
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.PopupModel
import com.grid.pos.model.ReportResult
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.ToastModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class BaseViewModel(
    private val sharedViewModel: SharedViewModel
) : ViewModel() {

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

    fun isLoading(): Boolean {
        return sharedViewModel.isLoading
    }

    fun showPopup(popupModel: PopupModel) {
        sharedViewModel.showPopup(true, popupModel)
    }

    fun showLoading(boolean: Boolean) {
        sharedViewModel.showLoading(boolean)
    }

    fun setIsRegistering(boolean: Boolean) {
        sharedViewModel.isRegistering = boolean
    }

    fun addReportResult(reportResults: List<ReportResult>) {
        sharedViewModel.reportsToPrint.clear()
        sharedViewModel.reportsToPrint.addAll(reportResults)
    }

    fun navigateTo(destination: String) {
        sharedViewModel.navigateTo(destination)
    }

    fun showWarning(
        warning: String,
        action: String? = null,
        onActionClicked: () -> Unit = {}
    ) {
        sharedViewModel.showToastMessage(
            ToastModel(message = warning, actionButton = action, onActionClick = onActionClicked)
        )
    }
}