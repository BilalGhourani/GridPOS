package com.grid.pos.ui.item.opening

import androidx.lifecycle.viewModelScope
import com.grid.pos.data.item.Item
import com.grid.pos.data.item.ItemRepository
import com.grid.pos.data.settings.SettingsRepository
import com.grid.pos.model.Event
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
        private val settingsRepository: SettingsRepository
) : BaseViewModel() {

    private val _state = MutableStateFlow(ItemOpeningState())
    val state: MutableStateFlow<ItemOpeningState> = _state

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
        }
    }

    fun resetState() {
        state.value = state.value.copy(
            warning = null,
            isLoading = false,
            clearCosts = false,
            clearWarehouseDetails = false
        )
    }

    fun getSelectedCurrencyIndex(currencyId: String?): Int {
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
        state.value = state.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val listOfItems = itemRepository.getAllItems()
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    items = listOfItems,
                    isLoading = false
                )
            }
        }
    }

    fun fetchWarehouses() {
        state.value = state.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val listOfWarehouses = settingsRepository.getAllWarehouses()
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    warehouses = listOfWarehouses,
                    isLoading = false
                )

            }
        }
    }

    fun showWarning(
            warning: String,
            action: String? = null
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            state.value = state.value.copy(
                warning = Event(warning),
                actionLabel = action,
                isLoading = false
            )
        }
    }

    fun saveItemCosts(
            item: Item?,
            cost: String,
            costFirst: String,
            costSecond: String
    ) {
        if (item == null) {
            state.value = state.value.copy(
                warning = Event("Please select an item at first."),
                isLoading = false
            )
            return
        }
        state.value = state.value.copy(
            isLoading = true
        )

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
                    state.value = state.value.copy(
                        //selectedItem = null,
                        isLoading = false,
                        warning = Event("item cost saved successfully."),
                        clearCosts = true
                    )
                }
            } else if (dataModel.message != null) {
                showWarning(dataModel.message)
            }
        }
    }

    fun saveItemWarehouse(
            item: Item?,
            warehouseId: String,
            location: String,
            openQty: String
    ) {
        if (item == null) {
            state.value = state.value.copy(
                warning = Event("Please select an item at first."),
                isLoading = false
            )
            return
        }

        if (warehouseId.isEmpty()) {
            state.value = state.value.copy(
                warning = Event("Please select a warehouse at first."),
                isLoading = false
            )
            return
        }

        state.value = state.value.copy(
            isLoading = true
        )

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
                    state.value = state.value.copy(
                        //selectedItem = null,
                        isLoading = false,
                        warning = Event("Warehouse details saved successfully."),
                        clearWarehouseDetails = true
                    )
                }
            } else if (dataModel.message != null) {
                showWarning(dataModel.message)
            }
        }
    }

}