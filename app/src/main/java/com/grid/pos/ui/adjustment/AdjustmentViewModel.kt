package com.grid.pos.ui.adjustment

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.grid.pos.SharedViewModel
import com.grid.pos.data.invoice.InvoiceRepository
import com.grid.pos.data.invoiceHeader.InvoiceHeader
import com.grid.pos.data.invoiceHeader.InvoiceHeaderRepository
import com.grid.pos.data.item.Item
import com.grid.pos.data.item.ItemRepository
import com.grid.pos.interfaces.OnBarcodeResult
import com.grid.pos.model.PopupModel
import com.grid.pos.ui.common.BaseViewModel
import com.grid.pos.utils.DateHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class AdjustmentViewModel @Inject constructor(
    private val invoiceHeaderRepository: InvoiceHeaderRepository,
    private val invoiceRepository: InvoiceRepository,
    private val itemRepository: ItemRepository,
    private val sharedViewModel: SharedViewModel
) : BaseViewModel(sharedViewModel) {
     val state = mutableStateOf(AdjustmentState())
    val dateFormat = "yyyy-MM-dd HH:mm"

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
            fetchItems()
        }
        updateState(
            state.value.copy(
                fromDateString = DateHelper.getDateInFormat(
                    DateHelper.editDate(Date(), 0, 0, 0),
                    dateFormat
                ),
                toDateString = DateHelper.getDateInFormat(
                    DateHelper.editDate(Date(), 23, 59, 59),
                    dateFormat
                )
            )
        )
    }

    fun updateState(newState: AdjustmentState) {
        state.value = newState
    }

    fun checkAndBack(callback: () -> Unit) {
        if (isLoading()) {
            showPopup(PopupModel().apply {
                onDismissRequest = {

                }
                onConfirmation = {
                    showLoading(false)
                    viewModelScope.cancel()
                    callback.invoke()
                }
                dialogText = "Are you sure you want to close?"
                positiveBtnText = "Cancel"
                negativeBtnText = "Close"
            })
        } else {
            callback.invoke()
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

    fun adjustRemainingQuantities() {
        if (state.value.selectedItem == null) {
            showWarning("select an Item at first!")
            return
        }
        val item = state.value.selectedItem
        showLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                viewModelScope.launch(Dispatchers.IO) {
                    val listOfInvoices = invoiceRepository.getAllInvoicesForAdjustment(
                        item?.itemId
                    )
                    val invoiceIds = listOfInvoices.map { it.invoiceHeaderId!! }

                    val listOfInvoiceHeaders = mutableListOf<InvoiceHeader>()
                    val batchSize = 30
                    var start = 0
                    val size = invoiceIds.size
                    while (start < size) {
                        val to = min(
                            start + batchSize,
                            size
                        )
                        val idsBatch = invoiceIds.subList(
                            start,
                            to
                        )
                        listOfInvoiceHeaders.addAll(
                            invoiceHeaderRepository.getAllInvoicesByIds(
                                idsBatch
                            )
                        )
                        start = to + 1
                    }

                    val invoicesMap = listOfInvoiceHeaders.associateBy { it.invoiceHeadId }
                    val itemsMap = state.value.items.associateBy { it.itemId }
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
                    withContext(Dispatchers.Main) {
                        resetState()
                        showLoading(false)
                        showWarning("Quantity is updated successfully.")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showWarning("an error has occurred!")
                }
            }
        }
    }

    fun updateItemCost() {
        if (state.value.selectedItem == null) {
            showWarning("select an Item at first!")
            return
        }
        val cost = state.value.itemCostString
        val item = state.value.selectedItem!!
        val from =
            DateHelper.getDateFromString(state.value.fromDateString, dateFormat)
        val to =
            DateHelper.getDateFromString(state.value.toDateString, dateFormat)
        val realCost = cost.toDoubleOrNull()
        if (cost.isEmpty() || realCost == null) {
            showWarning("Enter a valid cost!")
            return
        }
        showLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val listOfInvoices = invoiceRepository.getAllInvoicesForAdjustment(
                    item.itemId,
                    from,
                    to
                )
                listOfInvoices.forEach {
                    it.invoiceCost = realCost
                }
                invoiceRepository.update(listOfInvoices)
                withContext(Dispatchers.Main) {
                    resetState()
                    showLoading(false)
                    showWarning("Item cost is updated successfully.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showWarning("an error has occurred!")
                }
            }
        }
    }

    fun resetState() {
        state.value = state.value.copy(
            selectedItem = null,
            barcodeSearchedKey = "",
            itemCostString = "",
            fromDateString = DateHelper.getDateInFormat(
                DateHelper.editDate(Date(), 0, 0, 0),
                dateFormat
            ),
            toDateString = DateHelper.getDateInFormat(
                DateHelper.editDate(Date(), 23, 59, 59),
                dateFormat
            )
        )
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
                                        updateState(
                                            state.value.copy(
                                                selectedItem = item
                                            )
                                        )
                                    } else {
                                        updateState(
                                            state.value.copy(
                                                barcodeSearchedKey = resp
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