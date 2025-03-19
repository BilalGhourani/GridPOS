package com.grid.pos.model

import com.grid.pos.data.item.Item
import com.grid.pos.data.purchase.Purchase

data class PurchaseItemModel(
    val purchase: Purchase = Purchase(),
    var purchaseItem: Item = Item(),
    var isDeleted: Boolean = false
) {

    fun setItem(item: Item) {
        purchaseItem = item
        purchase.purchaseItId = item.itemId
        purchase.purchaseQty = 1.0
        purchase.purchasePrice =
            if (item.itemRealUnitPrice == 0.0) item.itemUnitPrice else item.itemRealUnitPrice
        purchase.purchaseCost =
            if (item.itemRealOpenCost == 0.0) item.itemOpenCost else item.itemRealOpenCost
        purchase.purchaseDisc = 0.0
        purchase.purchaseDiscAmt = 0.0
        purchase.purchaseVat = item.itemTax
        purchase.purchaseTax1 = item.itemTax1
        purchase.purchaseTax2 = item.itemTax2
        purchase.purchaseRemQty = item.itemRemQty
        purchase.purchaseDivName = item.it_div_name
    }

    fun getName(): String {
        return purchaseItem.itemName ?: "Item"
    }
}
