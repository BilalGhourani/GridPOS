package com.grid.pos.data.Invoice

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
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

    // Update list of Invoice
    @Update
    suspend fun update(invoices: List<Invoice>)

    // Get all Invoices as stream.
    @Query("SELECT * FROM `in_invoice`")
    fun getAllInvoices(): MutableList<Invoice>

    // Get all Invoices as stream.
    @Query("SELECT * FROM `in_invoice` WHERE in_hi_id = :id")
    fun getAllInvoiceItems(id: String): MutableList<Invoice>

    // Get all Invoices as stream.
    @Query("SELECT * FROM `in_invoice` WHERE in_it_id = :itemId AND in_hi_id IN (:ids)")
    fun getInvoicesByIds(
            itemId: String,
            ids: List<String>
    ): MutableList<Invoice>

    // Get all Invoices as stream.
    @Query("SELECT * FROM `in_invoice` WHERE in_it_id = :itemId")
    fun getInvoicesForItem(
            itemId: String
    ): MutableList<Invoice>

    // Get all Invoices as stream.
    @Query("SELECT * FROM `in_invoice` WHERE in_it_id = :itemId AND in_datetime >= :from AND in_datetime<= :to")
    fun getInvoicesForItemAndDates(
            itemId: String,
            from: Long,
            to: Long
    ): MutableList<Invoice>

    // Get all Invoices as stream.
    @Query("SELECT * FROM `in_invoice` WHERE in_datetime >= :from AND in_datetime<= :to")
    fun getInvoicesForDates(
            from: Long,
            to: Long
    ): MutableList<Invoice>

    // Get all Invoices as stream.
    @Query("SELECT * FROM `in_invoice` WHERE in_hi_id IN (:ids)")
    fun getInvoicesByIds(ids: List<String>): MutableList<Invoice>

    @Query("SELECT * FROM `in_invoice` WHERE in_it_id = :itemId LIMIT 1")
    fun getOneInvoiceByItemId(itemId: String): Invoice?

}