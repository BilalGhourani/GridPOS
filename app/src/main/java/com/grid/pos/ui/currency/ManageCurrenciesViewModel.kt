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

    private val _state = MutableStateFlow(ManageCurrenciesState())
    val state: MutableStateFlow<ManageCurrenciesState> = _state

    var currentCurrency: Currency = Currency()

    init {
        fetchCurrencies()
    }

    fun updateState(newState: ManageCurrenciesState) {
        state.value = newState
    }

    fun isAnyChangeDone(): Boolean {
        return state.value.currency.didChanged(currentCurrency)
    }

    private fun fetchCurrencies() {
        SettingsModel.currentCurrency?.let {
            currentCurrency = it.copy()
            updateState(
                state.value.copy(
                    currency = currentCurrency.copy(),
                    currencyName1DecStr = it.currencyName1Dec.toString(),
                    currencyName2DecStr = it.currencyName2Dec.toString(),
                    currencyRateStr = it.currencyRate.toString()
                )
            )
            viewModelScope.launch(Dispatchers.IO) {
                openConnectionIfNeeded()
            }
            return
        }
        state.value = state.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
            val currencies = currencyRepository.getAllCurrencies()
            val currency = if (currencies.size > 0) currencies[0] else Currency()
            SettingsModel.currentCurrency = currency.copy()
            withContext(Dispatchers.Main) {
                currentCurrency = currency.copy()
                updateState(
                    state.value.copy(
                        currency = currentCurrency.copy(),
                        currencyName1DecStr = currency.currencyName1Dec.toString(),
                        currencyName2DecStr = currency.currencyName2Dec.toString(),
                        currencyRateStr = currency.currencyRate.toString()
                    )
                )
                state.value = state.value.copy(
                    isLoading = false
                )
            }
        }
    }

    fun saveCurrency() {
        val currency = state.value.currency
        if (currency.currencyCode1.isNullOrEmpty() || currency.currencyName1.isNullOrEmpty() || currency.currencyCode2.isNullOrEmpty() || currency.currencyName2.isNullOrEmpty() || currency.currencyRate.isNaN()) {
            state.value = state.value.copy(
                warning = Event(
                    "Please fill all inputs"
                ),
                isLoading = false
            )
            return
        }
        state.value = state.value.copy(
            isLoading = true
        )
        currentCurrency = currency
        val isInserting = currency.isNew()
        CoroutineScope(Dispatchers.IO).launch {
            if (hasRelations(currency)) {
                withContext(Dispatchers.Main) {
                    state.value = state.value.copy(
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
                        state.value = state.value.copy(
                            isLoading = false,
                            isSaved = true,
                            warning = Event("Currency saved successfully."),
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
                val dataModel = currencyRepository.update(currency)
                if (dataModel.succeed) {
                    SettingsModel.currentCurrency = currency.copy()
                    withContext(Dispatchers.Main) {
                        state.value = state.value.copy(
                            warning = Event("Currency saved successfully."),
                            isSaved = true,
                            isLoading = false
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

    private suspend fun hasRelations(currency: Currency): Boolean {
        // check only if currency code has changed
        if (SettingsModel.currentCurrency?.didChangedCurrencyCode(currency) == true) {
            // if we have at least one invoice
            return invoiceHeaderRepository.getLastInvoice() != null
        }
        return false
    }

}