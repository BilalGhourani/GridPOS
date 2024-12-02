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
    var currentCurrency: Currency = Currency()

    init {
        fetchCurrencies()
    }

    private fun fetchCurrencies() {
        SettingsModel.currentCurrency?.let {
            manageCurrenciesState.value = manageCurrenciesState.value.copy(
                selectedCurrency = it.copy(),
                fillFields = true,
                isLoading = false
            )
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
                manageCurrenciesState.value = manageCurrenciesState.value.copy(
                    selectedCurrency = currency,
                    fillFields = true,
                    isLoading = false
                )
            }
        }
    }

    fun saveCurrency() {
        val currency = manageCurrenciesState.value.selectedCurrency
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
                val addedCurr = currencyRepository.insert(currency)
                SettingsModel.currentCurrency = addedCurr.copy()
                withContext(Dispatchers.Main) {
                    manageCurrenciesState.value = manageCurrenciesState.value.copy(
                        selectedCurrency = addedCurr,
                        isLoading = false,
                        isSaved = true,
                        warning = Event("Currency saved successfully."),
                    )
                }
            } else {
                currencyRepository.update(
                    currency
                )
                SettingsModel.currentCurrency = currency.copy()
                withContext(Dispatchers.Main) {
                    manageCurrenciesState.value = manageCurrenciesState.value.copy(
                        selectedCurrency = currency,
                        warning = Event("Currency saved successfully."),
                        isSaved = true,
                        isLoading = false
                    )
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