package com.grid.pos.ui.adjustment

import com.grid.pos.data.item.Item

data class AdjustmentState(
    val items: MutableList<Item> = mutableListOf(),
    var selectedItem: Item? = null,

    var barcodeSearchedKey: String = "",
    var itemCostString: String = "",
    var fromDateString :String = "",
    var toDateString :String = "",
)