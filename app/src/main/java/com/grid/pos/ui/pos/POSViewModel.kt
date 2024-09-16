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
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.data.ThirdParty.ThirdPartyRepository
import com.grid.pos.model.Event
import com.grid.pos.model.InvoiceItemModel
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
class POSViewModel @Inject constructor(
        private val invoiceHeaderRepository: InvoiceHeaderRepository,
        private val posReceiptRepository: PosReceiptRepository,
        private val invoiceRepository: InvoiceRepository,
        private val itemRepository: ItemRepository,
        private val thirdPartyRepository: ThirdPartyRepository,
        private val familyRepository: FamilyRepository
) : BaseViewModel() {

    private val _posState = MutableStateFlow(POSState())
    val posState: MutableStateFlow<POSState> = _posState
    private var clientsMao: Map<String, ThirdParty> = mutableMapOf()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
            fetchItems()
            fetchFamilies()
        }
    }

    fun loadFamiliesAndItems() {
        val loadItems = posState.value.items.isEmpty()
        val loadFamilies = posState.value.families.isEmpty()
        if (loadItems || loadFamilies) {
            posState.value = posState.value.copy(
                isLoading = true
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            if (loadItems) {
                fetchItems(!loadFamilies)
            }
            if (loadFamilies) {
                fetchFamilies(true)
            }
        }
    }

    private suspend fun fetchItems(stopLoading: Boolean = false) {
        val listOfItems = itemRepository.getAllItems()
        withContext(Dispatchers.Main) {
            posState.value = if (stopLoading) {
                posState.value.copy(
                    items = listOfItems,
                    isLoading = false
                )
            } else {
                posState.value.copy(
                    items = listOfItems
                )
            }
        }
    }

    private suspend fun fetchFamilies(stopLoading: Boolean = false) {
        val listOfFamilies = familyRepository.getAllFamilies()
        withContext(Dispatchers.Main) {
            posState.value = if (stopLoading) {
                posState.value.copy(
                    families = listOfFamilies,
                    isLoading = false
                )
            } else {
                posState.value.copy(
                    families = listOfFamilies
                )
            }
        }
    }

    fun fetchThirdParties() {
        posState.value = posState.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val listOfThirdParties = thirdPartyRepository.getAllThirdParties()
            clientsMao = listOfThirdParties.map { it.thirdPartyId to it }.toMap()
            withContext(Dispatchers.Main) {
                posState.value = posState.value.copy(
                    thirdParties = listOfThirdParties,
                    isLoading = false
                )
            }
        }
    }

    fun fetchInvoices() {
        posState.value = posState.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val listOfInvoices = invoiceHeaderRepository.getAllInvoiceHeaders()
            listOfInvoices.map {
                it.invoiceHeadThirdPartyNewName = clientsMao[it.invoiceHeadThirdPartyName]?.thirdPartyName
            }
            withContext(Dispatchers.Main) {
                posState.value = posState.value.copy(
                    invoiceHeaders = listOfInvoices,
                    isLoading = false
                )
            }
        }
    }

    fun showWarning(
            warning: String,
            action: String
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            posState.value = posState.value.copy(
                warning = Event(warning),
                actionLabel = action,
                isLoading = false
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
                    val lastTransactionIvn = invoiceHeaderRepository.getLastTransactionByType(
                        POSUtils.getInvoiceType(invoiceHeader)
                    )
                    invoiceHeader.invoiceHeadTransNo = POSUtils.getInvoiceTransactionNo(
                        lastTransactionIvn?.invoiceHeadTransNo ?: ""
                    )
                    if (invoiceHeader.invoiceHeadOrderNo.isNullOrEmpty()) {
                        val lastOrderInv = invoiceHeaderRepository.getLastOrderByType(
                            POSUtils.getInvoiceType(invoiceHeader)
                        )
                        invoiceHeader.invoiceHeadOrderNo = POSUtils.getInvoiceNo(
                            lastOrderInv?.invoiceHeadOrderNo ?: ""
                        )
                    }
                    invoiceHeader.invoiceHeadTtCode = SettingsModel.getTransactionType(invoiceHeader.invoiceHeadGrossAmount)
                } else {
                    val lastOrderInv = invoiceHeaderRepository.getLastOrderByType(
                        POSUtils.getInvoiceType(invoiceHeader)
                    )
                    invoiceHeader.invoiceHeadOrderNo = POSUtils.getInvoiceNo(
                        lastOrderInv?.invoiceHeadOrderNo ?: ""
                    )
                    invoiceHeader.invoiceHeadTtCode = null
                    invoiceHeader.invoiceHeadTransNo = null
                }
                invoiceHeader.prepareForInsert()
                val addedInv = invoiceHeaderRepository.insert(
                    invoiceHeader,
                    finish
                )
                posState.value.invoiceHeaders.add(addedInv)
                savePOSReceipt(
                    addedInv,
                    posReceipt,
                    invoiceItems
                )
            } else {
                if (finish && invoiceHeader.invoiceHeadTransNo.isNullOrEmpty()) {
                    val lastTransactionIvn = invoiceHeaderRepository.getLastTransactionByType(
                        POSUtils.getInvoiceType(invoiceHeader)
                    )
                    invoiceHeader.invoiceHeadTransNo = POSUtils.getInvoiceTransactionNo(
                        lastTransactionIvn?.invoiceHeadTransNo ?: ""
                    )
                    if (invoiceHeader.invoiceHeadOrderNo.isNullOrEmpty()) {
                        val lastOrderInv = invoiceHeaderRepository.getLastOrderByType(
                            POSUtils.getInvoiceType(invoiceHeader)
                        )
                        invoiceHeader.invoiceHeadOrderNo = POSUtils.getInvoiceNo(
                            lastOrderInv?.invoiceHeadOrderNo ?: ""
                        )
                    }
                    invoiceHeader.prepareForInsert()
                    invoiceHeader.invoiceHeadTtCode = SettingsModel.getTransactionType(invoiceHeader.invoiceHeadGrossAmount)
                }
                invoiceHeaderRepository.update(
                    invoiceHeader,
                    finish
                )
                savePOSReceipt(
                    invoiceHeader,
                    posReceipt,
                    invoiceItems
                )
            }
        }
    }


    private suspend fun savePOSReceipt(
            invoiceHeader: InvoiceHeader,
            posReceipt: PosReceipt,
            invoiceItems: MutableList<InvoiceItemModel>
    ) {
        val isInserting = posReceipt.isNew()
        if (isInserting) {
            posReceipt.posReceiptInvoiceId = invoiceHeader.invoiceHeadId
            posReceipt.prepareForInsert()
            posReceiptRepository.insert(posReceipt)
        } else {
            posReceiptRepository.update(posReceipt)
        }
        saveInvoiceItems(
            invoiceHeader,
            invoiceItems
        )
    }

    private suspend fun saveInvoiceItems(
            invoiceHeader: InvoiceHeader,
            invoiceItems: MutableList<InvoiceItemModel>
    ) {
        val itemsToInsert = invoiceItems.filter { it.invoice.isNew() }
        val itemsToUpdate = invoiceItems.filter { !it.invoice.isNew() }
        val itemsToDelete = posState.value.itemsToDelete.filter { !it.invoice.isNew() }

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

        itemsToDelete.forEach { invoiceItem ->
            invoiceRepository.delete(invoiceItem.invoice)
        }
    }

    private suspend fun saveInvoiceItem(
            invoice: Invoice,
            isInserting: Boolean,
            notify: Boolean = false
    ) {
        if (isInserting) {
            invoice.prepareForInsert()
            invoiceRepository.insert(invoice)
        } else {
            invoiceRepository.update(invoice)
        }
        if (notify) {
            withContext(Dispatchers.Main) {
                posState.value = posState.value.copy(
                    isLoading = false,
                    isSaved = true,
                    warning = Event("Invoice saved successfully."),
                )
            }
        }
    }

    fun loadInvoiceDetails(
            invoiceHeader: InvoiceHeader,
            onSuccess: (PosReceipt, MutableList<InvoiceItemModel>) -> Unit
    ) {
        posState.value = posState.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val result = invoiceRepository.getAllInvoices(invoiceHeader.invoiceHeadId)
            val invoices = mutableListOf<InvoiceItemModel>()
            result.forEach { inv ->
                invoices.add(InvoiceItemModel(invoice = inv,
                    invoiceItem = posState.value.items.firstOrNull {
                        it.itemId.equals(
                            inv.invoiceItemId,
                            ignoreCase = true
                        )
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
            posState.value = posState.value.copy(
                isLoading = false
            )
            onSuccess.invoke(
                posReceipt ?: PosReceipt(),
                invoices
            )
        }
    }

    fun deleteInvoiceHeader(
            invoiceHeader: InvoiceHeader,
            posReceipt: PosReceipt,
            invoiceItems: MutableList<InvoiceItemModel>
    ) {
        if (invoiceHeader.isNew()) {
            posState.value = posState.value.copy(
                isDeleted = true,
                isLoading = false,
            )
            return
        }
        posState.value = posState.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            invoiceHeaderRepository.delete(invoiceHeader)
            posReceiptRepository.delete(posReceipt)
            invoiceItems.forEach { invoiceItem ->
                invoiceRepository.delete(invoiceItem.invoice)
            }
            withContext(Dispatchers.Main) {
                posState.value = posState.value.copy(
                    isLoading = false,
                    warning = Event("successfully deleted."),
                    isDeleted = true
                )
            }
        }
    }

}