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

    var selectedItemIndex: Int = 0
    var pendingStockHeaderInOut: StockHeaderInOut? = null
    private var _stockHeaderInOutState = MutableStateFlow(StockHeaderInOut())
    var stockHeaderInOutState = _stockHeaderInOutState.asStateFlow()
    val items = mutableStateListOf<StockInOutItemModel>()
    val deletedItems: MutableList<StockInOutItemModel> = mutableListOf()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
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

    fun fetchItems(withLoading: Boolean = true) {
        if (withLoading) {
            state.value = state.value.copy(
                isLoading = true
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
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
            state.value = state.value.copy(
                isLoading = true
            )
        }
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
        state.value = state.value.copy(
            isLoading = true
        )

        /*CoroutineScope(Dispatchers.IO).launch {
            if (dataModel.succeed) {
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        //selectedItem = null,
                        isLoading = false,
                        warning = Event("Warehouse details saved successfully."),
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
        }*/
    }

    fun deleteEntry() {

    }

}