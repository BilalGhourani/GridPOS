package com.grid.pos.data.InvoiceHeader

import com.grid.pos.interfaces.OnResult
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface InvoiceHeaderRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(invoiceHeader: InvoiceHeader, callback: OnResult?)

    // Delete an Invoice Header
    suspend fun delete(invoiceHeader: InvoiceHeader, callback: OnResult?)

    // Update an Invoice Header
    suspend fun update(invoiceHeader: InvoiceHeader, callback: OnResult?)

    // Get Invoice Header by it's ID
    suspend fun getInvoiceHeaderById(id: String): InvoiceHeader

    // Get all Invoice Headers logs as stream.
    suspend fun getAllInvoiceHeaders(callback: OnResult?)
    suspend fun getLastInvoiceTransNo(type: String,callback: OnResult?)
    suspend fun getInvoiceByTable(tableNo: String,callback: OnResult?)
    suspend fun getInvoicesBetween(from: Date, to: Date, callback: OnResult?)

}
