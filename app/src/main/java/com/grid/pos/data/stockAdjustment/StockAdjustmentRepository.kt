package com.grid.pos.data.stockAdjustment

import com.grid.pos.model.DataModel

interface StockAdjustmentRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(
        stockAdjustment: StockAdjustment,
        source:String
    ): DataModel

    // Delete an Stock Adjustment
    suspend fun delete(stockAdjustment: StockAdjustment): DataModel

    // Update an Stock Adjustment
    suspend fun update(
        stockAdjustment: StockAdjustment,
        source:String
    ): DataModel

    suspend fun getAllStockAdjustments(stockHeaderAdjId: String): MutableList<StockAdjustment>
}
