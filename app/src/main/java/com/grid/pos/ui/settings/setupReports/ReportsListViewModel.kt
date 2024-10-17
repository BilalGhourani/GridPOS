package com.grid.pos.ui.settings.setupReports

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.App
import com.grid.pos.data.Company.CompanyRepository
import com.grid.pos.model.Event
import com.grid.pos.model.FileModel
import com.grid.pos.model.ReportTypeModel
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
        ReportTypeModel(ReportTypeEnum.PAY_SLIP.key),
        ReportTypeModel(ReportTypeEnum.PAY_TICKET.key)
    )

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            fetchReportList(App.getInstance())
        }
    }

    fun showError(message: String) {
        state.value = state.value.copy(
            warning = Event(message),
            isLoading = false
        )
    }

    private suspend fun fetchReportList(context: Context) {
        val file = File(
            context.filesDir,
            "Reports"
        )
        val filesListResult = FileUtils.getFileModels(file)
        val findSelected = filesListResult.findSelected
        val allReports = filesListResult.filesList
        val payslips = mutableListOf<FileModel>()
        val payTickets = mutableListOf<FileModel>()
        allReports.forEach {
            if (!findSelected && it.parentName.equals(
                    "default",
                    ignoreCase = true
                )
            ) {
                it.selected = true
            }
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

    fun deleteFile(
            context: Context,
            fileModel: FileModel
    ) {
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
                    fetchReportList(context)
                    "file deleted successfully!"
                } else {
                    "an error has occurred!"
                }
            } else {
                "file not found!"
            }
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    isLoading = false,
                    clear = true,
                    warning = Event(message)
                )
            }
        }
    }
}

enum class ReportTypeEnum(
        val key: String
) {
    PAY_SLIP("Pay-Slip"),
    PAY_TICKET("Pay-Ticket")
}