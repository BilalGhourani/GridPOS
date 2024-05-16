package com.grid.pos.ui.table

import com.grid.pos.data.InvoiceHeader.InvoiceHeader

data class TablesState(
    var invoiceHeader: InvoiceHeader = InvoiceHeader(),
    var step: Int = 1,
    val isLoading: Boolean = false,
    var clear: Boolean = false,
    val warning: String? = null,
)