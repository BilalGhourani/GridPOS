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
import com.grid.pos.data.PosReceipt.PosReceipt
import com.grid.pos.data.PosReceipt.PosReceiptRepository
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
        itemRepository.getAllItems(object : OnResult {
            override fun onSuccess(result: Any) {
                val listOfItems = mutableListOf<Item>()
                (result as List<*>).forEach {
                    listOfItems.add(it as Item)
                }
                viewModelScope.launch(Dispatchers.Main) {
                    posState.value = posState.value.copy(
                        items = listOfItems
                    )
                }
            }

            override fun onFailure(
                message: String,
                errorCode: Int
            ) {

            }

        })
    }

    private suspend fun fetchThirdParties() {
        thirdPartyRepository.getAllThirdParties(object : OnResult {
            override fun onSuccess(result: Any) {
                val listOfThirdParties = mutableListOf<ThirdParty>()
                (result as List<*>).forEach {
                    listOfThirdParties.add(it as ThirdParty)
                }
                viewModelScope.launch(Dispatchers.Main) {
                    posState.value = posState.value.copy(
                        thirdParties = listOfThirdParties
                    )
                }
            }

            override fun onFailure(
                message: String,
                errorCode: Int
            ) {

            }

        })
    }

    private suspend fun fetchFamilies() {
        familyRepository.getAllFamilies(object : OnResult {
            override fun onSuccess(result: Any) {
                val listOfFamilies = mutableListOf<Family>()
                (result as List<*>).forEach {
                    listOfFamilies.add(it as Family)
                }
                viewModelScope.launch(Dispatchers.Main) {
                    posState.value = posState.value.copy(
                        families = listOfFamilies
                    )
                }
            }

            override fun onFailure(
                message: String,
                errorCode: Int
            ) {

            }

        })
    }

    private suspend fun fetchInvoices() {
        invoiceHeaderRepository.getAllInvoiceHeaders(object : OnResult {
            override fun onSuccess(result: Any) {
                val listOfInvoices = mutableListOf<InvoiceHeader>()
                (result as List<*>).forEach {
                    listOfInvoices.add(it as InvoiceHeader)
                }
                viewModelScope.launch(Dispatchers.Main) {
                    posState.value = posState.value.copy(
                        invoiceHeaders = listOfInvoices
                    )
                }
            }

            override fun onFailure(
                message: String,
                errorCode: Int
            ) {

            }

        })
    }

    fun saveInvoiceHeader(
        invoiceHeader: InvoiceHeader,
        posReceipt: PosReceipt,
        invoiceItems: MutableList<InvoiceItemModel>,
        finish: Boolean = false
    ) {
        if (invoiceItems.isEmpty()) {
            posState.value = posState.value.copy(
                warning = "invoice doesn't contains any item!",
                isLoading = false,
            )
            return
        }
        posState.value = posState.value.copy(
            isLoading = true
        )
        val isInserting = invoiceHeader.invoiceHeadId.isNullOrEmpty()
        val callback = object : OnResult {
            override fun onSuccess(result: Any) {
                savePOSReceipt(
                    result as InvoiceHeader,
                    posReceipt,
                    invoiceItems
                )
            }

            override fun onFailure(
                message: String,
                errorCode: Int
            ) {
                viewModelScope.launch(Dispatchers.Main) {
                    posState.value = posState.value.copy(
                        warning = message,
                        isLoading = false
                    )
                }
            }

        }

        CoroutineScope(Dispatchers.IO).launch {
            if (isInserting) {
                if (finish) {
                    invoiceHeaderRepository.getLastInvoiceTransNo(posState.value.getInvoiceType(),
                        object : OnResult {
                            override fun onSuccess(result: Any) {
                                result as InvoiceHeader
                                invoiceHeader.invoiceHeadTransNo = Utils.getInvoiceTransactionNo(
                                    result.invoiceHeadTransNo ?: ""
                                )
                                invoiceHeader.invoiceHeadOrderNo = Utils.getInvoiceNo(
                                    result.invoiceHeadOrderNo ?: ""
                                )
                                invoiceHeader.prepareForInsert()
                                invoiceHeader.invoiceHeadTtCode =
                                    if (invoiceHeader.invoiceHeadGrossAmount > 0) "SI" else "RS"
                                CoroutineScope(Dispatchers.IO).launch {
                                    invoiceHeaderRepository.insert(
                                        invoiceHeader,
                                        callback
                                    )
                                }
                            }

                            override fun onFailure(
                                message: String,
                                errorCode: Int
                            ) {
                                viewModelScope.launch(Dispatchers.Main) {
                                    posState.value = posState.value.copy(
                                        warning = message,
                                        isLoading = false
                                    )
                                }
                            }
                        })
                } else {
                    invoiceHeader.invoiceHeadTtCode = null
                    invoiceHeader.prepareForInsert()
                    invoiceHeaderRepository.insert(
                        invoiceHeader,
                        callback
                    )
                }
            } else {
                if (invoiceHeader.invoiceHeadTransNo.isNullOrEmpty()) {
                    invoiceHeaderRepository.getLastInvoiceTransNo(posState.value.getInvoiceType(),
                        object : OnResult {
                            override fun onSuccess(result: Any) {
                                result as InvoiceHeader
                                invoiceHeader.invoiceHeadTransNo = Utils.getInvoiceTransactionNo(
                                    result.invoiceHeadTransNo ?: ""
                                )
                                invoiceHeader.invoiceHeadOrderNo = Utils.getInvoiceNo(
                                    result.invoiceHeadOrderNo ?: ""
                                )
                                invoiceHeader.prepareForInsert()
                                invoiceHeader.invoiceHeadTtCode =
                                    if (invoiceHeader.invoiceHeadGrossAmount > 0) "SI" else "RS"
                                CoroutineScope(Dispatchers.IO).launch {
                                    invoiceHeaderRepository.update(
                                        invoiceHeader,
                                        callback
                                    )
                                }
                            }

                            override fun onFailure(
                                message: String,
                                errorCode: Int
                            ) {
                                viewModelScope.launch(Dispatchers.Main) {
                                    posState.value = posState.value.copy(
                                        warning = message, isLoading = false
                                    )
                                }
                            }
                        })
                } else {
                    invoiceHeaderRepository.update(
                        invoiceHeader,
                        callback
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
        val callback = object : OnResult {
            override fun onSuccess(result: Any) {
                saveInvoiceItems(
                    invoiceHeader,
                    invoiceItems
                )
            }

            override fun onFailure(
                message: String,
                errorCode: Int
            ) {
                viewModelScope.launch(Dispatchers.Main) {
                    posState.value = posState.value.copy(
                        warning = message,
                        isLoading = false
                    )
                }
            }
        }
        val isInserting = posReceipt.posReceiptId.isNullOrEmpty()
        CoroutineScope(Dispatchers.IO).launch {
            if (isInserting) {
                posReceipt.posReceiptInvoiceId = invoiceHeader.invoiceHeadId
                posReceipt.prepareForInsert()
                posReceiptRepository.insert(
                    posReceipt,
                    callback
                )
            } else {
                posReceiptRepository.update(
                    posReceipt,
                    callback
                )
            }
        }
    }

    private fun saveInvoiceItems(
        invoiceHeader: InvoiceHeader,
        invoiceItems: MutableList<InvoiceItemModel>
    ) {
        val itemsToInsert = invoiceItems.filter { it.invoice.invoiceId.isNullOrEmpty() }
        val itemsToUpdate = invoiceItems.filter { !it.invoice.invoiceId.isNullOrEmpty() }

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
        val callback = if (notify) object : OnResult {
            override fun onSuccess(result: Any) {
                viewModelScope.launch(Dispatchers.Main) {
                    posState.value = posState.value.copy(
                        isLoading = false,
                        isSaved = true
                    )
                }
            }

            override fun onFailure(
                message: String,
                errorCode: Int
            ) {
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
                invoice.prepareForInsert()
                invoiceRepository.insert(
                    invoice,
                    callback
                )
            } else {
                invoiceRepository.update(
                    invoice,
                    callback
                )
            }
        }
    }

    fun loadInvoiceDetails(
        invoiceHeader: InvoiceHeader,
        onResult: OnResult
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            invoiceRepository.getAllInvoices(
                invoiceHeader.invoiceHeadId,
                object : OnResult {
                    override fun onSuccess(result: Any) {
                        viewModelScope.launch(Dispatchers.IO) {
                            val invoices = mutableListOf<InvoiceItemModel>()
                            result as List<*>
                            result.forEach { inv ->
                                inv as Invoice
                                invoices.add(InvoiceItemModel(invoice = inv,
                                    invoiceItem = posState.value.items.firstOrNull {
                                        it.itemId.equals(inv.invoiceItemId)
                                    } ?: Item()))
                            }
                            viewModelScope.launch(Dispatchers.Main) {
                                posState.value = posState.value.copy(
                                    invoices = invoices
                                )
                            }
                            getPosReceipt(
                                invoiceHeader.invoiceHeadId,
                                onResult
                            )
                        }
                    }

                    override fun onFailure(
                        message: String,
                        errorCode: Int
                    ) {
                        onResult.onFailure(
                            message,
                            errorCode
                        )
                    }
                })

        }
    }

    suspend fun getPosReceipt(
        invoiceHeaderId: String,
        onResult: OnResult
    ) {
        posReceiptRepository.getPosReceiptByInvoice(invoiceHeaderId,
            object : OnResult {
                override fun onSuccess(result: Any) {
                    viewModelScope.launch(Dispatchers.Main) {
                        posState.value = posState.value.copy(
                            posReceipt = result as PosReceipt
                        )
                        onResult.onSuccess(result)
                    }
                }

                override fun onFailure(
                    message: String,
                    errorCode: Int
                ) {
                    onResult.onFailure(
                        message,
                        errorCode
                    )
                }
            })
    }

    fun deleteInvoiceHeader(invoiceHeader: InvoiceHeader) {
        CoroutineScope(Dispatchers.IO).launch {
            invoiceRepository.getAllInvoices(
                invoiceHeader.invoiceHeadId,
                object : OnResult {
                    override fun onSuccess(result: Any) {
                        CoroutineScope(Dispatchers.IO).launch {
                            result as List<*>
                            val size = result.size
                            result.forEachIndexed { index, invoice ->
                                if (index == size) {
                                    invoiceRepository.delete(
                                        invoice as Invoice,
                                        object : OnResult {
                                            override fun onSuccess(result: Any) {
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    invoiceHeaderRepository.delete(
                                                        invoiceHeader,
                                                        null
                                                    )
                                                }
                                            }

                                            override fun onFailure(
                                                message: String,
                                                errorCode: Int
                                            ) {
                                            }

                                        })
                                } else {
                                    invoiceRepository.delete(
                                        invoice as Invoice,
                                        null
                                    )
                                }

                            }
                        }
                    }

                    override fun onFailure(
                        message: String,
                        errorCode: Int
                    ) {
                    }

                })
        }
    }

}