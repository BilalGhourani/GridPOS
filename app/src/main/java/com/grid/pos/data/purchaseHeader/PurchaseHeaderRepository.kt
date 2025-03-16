package com.grid.pos.data.purchaseHeader

import com.grid.pos.model.DataModel

interface PurchaseHeaderRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(
       purchaseHeader: PurchaseHeader
    ): DataModel

    // Delete an PurchaseHeader
    suspend fun delete( purchaseHeader: PurchaseHeader): DataModel

    // Update an PurchaseHeader
    suspend fun update(
        purchaseHeader: PurchaseHeader
    ): DataModel

    suspend fun getAllPurchaseHeaders(): MutableList<PurchaseHeader>

    suspend fun getPurchaseHeaderById(id:String): PurchaseHeader?
}
