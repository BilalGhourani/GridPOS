package com.grid.pos.ui.pos

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.grid.pos.App
import com.grid.pos.SharedViewModel
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
import com.grid.pos.interfaces.OnBarcodeResult
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.ReportResult
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.UserType
import com.grid.pos.ui.common.BaseViewModel
import com.grid.pos.utils.PrinterUtils
import com.grid.pos.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    private val settingsRepository: SettingsRepository,
    private val sharedViewModel: SharedViewModel
) : BaseViewModel(sharedViewModel) {
    val state: MutableState<POSState> = mutableStateOf(POSState())

    var taxRate: Double? = null
    var isLandscape: Boolean = false
    var triggerSaveCallback = mutableStateOf(false)
    var isEditBottomSheetVisible = mutableStateOf(false)
    var isAddItemBottomSheetVisible = mutableStateOf(false)
    var isPayBottomSheetVisible = mutableStateOf(false)

    var invoiceItemModels: MutableList<InvoiceItemModel> = mutableListOf()
    val itemsToDelete: MutableList<InvoiceItemModel> = mutableListOf()
    var selectedItemIndex: Int = -1
    val isDeviceLargerThan7Inches = Utils.isDeviceLargerThan7Inches(App.getInstance())
    var isTablet = false
    var isInvoiceEdited = false

    var currentInvoice: InvoiceHeader? = null

    private var clientsMap: Map<String, ThirdParty> = mutableMapOf()
    private val reportResults = mutableListOf<ReportResult>()

    var defaultThirdParty: ThirdParty? = null
    private var siTransactionType: String = "null"
    private var rsTransactionType: String = "null"
    private val mutex = Mutex()  // Create a Mutex object
    private var isInitiating: Boolean = false

    init {
        viewModelScope.launch(Dispatchers.IO) {
            mutex.withLock {
                isInitiating = true
                openConnectionIfNeeded()
                fetchItems()
                fetchFamilies()
                fetchGlobalSettings()
                if (SettingsModel.isConnectedToSqlServer()) {
                    taxRate = currencyRepository.getRate(
                        SettingsModel.currentCurrency?.currencyId,
                        SettingsModel.currentCompany?.companyCurCodeTax
                    )
                }
                isInitiating = false
            }
        }
    }

    fun isAnyPopupShown(): Boolean {
        return isEditBottomSheetVisible.value || isAddItemBottomSheetVisible.value || isPayBottomSheetVisible.value
    }

    fun handleBack(callback: (String, Boolean) -> Unit) {
        if (isAddItemBottomSheetVisible.value) {
            isAddItemBottomSheetVisible.value = false
        } else if (isEditBottomSheetVisible.value) {
            isEditBottomSheetVisible.value = false
        } else if (isPayBottomSheetVisible.value) {
            isPayBottomSheetVisible.value = false
        } else if (state.value.invoiceItems.isNotEmpty()) {
            callback.invoke("discard", true)
        } else {
            if (!state.value.invoiceHeader.invoiceHeadTableId.isNullOrEmpty()) {
                unLockTable()
            }
            if (SettingsModel.getUserType() == UserType.POS) {
                callback.invoke("back", true)
            } else {
                sharedViewModel.isFromTable = false
                callback.invoke("", false)
            }
        }
    }

    fun resetState() {
        itemsToDelete.clear()
        invoiceItemModels.clear()
        updateState(
            state.value.copy(
                invoiceItems = mutableListOf(),
                invoiceHeader = InvoiceHeader(),
                posReceipt = PosReceipt(),
                selectedThirdParty = defaultThirdParty ?: ThirdParty(),
            )
        )
    }

    fun updateState(newState: POSState) {
        state.value = newState
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
        if (isInitiating) {
            return
        }
        val loadItems = state.value.items.isEmpty()
        val loadFamilies = state.value.families.isEmpty()
        if (loadItems || loadFamilies) {
            showLoading(true)
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
        sharedViewModel.fetchItemsAgain = false
        if (SettingsModel.currentCurrency == null && SettingsModel.isConnectedToSqlServer()) {
            val currencies = currencyRepository.getAllCurrencies()
            val currency = if (currencies.size > 0) currencies[0] else Currency()
            SettingsModel.currentCurrency = currency
        }
        val listOfItems = itemRepository.getItemsForPOS()
        withContext(Dispatchers.Main) {
            updateState(
                state.value.copy(
                    items = listOfItems
                )
            )
            if (stopLoading) {
                showLoading(false)
            }
        }
    }

    private suspend fun fetchFamilies(stopLoading: Boolean = false) {
        val listOfFamilies =
            familyRepository.getFamiliesForPOS(Utils.getDeviceID(App.getInstance()))
        withContext(Dispatchers.Main) {
            updateState(
                state.value.copy(
                    families = listOfFamilies
                )
            )
            if (stopLoading) {
                showLoading(false)
            }
        }
    }

    fun fetchThirdParties(withLoading: Boolean = true) {
        sharedViewModel.fetchThirdPartiesAgain = false
        if (withLoading) {
            showLoading(true)
        }
        viewModelScope.launch(Dispatchers.IO) {
            val listOfThirdParties = thirdPartyRepository.getAllThirdParties()
            val defaultTp = state.value.selectedThirdParty
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
                updateState(
                    state.value.copy(
                        thirdParties = listOfThirdParties
                    )
                )
                if (withLoading) {
                    showLoading(false)
                }
            }
        }
    }

    fun fetchInvoices() {
        showLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            val listOfInvoices = invoiceHeaderRepository.getAllInvoiceHeaders()
            listOfInvoices.map {
                it.invoiceHeadThirdPartyNewName =
                    clientsMap[it.invoiceHeadThirdPartyName]?.thirdPartyName
            }
            withContext(Dispatchers.Main) {
                updateState(
                    state.value.copy(
                        invoiceHeaders = listOfInvoices
                    )
                )
                showLoading(false)
            }
        }
    }

    fun saveInvoiceHeader(
        context: Context,
        print: Boolean = false,
        finish: Boolean = false,
        proceedToPrint: Boolean = false,
        callback: () -> Unit
    ) {
        val invoiceHeader = state.value.invoiceHeader
        if (state.value.invoiceItems.isEmpty()) {
            showWarning("invoice doesn't contains any item!")
            return
        }
        showLoading(true)
        val isInserting = invoiceHeader.isNew()

        viewModelScope.launch(Dispatchers.IO) {
            if (isInserting) {
                invoiceHeader.invoiceHeadTaxRate = taxRate
                if (finish) {
                    val lastTransactionIvn = invoiceHeaderRepository.getLastTransactionByType(
                        getInvoiceType(invoiceHeader)
                    )
                    invoiceHeader.invoiceHeadTransNo = POSUtils.getInvoiceTransactionNo(
                        lastTransactionIvn?.invoiceHeadTransNo ?: ""
                    )
                } else {
                    val lastOrderInv = invoiceHeaderRepository.getLastOrderByType()
                    invoiceHeader.invoiceHeadOrderNo = POSUtils.getInvoiceNo(
                        lastOrderInv?.invoiceHeadOrderNo ?: ""
                    )
                }
                if (invoiceHeader.invoiceHeadTtCode.isNullOrEmpty()) {
                    invoiceHeader.invoiceHeadTtCode =
                        getTransactionType(invoiceHeader.invoiceHeadGrossAmount)
                }
                invoiceHeader.prepareForInsert()
                val dataModel = invoiceHeaderRepository.insert(invoiceHeader, print, finish)
                if (dataModel.succeed) {
                    val addedInv = dataModel.data as InvoiceHeader
                    if ((finish || invoiceHeader.invoiceHeadTaName.isNullOrEmpty()) && state.value.invoiceHeaders.isNotEmpty()) {
                        state.value.invoiceHeaders.add(0, addedInv)
                    }
                    savePOSReceipt(
                        context,
                        addedInv,
                        print,
                        proceedToPrint,
                        callback
                    )
                } else {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                    }
                }
            } else {
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
                } else if (invoiceHeader.invoiceHeadOrderNo.isNullOrEmpty()) {
                    val lastOrderInv = invoiceHeaderRepository.getLastOrderByType()
                    invoiceHeader.invoiceHeadOrderNo = POSUtils.getInvoiceNo(
                        lastOrderInv?.invoiceHeadOrderNo ?: ""
                    )
                }
                val dataModel = invoiceHeaderRepository.update(invoiceHeader, print, finish)
                if (dataModel.succeed) {
                    val index =
                        state.value.invoiceHeaders.indexOfFirst { it.invoiceHeadId == invoiceHeader.invoiceHeadId }
                    if (index >= 0) {
                        state.value.invoiceHeaders.removeAt(index)
                        state.value.invoiceHeaders.add(
                            index,
                            invoiceHeader
                        )
                    } else if ((finish || invoiceHeader.invoiceHeadTaName.isNullOrEmpty()) && state.value.invoiceHeaders.isNotEmpty()) {
                        state.value.invoiceHeaders.add(
                            0,
                            invoiceHeader
                        )
                    }
                    savePOSReceipt(
                        context,
                        invoiceHeader,
                        print,
                        proceedToPrint,
                        callback
                    )
                } else {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                    }
                }
            }
        }
    }

    private suspend fun savePOSReceipt(
        context: Context,
        invoiceHeader: InvoiceHeader,
        print: Boolean,
        proceedToPrint: Boolean,
        callback: () -> Unit
    ) {
        val posReceipt = state.value.posReceipt
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
            print,
            proceedToPrint,
            callback
        )
    }

    private suspend fun saveInvoiceItems(
        context: Context,
        print: Boolean,
        proceedToPrint: Boolean,
        callback: () -> Unit
    ) {
        val itemsToInsert = state.value.invoiceItems.filter { it.invoice.isNew() }
        val itemsToUpdate = state.value.invoiceItems.filter { !it.invoice.isNew() }
        val itemsToDelete = itemsToDelete.filter { !it.invoice.isNew() }

        itemsToInsert.forEach { invoiceItem ->
            invoiceItem.invoice.invoiceHeaderId = state.value.invoiceHeader.invoiceHeadId
            saveInvoiceItem(
                invoiceItem,
                true
            )
        }

        itemsToUpdate.forEach { invoiceItem ->
            invoiceItem.invoice.invoiceHeaderId = state.value.invoiceHeader.invoiceHeadId
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
            isPayBottomSheetVisible.value = false
            showWarning("Invoice saved successfully.")
            if (proceedToPrint) {
                resetState()
                addReportResult(reportResults)
                showLoading(false)
                callback.invoke()
            } else if (SettingsModel.autoPrintTickets) {
                prepareAutoPrint(context) {
                    resetState()
                    showLoading(false)
                    if (sharedViewModel.isFromTable) {
                        sharedViewModel.isFromTable = false
                        callback.invoke()
                    }
                }
            } else {
                resetState()
                showLoading(false)
                if (sharedViewModel.isFromTable) {
                    sharedViewModel.isFromTable = false
                    callback.invoke()
                }
            }
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
        callback: () -> Unit
    ) {
        if (isInvoiceEdited) {
            showWarning("Save your changes at first!")
            return
        } else if (state.value.invoiceHeader.isNew()) {
            showWarning("Save invoice at first!")
            return
        }
        showLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            state.value.invoiceItems.forEach { invoiceItemModel ->
                invoiceItemModel.shouldPrint = true
            }
            state.value.invoiceHeader.invoiceHeadPrinted += 1
            invoiceHeaderRepository.updateInvoiceHeader(
                state.value.invoiceHeader
            )
            prepareInvoiceReports(context)
            withContext(Dispatchers.Main) {
                isPayBottomSheetVisible.value = false
                updateState(
                    state.value.copy(
                        invoiceItems = mutableListOf(),
                        invoiceHeader = InvoiceHeader(),
                        posReceipt = PosReceipt()
                    )
                )
                addReportResult(reportResults)
                resetState()
                showLoading(false)
                callback.invoke()
            }
        }

    }

    fun loadInvoiceFromTable() {
        sharedViewModel.tempInvoiceHeader?.let { invoiceHeader ->
            sharedViewModel.tempInvoiceHeader = null
            sharedViewModel.shouldLoadInvoice = false
            loadInvoiceDetails(invoiceHeader)
        }
    }

    fun loadInvoiceDetails(
        invoiceHeader: InvoiceHeader
    ) {
        if (invoiceHeader.invoiceHeadId.isEmpty()) {
            updateState(
                state.value.copy(
                    invoiceItems = mutableListOf(),
                    invoiceHeader = invoiceHeader,
                    posReceipt = PosReceipt()
                )
            )
            return
        }
        showLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            if (state.value.items.isEmpty()) {
                fetchItems(false)
            }
            if (state.value.families.isEmpty()) {
                fetchFamilies(false)
            }
            if (state.value.thirdParties.isEmpty()) {
                fetchThirdParties(false)
            }
            val result = invoiceRepository.getAllInvoices(invoiceHeader.invoiceHeadId)
            val invoices = mutableListOf<InvoiceItemModel>()
            result.forEach { inv ->
                invoices.add(InvoiceItemModel(invoice = inv,
                    initialQty = inv.invoiceQuantity,
                    invoiceItem = state.value.items.firstOrNull {
                        it.itemId.equals(
                            inv.invoiceItemId,
                            ignoreCase = true
                        )
                    } ?: Item()))
            }

            val posReceipt =
                posReceiptRepository.getPosReceiptByInvoice(invoiceHeader.invoiceHeadId)
            invoiceItemModels = invoices.toMutableList()
            currentInvoice = invoiceHeader.copy()
            withContext(Dispatchers.Main) {
                updateState(
                    state.value.copy(
                        invoiceItems = invoices,
                        invoiceHeader = POSUtils.refreshValues(
                            invoices,
                            invoiceHeader.copy()
                        ),
                        posReceipt = posReceipt ?: PosReceipt()
                    )
                )
                showLoading(false)
            }
        }
    }

    fun deleteInvoiceHeader(callback: () -> Unit) {
        val invoiceHeader = state.value.invoiceHeader
        if (invoiceHeader.isNew()) {
            resetState()
            if (sharedViewModel.isFromTable) {
                sharedViewModel.isFromTable = false
                callback.invoke()
            }
            return
        }
        showLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            val dataModel = invoiceHeaderRepository.delete(invoiceHeader)
            if (dataModel.succeed) {
                posReceiptRepository.delete(state.value.posReceipt)
                state.value.invoiceItems.forEach { invoiceItem ->
                    invoiceRepository.delete(invoiceItem.invoice)
                    invoiceItem.invoiceItem.itemRemQty += invoiceItem.invoice.invoiceQuantity
                    itemRepository.update(invoiceItem.invoiceItem)
                }
                val invoiceHeaders = state.value.invoiceHeaders.toMutableList()
                val index =
                    invoiceHeaders.indexOfFirst { it.invoiceHeadId == invoiceHeader.invoiceHeadId }
                if (index >= 0) {
                    invoiceHeaders.removeAt(index)
                }
                withContext(Dispatchers.Main) {
                    updateState(
                        state.value.copy(
                            invoiceHeaders = invoiceHeaders
                        )
                    )
                    resetState()
                    showLoading(false)
                    showWarning("successfully deleted.")
                    if (sharedViewModel.isFromTable) {
                        sharedViewModel.isFromTable = false
                        callback.invoke()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                }
            }
        }
    }

    fun unLockTable() {
        viewModelScope.launch(Dispatchers.IO) {
            invoiceHeaderRepository.unLockTable(
                state.value.invoiceHeader.invoiceHeadId,
                state.value.invoiceHeader.invoiceHeadTableId!!,
                state.value.invoiceHeader.invoiceHeadTableType
            )
        }
    }

    private suspend fun prepareInvoiceReports(
        context: Context
    ) {
        val invoiceHeader = state.value.invoiceHeader
        val defaultThirdParty =
            if (invoiceHeader.invoiceHeadThirdPartyName.isNullOrEmpty() || invoiceHeader.invoiceHeadThirdPartyName == state.value.selectedThirdParty.thirdPartyId) {
                state.value.selectedThirdParty
            } else if (invoiceHeader.invoiceHeadThirdPartyName == defaultThirdParty?.thirdPartyId) {
                defaultThirdParty
            } else {
                if (state.value.thirdParties.isEmpty()) {
                    thirdPartyRepository.getThirdPartyByID(invoiceHeader.invoiceHeadThirdPartyName!!)
                } else {
                    state.value.thirdParties.firstOrNull {
                        it.thirdPartyId == invoiceHeader.invoiceHeadThirdPartyName
                    }
                }
            }
        val user =
            if (invoiceHeader.invoiceHeadUserStamp.isNullOrEmpty() || SettingsModel.currentUser?.userId == invoiceHeader.invoiceHeadUserStamp || SettingsModel.currentUser?.userUsername == invoiceHeader.invoiceHeadUserStamp) {
                SettingsModel.currentUser
            } else {
                if (state.value.users.isEmpty()) {
                    userRepository.getUserById(invoiceHeader.invoiceHeadUserStamp!!)
                } else {
                    state.value.users.firstOrNull {
                        it.userId == invoiceHeader.invoiceHeadUserStamp || it.userUsername == invoiceHeader.invoiceHeadUserStamp
                    } ?: SettingsModel.currentUser
                }
            }

        val invoiceReport = PrinterUtils.getInvoiceReceiptHtmlContent(
            context,
            invoiceHeader,
            state.value.invoiceItems,
            state.value.posReceipt,
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

    private fun prepareAutoPrint(context: Context, callback: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val invoices =
                state.value.invoiceItems.filter { it.invoice.isNew() || it.shouldPrint }
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
            if (state.value.printers.isEmpty()) {
                state.value.printers = posPrinterRepository.getAllPosPrinters()
            }
            val itemsPrintersMap =
                state.value.invoiceItems.filter { it.shouldPrint || it.isDeleted }
                    .groupBy { it.invoiceItem.itemPrinter ?: "" }
            itemsPrintersMap.entries.forEach { entry ->
                if (entry.key.isNotEmpty()) {
                    val itemsPrinter =
                        state.value.printers.firstOrNull { it.posPrinterId == entry.key }
                    if (itemsPrinter != null) {
                        val reportResult = PrinterUtils.getItemReceiptHtmlContent(
                            context = context,
                            invoiceHeader = state.value.invoiceHeader,
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

    fun shouldLoadInvoice(): Boolean {
        return sharedViewModel.shouldLoadInvoice
    }

    fun fetchItemsAgain(): Boolean {
        return sharedViewModel.fetchItemsAgain
    }

    fun fetchThirdPartiesAgain(): Boolean {
        return sharedViewModel.fetchThirdPartiesAgain
    }

    fun isFromTable(): Boolean {
        return sharedViewModel.isFromTable
    }

    fun needAddedData(boolean: Boolean) {
        sharedViewModel.needAddedData = boolean
    }

    suspend fun updateRealItemPrice(item: Item, withLoading: Boolean = true) {
        sharedViewModel.updateRealItemPrice(item, withLoading)
    }

    fun logout() {
        sharedViewModel.logout()
    }

    fun launchBarcodeScanner() {
        viewModelScope.launch(Dispatchers.Main) {
            if (state.value.items.isEmpty()) {
                if (isInitiating) {
                    showWarning("Loading your items, try again later.")
                    return@launch
                }
                showLoading(true)
                withContext(Dispatchers.IO) {
                    fetchItems(true)
                }
            }
            sharedViewModel.launchBarcodeScanner(false,
                ArrayList(state.value.items),
                object : OnBarcodeResult {
                    override fun OnBarcodeResult(barcodesList: List<Any>) {
                        if (barcodesList.isNotEmpty()) {
                            showLoading(true)
                            viewModelScope.launch(Dispatchers.IO) {
                                val map: Map<Item, Int> =
                                    barcodesList.groupingBy { item -> item as Item }
                                        .eachCount()
                                val invoiceItems =
                                    state.value.invoiceItems.toMutableList()
                                map.forEach { (item, count) ->
                                    if (!item.itemBarcode.isNullOrEmpty()) {
                                        updateRealItemPrice(item, false)
                                        val invoiceItemModel =
                                            InvoiceItemModel()
                                        invoiceItemModel.setItem(item)
                                        invoiceItemModel.shouldPrint =
                                            true
                                        invoiceItemModel.invoice.invoiceQuantity =
                                            count.toDouble()
                                        invoiceItems.add(
                                            invoiceItemModel
                                        )
                                    }
                                }
                                withContext(Dispatchers.Main) {
                                    updateState(
                                        state.value.copy(
                                            invoiceItems = invoiceItems,
                                            invoiceHeader = POSUtils.refreshValues(
                                                invoiceItems,
                                                state.value.invoiceHeader
                                            )
                                        )
                                    )
                                    showLoading(false)
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
}