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
    suspend fun getAllItems(): DataModel

    suspend fun getItemsForPOS(): DataModel

    suspend fun getOneItemByPrinter(printerID: String): DataModel
    suspend fun generateBarcode(): DataModel

    suspend fun getOneItemByFamily(familyId: String): DataModel
    suspend fun updateWarehouseData(item: Item) : DataModel
    suspend fun updateOpening(item: Item) : DataModel

}
