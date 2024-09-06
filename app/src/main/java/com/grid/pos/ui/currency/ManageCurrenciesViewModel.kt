package com.grid.pos.ui.currency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.Currency.CurrencyRepository
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
        private val currencyRepository: CurrencyRepository
) : BaseViewModel() {

    private val _manageCurrenciesState = MutableStateFlow(ManageCurrenciesState())
    val manageCurrenciesState: MutableStateFlow<ManageCurrenciesState> = _manageCurrenciesState
    var currentCurrency: Currency? = null

    init {
        manageCurrenciesState.value = manageCurrenciesState.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            openConnectionIfNeeded()
            fetchCurrencies()
        }
    }

    private suspend fun fetchCurrencies() {
        SettingsModel.currentCurrency?.let {
            viewModelScope.launch(Dispatchers.Main) {
                manageCurrenciesState.value = manageCurrenciesState.value.copy(
                    selectedCurrency = it,
                    fillFields = true,
                    isLoading = false
                )
            }
            return
        }
        val currencies = currencyRepository.getAllCurrencies()
        val currency = if (currencies.size > 0) currencies[0] else Currency()
        SettingsModel.currentCurrency = currency
        viewModelScope.launch(Dispatchers.Main) {
            manageCurrenciesState.value = manageCurrenciesState.value.copy(
                selectedCurrency = currency,
                fillFields = true,
                isLoading = false
            )
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
            if (isInserting) {
                currency.prepareForInsert()
                val addedCurr = currencyRepository.insert(currency)
                SettingsModel.currentCurrency = addedCurr
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
                SettingsModel.currentCurrency = currency
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

}