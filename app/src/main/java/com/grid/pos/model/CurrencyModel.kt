package com.grid.pos.model

import com.grid.pos.data.DataModel

data class CurrencyModel(
        var currencyId: String,
        var currencyCode: String,
        var currencyName: String
) : DataModel() {

    override fun getId(): String {
        return currencyId
    }

    override fun getName(): String {
        return currencyName
    }

}
