package com.grid.pos.data.item

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
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.Date

class ItemRepositoryImpl(
        private val itemDao: ItemDao
) : ItemRepository {
    override suspend fun insert(item: Item): Item {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val docRef = FirebaseFirestore.getInstance().collection("st_item")
                    .add(item.getMap()).await()
                item.itemDocumentId = docRef.id
            }

            CONNECTION_TYPE.LOCAL.key -> {
                itemDao.insert(item)
            }

            else -> {
                insertByProcedure(item)
            }
        }
        return item
    }

    override suspend fun delete(item: Item) {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                item.itemDocumentId?.let {
                    FirebaseFirestore.getInstance().collection("st_item").document(it).delete()
                        .await()
                }
            }

            CONNECTION_TYPE.LOCAL.key -> {
                itemDao.delete(item)
            }

            else -> {
                deleteByProcedure(item)
            }
        }
    }

    override suspend fun update(item: Item) {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                item.itemDocumentId?.let {
                    FirebaseFirestore.getInstance().collection("st_item").document(it)
                        .update(item.getMap()).await()
                }
            }

            CONNECTION_TYPE.LOCAL.key -> {
                itemDao.update(item)
            }

            else -> {
                updateItem(item)
            }
        }
    }

    override suspend fun update(items: List<Item>) {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val db = FirebaseFirestore.getInstance()
                val batch = db.batch()
                for (item in items) {
                    val modelRef = db.collection("st_item")
                        .document(item.itemDocumentId!!) // Assuming `id` is the document ID
                    batch.update(
                        modelRef,
                        item.getMap()
                    )
                }
                batch.commit().await()
            }

            CONNECTION_TYPE.LOCAL.key -> {
                itemDao.update(items)
            }

            else -> {
                items.forEach { item ->
                    updateItem(item)
                }

            }
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
                            mutableListOf(
                                "st_item.*",
                                "st_item_warehouse.*",
                                "currency.cur_newcode",
                                "op_id,op_unitcost"
                            ),
                            "it_cmp_id='${SettingsModel.getCompanyID()}'",
                            "order by it_name",
                            "INNER JOIN currency on it_cur_code = cur_code LEFT OUTER JOIN st_item_warehouse on it_id = uw_it_id LEFT OUTER JOIN st_opening on it_id=op_it_id"
                        )
                    } else {
                        SQLServerWrapper.getListOf(
                            "st_item",
                            "",
                            mutableListOf(
                                "st_item.*",
                                "st_item_warehouse.*",
                                "currency.cur_newcode",
                                "op_id,op_unitcost"
                            ),
                            "it_cmp_id='${SettingsModel.getCompanyID()}'",
                            "order by it_name",
                            "INNER JOIN currency on it_cur_code = cur_code LEFT OUTER JOIN st_item_warehouse on it_id = uw_it_id LEFT OUTER JOIN st_opening on it_id=op_it_id"
                        )
                        SQLServerWrapper.getQueryResult(
                            "select st_item.*,1 it_pos from st_item,pos_itembutton,pos_groupbutton,pos_station_groupbutton  where it_id=ib_it_id and ib_gb_id=gb_id and gb_id=psg_gb_id and psg_sta_name='.'  union select *,0 it_pos from st_item where it_id not in (select ib_it_id from pos_itembutton,pos_groupbutton,pos_station_groupbutton where ib_gb_id=gb_id and gb_id=psg_gb_id and psg_sta_name='.')"
                        )
                    }
                    dbResult?.let {
                        while (it.next()) {
                            items.add(getItemFromRow(it))
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

    override suspend fun getItemsForPOS(): MutableList<Item> {
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
                            mutableListOf(
                                "st_item.*",
                                "st_item_warehouse.*",
                                "currency.cur_newcode"
                            ),
                            "it_cmp_id='${SettingsModel.getCompanyID()}'",
                            "",
                            " INNER JOIN currency on it_cur_code = cur_code LEFT OUTER JOIN st_item_warehouse on it_id = uw_it_id"
                        )
                    } else {
                        SQLServerWrapper.getQueryResult(
                            "select st_item.*,1 it_pos from st_item,pos_itembutton,pos_groupbutton,pos_station_groupbutton  where it_id=ib_it_id and ib_gb_id=gb_id and gb_id=psg_gb_id and psg_sta_name='.'  union select *,0 it_pos from st_item where it_id not in (select ib_it_id from pos_itembutton,pos_groupbutton,pos_station_groupbutton where ib_gb_id=gb_id and gb_id=psg_gb_id and psg_sta_name='.')"
                        )
                    }
                    dbResult?.let {
                        while (it.next()) {
                            items.add(getItemFromRow(it))
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
                var item: Item? = null
                try {
                    val dbResult = if (SettingsModel.isSqlServerWebDb) {
                        SQLServerWrapper.getListOf(
                            "st_item",
                            "",
                            mutableListOf(
                                "st_item.*",
                                "st_item_warehouse.*",
                                "currency.cur_newcode"
                            ),
                            "it_cmp_id='${SettingsModel.getCompanyID()}' AND it_fa_name='$familyId'",
                            "",
                            " INNER JOIN currency on it_cur_code = cur_code LEFT OUTER JOIN st_item_warehouse on it_id = uw_it_id"
                        )
                    } else {
                        SQLServerWrapper.getQueryResult(
                            "select st_item.*,1 it_pos from st_item,pos_itembutton,pos_groupbutton,pos_station_groupbutton  where it_id=ib_it_id and ib_gb_id=gb_id and gb_id=psg_gb_id and psg_sta_name='.' and it_fa_name='$familyId'  union select *,0 it_pos from st_item where it_id not in (select ib_it_id from pos_itembutton,pos_groupbutton,pos_station_groupbutton where ib_gb_id=gb_id and gb_id=psg_gb_id and psg_sta_name='.'  and it_fa_name='$familyId')"
                        )
                    }
                    dbResult?.let {
                        while (it.next()) {
                            item = getItemFromRow(it)
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return item
            }
        }
    }

    override suspend fun updateWarehouseData(item: Item) {
        if (SettingsModel.isConnectedToSqlServer() && !item.itemWarehouseRowId.isNullOrEmpty()) {
            SQLServerWrapper.update(
                "st_item_warehouse",
                listOf(
                    "uw_wa_name",
                    "uw_location",
                    "uw_openqty",
                    "uw_userstamp",
                ),
                listOf(
                    item.itemWarehouse,
                    item.itemLocation,
                    item.itemOpenQty,
                    SettingsModel.currentUser?.userUsername
                ),
                "uw_id = '${item.itemWarehouseRowId}'"
            )
        }
    }

    override suspend fun updateOpening(
            openingId: String?,
            cost: Double,
            costFirst: Double,
            costSecond: Double
    ) {
        if (SettingsModel.isConnectedToSqlServer() && !openingId.isNullOrEmpty()) {
            SQLServerWrapper.update(
                "st_opening",
                listOf(
                    "op_unitcost",
                    "op_unitcostf",
                    "op_unitcosts",
                    "op_date",
                    "op_userstamp",
                ),
                listOf(
                    cost,
                    costFirst,
                    costSecond,
                    Timestamp(System.currentTimeMillis()),
                    SettingsModel.currentUser?.userUsername
                ),
                "op_id = '$openingId'"
            )
        }
    }

    private fun getItemFromRow(row: ResultSet): Item {
        return Item().apply {
            itemId = row.getStringValue("it_id")
            itemCompId = row.getStringValue("it_cmp_id")
            itemFaId = row.getStringValue("it_fa_name")
            itemName = row.getStringValue("it_name")
            itemBarcode = row.getStringValue("it_barcode")
            it_div_name = row.getStringValue(
                "it_div_name",
                "null"
            )
            itemCashback = row.getDoubleValue("it_cashback")
            itemGroup = row.getStringValue("it_group")


            itemTax = row.getDoubleValue("it_vat")
            itemTax1 = row.getDoubleValue("it_tax1")
            itemTax2 = row.getDoubleValue("it_tax2")
            itemPrinter = row.getStringValue("it_di_name").ifEmpty { null }

            itemOpeningId = row.getStringValue("op_id").ifEmpty { null }
            itemWarehouseRowId = row.getStringValue("uw_id").ifEmpty { null }
            itemWarehouse = row.getStringValue("uw_wa_name").ifEmpty { null }
            itemLocation = row.getStringValue("uw_location").ifEmpty { null }
            itemOpenQty = row.getDoubleValue("uw_openqty")
            itemRemQty = row.getDoubleValue("it_remqty")
            itemPos = row.getIntValue(
                "it_pos",
                1
            ) == 1
            itemBtnColor = row.getStringValue("it_color").ifEmpty { null }
            itemBtnTextColor = "#000000"
            val timeStamp = row.getObjectValue("it_timestamp")
            itemTimeStamp = when (timeStamp) {
                is Date -> timeStamp
                is String -> DateHelper.getDateFromString(
                    timeStamp,
                    "yyyy-MM-dd hh:mm:ss.SSS"
                )

                else -> null
            }
            itemDateTime = itemTimeStamp!!.time
            itemUserStamp = row.getStringValue("it_userstamp")
            itemImage = row.getStringValue("it_image").ifEmpty { null }
            itemCurrencyId = row.getStringValue("it_cur_code").ifEmpty { null }
            itemCurrencyCode = if (SettingsModel.isSqlServerWebDb) row.getStringValue("cur_newcode")
            else row.getStringValue("it_cur_code")
            itemUnitPrice = row.getDoubleValue("it_unitprice")
            itemOpenCost = row.getDoubleValue(
                "op_unitcost",
                row.getDoubleValue("it_cost")
            )
            itemRealUnitPrice = 0.0
        }
    }

    private fun insertByProcedure(item: Item) {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                item.itemId,//@it_id
                SettingsModel.getCompanyID(),//@it_cmp_id
                item.itemName,//@it_name
                item.itemFaId,//@it_fa_name
                item.itemGroup,//@it_group
                SettingsModel.defaultSqlServerBranch,//@it_bra_name
                null,//@it_br_name
                null,//@it_unit
                item.itemCurrencyId,//@it_cur_code
                item.itemUnitPrice,//@it_unitprice
                item.itemTax,//@it_vat
                item.itemBarcode,//@it_barcode
                null,//@it_alertqty
                null,//@it_di_name
                null,//@it_inactive
                SettingsModel.currentCompany?.cmp_multibranchcode,//@branchcode
                null,//@it_size
                item.itemBtnColor,//@it_color
                null,//@it_code
                null,//@it_points
                item.it_div_name,//@it_div_name
                item.itemImage,//@it_image
                null,//@it_profit
                null,//@it_profrule
                item.itemOpenQty,//@openqty
                item.itemOpenCost,//@opencost
                SettingsModel.currentUser?.userUsername,//@it_userstamp
                SettingsModel.defaultSqlServerWarehouse,//@mainwarehouse
                item.itemCurrencyId,//@firstcurr
                null,//@secondcurr
                Timestamp(System.currentTimeMillis()),//@dateyearstart
                null,//@it_altname
                null,//@it_wa_name
                null,//@it_specialcode
                null,//@it_commission
                null,//@it_minprice
                item.itemRemQty,//@it_remqty
                item.itemOpenCost,//@it_cost
                null,//@it_lastsuppliername
                null,//@it_lastsupplierprice
                item.itemCashback,//@it_cashback
                item.itemTax1,//@it_tax1
                item.itemTax2,//@it_tax2
                null,//@it_maxqty
                item.itemPos,//@it_pos
                null,//@it_desc
                null,//@it_type
                null,//@it_online
                null,//@it_digitalmenu
            )
        } else {
            listOf(
                SettingsModel.getCompanyID(),//@it_cmp_id
                item.itemName,//@it_name
                item.itemFaId,//@it_fa_name
                item.itemGroup,//@it_group
                SettingsModel.defaultSqlServerBranch,//@it_bra_name
                null,//@it_br_name
                null,//@it_unit
                item.itemCurrencyId,//@it_cur_code
                item.itemUnitPrice,//@it_unitprice
                item.itemTax,//@it_vat
                item.itemBarcode,//@it_barcode
                null,//@it_alertqty
                null,//@it_di_name
                null,//@it_inactive
                SettingsModel.currentCompany?.cmp_multibranchcode,//@branchcode
                null,//@it_size
                item.itemBtnColor,//@it_color
                null,//@it_code
                null,//@it_points
                item.it_div_name,//@it_div_name
                item.itemImage,//@it_image
                null,//@it_profit
                null,//@it_profrule
                item.itemOpenQty,//@openqty
                item.itemOpenCost,//@opencost
                SettingsModel.currentUser?.userUsername,//@it_userstamp
                SettingsModel.defaultSqlServerWarehouse,//@mainwarehouse
                item.itemCurrencyId,//@firstcurr
                null,//@secondcurr
                Timestamp(System.currentTimeMillis()),//@dateyearstart
                null,//@it_altname
                null,//@it_wa_name
                null,//@it_specialcode
                null,//@it_commission
                null,//@it_minprice
                item.itemRemQty,//@it_remqty
                item.itemOpenCost,//@it_cost
                null,//@it_lastsuppliername
                null,//@it_lastsupplierprice
                item.itemCashback,//@it_cashback
                item.itemTax1,//@it_tax1
                item.itemTax2,//@it_tax2
                null,//@it_maxqty
            )
        }
        SQLServerWrapper.executeProcedure(
            "addst_item",
            parameters
        )/*if (id.isNullOrEmpty()) {
            try {
                val dbResult = SQLServerWrapper.getQueryResult("select max(it_id) as id from st_item")
                dbResult?.let {
                    while (it.next()) {
                        item.itemId = it.getStringValue(
                            "id",
                            item.itemId
                        )
                    }
                    SQLServerWrapper.closeResultSet(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            item.itemId = id
        }*/
    }

    private fun updateItem(
            item: Item
    ) {
        SQLServerWrapper.update(
            "st_item",
            listOf(
                "it_name",
                "it_fa_name",
                "it_group",
                "it_cur_code",
                "it_unitprice",
                "it_vat",
                "it_barcode",
                "it_color",
                "it_image",
                "it_maxqty",
                "it_cost",
                "it_userstamp",
                "it_timestamp",
                "it_remqty",
                "it_cashback",
                "it_tax1",
                "it_tax2",
            ),
            listOf(
                item.itemName,//@it_name
                item.itemFaId,//@it_fa_name
                item.itemGroup,//@it_group
                item.itemCurrencyId,//@it_cur_code
                item.itemUnitPrice,//@it_unitprice
                item.itemTax,//@it_vat
                item.itemBarcode,//@it_barcode
                item.itemBtnColor,//@it_color
                item.itemImage,//@it_image
                item.itemOpenQty,//@openqty
                item.itemOpenCost,//@opencost
                SettingsModel.currentUser?.userUsername,//@it_userstamp
                Timestamp(System.currentTimeMillis()),//@dateyearstart
                item.itemRemQty,//@it_remqty
                item.itemCashback,//@it_cashback
                item.itemTax1,//@it_tax1
                item.itemTax2,//@it_tax2
            ),
            "it_id = '${item.itemId}'"
        )
    }

    private fun deleteByProcedure(item: Item): String {
        return SQLServerWrapper.executeProcedure(
            "delst_item",
            listOf(
                item.itemId,
                item.itemUserStamp,
                SettingsModel.currentCompany?.cmp_multibranchcode
            )
        ) ?: ""
    }

}