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

    // insert list of Invoice Headers
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(order: List<InvoiceHeader>)

    // Delete an Invoice Header
    @Delete
    suspend fun delete(invoiceHeader: InvoiceHeader)

    // Delete all Invoice Headers
    @Query("DELETE FROM in_hinvoice")
    suspend fun deleteAll()

    // Update an Invoice Header
    @Update
    suspend fun update(invoiceHeader: InvoiceHeader)

    // Get all Invoice Headers as stream.
    @Query("SELECT * FROM `in_hinvoice` WHERE hi_cmp_id=:companyId ORDER BY hi_transno DESC")
    fun getAllInvoiceHeaders(companyId: String): MutableList<InvoiceHeader>

    // Get all Invoice Headers as stream.
    @Query("SELECT * FROM `in_hinvoice` WHERE hi_tt_code = :type AND hi_cmp_id=:companyId ORDER BY hi_datetime DESC LIMIT 1")
    fun getLastInvoiceNo(
            type: String,
            companyId: String
    ): InvoiceHeader?

    // Get last Invoice Headers as stream.
    @Query("SELECT * FROM `in_hinvoice` WHERE hi_cmp_id=:companyId ORDER BY hi_transno DESC LIMIT 1")
    fun getLastInvoice(
            companyId: String
    ): InvoiceHeader?

    // Get all Invoice Headers as stream.
    @Query("SELECT * FROM `in_hinvoice` WHERE hi_ta_name = :tableNo AND hi_cmp_id=:companyId AND (hi_transno IS NULL OR hi_transno = '') LIMIT 1")
    fun getInvoiceByTable(
            tableNo: String,
            companyId: String
    ): InvoiceHeader?

    // Get all Invoices as stream.
    @Query("SELECT * FROM `in_hinvoice` WHERE hi_datetime >= :from AND hi_datetime<= :to AND hi_cmp_id=:companyId")
    fun getInvoicesBetween(
            from: Long,
            to: Long,
            companyId: String
    ): MutableList<InvoiceHeader>

    @Query("SELECT * FROM `in_hinvoice` WHERE hi_userstamp = :userID LIMIT 1")
    fun getOneInvoiceByUserId(userID: String): InvoiceHeader?

    @Query("SELECT * FROM `in_hinvoice` WHERE hi_tp_name = :clientID LIMIT 1")
    fun getOneInvoiceByClientId(clientID: String): InvoiceHeader?

}