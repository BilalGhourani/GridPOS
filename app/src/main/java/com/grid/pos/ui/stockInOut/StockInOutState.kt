package com.grid.pos.ui.stockInOut

import com.grid.pos.data.item.Item
import com.grid.pos.data.stockHeaderAdjustment.StockHeaderAdjustment
import com.grid.pos.model.CurrencyModel
import com.grid.pos.model.Event
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.StockAdjItemModel
import com.grid.pos.model.WarehouseModel

data class StockInOutState(
        val itemsToDelete: MutableList<StockAdjItemModel> = mutableListOf(),
        val stockHeaderAdjustments: MutableList<StockHeaderAdjustment> = mutableListOf(),
        val items: MutableList<Item> = mutableListOf(),
        val warehouses: MutableList<WarehouseModel> = mutableListOf(),
        val isLoading: Boolean = false,
        val clear: Boolean = false,
        val warning: Event<String>? = null,
        val actionLabel: String? = null,
)