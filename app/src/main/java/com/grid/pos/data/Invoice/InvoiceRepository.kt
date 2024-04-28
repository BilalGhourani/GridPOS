package com.grid.pos.data.Invoice

import com.grid.pos.interfaces.OnResult
import kotlinx.coroutines.flow.Flow

interface InvoiceRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(invoice: Invoice, callback: OnResult?)

    // Delete an Invoice
    suspend fun delete(invoice: Invoice, callback: OnResult?)

    // Update an Invoice
    suspend fun update(invoice: Invoice, callback: OnResult?)

    // Get Invoice by it's ID
    suspend fun getInvoiceById(id: String): Invoice

    // Get all Invoices logs as stream.
    suspend fun getAllInvoices(invoiceHeaderId: String, callback: OnResult?)

}
