package com.grid.pos.data.Item

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.model.SettingsModel
import kotlinx.coroutines.tasks.await

class ItemRepositoryImpl(
        private val itemDao: ItemDao
) : ItemRepository {
    override suspend fun insert(item: Item): Item {
        if (SettingsModel.isConnectedToFireStore()) {
            val docRef = FirebaseFirestore.getInstance().collection("st_item").add(item.getMap())
                .await()
            item.itemDocumentId = docRef.id
        } else {
            itemDao.insert(item)
        }
        return item
    }

    override suspend fun delete(item: Item) {
        if (SettingsModel.isConnectedToFireStore()) {
            item.itemDocumentId?.let {
                FirebaseFirestore.getInstance().collection("st_item").document(it)
                    .delete().await()
            }
        } else {
            itemDao.delete(item)
        }
    }

    override suspend fun update(item: Item) {
        if (SettingsModel.isConnectedToFireStore()) {
            item.itemDocumentId?.let {
                FirebaseFirestore.getInstance().collection("st_item").document(it)
                    .update(item.getMap()).await()
            }
        } else {
            itemDao.update(item)
        }
    }

    override suspend fun getItemById(id: String): Item {
        return itemDao.getItemById(id)
    }

    override suspend fun getAllItems(): MutableList<Item> {
        if (SettingsModel.isConnectedToFireStore()) {
            val querySnapshot = FirebaseFirestore.getInstance().collection("st_item").whereEqualTo(
                "it_cmp_id",
                SettingsModel.companyID
            ).get().await()

            val items = mutableListOf<Item>()
            if (querySnapshot.size() > 0) {
                for (document in querySnapshot) {
                    val obj = document.toObject(Item::class.java)
                    if (obj.itemId.isNotEmpty()) {
                        obj.itemDocumentId = document.id
                        items.add(obj)
                    }
                }
            }
            return items
        } else {
            return itemDao.getAllItems()
        }
    }

}