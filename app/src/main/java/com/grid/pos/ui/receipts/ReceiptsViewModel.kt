package com.grid.pos.ui.receipts

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.grid.pos.SharedViewModel
import com.grid.pos.data.EntityModel
import com.grid.pos.data.currency.CurrencyRepository
import com.grid.pos.data.receipt.Receipt
import com.grid.pos.data.receipt.ReceiptRepository
import com.grid.pos.data.settings.SettingsRepository
import com.grid.pos.data.thirdParty.ThirdParty
import com.grid.pos.data.thirdParty.ThirdPartyRepository
import com.grid.pos.data.user.UserRepository
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ReceiptsViewModel @Inject constructor(
    private val receiptRepository: ReceiptRepository,
    private val thirdPartyRepository: ThirdPartyRepository,
    private val currencyRepository: CurrencyRepository,
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository,
    private val sharedViewModel: SharedViewModel
) : BaseViewModel(sharedViewModel) {

    val state = mutableStateOf(ReceiptsState())

    var currentReceipt: Receipt = Receipt()
    var reportResult = ReportResult()
    private var clientsMap: Map<String, ThirdParty> = mutableMapOf()
    val receiptTypes: MutableList<EntityModel> = mutableListOf(
        PaymentTypeModel("Cash"),
        PaymentTypeModel("Credit"),
        PaymentTypeModel("Debit")
    )

    private var transactionType: String? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
            fetchCurrencies()
            transactionType = getTransactionType()
            reportResult = ReportResult()
        }
    }

    private suspend fun getTransactionType(): String? {
        return if (SettingsModel.isConnectedToSqlServer()) {
            settingsRepository.getTransactionTypeId(
                if (SettingsModel.isSqlServerWebDb) "Receipt Voucher" else "Receipt"
            )
        } else {
            SettingsModel.defaultReceipt
        }
    }

    fun resetState() {
        currentReceipt = Receipt()
        state.value = state.value.copy(
            receipt = currentReceipt.copy(),
            currencyIndex = 0,
        )
    }

    fun updateState(newState: ReceiptsState) {
        state.value = newState
    }

    fun isAnyChangeDone(): Boolean {
        return state.value.receipt.didChanged(currentReceipt)
    }


    fun fetchReceipts() {
        showLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            if (state.value.thirdParties.isEmpty()) {
                fetchThirdParties(false)
            }
            val listOfReceipts = receiptRepository.getAllReceipts()
            listOfReceipts.map {
                it.receiptThirdPartyName = clientsMap[it.receiptThirdParty]?.thirdPartyName
            }
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    receipts = listOfReceipts
                )
                showLoading(false)
            }
        }
    }

    private fun fetchCurrencies() {
        viewModelScope.launch(Dispatchers.IO) {
            val currencies = currencyRepository.getAllCurrencyModels()
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    currencies = currencies
                )
            }
        }
    }

    fun getCurrencyCode(currID: String?): String? {
        if (currID.isNullOrEmpty()) return null
        return state.value.currencies.firstOrNull { it.currencyId == currID || it.currencyCode == currID }?.currencyCode
    }

    suspend fun fetchThirdParties(loading: Boolean = true) {
        if (loading) {
            withContext(Dispatchers.Main) {
                showLoading(true)
            }
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
                showLoading(false)
            }
            state.value = state.value.copy(
                thirdParties = listOfThirdParties
            )
        }
    }

    fun save(
        context: Context,
        callback: (String) -> Unit
    ) {
        val receipt = state.value.receipt
        if (receipt.receiptThirdParty.isNullOrEmpty()) {
            showWarning("Please select a Client.")
            return
        }
        if (receipt.receiptType.isNullOrEmpty()) {
            showWarning("Please select a Type.")
            return
        }
        if (receipt.receiptCurrency.isNullOrEmpty()) {
            showWarning("Please select a Currency.")
            return
        }
        if (receipt.receiptAmount == 0.0 || receipt.receiptAmount.isNaN()) {
            showWarning("Please enter an Amount.")
            return
        }
        showLoading(true)
        val isInserting = receipt.isNew()
        CoroutineScope(Dispatchers.IO).launch {
            receipt.calculateAmountsIfNeeded()
            if (isInserting) {
                receipt.prepareForInsert()
                val lastTransactionNo = receiptRepository.getLastTransactionNo()
                receipt.receiptTransNo = POSUtils.getInvoiceTransactionNo(
                    lastTransactionNo?.receiptTransNo ?: ""
                )
                receipt.receiptTransCode = transactionType ?: getTransactionType()
                val dataModel = receiptRepository.insert(receipt)
                if (dataModel.succeed) {
                    val addedModel = dataModel.data as Receipt
                    val receipts = state.value.receipts
                    if (receipts.isNotEmpty()) {
                        receipts.add(
                            0,
                            addedModel
                        )
                    }
                    prepareReceiptReport(
                        context,
                        addedModel
                    )
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            receipts = receipts
                        )
                        resetState()
                        showLoading(false)
                        showWarning("successfully saved.")
                        sharedViewModel.reportsToPrint.clear()
                        sharedViewModel.reportsToPrint.add(reportResult)
                        resetState()
                        callback.invoke("UIWebView")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                    }
                }
            } else {
                if (receipt.receiptTransCode.isNullOrEmpty()) {
                    receipt.receiptTransCode = transactionType ?: getTransactionType()
                }
                val dataModel = receiptRepository.update(receipt)
                if (dataModel.succeed) {
                    val receipts = state.value.receipts.toMutableList()
                    val index = receipts.indexOfFirst { it.receiptId == receipt.receiptId }
                    if (index >= 0) {
                        receipts.removeAt(index)
                        receipts.add(
                            index,
                            receipt
                        )
                    }
                    prepareReceiptReport(
                        context,
                        receipt
                    )
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            receipts = receipts
                        )
                        resetState()
                        showLoading(false)
                        showWarning("successfully saved.")
                        sharedViewModel.reportsToPrint.clear()
                        sharedViewModel.reportsToPrint.add(reportResult)
                        resetState()
                        callback.invoke("UIWebView")
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
        val receipt = state.value.receipt
        if (receipt.receiptId.isEmpty()) {
            showWarning("Please select a Receipt to delete")
            return
        }
        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            val dataModel = receiptRepository.delete(receipt)
            if (dataModel.succeed) {
                val receipts = state.value.receipts
                receipts.remove(receipt)
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        receipts = receipts
                    )
                    resetState()
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

    private suspend fun prepareReceiptReport(
        context: Context,
        receipt: Receipt
    ) {
        val thirdPartyId = receipt.receiptThirdParty
        val userId = receipt.receiptUserStamp
        val defaultThirdParty = if (thirdPartyId.isNullOrEmpty()) {
            state.value.thirdParties.firstOrNull { it.thirdPartyDefault }
        } else {
            state.value.thirdParties.firstOrNull {
                it.thirdPartyId == thirdPartyId
            }
        }
        val user =
            if (userId.isNullOrEmpty() || SettingsModel.currentUser?.userId == userId || SettingsModel.currentUser?.userUsername == userId) {
                SettingsModel.currentUser
            } else {
                if (state.value.users.isEmpty()) {
                    state.value.users = userRepository.getAllUsers()
                }
                state.value.users.firstOrNull {
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