package com.grid.pos.data.Item

import androidx.lifecycle.asLiveData
import com.grid.pos.data.Company.Company
import com.grid.pos.interfaces.OnResult
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ItemRepositoryImpl(
    private val itemDao: ItemDao
) : ItemRepository {
    override suspend fun insert(item: Item, callback: OnResult?) {
        FirebaseFirestore.getInstance().collection("st_item")
            .add(item)
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    itemDao.insert(item)
                    callback?.onSuccess(item)
                }
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }
    }

    override suspend fun delete(item: Item, callback: OnResult?) {
        FirebaseFirestore.getInstance().collection("st_item")
            .document(item.itemDocumentId!!)
            .delete()
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    itemDao.delete(item)
                    callback?.onSuccess(item)
                }
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }
    }

    override suspend fun update(item: Item, callback: OnResult?) {
        FirebaseFirestore.getInstance().collection("st_item")
            .document(item.itemDocumentId!!)
            .update(item.getMap())
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    itemDao.update(item)
                    callback?.onSuccess(item)
                }
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }
    }

    override suspend fun getItemById(id: String): Item {
        return itemDao.getItemById(id)
    }

    override suspend fun getAllItems(callback: OnResult?) {
        val localItems = itemDao.getAllItems().asLiveData().value
        if (!localItems.isNullOrEmpty()) {
            callback?.onSuccess(localItems)
        }
        FirebaseFirestore.getInstance().collection("st_item").get()
            .addOnSuccessListener { result ->
                CoroutineScope(Dispatchers.IO).launch {
                    val items = mutableListOf<Item>()
                    itemDao.deleteAll()
                    for (document in result) {
                        val obj = document.toObject(Item::class.java)
                        if (!obj.itemId.isNullOrEmpty()) {
                            obj.itemDocumentId = document.id
                            items.add(obj)
                        }
                    }
                    itemDao.insertAll(items.toList())
                    callback?.onSuccess(items)
                }
            }.addOnFailureListener { exception ->
                callback?.onFailure(
                    exception.message ?: "Network error! Can't get companies from remote."
                )
            }
    }

}