package com.grid.pos.data.InvoiceHeader

import com.grid.pos.model.TableModel
import java.util.Date

interface InvoiceHeaderRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(invoiceHeader: InvoiceHeader,isFinished: Boolean = false): InvoiceHeader

    // Delete an Invoice Header
    suspend fun delete(invoiceHeader: InvoiceHeader)

    // Update an Invoice Header
    suspend fun updateInvoiceHeader(invoiceHeader: InvoiceHeader)
    suspend fun update(invoiceHeader: InvoiceHeader,isFinished: Boolean = false)

    // Get all Invoice Headers logs as stream.
    suspend fun getAllInvoiceHeaders(): MutableList<InvoiceHeader>
    suspend fun getLastOrderByType(): InvoiceHeader?
    suspend fun getLastTransactionByType(type: String): InvoiceHeader?
    suspend fun getLastInvoice(): InvoiceHeader?

    suspend fun getAllOpenedTables(): MutableList<TableModel>
    suspend fun getInvoiceByTable(tableNo: String): InvoiceHeader
    suspend fun getInvoicesBetween(
            from: Date,
            to: Date
    ): MutableList<InvoiceHeader>

    suspend fun getOneInvoiceByUserID(userId: String): InvoiceHeader?

    suspend fun getOneInvoiceByClientID(clientId: String): InvoiceHeader?
}
