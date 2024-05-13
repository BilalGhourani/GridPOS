package com.grid.pos.data.PosReceipt

import com.grid.pos.interfaces.OnResult
import kotlinx.coroutines.flow.Flow

interface PosReceiptRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(posReceipt: PosReceipt, callback: OnResult?)

    // Delete a POS Receipts
    suspend fun delete(posReceipt: PosReceipt, callback: OnResult?)

    // Update a POS Receipt
    suspend fun update(posReceipt: PosReceipt, callback: OnResult?)

    // Get POS Receipt by it's ID
    suspend fun getPosReceiptById(id: String): PosReceipt

    // Get all POS Receipts as stream.
    suspend fun getAllPosReceipts(callback: OnResult?)

    suspend fun getPosReceiptByInvoice(
            invoiceHeaderId: String,
            callback: OnResult?
    )

}
