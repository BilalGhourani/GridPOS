package com.grid.pos

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.Company.Company
import com.grid.pos.data.Company.CompanyRepository
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.Currency.CurrencyRepository
import com.grid.pos.data.Family.Family
import com.grid.pos.data.Family.FamilyRepository
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.Item.Item
import com.grid.pos.data.Item.ItemRepository
import com.grid.pos.data.PosPrinter.PosPrinter
import com.grid.pos.data.PosPrinter.PosPrinterRepository
import com.grid.pos.data.PosReceipt.PosReceipt
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.data.ThirdParty.ThirdPartyRepository
import com.grid.pos.data.User.User
import com.grid.pos.data.User.UserRepository
import com.grid.pos.model.Event
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DataStoreManager
import com.grid.pos.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ActivityScopedViewModel @Inject constructor(
        private val currencyRepository: CurrencyRepository,
        private val companyRepository: CompanyRepository,
        private val userRepository: UserRepository,
        private val thirdPartyRepository: ThirdPartyRepository,
        private val familyRepository: FamilyRepository,
        private val itemRepository: ItemRepository,
        private val posPrinterRepository: PosPrinterRepository,
) : ViewModel() {
    var posReceipt: PosReceipt = PosReceipt()
    var invoiceHeader: InvoiceHeader = InvoiceHeader()
    var pendingInvHeadState: InvoiceHeader? = null
    var invoiceItemModels: MutableList<InvoiceItemModel> = mutableListOf()
    var shouldPrintInvoice: Boolean = false
    var shouldLoadInvoice: Boolean = false
    var isFromTable: Boolean = false
    var shouldUpdateLists: Boolean = false
    var companies: MutableList<Company> = mutableListOf()
    var localCompanies: MutableList<Company> = mutableListOf()
    var currencies: MutableList<Currency> = mutableListOf()
    var users: MutableList<User> = mutableListOf()
    var thirdParties: MutableList<ThirdParty> = mutableListOf()
    var families: MutableList<Family> = mutableListOf()
    var items: MutableList<Item> = mutableListOf()
    var invoiceHeaders: MutableList<InvoiceHeader> = mutableListOf()
    var printers: MutableList<PosPrinter> = mutableListOf()

    private val _activityState = MutableStateFlow(ActivityState())
    val activityState: MutableStateFlow<ActivityState> = _activityState

    suspend fun initiateValues() {
        if (SettingsModel.currentUser != null) {
            fetchCompanies()
            fetchCurrencies()
            fetchThirdParties()
            fetchFamilies()
            fetchItems()
            fetchPrinters()
        }
    }

    private suspend fun fetchCurrencies() {
        if (SettingsModel.currentCurrency == null) {
            viewModelScope.launch(Dispatchers.IO) {
                currencies = currencyRepository.getAllCurrencies()
                currencies.forEach {
                    if (it.currencyCompId.equals(
                            SettingsModel.getCompanyID(),
                            ignoreCase = true
                        )
                    ) {
                        SettingsModel.currentCurrency = it
                    }
                }
                if (SettingsModel.currentCurrency == null) {
                    SettingsModel.currentCurrency = Currency()
                }
            }
        }
    }

    fun getLocalCompanies(onResult: (MutableList<Company>) -> Unit) {
        if (localCompanies.isEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                localCompanies = companyRepository.getLocalCompanies()
                withContext(Dispatchers.IO) {
                    onResult.invoke(localCompanies)
                }
            }
        } else {
            onResult.invoke(localCompanies)
        }
    }

    private suspend fun fetchCompanies() {
        viewModelScope.launch(Dispatchers.IO) {
            companies = companyRepository.getAllCompanies()
            companies.forEach {
                if (it.companyId.equals(
                        SettingsModel.getCompanyID(),
                        ignoreCase = true
                    )
                ) {
                    SettingsModel.currentCompany = it
                }
            }
            if (SettingsModel.currentCompany?.companySS == true) {
                viewModelScope.launch(Dispatchers.IO) {
                    SettingsModel.currentUserId = null
                    SettingsModel.currentUser = null
                    DataStoreManager.removeKey(
                        DataStoreManager.DataStoreKeys.CURRENT_USER_ID.key
                    )
                    withContext(Dispatchers.Main) {
                        activityState.value = activityState.value.copy(
                            isLoggedIn = false,
                            warning = Event(SettingsModel.companyAccessWarning),
                            forceLogout = true
                        )
                    }
                }
            }
        }
    }

    private suspend fun fetchThirdParties() {
        thirdParties = thirdPartyRepository.getAllThirdParties()
    }

    private suspend fun fetchItems() {
        items = itemRepository.getAllItems()
    }

    private suspend fun fetchFamilies() {
        families = familyRepository.getAllFamilies()
    }

    private suspend fun fetchPrinters() {
        printers = posPrinterRepository.getAllPosPrinters()
    }

    fun clearPosValues() {
        invoiceItemModels = mutableListOf()
        invoiceHeader = InvoiceHeader()
        posReceipt = PosReceipt()
        shouldPrintInvoice = true
        pendingInvHeadState = null
        shouldLoadInvoice = false
        isFromTable = false
    }

    fun getInvoiceReceiptHtmlContent(
            context: Context,
            content: String = Utils.readFileFromAssets(
                "invoice_receipt.html",
                context
            )
    ): String {
        var result = content.ifEmpty { Utils.getDefaultReceipt() }
        if (invoiceItemModels.isNotEmpty()) {
            val trs = StringBuilder("")
            invoiceItemModels.forEach { item ->
                trs.append(
                    "<tr> <td>${item.getName()}</td>  <td>${
                        String.format(
                            "%.2f",
                            item.getQuantity()
                        )
                    }</td> <td>$${
                        String.format(
                            "%.2f",
                            item.getNetAmount()
                        )
                    }</td>  </tr>"
                )
            }
            result = result.replace(
                "{rows_content}",
                trs.toString()
            )
            result = result.replace(
                "{total}",
                String.format(
                    "%.2f",
                    invoiceHeader.invoiceHeadGrossAmount
                )
            )
        }
        return result
    }

    fun getItemReceiptHtmlContent(
            context: Context,
            content: String = Utils.readFileFromAssets(
                "item_receipt.html",
                context
            ),
            invoiceHeader: InvoiceHeader,
            invItemModels: List<InvoiceItemModel>
    ): String {
        var result = content.ifEmpty { Utils.getDefaultItemReceipt() }
        result = result.replace(
            "{table_name}",
            invoiceHeader.invoiceHeadTaName ?: ""
        )
        result = result.replace(
            "{order_no}",
            invoiceHeader.invoiceHeadOrderNo ?: ""
        )
        result = result.replace(
            "{trans_no}",
            invoiceHeader.invoiceHeadTransNo ?: ""
        )

        result = result.replace(
            "{invoice_time}",
            Utils.getDateinFormat(
                invoiceHeader.invoiceHeadTimeStamp ?: Date(
                    invoiceHeader.invoiceHeadDateTime.div(
                        1000
                    )
                ),
                "dd/MM/yyyy hh:mm:ss"
            )
        )
        if (invItemModels.isNotEmpty()) {
            val trs = StringBuilder("")
            invoiceItemModels.forEach { item ->
                trs.append(
                    "<tr> <td>${
                        String.format(
                            "%.2f",
                            item.getQuantity()
                        )
                    }</td> <td>${item.getName()}</td>  </tr>"
                )
            }
            result = result.replace(
                "{rows_content}",
                trs.toString()
            )
        }
        return result
    }

    fun print() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = App.getInstance().applicationContext
            SettingsModel.currentCompany?.companyPrinterId?.let { companyPrinter ->
                val invoicePrinter = printers.firstOrNull { it.posPrinterId == companyPrinter }
                if (invoicePrinter != null) {
                    val invoiceContent = getInvoiceReceiptHtmlContent(context = context)
                    Utils.printInvoice(
                        invoiceContent,
                        invoicePrinter.posPrinterHost,
                        invoicePrinter.posPrinterPort
                    )
                }
            }

            val itemsPrintersMap = invoiceItemModels.groupBy { it.invoiceItem.itemPrinter ?: "" }
            itemsPrintersMap.entries.forEach { entry ->
                if (entry.key.isNotEmpty()) {
                    val itemsPrinter = printers.firstOrNull { it.posPrinterId == entry.key }
                    if (itemsPrinter != null) {
                        val invoiceContent = getItemReceiptHtmlContent(
                            context = context,
                            invoiceHeader = invoiceHeader,
                            invItemModels = entry.value
                        )
                        Utils.printInvoice(
                            invoiceContent,
                            itemsPrinter.posPrinterHost,
                            itemsPrinter.posPrinterPort
                        )
                    }
                }
            }
        }
    }

    fun isLoggedIn(): Boolean {
        return activityState.value.isLoggedIn
    }

    fun logout() {
        activityState.value.isLoggedIn = false
        SettingsModel.currentUser = null
        SettingsModel.currentUserId = null
        posReceipt = PosReceipt()
        invoiceHeader = InvoiceHeader()
        pendingInvHeadState = null
        invoiceItemModels.clear()
        shouldPrintInvoice = false
        shouldLoadInvoice = false
        isFromTable = false
        companies.clear()
        currencies.clear()
        users.clear()
        thirdParties.clear()
        families.clear()
        invoiceHeaders.clear()
        printers.clear()
    }
}