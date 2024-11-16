package com.grid.pos.ui.item

import androidx.lifecycle.viewModelScope
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.Currency.CurrencyRepository
import com.grid.pos.data.DataModel
import com.grid.pos.data.Family.Family
import com.grid.pos.data.Family.FamilyRepository
import com.grid.pos.data.Invoice.InvoiceRepository
import com.grid.pos.data.Item.Item
import com.grid.pos.data.Item.ItemRepository
import com.grid.pos.data.PosPrinter.PosPrinterRepository
import com.grid.pos.model.CurrencyModel
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
class ManageItemsViewModel @Inject constructor(
        private val itemRepository: ItemRepository,
        private val familyRepository: FamilyRepository,
        private val posPrinterRepository: PosPrinterRepository,
        private val currencyRepository: CurrencyRepository,
        private val invoiceRepository: InvoiceRepository
) : BaseViewModel() {

    private val _manageItemsState = MutableStateFlow(ManageItemsState())
    val manageItemsState: MutableStateFlow<ManageItemsState> = _manageItemsState
    var currentITem: Item = Item()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
            fetchFamilies()
            fetchPrinters()
        }
    }

    fun fillCachedItems(
            items: MutableList<Item> = mutableListOf(),
            families: MutableList<Family> = mutableListOf()
    ) {
        if (true) return
        if (manageItemsState.value.items.isEmpty()) {
            manageItemsState.value = manageItemsState.value.copy(
                items = items.toMutableList()
            )
        }
        if (manageItemsState.value.families.isEmpty()) {
            manageItemsState.value = manageItemsState.value.copy(
                families = families
            )
        }
    }

    fun fetchItems() {
        manageItemsState.value = manageItemsState.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            if (SettingsModel.currentCurrency == null && SettingsModel.isConnectedToSqlServer()) {
                val currencies = currencyRepository.getAllCurrencies()
                val currency = if (currencies.size > 0) currencies[0] else Currency()
                SettingsModel.currentCurrency = currency
            }
            val listOfItems = itemRepository.getAllItems()
            withContext(Dispatchers.Main) {
                manageItemsState.value = manageItemsState.value.copy(
                    items = listOfItems,
                    isLoading = false
                )
            }
        }
    }

    private suspend fun fetchFamilies() {
        val listOfFamilies = familyRepository.getAllFamilies()
        withContext(Dispatchers.Main) {
            manageItemsState.value = manageItemsState.value.copy(
                families = listOfFamilies
            )
        }
    }

    fun fetchCurrencies() {
        manageItemsState.value = manageItemsState.value.copy(
            warning = null,
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val currencies = currencyRepository.getAllCurrencyModels()
            withContext(Dispatchers.Main) {
                manageItemsState.value = manageItemsState.value.copy(
                    currencies = currencies,
                    isLoading = false
                )
            }
        }
    }

    private suspend fun fetchPrinters() {
        val listOfPrinters = posPrinterRepository.getAllPosPrinters()
        withContext(Dispatchers.Main) {
            manageItemsState.value = manageItemsState.value.copy(
                printers = listOfPrinters
            )
        }
    }

    fun showWarning(
            warning: String,
            action: String
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            manageItemsState.value = manageItemsState.value.copy(
                warning = Event(warning),
                actionLabel = action,
                isLoading = false
            )
        }
    }

    fun saveItem(item: Item) {
        if (item.itemName.isNullOrEmpty() || item.itemFaId.isNullOrEmpty()) {
            manageItemsState.value = manageItemsState.value.copy(
                warning = Event("Please fill item name and family"),
                isLoading = false
            )
            return
        }
        manageItemsState.value = manageItemsState.value.copy(
            isLoading = true
        )
        val isInserting = item.isNew()

        CoroutineScope(Dispatchers.IO).launch {
            if (isInserting) {
                item.prepareForInsert()
                val addedModel = itemRepository.insert(item)
                val items = manageItemsState.value.items
                if (items.isNotEmpty()) {
                    items.add(addedModel)
                }
                withContext(Dispatchers.Main) {
                    manageItemsState.value = manageItemsState.value.copy(
                        items = items,
                        selectedItem = addedModel,
                        isLoading = false,
                        warning = Event("Item saved successfully."),
                        clear = true
                    )
                }
            } else {
                itemRepository.update(item)
                withContext(Dispatchers.Main) {
                    manageItemsState.value = manageItemsState.value.copy(
                        selectedItem = item,
                        isLoading = false,
                        warning = Event("Item saved successfully."),
                        clear = true
                    )
                }
            }
        }
    }

    fun deleteSelectedItem(item: Item) {
        if (item.itemId.isEmpty()) {
            manageItemsState.value = manageItemsState.value.copy(
                warning = Event("Please select an Item to delete"),
                isLoading = false
            )
            return
        }
        manageItemsState.value = manageItemsState.value.copy(
            warning = null,
            isLoading = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            if (hasRelations(item.itemId)) {
                withContext(Dispatchers.Main) {
                    manageItemsState.value = manageItemsState.value.copy(
                        warning = Event("You can't delete this Item,because it has related data!"),
                        isLoading = false
                    )
                }
                return@launch
            }
            itemRepository.delete(item)
            val items = manageItemsState.value.items
            items.remove(item)
            withContext(Dispatchers.Main) {
                manageItemsState.value = manageItemsState.value.copy(
                    items = items,
                    selectedItem = Item(),
                    isLoading = false,
                    warning = Event("successfully deleted."),
                    clear = true
                )
            }
        }
    }

    private suspend fun hasRelations(itemId: String): Boolean {
        return invoiceRepository.getOneInvoiceByItemID(itemId) != null
    }
}