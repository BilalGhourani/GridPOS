package com.grid.pos.model

import com.grid.pos.data.InvoiceHeader.InvoiceHeader

data class TableInvoiceModel(
        var invoiceHeader: InvoiceHeader?=null,
        var lockedByUser: String?=null,
)
