package com.grid.pos.ui.table

import com.grid.pos.data.invoiceHeader.InvoiceHeader
import com.grid.pos.model.TableModel

data class TablesState(
        val tables : MutableList<TableModel> = mutableListOf(),
        var invoiceHeader: InvoiceHeader = InvoiceHeader(),
        var tableName:String = "",
        var clientCount:String = "",
        var step: Int = 1,
        val isLoadingTables: Boolean = false
)