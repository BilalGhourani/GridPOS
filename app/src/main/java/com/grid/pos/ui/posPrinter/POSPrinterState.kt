package com.grid.pos.ui.posPrinter

import com.grid.pos.data.posPrinter.PosPrinter

data class POSPrinterState(
    val printers: MutableList<PosPrinter> = mutableListOf(),
    val printer: PosPrinter = PosPrinter(),
    val posPrinterPortStr: String = ""
)