package com.grid.pos.model

import com.grid.pos.data.invoiceHeader.InvoiceHeader

data class TableInvoiceModel(
        var invoiceHeader: InvoiceHeader?=null,
        var lockedByUser: String?=null,
)
