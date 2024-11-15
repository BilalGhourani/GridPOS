package com.grid.pos.data.Receipt

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ReceiptDao {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(receipt: Receipt)

    // insert list of Receipts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(receipts: List<Receipt>)

    // Delete a Receipt
    @Delete
    suspend fun delete(receipt: Receipt)

    // Delete all companies
    @Query("DELETE FROM receipt")
    suspend fun deleteAll()

    // Update a Receipt
    @Update
    suspend fun update(receipt: Receipt)

    // Update list of Receipts
    @Update
    suspend fun updateAll(receipts: List<Receipt>)

    // Get Receipt by it's ID
    @Query("SELECT * FROM receipt WHERE rec_id = :id")
    suspend fun getReceiptById(id: String): Receipt

    // Get all Companies as stream.
    @Query("SELECT * FROM `receipt`")
    fun getAllReceipts(): MutableList<Receipt>

}