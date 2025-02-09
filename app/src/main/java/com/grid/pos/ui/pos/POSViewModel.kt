package com.grid.pos.ui.pos

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.grid.pos.App
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
import com.grid.pos.data.settings.SettingsRepository
import com.grid.pos.data.thirdParty.ThirdParty
import com.grid.pos.data.thirdParty.ThirdPartyRepository
import com.grid.pos.data.user.UserRepository
import com.grid.pos.model.Event
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.PopupState
import com.grid.pos.model.ReportResult
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.BaseViewModel
import com.grid.pos.utils.PrinterUtils
import com.grid.pos.utils.Utils
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
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository
) : BaseViewModel() {

    private val _posState = MutableStateFlow(POSState())
    val posState: MutableStateFlow<POSState> = _posState

    var invoiceItemModels: MutableList<InvoiceItemModel> = mutableListOf()
    val itemsToDelete: MutableList<InvoiceItemModel> = mutableListOf()
    var selectedItemIndex: Int = -1
    var proceedToPrint: Boolean = true
    val isDeviceLargerThan7Inches = Utils.isDeviceLargerThan7Inches(App.getInstance())
    var isTablet = false
    var isInvoiceEdited = false

    var popupState: PopupState? = null

    private var clientsMap: Map<String, ThirdParty> = mutableMapOf()
    val reportResults = mutableListOf<ReportResult>()

    var defaultThirdParty: ThirdParty? = null
    private var siTransactionType: String = "null"
    private var rsTransactionType: String = "null"

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
            fetchItems()
            fetchFamilies()
            fetchGlobalSettings()
        }
    }

    fun updateState(newState: POSState) {
        _posState.value = newState
    }

    private suspend fun fetchGlobalSettings() {
        defaultThirdParty = thirdPartyRepository.getDefaultThirdParty()
        if (SettingsModel.isConnectedToSqlServer()) {
            if (SettingsModel.siTransactionType.isNullOrEmpty()) {
                SettingsModel.siTransactionType =
                    settingsRepository.getTransactionTypeId("Sale Invoice")
            }
            if (SettingsModel.rsTransactionType.isNullOrEmpty()) {
                SettingsModel.rsTransactionType =
                    settingsRepository.getTransactionTypeId("Return Sale")
            }
            siTransactionType = SettingsModel.siTransactionType ?: "null"
            rsTransactionType = SettingsModel.rsTransactionType ?: "null"

            val currency = SettingsModel.currentCurrency ?: return

            if (SettingsModel.posReceiptAccCashId.isNullOrEmpty()) {
                SettingsModel.posReceiptAccCashId = settingsRepository.getPosReceiptAccIdBy(
                    "Cash",
                    currency.currencyId
                )
            }
            if (SettingsModel.posReceiptAccCash1Id.isNullOrEmpty()) {
                SettingsModel.posReceiptAccCash1Id = settingsRepository.getPosReceiptAccIdBy(
                    "Cash",
                    currency.currencyDocumentId ?: ""
                )
            }
            if (SettingsModel.posReceiptAccCreditId.isNullOrEmpty()) {
                SettingsModel.posReceiptAccCreditId = settingsRepository.getPosReceiptAccIdBy(
                    "Credit Card",
                    currency.currencyId
                )
            }
            if (SettingsModel.posReceiptAccCredit1Id.isNullOrEmpty()) {
                SettingsModel.posReceiptAccCredit1Id = settingsRepository.getPosReceiptAccIdBy(
                    "Credit Card",
                    currency.currencyDocumentId ?: ""
                )
            }
            if (SettingsModel.posReceiptAccDebitId.isNullOrEmpty()) {
                SettingsModel.posReceiptAccDebitId = settingsRepository.getPosReceiptAccIdBy(
                    "Debit Card",
                    currency.currencyId
                )
            }
            if (SettingsModel.posReceiptAccDebit1Id.isNullOrEmpty()) {
                SettingsModel.posReceiptAccDebit1Id = settingsRepository.getPosReceiptAccIdBy(
                    "Debit Card",
                    currency.currencyDocumentId ?: ""
                )
            }
        } else {
            siTransactionType = SettingsModel.defaultSaleInvoice
            rsTransactionType = SettingsModel.defaultReturnSale
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
        print: Boolean = false,
        finish: Boolean = false,
    ) {
        val invoiceHeader = posState.value.invoiceHeader
        if (posState.value.invoiceItems.isEmpty()) {
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
                        getInvoiceType(invoiceHeader)
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
                        getTransactionType(invoiceHeader.invoiceHeadGrossAmount)
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
                        getTransactionType(invoiceHeader.invoiceHeadGrossAmount)
                }
                if (finish) {
                    if (!invoiceHeader.isFinished()) {
                        val lastTransactionIvn = invoiceHeaderRepository.getLastTransactionByType(
                            getInvoiceType(invoiceHeader)
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
        print: Boolean
    ) {
        val posReceipt = posState.value.posReceipt
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
            print
        )
    }

    private suspend fun saveInvoiceItems(
        context: Context,
        print: Boolean
    ) {
        val itemsToInsert = posState.value.invoiceItems.filter { it.invoice.isNew() }
        val itemsToUpdate = posState.value.invoiceItems.filter { !it.invoice.isNew() }
        val itemsToDelete = itemsToDelete.filter { !it.invoice.isNew() }

        itemsToInsert.forEach { invoiceItem ->
            invoiceItem.invoice.invoiceHeaderId = posState.value.invoiceHeader.invoiceHeadId
            saveInvoiceItem(
                invoiceItem,
                true
            )
        }

        itemsToUpdate.forEach { invoiceItem ->
            invoiceItem.invoice.invoiceHeaderId = posState.value.invoiceHeader.invoiceHeadId
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
            prepareInvoiceReports(context)
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
        context: Context
    ) {
        posState.value = posState.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            invoiceHeaderRepository.updateInvoiceHeader(
                posState.value.invoiceHeader
            )
            prepareInvoiceReports(context)
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
        invoiceHeader: InvoiceHeader
    ) {
        if (invoiceHeader.invoiceHeadId.isEmpty()) {
            posState.value = posState.value.copy(
                isLoading = false
            )
            return
        }
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

            val posReceipt =
                posReceiptRepository.getPosReceiptByInvoice(invoiceHeader.invoiceHeadId)
            invoiceItemModels = invoices.toMutableList()
            viewModelScope.launch(Dispatchers.Main) {
                posState.value = posState.value.copy(
                    isLoading = false,
                    invoiceItems = invoices,
                    invoiceHeader = POSUtils.refreshValues(
                        invoices,
                        invoiceHeader.copy()
                    ),
                    posReceipt = posReceipt ?: PosReceipt()
                )
            }
        }
    }

    fun deleteInvoiceHeader() {
        val invoiceHeader = posState.value.invoiceHeader
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
                posReceiptRepository.delete(posState.value.posReceipt)
                posState.value.invoiceItems.forEach { invoiceItem ->
                    invoiceRepository.delete(invoiceItem.invoice)
                    invoiceItem.invoiceItem.itemRemQty += invoiceItem.invoice.invoiceQuantity
                    itemRepository.update(invoiceItem.invoiceItem)
                }
                val invoiceHeaders = posState.value.invoiceHeaders.toMutableList()
                val index =
                    invoiceHeaders.indexOfFirst { it.invoiceHeadId == invoiceHeader.invoiceHeadId }
                if (index >= 0) {
                    invoiceHeaders.removeAt(index)
                }
                withContext(Dispatchers.Main) {
                    posState.value = posState.value.copy(
                        invoiceHeaders = invoiceHeaders,
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

    fun unLockTable() {
        viewModelScope.launch(Dispatchers.IO) {
            invoiceHeaderRepository.unLockTable(
                posState.value.invoiceHeader.invoiceHeadId,
                posState.value.invoiceHeader.invoiceHeadTableId!!,
                posState.value.invoiceHeader.invoiceHeadTableType
            )
        }
    }

    private suspend fun prepareInvoiceReports(
        context: Context
    ) {
        val invoiceHeader = posState.value.invoiceHeader
        val defaultThirdParty =
            if (invoiceHeader.invoiceHeadThirdPartyName.isNullOrEmpty() || invoiceHeader.invoiceHeadThirdPartyName == posState.value.selectedThirdParty.thirdPartyId) {
                posState.value.selectedThirdParty
            } else if (invoiceHeader.invoiceHeadThirdPartyName == defaultThirdParty?.thirdPartyId) {
                defaultThirdParty
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
            posState.value.invoiceItems,
            posState.value.posReceipt,
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

        prepareItemsReports(context)
    }

    fun prepareAutoPrint(context: Context, callback: () -> Unit) {
        viewModelScope.launch(Dispatchers.Default) {
            val invoices =
                posState.value.invoiceItems.filter { it.invoice.isNew() || it.shouldPrint }
                    .toMutableList()
            invoices.addAll(itemsToDelete)
            if (invoices.isNotEmpty()) {
                prepareItemsReports(context)
                reportResults.forEach {
                    PrinterUtils.printReport(
                        context,
                        it
                    )
                }
                withContext(Dispatchers.Main) {
                    callback.invoke()
                }
            } else {
                withContext(Dispatchers.Main) {
                    callback.invoke()
                }
            }
        }
    }

    private suspend fun prepareItemsReports(context: Context) {
        if (SettingsModel.autoPrintTickets) {
            if (posState.value.printers.isEmpty()) {
                posState.value.printers = posPrinterRepository.getAllPosPrinters()
            }
            val itemsPrintersMap =
                posState.value.invoiceItems.filter { it.shouldPrint || it.isDeleted }
                    .groupBy { it.invoiceItem.itemPrinter ?: "" }
            itemsPrintersMap.entries.forEach { entry ->
                if (entry.key.isNotEmpty()) {
                    val itemsPrinter =
                        posState.value.printers.firstOrNull { it.posPrinterId == entry.key }
                    if (itemsPrinter != null) {
                        val reportResult = PrinterUtils.getItemReceiptHtmlContent(
                            context = context,
                            invoiceHeader = posState.value.invoiceHeader,
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

    private fun getInvoiceType(invoiceHeader: InvoiceHeader): String {
        return if (!invoiceHeader.invoiceHeadTtCode.isNullOrEmpty()) {
            invoiceHeader.invoiceHeadTtCode!!
        } else {
            getTransactionType(invoiceHeader.invoiceHeadTotal)
        }
    }

    private fun getTransactionType(amount: Double): String {
        return if (amount >= 0) siTransactionType else rsTransactionType
    }

}