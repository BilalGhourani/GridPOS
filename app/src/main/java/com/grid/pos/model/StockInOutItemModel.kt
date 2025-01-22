package com.grid.pos.model

import com.grid.pos.data.item.Item
import com.grid.pos.data.stockInOut.StockInOut

data class StockInOutItemModel(
    val stockInOut: StockInOut = StockInOut(),
    var stockItem: Item = Item(),
    var isDeleted: Boolean = false
) {

    fun setItem(item: Item) {
        stockItem = item
        stockInOut.stockInOutItemId = item.itemId
        stockInOut.stockInOutQty = 1.0
        stockInOut.stockInOutCost =
            if (item.itemRealOpenCost == 0.0) item.itemOpenCost else item.itemRealOpenCost
    }

    fun getName(): String {
        return stockItem.itemName ?: "Item"
    }
}
