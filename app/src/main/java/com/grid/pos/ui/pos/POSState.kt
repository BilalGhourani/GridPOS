package com.grid.pos.ui.pos

import com.grid.pos.data.Family.Family
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.Item.Item
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.model.Event
import com.grid.pos.model.InvoiceItemModel

data class POSState(
        val itemsToDelete: MutableList<InvoiceItemModel> = mutableListOf(),
        var families: MutableList<Family> = mutableListOf(),
        var items: MutableList<Item> = mutableListOf(),
        var defaultThirdParty: ThirdParty? = null,
        var thirdParties: MutableList<ThirdParty> = mutableListOf(),
        var invoiceHeaders: MutableList<InvoiceHeader> = mutableListOf(),
        var selectedThirdParty: ThirdParty = ThirdParty(),
        var isSaved: Boolean = false,
        var isDeleted: Boolean = false,
        var isLoading: Boolean = false,
        val warning: Event<String>? = null,
        val actionLabel: String? = null,
)