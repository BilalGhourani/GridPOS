package com.grid.pos.ui.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.Family.FamilyRepository
import com.grid.pos.data.Invoice.Invoice
import com.grid.pos.data.Invoice.InvoiceRepository
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.InvoiceHeader.InvoiceHeaderRepository
import com.grid.pos.data.Item.Item
import com.grid.pos.data.Item.ItemRepository
import com.grid.pos.data.PosReceipt.PosReceipt
import com.grid.pos.data.PosReceipt.PosReceiptRepository
import com.grid.pos.data.ThirdParty.ThirdPartyRepository
import com.grid.pos.model.Event
import com.grid.pos.model.InvoiceItemModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class POSViewModel @Inject constructor(
        private val invoiceHeaderRepository: InvoiceHeaderRepository,
        private val posReceiptRepository: PosReceiptRepository,
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
            fetchFamilies()
            fetchThirdParties()
            fetchInvoices()
        }
    }

    private suspend fun fetchItems() {
        val listOfItems = itemRepository.getAllItems()
        viewModelScope.launch(Dispatchers.Main) {
            posState.value = posState.value.copy(
                items = listOfItems
            )
        }
    }

    private suspend fun fetchThirdParties() {
        val listOfThirdParties = thirdPartyRepository.getAllThirdParties()
        viewModelScope.launch(Dispatchers.Main) {
            posState.value = posState.value.copy(
                thirdParties = listOfThirdParties
            )
        }
    }

    private suspend fun fetchFamilies() {
        val listOfFamilies = familyRepository.getAllFamilies()
        viewModelScope.launch(Dispatchers.Main) {
            posState.value = posState.value.copy(
                families = listOfFamilies
            )
        }
    }

    private suspend fun fetchInvoices() {
        val listOfInvoices = invoiceHeaderRepository.getAllInvoiceHeaders()
        viewModelScope.launch(Dispatchers.Main) {
            posState.value = posState.value.copy(
                invoiceHeaders = listOfInvoices
            )
        }
    }

    fun saveInvoiceHeader(
            invoiceHeader: InvoiceHeader,
            posReceipt: PosReceipt,
            invoiceItems: MutableList<InvoiceItemModel>,
            finish: Boolean = false,
    ) {
        if (invoiceItems.isEmpty()) {
            posState.value = posState.value.copy(
                warning = Event("invoice doesn't contains any item!"),
                isLoading = false,
            )
            return
        }
        posState.value = posState.value.copy(
            isLoading = true
        )
        val isInserting = invoiceHeader.isNew()

        CoroutineScope(Dispatchers.IO).launch {
            if (isInserting) {
                if (finish) {
                    val result = invoiceHeaderRepository.getLastInvoiceByType(POSUtils.getInvoiceType(invoiceHeader))
                    invoiceHeader.invoiceHeadTransNo = POSUtils.getInvoiceTransactionNo(
                        result?.invoiceHeadTransNo ?: ""
                    )
                    invoiceHeader.invoiceHeadOrderNo = POSUtils.getInvoiceNo(
                        result?.invoiceHeadOrderNo ?: ""
                    )
                    invoiceHeader.prepareForInsert()
                    invoiceHeader.invoiceHeadTtCode = if (invoiceHeader.invoiceHeadGrossAmount > 0) "SI" else "RS"

                    val addedInv = invoiceHeaderRepository.insert(invoiceHeader)
                    posState.value.invoiceHeaders.add(addedInv)
                    savePOSReceipt(
                        addedInv,
                        posReceipt,
                        invoiceItems
                    )
                } else {
                    invoiceHeader.invoiceHeadTtCode = null
                    invoiceHeader.prepareForInsert()
                    invoiceHeaderRepository.insert(invoiceHeader)
                    posState.value.invoiceHeaders.add(invoiceHeader)
                    savePOSReceipt(
                        invoiceHeader,
                        posReceipt,
                        invoiceItems
                    )
                }
            } else {
                if (invoiceHeader.invoiceHeadTransNo.isNullOrEmpty()) {
                    val result = invoiceHeaderRepository.getLastInvoiceByType(POSUtils.getInvoiceType(invoiceHeader))
                    invoiceHeader.invoiceHeadTransNo = POSUtils.getInvoiceTransactionNo(
                        result?.invoiceHeadTransNo ?: ""
                    )
                    invoiceHeader.invoiceHeadOrderNo = POSUtils.getInvoiceNo(
                        result?.invoiceHeadOrderNo ?: ""
                    )
                    invoiceHeader.prepareForInsert()
                    invoiceHeader.invoiceHeadTtCode = if (invoiceHeader.invoiceHeadGrossAmount > 0) "SI" else "RS"
                    invoiceHeaderRepository.update(invoiceHeader)
                    savePOSReceipt(
                        invoiceHeader,
                        posReceipt,
                        invoiceItems
                    )
                } else {
                    invoiceHeaderRepository.update(invoiceHeader)
                    savePOSReceipt(
                        invoiceHeader,
                        posReceipt,
                        invoiceItems
                    )
                }
            }
        }
    }

    private fun savePOSReceipt(
            invoiceHeader: InvoiceHeader,
            posReceipt: PosReceipt,
            invoiceItems: MutableList<InvoiceItemModel>
    ) {
        val isInserting = posReceipt.isNew()
        CoroutineScope(Dispatchers.IO).launch {
            if (isInserting) {
                posReceipt.posReceiptInvoiceId = invoiceHeader.invoiceHeadId
                posReceipt.prepareForInsert()
                posReceiptRepository.insert(posReceipt)
                saveInvoiceItems(
                    invoiceHeader,
                    invoiceItems
                )
            } else {
                posReceiptRepository.update(posReceipt)
                saveInvoiceItems(
                    invoiceHeader,
                    invoiceItems
                )
            }
        }
    }

    private fun saveInvoiceItems(
            invoiceHeader: InvoiceHeader,
            invoiceItems: MutableList<InvoiceItemModel>
    ) {
        val itemsToInsert = invoiceItems.filter { it.invoice.isNew() }
        val itemsToUpdate = invoiceItems.filter { !it.invoice.isNew() }

        val size = itemsToInsert.size
        itemsToInsert.forEachIndexed { index, invoiceItem ->
            invoiceItem.invoice.invoiceHeaderId = invoiceHeader.invoiceHeadId
            saveInvoiceItem(
                invoiceItem.invoice,
                true,
                index == size - 1
            )
        }

        val size2 = itemsToUpdate.size
        itemsToUpdate.forEachIndexed { index, invoiceItem ->
            invoiceItem.invoice.invoiceHeaderId = invoiceHeader.invoiceHeadId
            saveInvoiceItem(
                invoiceItem.invoice,
                false,
                index == size2 - 1
            )
        }
    }

    private fun saveInvoiceItem(
            invoice: Invoice,
            isInserting: Boolean,
            notify: Boolean = false
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            if (isInserting) {
                invoice.prepareForInsert()
                invoiceRepository.insert(invoice)
            } else {
                invoiceRepository.update(invoice)
            }
            if (notify) {
                viewModelScope.launch(Dispatchers.Main) {
                    posState.value = posState.value.copy(
                        isLoading = false,
                        isSaved = true,
                    )
                }
            }
        }
    }

    fun loadInvoiceDetails(
            invoiceHeader: InvoiceHeader,
            onSuccess: (PosReceipt, MutableList<InvoiceItemModel>) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = invoiceRepository.getAllInvoices(invoiceHeader.invoiceHeadId)
            val invoices = mutableListOf<InvoiceItemModel>()
            result.forEach { inv ->
                invoices.add(InvoiceItemModel(invoice = inv,
                    invoiceItem = posState.value.items.firstOrNull {
                        it.itemId.equals(inv.invoiceItemId,ignoreCase = true)
                    } ?: Item()))
            }
            getPosReceipt(
                invoiceHeader.invoiceHeadId,
                invoices,
                onSuccess
            )

        }
    }

    private suspend fun getPosReceipt(
            invoiceHeaderId: String,
            invoices: MutableList<InvoiceItemModel>,
            onSuccess: (PosReceipt, MutableList<InvoiceItemModel>) -> Unit
    ) {
        val posReceipt = posReceiptRepository.getPosReceiptByInvoice(invoiceHeaderId)
        viewModelScope.launch(Dispatchers.Main) {
            onSuccess.invoke(
                posReceipt ?: PosReceipt(),
                invoices
            )
        }
    }

    fun deleteInvoiceHeader(invoiceHeader: InvoiceHeader) {
        viewModelScope.launch(Dispatchers.IO) {
            val posReceipt = posReceiptRepository.getPosReceiptByInvoice(invoiceHeader.invoiceHeadId)
            posReceipt?.let {
                posReceiptRepository.delete(it)
            }
            val invoiceToDelete = invoiceRepository.getAllInvoices(invoiceHeader.invoiceHeadId)
            val size = invoiceToDelete.size
            invoiceToDelete.forEachIndexed { index, invoice ->
                invoiceRepository.delete(invoice)
                if (index == size) {
                    invoiceHeaderRepository.delete(invoiceHeader)
                }
            }
            withContext(Dispatchers.Main) {
                posState.value = posState.value.copy(
                    isLoading = false
                )
            }
        }
    }

}