package com.grid.pos.ui.posPrinter

import com.grid.pos.data.PosPrinter.PosPrinter
import com.grid.pos.model.Event

data class POSPrinterState(
        val printers: MutableList<PosPrinter> = mutableListOf(),
        var selectedPrinter: PosPrinter = PosPrinter(),
        var isLoading: Boolean = false,
        val clear: Boolean = false,
        val warning: Event<String>? = null,
        val actionLabel: String? = null,
    )