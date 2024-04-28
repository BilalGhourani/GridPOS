package com.grid.pos.ui.currency

import com.grid.pos.data.Currency.Currency

data class ManageCurrenciesState(
    val currencies: MutableList<Currency> = mutableListOf(),
    var selectedCurrency: Currency = Currency(),
    val isLoading: Boolean = false,
    val warning: String? = null,
    )