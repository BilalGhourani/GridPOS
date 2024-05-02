package com.grid.pos.data.Currency

import com.grid.pos.interfaces.OnResult
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(currency: Currency,callback: OnResult? = null)

    // Delete a Currency
    suspend fun delete(currency: Currency,callback: OnResult? = null)

    // Update a Currency
    suspend fun update(currency: Currency,callback: OnResult? = null)

    // Get Currency by it's ID
    suspend fun getCurrencyById(id: String): Currency

    // Get all Currencies as stream.
   suspend fun getAllCurrencies(callback: OnResult? = null)

}
