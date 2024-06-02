package com.grid.pos.ui.table

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.InvoiceHeader.InvoiceHeaderRepository
import com.grid.pos.model.Event
import dagger.hilt.android.lifecycle.HiltViewModel
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
            val invoiceHeader = invoiceHeaderRepository.getInvoiceByTable(tableNo)
            viewModelScope.launch(Dispatchers.Main) {
                invoiceHeader?.let {
                    tablesState.value = tablesState.value.copy(
                        invoiceHeader = it,
                        moveToPos = true,
                        isLoading = false
                    )
                } ?: run {
                    tablesState.value = tablesState.value.copy(
                        invoiceHeader = InvoiceHeader(),
                        step = 2,
                        moveToPos = false,
                        isLoading = false
                    )
                }

            }
        }
    }
}