package com.grid.pos.data.InvoiceHeader

import java.util.Date

interface InvoiceHeaderRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(invoiceHeader: InvoiceHeader,isFinished: Boolean = false): InvoiceHeader

    // Delete an Invoice Header
    suspend fun delete(invoiceHeader: InvoiceHeader)

    // Update an Invoice Header
    suspend fun update(invoiceHeader: InvoiceHeader,isFinished: Boolean = false)

    // Get all Invoice Headers logs as stream.
    suspend fun getAllInvoiceHeaders(): MutableList<InvoiceHeader>
    suspend fun getLastInvoiceByType(type: String): InvoiceHeader?
    suspend fun getLastInvoice(): InvoiceHeader?
    suspend fun getInvoiceByTable(tableNo: String): InvoiceHeader?
    suspend fun getInvoicesBetween(
            from: Date,
            to: Date
    ): MutableList<InvoiceHeader>

    suspend fun getOneInvoiceByUserID(userId: String): InvoiceHeader?

    suspend fun getOneInvoiceByClientID(clientId: String): InvoiceHeader?
}
