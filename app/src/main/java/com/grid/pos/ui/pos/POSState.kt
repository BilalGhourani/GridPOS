package com.grid.pos.ui.pos

import com.grid.pos.data.Family.Family
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.Item.Item
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.model.InvoiceItemModel

data class POSState(
    var invoices: MutableList<InvoiceItemModel> = mutableListOf(),
    val families: MutableList<Family> = mutableListOf(),
    val items: MutableList<Item> = mutableListOf(),
    val thirdParties: MutableList<ThirdParty> = mutableListOf(),
    var invoiceHeader: InvoiceHeader = InvoiceHeader(),
    val isLoading: Boolean = false,
    val warning: String? = null,
)