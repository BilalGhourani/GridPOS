package com.grid.pos.data.posPrinter

interface PosPrinterRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(posPrinter: PosPrinter): PosPrinter

    // Delete a POS Printer
    suspend fun delete(posPrinter: PosPrinter)

    // Update a POS Printer
    suspend fun update(posPrinter: PosPrinter)


    // Get all POS Receipts as stream.
    suspend fun getAllPosPrinters(): MutableList<PosPrinter>

    suspend fun getOnePosPrinter(companyId: String): PosPrinter?


}
