package com.grid.pos.ui.stockInOut

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.item.Item
import com.grid.pos.data.item.ItemRepository
import com.grid.pos.data.settings.SettingsRepository
import com.grid.pos.data.stockHeadInOut.header.StockHeaderInOut
import com.grid.pos.data.stockInOut.StockInOutRepository
import com.grid.pos.data.stockInOut.header.StockHeaderInOutRepository
import com.grid.pos.model.Event
import com.grid.pos.model.StockInOutItemModel
import com.grid.pos.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class StockInOutViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val stockHeaderInOutRepository: StockHeaderInOutRepository,
    private val stockInOutRepository: StockInOutRepository,
    private val settingsRepository: SettingsRepository
) : BaseViewModel() {

    private val _state = MutableStateFlow(StockInOutState())
    val state: MutableStateFlow<StockInOutState> = _state

    var stockIOTransCode: String? = null
    var selectedItemIndex: Int = 0
    var pendingStockHeaderInOut: StockHeaderInOut? = null
    private var _stockHeaderInOutState = MutableStateFlow(StockHeaderInOut())
    var stockHeaderInOutState = _stockHeaderInOutState.asStateFlow()
    val items = mutableStateListOf<StockInOutItemModel>()
    val deletedItems: MutableList<StockInOutItemModel> = mutableListOf()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
            stockIOTransCode = settingsRepository.getTransactionTypeId("Stock InOut")
        }
    }

    fun updateStockHeaderInOut(stockHeaderInOut: StockHeaderInOut) {
        _stockHeaderInOutState.value = stockHeaderInOut
    }

    fun resetState() {
        state.update {
            it.copy(
                warning = null,
                isLoading = false,
                clear = false
            )
        }
        items.clear()
        deletedItems.clear()
        updateStockHeaderInOut(StockHeaderInOut())
    }

    fun fetchTransfers() {
        state.value = state.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val listOfTransfer = stockHeaderInOutRepository.getAllStockHeaderInOuts()
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    stockHeaderInOutList = listOfTransfer,
                    isLoading = false
                )
            }
        }
    }

    fun loadTransferDetails(
        stockHeaderInOut: StockHeaderInOut
    ) {
        state.value = state.value.copy(
            isLoading = true
        )
        updateStockHeaderInOut(stockHeaderInOut)
        viewModelScope.launch(Dispatchers.IO) {
            if (state.value.items.isEmpty()) {
                fetchItems(false)
            }
            if (state.value.warehouses.isEmpty()) {
                fetchWarehouses(false)
            }
            val result =
                stockInOutRepository.getAllStockInOuts(stockHeaderInOut.stockHeadInOutId)
            val stockInOutItemModel = mutableListOf<StockInOutItemModel>()
            result.forEach { stockIO ->
                stockInOutItemModel.add(StockInOutItemModel(
                    stockInOut = stockIO,
                    stockItem = state.value.items.firstOrNull {
                        it.itemId.equals(
                            stockIO.stockInOutItemId,
                            ignoreCase = true
                        )
                    } ?: Item()))
            }
            withContext(Dispatchers.Main) {
                items.clear()
                items.addAll(stockInOutItemModel)
                state.value = state.value.copy(
                    isLoading = false
                )
            }
        }
    }

    suspend fun fetchItems(withLoading: Boolean = true) {
        if (withLoading) {
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    isLoading = true
                )
            }
        }
        withContext(Dispatchers.IO) {
            val listOfItems = itemRepository.getAllItems()
            withContext(Dispatchers.Main) {
                if (withLoading) {
                    state.value = state.value.copy(
                        items = listOfItems,
                        isLoading = false
                    )
                } else {
                    state.value = state.value.copy(
                        items = listOfItems
                    )
                }
            }
        }
    }

    suspend fun fetchWarehouses(withLoading: Boolean = true) {
        if (withLoading) {
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    isLoading = true
                )
            }
        }
        withContext(Dispatchers.IO) {
            val listOfWarehouses = settingsRepository.getAllWarehouses()
            withContext(Dispatchers.Main) {
                if (withLoading) {
                    state.value = state.value.copy(
                        warehouses = listOfWarehouses,
                        isLoading = false
                    )
                } else {
                    state.value = state.value.copy(
                        warehouses = listOfWarehouses,
                    )
                }
            }
        }
    }

    suspend fun fetchDivisions(withLoading: Boolean = true) {
        if (withLoading) {
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    isLoading = true
                )
            }
        }
        withContext(Dispatchers.IO) {
            val listOfDivisions = settingsRepository.getAllDivisions()
            withContext(Dispatchers.Main) {
                if (withLoading) {
                    state.value = state.value.copy(
                        divisions = listOfDivisions,
                        isLoading = false
                    )
                } else {
                    state.value = state.value.copy(
                        divisions = listOfDivisions,
                    )
                }
            }
        }
    }

    suspend fun fetchTransactionTypes(withLoading: Boolean = true) {
        if (withLoading) {
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    isLoading = true
                )
            }
        }
        withContext(Dispatchers.IO) {
            val transactionTypeList = settingsRepository.getTransactionTypes("Stock InOut")
            withContext(Dispatchers.Main) {
                if (withLoading) {
                    state.value = state.value.copy(
                        transactionTypes = transactionTypeList,
                        isLoading = false
                    )
                } else {
                    state.value = state.value.copy(
                        transactionTypes = transactionTypeList,
                    )
                }
            }
        }
    }

    fun showWarning(
        warning: String?,
        action: String? = null
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            state.value = state.value.copy(
                warning = if (warning.isNullOrEmpty()) null else Event(warning),
                actionLabel = action,
                isLoading = false
            )
        }
    }

    fun save() {
        if (stockHeaderInOutState.value.stockHeadInOutWaName.isNullOrEmpty()) {
            showWarning("Please select from warehouse at first!")
            return
        }
        if (stockHeaderInOutState.value.stockHeadInOutWaTpName.isNullOrEmpty()) {
            showWarning("Please select to warehouse at first!")
            return
        }
        state.value = state.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val stockHInOut = stockHeaderInOutState.value.copy()
            val succeed: Boolean
            if (stockHInOut.isNew()) {
                stockHInOut.prepareForInsert()
                if (stockHInOut.stockHeadInOutTtCode.isNullOrEmpty()) {
                    val transType =
                        state.value.transactionTypes.firstOrNull { it.transactionTypeDefault == 1 }
                    stockHInOut.stockHeadInOutTtCode =
                        transType?.transactionTypeId ?: stockIOTransCode
                    stockHInOut.stockHeadInOutTtCodeName = transType?.transactionTypeCode
                }
                val dataModel = stockHeaderInOutRepository.insert(stockHInOut)
                succeed = dataModel.succeed
                if (succeed) {
                    val addedModel = dataModel.data as StockHeaderInOut
                    val stockHInOuts = state.value.stockHeaderInOutList
                    if (stockHInOuts.isNotEmpty()) {
                        stockHInOuts.add(0, addedModel)
                    }
                    saveStockInOutItems(addedModel)
                }
            } else {
                val dataModel = stockHeaderInOutRepository.update(stockHInOut)
                succeed = dataModel.succeed
                if (succeed) {
                    val addedModel = dataModel.data as StockHeaderInOut
                    val stockHInOuts = state.value.stockHeaderInOutList
                    val index =
                        stockHInOuts.indexOfFirst { it.stockHeadInOutId == addedModel.stockHeadInOutId }
                    if (index >= 0) {
                        stockHInOuts.removeAt(index)
                        stockHInOuts.add(
                            index,
                            addedModel
                        )
                    }
                    saveStockInOutItems(addedModel)
                }
            }
            if (succeed) {
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        isLoading = false,
                        warning = Event("data saved successfully."),
                        clear = true
                    )
                }
            } else {
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun saveStockInOutItems(stockHInOut: StockHeaderInOut) {
        val stockInOutItems = items.toMutableList()
        stockInOutItems.forEach {
            it.stockInOut.prepareForInsert()
            it.stockInOut.stockInOutHeaderId = stockHInOut.stockHeadInOutId
            it.stockInOut.stockInOutWaTpName = stockHInOut.stockHeadInOutWaTpName
            if (it.stockInOut.isNew()) {
                stockInOutRepository.insert(it.stockInOut)
            } else {
                stockInOutRepository.update(it.stockInOut)
            }
        }
        deletedItems.forEach {
            if (!it.stockInOut.isNew()) {
                stockInOutRepository.delete(it.stockInOut)
            }
        }
    }

    fun delete() {
        state.value = state.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val stockHInOut = stockHeaderInOutState.value.copy()
            val dataModel = stockHeaderInOutRepository.delete(stockHInOut)
            if (dataModel.succeed) {
                val stockHInOuts = state.value.stockHeaderInOutList
                val index =
                    stockHInOuts.indexOfFirst { it.stockHeadInOutId == stockHInOut.stockHeadInOutId }
                if (index >= 0) {
                    stockHInOuts.removeAt(index)
                }
                val stockInOutItems = items.toMutableList()
                stockInOutItems.forEach {
                    stockInOutRepository.delete(it.stockInOut)
                }
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        isLoading = false,
                        warning = Event("data deleted successfully."),
                        clear = true
                    )
                }
            } else {
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        isLoading = false
                    )
                }
            }
        }
    }

}