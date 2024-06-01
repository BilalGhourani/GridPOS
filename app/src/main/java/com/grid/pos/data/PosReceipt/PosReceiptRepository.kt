package com.grid.pos.data.PosReceipt

interface PosReceiptRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(posReceipt: PosReceipt):PosReceipt

    // Delete a POS Receipts
    suspend fun delete(posReceipt: PosReceipt)

    // Update a POS Receipt
    suspend fun update(
            posReceipt: PosReceipt
    )

    // Get POS Receipt by it's ID
    suspend fun getPosReceiptById(id: String): PosReceipt?

    // Get all POS Receipts as stream.
    suspend fun getAllPosReceipts(): MutableList<PosReceipt>

    suspend fun getPosReceiptByInvoice(
            invoiceHeaderId: String
    ): PosReceipt?

}
