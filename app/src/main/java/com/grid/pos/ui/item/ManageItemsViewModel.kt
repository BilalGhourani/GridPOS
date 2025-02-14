package com.grid.pos.ui.item

import android.content.Context
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewModelScope
import com.grid.pos.SharedViewModel
import com.grid.pos.data.currency.CurrencyRepository
import com.grid.pos.data.family.FamilyRepository
import com.grid.pos.data.invoice.InvoiceRepository
import com.grid.pos.data.item.Item
import com.grid.pos.data.item.ItemRepository
import com.grid.pos.data.posPrinter.PosPrinterRepository
import com.grid.pos.data.settings.SettingsRepository
import com.grid.pos.interfaces.OnBarcodeResult
import com.grid.pos.interfaces.OnGalleryResult
import com.grid.pos.model.ItemGroupModel
import com.grid.pos.model.PopupModel
import com.grid.pos.model.ReportResult
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.ToastModel
import com.grid.pos.ui.common.BaseViewModel
import com.grid.pos.utils.PrinterUtils
import com.grid.pos.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ManageItemsViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val familyRepository: FamilyRepository,
    private val posPrinterRepository: PosPrinterRepository,
    private val currencyRepository: CurrencyRepository,
    private val invoiceRepository: InvoiceRepository,
    private val settingsRepository: SettingsRepository,
    private val sharedViewModel: SharedViewModel
) : BaseViewModel() {

    private val _state = MutableStateFlow(ManageItemsState())
    val state: MutableStateFlow<ManageItemsState> = _state


    var currentITem: Item = Item()
    var oldImage: String? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
            fetchCurrencies()
            fillGroups()
        }
    }

    fun resetState() {
        val firstCurrency = getFirstCurrency()
        currentITem = Item().copy(
            itemTax = SettingsModel.currentCompany?.companyTax ?: 0.0,
            itemTax1 = SettingsModel.currentCompany?.companyTax1 ?: 0.0,
            itemTax2 = SettingsModel.currentCompany?.companyTax2 ?: 0.0,
            itemGroup = if (state.value.groups.isNotEmpty()) {
                state.value.groups[0].groupName
            } else {
                ""
            },
            itemCurrencyId = firstCurrency.first,
            itemCurrencyCode = firstCurrency.second,
        )
        state.value = state.value.copy(
            item = currentITem.copy(),
            clear = false
        )
    }

    fun updateState(newState: ManageItemsState) {
        state.value = newState
    }

    fun isAnyChangeDone(): Boolean {
        return state.value.item.didChanged(currentITem)
    }

    fun shouldDisableCostAndQty(): Boolean {
        return state.value.isConnectingToSQLServer && state.value.item.itemId.isNotEmpty()
    }

    fun fetchItems() {
        showLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            val listOfItems = itemRepository.getAllItems()
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    items = listOfItems
                )
                showLoading(false)
            }
        }
    }

    fun selectionPrerequisite(succeeded: () -> Unit) {
        val familiesSize = state.value.families.size
        val printersSize = state.value.printers.size
        if (familiesSize == 0 || printersSize == 0) {
            viewModelScope.launch(Dispatchers.IO) {
                if (familiesSize == 0) {
                    fetchFamilies(false)
                }
                if (printersSize == 0) {
                    fetchPrinters(false)
                }
                withContext(Dispatchers.Main) {
                    succeeded.invoke()
                }
            }
        } else {
            succeeded.invoke()
        }
    }

    fun fetchFamilies(loading: Boolean = true) {
        if (loading) {
            sharedViewModel.showLoading(true)
        }
        viewModelScope.launch(Dispatchers.IO) {
            val listOfFamilies = familyRepository.getAllFamilies()
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    families = listOfFamilies
                )
                if (loading) {
                    sharedViewModel.showLoading(false)
                }
            }
        }
    }

    private suspend fun fetchCurrencies() {
        val currencies = currencyRepository.getAllCurrencyModels()
        val firstCurrency = getFirstCurrency()
        withContext(Dispatchers.Main) {
            state.value = state.value.copy(
                currencies = currencies,
                item = currentITem.copy(
                    itemCurrencyId = firstCurrency.first,
                    itemCurrencyCode = firstCurrency.second,
                )
            )
        }
    }

    fun fetchPrinters(loading: Boolean = true) {
        if (loading) {
            sharedViewModel.showLoading(true)
        }
        viewModelScope.launch(Dispatchers.IO) {
            val listOfPrinters = posPrinterRepository.getAllPosPrinters()
            withContext(Dispatchers.Main) {
                updateState(
                    state.value.copy(
                        printers = listOfPrinters
                    )
                )
                if (loading) {
                    sharedViewModel.showLoading(false)
                }
            }
        }
    }

    private suspend fun fillGroups() {
        val isConnectedToSQL = SettingsModel.isConnectedToSqlServer()
        val groups = mutableListOf(
            ItemGroupModel("Stock"),
            ItemGroupModel("Mixed Product"),
            ItemGroupModel("Finished Product"),
            ItemGroupModel("Pack"),
            ItemGroupModel("Set"),
            ItemGroupModel("Non Stock"),
        )
        withContext(Dispatchers.Main) {
            state.value = state.value.copy(
                groups = groups,
                item = currentITem.copy(
                    itemGroup = groups[0].groupName
                ),
                isConnectingToSQLServer = isConnectedToSQL
            )
        }
    }

    fun isLoading(): Boolean {
        return sharedViewModel.isLoading
    }

    fun showPopup(popupModel: PopupModel) {
        sharedViewModel.showPopup(true, popupModel)
    }

    fun showLoading(boolean: Boolean) {
        sharedViewModel.showLoading(boolean)
    }

    fun addReportResult(reportResults: List<ReportResult>){
        sharedViewModel.reportsToPrint.clear()
        sharedViewModel.reportsToPrint.addAll(reportResults)
    }

    fun showWarning(
        warning: String,
        action: String? = null,
        onActionClicked: () -> Unit = {}
    ) {
        sharedViewModel.showToastMessage(
            ToastModel(message = warning, actionButton = action, onActionClick = onActionClicked)
        )
    }

    fun save() {
        val item = state.value.item
        if (item.itemName.isNullOrEmpty() || item.itemFaId.isNullOrEmpty()) {
            showWarning("Please fill item name and family")
            return
        }
        if (state.value.isConnectingToSQLServer && item.itemGroup.isNullOrEmpty()) {
            showWarning("Please select an item group")
            return
        }
        showLoading(true)
        val isInserting = item.isNew()
        item.itemUnitPrice = Utils.roundDoubleValue(
            item.itemUnitPrice,
            SettingsModel.currentCurrency?.currencyName1Dec
        )
        item.itemOpenCost = Utils.roundDoubleValue(
            item.itemOpenCost,
            SettingsModel.currentCurrency?.currencyName1Dec
        )
        CoroutineScope(Dispatchers.IO).launch {
            if (isInserting) {
                item.prepareForInsert()
                val dataModel = itemRepository.insert(item)
                if (dataModel.succeed) {
                    val addedModel = dataModel.data as Item
                    val items = state.value.items
                    if (items.isNotEmpty()) {
                        items.add(addedModel)
                    }
                    if (sharedViewModel.needAddedData) {
                        sharedViewModel.needAddedData = false
                        sharedViewModel.fetchItemsAgain = true
                    }
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            items = items,
                            clear = true
                        )
                    }
                    showLoading(false)
                    showWarning("Item saved successfully.")
                } else {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                    }
                }
            } else {
                val updateShowInPOS = currentITem.itemPos != item.itemPos
                val dataModel = itemRepository.update(item, updateShowInPOS)
                if (dataModel.succeed) {
                    val items = state.value.items.toMutableList()
                    val index = items.indexOfFirst { it.itemId == item.itemId }
                    if (index >= 0) {
                        items.removeAt(index)
                        items.add(
                            index,
                            item
                        )
                    }
                    if (sharedViewModel.needAddedData) {
                        sharedViewModel.needAddedData = false
                        sharedViewModel.fetchItemsAgain = true
                    }
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            items = items,
                            clear = true
                        )
                        showLoading(false)
                        showWarning("Item saved successfully.")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                    }
                }
            }
        }
    }

    fun delete() {
        val item = state.value.item
        if (item.itemId.isEmpty()) {
            showWarning("Please select an Item to delete")
            return
        }
        showLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            if (hasRelations(item.itemId)) {
                showLoading(false)
                showWarning("You can't delete this Item,because it has related data!")
                return@launch
            }
            val dataModel = itemRepository.delete(item)
            if (dataModel.succeed) {
                val items = state.value.items
                items.remove(item)
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        items = items,
                        clear = true
                    )
                    showLoading(false)
                    showWarning("successfully deleted.")
                }
            } else {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                }
            }
        }
    }

    suspend fun generateBarcode(): String {
        return itemRepository.generateBarcode()
    }

    suspend fun prepareItemBarcodeReport(
        context: Context,
        item: Item
    ): ReportResult {
        if (item.itemCurrencyId.isNullOrEmpty()) {
            val firstCurr = getFirstCurrency()
            item.itemCurrencyId = firstCurr.first
            item.itemCurrencyCode = firstCurr.second
        } else if (item.itemCurrencyCode.isNullOrEmpty()) {
            item.itemCurrencyCode =
                state.value.currencies.firstOrNull { it.currencyId == item.itemCurrencyId }?.currencyCode
        }
        val family = if (!item.itemFaId.isNullOrEmpty()) {
            state.value.families.firstOrNull { it.familyId == item.itemFaId } ?: run {
                familyRepository.getFamilyById(item.itemFaId!!)
            }
        } else {
            null
        }
        var itemColorName = item.itemColor
        var itemSizeName = item.itemSize
        var itemBranchName = item.itemBranchName
        if (SettingsModel.isConnectedToSqlServer() && SettingsModel.isSqlServerWebDb) {
            if (!itemSizeName.isNullOrEmpty()) {
                itemSizeName = settingsRepository.getSizeById(itemSizeName)
            }
            if (!itemColorName.isNullOrEmpty()) {
                itemColorName = settingsRepository.getColorById(itemColorName)
            }
            if (!itemBranchName.isNullOrEmpty()) {
                itemBranchName = settingsRepository.getBranchById(itemBranchName)
            }
        }
        val reportResult = PrinterUtils.getItemBarcodeHtmlContent(
            context,
            item,
            family,
            itemSizeName ?: "",
            itemColorName ?: "",
            itemBranchName
        )
        SettingsModel.cashPrinter?.let {
            if (it.contains(":")) {
                val printerDetails = it.split(":")
                val size = printerDetails.size
                reportResult.printerIP = if (size > 0) printerDetails[0] else ""
                val port = if (size > 1) printerDetails[1] else "-1"
                reportResult.printerPort = port.toIntOrNull() ?: -1
            } else {
                reportResult.printerName = it
            }
        }
        return reportResult
    }

    private suspend fun hasRelations(itemId: String): Boolean {
        return invoiceRepository.getOneInvoiceByItemID(itemId) != null
    }

    private fun getFirstCurrency(): Pair<String, String> {
        return if (SettingsModel.isConnectedToSqlServer()) {
            Pair(
                SettingsModel.currentCurrency?.currencyId ?: "",
                SettingsModel.currentCurrency?.currencyCode1 ?: ""
            )
        } else {
            Pair(
                SettingsModel.currentCurrency?.currencyCode1 ?: "",
                SettingsModel.currentCurrency?.currencyCode1 ?: ""
            )
        }
    }

    fun launchBarcodeScanner(callback:(List<Any>)->Unit){
        sharedViewModel.launchBarcodeScanner(true,
            ArrayList(state.value.items),
            object : OnBarcodeResult {
                override fun OnBarcodeResult(barcodesList: List<Any>) {
                    callback.invoke(barcodesList)
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

    fun launchGalleryPicker(context:Context){
        sharedViewModel.launchGalleryPicker(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
            object : OnGalleryResult {
                override fun onGalleryResult(uris: List<Uri>) {
                    if (uris.isNotEmpty()) {
                        sharedViewModel.copyToInternalStorage(
                            context = context,
                            uri = uris[0],
                            parent = "item",
                            fileName = ((state.value.item.itemName
                                ?: "item").trim().replace(
                                " ", "_"
                            ))
                        ) { internalPath ->
                            oldImage = state.value.item.itemImage
                            updateState(
                                state.value.copy(
                                    item = state.value.item.copy(
                                        itemImage = internalPath
                                    )
                                )

                            )
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