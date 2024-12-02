package com.grid.pos.data.payment

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PaymentDao {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: Payment)

    // insert list of Payments
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(order: List<Payment>)

    // Delete a Payment
    @Delete
    suspend fun delete(payment: Payment)

    // Delete all companies
    @Query("DELETE FROM payment")
    suspend fun deleteAll()

    // Update a Payment
    @Update
    suspend fun update(payment: Payment)

    // Update list of Payments
    @Update
    suspend fun updateAll(payments: List<Payment>)

    // Get Payment by it's ID
    @Query("SELECT * FROM payment WHERE pay_id = :id")
    suspend fun getPaymentById(id: String): Payment

    // Get all Payments as stream.
    @Query("SELECT * FROM `payment`")
    fun getAllPayments(): MutableList<Payment>

    // Get last Payments with Transaction no as stream.
    @Query("SELECT * FROM `payment` WHERE pay_cmp_id=:companyId AND (pay_transno IS NOT NULL AND pay_transno != '') ORDER BY pay_transno DESC LIMIT 1")
    fun getLastTransactionByType(
            companyId: String
    ): Payment?

}