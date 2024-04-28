package com.grid.pos.data.Currency

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.grid.pos.data.Company.Company
import kotlinx.coroutines.flow.Flow

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

    // Get Currency by it's ID
    @Query("SELECT * FROM `currency` WHERE cur_id = :id")
    suspend fun getCurrencyById(id: String): Currency

    // Get all Currencies as stream.
    @Query("SELECT * FROM `currency`")
    fun getAllCurrencies(): Flow<List<Currency>>
}