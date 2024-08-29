package com.grid.pos.ui.settings.setupReports

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.App
import com.grid.pos.data.Company.CompanyRepository
import com.grid.pos.model.Event
import com.grid.pos.model.FileModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DataStoreManager
import com.grid.pos.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ReportsListViewModel @Inject constructor(
        private val companyRepository: CompanyRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReportsListState())
    val state: MutableStateFlow<ReportsListState> = _state
    val tabs = listOf(
        "Pay-Slip",
        "Pay-Ticket"
    )

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            fetchPayslips(App.getInstance())
        }
    }

    fun showError(message: String) {
        state.value = state.value.copy(
            warning = Event(message),
            isLoading = false
        )
    }

    private suspend fun fetchPayslips(context: Context) {
        val file = File(
            context.filesDir,
            "Reports"
        )
        val allReports = FileUtils.getFileModels(
            file,
            SettingsModel.selectedPayslip,
            SettingsModel.selectedPayTicket
        )
        val payslips = mutableListOf<FileModel>()
        val payTickets = mutableListOf<FileModel>()
        allReports.forEach {
            if (it.isPaySlip) {
                payslips.add(it)
            } else {
                payTickets.add(it)
            }
        }
        withContext(Dispatchers.Main) {
            state.value = state.value.copy(
                paySlips = payslips,
                payTickets = payTickets
            )
        }
    }

    fun selectFile(fileModel: FileModel) {
        state.value = state.value.copy(
            warning = null,
            isLoading = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            DataStoreManager.putString(
                DataStoreManager.DataStoreKeys.SELECTED_PAYSLIP.key,
                fileModel.getFullName()
            )
            val fileModels = if (fileModel.isPaySlip) state.value.paySlips else state.value.payTickets
            fileModels.forEach {
                it.selected = it.parentName == fileModel.parentName && it.fileName == fileModel.fileName
            }
            withContext(Dispatchers.Main) {
                if (fileModel.isPaySlip) {
                    state.value = state.value.copy(
                        paySlips = fileModels,
                        isLoading = false,
                        clear = true
                    )
                } else {
                    state.value = state.value.copy(
                        payTickets = fileModels,
                        isLoading = false,
                        clear = true
                    )
                }
            }
        }
    }

    fun deleteFile(fileModel: FileModel) {
        state.value = state.value.copy(
            warning = null,
            isLoading = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            val rootFile = File(
                App.getInstance().filesDir,
                "Reports"
            )
            val fileModels = if (fileModel.isPaySlip) state.value.paySlips else state.value.payTickets
            val file = fileModel.getFile(rootFile)
            val message = if (file.exists()) {
                if (file.delete()) {
                    fileModels.remove(fileModel)
                    "file deleted successfully!"
                } else {
                    "an error has occurred!"
                }
            } else {
                "file not found!"
            }
            withContext(Dispatchers.Main) {
                if (fileModel.isPaySlip) {
                    state.value = state.value.copy(
                        paySlips = fileModels,
                        isLoading = false,
                        clear = true,
                        warning = Event(message)
                    )
                } else {
                    state.value = state.value.copy(
                        payTickets = fileModels,
                        isLoading = false,
                        clear = true,
                        warning = Event(message)
                    )
                }
            }
        }
    }
}