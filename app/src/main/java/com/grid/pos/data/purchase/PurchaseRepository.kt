package com.grid.pos.data.purchase

import com.grid.pos.model.DataModel

interface PurchaseRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(
        purchase: Purchase
    ): DataModel

    // Delete an Purchase
    suspend fun delete(purchase: Purchase): DataModel

    // Update an Purchase
    suspend fun update(
        purchase: Purchase
    ): DataModel

    suspend fun getAllPurchases(purchaseHeaderId: String): MutableList<Purchase>
}
