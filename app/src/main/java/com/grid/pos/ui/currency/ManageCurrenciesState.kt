package com.grid.pos.ui.currency

import com.grid.pos.data.Currency.Currency
import com.grid.pos.model.Event

data class ManageCurrenciesState(
        var selectedCurrency: Currency = Currency(),
        val isLoading: Boolean = false,
        var fillFields: Boolean = false,
        val warning: Event<String>? = null,
    )