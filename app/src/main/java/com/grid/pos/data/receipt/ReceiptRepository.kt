package com.grid.pos.data.receipt

import com.grid.pos.model.DataModel

interface ReceiptRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(receipt: Receipt): DataModel

    // Delete a Receipt
    suspend fun delete(receipt: Receipt):DataModel

    // Update a Receipt
    suspend fun update(receipt: Receipt):DataModel

    // Get Receipt by it's ID
    suspend fun getReceiptById(id: String): DataModel

    // Get all Receipts logs as stream.
    suspend fun getAllReceipts(): DataModel

    suspend fun getLastTransactionNo(): DataModel

}
