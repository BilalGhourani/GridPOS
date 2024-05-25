package com.grid.pos.ui.reports

import com.grid.pos.data.Invoice.Invoice
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.Item.Item
import com.grid.pos.model.Event
import com.grid.pos.model.InvoiceItemModel

data class ReportsState(
    var isLoading: Boolean = false,
    var clear: Boolean = false,
    var warning: Event<String>? = null,
)