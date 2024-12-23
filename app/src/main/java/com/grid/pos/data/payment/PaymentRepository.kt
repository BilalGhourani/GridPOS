package com.grid.pos.data.payment

import com.grid.pos.model.DataModel

interface PaymentRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(payment: Payment): DataModel

    // Delete a Payment
    suspend fun delete(payment: Payment):DataModel

    // Update a Payment
    suspend fun update(payment: Payment):DataModel

    // Get Payment by it's ID
    suspend fun getPaymentById(id: String): DataModel

    // Get all Companies logs as stream.
    suspend fun getAllPayments(): DataModel
    suspend fun getLastTransactionNo(): DataModel

}
