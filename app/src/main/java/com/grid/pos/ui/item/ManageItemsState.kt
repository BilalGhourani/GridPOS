package com.grid.pos.ui.item

import com.grid.pos.data.family.Family
import com.grid.pos.data.item.Item
import com.grid.pos.data.posPrinter.PosPrinter
import com.grid.pos.model.CurrencyModel
import com.grid.pos.model.Event

data class ManageItemsState(
        val items: MutableList<Item> = mutableListOf(),
        val families: MutableList<Family> = mutableListOf(),
        val printers: MutableList<PosPrinter> = mutableListOf(),
        val currencies: MutableList<CurrencyModel> = mutableListOf(),
        var selectedItem: Item = Item(),
        val isLoading: Boolean = false,
        val clear: Boolean = false,
        val warning: Event<String>? = null,
        val actionLabel: String? = null,
)