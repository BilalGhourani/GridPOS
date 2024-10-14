package com.grid.pos.model

import com.grid.pos.data.DataModel

data class CurrencyModel(
        var currencyCode: String,
        var currencyName: String
) : DataModel() {

    override fun getId(): String {
        return currencyCode
    }

    override fun getName(): String {
        return currencyName
    }

}
