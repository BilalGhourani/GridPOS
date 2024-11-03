package com.grid.pos.ui.adjustment

import com.grid.pos.data.Item.Item
import com.grid.pos.model.Event

data class AdjustmentState(
        val items: MutableList<Item> = mutableListOf(),
        var selectedItem: Item? = null,
        var isLoading: Boolean = false,
        var clear: Boolean = false,
        var warning: Event<String>? = null,
)