package com.grid.pos.ui.receipts

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.currency.CurrencyRepository
import com.grid.pos.data.DataModel
import com.grid.pos.data.receipt.Receipt
import com.grid.pos.data.receipt.ReceiptRepository
import com.grid.pos.data.thirdParty.ThirdParty
import com.grid.pos.data.thirdParty.ThirdPartyRepository
import com.grid.pos.data.user.UserRepository
import com.grid.pos.model.Event
import com.grid.pos.model.PaymentTypeModel
import com.grid.pos.model.ReportResult
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.ThirdPartyType
import com.grid.pos.ui.common.BaseViewModel
import com.grid.pos.ui.pos.POSUtils
import com.grid.pos.utils.PrinterUtils
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
        private val userRepository: UserRepository
) : BaseViewModel() {

    private val _receiptsState = MutableStateFlow(ReceiptsState())
    val receiptsState: MutableStateFlow<ReceiptsState> = _receiptsState
    var currentReceipt: Receipt = Receipt()
    var reportResult = ReportResult()
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

    fun resetState() {
        receiptsState.value = receiptsState.value.copy(
            warning = null,
            isLoading = false,
            clear = false,
            isSaved = false,
        )
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

    fun getCurrencyCode(currID: String?): String? {
        if (currID.isNullOrEmpty()) return null
        return receiptsState.value.currencies.firstOrNull { it.currencyId == currID || it.currencyCode == currID }?.currencyCode
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

    fun saveReceipt(
            context: Context,
            receipt: Receipt
    ) {
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
                    receipts.add(0,addedModel)
                }
                prepareReceiptReport(
                    context,
                    addedModel
                )
                withContext(Dispatchers.Main) {
                    receiptsState.value = receiptsState.value.copy(
                        receipts = receipts,
                        selectedReceipt = addedModel,
                        isLoading = false,
                        warning = Event("successfully saved."),
                        isSaved = true,
                        clear = false
                    )
                }
            } else {
                receiptRepository.update(receipt)
                val index = receiptsState.value.receipts.indexOfFirst { it.receiptId == receipt.receiptId }
                if (index >= 0) {
                    receiptsState.value.receipts.removeAt(index)
                    receiptsState.value.receipts.add(
                        index,
                        receipt
                    )
                }
                prepareReceiptReport(
                    context,
                    receipt
                )
                withContext(Dispatchers.Main) {
                    receiptsState.value = receiptsState.value.copy(
                        selectedReceipt = receipt,
                        isLoading = false,
                        warning = Event("successfully saved."),
                        isSaved = true,
                        clear = false
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
                    clear = true,
                    isSaved = false
                )
            }
        }
    }

    private suspend fun prepareReceiptReport(
            context: Context,
            receipt: Receipt
    ) {
        val thirdPartyId = receipt.receiptThirdParty
        val userId = receipt.receiptUserStamp
        val defaultThirdParty = if (thirdPartyId.isNullOrEmpty()) {
            receiptsState.value.thirdParties.firstOrNull { it.thirdPartyDefault }
        } else {
            receiptsState.value.thirdParties.firstOrNull {
                it.thirdPartyId == thirdPartyId
            }
        }
        val user = if (userId.isNullOrEmpty() || SettingsModel.currentUser?.userId == userId || SettingsModel.currentUser?.userUsername == userId) {
            SettingsModel.currentUser
        } else {
            if (receiptsState.value.users.isEmpty()) {
                receiptsState.value.users = userRepository.getAllUsers()
            }
            receiptsState.value.users.firstOrNull {
                it.userId == userId || it.userUsername == userId
            } ?: SettingsModel.currentUser
        }
        reportResult = PrinterUtils.getReceiptHtmlContent(
            context,
            receipt,
            user,
            defaultThirdParty,
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
    }

}