package com.grid.pos.ui.item

import com.grid.pos.data.family.Family
import com.grid.pos.data.item.Item
import com.grid.pos.data.posPrinter.PosPrinter
import com.grid.pos.model.CurrencyModel
import com.grid.pos.model.Event
import com.grid.pos.model.ItemGroupModel

data class ManageItemsState(
    val items: MutableList<Item> = mutableListOf(),
    val families: MutableList<Family> = mutableListOf(),
    val printers: MutableList<PosPrinter> = mutableListOf(),
    val currencies: MutableList<CurrencyModel> = mutableListOf(),
    val groups: MutableList<ItemGroupModel> = mutableListOf(),

    val item: Item = Item(),
    val itemUnitPriceStr: String = "",
    val itemTaxStr: String = "",
    val itemTax1Str: String = "",
    val itemTax2Str: String = "",
    val itemOpenQtyStr: String = "",
    val itemOpenCostStr: String = "",

    val isConnectingToSQLServer: Boolean = false,
    val isLoading: Boolean = false,
    val clear: Boolean = false,
    val warning: Event<String>? = null,
    val actionLabel: String? = null,
)