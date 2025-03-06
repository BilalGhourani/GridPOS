package com.grid.pos.ui.currency

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.grid.pos.SharedViewModel
import com.grid.pos.data.currency.Currency
import com.grid.pos.data.currency.CurrencyRepository
import com.grid.pos.data.invoiceHeader.InvoiceHeaderRepository
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ManageCurrenciesViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository,
    private val invoiceHeaderRepository: InvoiceHeaderRepository,
    private val sharedViewModel: SharedViewModel
) : BaseViewModel(sharedViewModel) {

     val state = mutableStateOf(ManageCurrenciesState())

    var currentCurrency: Currency = Currency()

    init {
        fetchCurrencies()
    }

    fun updateState(newState: ManageCurrenciesState) {
        state.value = newState
    }

    private fun isAnyChangeDone(): Boolean {
        return state.value.currency.didChanged(currentCurrency)
    }

    fun checkChanges(callback: () -> Unit) {
        if (isAnyChangeDone()) {
            sharedViewModel.showPopup(true,
                PopupModel().apply {
                    onDismissRequest = {
                        currentCurrency = state.value.currency
                        callback.invoke()
                    }
                    onConfirmation = {
                        saveCurrency {
                            checkChanges(callback)
                        }
                    }
                    dialogText = "Do you want to save your changes"
                    positiveBtnText = "Save"
                    negativeBtnText = "Close"
                    cancelable = false
                })
        } else {
            callback.invoke()
        }
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
        showLoading(true)
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
                showLoading(false)
            }
        }
    }

    fun saveCurrency(callback: (() -> Unit)?=null) {
        val currency = state.value.currency
        if (currency.currencyCode1.isNullOrEmpty() || currency.currencyName1.isNullOrEmpty() || currency.currencyCode2.isNullOrEmpty() || currency.currencyName2.isNullOrEmpty() || currency.currencyRate.isNaN()) {
            showLoading(false)
            showWarning("Please fill all inputs")
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            if (hasRelations(currency)) {
                withContext(Dispatchers.Main) {
                    showWarning("You can't update the Currency code!")
                }
                return@launch
            }
            withContext(Dispatchers.Main) {
                showLoading(true)
            }
            currentCurrency = currency
            val isInserting = currency.isNew()
            if (isInserting) {
                currency.prepareForInsert()
                val dataModel = currencyRepository.insert(currency)
                if (dataModel.succeed) {
                    val addedCurr = dataModel.data as Currency
                    SettingsModel.currentCurrency = addedCurr.copy()
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        showWarning("Currency saved successfully.")
                        callback?.invoke()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                    }
                }
            } else {
                val dataModel = currencyRepository.update(currency)
                if (dataModel.succeed) {
                    SettingsModel.currentCurrency = currency.copy()
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        showWarning("Currency saved successfully.")
                        callback?.invoke()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
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