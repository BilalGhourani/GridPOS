package com.grid.pos.ui.currency

import com.grid.pos.data.currency.Currency

data class ManageCurrenciesState(
    val currency: Currency = Currency(),
    val currencyName1DecStr: String = "",
    val currencyName2DecStr: String = "",
    val currencyRateStr: String = ""
)