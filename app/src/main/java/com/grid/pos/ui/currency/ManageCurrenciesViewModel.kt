package com.grid.pos.ui.currency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.Currency.CurrencyRepository
import com.grid.pos.interfaces.OnResult
import com.grid.pos.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageCurrenciesViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    private val _manageCurrenciesState = MutableStateFlow(ManageCurrenciesState())
    val manageCurrenciesState: MutableStateFlow<ManageCurrenciesState> = _manageCurrenciesState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            fetchCurrencies()
        }
    }

    private fun fetchCurrencies() {
        currencyRepository.getAllCurrencies(object : OnResult {
            override fun onSuccess(result: Any) {
                val listOfCurrencies = mutableListOf<Currency>()
                (result as List<Currency>).forEach {
                    listOfCurrencies.add(it)
                }
                viewModelScope.launch(Dispatchers.Main) {
                    manageCurrenciesState.value = manageCurrenciesState.value.copy(
                        currencies = listOfCurrencies
                    )
                }
            }

            override fun onFailure(message: String) {

            }

        })
    }

    fun saveCurrency(currency: Currency) {
        if (currency.currencyCode1.isNullOrEmpty() || currency.currencyName1.isNullOrEmpty() || currency.currencyCode2.isNullOrEmpty() || currency.currencyName2.isNullOrEmpty() || currency.currencyRate.isNullOrEmpty()) {
            manageCurrenciesState.value = manageCurrenciesState.value.copy(
                warning = "Please fill all inputs",
                isLoading = false
            )
            return
        }
        manageCurrenciesState.value = manageCurrenciesState.value.copy(
            isLoading = true
        )
        val callback = object : OnResult {
            override fun onSuccess(result: Any) {
                viewModelScope.launch(Dispatchers.Main) {
                    manageCurrenciesState.value = manageCurrenciesState.value.copy(
                        selectedCurrency = result as Currency,
                        isLoading = false
                    )
                }
            }

            override fun onFailure(message: String) {
                viewModelScope.launch(Dispatchers.Main) {
                    manageCurrenciesState.value = manageCurrenciesState.value.copy(
                        isLoading = false
                    )
                }
            }

        }
        CoroutineScope(Dispatchers.IO).launch {
            if (currency.currencyDocumentId.isNullOrEmpty()) {
                currency.currencyId = Utils.generateRandomUuidString()
                currencyRepository.insert(currency, callback)
            } else {
                currencyRepository.update(currency, callback)
            }
        }
    }

    fun deleteSelectedCurrency(currency: Currency) {
        if (currency.currencyCode1.isNullOrEmpty() || currency.currencyName1.isNullOrEmpty() || currency.currencyCode2.isNullOrEmpty() || currency.currencyName2.isNullOrEmpty() || currency.currencyRate.isNullOrEmpty()) {
            manageCurrenciesState.value = manageCurrenciesState.value.copy(
                warning = "Please select an Currency to delete",
                isLoading = false
            )
            return
        }
        manageCurrenciesState.value = manageCurrenciesState.value.copy(
            warning = null,
            isLoading = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            currencyRepository.delete(currency, object : OnResult {
                override fun onSuccess(result: Any) {
                    val currencies = manageCurrenciesState.value.currencies
                    val position =
                        currencies.indexOfFirst {
                            currency.currencyId.equals(
                                it.currencyId,
                                ignoreCase = true
                            )
                        }
                    if (position >= 0) {
                        currencies.removeAt(position)
                    }
                    viewModelScope.launch(Dispatchers.Main) {
                        manageCurrenciesState.value = manageCurrenciesState.value.copy(
                            currencies = currencies,
                            selectedCurrency = Currency(),
                            isLoading = false
                        )
                    }
                }

                override fun onFailure(message: String) {
                    viewModelScope.launch(Dispatchers.Main) {
                        manageCurrenciesState.value = manageCurrenciesState.value.copy(
                            isLoading = false
                        )
                    }
                }

            })
        }
    }

}