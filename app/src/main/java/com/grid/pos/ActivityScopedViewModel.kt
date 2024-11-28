package com.grid.pos

import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.Company.Company
import com.grid.pos.data.Company.CompanyRepository
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.Currency.CurrencyRepository
import com.grid.pos.data.Family.Family
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.Item.Item
import com.grid.pos.data.PosPrinter.PosPrinter
import com.grid.pos.data.PosPrinter.PosPrinterRepository
import com.grid.pos.data.PosReceipt.PosReceipt
import com.grid.pos.data.Settings.SettingsRepository
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.data.ThirdParty.ThirdPartyRepository
import com.grid.pos.data.User.User
import com.grid.pos.data.User.UserRepository
import com.grid.pos.interfaces.OnBarcodeResult
import com.grid.pos.interfaces.OnGalleryResult
import com.grid.pos.model.Event
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.PopupModel
import com.grid.pos.model.ReportCountry
import com.grid.pos.model.ReportResult
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.BaseViewModel
import com.grid.pos.utils.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ActivityScopedViewModel @Inject constructor(
        private val settingsRepository: SettingsRepository,
        private val currencyRepository: CurrencyRepository,
        private val companyRepository: CompanyRepository,
        private val thirdPartyRepository: ThirdPartyRepository,
        private val posPrinterRepository: PosPrinterRepository,
        private val userRepository: UserRepository
) : BaseViewModel() {
    private val _mainActivityEvent = Channel<ActivityScopedUIEvent>()
    val mainActivityEvent = _mainActivityEvent.receiveAsFlow()

    var isRegistering: Boolean = false

    var posReceipt: PosReceipt = PosReceipt()
    var invoiceHeader: InvoiceHeader = InvoiceHeader()
    var pendingInvHeadState: InvoiceHeader? = null
    var reportsToPrint: MutableList<ReportResult> = mutableListOf()
    var invoiceItemModels: MutableList<InvoiceItemModel> = mutableListOf()
    var initialInvoiceItemModels: MutableList<InvoiceItemModel> = mutableListOf()
    var deletedInvoiceItems: MutableList<InvoiceItemModel> = mutableListOf()
    var shouldLoadInvoice: Boolean = false
    var isFromTable: Boolean = false
    var companies: MutableList<Company> = mutableListOf()
    var currencies: MutableList<Currency> = mutableListOf()
    var users: MutableList<User> = mutableListOf()
    var thirdParties: MutableList<ThirdParty> = mutableListOf()
    var families: MutableList<Family> = mutableListOf()
    var items: MutableList<Item> = mutableListOf()
    var invoiceHeaders: MutableList<InvoiceHeader> = mutableListOf()
    var printers: MutableList<PosPrinter> = mutableListOf()
    var reportCountries: MutableList<ReportCountry> = mutableListOf()

    var selectedReportType: String? = null

    private val _activityState = MutableStateFlow(ActivityState())
    val activityState: MutableStateFlow<ActivityState> = _activityState

    suspend fun initiateValues() {
        if (SettingsModel.currentUser != null) {
            openConnectionIfNeeded()
            fetchCompanies()
            fetchCurrencies()
            fetchSettings()
            fetchPrinters()
        }
    }

    private suspend fun fetchSettings() {
        SettingsModel.siTransactionType = settingsRepository.getTransactionTypeId("Sale Invoice") ?: "null"
        SettingsModel.rsTransactionType = settingsRepository.getTransactionTypeId("Return Sale") ?: "null"
        SettingsModel.pvTransactionType = settingsRepository.getTransactionTypeId(
            if (SettingsModel.isSqlServerWebDb) "Payment Voucher" else "Payment"
        )
        SettingsModel.rvTransactionType = settingsRepository.getTransactionTypeId(
            if (SettingsModel.isSqlServerWebDb) "Receipt Voucher" else "Receipt"
        )
        SettingsModel.defaultSqlServerBranch = settingsRepository.getDefaultBranch()
        SettingsModel.defaultSqlServerWarehouse = settingsRepository.getDefaultWarehouse()
        SettingsModel.defaultThirdParty = thirdPartyRepository.getDefaultThirdParty()
        val currency = SettingsModel.currentCurrency ?: return

        SettingsModel.posReceiptAccCashId = settingsRepository.getPosReceiptAccIdBy(
            "Cash",
            currency.currencyId
        )
        SettingsModel.posReceiptAccCash1Id = settingsRepository.getPosReceiptAccIdBy(
            "Cash",
            currency.currencyDocumentId ?: ""
        )
        SettingsModel.posReceiptAccCreditId = settingsRepository.getPosReceiptAccIdBy(
            "Credit Card",
            currency.currencyId
        )
        SettingsModel.posReceiptAccCredit1Id = settingsRepository.getPosReceiptAccIdBy(
            "Credit Card",
            currency.currencyDocumentId ?: ""
        )
        SettingsModel.posReceiptAccDebitId = settingsRepository.getPosReceiptAccIdBy(
            "Debit Card",
            currency.currencyId
        )
        SettingsModel.posReceiptAccDebit1Id = settingsRepository.getPosReceiptAccIdBy(
            "Debit Card",
            currency.currencyDocumentId ?: ""
        )
    }

    fun fetchCountries(onResult: (MutableList<ReportCountry>) -> Unit) {
        if (reportCountries.isEmpty()) {
            showLoading(true)
            viewModelScope.launch(Dispatchers.IO) {
                reportCountries = settingsRepository.getCountries()
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    onResult.invoke(reportCountries)
                }
            }
        } else {
            onResult.invoke(reportCountries)
        }
    }

    private suspend fun fetchCurrencies() {
        if (SettingsModel.currentCurrency == null) {
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
        }
    }

    private suspend fun fetchCompanies() {
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

    private suspend fun fetchPrinters() {
        printers = posPrinterRepository.getAllPosPrinters()
    }

    suspend fun updateRealItemPrice(item: Item): Double {
        if (item.itemRealUnitPrice > 0.0 || item.itemCurrencyId.isNullOrEmpty()) {
            return item.itemRealUnitPrice
        }
        val currency = SettingsModel.currentCurrency ?: return item.itemUnitPrice
        when (item.itemCurrencyId) {
            currency.currencyDocumentId, currency.currencyCode2 -> {//second currency
                item.itemRealUnitPrice = item.itemUnitPrice.div(currency.currencyRate)
            }

            currency.currencyId, currency.currencyCode1 -> {//first currency
                item.itemRealUnitPrice = item.itemUnitPrice
            }

            else -> {
                withContext(Dispatchers.Main) {
                    showLoading(true)
                }
                val rate = currencyRepository.getRate(
                    currency.currencyId,
                    item.itemCurrencyId!!
                )
                item.itemRealUnitPrice = item.itemUnitPrice.div(rate)
                withContext(Dispatchers.Main) {
                    showLoading(false)
                }
            }
        }
        return item.itemRealUnitPrice
    }

    fun clearPosValues() {
        invoiceItemModels = mutableListOf()
        initialInvoiceItemModels = mutableListOf()
        deletedInvoiceItems = mutableListOf()
        invoiceHeader = InvoiceHeader()
        posReceipt = PosReceipt()
        pendingInvHeadState = null
        shouldLoadInvoice = false
        isFromTable = false
        reportsToPrint = mutableListOf()
    }

    fun finish() {
        viewModelScope.launch {
            _mainActivityEvent.send(ActivityScopedUIEvent.Finish)
        }
    }

    fun showLoading(
            show: Boolean,
            timeout: Long = 30000
    ) {
        viewModelScope.launch {
            _mainActivityEvent.send(
                ActivityScopedUIEvent.ShowLoading(
                    show,
                    timeout
                )
            )
        }
    }

    fun showPopup(
            show: Boolean,
            popupModel: PopupModel?
    ) {
        viewModelScope.launch {
            _mainActivityEvent.send(
                ActivityScopedUIEvent.ShowPopup(
                    show,
                    popupModel
                )
            )
        }
    }

    fun openAppStorageSettings() {
        viewModelScope.launch {
            _mainActivityEvent.send(ActivityScopedUIEvent.OpenAppSettings)
        }
    }

    fun launchGalleryPicker(
            mediaType: ActivityResultContracts.PickVisualMedia.VisualMediaType,
            delegate: OnGalleryResult,
            onPermissionDenied: () -> Unit
    ) {
        viewModelScope.launch {
            _mainActivityEvent.send(
                ActivityScopedUIEvent.LaunchGalleryPicker(
                    mediaType,
                    delegate,
                    onPermissionDenied
                )
            )
        }
    }

    fun launchFilePicker(
            intentType: String,
            delegate: OnGalleryResult,
            onPermissionDenied: () -> Unit
    ) {
        viewModelScope.launch {
            _mainActivityEvent.send(
                ActivityScopedUIEvent.LaunchFilePicker(
                    intentType,
                    delegate,
                    onPermissionDenied
                )
            )
        }
    }

    fun startChooserActivity(
            intent: Intent
    ) {
        viewModelScope.launch {
            _mainActivityEvent.send(ActivityScopedUIEvent.StartChooserActivity(intent))
        }
    }

    fun launchBarcodeScanner(
            scanToAdd: Boolean,
            items: ArrayList<Item>?,
            delegate: OnBarcodeResult,
            onPermissionDenied: () -> Unit
    ) {
        viewModelScope.launch {
            _mainActivityEvent.send(
                ActivityScopedUIEvent.LaunchBarcodeScanner(
                    scanToAdd,
                    items,
                    delegate,
                    onPermissionDenied
                )
            )
        }
    }

    fun changeAppOrientation(orientationType: String) {
        viewModelScope.launch {
            _mainActivityEvent.send(
                ActivityScopedUIEvent.ChangeAppOrientation(
                    orientationType
                )
            )
        }
    }

    fun isLoggedIn(): Boolean {
        return activityState.value.isLoggedIn
    }

    fun isInvoiceItemQtyChanged(
            invoiceItemId: String,
            newQty: Double
    ): Boolean {
        if (invoiceItemId.isEmpty()) {
            return false
        }
        initialInvoiceItemModels.forEach {
            if (it.invoice.invoiceId == invoiceItemId && it.invoice.invoiceQuantity != newQty) {
                return true
            }
        }
        return false
    }

    fun logout() {
        activityState.value.isLoggedIn = false
        SettingsModel.currentUser = null
        SettingsModel.currentUserId = null
        SettingsModel.currentCompany = null
        SettingsModel.currentCurrency = null
        SettingsModel.defaultSqlServerBranch = null
        SettingsModel.defaultSqlServerWarehouse = null
        posReceipt = PosReceipt()
        invoiceHeader = InvoiceHeader()
        pendingInvHeadState = null
        invoiceItemModels.clear()
        shouldLoadInvoice = false
        isFromTable = false
        companies.clear()
        currencies.clear()
        users.clear()
        thirdParties.clear()
        families.clear()
        invoiceHeaders.clear()
        printers.clear()
        closeConnectionIfNeeded()
    }
}