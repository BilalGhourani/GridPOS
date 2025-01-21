package com.grid.pos.data.stockInOut.header

import com.grid.pos.data.stockHeadInOut.header.StockHeaderInOut
import com.grid.pos.model.DataModel

interface StockHeaderInOutRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(
        stockHeaderInOut: StockHeaderInOut
    ): DataModel

    // Delete an Stock In Out
    suspend fun delete(stockHeaderInOut: StockHeaderInOut): DataModel

    // Update an Stock In Out
    suspend fun update(
        stockHeaderInOut: StockHeaderInOut
    ): DataModel

    suspend fun getAllStockHeaderInOuts(): MutableList<StockHeaderInOut>
}
