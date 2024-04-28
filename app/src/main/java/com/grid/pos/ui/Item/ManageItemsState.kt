package com.grid.pos.ui.Item

import com.grid.pos.data.Company.Company
import com.grid.pos.data.Family.Family
import com.grid.pos.data.Item.Item
import com.grid.pos.data.PosPrinter.PosPrinter

data class ManageItemsState(
    val items: MutableList<Item> = mutableListOf(),
    val companies: MutableList<Company> = mutableListOf(),
    val families: MutableList<Family> = mutableListOf(),
    val printers: MutableList<PosPrinter> = mutableListOf(),
    var selectedItem: Item = Item(),
    val isLoading: Boolean = false,
    val warning: String? = null,
)