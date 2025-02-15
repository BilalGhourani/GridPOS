package com.grid.pos.ui.table

import androidx.lifecycle.viewModelScope
import com.grid.pos.SharedViewModel
import com.grid.pos.data.invoiceHeader.InvoiceHeader
import com.grid.pos.data.invoiceHeader.InvoiceHeaderRepository
import com.grid.pos.data.user.UserRepository
import com.grid.pos.model.Event
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.TableModel
import com.grid.pos.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TablesViewModel @Inject constructor(
    private val invoiceHeaderRepository: InvoiceHeaderRepository,
    private val userRepository: UserRepository,
    private val sharedViewModel: SharedViewModel
) : BaseViewModel(sharedViewModel) {

    private val _tablesState = MutableStateFlow(TablesState())
    val tablesState: MutableStateFlow<TablesState> = _tablesState
    private var openedTables: MutableList<TableModel> = mutableListOf()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
        }
    }

    fun updateState(newState: TablesState) {
        _tablesState.value = newState
    }

    fun resetState() {
        tablesState.value = tablesState.value.copy(
            invoiceHeader = InvoiceHeader(),
            tableName = "",
            clientCount = "",
            step = 1
        )
    }


    suspend fun fetchAllTables() {
        withContext(Dispatchers.Main) {
            tablesState.value = tablesState.value.copy(
                isLoadingTables = true
            )
        }
        withContext(Dispatchers.IO) {
            openedTables = invoiceHeaderRepository.getAllOpenedTables()
        }
        withContext(Dispatchers.Main) {
            tablesState.value = tablesState.value.copy(
                tables = openedTables,
                isLoadingTables = false
            )
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
                    tables = filteredTables
                )
                showLoading(false)
            }
        }
    }

    fun fetchInvoiceByTable(tableNo: String) {
        if (tableNo.isEmpty()) {
            showWarning("Please enter table number!")
            return
        }
        showLoading(true)
        val tableModel = tablesState.value.tables.firstOrNull {
            it.table_name.equals(
                tableNo,
                ignoreCase = true
            )
        } ?: TableModel(table_name = tableNo)
        viewModelScope.launch(Dispatchers.IO) {
            val tableInvoiceModel = invoiceHeaderRepository.getInvoiceByTable(tableModel)
            if (!tableInvoiceModel.lockedByUser.isNullOrEmpty()) {
                val user = userRepository.getUserById(tableInvoiceModel.lockedByUser!!)
                var name = "someone"
                if (user != null) {
                    name = user.userName ?: "someone"
                }
                viewModelScope.launch(Dispatchers.Main) {
                    showLoading(false)
                    showWarning("This table is locked by $name")
                }
            } else {
                val invoiceHeader = tableInvoiceModel.invoiceHeader!!
                viewModelScope.launch(Dispatchers.Main) {
                    if (!invoiceHeader.isNew()) {
                        tablesState.value = tablesState.value.copy(
                            invoiceHeader = invoiceHeader
                        )
                        showLoading(false)
                        lockTableAndMoveToPos()
                    } else {
                        tablesState.value = tablesState.value.copy(
                            invoiceHeader = invoiceHeader,
                            step = 2
                        )
                        showLoading(false)
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

    fun lockTableAndMoveToPos() {
        if (SettingsModel.isConnectedToSqlServer()) {
            showLoading(true)
            viewModelScope.launch(Dispatchers.IO) {
                lockTable(tablesState.value.tableName)
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    tablesState.value.invoiceHeader.invoiceHeadTaName = tablesState.value.tableName
                    tablesState.value.invoiceHeader.invoiceHeadClientsCount =
                        tablesState.value.clientCount.toIntOrNull() ?: 1

                    sharedViewModel.tempInvoiceHeader = tablesState.value.invoiceHeader
                    sharedViewModel.shouldLoadInvoice = true
                    sharedViewModel.isFromTable = true
                    resetState()
                    navigateTo("POSView")
                }
            }
        } else {
            tablesState.value.invoiceHeader.invoiceHeadTaName = tablesState.value.tableName
            tablesState.value.invoiceHeader.invoiceHeadClientsCount =
                tablesState.value.clientCount.toIntOrNull() ?: 1

            sharedViewModel.tempInvoiceHeader = tablesState.value.invoiceHeader
            sharedViewModel.shouldLoadInvoice = true
            sharedViewModel.isFromTable = true
            resetState()
            navigateTo("POSView")
        }
    }

    fun logout() {
        sharedViewModel.logout()
    }

}