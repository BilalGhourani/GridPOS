package com.grid.pos.data.currency

import com.grid.pos.model.CurrencyModel

interface CurrencyRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(currency: Currency): Currency

    // Delete a Currency
    suspend fun delete(currency: Currency)

    // Update a Currency
    suspend fun update(currency: Currency)

    // Get all Currencies as stream.
    suspend fun getAllCurrencies(): MutableList<Currency>
    suspend fun getRate(firstCurr: String, secondCurr: String): Double

    // Get all Currencies as stream.
    suspend fun getAllCurrencyModels(): MutableList<CurrencyModel>

}
