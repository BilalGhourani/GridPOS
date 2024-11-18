package com.grid.pos.ui.payments

import androidx.lifecycle.viewModelScope
import com.grid.pos.data.Currency.CurrencyRepository
import com.grid.pos.data.DataModel
import com.grid.pos.data.Payment.Payment
import com.grid.pos.data.Payment.PaymentRepository
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
class PaymentsViewModel @Inject constructor(
        private val paymentRepository: PaymentRepository,
        private val thirdPartyRepository: ThirdPartyRepository,
        private val currencyRepository: CurrencyRepository,
) : BaseViewModel() {

    private val _paymentsState = MutableStateFlow(PaymentsState())
    val paymentsState: MutableStateFlow<PaymentsState> = _paymentsState
    var currentPayment: Payment = Payment()
    private var clientsMap: Map<String, ThirdParty> = mutableMapOf()
    val paymentTypes: MutableList<DataModel> = mutableListOf(
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
        if (paymentTypes.isNotEmpty()) {
            return paymentTypes[0].getId()
        }
        return "Cash"
    }

    fun fetchPayments() {
        paymentsState.value = paymentsState.value.copy(
            warning = null,
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val listOfPayments = paymentRepository.getAllPayments()
            listOfPayments.map {
                it.paymentThirdPartyName = clientsMap[it.paymentThirdParty]?.thirdPartyName
            }
            withContext(Dispatchers.Main) {
                paymentsState.value = paymentsState.value.copy(
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
                paymentsState.value = paymentsState.value.copy(
                    currencies = currencies
                )
            }
        }
    }

    suspend fun fetchThirdParties(loading: Boolean = true) {
        if (loading) {
            paymentsState.value = paymentsState.value.copy(
                warning = null,
                isLoading = true
            )
        }
        val listOfThirdParties = thirdPartyRepository.getAllThirdParties(
            listOf(
                ThirdPartyType.PAYABLE.type,
                ThirdPartyType.PAYABLE_RECEIVALBE.type
            )
        )
        withContext(Dispatchers.Main) {
            if (loading) {
                paymentsState.value = paymentsState.value.copy(
                    thirdParties = listOfThirdParties,
                    isLoading = false
                )
            } else {
                paymentsState.value = paymentsState.value.copy(
                    thirdParties = listOfThirdParties
                )
            }
        }
    }

    fun savePayment(payment: Payment) {
        if (payment.paymentThirdParty.isNullOrEmpty()) {
            paymentsState.value = paymentsState.value.copy(
                warning = Event("Please select a Client."),
                isLoading = false
            )
            return
        }
        if (payment.paymentType.isNullOrEmpty()) {
            paymentsState.value = paymentsState.value.copy(
                warning = Event("Please select a Type."),
                isLoading = false
            )
            return
        }
        if (payment.paymentCurrency.isNullOrEmpty()) {
            paymentsState.value = paymentsState.value.copy(
                warning = Event("Please select a Currency."),
                isLoading = false
            )
            return
        }
        if (payment.paymentAmount == 0.0 || payment.paymentAmount.isNaN()) {
            paymentsState.value = paymentsState.value.copy(
                warning = Event("Please enter an Amount."),
                isLoading = false
            )
            return
        }
        paymentsState.value = paymentsState.value.copy(
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
                payment.paymentTransCode = SettingsModel.defaultPayment
                val addedModel = paymentRepository.insert(payment)
                val payments = paymentsState.value.payments
                if (payments.isNotEmpty()) {
                    payments.add(addedModel)
                }
                withContext(Dispatchers.Main) {
                    paymentsState.value = paymentsState.value.copy(
                        payments = payments,
                        selectedPayment = addedModel,
                        isLoading = false,
                        warning = Event("successfully saved."),
                        isSaved = true,
                        clear = false
                    )
                }
            } else {
                paymentRepository.update(payment)
                withContext(Dispatchers.Main) {
                    paymentsState.value = paymentsState.value.copy(
                        selectedPayment = payment,
                        isLoading = false,
                        warning = Event("successfully saved."),
                        isSaved = true,
                        clear = false
                    )
                }
            }
        }
    }

    fun deleteSelectedPayment() {
        val payment = paymentsState.value.selectedPayment
        if (payment.paymentId.isEmpty()) {
            paymentsState.value = paymentsState.value.copy(
                warning = Event("Please select a Payment to delete"),
                isLoading = false
            )
            return
        }
        paymentsState.value = paymentsState.value.copy(
            warning = null,
            isLoading = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            paymentRepository.delete(payment)
            val payments = paymentsState.value.payments
            payments.remove(payment)
            withContext(Dispatchers.Main) {
                paymentsState.value = paymentsState.value.copy(
                    payments = payments,
                    selectedPayment = Payment(),
                    isLoading = false,
                    warning = Event("successfully deleted."),
                    clear = true,
                    isSaved = false
                )
            }
        }
    }

}