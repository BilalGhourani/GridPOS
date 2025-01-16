package com.grid.pos.ui.stockInOut

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.invoiceHeader.InvoiceHeader
import com.grid.pos.data.item.Item
import com.grid.pos.data.item.ItemRepository
import com.grid.pos.data.posReceipt.PosReceipt
import com.grid.pos.data.settings.SettingsRepository
import com.grid.pos.data.stockAdjustment.StockAdjustmentRepository
import com.grid.pos.data.stockHeaderAdjustment.StockHeaderAdjustment
import com.grid.pos.data.stockHeaderAdjustment.StockHeaderAdjustmentRepository
import com.grid.pos.model.Event
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.StockAdjItemModel
import com.grid.pos.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class StockInOutViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val stockAdjustmentRepository: StockAdjustmentRepository,
    private val stockHeaderAdjustmentRepository: StockHeaderAdjustmentRepository,
    private val settingsRepository: SettingsRepository
) : BaseViewModel() {

    private val _state = MutableStateFlow(StockInOutState())
    val state: MutableStateFlow<StockInOutState> = _state

    val items = mutableStateListOf<StockAdjItemModel>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
        }
    }

    fun resetState() {
        state.value = state.value.copy(
            warning = null,
            isLoading = false,
            clear = false
        )
    }

    fun fetchTransfers() {
        state.value = state.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val listOfTransfer = stockHeaderAdjustmentRepository.getAllStockHeaderAdjustments()
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    stockHeaderAdjustments = listOfTransfer,
                    isLoading = false
                )
            }
        }
    }

    fun loadTransferDetails(
        stockHeaderAdjustment: StockHeaderAdjustment
    ) {
        state.value = state.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            if (state.value.items.isEmpty()) {
                fetchItems(false)
            }
            if (state.value.warehouses.isEmpty()) {
                fetchWarehouses(false)
            }
            val result =
                stockAdjustmentRepository.getAllStockAdjustments(stockHeaderAdjustment.stockHAId)
            val stockAdjItemModels = mutableListOf<StockAdjItemModel>()
            result.forEach { stockAdj ->
                stockAdjItemModels.add(StockAdjItemModel(
                    stockAdjustment = stockAdj,
                    stockAdjItem = state.value.items.firstOrNull {
                        it.itemId.equals(
                            stockAdj.stockAdjItemId,
                            ignoreCase = true
                        )
                    } ?: Item()))
            }
            withContext(Dispatchers.Main) {
                items.clear()
                items.addAll(stockAdjItemModels)
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

}