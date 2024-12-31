package com.grid.pos.data.item

import com.grid.pos.model.DataModel

interface ItemRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(item: Item): DataModel

    // Delete an Item
    suspend fun delete(item: Item):DataModel

    // Update an Item
    suspend fun update(item: Item):DataModel

    // Update list of Items
    suspend fun update(items: List<Item>):DataModel

    // Get all Items logs as stream.
    suspend fun getAllItems(): MutableList<Item>

    suspend fun getItemsForPOS(): MutableList<Item>

    suspend fun getOneItemByPrinter(printerID: String): Item?
    suspend fun generateBarcode(): String

    suspend fun getOneItemByFamily(familyId: String): Item?
    suspend fun updateWarehouseData(item: Item):DataModel
    suspend fun updateOpening(item: Item):DataModel

}
