package com.grid.pos.model

import com.grid.pos.data.item.Item
import com.grid.pos.data.purchase.Purchase
import com.grid.pos.data.stockInOut.StockInOut

data class PurchaseItemModel(
    val purchase: Purchase = Purchase(),
    var stockItem: Item = Item(),
    var isDeleted: Boolean = false
) {

    fun setItem(item: Item) {
        stockItem = item
        purchase.purchaseItId = item.itemId
        purchase.purchaseQty = 1.0
        purchase.purchaseCost =
            if (item.itemRealOpenCost == 0.0) item.itemOpenCost else item.itemRealOpenCost
    }

    fun getName(): String {
        return stockItem.itemName ?: "Item"
    }
}
