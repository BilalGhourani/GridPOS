package com.grid.pos.ui.adjustment

import com.grid.pos.data.item.Item
import com.grid.pos.model.Event

data class AdjustmentState(
    val items: MutableList<Item> = mutableListOf(),
    var selectedItem: Item? = null,

    var barcodeSearchedKey: String = "",
    var itemCostString: String = "",
    var fromDateString :String = "",
    var toDateString :String = "",

    var isLoading: Boolean = false,
    var clear: Boolean = false,
    var warning: Event<String>? = null,
    var actionLabel: String? = null,
)