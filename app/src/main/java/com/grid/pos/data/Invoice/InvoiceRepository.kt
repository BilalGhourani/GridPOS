package com.grid.pos.data.Invoice

import kotlinx.coroutines.flow.Flow

interface InvoiceRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(invoice: Invoice)

    // Delete an Invoice
    suspend fun delete(invoice: Invoice)

    // Update an Invoice
    suspend fun update(invoice: Invoice)

    // Get Invoice by it's ID
    suspend fun getInvoiceById(id: String): Invoice

    // Get all Invoices logs as stream.
    fun getAllInvoices(): Flow<List<Invoice>>

}
