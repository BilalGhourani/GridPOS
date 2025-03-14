package com.grid.pos.ui.stockInOut

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.grid.pos.SharedViewModel
import com.grid.pos.data.item.Item
import com.grid.pos.data.item.ItemRepository
import com.grid.pos.data.settings.SettingsRepository
import com.grid.pos.data.stockHeaderInOut.StockHeaderInOut
import com.grid.pos.data.stockInOut.StockInOutRepository
import com.grid.pos.data.stockHeaderInOut.StockHeaderInOutRepository
import com.grid.pos.interfaces.OnBarcodeResult
import com.grid.pos.model.StockInOutItemModel
import com.grid.pos.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class StockInOutViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val stockHeaderInOutRepository: StockHeaderInOutRepository,
    private val stockInOutRepository: StockInOutRepository,
    private val settingsRepository: SettingsRepository,
    private val sharedViewModel: SharedViewModel
) : BaseViewModel(sharedViewModel) {

    val state = mutableStateOf(StockInOutState())

    private var stockIOTransCode: String? = null
    var selectedItemIndex: Int = 0

    var stockHeaderInOutState = mutableStateOf(StockHeaderInOut())
    val items = mutableStateListOf<StockInOutItemModel>()
    val deletedItems: MutableList<StockInOutItemModel> = mutableListOf()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
            stockIOTransCode = settingsRepository.getTransactionTypeId("Stock InOut")
        }
    }

    fun updateStockHeaderInOut(stockHeaderInOut: StockHeaderInOut) {
        stockHeaderInOutState.value = stockHeaderInOut
    }

    fun resetState() {
        items.clear()
        deletedItems.clear()
        updateStockHeaderInOut(StockHeaderInOut())
    }

    suspend fun updateRealItemPrice(item: Item, withLoading: Boolean = true) {
        sharedViewModel.updateRealItemPrice(item, withLoading)
    }

    fun fetchTransfers() {
        showLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            val listOfTransfer = stockHeaderInOutRepository.getAllStockHeaderInOuts()
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    stockHeaderInOutList = listOfTransfer
                )
                showLoading(false)
            }
        }
    }

    fun loadTransferDetails(
        stockHeaderInOut: StockHeaderInOut
    ) {
        showLoading(true)
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
        if (stockHeaderInOutState.value.stockHeadInOutWaName.isNullOrEmpty()) {
            showWarning("Please select from warehouse at first!")
            return
        }
        if (stockHeaderInOutState.value.stockHeadInOutWaTpName.isNullOrEmpty()) {
            showWarning("Please select to warehouse at first!")
            return
        }
        if (stockHeaderInOutState.value.stockHeadInOutWaTpName == stockHeaderInOutState.value.stockHeadInOutWaName) {
            showWarning("Please select two different warehouses!")
            return
        }
        showLoading(true)
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

    private suspend fun saveStockInOutItems(stockHInOut: StockHeaderInOut) {
        val stockInOutItems = items.toMutableList()
        stockInOutItems.forEachIndexed { index, model ->
            model.stockInOut.prepareForInsert()
            model.stockInOut.stockInOutHeaderId = stockHInOut.stockHeadInOutId
            model.stockInOut.stockInOutWaTpName = stockHInOut.stockHeadInOutWaTpName
            model.stockInOut.stockInOutLineNo = index + 1
            if (model.stockInOut.isNew()) {
                stockInOutRepository.insert(model.stockInOut)
            } else {
                stockInOutRepository.update(model.stockInOut)
            }
        }
        deletedItems.forEach {
            if (!it.stockInOut.isNew()) {
                stockInOutRepository.delete(it.stockInOut)
            }
        }
    }

    fun delete() {
        showLoading(true)
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
                                val itemsToAdd = mutableListOf<StockInOutItemModel>()
                                map.forEach { (item, count) ->
                                    if (!item.itemBarcode.isNullOrEmpty()) {
                                        updateRealItemPrice(item, false)
                                        val stockInOutItem = StockInOutItemModel()
                                        stockInOutItem.setItem(item)
                                        stockInOutItem.stockInOut.stockInOutQty =
                                            count.toDouble()
                                        itemsToAdd.add(stockInOutItem)
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