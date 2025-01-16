package com.grid.pos.data.stockHeaderAdjustment

import com.grid.pos.model.DataModel

interface StockHeaderAdjustmentRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(
        stockHeaderAdjustment: StockHeaderAdjustment
    ): DataModel

    // Delete an Stock header adjustment
    suspend fun delete( stockHeaderAdjustment: StockHeaderAdjustment): DataModel

    // Update an Stock header adjustment
    suspend fun update(
        stockHeaderAdjustment: StockHeaderAdjustment
    ): DataModel

    suspend fun getAllStockHeaderAdjustments(): MutableList<StockHeaderAdjustment>
}
