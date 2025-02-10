package com.grid.pos.ui.receipts

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.EntityModel
import com.grid.pos.data.currency.CurrencyRepository
import com.grid.pos.data.receipt.Receipt
import com.grid.pos.data.receipt.ReceiptRepository
import com.grid.pos.data.settings.SettingsRepository
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
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository
) : BaseViewModel() {

    private val _state = MutableStateFlow(ReceiptsState())
    val state: MutableStateFlow<ReceiptsState> = _state

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
        _state.value = state.value.copy(
            receipt = currentReceipt.copy(),
            currencyIndex = 0,
            warning = null,
            isLoading = false,
            clear = false,
            isSaved = false,
        )
    }

    fun updateState(newState: ReceiptsState) {
        _state.value = newState
    }

    fun isAnyChangeDone(): Boolean {
        return state.value.receipt.didChanged(currentReceipt)
    }


    fun fetchReceipts() {
        state.value = state.value.copy(
            warning = null,
            isLoading = true
        )
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
                state.value = state.value.copy(
                    warning = null,
                    isLoading = true
                )
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
                state.value = state.value.copy(
                    thirdParties = listOfThirdParties,
                    isLoading = false
                )
            } else {
                state.value = state.value.copy(
                    thirdParties = listOfThirdParties
                )
            }
        }
    }

    fun save(context: Context) {
        val receipt = state.value.receipt
        if (receipt.receiptThirdParty.isNullOrEmpty()) {
            state.value = state.value.copy(
                warning = Event("Please select a Client."),
                isLoading = false
            )
            return
        }
        if (receipt.receiptType.isNullOrEmpty()) {
            state.value = state.value.copy(
                warning = Event("Please select a Type."),
                isLoading = false
            )
            return
        }
        if (receipt.receiptCurrency.isNullOrEmpty()) {
            state.value = state.value.copy(
                warning = Event("Please select a Currency."),
                isLoading = false
            )
            return
        }
        if (receipt.receiptAmount == 0.0 || receipt.receiptAmount.isNaN()) {
            state.value = state.value.copy(
                warning = Event("Please enter an Amount."),
                isLoading = false
            )
            return
        }
        state.value = state.value.copy(
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
                            receipts = receipts,
                            isLoading = false,
                            warning = Event("successfully saved."),
                            isSaved = true,
                            clear = false
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            isLoading = false
                        )
                    }
                }
            } else {
                if (receipt.receiptTransCode.isNullOrEmpty()) {
                    receipt.receiptTransCode = transactionType ?: getTransactionType()
                }
                val dataModel = receiptRepository.update(receipt)
                if (dataModel.succeed) {
                    val index =
                        state.value.receipts.indexOfFirst { it.receiptId == receipt.receiptId }
                    if (index >= 0) {
                        state.value.receipts.removeAt(index)
                        state.value.receipts.add(
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
                            isLoading = false,
                            warning = Event("successfully saved."),
                            isSaved = true,
                            clear = false
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun delete() {
        val receipt = state.value.receipt
        if (receipt.receiptId.isEmpty()) {
            state.value = state.value.copy(
                warning = Event("Please select a Receipt to delete"),
                isLoading = false
            )
            return
        }
        state.value = state.value.copy(
            warning = null,
            isLoading = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            val dataModel = receiptRepository.delete(receipt)
            if (dataModel.succeed) {
                val receipts = state.value.receipts
                receipts.remove(receipt)
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        receipts = receipts,
                        isLoading = false,
                        warning = Event("successfully deleted."),
                        clear = true,
                        isSaved = false
                    )
                }
            } else {
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        isLoading = false
                    )
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