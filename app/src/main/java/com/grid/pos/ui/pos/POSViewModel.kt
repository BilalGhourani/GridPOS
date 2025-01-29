package com.grid.pos.ui.pos

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.currency.Currency
import com.grid.pos.data.currency.CurrencyRepository
import com.grid.pos.data.family.FamilyRepository
import com.grid.pos.data.invoice.InvoiceRepository
import com.grid.pos.data.invoiceHeader.InvoiceHeader
import com.grid.pos.data.invoiceHeader.InvoiceHeaderRepository
import com.grid.pos.data.item.Item
import com.grid.pos.data.item.ItemRepository
import com.grid.pos.data.posPrinter.PosPrinterRepository
import com.grid.pos.data.posReceipt.PosReceipt
import com.grid.pos.data.posReceipt.PosReceiptRepository
import com.grid.pos.data.thirdParty.ThirdParty
import com.grid.pos.data.thirdParty.ThirdPartyRepository
import com.grid.pos.data.user.UserRepository
import com.grid.pos.model.Event
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.ReportResult
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.BaseViewModel
import com.grid.pos.utils.PrinterUtils
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val currencyRepository: CurrencyRepository,
    private val familyRepository: FamilyRepository,
    private val posPrinterRepository: PosPrinterRepository,
    private val userRepository: UserRepository
) : BaseViewModel() {

    private val _posState = MutableStateFlow(POSState())
    val posState: MutableStateFlow<POSState> = _posState
    private var clientsMap: Map<String, ThirdParty> = mutableMapOf()
    val reportResults = mutableListOf<ReportResult>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
            fetchItems()
            fetchFamilies()
        }
    }

    fun clearPosState() {
        posState.value = posState.value.copy(
            itemsToDelete = mutableListOf(),
            selectedThirdParty = SettingsModel.defaultThirdParty ?: ThirdParty(),
            isSaved = false,
            isDeleted = false,
            isLoading = false,
            warning = null,
            actionLabel = null
        )
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

    suspend fun fetchItems(stopLoading: Boolean = false) {
        if (SettingsModel.currentCurrency == null && SettingsModel.isConnectedToSqlServer()) {
            val currencies = currencyRepository.getAllCurrencies()
            val currency = if (currencies.size > 0) currencies[0] else Currency()
            SettingsModel.currentCurrency = currency
        }
        val listOfItems = itemRepository.getItemsForPOS()
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

    fun fetchThirdParties(withLoading: Boolean = true) {
        if (withLoading) {
            posState.value = posState.value.copy(
                isLoading = true
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val listOfThirdParties = thirdPartyRepository.getAllThirdParties()
            val defaultTp = posState.value.selectedThirdParty
            if (defaultTp.thirdPartyId.isNotEmpty()) {
                val defTp =
                    listOfThirdParties.firstOrNull { it.thirdPartyId == defaultTp.thirdPartyId }
                if (defTp == null) {
                    listOfThirdParties.add(
                        0,
                        defaultTp
                    )
                }
            }
            clientsMap = listOfThirdParties.associateBy { it.thirdPartyId }
            withContext(Dispatchers.Main) {
                posState.value = if (withLoading) {
                    posState.value.copy(
                        thirdParties = listOfThirdParties,
                        isLoading = false
                    )
                } else {
                    posState.value.copy(
                        thirdParties = listOfThirdParties
                    )
                }
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
                it.invoiceHeadThirdPartyNewName =
                    clientsMap[it.invoiceHeadThirdPartyName]?.thirdPartyName
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
        action: String? = null
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
        context: Context,
        invoiceHeader: InvoiceHeader,
        posReceipt: PosReceipt,
        invoiceItems: MutableList<InvoiceItemModel>,
        print: Boolean = false,
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

        viewModelScope.launch(Dispatchers.IO) {
            if (isInserting) {
                if (finish) {
                    val lastTransactionIvn = invoiceHeaderRepository.getLastTransactionByType(
                        POSUtils.getInvoiceType(invoiceHeader)
                    )
                    invoiceHeader.invoiceHeadTransNo = POSUtils.getInvoiceTransactionNo(
                        lastTransactionIvn?.invoiceHeadTransNo ?: ""
                    )
                    if (invoiceHeader.invoiceHeadOrderNo.isNullOrEmpty()) {
                        val lastOrderInv = invoiceHeaderRepository.getLastOrderByType()
                        invoiceHeader.invoiceHeadOrderNo = POSUtils.getInvoiceNo(
                            lastOrderInv?.invoiceHeadOrderNo ?: ""
                        )
                    }
                    invoiceHeader.invoiceHeadStatus = null
                } else {
                    val lastOrderInv = invoiceHeaderRepository.getLastOrderByType()
                    invoiceHeader.invoiceHeadOrderNo = POSUtils.getInvoiceNo(
                        lastOrderInv?.invoiceHeadOrderNo ?: ""
                    )
                    invoiceHeader.invoiceHeadTransNo = null
                }
                if (invoiceHeader.invoiceHeadTtCode.isNullOrEmpty()) {
                    invoiceHeader.invoiceHeadTtCode =
                        SettingsModel.getTransactionType(invoiceHeader.invoiceHeadGrossAmount)
                }
                invoiceHeader.prepareForInsert()
                val dataModel = invoiceHeaderRepository.insert(
                    invoiceHeader,
                    print,
                    finish
                )
                if (dataModel.succeed) {
                    val addedInv = dataModel.data as InvoiceHeader
                    if ((finish || invoiceHeader.invoiceHeadTaName.isNullOrEmpty()) && posState.value.invoiceHeaders.isNotEmpty()) {
                        posState.value.invoiceHeaders.add(
                            0,
                            addedInv
                        )
                    }
                    savePOSReceipt(
                        context,
                        addedInv,
                        posReceipt,
                        invoiceItems,
                        print
                    )
                } else {
                    withContext(Dispatchers.Main) {
                        posState.value = posState.value.copy(
                            isLoading = false
                        )
                    }
                }
            } else {
                if (invoiceHeader.invoiceHeadOrderNo.isNullOrEmpty()) {
                    val lastOrderInv = invoiceHeaderRepository.getLastOrderByType()
                    invoiceHeader.invoiceHeadOrderNo = POSUtils.getInvoiceNo(
                        lastOrderInv?.invoiceHeadOrderNo ?: ""
                    )
                }
                if (invoiceHeader.invoiceHeadTtCode.isNullOrEmpty()) {
                    invoiceHeader.invoiceHeadTtCode =
                        SettingsModel.getTransactionType(invoiceHeader.invoiceHeadGrossAmount)
                }
                if (finish) {
                    if (!invoiceHeader.isFinished()) {
                        val lastTransactionIvn = invoiceHeaderRepository.getLastTransactionByType(
                            POSUtils.getInvoiceType(invoiceHeader)
                        )
                        invoiceHeader.invoiceHeadTransNo = POSUtils.getInvoiceTransactionNo(
                            lastTransactionIvn?.invoiceHeadTransNo ?: ""
                        )
                    }
                }
                val dataModel = invoiceHeaderRepository.update(
                    invoiceHeader,
                    print,
                    finish
                )
                if (dataModel.succeed) {
                    val index =
                        posState.value.invoiceHeaders.indexOfFirst { it.invoiceHeadId == invoiceHeader.invoiceHeadId }
                    if (index >= 0) {
                        posState.value.invoiceHeaders.removeAt(index)
                        posState.value.invoiceHeaders.add(
                            index,
                            invoiceHeader
                        )
                    } else if ((finish || invoiceHeader.invoiceHeadTaName.isNullOrEmpty()) && posState.value.invoiceHeaders.isNotEmpty()) {
                        posState.value.invoiceHeaders.add(
                            0,
                            invoiceHeader
                        )
                    }
                    savePOSReceipt(
                        context,
                        invoiceHeader,
                        posReceipt,
                        invoiceItems,
                        print
                    )
                } else {
                    withContext(Dispatchers.Main) {
                        posState.value = posState.value.copy(
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    private suspend fun savePOSReceipt(
        context: Context,
        invoiceHeader: InvoiceHeader,
        posReceipt: PosReceipt,
        invoiceItems: MutableList<InvoiceItemModel>,
        print: Boolean
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
            context,
            invoiceHeader,
            invoiceItems,
            posReceipt,
            print
        )
    }

    private suspend fun saveInvoiceItems(
        context: Context,
        invoiceHeader: InvoiceHeader,
        invoiceItems: MutableList<InvoiceItemModel>,
        posReceipt: PosReceipt,
        print: Boolean
    ) {
        val itemsToInsert = invoiceItems.filter { it.invoice.isNew() }
        val itemsToUpdate = invoiceItems.filter { !it.invoice.isNew() }
        val itemsToDelete = posState.value.itemsToDelete.filter { !it.invoice.isNew() }

        itemsToInsert.forEach { invoiceItem ->
            invoiceItem.invoice.invoiceHeaderId = invoiceHeader.invoiceHeadId
            saveInvoiceItem(
                invoiceItem,
                true
            )
        }

        itemsToUpdate.forEach { invoiceItem ->
            invoiceItem.invoice.invoiceHeaderId = invoiceHeader.invoiceHeadId
            saveInvoiceItem(
                invoiceItem,
                false
            )
        }

        itemsToDelete.forEach { invoiceItem ->
            invoiceRepository.delete(invoiceItem.invoice)
            invoiceItem.invoiceItem.itemRemQty += invoiceItem.invoice.invoiceQuantity
            itemRepository.update(invoiceItem.invoiceItem)
        }
        if (print) {
            prepareInvoiceReports(
                context,
                invoiceHeader,
                invoiceItems,
                posReceipt
            )
        } else {
            reportResults.clear()
        }
        withContext(Dispatchers.Main) {
            posState.value = posState.value.copy(
                isLoading = false,
                isSaved = true,
                warning = Event("Invoice saved successfully."),
            )
        }
    }

    private suspend fun saveInvoiceItem(
        invoiceModel: InvoiceItemModel,
        isInserting: Boolean
    ) {
        if (isInserting) {
            invoiceModel.invoice.prepareForInsert()
            val remQty = invoiceModel.invoiceItem.itemRemQty - invoiceModel.invoice.invoiceQuantity
            invoiceModel.invoice.invoiceRemQty = remQty
            invoiceRepository.insert(invoiceModel.invoice)
            invoiceModel.invoiceItem.itemRemQty = remQty
            itemRepository.update(invoiceModel.invoiceItem)
        } else {
            if (invoiceModel.initialQty > invoiceModel.invoice.invoiceQuantity) {// was 4 and now is 3 => increase by 1
                invoiceModel.invoiceItem.itemRemQty += invoiceModel.initialQty - invoiceModel.invoice.invoiceQuantity
                invoiceModel.invoice.invoiceRemQty = invoiceModel.invoiceItem.itemRemQty
                invoiceRepository.update(invoiceModel.invoice)
                itemRepository.update(invoiceModel.invoiceItem)
            } else if (invoiceModel.initialQty < invoiceModel.invoice.invoiceQuantity) { // was 4 and now is 5 => decrease by 1
                invoiceModel.invoiceItem.itemRemQty -= invoiceModel.invoice.invoiceQuantity - invoiceModel.initialQty
                invoiceModel.invoice.invoiceRemQty = invoiceModel.invoiceItem.itemRemQty
                invoiceRepository.update(invoiceModel.invoice)
                itemRepository.update(invoiceModel.invoiceItem)
            }
        }
    }

    fun savePrintedNumber(
        context: Context,
        invoiceHeader: InvoiceHeader,
        invoiceItems: MutableList<InvoiceItemModel>,
        posReceipt: PosReceipt
    ) {
        posState.value = posState.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            invoiceHeaderRepository.updateInvoiceHeader(
                invoiceHeader
            )
            prepareInvoiceReports(
                context,
                invoiceHeader,
                invoiceItems,
                posReceipt
            )
            withContext(Dispatchers.Main) {
                posState.value = posState.value.copy(
                    isLoading = false,
                    isSaved = true,
                    warning = null
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
            if (posState.value.items.isEmpty()) {
                fetchItems(false)
            }
            if (posState.value.families.isEmpty()) {
                fetchFamilies(false)
            }
            if (posState.value.thirdParties.isEmpty()) {
                fetchThirdParties(false)
            }
            val result = invoiceRepository.getAllInvoices(invoiceHeader.invoiceHeadId)
            val invoices = mutableListOf<InvoiceItemModel>()
            result.forEach { inv ->
                invoices.add(InvoiceItemModel(invoice = inv,
                    initialQty = inv.invoiceQuantity,
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
            val dataModel = invoiceHeaderRepository.delete(invoiceHeader)
            if (dataModel.succeed) {
                posReceiptRepository.delete(posReceipt)
                invoiceItems.forEach { invoiceItem ->
                    invoiceRepository.delete(invoiceItem.invoice)
                    invoiceItem.invoiceItem.itemRemQty += invoiceItem.invoice.invoiceQuantity
                    itemRepository.update(invoiceItem.invoiceItem)
                }
                withContext(Dispatchers.Main) {
                    posState.value = posState.value.copy(
                        isLoading = false,
                        warning = Event("successfully deleted."),
                        isDeleted = true
                    )
                }
            } else {
                withContext(Dispatchers.Main) {
                    posState.value = posState.value.copy(
                        isLoading = false
                    )
                }
            }
        }
    }

    fun unLockTable(
        invoiceId: String,
        tableId: String,
        tableType: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            invoiceHeaderRepository.unLockTable(
                invoiceId,
                tableId,
                tableType
            )
        }
    }

    private suspend fun prepareInvoiceReports(
        context: Context,
        invoiceHeader: InvoiceHeader,
        invoiceItems: MutableList<InvoiceItemModel>,
        posReceipt: PosReceipt
    ) {
        val defaultThirdParty =
            if (invoiceHeader.invoiceHeadThirdPartyName.isNullOrEmpty() || invoiceHeader.invoiceHeadThirdPartyName == posState.value.selectedThirdParty.thirdPartyId) {
                posState.value.selectedThirdParty
            } else if (invoiceHeader.invoiceHeadThirdPartyName == SettingsModel.defaultThirdParty?.thirdPartyId) {
                SettingsModel.defaultThirdParty
            } else {
                if (posState.value.thirdParties.isEmpty()) {
                    thirdPartyRepository.getThirdPartyByID(invoiceHeader.invoiceHeadThirdPartyName!!)
                } else {
                    posState.value.thirdParties.firstOrNull {
                        it.thirdPartyId == invoiceHeader.invoiceHeadThirdPartyName
                    }
                }
            }
        val user =
            if (invoiceHeader.invoiceHeadUserStamp.isNullOrEmpty() || SettingsModel.currentUser?.userId == invoiceHeader.invoiceHeadUserStamp || SettingsModel.currentUser?.userUsername == invoiceHeader.invoiceHeadUserStamp) {
                SettingsModel.currentUser
            } else {
                if (posState.value.users.isEmpty()) {
                    userRepository.getUserById(invoiceHeader.invoiceHeadUserStamp!!)
                } else {
                    posState.value.users.firstOrNull {
                        it.userId == invoiceHeader.invoiceHeadUserStamp || it.userUsername == invoiceHeader.invoiceHeadUserStamp
                    } ?: SettingsModel.currentUser
                }
            }

        val invoiceReport = PrinterUtils.getInvoiceReceiptHtmlContent(
            context,
            invoiceHeader,
            invoiceItems,
            posReceipt,
            defaultThirdParty,
            user,
            SettingsModel.currentCompany
        )
        SettingsModel.cashPrinter?.let {
            if (it.contains(":")) {
                val printerDetails = it.split(":")
                val size = printerDetails.size
                invoiceReport.printerIP = if (size > 0) printerDetails[0] else ""
                val port = if (size > 1) printerDetails[1] else "-1"
                invoiceReport.printerPort = port.toIntOrNull() ?: -1
            } else {
                invoiceReport.printerName = it
            }
        }
        reportResults.add(invoiceReport)

        prepareItemsReports(
            context,
            invoiceHeader,
            invoiceItems
        )
    }

    suspend fun prepareItemsReports(
        context: Context,
        invoiceHeader: InvoiceHeader,
        invoiceItems: MutableList<InvoiceItemModel>
    ) {
        if (SettingsModel.autoPrintTickets) {
            if (posState.value.printers.isEmpty()) {
                posState.value.printers = posPrinterRepository.getAllPosPrinters()
            }
            val itemsPrintersMap = invoiceItems.filter { it.shouldPrint || it.isDeleted }
                .groupBy { it.invoiceItem.itemPrinter ?: "" }
            itemsPrintersMap.entries.forEach { entry ->
                if (entry.key.isNotEmpty()) {
                    val itemsPrinter =
                        posState.value.printers.firstOrNull { it.posPrinterId == entry.key }
                    if (itemsPrinter != null) {
                        val reportResult = PrinterUtils.getItemReceiptHtmlContent(
                            context = context,
                            invoiceHeader = invoiceHeader,
                            invItemModels = entry.value
                        )
                        reportResult.printerName = itemsPrinter.posPrinterName ?: ""
                        reportResult.printerIP = itemsPrinter.posPrinterHost
                        reportResult.printerPort = itemsPrinter.posPrinterPort
                        reportResults.add(reportResult)
                    }
                }
            }
        }
    }

}