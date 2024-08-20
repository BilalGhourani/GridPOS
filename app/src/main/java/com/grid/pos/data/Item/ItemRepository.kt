package com.grid.pos.data.Item

interface ItemRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(item: Item): Item

    // Delete an Item
    suspend fun delete(item: Item)

    // Update an Item
    suspend fun update(item: Item)

    // Get all Items logs as stream.
    suspend fun getAllItems(): MutableList<Item>

    suspend fun getOneItemByPrinter(printerID: String): Item?

    suspend fun getOneItemByFamily(familyId: String): Item?

}
