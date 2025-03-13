package com.grid.pos.ui.stockAdjustment

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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

    val state = mutableStateOf(StockAdjustmentState())

    var isStockAdjustment:Boolean = true
    private var stockIOTransCode: String? = null
    var selectedItemIndex: Int = 0

    var stockHeaderAdjState = mutableStateOf(StockHeaderAdjustment())
    val items = mutableStateListOf<StockAdjItemModel>()
    val deletedItems: MutableList<StockAdjItemModel> = mutableListOf()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
            stockIOTransCode = settingsRepository.getTransactionTypeId("Stock Adjustment")
        }
    }

    fun updateStockHeaderAdjustment(stockHeaderAdjustment: StockHeaderAdjustment) {
        stockHeaderAdjState.value = stockHeaderAdjustment
    }

    fun resetState() {
        items.clear()
        deletedItems.clear()
        updateStockHeaderAdjustment(StockHeaderAdjustment())
    }

    suspend fun updateRealItemPrice(item: Item, withLoading: Boolean = true) {
        sharedViewModel.updateRealItemPrice(item, withLoading)
    }

    fun fetchTransfers(source: String) {
        showLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            val listOfAdjustments =
                stockHeaderAdjustmentRepository.getAllStockHeaderAdjustments(source)
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
            val transactionTypeList = settingsRepository.getTransactionTypes("Stock Adjustment")
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

    fun save(source: String) {
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
                stockHeaderAdj.stockHASource = source
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
                    saveStockAdjustmentItems(addedModel, source)
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
                    saveStockAdjustmentItems(addedModel, source)
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

    private suspend fun saveStockAdjustmentItems(stockHeaderAdjustment: StockHeaderAdjustment,source: String) {
        val stockAdjustmentItems = items.toMutableList()
        stockAdjustmentItems.forEachIndexed { index, model ->
            model.stockAdjustment.stockAdjHeaderId = stockHeaderAdjustment.stockHAId
            model.stockAdjustment.stockAdjWaName = stockHeaderAdjustment.stockHAWaName
            model.stockAdjustment.stockAdjLineNo = index + 1
            if (model.stockAdjustment.isNew()) {
                model.stockAdjustment.prepareForInsert()
                stockAdjustmentRepository.insert(model.stockAdjustment,source)
            } else {
                stockAdjustmentRepository.update(model.stockAdjustment,source)
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

    fun launchBarcodeScanner(source: String) {
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
                                map.forEach { (item, count) ->
                                    if (!item.itemBarcode.isNullOrEmpty()) {
                                        updateRealItemPrice(item, false)
                                        val stockAdjItem = StockAdjItemModel()
                                        stockAdjItem.setItem(item, source)
                                        stockAdjItem.stockAdjustment.stockAdjQty =
                                            count.toDouble()
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