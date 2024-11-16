package com.grid.pos.ui.receipts

import androidx.lifecycle.viewModelScope
import com.grid.pos.data.Currency.CurrencyRepository
import com.grid.pos.data.DataModel
import com.grid.pos.data.Receipt.Receipt
import com.grid.pos.data.Receipt.ReceiptRepository
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.data.ThirdParty.ThirdPartyRepository
import com.grid.pos.model.Event
import com.grid.pos.model.PaymentTypeModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.ThirdPartyType
import com.grid.pos.ui.common.BaseViewModel
import com.grid.pos.ui.pos.POSUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ReceiptsViewModel @Inject constructor(
        private val receiptRepository: ReceiptRepository,
        private val thirdPartyRepository: ThirdPartyRepository,
        private val currencyRepository: CurrencyRepository,
) : BaseViewModel() {

    private val _receiptsState = MutableStateFlow(ReceiptsState())
    val receiptsState: MutableStateFlow<ReceiptsState> = _receiptsState
    var currentReceipt: Receipt = Receipt()
    private var clientsMap: Map<String, ThirdParty> = mutableMapOf()
    val receiptTypes: MutableList<DataModel> = mutableListOf(
        PaymentTypeModel("Cash"),
        PaymentTypeModel("Credit"),
        PaymentTypeModel("Debit")
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
            fetchCurrencies()
        }
    }

    fun getDefaultType(): String {
        if (receiptTypes.isNotEmpty()) {
            return receiptTypes[0].getId()
        }
        return "Cash"
    }

    fun fetchReceipts() {
        receiptsState.value = receiptsState.value.copy(
            warning = null,
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            if (receiptsState.value.thirdParties.isEmpty()) {
                fetchThirdParties(false)
            }
            val listOfReceipts = receiptRepository.getAllReceipts()
            listOfReceipts.map {
                it.receiptThirdPartyName = clientsMap[it.receiptThirdParty]?.thirdPartyName
            }
            withContext(Dispatchers.Main) {
                receiptsState.value = receiptsState.value.copy(
                    receipts = listOfReceipts,
                    isLoading = false
                )
            }
        }
    }

    private fun fetchCurrencies() {
        viewModelScope.launch(Dispatchers.IO) {
            val currencies = currencyRepository.getAllCurrencyModels()
            withContext(Dispatchers.Main) {
                receiptsState.value = receiptsState.value.copy(
                    currencies = currencies
                )
            }
        }
    }

    suspend fun fetchThirdParties(loading: Boolean = true) {
        if (loading) {
            receiptsState.value = receiptsState.value.copy(
                warning = null,
                isLoading = true
            )
        }
        val listOfThirdParties = thirdPartyRepository.getAllThirdParties(
            listOf(
                ThirdPartyType.RECEIVALBE.type,
                ThirdPartyType.PAYABLE_RECEIVALBE.type
            )
        )
        clientsMap = listOfThirdParties.associateBy { it.thirdPartyId }
        withContext(Dispatchers.Main) {
            if (loading) {
                receiptsState.value = receiptsState.value.copy(
                    thirdParties = listOfThirdParties,
                    isLoading = false
                )
            } else {
                receiptsState.value = receiptsState.value.copy(
                    thirdParties = listOfThirdParties
                )
            }
        }
    }

    fun saveReceipt(receipt: Receipt) {
        if (receipt.receiptThirdParty.isNullOrEmpty()) {
            receiptsState.value = receiptsState.value.copy(
                warning = Event("Please select a Client."),
                isLoading = false
            )
            return
        }
        if (receipt.receiptType.isNullOrEmpty()) {
            receiptsState.value = receiptsState.value.copy(
                warning = Event("Please select a Type."),
                isLoading = false
            )
            return
        }
        if (receipt.receiptCurrency.isNullOrEmpty()) {
            receiptsState.value = receiptsState.value.copy(
                warning = Event("Please select a Currency."),
                isLoading = false
            )
            return
        }
        if (receipt.receiptAmount == 0.0 || receipt.receiptAmount.isNaN()) {
            receiptsState.value = receiptsState.value.copy(
                warning = Event("Please enter an Amount."),
                isLoading = false
            )
            return
        }
        receiptsState.value = receiptsState.value.copy(
            isLoading = true
        )
        val isInserting = receipt.isNew()
        CoroutineScope(Dispatchers.IO).launch {
            receipt.calculateAmountsIfNeeded()
            if (isInserting) {
                receipt.prepareForInsert()
                val lastTransactionNo = receiptRepository.getLastTransactionNo()
                receipt.receiptTransNo = POSUtils.getInvoiceTransactionNo(
                    lastTransactionNo?.receiptTransNo ?: ""
                )
                receipt.receiptTransCode = SettingsModel.defaultReceipt
                val addedModel = receiptRepository.insert(receipt)
                val receipts = receiptsState.value.receipts
                if (receipts.isNotEmpty()) {
                    receipts.add(addedModel)
                }
                withContext(Dispatchers.Main) {
                    receiptsState.value = receiptsState.value.copy(
                        receipts = receipts,
                        selectedReceipt = addedModel,
                        isLoading = false,
                        warning = Event("successfully saved."),
                        clear = true
                    )
                }
            } else {
                receiptRepository.update(receipt)
                withContext(Dispatchers.Main) {
                    receiptsState.value = receiptsState.value.copy(
                        selectedReceipt = receipt,
                        isLoading = false,
                        warning = Event("successfully saved."),
                        clear = true
                    )
                }
            }
        }
    }

    fun deleteSelectedReceipt() {
        val receipt = receiptsState.value.selectedReceipt
        if (receipt.receiptId.isEmpty()) {
            receiptsState.value = receiptsState.value.copy(
                warning = Event("Please select a Receipt to delete"),
                isLoading = false
            )
            return
        }
        receiptsState.value = receiptsState.value.copy(
            warning = null,
            isLoading = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            receiptRepository.delete(receipt)
            val receipts = receiptsState.value.receipts
            receipts.remove(receipt)
            withContext(Dispatchers.Main) {
                receiptsState.value = receiptsState.value.copy(
                    receipts = receipts,
                    selectedReceipt = Receipt(),
                    isLoading = false,
                    warning = Event("successfully deleted."),
                    clear = true
                )
            }
        }
    }

}