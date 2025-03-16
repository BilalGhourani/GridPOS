package com.grid.pos.ui.purchase

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.grid.pos.SharedViewModel
import com.grid.pos.data.item.Item
import com.grid.pos.data.item.ItemRepository
import com.grid.pos.data.purchase.PurchaseRepository
import com.grid.pos.data.purchaseHeader.PurchaseHeader
import com.grid.pos.data.purchaseHeader.PurchaseHeaderRepository
import com.grid.pos.data.settings.SettingsRepository
import com.grid.pos.data.thirdParty.ThirdPartyRepository
import com.grid.pos.interfaces.OnBarcodeResult
import com.grid.pos.model.PurchaseItemModel
import com.grid.pos.model.ThirdPartyType
import com.grid.pos.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PurchaseViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val purchaseHeaderRepository: PurchaseHeaderRepository,
    private val purchaseRepository: PurchaseRepository,
    private val thirdPartyRepository: ThirdPartyRepository,
    private val settingsRepository: SettingsRepository,
    private val sharedViewModel: SharedViewModel
) : BaseViewModel(sharedViewModel) {

    val state = mutableStateOf(PurchaseState())

    private var purchaseTransCode: String? = null
    var selectedItemIndex: Int = 0

    var purchaseHeaderState = mutableStateOf(PurchaseHeader())
    val items = mutableStateListOf<PurchaseItemModel>()
    val deletedItems: MutableList<PurchaseItemModel> = mutableListOf()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
            purchaseTransCode = settingsRepository.getTransactionTypeId("Purchase Invoice")
        }
    }

    fun updatePurchaseHeader(purchaseHeader: PurchaseHeader) {
        purchaseHeaderState.value = purchaseHeader
    }

    fun resetState() {
        items.clear()
        deletedItems.clear()
        updatePurchaseHeader(PurchaseHeader())
    }

    suspend fun updateRealItemPrice(item: Item, withLoading: Boolean = true) {
        sharedViewModel.updateRealItemPrice(item, withLoading)
    }

    fun fetchPurchases() {
        showLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            val listOfTransfer = purchaseHeaderRepository.getAllPurchaseHeaders()
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    purchaseHeaders = listOfTransfer
                )
                showLoading(false)
            }
        }
    }

    fun loadPurchaseDetails(
        purchaseHeader: PurchaseHeader
    ) {
        showLoading(true)
        updatePurchaseHeader(purchaseHeader)
        viewModelScope.launch(Dispatchers.IO) {
            if (state.value.items.isEmpty()) {
                fetchItems(false)
            }
            if (state.value.warehouses.isEmpty()) {
                fetchWarehouses(false)
            }
            val result =
                purchaseRepository.getAllPurchases(purchaseHeader.purchaseHeaderId)
            val purchaseItemModels = mutableListOf<PurchaseItemModel>()
            result.forEach { purchase ->
                purchaseItemModels.add(PurchaseItemModel(
                    purchase = purchase,
                    purchaseItem = state.value.items.firstOrNull {
                        it.itemId.equals(
                            purchase.purchaseItId,
                            ignoreCase = true
                        )
                    } ?: Item()))
            }
            withContext(Dispatchers.Main) {
                items.clear()
                items.addAll(purchaseItemModels)
                showLoading(false)
            }
        }
    }

    suspend fun fetchItems(withLoading: Boolean = true) {
        if (withLoading) {
            withContext(Dispatchers.Main) {
                showLoading(true)
            }
        }
        withContext(Dispatchers.IO) {
            val listOfItems = itemRepository.getAllItems()
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    items = listOfItems
                )
                if (withLoading) {
                    showLoading(false)
                }
            }
        }
    }

    suspend fun fetchWarehouses(withLoading: Boolean = true) {
        if (withLoading) {
            withContext(Dispatchers.Main) {
                showLoading(true)
            }
        }
        withContext(Dispatchers.IO) {
            val listOfWarehouses = settingsRepository.getAllWarehouses()
            withContext(Dispatchers.Main) {
                if (withLoading) {
                    showLoading(false)
                }
                state.value = state.value.copy(
                    warehouses = listOfWarehouses,
                )
            }
        }
    }

    suspend fun fetchSuppliers(withLoading: Boolean = true) {
        if (withLoading) {
            withContext(Dispatchers.Main) {
                showLoading(true)
            }
        }
        withContext(Dispatchers.IO) {
            val listOfSuppliers = thirdPartyRepository.getAllThirdParties(
                listOf(
                    ThirdPartyType.PAYABLE.type,
                    ThirdPartyType.PAYABLE_RECEIVALBE.type
                )
            )
            withContext(Dispatchers.Main) {
                if (withLoading) {
                    showLoading(false)
                }
                state.value = state.value.copy(
                    suppliers = listOfSuppliers,
                )
            }
        }
    }

    suspend fun fetchDivisions(withLoading: Boolean = true) {
        if (withLoading) {
            withContext(Dispatchers.Main) {
                showLoading(true)
            }
        }
        withContext(Dispatchers.IO) {
            val listOfDivisions = settingsRepository.getAllDivisions()
            withContext(Dispatchers.Main) {
                if (withLoading) {
                    showLoading(false)
                }
                state.value = state.value.copy(
                    divisions = listOfDivisions,
                )
            }
        }
    }

    suspend fun fetchTransactionTypes(withLoading: Boolean = true) {
        if (withLoading) {
            withContext(Dispatchers.Main) {
                showLoading(true)
            }
        }
        withContext(Dispatchers.IO) {
            val transactionTypeList = settingsRepository.getTransactionTypes("Purchase Invoice")
            withContext(Dispatchers.Main) {
                if (withLoading) {
                    showLoading(false)
                }
                state.value = state.value.copy(
                    transactionTypes = transactionTypeList,
                )
            }
        }
    }

    fun save() {
        if (items.isEmpty()) {
            showWarning("Please select one item at least!")
            return
        }
        if (purchaseHeaderState.value.purchaseHeaderWaName.isNullOrEmpty()) {
            showWarning("Please select from warehouse at first!")
            return
        }
        if (purchaseHeaderState.value.purchaseHeaderTpName.isNullOrEmpty()) {
            showWarning("Please select a supplier at first!")
            return
        }
        showLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            val purchaseHeader = purchaseHeaderState.value.copy()
            val succeed: Boolean
            if (purchaseHeader.isNew()) {
                purchaseHeader.prepareForInsert()
                if (purchaseHeader.purchaseHeaderTtCode.isNullOrEmpty()) {
                    val transType =
                        state.value.transactionTypes.firstOrNull { it.transactionTypeDefault == 1 }
                    purchaseHeader.purchaseHeaderTtCode =
                        transType?.transactionTypeId ?: purchaseTransCode
                    purchaseHeader.purchaseHeaderTtCodeName = transType?.transactionTypeCode
                }
                val dataModel = purchaseHeaderRepository.insert(purchaseHeader)
                succeed = dataModel.succeed
                if (succeed) {
                    val addedModel = dataModel.data as PurchaseHeader
                    val purchaseHeaders = state.value.purchaseHeaders
                    if (purchaseHeaders.isNotEmpty()) {
                        purchaseHeaders.add(0, addedModel)
                    }
                    savePurchaseItems(addedModel)
                }
            } else {
                val dataModel = purchaseHeaderRepository.update(purchaseHeader)
                succeed = dataModel.succeed
                if (succeed) {
                    val addedModel = dataModel.data as PurchaseHeader
                    val purchaseHeaders = state.value.purchaseHeaders
                    val index =
                        purchaseHeaders.indexOfFirst { it.purchaseHeaderId == addedModel.purchaseHeaderId }
                    if (index >= 0) {
                        purchaseHeaders.removeAt(index)
                        purchaseHeaders.add(
                            index,
                            addedModel
                        )
                    }
                    savePurchaseItems(addedModel)
                }
            }
            if (succeed) {
                withContext(Dispatchers.Main) {
                    resetState()
                    showLoading(false)
                    showWarning("data saved successfully.")
                }
            } else {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                }
            }
        }
    }

    private suspend fun savePurchaseItems(purchaseHeader: PurchaseHeader) {
        val purchaseItems = items.toMutableList()
        purchaseItems.forEachIndexed { index, model ->
            model.purchase.prepareForInsert()
            model.purchase.purchaseHpId = purchaseHeader.purchaseHeaderId
            model.purchase.purchaseWaName = purchaseHeader.purchaseHeaderWaName
            model.purchase.purchaseLineNo = index + 1
            if (model.purchase.isNew()) {
                purchaseRepository.insert(model.purchase)
            } else {
                purchaseRepository.update(model.purchase)
            }
        }
        deletedItems.forEach {
            if (!it.purchase.isNew()) {
                purchaseRepository.delete(it.purchase)
            }
        }
    }

    fun delete() {
        showLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            val purchaseHeader = purchaseHeaderState.value.copy()
            val dataModel = purchaseHeaderRepository.delete(purchaseHeader)
            if (dataModel.succeed) {
                val purchaseHeaders = state.value.purchaseHeaders
                val index =
                    purchaseHeaders.indexOfFirst { it.purchaseHeaderId == purchaseHeader.purchaseHeaderId }
                if (index >= 0) {
                    purchaseHeaders.removeAt(index)
                }
                val stockInOutItems = items.toMutableList()
                stockInOutItems.forEach {
                    purchaseRepository.delete(it.purchase)
                }
                withContext(Dispatchers.Main) {
                    resetState()
                    showLoading(false)
                    showWarning("data deleted successfully.")
                }
            } else {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                }
            }
        }
    }

    fun launchBarcodeScanner() {
        viewModelScope.launch(Dispatchers.Main) {
            if (state.value.items.isEmpty()) {
                showLoading(true)
                fetchItems(true)
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
                                val itemsToAdd = mutableListOf<PurchaseItemModel>()
                                map.forEach { (item, count) ->
                                    if (!item.itemBarcode.isNullOrEmpty()) {
                                        updateRealItemPrice(item, false)
                                        val purchaseItem = PurchaseItemModel()
                                        purchaseItem.setItem(item)
                                        purchaseItem.purchase.purchaseQty =
                                            count.toDouble()
                                        itemsToAdd.add(purchaseItem)
                                    }
                                }
                                withContext(Dispatchers.Main) {
                                    items.addAll(itemsToAdd)
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