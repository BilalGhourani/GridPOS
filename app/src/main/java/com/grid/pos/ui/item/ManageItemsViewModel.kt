package com.grid.pos.ui.item

import androidx.lifecycle.viewModelScope
import com.grid.pos.data.currency.CurrencyRepository
import com.grid.pos.data.family.FamilyRepository
import com.grid.pos.data.invoice.InvoiceRepository
import com.grid.pos.data.item.Item
import com.grid.pos.data.item.ItemRepository
import com.grid.pos.data.posPrinter.PosPrinterRepository
import com.grid.pos.model.Event
import com.grid.pos.model.ItemGroupModel
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
            fetchCurrencies()
            fillGroups()
        }
    }

    fun resetState() {
        manageItemsState.value = manageItemsState.value.copy(
            warning = null,
            isLoading = false,
            clear = false
        )
    }

    fun fetchItems() {
        manageItemsState.value = manageItemsState.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val listOfItems = itemRepository.getAllItems()
            withContext(Dispatchers.Main) {
                manageItemsState.value = manageItemsState.value.copy(
                    items = listOfItems,
                    isLoading = false
                )
            }
        }
    }

    fun fetchFamilies(loading: Boolean = true) {
        if (loading) {
            manageItemsState.value = manageItemsState.value.copy(
                isLoading = true
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val listOfFamilies = familyRepository.getAllFamilies()
            withContext(Dispatchers.Main) {
                manageItemsState.value = if (loading) {
                    manageItemsState.value.copy(
                        families = listOfFamilies,
                        isLoading = false
                    )
                } else {
                    manageItemsState.value.copy(
                        families = listOfFamilies
                    )
                }

            }
        }
    }

    private suspend fun fetchCurrencies() {
        val currencies = currencyRepository.getAllCurrencyModels()
        withContext(Dispatchers.Main) {
            manageItemsState.value = manageItemsState.value.copy(
                currencies = currencies,
                isLoading = false
            )
        }
    }

    fun fetchPrinters(loading: Boolean = true) {
        if (loading) {
            manageItemsState.value = manageItemsState.value.copy(
                isLoading = true
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val listOfPrinters = posPrinterRepository.getAllPosPrinters()
            withContext(Dispatchers.Main) {
                manageItemsState.value = if (loading) {
                    manageItemsState.value.copy(
                        printers = listOfPrinters,
                        isLoading = false
                    )
                } else {
                    manageItemsState.value.copy(
                        printers = listOfPrinters
                    )
                }
            }
        }
    }

    private suspend fun fillGroups() {
        val isConnectedToSQL = SettingsModel.isConnectedToSqlServer()
        val groups = mutableListOf(
            ItemGroupModel("Stock"),
            ItemGroupModel("Mixed Product"),
            ItemGroupModel("Finished Product"),
            ItemGroupModel("Pack"),
            ItemGroupModel("Set"),
            ItemGroupModel("Non Stock"),
        )
        val listOfPrinters = posPrinterRepository.getAllPosPrinters()
        withContext(Dispatchers.Main) {
            manageItemsState.value = manageItemsState.value.copy(
                groups = groups,
                isConnectingToSQLServer = isConnectedToSQL
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
        if (manageItemsState.value.isConnectingToSQLServer && item.itemGroup.isNullOrEmpty()) {
            manageItemsState.value = manageItemsState.value.copy(
                warning = Event("Please select an item group"),
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

        viewModelScope.launch(Dispatchers.IO) {
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

    suspend fun generateBarcode(): String? {
        return itemRepository.generateBarcode()
    }

    private suspend fun hasRelations(itemId: String): Boolean {
        return invoiceRepository.getOneInvoiceByItemID(itemId) != null
    }
}