package com.grid.pos.data.Invoice

interface InvoiceRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(invoice: Invoice): Invoice

    // Delete an Invoice
    suspend fun delete(invoice: Invoice)

    // Update an Invoice
    suspend fun update(invoice: Invoice)

    // Get all Invoices logs as stream.
    suspend fun getAllInvoices(invoiceHeaderId: String): MutableList<Invoice>

    // Get all Invoices between Dates.
    suspend fun getInvoicesByIds(ids: List<String>): MutableList<Invoice>

}
