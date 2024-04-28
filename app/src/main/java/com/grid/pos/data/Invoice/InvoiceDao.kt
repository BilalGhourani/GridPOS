package com.grid.pos.data.Invoice

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invoice: Invoice)

    // Delete an Invoice
    @Delete
    suspend fun delete(invoice: Invoice)

    // Update an Invoice
    @Update
    suspend fun update(invoice: Invoice)

    // Get Invoice by it's ID
    @Query("SELECT * FROM in_invoice WHERE in_id = :id")
    suspend fun getInvoiceById(id: String): Invoice

    // Get all Invoices as stream.
    @Query("SELECT * FROM `in_invoice`")
    fun getAllInvoices(): Flow<List<Invoice>>

}