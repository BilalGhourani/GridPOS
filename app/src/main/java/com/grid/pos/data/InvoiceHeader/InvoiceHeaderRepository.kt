package com.grid.pos.data.InvoiceHeader

import kotlinx.coroutines.flow.Flow

interface InvoiceHeaderRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(invoiceHeader: InvoiceHeader)

    // Delete an Invoice Header
    suspend fun delete(invoiceHeader: InvoiceHeader)

    // Update an Invoice Header
    suspend fun update(invoiceHeader: InvoiceHeader)

    // Get Invoice Header by it's ID
    suspend fun getInvoiceHeaderById(id: String): InvoiceHeader

    // Get all Invoice Headers logs as stream.
    fun getAllInvoiceHeaders(): Flow<List<InvoiceHeader>>

}
