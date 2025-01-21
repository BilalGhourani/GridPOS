package com.grid.pos.data.stockInOut

import com.grid.pos.model.DataModel

interface StockInOutRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(
        stockInOut: StockInOut
    ): DataModel

    // Delete an Stock In Out
    suspend fun delete( stockInOut: StockInOut): DataModel

    // Update an Stock In Out
    suspend fun update(
        stockInOut: StockInOut
    ): DataModel

    suspend fun getAllStockInOuts(stockHeaderInOutId: String): MutableList<StockInOut>
}
