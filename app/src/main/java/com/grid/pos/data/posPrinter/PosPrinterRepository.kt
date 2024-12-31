package com.grid.pos.data.posPrinter

import com.grid.pos.model.DataModel

interface PosPrinterRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(posPrinter: PosPrinter): DataModel

    // Delete a POS Printer
    suspend fun delete(posPrinter: PosPrinter):DataModel

    // Update a POS Printer
    suspend fun update(posPrinter: PosPrinter):DataModel


    // Get all POS Receipts as stream.
    suspend fun getAllPosPrinters(): MutableList<PosPrinter>

    suspend fun getOnePosPrinter(companyId: String): PosPrinter?


}
