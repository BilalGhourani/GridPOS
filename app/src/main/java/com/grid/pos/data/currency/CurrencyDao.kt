package com.grid.pos.data.currency

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CurrencyDao {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(currency: Currency)

    // insert list of Currencies
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(order: List<Currency>)

    // Delete a Currency
    @Delete
    suspend fun delete(currency: Currency)

    // Delete all Currencies
    @Query("DELETE FROM currency")
    suspend fun deleteAll()

    // Update a Currency
    @Update
    suspend fun update(currency: Currency)


    // Get all Currencies as stream.
    @Query("SELECT * FROM `currency` WHERE cur_cmp_id = :companyId")
    fun getAllCurrencies(companyId:String): MutableList<Currency>
}