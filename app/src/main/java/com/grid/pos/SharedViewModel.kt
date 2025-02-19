package com.grid.pos

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.FirebaseWrapper
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.data.currency.CurrencyRepository
import com.grid.pos.data.invoiceHeader.InvoiceHeader
import com.grid.pos.data.item.Item
import com.grid.pos.data.settings.SettingsRepository
import com.grid.pos.interfaces.OnBarcodeResult
import com.grid.pos.interfaces.OnGalleryResult
import com.grid.pos.model.PopupModel
import com.grid.pos.model.ReportCountry
import com.grid.pos.model.ReportResult
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.ToastModel
import com.grid.pos.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val currencyRepository: CurrencyRepository
) : ViewModel() {
    private val _mainActivityEvent = Channel<ActivityUIEvent>()
    val mainActivityEvent = _mainActivityEvent.receiveAsFlow()

    private var userPermissions: Map<String, String>? = null
    var isRegistering: Boolean = false

    var isLoggedIn: Boolean = false
    var forceLogout: Boolean = false
    var homeWarning: String? = null

    var tempInvoiceHeader: InvoiceHeader? = null
    var reportsToPrint: MutableList<ReportResult> = mutableListOf()
    var shouldLoadInvoice: Boolean = false
    var isFromTable: Boolean = false

    var isLoading: Boolean = false

    var reportCountries: MutableList<ReportCountry> = mutableListOf()

    var needAddedData = false
    var fetchItemsAgain = false
    var fetchThirdPartiesAgain = false

    var selectedReportType: String? = null

    init {
        FirebaseWrapper.initialize(this)
        SQLServerWrapper.initialize(this)
    }

    suspend fun initiateValues() {
        if (SettingsModel.currentUser != null) {
            if (SettingsModel.isConnectedToSqlServer()) {
                SQLServerWrapper.openConnection()
            }
            fetchCurrencies()
            fetchSettings()
        }
    }

    private suspend fun fetchSettings() {
        SettingsModel.defaultSqlServerBranch = settingsRepository.getDefaultBranch()
        SettingsModel.defaultSqlServerWarehouse = settingsRepository.getDefaultWarehouse()
        userPermissions =
            settingsRepository.getUserPermissions(SettingsModel.currentUser?.userUsername ?: "")
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
            val currencies = currencyRepository.getAllCurrencies()
            if (currencies.isNotEmpty())
                SettingsModel.currentCurrency = currencies[0]
        }
    }


    suspend fun updateRealItemPrice(item: Item, withLoading: Boolean) {
        if (item.itemCurrencyId.isNullOrEmpty()) return
        val currency = SettingsModel.currentCurrency ?: return
        when (item.itemCurrencyId) {
            currency.currencyDocumentId, currency.currencyCode2 -> {//second currency
                item.itemRealUnitPrice = item.itemUnitPrice.div(currency.currencyRate)
                item.itemRealOpenCost = item.itemOpenCost.div(currency.currencyRate)
            }

            currency.currencyId, currency.currencyCode1 -> {//first currency
                item.itemRealUnitPrice = item.itemUnitPrice
                item.itemRealOpenCost = item.itemOpenCost
            }

            else -> {
                if (withLoading) {
                    withContext(Dispatchers.Main) {
                        showLoading(true)
                    }
                }
                val rate = currencyRepository.getRate(
                    currency.currencyId,
                    item.itemCurrencyId
                )
                item.itemRealUnitPrice = item.itemUnitPrice.div(rate)
                item.itemRealOpenCost = item.itemOpenCost.div(rate)
                if (withLoading) {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                    }
                }
            }
        }
    }

    fun finish() {
        viewModelScope.launch {
            _mainActivityEvent.send(ActivityUIEvent.Finish)
        }
    }

    fun showLoading(
        show: Boolean,
        timeout: Long = 30000
    ) {
        isLoading = show
        viewModelScope.launch {
            _mainActivityEvent.send(
                ActivityUIEvent.ShowLoading(
                    show,
                    timeout
                )
            )
        }
    }

    fun showToastMessage(toastModel: ToastModel) {
        viewModelScope.launch {
            _mainActivityEvent.send(
                ActivityUIEvent.ShowToastMessage(toastModel)
            )
        }
    }

    fun showPopup(
        show: Boolean,
        popupModel: PopupModel?
    ) {
        viewModelScope.launch {
            _mainActivityEvent.send(
                ActivityUIEvent.ShowPopup(
                    show,
                    popupModel
                )
            )
        }
    }

    fun openAppStorageSettings() {
        viewModelScope.launch {
            _mainActivityEvent.send(ActivityUIEvent.OpenAppSettings)
        }
    }

    fun launchGalleryPicker(
        mediaType: ActivityResultContracts.PickVisualMedia.VisualMediaType,
        delegate: OnGalleryResult,
        onPermissionDenied: () -> Unit
    ) {
        viewModelScope.launch {
            _mainActivityEvent.send(
                ActivityUIEvent.LaunchGalleryPicker(
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
                ActivityUIEvent.LaunchFilePicker(
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
            _mainActivityEvent.send(ActivityUIEvent.StartChooserActivity(intent))
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
                ActivityUIEvent.LaunchBarcodeScanner(
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
                ActivityUIEvent.ChangeAppOrientation(
                    orientationType
                )
            )
        }
    }

    fun checkPermission(permission: String): Boolean {
        if (SettingsModel.isConnectedToSqlServer()) {
            val value = userPermissions?.getValue(permission)
            return value.equals("yes", ignoreCase = true)
        }
        return true
    }


    fun logout() {
        isLoggedIn = false
        SettingsModel.currentUser = null
        SettingsModel.currentCompany = null
        SettingsModel.currentCurrency = null
        SettingsModel.defaultSqlServerBranch = null
        SettingsModel.defaultSqlServerWarehouse = null

        userPermissions = null
        tempInvoiceHeader = null
        shouldLoadInvoice = false
        isFromTable = false
        viewModelScope.launch(Dispatchers.IO) {
            if (SettingsModel.isConnectedToSqlServer()) {
                SQLServerWrapper.closeConnection()
            }
        }
    }

    fun copyToInternalStorage(
        context: Context,
        uri: Uri,
        parent: String,
        fileName: String,
        callback: (String?) -> Unit
    ) {
        showLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            val internalPath =
                FileUtils.saveToExternalStorage(
                    context = context,
                    parent = parent,
                    sourceFilePath = uri,
                    destName = fileName,
                )
            withContext(Dispatchers.Main) {
                showLoading(false)
                callback.invoke(internalPath)
            }
        }
    }
}