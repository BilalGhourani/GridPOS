package com.grid.pos.data.stockAdjustment

import com.grid.pos.model.DataModel

interface StockAdjustmentRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(
        stockAdjustment: StockAdjustment
    ): DataModel

    // Delete an Stock Adjustment
    suspend fun delete(stockAdjustment: StockAdjustment): DataModel

    // Update an Stock Adjustment
    suspend fun update(
        stockAdjustment: StockAdjustment
    ): DataModel

    suspend fun getAllStockAdjustments(stockHeaderAdjId: String): MutableList<StockAdjustment>
}
