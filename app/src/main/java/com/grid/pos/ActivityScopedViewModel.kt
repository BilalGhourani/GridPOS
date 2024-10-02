package com.grid.pos

import android.content.Context
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
import com.grid.pos.interfaces.OnBarcodeResult
import com.grid.pos.interfaces.OnGalleryResult
import com.grid.pos.model.Event
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.PopupModel
import com.grid.pos.model.ReportResult
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.BaseViewModel
import com.grid.pos.utils.DataStoreManager
import com.grid.pos.utils.PrinterUtils
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
) : BaseViewModel() {
    private val _mainActivityEvent = Channel<ActivityScopedUIEvent>()
    val mainActivityEvent = _mainActivityEvent.receiveAsFlow()

    var posReceipt: PosReceipt = PosReceipt()
    var invoiceHeader: InvoiceHeader = InvoiceHeader()
    var pendingInvHeadState: InvoiceHeader? = null
    var invoiceItemModels: MutableList<InvoiceItemModel> = mutableListOf()
    var initialInvoiceItemModels: MutableList<InvoiceItemModel> = mutableListOf()
    var deletedInvoiceItems: MutableList<InvoiceItemModel> = mutableListOf()
    var shouldLoadInvoice: Boolean = false
    var isFromTable: Boolean = false
    var companies: MutableList<Company> = mutableListOf()
    private var localCompanies: MutableList<Company> = mutableListOf()
    var currencies: MutableList<Currency> = mutableListOf()
    var users: MutableList<User> = mutableListOf()
    var thirdParties: MutableList<ThirdParty> = mutableListOf()
    var families: MutableList<Family> = mutableListOf()
    var items: MutableList<Item> = mutableListOf()
    var invoiceHeaders: MutableList<InvoiceHeader> = mutableListOf()
    var printers: MutableList<PosPrinter> = mutableListOf()

    var isPaySlip: Boolean = true

    private val _activityState = MutableStateFlow(ActivityState())
    val activityState: MutableStateFlow<ActivityState> = _activityState

    suspend fun initiateValues() {
        if (SettingsModel.currentUser != null) {
            openConnectionIfNeeded()
            fetchSettings()
            fetchCompanies()
            fetchCurrencies()
            fetchPrinters()
        }
    }

    private suspend fun fetchSettings() {
        SettingsModel.siTransactionType = settingsRepository.getSalesInvoiceTransType()
        SettingsModel.rsTransactionType = settingsRepository.getReturnSalesTransType()
        SettingsModel.defaultBranch = settingsRepository.getDefaultBranch()
        SettingsModel.defaultWarehouse = settingsRepository.getDefaultWarehouse()
        SettingsModel.defaultThirdParty = thirdPartyRepository.getDefaultThirdParties()
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

    fun clearPosValues() {
        invoiceItemModels = mutableListOf()
        invoiceHeader = InvoiceHeader()
        posReceipt = PosReceipt()
        pendingInvHeadState = null
        shouldLoadInvoice = false
        isFromTable = false
    }

    fun print(
            context: Context,
            printInvoice: Boolean,
            reportResult: ReportResult? = null
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            val thirdParty = if (invoiceHeader.invoiceHeadThirdPartyName.isNullOrEmpty()) {
                thirdParties.firstOrNull { it.thirdPartyDefault }
            } else {
                thirdParties.firstOrNull {
                    it.thirdPartyId == invoiceHeader.invoiceHeadThirdPartyName
                }
            }
            val user = if (invoiceHeader.invoiceHeadUserStamp.isNullOrEmpty()) {
                null
            } else if (SettingsModel.currentUser?.userId.equals(invoiceHeader.invoiceHeadUserStamp)) {
                SettingsModel.currentUser
            } else {
                users.firstOrNull {
                    it.userId == invoiceHeader.invoiceHeadUserStamp
                }
            }
            val invoiceItems = invoiceItemModels.toMutableList()
            invoiceItems.addAll(deletedInvoiceItems)
            PrinterUtils.print(
                context,
                invoiceHeader,
                invoiceItems,
                posReceipt,
                thirdParty,
                user,
                SettingsModel.currentCompany,
                printers,
                reportResult,
                printInvoice
            )
        }
    }

    fun getInvoiceReceiptHtmlContent(context: Context): ReportResult {
        val defaultThirdParty = if (invoiceHeader.invoiceHeadThirdPartyName.isNullOrEmpty()) {
            thirdParties.firstOrNull { it.thirdPartyDefault }
        } else {
            thirdParties.firstOrNull {
                it.thirdPartyId.equals(
                    invoiceHeader.invoiceHeadThirdPartyName,
                    ignoreCase = true
                )
            }
        }
        return PrinterUtils.getInvoiceReceiptHtmlContent(
            context,
            invoiceHeader,
            invoiceItemModels,
            posReceipt,
            defaultThirdParty,
            SettingsModel.currentUser,
            SettingsModel.currentCompany
        )
    }

    fun finish() {
        viewModelScope.launch {
            _mainActivityEvent.send(ActivityScopedUIEvent.Finish)
        }
    }

    fun showLoading(show: Boolean) {
        viewModelScope.launch {
            _mainActivityEvent.send(ActivityScopedUIEvent.ShowLoading(show))
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

    fun isInvoiceItemQtyChanged(invoiceItemId:String,newQty:Double) : Boolean{
        if(invoiceItemId.isEmpty()){
            return false
        }
        initialInvoiceItemModels.forEach {
            if(it.invoice.invoiceId == invoiceItemId && it.invoice.invoiceQuantity != newQty){
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
        SettingsModel.defaultBranch = null
        SettingsModel.defaultWarehouse = null
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