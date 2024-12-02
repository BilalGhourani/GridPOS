package com.grid.pos.data.item

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ItemDao {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Item)

    // insert list of Items
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(order: List<Item>)

    // Delete an Item
    @Delete
    suspend fun delete(item: Item)

    // Delete all Items
    @Query("DELETE FROM st_item")
    suspend fun deleteAll()

    // Update an Item
    @Update
    suspend fun update(item: Item)

    // Update list of Item
    @Update
    suspend fun update(items: List<Item>)

    // Get all Items as stream.
    @Query("SELECT * FROM `st_item` WHERE it_cmp_id =:companyID")
    fun getAllItems(companyID: String): MutableList<Item>

    // Get One Item By Printer as stream.
    @Query("SELECT * FROM `st_item` WHERE it_fa_id =:familyId LIMIT 1")
    fun getOneItemByFamily(familyId: String): Item?

    // Get One Item By Printer as stream.
    @Query("SELECT * FROM `st_item` WHERE it_printer =:printerID LIMIT 1")
    fun getOneItemByPrinter(printerID: String): Item?

}