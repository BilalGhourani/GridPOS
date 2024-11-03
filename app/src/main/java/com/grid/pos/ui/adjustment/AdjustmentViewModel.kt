package com.grid.pos.ui.adjustment

import androidx.lifecycle.viewModelScope
import com.grid.pos.data.Invoice.Invoice
import com.grid.pos.data.Invoice.InvoiceRepository
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.InvoiceHeader.InvoiceHeaderRepository
import com.grid.pos.data.Item.Item
import com.grid.pos.data.Item.ItemRepository
import com.grid.pos.model.Event
import com.grid.pos.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class AdjustmentViewModel @Inject constructor(
        private val invoiceHeaderRepository: InvoiceHeaderRepository,
        private val invoiceRepository: InvoiceRepository,
        private val itemRepository: ItemRepository
) : BaseViewModel() {
    private val _state = MutableStateFlow(AdjustmentState())
    val state: MutableStateFlow<AdjustmentState> = _state

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
            fetchItems()
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

    private suspend fun fetchInvoices(
            from: Date,
            to: Date,
            callback: (MutableList<InvoiceHeader>) -> Unit
    ) {
        val listOfInvoices = invoiceHeaderRepository.getInvoicesBetween(
            from,
            to
        )
        callback.invoke(listOfInvoices)
    }

    fun adjustRemainingQuantities(
            item: Item?
    ) {
        state.value = state.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            try {
                viewModelScope.launch(Dispatchers.IO) {
                    val invoicesMap = invoiceHeaderRepository.getAllInvoices()
                        .associateBy { it.invoiceHeadId }
                    val itemsMap = state.value.items.associateBy { it.itemId }
                    val listOfInvoices = invoiceRepository.getAllInvoicesForAdjustment(item?.itemId)
                    val itemQtyMap: MutableMap<String, Double> = mutableMapOf()
                    listOfInvoices.forEach {
                        val invoice = invoicesMap[it.invoiceHeaderId]
                        var soldQty = itemQtyMap[it.invoiceItemId!!] ?: 0.0
                        if (invoice != null) {
                            if (invoice.invoiceHeadGrossAmount >= 0) {
                                soldQty += it.invoiceQuantity
                            } else {
                                soldQty -= it.invoiceQuantity
                            }
                        }
                        itemQtyMap[it.invoiceItemId!!] = soldQty
                    }
                    val itemsToUpdate: MutableList<Item> = mutableListOf()
                    itemQtyMap.forEach { (itemId, soldQty) ->
                        val itm = itemsMap[itemId]
                        if (itm != null) {
                            itm.itemRemQty = itm.itemOpenQty - soldQty
                            itemsToUpdate.add(itm)
                        }
                    }
                    itemRepository.update(itemsToUpdate)
                    state.value = state.value.copy(
                        warning = Event("Quantity is updated successfully."),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        warning = Event("an error has occurred!"),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateItemCost(
            item: Item,
            cost: String,
            from: Date,
            to: Date,
    ) {
        val realCost = cost.toDoubleOrNull()
        if (cost.isEmpty() || realCost == null) {
            showError("Enter a valid cost!")
            return
        }
        state.value = state.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            try {
                fetchInvoices(
                    from,
                    to
                ) { invoices ->
                    viewModelScope.launch(Dispatchers.IO) {
                        val listOfInvoices = mutableListOf<Invoice>()
                        val batchSize = 30
                        var start = 0
                        val ids = invoices.map { it.invoiceHeadId }
                        val size = ids.size
                        while (start < size) {
                            val end = min(
                                start + batchSize,
                                size
                            )
                            val idsBatch = ids.subList(
                                start,
                                end
                            )
                            listOfInvoices.addAll(
                                invoiceRepository.getInvoicesByIds(
                                    idsBatch,
                                    item.itemId
                                )
                            )
                            start = end + 1
                        }
                        listOfInvoices.forEach {
                            it.invoiceCost = realCost
                        }
                        invoiceRepository.update(listOfInvoices)
                        state.value = state.value.copy(
                            warning = Event("Item cost is updated successfully."),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        warning = Event("an error has occurred!"),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun showError(message: String) {
        state.value = state.value.copy(
            warning = Event(message),
            isLoading = false
        )
    }

}