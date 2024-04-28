package com.grid.pos.data.Item

import com.grid.pos.data.Family.Family
import com.grid.pos.interfaces.OnResult
import kotlinx.coroutines.flow.Flow

interface ItemRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(item: Item,callback: OnResult?)

    // Delete an Item
    suspend fun delete(item: Item,callback: OnResult?)

    // Update an Item
    suspend fun update(item: Item,callback: OnResult?)

    // Get Item by it's ID
    suspend fun getItemById(id: String): Item

    // Get all Items logs as stream.
    suspend fun getAllItems(callback: OnResult?)

}
