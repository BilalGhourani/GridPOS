package com.grid.pos.ui.item.opening

import androidx.lifecycle.viewModelScope
import com.grid.pos.SharedViewModel
import com.grid.pos.data.item.Item
import com.grid.pos.data.item.ItemRepository
import com.grid.pos.data.settings.SettingsRepository
import com.grid.pos.interfaces.OnBarcodeResult
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ItemOpeningViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val settingsRepository: SettingsRepository,
    private val sharedViewModel: SharedViewModel
) : BaseViewModel(sharedViewModel) {

    private val _state = MutableStateFlow(ItemOpeningState())
    val state: MutableStateFlow<ItemOpeningState> = _state

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
        }
    }

    fun resetState() {
        state.value = state.value.copy(
            selectedItem = null,
            barcodeSearchState = "",

            warehouseState = "",
            locationState = "",
            openQtyState = "",

            currencyIndexState = 0,
            costState = "",
            costFirstState = "",
            costSecondState = ""
        )
    }

    fun updateState(newState: ItemOpeningState) {
        state.value = newState
    }

    fun selectItem(item: Item) {
        if (state.value.warehouses.isEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                fetchWarehouses()
                withContext(Dispatchers.Main) {
                    selectItemNow(item)
                }
            }
        } else {
            selectItemNow(item)
        }
    }

    private fun selectItemNow(item: Item) {
        updateState(state.value.copy(
            selectedItem = item,
            warehouseState = item.itemWarehouse ?: "",
            locationState = item.itemLocation ?: "",
            openQtyState = (item.itemOpenQty.takeIf { it > 0.0 } ?: "").toString(),

            currencyIndexState = getSelectedCurrencyIndex(item.itemCurrencyId),
            costState = (item.itemOpenCost.takeIf { it > 0.0 } ?: "").toString(),
            costFirstState = item.itemCostFirst?.toString() ?: "",
            costSecondState = item.itemCostSecond?.toString() ?: ""
        ))
    }

    private fun getSelectedCurrencyIndex(currencyId: String?): Int {
        if (currencyId.isNullOrEmpty()) return 0
        return when (currencyId) {
            SettingsModel.currentCurrency?.currencyId, SettingsModel.currentCurrency?.currencyCode1 -> {
                1
            }

            SettingsModel.currentCurrency?.currencyDocumentId, SettingsModel.currentCurrency?.currencyCode2 -> {
                2
            }

            else -> {
                0
            }
        }
    }

    fun fetchItems() {
        showLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            val listOfItems = itemRepository.getAllItems()
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    items = listOfItems
                )
                showLoading(false)
            }
        }
    }

    fun fetchWarehouses() {
        showLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            val listOfWarehouses = settingsRepository.getAllWarehouses()
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    warehouses = listOfWarehouses
                )
                showLoading(false)
            }
        }
    }

    fun saveItemCosts() {
        val item = state.value.selectedItem
        val cost = state.value.costState
        val costFirst = state.value.costFirstState
        val costSecond = state.value.costSecondState
        if (item == null) {
            showWarning("Please select an item at first.")
            return
        }
        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            if (cost.isNotEmpty()) {
                item.itemOpenCost = cost.toDoubleOrNull() ?: 0.0
            }
            if (cost.isNotEmpty()) {
                item.itemCostFirst = costFirst.toDoubleOrNull() ?: 0.0
            }
            if (cost.isNotEmpty()) {
                item.itemCostSecond = costSecond.toDoubleOrNull() ?: 0.0
            }
            val dataModel = itemRepository.updateOpening(
                item
            )
            if (dataModel.succeed) {
                withContext(Dispatchers.Main) {
                    /*state.value = state.value.copy(
                        costState = "",
                        costFirstState = "",
                        costSecondState = ""
                    )*/
                    showLoading(false)
                    showWarning("item cost saved successfully.")
                }
            } else {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                }
            }
        }
    }

    fun saveItemWarehouse() {
        val item = state.value.selectedItem
        val warehouseId = state.value.warehouseState
        val location = state.value.locationState
        val openQty = state.value.openQtyState
        if (item == null) {
            showWarning("Please select an item at first.")
            return
        }

        if (warehouseId.isEmpty()) {
            showWarning("Please select a warehouse at first.")
            return
        }

        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            item.itemWarehouse = warehouseId
            item.itemLocation = location.ifEmpty { null }
            if (openQty.isNotEmpty()) {
                item.itemOpenQty = openQty.toDoubleOrNull() ?: item.itemOpenQty
            }
            val dataModel = itemRepository.updateWarehouseData(
                item
            )
            if (dataModel.succeed) {
                withContext(Dispatchers.Main) {
//                    state.value = state.value.copy(
//                        warehouseState = "",
//                        locationState = "",
//                        openQtyState = ""
//                    )
                    showLoading(false)
                    showWarning("Warehouse details saved successfully.")
                }
            } else {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                }
            }
        }
    }

    fun launchBarcodeScanner(callback: () -> Unit) {
        sharedViewModel.launchBarcodeScanner(true,
            null,
            object : OnBarcodeResult {
                override fun OnBarcodeResult(barcodesList: List<Any>) {
                    if (barcodesList.isNotEmpty()) {
                        val resp = barcodesList[0]
                        if (resp is String) {
                            viewModelScope.launch(Dispatchers.Default) {
                                val item = state.value.items.firstOrNull { iterator ->
                                    iterator.itemBarcode.equals(
                                        resp,
                                        ignoreCase = true
                                    )
                                }
                                withContext(Dispatchers.Main) {
                                    if (item != null) {
                                        callback.invoke()
                                        selectItem(item)
                                    } else {
                                        updateState(
                                            state.value.copy(
                                                barcodeSearchState = resp
                                            )
                                        )
                                    }
                                }
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