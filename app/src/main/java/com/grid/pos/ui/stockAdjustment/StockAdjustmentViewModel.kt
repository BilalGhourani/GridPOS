package com.grid.pos.ui.stockAdjustment

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.grid.pos.SharedViewModel
import com.grid.pos.data.item.Item
import com.grid.pos.data.item.ItemRepository
import com.grid.pos.data.settings.SettingsRepository
import com.grid.pos.data.stockAdjustment.StockAdjustmentRepository
import com.grid.pos.data.stockHeaderAdjustment.StockHeaderAdjustment
import com.grid.pos.data.stockHeaderAdjustment.StockHeaderAdjustmentRepository
import com.grid.pos.interfaces.OnBarcodeResult
import com.grid.pos.model.StockAdjItemModel
import com.grid.pos.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class StockAdjustmentViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val stockHeaderAdjustmentRepository: StockHeaderAdjustmentRepository,
    private val stockAdjustmentRepository: StockAdjustmentRepository,
    private val settingsRepository: SettingsRepository,
    private val sharedViewModel: SharedViewModel
) : BaseViewModel(sharedViewModel) {

    private val _state = MutableStateFlow(StockAdjustmentState())
    val state: MutableStateFlow<StockAdjustmentState> = _state

    private var stockIOTransCode: String? = null
    var selectedItemIndex: Int = 0

    private var _stockHeaderAdjState = MutableStateFlow(StockHeaderAdjustment())
    var stockHeaderAdjState = _stockHeaderAdjState.asStateFlow()
    val items = mutableStateListOf<StockAdjItemModel>()
    val deletedItems: MutableList<StockAdjItemModel> = mutableListOf()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
            stockIOTransCode = settingsRepository.getTransactionTypeId("Stock InOut")
        }
    }

    fun updateStockHeaderAdjustment(stockHeaderAdjustment: StockHeaderAdjustment) {
        _stockHeaderAdjState.value = stockHeaderAdjustment
    }

    fun resetState() {
        items.clear()
        deletedItems.clear()
        updateStockHeaderAdjustment(StockHeaderAdjustment())
    }

    suspend fun updateRealItemPrice(item: Item, withLoading: Boolean = true) {
        sharedViewModel.updateRealItemPrice(item, withLoading)
    }

    fun fetchTransfers() {
        showLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            val listOfAdjustments = stockHeaderAdjustmentRepository.getAllStockHeaderAdjustments()
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    stockHeaderAdjustmentList = listOfAdjustments
                )
                showLoading(false)
            }
        }
    }

    fun loadTransferDetails(
        stockHeaderAdjustment: StockHeaderAdjustment
    ) {
        showLoading(true)
        updateStockHeaderAdjustment(stockHeaderAdjustment)
        viewModelScope.launch(Dispatchers.IO) {
            if (state.value.items.isEmpty()) {
                fetchItems(false)
            }
            if (state.value.warehouses.isEmpty()) {
                fetchWarehouses(false)
            }
            val result =
                stockAdjustmentRepository.getAllStockAdjustments(stockHeaderAdjustment.stockHAId)
            val stockAdjItemModel = mutableListOf<StockAdjItemModel>()
            result.forEach { stockAdj ->
                stockAdjItemModel.add(StockAdjItemModel(
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
                items.addAll(stockAdjItemModel)
                showLoading(false)
            }
        }
    }

    suspend fun fetchItems(withLoading: Boolean = true) {
        if (withLoading) {
            withContext(Dispatchers.Main) {
                showLoading(true)
            }
        }
        withContext(Dispatchers.IO) {
            val listOfItems = itemRepository.getAllItems()
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    items = listOfItems
                )
                if (withLoading) {
                    showLoading(false)
                }
            }
        }
    }

    suspend fun fetchWarehouses(withLoading: Boolean = true) {
        if (withLoading) {
            withContext(Dispatchers.Main) {
                showLoading(true)
            }
        }
        withContext(Dispatchers.IO) {
            val listOfWarehouses = settingsRepository.getAllWarehouses()
            withContext(Dispatchers.Main) {
                if (withLoading) {
                    showLoading(false)
                }
                state.value = state.value.copy(
                    warehouses = listOfWarehouses,
                )
            }
        }
    }

    suspend fun fetchDivisions(withLoading: Boolean = true) {
        if (withLoading) {
            withContext(Dispatchers.Main) {
                showLoading(true)
            }
        }
        withContext(Dispatchers.IO) {
            val listOfDivisions = settingsRepository.getAllDivisions()
            withContext(Dispatchers.Main) {
                if (withLoading) {
                    showLoading(false)
                }
                state.value = state.value.copy(
                    divisions = listOfDivisions,
                )
            }
        }
    }

    suspend fun fetchTransactionTypes(withLoading: Boolean = true) {
        if (withLoading) {
            withContext(Dispatchers.Main) {
                showLoading(true)
            }
        }
        withContext(Dispatchers.IO) {
            val transactionTypeList = settingsRepository.getTransactionTypes("Stock InOut")
            withContext(Dispatchers.Main) {
                if (withLoading) {
                    showLoading(false)
                }
                state.value = state.value.copy(
                    transactionTypes = transactionTypeList,
                )
            }
        }
    }

    fun save() {
        if (items.isEmpty()) {
            showWarning("Please select one item at least!")
            return
        }
        if (stockHeaderAdjState.value.stockHAWaName.isNullOrEmpty()) {
            showWarning("Please select warehouse at first!")
            return
        }
        showLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            val stockHeaderAdj = stockHeaderAdjState.value.copy()
            val succeed: Boolean
            if (stockHeaderAdj.isNew()) {
                stockHeaderAdj.prepareForInsert()
                if (stockHeaderAdj.stockHATtCode.isNullOrEmpty()) {
                    val transType =
                        state.value.transactionTypes.firstOrNull { it.transactionTypeDefault == 1 }
                    stockHeaderAdj.stockHATtCode = transType?.transactionTypeId ?: stockIOTransCode
                    stockHeaderAdj.stockHATtCodeName = transType?.transactionTypeCode
                }
                val dataModel = stockHeaderAdjustmentRepository.insert(stockHeaderAdj)
                succeed = dataModel.succeed
                if (succeed) {
                    val addedModel = dataModel.data as StockHeaderAdjustment
                    val stockHAdjList = state.value.stockHeaderAdjustmentList
                    if (stockHAdjList.isNotEmpty()) {
                        stockHAdjList.add(0, addedModel)
                    }
                    saveStockAdjustmentItems(addedModel)
                }
            } else {
                val dataModel = stockHeaderAdjustmentRepository.update(stockHeaderAdj)
                succeed = dataModel.succeed
                if (succeed) {
                    val addedModel = dataModel.data as StockHeaderAdjustment
                    val stockHAdjList = state.value.stockHeaderAdjustmentList
                    val index = stockHAdjList.indexOfFirst { it.stockHAId == addedModel.stockHAId }
                    if (index >= 0) {
                        stockHAdjList.removeAt(index)
                        stockHAdjList.add(
                            index,
                            addedModel
                        )
                    }
                    saveStockAdjustmentItems(addedModel)
                }
            }
            if (succeed) {
                withContext(Dispatchers.Main) {
                    resetState()
                    showLoading(false)
                    showWarning("data saved successfully.")
                }
            } else {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                }
            }
        }
    }

    private suspend fun saveStockAdjustmentItems(stockHeaderAdjustment: StockHeaderAdjustment) {
        val stockAdjustmentItems = items.toMutableList()
        stockAdjustmentItems.forEach {
            it.stockAdjustment.prepareForInsert()
            it.stockAdjustment.stockAdjHeaderId = stockHeaderAdjustment.stockHAId
            if (it.stockAdjustment.isNew()) {
                stockAdjustmentRepository.insert(it.stockAdjustment)
            } else {
                stockAdjustmentRepository.update(it.stockAdjustment)
            }
        }
        deletedItems.forEach {
            if (!it.stockAdjustment.isNew()) {
                stockAdjustmentRepository.delete(it.stockAdjustment)
            }
        }
    }

    fun delete() {
        showLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            val stockHeaderAdj = stockHeaderAdjState.value.copy()
            val dataModel = stockHeaderAdjustmentRepository.delete(stockHeaderAdj)
            if (dataModel.succeed) {
                val stockHAdjList = state.value.stockHeaderAdjustmentList
                val index =
                    stockHAdjList.indexOfFirst { it.stockHAId == stockHeaderAdj.stockHAId }
                if (index >= 0) {
                    stockHAdjList.removeAt(index)
                }
                val stockAdjustmentItems = items.toMutableList()
                stockAdjustmentItems.forEach {
                    stockAdjustmentRepository.delete(it.stockAdjustment)
                }
                withContext(Dispatchers.Main) {
                    resetState()
                    showLoading(false)
                    showWarning("data deleted successfully.")
                }
            } else {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                }
            }
        }
    }

    fun launchBarcodeScanner() {
        viewModelScope.launch(Dispatchers.Main) {
            if (state.value.items.isEmpty()) {
                showLoading(true)
                fetchItems(true)
            }
            sharedViewModel.launchBarcodeScanner(false,
                ArrayList(state.value.items),
                object : OnBarcodeResult {
                    override fun OnBarcodeResult(barcodesList: List<Any>) {
                        if (barcodesList.isNotEmpty()) {
                            showLoading(true)
                            viewModelScope.launch(Dispatchers.IO) {
                                val map: Map<Item, Int> =
                                    barcodesList.groupingBy { item -> item as Item }
                                        .eachCount()
                                val itemsToAdd = mutableListOf<StockAdjItemModel>()
                                var index =  items.size
                                map.forEach { (item, count) ->
                                    if (!item.itemBarcode.isNullOrEmpty()) {
                                        updateRealItemPrice(item, false)
                                        val stockAdjItem = StockAdjItemModel()
                                        stockAdjItem.setItem(item)
                                        stockAdjItem.stockAdjustment.stockAdjQty =
                                            count.toDouble()
                                        stockAdjItem.stockAdjustment.stockAdjLineNo = ++index
                                        itemsToAdd.add(stockAdjItem)
                                    }
                                }
                                withContext(Dispatchers.Main) {
                                    items.addAll(itemsToAdd)
                                    showLoading(false)
                                }
                            }
                        }
                    }
                },
                onPermissionDenied = {
                    showWarning(
                        "Permission Denied", "Settings"
                    ) {
                        sharedViewModel.openAppStorageSettings()

                    }
                })
        }
    }

}