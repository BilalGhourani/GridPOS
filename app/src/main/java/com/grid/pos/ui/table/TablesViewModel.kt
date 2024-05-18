package com.grid.pos.ui.table

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.InvoiceHeader.InvoiceHeaderRepository
import com.grid.pos.data.User.User
import com.grid.pos.data.User.UserRepository
import com.grid.pos.interfaces.OnResult
import com.grid.pos.model.Event
import com.grid.pos.utils.Extension.encryptCBC
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TablesViewModel @Inject constructor(
        private val invoiceHeaderRepository: InvoiceHeaderRepository
) : ViewModel() {

    private val _tablesState = MutableStateFlow(TablesState())
    val tablesState: MutableStateFlow<TablesState> = _tablesState

    fun showError(message: String) {
        tablesState.value = tablesState.value.copy(
            warning = Event(message),
            isLoading = false
        )
    }

    fun fetchInvoiceByTable(tableNo: String) {
        if (tableNo.isEmpty()) {
            tablesState.value = tablesState.value.copy(
                warning = Event("Please enter table number!"),
                isLoading = false
            )
            return
        }
        tablesState.value = tablesState.value.copy(
            warning = null,
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            invoiceHeaderRepository.getInvoiceByTable(tableNo,
                object : OnResult {
                    override fun onSuccess(result: Any) {
                        viewModelScope.launch(Dispatchers.Main) {
                            tablesState.value = tablesState.value.copy(
                                invoiceHeader = result as InvoiceHeader,
                                step = 2,
                                isLoading = false
                            )
                        }
                    }

                    override fun onFailure(
                            message: String,
                            errorCode: Int
                    ) {
                        viewModelScope.launch(Dispatchers.Main) {
                            tablesState.value = tablesState.value.copy(
                                warning = Event("Failed to retrieve related invoice!")
                            )
                        }
                    }
                })
        }
    }
}