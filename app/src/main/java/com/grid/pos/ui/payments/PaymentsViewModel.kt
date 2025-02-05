package com.grid.pos.ui.payments

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.EntityModel
import com.grid.pos.data.currency.CurrencyRepository
import com.grid.pos.data.payment.Payment
import com.grid.pos.data.payment.PaymentRepository
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
class PaymentsViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val thirdPartyRepository: ThirdPartyRepository,
    private val currencyRepository: CurrencyRepository,
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository
) : BaseViewModel() {
    private val _state = MutableStateFlow(PaymentsState())
    val state: MutableStateFlow<PaymentsState> = _state

    var currentPayment: Payment = Payment()
    var reportResult = ReportResult()
    private var clientsMap: Map<String, ThirdParty> = mutableMapOf()
    val paymentTypes: MutableList<EntityModel> = mutableListOf(
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
                if (SettingsModel.isSqlServerWebDb) "Payment Voucher" else "Payment"
            )
        } else {
            SettingsModel.defaultPayment
        }
    }

    fun resetState() {
        currentPayment = Payment()
        state.value = state.value.copy(
            payment = currentPayment.copy(),
            currencyIndex = 0,
            warning = null,
            isLoading = false,
            clear = false,
            isSaved = false,
        )
    }

    fun updatePayment(payment: Payment, currIndex: Int? = null) {
        state.value = state.value.copy(
            payment = payment,
            currencyIndex = currIndex ?: state.value.currencyIndex
        )
    }

    fun isAnyChangeDone(): Boolean {
        return state.value.payment.didChanged(currentPayment)
    }

    fun fetchPayments() {
        state.value = state.value.copy(
            warning = null,
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            if (state.value.thirdParties.isEmpty()) {
                fetchThirdParties(false)
            }
            val listOfPayments = paymentRepository.getAllPayments()
            listOfPayments.map {
                it.paymentThirdPartyName = clientsMap[it.paymentThirdParty]?.thirdPartyName
            }
            withContext(Dispatchers.Main) {
                state.value = state.value.copy(
                    payments = listOfPayments,
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
                ThirdPartyType.PAYABLE.type,
                ThirdPartyType.PAYABLE_RECEIVALBE.type
            )
        )
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

    fun save(
        context: Context
    ) {
        val payment = state.value.payment
        if (payment.paymentThirdParty.isNullOrEmpty()) {
            state.value = state.value.copy(
                warning = Event("Please select a Client."),
                isLoading = false
            )
            return
        }
        if (payment.paymentType.isNullOrEmpty()) {
            state.value = state.value.copy(
                warning = Event("Please select a Type."),
                isLoading = false
            )
            return
        }
        if (payment.paymentCurrency.isNullOrEmpty()) {
            state.value = state.value.copy(
                warning = Event("Please select a Currency."),
                isLoading = false
            )
            return
        }
        if (payment.paymentAmount == 0.0 || payment.paymentAmount.isNaN()) {
            state.value = state.value.copy(
                warning = Event("Please enter an Amount."),
                isLoading = false
            )
            return
        }
        state.value = state.value.copy(
            isLoading = true
        )
        val isInserting = payment.isNew()
        CoroutineScope(Dispatchers.IO).launch {
            payment.calculateAmountsIfNeeded()
            if (isInserting) {
                payment.prepareForInsert()
                val lastTransactionNo = paymentRepository.getLastTransactionNo()
                payment.paymentTransNo = POSUtils.getInvoiceTransactionNo(
                    lastTransactionNo?.paymentTransNo ?: ""
                )
                payment.paymentTransCode = transactionType ?: getTransactionType()
                val dataModel = paymentRepository.insert(payment)
                if (dataModel.succeed) {
                    val addedModel = dataModel.data as Payment
                    val payments = state.value.payments
                    if (payments.isNotEmpty()) {
                        payments.add(
                            0,
                            addedModel
                        )
                    }
                    preparePaymentReport(
                        context,
                        addedModel
                    )
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            payments = payments,
                            isLoading = false,
                            warning = Event("successfully saved."),
                            isSaved = true,
                            clear = false
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            isLoading = false,
                            warning = null,
                        )
                    }
                }
            } else {
                if (payment.paymentTransCode.isNullOrEmpty()) {
                    payment.paymentTransCode = transactionType ?: getTransactionType()
                }
                val dataModel = paymentRepository.update(payment)
                if (dataModel.succeed) {
                    val index =
                        state.value.payments.indexOfFirst { it.paymentId == payment.paymentId }
                    if (index >= 0) {
                        state.value.payments.removeAt(index)
                        state.value.payments.add(
                            index,
                            payment
                        )
                    }
                    preparePaymentReport(
                        context,
                        payment
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
                            isLoading = false,
                            warning = null,
                        )
                    }
                }
            }
        }
    }

    fun delete() {
        val payment = state.value.payment
        if (payment.paymentId.isEmpty()) {
            state.value = state.value.copy(
                warning = Event("Please select a Payment to delete"),
                isLoading = false
            )
            return
        }
        state.value = state.value.copy(
            warning = null,
            isLoading = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            val dataModel = paymentRepository.delete(payment)
            if (dataModel.succeed) {
                val payments = state.value.payments
                payments.remove(payment)
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        payments = payments,
                        isLoading = false,
                        warning = Event("successfully deleted."),
                        clear = true,
                        isSaved = false
                    )
                }
            } else {
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
                        isLoading = false,
                        warning = null,
                    )
                }
            }
        }
    }

    private suspend fun preparePaymentReport(
        context: Context,
        payment: Payment
    ) {
        val thirdPartyId = payment.paymentThirdParty
        val userId = payment.paymentUserStamp
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
        reportResult = PrinterUtils.getPaymentHtmlContent(
            context,
            payment,
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