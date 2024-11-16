package com.grid.pos.ui.item

import com.grid.pos.data.Company.Company
import com.grid.pos.data.Family.Family
import com.grid.pos.data.Item.Item
import com.grid.pos.data.PosPrinter.PosPrinter
import com.grid.pos.model.CurrencyModel
import com.grid.pos.model.Event

data class ManageItemsState(
        val items: MutableList<Item> = mutableListOf(),
        val families: MutableList<Family> = mutableListOf(),
        val printers: MutableList<PosPrinter> = mutableListOf(),
        val currencies: MutableList<CurrencyModel> = mutableListOf(),
        var selectedItem: Item = Item(),
        var isLoading: Boolean = false,
        var clear: Boolean = false,
        val warning: Event<String>? = null,
        val actionLabel: String? = null,
)