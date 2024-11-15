package com.grid.pos.data.Receipt

interface ReceiptRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(receipt: Receipt): Receipt

    // Delete a Receipt
    suspend fun delete(receipt: Receipt)

    // Update a Receipt
    suspend fun update(receipt: Receipt)

    // Get Receipt by it's ID
    suspend fun getReceiptById(id: String): Receipt?

    // Get all Receipts logs as stream.
    suspend fun getAllReceipts(): MutableList<Receipt>

}
