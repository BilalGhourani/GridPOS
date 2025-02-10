package com.grid.pos.ui.currency

import com.grid.pos.data.currency.Currency
import com.grid.pos.model.Event

data class ManageCurrenciesState(
    val currency: Currency = Currency(),
    val currencyName1DecStr: String = "",
    val currencyName2DecStr: String = "",
    val currencyRateStr: String = "",
    val isLoading: Boolean = false,
    var isSaved: Boolean = false,
    val warning: Event<String>? = null,
)