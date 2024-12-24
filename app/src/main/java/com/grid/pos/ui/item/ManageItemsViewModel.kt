package com.grid.pos.ui.item

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.currency.CurrencyRepository
import com.grid.pos.data.family.Family
import com.grid.pos.data.family.FamilyRepository
import com.grid.pos.data.invoice.InvoiceRepository
import com.grid.pos.data.item.Item
import com.grid.pos.data.item.ItemRepository
import com.grid.pos.data.posPrinter.PosPrinter
import com.grid.pos.data.posPrinter.PosPrinterRepository
import com.grid.pos.model.CurrencyModel
import com.grid.pos.model.Event
import com.grid.pos.model.ItemGroupModel
import com.grid.pos.model.ReportResult
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.BaseViewModel
import com.grid.pos.utils.PrinterUtils
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
            val dataModel = itemRepository.getAllItems()
            if (dataModel.succeed) {
                val listOfItems = convertToMutableList(
                    dataModel.data,
                    Item::class.java
                )
                withContext(Dispatchers.Main) {
                    manageItemsState.value = manageItemsState.value.copy(
                        items = listOfItems,
                        isLoading = false
                    )
                }
            } else if (dataModel.message != null) {
                showWarning(dataModel.message)
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
            val dataModel = familyRepository.getAllFamilies()
            if (dataModel.succeed) {
                val listOfFamilies = convertToMutableList(
                    dataModel.data,
                    Family::class.java
                )
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
            } else if (dataModel.message != null) {
                showWarning(dataModel.message)
            }
        }
    }

    private suspend fun fetchCurrencies() {
        val dataModel = currencyRepository.getAllCurrencyModels()
        if (dataModel.succeed) {
            val currencies = convertToMutableList(
                dataModel.data,
                CurrencyModel::class.java
            )
            withContext(Dispatchers.Main) {
                manageItemsState.value = manageItemsState.value.copy(
                    currencies = currencies,
                    isLoading = false
                )
            }
        } else if (dataModel.message != null) {
            showWarning(dataModel.message)
        }
    }

    fun fetchPrinters(loading: Boolean = true) {
        if (loading) {
            manageItemsState.value = manageItemsState.value.copy(
                isLoading = true
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val dataModel = posPrinterRepository.getAllPosPrinters()
            if (dataModel.succeed) {
                val listOfPrinters = convertToMutableList(
                    dataModel.data,
                    PosPrinter::class.java
                )
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
            } else if (dataModel.message != null) {
                showWarning(dataModel.message)
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
        withContext(Dispatchers.Main) {
            manageItemsState.value = manageItemsState.value.copy(
                groups = groups,
                isConnectingToSQLServer = isConnectedToSQL
            )
        }
    }

    fun showWarning(
            warning: String,
            action: String? = null
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
                val dataModel = itemRepository.insert(item)
                if (dataModel.succeed) {
                    val addedModel = dataModel.data as Item
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
                } else if (dataModel.message != null) {
                    showWarning(dataModel.message)
                }
            } else {
                val dataModel = itemRepository.update(item)
                if (dataModel.succeed) {
                    val index = manageItemsState.value.items.indexOfFirst { it.itemId == item.itemId }
                    if (index >= 0) {
                        manageItemsState.value.items.removeAt(index)
                        manageItemsState.value.items.add(
                            index,
                            item
                        )
                    }
                    withContext(Dispatchers.Main) {
                        manageItemsState.value = manageItemsState.value.copy(
                            selectedItem = item,
                            isLoading = false,
                            warning = Event("Item saved successfully."),
                            clear = true
                        )
                    }
                } else if (dataModel.message != null) {
                    showWarning(dataModel.message)
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
            val dataModel = itemRepository.delete(item)
            if (dataModel.succeed) {
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
            } else if (dataModel.message != null) {
                showWarning(dataModel.message)
            }
        }
    }

    suspend fun generateBarcode(): String {
        val dataModel = itemRepository.generateBarcode()
        if (dataModel.succeed) {
            return dataModel.data as String
        } else if (dataModel.message != null) {
            showWarning(dataModel.message)
        }
        return ""
    }

    fun prepareItemBarcodeReport(
            context: Context,
            item: Item
    ): ReportResult {
        val reportResult = PrinterUtils.getItemBarcodeHtmlContent(
            context,
            item
        )
        SettingsModel.cashPrinter?.let {
            if (it.contains(":")) {
                val printerDetails = it.split(":")
                val size = printerDetails.size
                reportResult.printerIP = if (size > 0) printerDetails[0] else ""
                val port = if (size > 1) printerDetails[1] else "-1"
                reportResult.printerPort = port.toIntOrNull() ?: -1
            } else {
                reportResult.printerName = it
            }
        }
        return reportResult
    }

    private suspend fun hasRelations(itemId: String): Boolean {
        return invoiceRepository.getOneInvoiceByItemID(itemId) != null
    }
}