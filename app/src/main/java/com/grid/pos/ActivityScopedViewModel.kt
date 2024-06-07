package com.grid.pos

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
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
import com.grid.pos.interfaces.OnGalleryResult
import com.grid.pos.model.Event
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.SettingsModel
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
        private val currencyRepository: CurrencyRepository,
        private val companyRepository: CompanyRepository,
        private val thirdPartyRepository: ThirdPartyRepository,
        private val familyRepository: FamilyRepository,
        private val itemRepository: ItemRepository,
        private val posPrinterRepository: PosPrinterRepository,
) : ViewModel() {
    private val _mainActivityEvent = Channel<ActivityScopedUIEvent>()
    val mainActivityEvent = _mainActivityEvent.receiveAsFlow()

    var posReceipt: PosReceipt = PosReceipt()
    var invoiceHeader: InvoiceHeader = InvoiceHeader()
    var pendingInvHeadState: InvoiceHeader? = null
    var invoiceItemModels: MutableList<InvoiceItemModel> = mutableListOf()
    var shouldPrintInvoice: Boolean = false
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

    fun print() {
        viewModelScope.launch(Dispatchers.IO) {
            PrinterUtils.print(
                invoiceHeader,
                invoiceItemModels,
                printers
            )
        }
    }

    fun getInvoiceReceiptHtmlContent(context: Context): String {
        return PrinterUtils.getInvoiceReceiptHtmlContent(context, invoiceHeader, invoiceItemModels)
    }

    fun finish() {
        viewModelScope.launch {
            _mainActivityEvent.send(ActivityScopedUIEvent.Finish)
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
            delegate: OnGalleryResult,
            onPermissionDenied: () -> Unit
    ) {
        viewModelScope.launch {
            _mainActivityEvent.send(
                ActivityScopedUIEvent.LaunchFilePicker(
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