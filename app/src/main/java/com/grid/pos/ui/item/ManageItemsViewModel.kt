package com.grid.pos.ui.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.Family.Family
import com.grid.pos.data.Family.FamilyRepository
import com.grid.pos.data.Invoice.InvoiceRepository
import com.grid.pos.data.Item.Item
import com.grid.pos.data.Item.ItemRepository
import com.grid.pos.data.PosPrinter.PosPrinterRepository
import com.grid.pos.model.Event
import com.grid.pos.model.SettingsModel
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
        private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val _manageItemsState = MutableStateFlow(ManageItemsState())
    val manageItemsState: MutableStateFlow<ManageItemsState> = _manageItemsState
    var currentITem: Item? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            fetchItems()
            fetchFamilies()
            fetchPrinters()
        }
    }

    fun fillCachedItems(
            items: MutableList<Item> = mutableListOf(),
            families: MutableList<Family> = mutableListOf()
    ) {
        if (manageItemsState.value.items.isEmpty()) {
            viewModelScope.launch(Dispatchers.Main) {
                manageItemsState.value = manageItemsState.value.copy(
                    items = items,
                    families = families
                )
            }
        }
    }

    private suspend fun fetchItems() {
        val listOfItems = itemRepository.getAllItems()
        viewModelScope.launch(Dispatchers.Main) {
            manageItemsState.value = manageItemsState.value.copy(
                items = listOfItems
            )
        }
    }

    private suspend fun fetchFamilies() {
        val listOfFamilies = familyRepository.getAllFamilies()
        viewModelScope.launch(Dispatchers.Main) {
            manageItemsState.value = manageItemsState.value.copy(
                families = listOfFamilies
            )
        }
    }

    private suspend fun fetchPrinters() {
        val listOfPrinters = posPrinterRepository.getAllPosPrinters()
        viewModelScope.launch(Dispatchers.Main) {
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
                items.add(addedModel)
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
            viewModelScope.launch(Dispatchers.Main) {
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
        if (invoiceRepository.getOneInvoiceByItemID(itemId) != null) return true

        return false
    }
}