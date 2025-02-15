package com.grid.pos.ui.item.opening

import com.grid.pos.data.item.Item
import com.grid.pos.model.CurrencyModel
import com.grid.pos.model.WarehouseModel

data class ItemOpeningState(
        val items: MutableList<Item> = mutableListOf(),
        val warehouses: MutableList<WarehouseModel> = mutableListOf(),
        val currencies: MutableList<CurrencyModel> = mutableListOf(),
        val selectedItem: Item? = null,

        val barcodeSearchState :String = "",

        val warehouseState :String = "",
        val locationState :String = "",
        val openQtyState :String = "",

        val currencyIndexState:Int = 0,
        val costState :String = "",
        val costFirstState :String = "",
        val costSecondState :String = ""
)