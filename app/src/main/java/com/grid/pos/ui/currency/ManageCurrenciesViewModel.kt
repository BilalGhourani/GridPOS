package com.grid.pos.ui.currency

import androidx.lifecycle.viewModelScope
import com.grid.pos.data.currency.Currency
import com.grid.pos.data.currency.CurrencyRepository
import com.grid.pos.data.invoiceHeader.InvoiceHeaderRepository
import com.grid.pos.model.Event
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ManageCurrenciesViewModel @Inject constructor(
        private val currencyRepository: CurrencyRepository,
        private val invoiceHeaderRepository: InvoiceHeaderRepository
) : BaseViewModel() {

    private val _manageCurrenciesState = MutableStateFlow(ManageCurrenciesState())
    val manageCurrenciesState: MutableStateFlow<ManageCurrenciesState> = _manageCurrenciesState

    private var _currencyState = MutableStateFlow(Currency())
    var currencyState = _currencyState.asStateFlow()

    var currentCurrency: Currency = Currency()

    init {
        fetchCurrencies()
    }

    fun updateCurrency(currency: Currency) {
        _currencyState.value = currency
    }

    private fun fetchCurrencies() {
        SettingsModel.currentCurrency?.let {
            updateCurrency(it.copy())
            viewModelScope.launch(Dispatchers.IO) {
                openConnectionIfNeeded()
            }
            return
        }
        manageCurrenciesState.value = manageCurrenciesState.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
            val currencies = currencyRepository.getAllCurrencies()
            val currency = if (currencies.size > 0) currencies[0] else Currency()
            SettingsModel.currentCurrency = currency.copy()
            withContext(Dispatchers.Main) {
                updateCurrency(currency)
                manageCurrenciesState.value = manageCurrenciesState.value.copy(
                    isLoading = false
                )
            }
        }
    }

    fun saveCurrency() {
        val currency = currencyState.value
        if (currency.currencyCode1.isNullOrEmpty() || currency.currencyName1.isNullOrEmpty() || currency.currencyCode2.isNullOrEmpty() || currency.currencyName2.isNullOrEmpty() || currency.currencyRate.isNaN()) {
            manageCurrenciesState.value = manageCurrenciesState.value.copy(
                warning = Event(
                    "Please fill all inputs"
                ),
                isLoading = false
            )
            return
        }
        manageCurrenciesState.value = manageCurrenciesState.value.copy(
            isLoading = true
        )
        currentCurrency = currency
        val isInserting = currency.isNew()
        CoroutineScope(Dispatchers.IO).launch {
            if (hasRelations(currency)) {
                withContext(Dispatchers.Main) {
                    manageCurrenciesState.value = manageCurrenciesState.value.copy(
                        warning = Event("You can't update the Currency code!"),
                        isLoading = false
                    )
                }
                return@launch
            }
            if (isInserting) {
                currency.prepareForInsert()
                val dataModel = currencyRepository.insert(currency)
                if (dataModel.succeed) {
                    val addedCurr = dataModel.data as Currency
                    SettingsModel.currentCurrency = addedCurr.copy()
                    withContext(Dispatchers.Main) {
                        manageCurrenciesState.value = manageCurrenciesState.value.copy(
                            isLoading = false,
                            isSaved = true,
                            warning = Event("Currency saved successfully."),
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        manageCurrenciesState.value = manageCurrenciesState.value.copy(
                            isLoading = false,
                            warning = null,
                        )
                    }
                }
            } else {
                val dataModel = currencyRepository.update(currency)
                if (dataModel.succeed) {
                    SettingsModel.currentCurrency = currency.copy()
                    withContext(Dispatchers.Main) {
                        manageCurrenciesState.value = manageCurrenciesState.value.copy(
                            warning = Event("Currency saved successfully."),
                            isSaved = true,
                            isLoading = false
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        manageCurrenciesState.value = manageCurrenciesState.value.copy(
                            isLoading = false,
                            warning = null,
                        )
                    }
                }
            }
        }
    }

    private suspend fun hasRelations(currency: Currency): Boolean {
        // check only if currency code has changed
        if (SettingsModel.currentCurrency?.didChangedCurrencyCode(currency) == true) {
            // if we have at least one invoice
            return invoiceHeaderRepository.getLastInvoice() != null
        }
        return false
    }

}