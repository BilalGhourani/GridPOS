package com.grid.pos.data.item

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Extension.getBooleanValue
import com.grid.pos.utils.Extension.getDoubleValue
import com.grid.pos.utils.Extension.getIntValue
import com.grid.pos.utils.Extension.getObjectValue
import com.grid.pos.utils.Extension.getStringValue
import kotlinx.coroutines.tasks.await
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
                    FirebaseFirestore.getInstance().collection("st_item").document(it).delete().await()
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
                updateByProcedure(item)
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
                items.forEach {item->
                    updateByProcedure(item)
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
                                "currency.cur_newcode"
                            ),
                            "it_cmp_id='${SettingsModel.getCompanyID()}'",
                            "",
                          /*INNER JOIN st_item_warehouse on it_id = uw_it_id*/  " INNER JOIN currency on it_cur_code = cur_code"
                        )
                    } else {
                        SQLServerWrapper.getQueryResult(
                            "select st_item.*,1 it_pos from st_item,pos_itembutton,pos_groupbutton,pos_station_groupbutton  where it_id=ib_it_id and ib_gb_id=gb_id and gb_id=psg_gb_id and psg_sta_name='.'  union select *,0 it_pos from st_item where it_id not in (select ib_it_id from pos_itembutton,pos_groupbutton,pos_station_groupbutton where ib_gb_id=gb_id and gb_id=psg_gb_id and psg_sta_name='.')"
                        )
                    }
                    dbResult?.let {
                        while (it.next()) {
                            items.add(Item().apply {
                                itemId = it.getStringValue("it_id")
                                itemCompId = it.getStringValue("it_cmp_id")
                                itemFaId = it.getStringValue("it_fa_name")
                                itemName = it.getStringValue("it_name")
                                itemBarcode = it.getStringValue("it_barcode")
                                it_cashback = it.getDoubleValue("it_cashback")
                                itemGroup = it.getStringValue("it_group")
                                itemAlertQty = it.getDoubleValue("it_alertqty",1.0)
                                itemInactive = it.getBooleanValue("it_inactive",false)
                                itemSize = it.getStringValue("it_size").ifEmpty { null }
                                itemCode = it.getStringValue("it_code").ifEmpty { null }
                                itemDivName = it.getStringValue("it_div_name").ifEmpty { null }
                                itemPoints = it.getIntValue("it_points")
                                itemProfit = it.getDoubleValue("it_profit")
                                itemProfitRule = it.getStringValue("it_profrule").ifEmpty { null }
                                itemAltName = it.getStringValue("it_altname").ifEmpty { null }
                                itemWarehouseName = it.getStringValue("it_wa_name").ifEmpty { null }
                                //itemMainWarehouseName = it.getStringValue("uw_wa_name").ifEmpty { null }
                                //itemWarehouseLocation = it.getStringValue("uw_location").ifEmpty { null }
                                itemSpecialCode = it.getBooleanValue("it_specialcode")
                                itemCommission = it.getDoubleValue("it_commission")
                                itemMinPrice = it.getDoubleValue("it_minprice")
                                itemMaxQty = it.getDoubleValue("it_maxqty")
                                itemLastSupplierName = it.getStringValue("it_lastsuppliername").ifEmpty { null }
                                itemLastSupplierPrice = it.getDoubleValue("it_lastsupplierprice")
                                itemOrder = it.getIntValue("it_order")
                                itemDesc = it.getStringValue("it_desc").ifEmpty { null }
                                itemType = it.getStringValue("it_type").ifEmpty { null }
                                itemOnline = it.getBooleanValue("it_online")
                                itemDigitalMenu = it.getBooleanValue("it_digitalmenu")
                                itemBrand = it.getStringValue("it_br_name").ifEmpty { null }
                                itemUnit = it.getStringValue("it_unit").ifEmpty { null }
                                itemDiName = it.getStringValue("it_di_name").ifEmpty { null }


                                itemTax = it.getDoubleValue("it_vat")
                                itemTax1 = it.getDoubleValue("it_tax1")
                                itemTax2 = it.getDoubleValue("it_tax2")
                                itemPrinter = it.getStringValue("it_di_name").ifEmpty { null }
                                itemOpenQty = it.getDoubleValue("it_maxqty"/*"uw_openqty"*/)
                                itemRemQty = it.getDoubleValue("it_remqty")
                                itemMaxQty = it.getDoubleValue("it_maxqty")
                                itemPos = it.getIntValue(
                                    "it_pos",
                                    1
                                ) == 1
                                itemBtnColor = it.getStringValue("it_color").ifEmpty { null }
                                itemBtnTextColor = "#000000"
                                val timeStamp = it.getObjectValue("it_timestamp")
                                itemTimeStamp = when (timeStamp) {
                                    is Date -> timeStamp
                                    is String -> DateHelper.getDateFromString(
                                        timeStamp,
                                        "yyyy-MM-dd hh:mm:ss.SSS"
                                    )

                                    else -> null
                                }
                                itemDateTime = itemTimeStamp!!.time
                                itemUserStamp = it.getStringValue("it_userstamp")
                                itemImage = it.getStringValue("it_image").ifEmpty { null }
                                itemCurrencyId = it.getStringValue("it_cur_code").ifEmpty { null }
                                itemCurrencyCode = if (SettingsModel.isSqlServerWebDb) it.getStringValue("cur_newcode")
                                else it.getStringValue("it_cur_code")
                                itemUnitPrice = it.getDoubleValue("it_unitprice")
                                itemOpenCost = it.getDoubleValue("it_cost")
                                itemRealUnitPrice = 0.0
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

    private fun insertByProcedure(item: Item) {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                "null_string_output",//@it_id
                SettingsModel.getCompanyID(),//@it_cmp_id
                item.itemName,//@it_name
                item.itemFaId,//@it_fa_name
                item.itemGroup,//@it_group
                SettingsModel.defaultSqlServerBranch,//@it_bra_name
                item.itemBrand,//@it_br_name
                item.itemUnit,//@it_unit
                item.itemCurrencyId,//@it_cur_code
                item.itemUnitPrice,//@it_unitprice
                item.itemTax,//@it_vat
                item.itemBarcode,//@it_barcode
                item.itemAlertQty,//@it_alertqty
                item.itemDiName,//@it_di_name
                item.itemInactive,//@it_inactive
                SettingsModel.currentCompany?.cmp_multibranchcode,//@branchcode
                item.itemSize,//@it_size
                item.itemBtnColor,//@it_color
                item.itemCode,//@it_code
                item.itemPoints,//@it_points
                item.itemDivName,//@it_div_name
                item.itemImage,//@it_image
                item.itemProfit,//@it_profit
                item.itemProfitRule,//@it_profrule
                item.itemOpenQty,//@openqty
                item.itemOpenCost,//@opencost
                SettingsModel.currentUser?.userUsername,//@it_userstamp
                item.itemWarehouseName,//@mainwarehouse
                item.itemCurrencyId,//@firstcurr
                null,//@secondcurr
                Timestamp(System.currentTimeMillis()),//@dateyearstart
                item.itemAltName,//@it_altname
                item.itemWarehouseName,//@it_wa_name
                item.itemSpecialCode,//@it_specialcode
                item.itemCommission,//@it_commission
                item.itemMinPrice,//@it_minprice
                item.itemRemQty,//@it_remqty
                item.itemOpenCost,//@it_cost
                item.itemLastSupplierName,//@it_lastsuppliername
                item.itemLastSupplierPrice,//@it_lastsupplierprice
                item.it_cashback,//@it_cashback
                item.itemTax1,//@it_tax1
                item.itemTax2,//@it_tax2
                item.itemMaxQty,//@it_maxqty
                item.itemPos,//@it_pos
                item.itemDesc,//@it_desc
                item.itemType,//@it_type
                item.itemOnline,//@it_online
                item.itemDigitalMenu,//@it_digitalmenu
            )
        } else {
            listOf(
                SettingsModel.getCompanyID(),//@it_cmp_id
                item.itemName,//@it_name
                item.itemFaId,//@it_fa_name
                item.itemGroup,//@it_group
                SettingsModel.defaultSqlServerBranch,//@it_bra_name
                item.itemBrand,//@it_br_name
                item.itemUnit,//@it_unit
                item.itemCurrencyId,//@it_cur_code
                item.itemUnitPrice,//@it_unitprice
                item.itemTax,//@it_vat
                item.itemBarcode,//@it_barcode
                item.itemAlertQty,//@it_alertqty
                item.itemDiName,//@it_di_name
                item.itemInactive,//@it_inactive
                SettingsModel.currentCompany?.cmp_multibranchcode,//@branchcode
                item.itemSize,//@it_size
                item.itemBtnColor,//@it_color
                item.itemCode,//@it_code
                item.itemPoints,//@it_points
                item.itemDivName,//@it_div_name
                item.itemImage,//@it_image
                item.itemProfit,//@it_profit
                item.itemProfitRule,//@it_profrule
                item.itemOpenQty,//@openqty
                item.itemOpenCost,//@opencost
                SettingsModel.currentUser?.userUsername,//@it_userstamp
                SettingsModel.defaultSqlServerWarehouse,//@mainwarehouse
                item.itemCurrencyId,//@firstcurr
                null,//@secondcurr
                Timestamp(System.currentTimeMillis()),//@dateyearstart
                item.itemAltName,//@it_altname
                item.itemWarehouseName,//@it_wa_name
                item.itemSpecialCode,//@it_specialcode
                item.itemCommission,//@it_commission
                item.itemMinPrice,//@it_minprice
                item.itemRemQty,//@it_remqty
                item.itemOpenCost,//@it_cost
                item.itemLastSupplierName,//@it_lastsuppliername
                item.itemLastSupplierPrice,//@it_lastsupplierprice
                item.it_cashback,//@it_cashback
                item.itemTax1,//@it_tax1
                item.itemTax2,//@it_tax2
                item.itemMaxQty,//@it_maxqty
            )
        }
        val id = SQLServerWrapper.executeProcedure(
            "addst_item",
            parameters
        )
        if (id.isNullOrEmpty()) {
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
        }
    }

    private fun updateByProcedure(
            item: Item
    ): String {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                item.itemId,//@@it_id
                item.itemCompId,//@it_cmp_id
                item.itemName,//@it_name
                item.itemFaId,//@it_fa_name
                item.itemGroup,//@it_group
                SettingsModel.defaultSqlServerBranch,//@it_bra_name
                item.itemBrand,//@it_br_name
                item.itemUnit,//@it_unit
                item.itemCurrencyId,//@it_cur_code
                item.itemUnitPrice,//@it_unitprice
                item.itemTax,//@it_vat
                item.itemBarcode,//@it_barcode
                item.itemAlertQty,//@it_alertqty
                item.itemDiName,//@it_di_name
                item.itemInactive,//@it_inactive
                item.itemSize,//@it_size
                item.itemBtnColor,//@it_color
                item.itemCode,//@it_code
                item.itemPoints,//@it_points
                item.itemDivName,//@it_div_name
                item.itemImage,//@it_image
                item.itemProfit,//@it_profit
                item.itemProfitRule,//@it_profrule
                item.itemOpenQty,//@openqty
                item.itemOpenCost,//@opencost
                SettingsModel.currentUser?.userUsername,//@it_userstamp
                SettingsModel.defaultSqlServerWarehouse,//@mainwarehouse
                item.itemCurrencyId,//@firstcurr
                null,//@secondcurr
                Timestamp(System.currentTimeMillis()),//@dateyearstart
                SettingsModel.currentCompany?.cmp_multibranchcode,//@branchcode
                item.itemAltName,//@it_altname
                item.itemWarehouseName,//@it_wa_name
                item.itemSpecialCode,//@it_specialcode
                item.itemCommission,//@it_commission
                item.itemMinPrice,//@it_minprice
                item.itemRemQty,//@it_remqty
                item.itemOpenCost,//@it_cost
                item.itemLastSupplierName,//@it_lastsuppliername
                item.itemLastSupplierPrice,//@it_lastsupplierprice
                item.it_cashback,//@it_cashback
                item.itemTax1,//@it_tax1
                item.itemTax2,//@it_tax2
                item.itemMaxQty,//@it_maxqty
                item.itemOrder,//@it_order
                item.itemPos,//@it_pos
                item.itemDesc,//@it_desc
                item.itemType,//@it_type
                item.itemOnline,//@it_online
                item.itemDigitalMenu,//@it_digitalmenu
            )
        } else {
            listOf(
                item.itemId,//@@it_id
                SettingsModel.getCompanyID(),//@it_cmp_id
                item.itemName,//@it_name
                item.itemFaId,//@it_fa_name
                item.itemGroup,//@it_group
                SettingsModel.defaultSqlServerBranch,//@it_bra_name
                item.itemBrand,//@it_br_name
                item.itemUnit,//@it_unit
                item.itemCurrencyId,//@it_cur_code
                item.itemUnitPrice,//@it_unitprice
                item.itemTax,//@it_vat
                item.itemBarcode,//@it_barcode
                item.itemAlertQty,//@it_alertqty
                item.itemDiName,//@it_di_name
                item.itemInactive,//@it_inactive
                item.itemSize,//@it_size
                item.itemBtnColor,//@it_color
                item.itemCode,//@it_code
                item.itemPoints,//@it_points
                item.itemDivName,//@it_div_name
                item.itemImage,//@it_image
                item.itemProfit,//@it_profit
                item.itemProfitRule,//@it_profrule
                item.itemOpenQty,//@openqty
                item.itemOpenCost,//@opencost
                SettingsModel.currentUser?.userUsername,//@it_userstamp
                item.itemWarehouseName?:SettingsModel.defaultSqlServerWarehouse,//@mainwarehouse
                item.itemCurrencyId,//@firstcurr
                null,//@secondcurr
                Timestamp(System.currentTimeMillis()),//@dateyearstart
                SettingsModel.currentCompany?.cmp_multibranchcode,//@branchcode
                item.itemAltName,//@it_altname
                item.itemWarehouseName,//@it_wa_name
                item.itemSpecialCode,//@it_specialcode
                item.itemCommission,//@it_commission
                item.itemMinPrice,//@it_minprice
                item.itemRemQty,//@it_remqty
                item.itemOpenCost,//@it_cost
                item.itemLastSupplierName,//@it_lastsuppliername
                item.itemLastSupplierPrice,//@it_lastsupplierprice
                item.it_cashback,//@it_cashback
                item.itemTax1,//@it_tax1
                item.itemTax2,//@it_tax2
                item.itemMaxQty,//@it_maxqty
            )
        }
        return SQLServerWrapper.executeProcedure(
            "updst_item",
            parameters
        ) ?: ""
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