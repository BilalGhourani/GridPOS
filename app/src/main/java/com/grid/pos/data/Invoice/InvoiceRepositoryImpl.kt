package com.grid.pos.data.Invoice

import kotlinx.coroutines.flow.Flow

class InvoiceRepositoryImpl(
    private val invoiceDao: InvoiceDao
) : InvoiceRepository {
    override suspend fun insert(invoice: Invoice) {
        invoiceDao.insert(invoice)
    }

    override suspend fun delete(invoice: Invoice) {
        invoiceDao.delete(invoice)
    }

    override suspend fun update(invoice: Invoice) {
        invoiceDao.update(invoice)
    }

    override suspend fun getInvoiceById(id: String): Invoice {
        return invoiceDao.getInvoiceById(id)
    }

    override fun getAllInvoices(): Flow<List<Invoice>> {
        return invoiceDao.getAllInvoices()
    }
}