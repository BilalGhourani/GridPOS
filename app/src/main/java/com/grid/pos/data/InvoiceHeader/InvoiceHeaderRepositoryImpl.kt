package com.grid.pos.data.InvoiceHeader

import kotlinx.coroutines.flow.Flow

class InvoiceHeaderRepositoryImpl(
    private val invoiceHeaderDao: InvoiceHeaderDao
) : InvoiceHeaderRepository {
    override suspend fun insert(invoiceHeader: InvoiceHeader) {
        invoiceHeaderDao.insert(invoiceHeader)
    }

    override suspend fun delete(invoiceHeader: InvoiceHeader) {
        invoiceHeaderDao.delete(invoiceHeader)
    }

    override suspend fun update(invoiceHeader: InvoiceHeader) {
        invoiceHeaderDao.update(invoiceHeader)
    }

    override suspend fun getInvoiceHeaderById(id: String): InvoiceHeader {
        return invoiceHeaderDao.getInvoiceHeaderById(id)
    }

    override fun getAllInvoiceHeaders(): Flow<List<InvoiceHeader>> {
        return invoiceHeaderDao.getAllInvoiceHeaders()
    }
}