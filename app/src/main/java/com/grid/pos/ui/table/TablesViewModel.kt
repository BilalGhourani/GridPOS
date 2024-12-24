package com.grid.pos.ui.table

import androidx.lifecycle.viewModelScope
import com.grid.pos.data.invoiceHeader.InvoiceHeaderRepository
import com.grid.pos.data.user.User
import com.grid.pos.data.user.UserRepository
import com.grid.pos.model.Event
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.TableModel
import com.grid.pos.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TablesViewModel @Inject constructor(
        private val invoiceHeaderRepository: InvoiceHeaderRepository,
        private val userRepository: UserRepository
) : BaseViewModel() {

    private val _tablesState = MutableStateFlow(TablesState())
    val tablesState: MutableStateFlow<TablesState> = _tablesState
    var openedTables: MutableList<TableModel> = mutableListOf()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
        }
    }

    fun resetState() {
        tablesState.value = tablesState.value.copy(
            warning = null,
            isLoading = false,
            clear = false,
            step = 1
        )
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
        val tableModel = tablesState.value.tables.firstOrNull {
            it.table_name.equals(
                tableNo,
                ignoreCase = true
            )
        } ?: TableModel(table_name = tableNo)
        viewModelScope.launch(Dispatchers.IO) {
            val tableInvoiceModel = invoiceHeaderRepository.getInvoiceByTable(tableModel)
            if (!tableInvoiceModel.lockedByUser.isNullOrEmpty()) {
                val dataModel = userRepository.getUserById(tableInvoiceModel.lockedByUser!!)
                var name = "someone"
                if (dataModel.succeed) {
                    val user = dataModel.data as? User
                    if (user != null) {
                        name = user.userName ?: "someone"
                    }
                }
                viewModelScope.launch(Dispatchers.Main) {
                    tablesState.value = tablesState.value.copy(
                        warning = Event("This table is locked by $name"),
                        moveToPos = false,
                        isLoading = false
                    )
                }
            } else {
                val invoiceHeader = tableInvoiceModel.invoiceHeader!!
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

    suspend fun lockTable(
            tableName: String
    ) {
        val tableModel = tablesState.value.tables.firstOrNull {
            it.table_name.equals(
                tableName,
                ignoreCase = true
            )
        } ?: TableModel(
            table_id = tablesState.value.invoiceHeader.invoiceHeadTableId ?: "",
            table_name = tablesState.value.invoiceHeader.invoiceHeadTaName ?: "",
            table_type = tablesState.value.invoiceHeader.invoiceHeadTableType,
        )
        val finalTableId = invoiceHeaderRepository.lockTable(
            tableModel.table_id,
            tableName
        ) ?: tableModel.table_id
        if (finalTableId.isNotEmpty()) {
            val type = if (SettingsModel.isSqlServerWebDb) {
                tableModel.table_type ?: "temp"
            } else {
                "table"
            }
            tablesState.value.invoiceHeader.invoiceHeadTableId = finalTableId
            tablesState.value.invoiceHeader.invoiceHeadTableType = type
        }
    }
}