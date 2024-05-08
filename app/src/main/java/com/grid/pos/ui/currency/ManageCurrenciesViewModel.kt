package com.grid.pos.ui.currency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.Currency.CurrencyRepository
import com.grid.pos.interfaces.OnResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageCurrenciesViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository) : ViewModel() {

    private val _manageCurrenciesState = MutableStateFlow(ManageCurrenciesState())
    val manageCurrenciesState: MutableStateFlow<ManageCurrenciesState> = _manageCurrenciesState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            fetchCurrencies()
        }
    }

    private suspend fun fetchCurrencies() {
        currencyRepository.getAllCurrencies(object : OnResult {
            override fun onSuccess(result: Any) {
                result as List<*>
                val currency = if (result.size > 0) result[0] as Currency else Currency()
                viewModelScope.launch(Dispatchers.Main) {
                    manageCurrenciesState.value = manageCurrenciesState.value.copy(
                        selectedCurrency = currency, fillFields = true
                    )
                }
            }

            override fun onFailure(message: String, errorCode: Int) {

            }

        })
    }

    fun saveCurrency(currency: Currency) {
        if (currency.currencyCode1.isNullOrEmpty() || currency.currencyName1.isNullOrEmpty() || currency.currencyCode2.isNullOrEmpty() || currency.currencyName2.isNullOrEmpty() || currency.currencyRate.isNaN()) {
            manageCurrenciesState.value = manageCurrenciesState.value.copy(
                warning = "Please fill all inputs", isLoading = false
            )
            return
        }
        manageCurrenciesState.value = manageCurrenciesState.value.copy(
            isLoading = true
        )
        val isInserting = currency.currencyDocumentId.isNullOrEmpty()
        val callback = object : OnResult {
            override fun onSuccess(result: Any) {
                viewModelScope.launch(Dispatchers.Main) {
                    manageCurrenciesState.value = manageCurrenciesState.value.copy(
                        selectedCurrency = result as Currency, isLoading = false
                    )
                }
            }

            override fun onFailure(message: String, errorCode: Int) {
                viewModelScope.launch(Dispatchers.Main) {
                    manageCurrenciesState.value = manageCurrenciesState.value.copy(
                        isLoading = false
                    )
                }
            }

        }
        CoroutineScope(Dispatchers.IO).launch {
            if (isInserting) {
                currency.prepareForInsert()
                currencyRepository.insert(currency, callback)
            } else {
                currencyRepository.update(currency, callback)
            }
        }
    }

}