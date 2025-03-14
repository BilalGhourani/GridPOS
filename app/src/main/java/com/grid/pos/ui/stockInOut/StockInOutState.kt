package com.grid.pos.ui.stockInOut

import com.grid.pos.data.item.Item
import com.grid.pos.data.stockHeaderInOut.StockHeaderInOut
import com.grid.pos.model.DivisionModel
import com.grid.pos.model.TransactionTypeModel
import com.grid.pos.model.WarehouseModel

data class StockInOutState(
    val stockHeaderInOutList: MutableList<StockHeaderInOut> = mutableListOf(),
    val items: MutableList<Item> = mutableListOf(),
    val warehouses: MutableList<WarehouseModel> = mutableListOf(),
    val divisions: MutableList<DivisionModel> = mutableListOf(),
    val transactionTypes: MutableList<TransactionTypeModel> = mutableListOf(),
)