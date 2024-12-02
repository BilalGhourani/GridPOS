package com.grid.pos.data.posReceipt

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PosReceiptDao {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posReceipt: PosReceipt)

    // Delete a POS Receipt
    @Delete
    suspend fun delete(posReceipt: PosReceipt)

    // Update a POS Receipt
    @Update
    suspend fun update(posReceipt: PosReceipt)

    // Get POS Receipt by it's ID
    @Query("SELECT * FROM pos_receipt WHERE pr_id = :id")
    suspend fun getPosReceiptById(id: String): PosReceipt


    // Get POS Receipt by it's ID
    @Query("SELECT * FROM pos_receipt WHERE pr_hi_id = :id")
    suspend fun getPosReceiptByInvoice(id: String): PosReceipt
}