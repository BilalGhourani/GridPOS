package com.grid.pos.ui.stockInOut

import com.grid.pos.data.item.Item
import com.grid.pos.data.stockHeadInOut.header.StockHeaderInOut
import com.grid.pos.model.Event
import com.grid.pos.model.WarehouseModel

data class StockInOutState(
        val stockHeaderInOutList: MutableList<StockHeaderInOut> = mutableListOf(),
        val items: MutableList<Item> = mutableListOf(),
        val warehouses: MutableList<WarehouseModel> = mutableListOf(),
        val stockHeaderInOut: StockHeaderInOut = StockHeaderInOut(),
        val isLoading: Boolean = false,
        val clear: Boolean = false,
        val warning: Event<String>? = null,
        val actionLabel: String? = null,
)