package com.grid.pos.ui.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.Family.Family
import com.grid.pos.data.Family.FamilyRepository
import com.grid.pos.data.Invoice.Invoice
import com.grid.pos.data.Invoice.InvoiceRepository
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.InvoiceHeader.InvoiceHeaderRepository
import com.grid.pos.data.Item.Item
import com.grid.pos.data.Item.ItemRepository
import com.grid.pos.data.PosPrinter.PosPrinterRepository
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.data.ThirdParty.ThirdPartyRepository
import com.grid.pos.interfaces.OnResult
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class POSViewModel @Inject constructor(
    private val invoiceHeaderRepository: InvoiceHeaderRepository,
    private val invoiceRepository: InvoiceRepository,
    private val itemRepository: ItemRepository,
    private val thirdPartyRepository: ThirdPartyRepository,
    private val familyRepository: FamilyRepository
) : ViewModel() {

    private val _posState = MutableStateFlow(POSState())
    val posState: MutableStateFlow<POSState> = _posState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            fetchItems()
            fetchThirdParties()
            fetchFamilies()
        }
    }

    private suspend fun fetchItems() {
        itemRepository.getAllItems(object : OnResult {
            override fun onSuccess(result: Any) {
                val listOfItems = mutableListOf<Item>()
                (result as List<Item>).forEach {
                    listOfItems.add(it)
                }
                viewModelScope.launch(Dispatchers.Main) {
                    posState.value = posState.value.copy(
                        items = listOfItems
                    )
                }
            }

            override fun onFailure(message: String, errorCode: Int) {

            }

        })
    }

    private suspend fun fetchThirdParties() {
        thirdPartyRepository.getAllThirdParties(object : OnResult {
            override fun onSuccess(result: Any) {
                val listOfThirdParties = mutableListOf<ThirdParty>()
                (result as List<ThirdParty>).forEach {
                    listOfThirdParties.add(it)
                }
                viewModelScope.launch(Dispatchers.Main) {
                    posState.value = posState.value.copy(
                        thirdParties = listOfThirdParties
                    )
                }
            }

            override fun onFailure(message: String, errorCode: Int) {

            }

        })
    }

    private suspend fun fetchFamilies() {
        familyRepository.getAllFamilies(object : OnResult {
            override fun onSuccess(result: Any) {
                val listOfFamilies = mutableListOf<Family>()
                (result as List<Family>).forEach {
                    listOfFamilies.add(it)
                }
                viewModelScope.launch(Dispatchers.Main) {
                    posState.value = posState.value.copy(
                        families = listOfFamilies
                    )
                }
            }

            override fun onFailure(message: String, errorCode: Int) {

            }

        })
    }

    fun saveInvoiceHeader(
        invoiceHeader: InvoiceHeader,
        invoiceItems: MutableList<InvoiceItemModel>
    ) {
        if (invoiceItems.isEmpty()) {
            posState.value = posState.value.copy(
                warning = "invoice doesn't contains any item!",
                isLoading = false
            )
            return
        }
        posState.value = posState.value.copy(
            isLoading = true
        )
        val isInserting = invoiceHeader.invoiceHeadDocumentId.isNullOrEmpty()
        val callback = object : OnResult {
            override fun onSuccess(result: Any) {
                saveInvoiceItems(result as InvoiceHeader, invoiceItems)
            }

            override fun onFailure(message: String, errorCode: Int) {
                viewModelScope.launch(Dispatchers.Main) {
                    posState.value = posState.value.copy(
                        isLoading = false
                    )
                }
            }

        }

        CoroutineScope(Dispatchers.IO).launch {
            if (isInserting) {
                invoiceHeader.invoiceHeadId = Utils.generateRandomUuidString()
                invoiceHeaderRepository.insert(invoiceHeader, callback)
            } else {
                invoiceHeaderRepository.update(invoiceHeader, callback)
            }
        }
    }

    fun saveInvoiceItems(
        invoiceHeader: InvoiceHeader,
        invoiceItems: MutableList<InvoiceItemModel>
    ) {
        val itemsToInsert = invoiceItems.filter { it.invoice.invoiceId.isNullOrEmpty() }
        val itemsToUpdate = invoiceItems.filter { !it.invoice.invoiceId.isNullOrEmpty() }

        val size = itemsToInsert.size
        itemsToInsert.forEachIndexed { index, invoiceItem ->
            invoiceItem.invoice.invoiceHeaderId = invoiceHeader.invoiceHeadId
            saveInvoiceItem(invoiceItem.invoice, true, index == size - 1)
        }

        val size2 = itemsToUpdate.size
        itemsToUpdate.forEachIndexed { index, invoiceItem ->
            invoiceItem.invoice.invoiceHeaderId = invoiceHeader.invoiceHeadId
            saveInvoiceItem(invoiceItem.invoice, false, index == size2 - 1)
        }
    }

    fun saveInvoiceItem(invoice: Invoice, isInserting: Boolean, notify: Boolean = false) {
        val callback = if (notify) object : OnResult {
            override fun onSuccess(result: Any) {
                viewModelScope.launch(Dispatchers.Main) {
                    posState.value = posState.value.copy(
                        isLoading = false
                    )
                }
            }

            override fun onFailure(message: String, errorCode: Int) {
                viewModelScope.launch(Dispatchers.Main) {
                    posState.value = posState.value.copy(
                        isLoading = false
                    )
                }
            }
        } else {
            null
        }

        CoroutineScope(Dispatchers.IO).launch {
            if (isInserting) {
                invoice.invoiceId = Utils.generateRandomUuidString()
                invoiceRepository.insert(invoice, callback)
            } else {
                invoiceRepository.update(invoice, callback)
            }
        }
    }

}