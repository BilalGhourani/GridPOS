package com.grid.pos.ui.reports

import com.grid.pos.data.Invoice.Invoice
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.model.Event
import com.grid.pos.model.InvoiceItemModel

data class ReportsState(
    var invoices: MutableList<Invoice> = mutableListOf(),
    var invoiceHeaders: MutableList<InvoiceHeader> = mutableListOf(),
    var step: Int = 1,
    var isLoading: Boolean = false,
    var clear: Boolean = false,
    var warning: Event<String>? = null,
)