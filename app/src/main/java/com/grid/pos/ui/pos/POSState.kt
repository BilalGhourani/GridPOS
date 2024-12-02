package com.grid.pos.ui.pos

import com.grid.pos.data.family.Family
import com.grid.pos.data.invoiceHeader.InvoiceHeader
import com.grid.pos.data.item.Item
import com.grid.pos.data.posPrinter.PosPrinter
import com.grid.pos.data.thirdParty.ThirdParty
import com.grid.pos.data.user.User
import com.grid.pos.model.Event
import com.grid.pos.model.InvoiceItemModel

data class POSState(
        val itemsToDelete: MutableList<InvoiceItemModel> = mutableListOf(),
        var families: MutableList<Family> = mutableListOf(),
        var items: MutableList<Item> = mutableListOf(),
        var thirdParties: MutableList<ThirdParty> = mutableListOf(),
        var invoiceHeaders: MutableList<InvoiceHeader> = mutableListOf(),
        var users: MutableList<User> = mutableListOf(),
        var printers: MutableList<PosPrinter> = mutableListOf(),
        var selectedThirdParty: ThirdParty = ThirdParty(),
        var isSaved: Boolean = false,
        var isDeleted: Boolean = false,
        var isLoading: Boolean = false,
        val warning: Event<String>? = null,
        val actionLabel: String? = null,
)