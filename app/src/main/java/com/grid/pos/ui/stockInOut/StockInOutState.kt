package com.grid.pos.ui.stockInOut

import com.grid.pos.data.item.Item
import com.grid.pos.model.CurrencyModel
import com.grid.pos.model.Event
import com.grid.pos.model.WarehouseModel

data class StockInOutState(
        val items: MutableList<Item> = mutableListOf(),
        val warehouses: MutableList<WarehouseModel> = mutableListOf(),
        val currencies: MutableList<CurrencyModel> = mutableListOf(),
        var selectedItem: Item? = null,
        val isLoading: Boolean = false,
        val clear: Boolean = false,
        val warning: Event<String>? = null,
        val actionLabel: String? = null,
)