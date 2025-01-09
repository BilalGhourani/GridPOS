package com.grid.pos.model

import com.grid.pos.data.item.Item
import com.grid.pos.data.stockAdjustment.StockAdjustment

data class StockAdjItemModel(
    val stockAdjustment: StockAdjustment = StockAdjustment(),
    var stockAdjItem: Item = Item(),
    var isDeleted: Boolean = false
) {

    fun setItem(item: Item) {
        stockAdjItem = item
        stockAdjustment.stockAdjItemId = item.itemId
        stockAdjustment.stockAdjQty = 1.0
        stockAdjustment.stockAdjCost =
            if (item.itemRealOpenCost == 0.0) item.itemOpenCost else item.itemRealOpenCost
    }

    fun getName(): String {
        return stockAdjItem.itemName ?: "Item"
    }
}
