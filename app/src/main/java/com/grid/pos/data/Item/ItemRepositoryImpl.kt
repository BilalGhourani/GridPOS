package com.grid.pos.data.Item

import androidx.lifecycle.asLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.interfaces.OnResult
import com.grid.pos.model.SettingsModel

class ItemRepositoryImpl(
    private val itemDao: ItemDao
) : ItemRepository {
    override suspend fun insert(item: Item, callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("st_item")
                .add(item)
                .addOnSuccessListener {
                    item.itemDocumentId = it.id
                    callback?.onSuccess(item)
                }
                .addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        }else{
            itemDao.insert(item)
            callback?.onSuccess(item)
        }
    }

    override suspend fun delete(item: Item, callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("st_item")
                .document(item.itemDocumentId!!)
                .delete()
                .addOnSuccessListener {
                    callback?.onSuccess(item)
                }
                .addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        }else{
            itemDao.delete(item)
            callback?.onSuccess(item)
        }
    }

    override suspend fun update(item: Item, callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("st_item")
                .document(item.itemDocumentId!!)
                .update(item.getMap())
                .addOnSuccessListener {
                    callback?.onSuccess(item)
                }
                .addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        }else{
            itemDao.update(item)
            callback?.onSuccess(item)
        }
    }

    override suspend fun getItemById(id: String): Item {
        return itemDao.getItemById(id)
    }

    override suspend fun getAllItems(callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("st_item").get()
                .addOnSuccessListener { result ->
                    val items = mutableListOf<Item>()
                    if (result.size() > 0) {
                        for (document in result) {
                            val obj = document.toObject(Item::class.java)
                            if (!obj.itemId.isNullOrEmpty()) {
                                obj.itemDocumentId = document.id
                                items.add(obj)
                            }
                        }
                    }
                    callback?.onSuccess(items)
                }.addOnFailureListener { exception ->
                    callback?.onFailure(
                        exception.message ?: "Network error! Can't get items from remote."
                    )
                }
        }else {
            val localItems = itemDao.getAllItems().asLiveData().value
            if (!localItems.isNullOrEmpty()) {
                callback?.onSuccess(localItems)
            }
        }
    }

}