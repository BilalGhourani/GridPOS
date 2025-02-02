package com.grid.pos.ui.posPrinter

import com.grid.pos.data.posPrinter.PosPrinter
import com.grid.pos.model.Event

data class POSPrinterState(
        val printers: MutableList<PosPrinter> = mutableListOf(),
        var isLoading: Boolean = false,
        val clear: Boolean = false,
        val warning: Event<String>? = null,
        val actionLabel: String? = null,
    )