package com.grid.pos.data.PosPrinter

import com.grid.pos.data.Item.Item
import com.grid.pos.interfaces.OnResult
import kotlinx.coroutines.flow.Flow

interface PosPrinterRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(posPrinter: PosPrinter,callback: OnResult?)

    // Delete a POS Printer
    suspend fun delete(posPrinter: PosPrinter,callback: OnResult?)

    // Update a POS Printer
    suspend fun update(posPrinter: PosPrinter,callback: OnResult?)

    // Get POS Receipt by it's ID
    suspend fun getPosPrinterById(id: String): PosPrinter

    // Get all POS Receipts as stream.
    suspend fun getAllPosPrinters(callback: OnResult?)


}
