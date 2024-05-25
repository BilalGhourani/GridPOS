package com.grid.pos.data.Invoice

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.grid.pos.data.Item.Item
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invoice: Invoice)

    // insert list of Invoices
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(order: List<Invoice>)

    // Delete an Invoice
    @Delete
    suspend fun delete(invoice: Invoice)

    // Delete all Items
    @Query("DELETE FROM in_invoice")
    suspend fun deleteAll()

    // Update an Invoice
    @Update
    suspend fun update(invoice: Invoice)

    // Get Invoice by it's ID
    @Query("SELECT * FROM in_invoice WHERE in_id = :id")
    suspend fun getInvoiceById(id: String): Invoice

    // Get all Invoices as stream.
    @Query("SELECT * FROM `in_invoice`")
    fun getAllInvoices(): Flow<List<Invoice>>

    // Get all Invoices as stream.
    @Query("SELECT * FROM `in_invoice` WHERE in_hi_id = :id")
    fun getAllInvoiceItems(id: String): Flow<List<Invoice>>

    // Get all Invoices as stream.
    @Query("SELECT * FROM `in_invoice` WHERE in_datetime >= :from AND in_datetime<= :to")
    fun getInvoicesBetween(from: Long,to:Long): Flow<List<Invoice>>

}