package com.grid.pos.data.InvoiceHeader

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceHeaderDao {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invoiceHeader: InvoiceHeader)

    // Delete an Invoice Header
    @Delete
    suspend fun delete(invoiceHeader: InvoiceHeader)

    // Update an Invoice Header
    @Update
    suspend fun update(invoiceHeader: InvoiceHeader)

    // Get Invoice Header by it's ID
    @Query("SELECT * FROM in_hinvoice WHERE hi_id = :id")
    suspend fun getInvoiceHeaderById(id: String): InvoiceHeader

    // Get all Invoice Headers as stream.
    @Query("SELECT * FROM `in_hinvoice`")
    fun getAllInvoiceHeaders(): Flow<List<InvoiceHeader>>

}