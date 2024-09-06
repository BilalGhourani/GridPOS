package com.grid.pos.data.Item

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DateHelper
import kotlinx.coroutines.tasks.await
import java.util.Date

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
                FirebaseFirestore.getInstance().collection("st_item").document(it).delete().await()
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

    override suspend fun getAllItems(): MutableList<Item> {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("st_item")
                    .whereEqualTo(
                        "it_cmp_id",
                        SettingsModel.getCompanyID()
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
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return itemDao.getAllItems(SettingsModel.getCompanyID() ?: "")
            }

            else -> {
                val items: MutableList<Item> = mutableListOf()
                try {
                    val where = "it_cmp_id='${SettingsModel.getCompanyID()}'"
                    val dbResult = SQLServerWrapper.getListOf(
                        "st_item",
                        "",
                        mutableListOf("*"),
                        where
                    )
                    dbResult?.let {
                        while (it.next()) {
                            items.add(Item().apply {
                                itemId = it.getString("it_id")
                                itemCompId = it.getString("it_cmp_id")
                                itemFaId = it.getString("it_fa_name")
                                itemName = it.getString("it_name")
                                itemBarcode = it.getString("it_barcode")
                                itemUnitPrice = it.getDouble("it_unitprice")
                                itemTax = it.getDouble("it_tax")
                                itemTax1 = it.getDouble("it_tax1")
                                itemTax2 = it.getDouble("it_tax2")
                                itemPrinter = it.getString("it_printer")
                                itemOpenQty = it.getDouble("it_maxqty")
                                itemOpenCost = it.getDouble("it_cost")
                                itemPos = it.getInt("it_pos") == 1
                                itemBtnColor = it.getString("it_color")
                                itemBtnTextColor = "#000000"
                                val timeStamp = it.getObject("it_timestamp")
                                itemTimeStamp = if (timeStamp is Date) timeStamp else DateHelper.getDateFromString(
                                    timeStamp as String,
                                    "yyyy-MM-dd hh:mm:ss.SSS"
                                )
                                itemDateTime = itemTimeStamp!!.time
                                itemUserStamp = it.getString("it_userstamp")
                            })
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return items
            }
        }

    }

    override suspend fun getOneItemByPrinter(printerID: String): Item? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("st_item")
                    .whereEqualTo(
                        "it_printer",
                        printerID
                    ).limit(1).get().await()

                if (querySnapshot.size() > 0) {
                    for (document in querySnapshot) {
                        val obj = document.toObject(Item::class.java)
                        if (obj.itemId.isNotEmpty()) {
                            obj.itemDocumentId = document.id
                            return obj
                        }
                    }
                }
                return null
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return itemDao.getOneItemByPrinter(printerID)
            }

            else -> {
                return null
            }
        }
    }

    override suspend fun getOneItemByFamily(familyId: String): Item? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("st_item")
                    .whereEqualTo(
                        "it_fa_id",
                        familyId
                    ).limit(1).get().await()

                if (querySnapshot.size() > 0) {
                    for (document in querySnapshot) {
                        val obj = document.toObject(Item::class.java)
                        if (obj.itemId.isNotEmpty()) {
                            obj.itemDocumentId = document.id
                            return obj
                        }
                    }
                }
                return null
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return itemDao.getOneItemByFamily(familyId)
            }

            else -> {
                return null
            }
        }
    }

}