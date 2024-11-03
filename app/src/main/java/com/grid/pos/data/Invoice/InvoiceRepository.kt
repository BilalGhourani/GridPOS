package com.grid.pos.data.Invoice

import java.util.Date

interface InvoiceRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(invoice: Invoice): Invoice

    // Delete an Invoice
    suspend fun delete(invoice: Invoice)

    // Update an Invoice
    suspend fun update(invoice: Invoice)

    // Update list of Invoices
    suspend fun update(invoices: List<Invoice>)

    // Get all Invoices logs as stream.
    suspend fun getAllInvoices(invoiceHeaderId: String): MutableList<Invoice>

    // Get all Invoices between Dates.
    suspend fun getInvoicesByIds(
            ids: List<String>,
            itemId: String? = null
    ): MutableList<Invoice>

    // Get all Invoices between Dates.
    suspend fun getAllInvoicesForAdjustment(
            itemId: String? = null
    ): MutableList<Invoice>

    suspend fun getOneInvoiceByItemID(itemId: String): Invoice?

}
