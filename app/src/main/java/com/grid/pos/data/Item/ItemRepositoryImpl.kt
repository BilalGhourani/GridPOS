package com.grid.pos.data.Item

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Extension.getDoubleValue
import com.grid.pos.utils.Extension.getIntValue
import com.grid.pos.utils.Extension.getObjectValue
import com.grid.pos.utils.Extension.getStringValue
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
                    val dbResult = if (SettingsModel.isSqlServerWebDb) {
                        SQLServerWrapper.getListOf(
                            "st_item",
                            "",
                            mutableListOf("*"),
                            "it_cmp_id='${SettingsModel.getCompanyID()}'"
                        )
                    } else {
                        SQLServerWrapper.getQueryResult(
                            "select st_item.*,1 it_pos from st_item,pos_itembutton,pos_groupbutton,pos_station_groupbutton " + "where it_id=ib_it_id and ib_gb_id=gb_id and gb_id=psg_gb_id and psg_sta_name='.' " + "union " + "select *,0 it_pos from st_item where it_id not in (select ib_it_id from pos_itembutton,pos_groupbutton,pos_station_groupbutton " + "where ib_gb_id=gb_id and gb_id=psg_gb_id and psg_sta_name='.')"
                        )
                    }
                    val currency = SettingsModel.currentCurrency
                    dbResult?.let {
                        while (it.next()) {
                            items.add(Item().apply {
                                itemId = it.getStringValue("it_id")
                                itemCompId = it.getStringValue("it_cmp_id")
                                itemFaId = it.getStringValue("it_fa_name")
                                itemName = it.getStringValue("it_name")
                                itemBarcode = it.getStringValue("it_barcode")

                                itemTax = it.getDoubleValue("it_vat")
                                itemTax1 = it.getDoubleValue("it_tax1")
                                itemTax2 = it.getDoubleValue("it_tax2")
                                itemPrinter = it.getStringValue("it_di_name")
                                itemOpenQty = it.getDoubleValue("it_maxqty")
                                itemPos = it.getIntValue(
                                    "it_pos",
                                    1
                                ) == 1
                                itemBtnColor = it.getStringValue("it_color")
                                itemBtnTextColor = "#000000"
                                val timeStamp = it.getObjectValue("it_timestamp")
                                itemTimeStamp = if (timeStamp is Date) timeStamp else DateHelper.getDateFromString(
                                    timeStamp as String,
                                    "yyyy-MM-dd hh:mm:ss.SSS"
                                )
                                itemDateTime = itemTimeStamp!!.time
                                itemUserStamp = it.getStringValue("it_userstamp")
                                if (currency != null) {
                                    val currencyCode = it.getStringValue("it_cur_code")
                                    val unitPrice = it.getDoubleValue("it_unitprice")
                                    val unitCost = it.getDoubleValue("it_cost")
                                    if (currencyCode == currency.currencyDocumentId) {//second currency
                                        if (currency.currencyRate < 1.0) {
                                            itemUnitPrice = unitPrice.div(currency.currencyRate)
                                            itemOpenCost = unitCost.div(currency.currencyRate)
                                        } else {
                                            itemUnitPrice = unitPrice.times(currency.currencyRate)
                                            itemOpenCost = unitCost.times(currency.currencyRate)
                                        }
                                    }
                                } else {
                                    itemUnitPrice = it.getDoubleValue("it_unitprice")
                                    itemOpenCost = it.getDoubleValue("it_cost")
                                }
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