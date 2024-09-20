package com.grid.pos.ui.table

import androidx.compose.ui.text.toLowerCase
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.InvoiceHeader.InvoiceHeaderRepository
import com.grid.pos.model.Event
import com.grid.pos.model.TableModel
import com.grid.pos.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TablesViewModel @Inject constructor(
        private val invoiceHeaderRepository: InvoiceHeaderRepository
) : BaseViewModel() {

    private val _tablesState = MutableStateFlow(TablesState())
    val tablesState: MutableStateFlow<TablesState> = _tablesState
    var openedTables: MutableList<TableModel> = mutableListOf()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
        }
    }

    fun showError(message: String) {
        tablesState.value = tablesState.value.copy(
            warning = Event(message),
            isLoading = false
        )
    }

    fun fetchAllTables() {
        tablesState.value = tablesState.value.copy(
            isLoadingTables = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            openedTables = invoiceHeaderRepository.getAllOpenedTables()
            viewModelScope.launch(Dispatchers.Main) {
                tablesState.value = tablesState.value.copy(
                    tables = openedTables,
                    isLoadingTables = false
                )
            }
        }
    }

    fun filterTables(key: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val tables = openedTables.toMutableList()
            val filteredTables = tables.filter {
                it.table_name.lowercase().contains(key.lowercase())
            }.toMutableList()
            viewModelScope.launch(Dispatchers.Main) {
                tablesState.value = tablesState.value.copy(
                    tables = filteredTables,
                    isLoading = false
                )
            }
        }
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
                if (!invoiceHeader.isNew()) {
                    tablesState.value = tablesState.value.copy(
                        invoiceHeader = invoiceHeader,
                        moveToPos = true,
                        isLoading = false
                    )
                } else {
                    tablesState.value = tablesState.value.copy(
                        invoiceHeader = invoiceHeader,
                        step = 2,
                        moveToPos = false,
                        isLoading = false
                    )
                }

            }
        }
    }
}