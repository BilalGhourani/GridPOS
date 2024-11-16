package com.grid.pos.data.Payment

interface PaymentRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(payment: Payment): Payment

    // Delete a Payment
    suspend fun delete(payment: Payment)

    // Update a Payment
    suspend fun update(payment: Payment)

    // Get Payment by it's ID
    suspend fun getPaymentById(id: String): Payment?

    // Get all Companies logs as stream.
    suspend fun getAllPayments(): MutableList<Payment>
    suspend fun getLastTransactionNo(): Payment?

}
