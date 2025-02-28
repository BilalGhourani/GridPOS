package com.grid.pos.ui.stockAdjustment

import com.grid.pos.data.item.Item
import com.grid.pos.data.stockHeadInOut.header.StockHeaderInOut
import com.grid.pos.data.stockHeaderAdjustment.StockHeaderAdjustment
import com.grid.pos.model.DivisionModel
import com.grid.pos.model.TransactionTypeModel
import com.grid.pos.model.WarehouseModel

data class StockAdjustmentState(
    val stockHeaderAdjustmentList: MutableList<StockHeaderAdjustment> = mutableListOf(),
    val items: MutableList<Item> = mutableListOf(),
    val warehouses: MutableList<WarehouseModel> = mutableListOf(),
    val divisions: MutableList<DivisionModel> = mutableListOf(),
    val transactionTypes: MutableList<TransactionTypeModel> = mutableListOf(),
)