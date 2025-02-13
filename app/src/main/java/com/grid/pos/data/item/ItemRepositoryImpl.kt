package com.grid.pos.data.item

import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.Query
import com.grid.pos.data.FirebaseWrapper
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.DataModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Extension.getDoubleValue
import com.grid.pos.utils.Extension.getIntValue
import com.grid.pos.utils.Extension.getObjectValue
import com.grid.pos.utils.Extension.getStringValue
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.Date

class ItemRepositoryImpl(
    private val itemDao: ItemDao
) : ItemRepository {
    override suspend fun insert(item: Item): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                return FirebaseWrapper.insert(
                    "st_item",
                    item
                )
            }

            CONNECTION_TYPE.LOCAL.key -> {
                itemDao.insert(item)
                return DataModel(item)
            }

            else -> {
                return insertByProcedure(item)
            }
        }
    }

    override suspend fun delete(item: Item): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                return FirebaseWrapper.delete(
                    "st_item",
                    item
                )
            }

            CONNECTION_TYPE.LOCAL.key -> {
                itemDao.delete(item)
                return DataModel(item)
            }

            else -> {
                return deleteByProcedure(item)
            }
        }
    }

    override suspend fun update(item: Item, updateShowInPOS: Boolean): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                return FirebaseWrapper.update(
                    "st_item",
                    item
                )
            }

            CONNECTION_TYPE.LOCAL.key -> {
                itemDao.update(item)
                return DataModel(item)
            }

            else -> {
                return updateItem(item, updateShowInPOS)
            }
        }
    }

    override suspend fun update(items: List<Item>): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                return FirebaseWrapper.update(
                    "st_item",
                    items
                )
            }

            CONNECTION_TYPE.LOCAL.key -> {
                itemDao.update(items)
                return DataModel(items)
            }

            else -> {
                items.forEach { item ->
                    updateItem(item)
                }
                return DataModel(items)
            }
        }

    }

    override suspend fun getAllItems(): MutableList<Item> {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "st_item",
                    filters = mutableListOf(
                        Filter.equalTo(
                            "it_cmp_id",
                            SettingsModel.getCompanyID()
                        )
                    )
                )
                val size = querySnapshot?.size() ?: 0
                val items = mutableListOf<Item>()
                if (size > 0) {
                    for (document in querySnapshot!!) {
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
                                "op_id,op_unitcost,op_unitcostf,op_unitcosts"
                            ),
                            "it_cmp_id='${SettingsModel.getCompanyID()}'",
                            "order by it_name",
                            "LEFT OUTER JOIN currency on it_cur_code = cur_code LEFT OUTER JOIN st_item_warehouse on it_id = uw_it_id LEFT OUTER JOIN st_opening on it_id=op_it_id"
                        )
                    } else {
                        SQLServerWrapper.getListOf(
                            "st_item",
                            "",
                            mutableListOf(
                                "st_item.*",
                                "st_item_warehouse.*",
                                "currency.cur_code",
                                "op_id,op_unitcost,op_unitcostf,op_unitcosts"
                            ),
                            "",
                            "order by it_name",
                            "LEFT OUTER JOIN currency on it_cur_code = cur_code LEFT OUTER JOIN st_item_warehouse on it_id = uw_it_id LEFT OUTER JOIN st_opening on it_id=op_it_id"
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
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "st_item",
                    filters = mutableListOf(
                        Filter.equalTo(
                            "it_cmp_id",
                            SettingsModel.getCompanyID()
                        )
                    )
                )
                val size = querySnapshot?.size() ?: 0
                val items = mutableListOf<Item>()
                if (size > 0) {
                    for (document in querySnapshot!!) {
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
                                "ib_gb_id",
                                "currency.cur_newcode"
                            ),
                            "it_cmp_id='${SettingsModel.getCompanyID()}'",
                            "",
                            " INNER JOIN pos_itembutton on it_id = ib_it_id INNER JOIN currency on it_cur_code = cur_code LEFT OUTER JOIN st_item_warehouse on it_id = uw_it_id"
                        )
                    } else {
                        SQLServerWrapper.getQueryResult(
                            "select st_item.*,1 it_pos,ib_gb_id from st_item,pos_itembutton,pos_groupbutton,pos_station_groupbutton  where it_id=ib_it_id and ib_gb_id=gb_id and gb_id=psg_gb_id and psg_sta_name='.'  union select st_item.*,0 it_pos,ib_gb_id from st_item,pos_itembutton where it_id not in (select ib_it_id from pos_itembutton,pos_groupbutton,pos_station_groupbutton where ib_gb_id=gb_id and gb_id=psg_gb_id and psg_sta_name='.')"
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
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "st_item",
                    limit = 1,
                    filters = mutableListOf(
                        Filter.equalTo(
                            "it_printer",
                            printerID
                        )
                    )
                )
                val size = querySnapshot?.size() ?: 0
                if (size > 0) {
                    for (document in querySnapshot!!) {
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

    override suspend fun generateBarcode(): String {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "st_item",
                    orderBy = mutableListOf(
                        "it_barcode" to Query.Direction.DESCENDING
                    )
                )
                val barcode = querySnapshot?.documents?.mapNotNull {
                    it.getString("it_barcode")?.toLongOrNull()
                }?.maxOrNull() // Find the largest numeric barcode
                return barcode?.let {
                    (it + 1).toString()
                } ?: "10000"
            }

            CONNECTION_TYPE.LOCAL.key -> {
                val result = itemDao.getLastNumericPassword()
                val numBarcode = result?.toLongOrNull() ?: 9999
                return (numBarcode + 1).toString()
            }

            else -> {
                var barcode: String? = null
                try {
                    val dbResult = if (SettingsModel.isSqlServerWebDb) {
                        SQLServerWrapper.getListOf(
                            "st_item",
                            "",
                            mutableListOf(
                                "max(it_barcode) as it_barcode"
                            ),
                            "it_cmp_id='${SettingsModel.getCompanyID()}' AND ISNUMERIC(it_barcode)=1"
                        )
                    } else {
                        SQLServerWrapper.getListOf(
                            "st_item",
                            "",
                            mutableListOf(
                                "max(it_barcode) as it_barcode"
                            ),
                            "ISNUMERIC(it_barcode)=1"
                        )
                    }
                    dbResult?.let {
                        while (it.next()) {
                            barcode = it.getStringValue("it_barcode")
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return ((barcode?.toLongOrNull() ?: 9999) + 1).toString()
            }
        }
    }

    override suspend fun getOneItemByFamily(familyId: String): Item? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "st_item",
                    limit = 1,
                    filters = mutableListOf(
                        Filter.equalTo(
                            "it_fa_id",
                            familyId
                        )
                    )
                )
                val size = querySnapshot?.size() ?: 0
                if (size > 0) {
                    for (document in querySnapshot!!) {
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

    override suspend fun updateWarehouseData(item: Item): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                return DataModel(null)
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return DataModel(null)
            }

            else -> {
                val succeed = if (!item.itemWarehouseRowId.isNullOrEmpty()) {
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
                } else {
                    SQLServerWrapper.executeProcedure(
                        "addst_item_warehouse",
                        listOf(
                            SettingsModel.getCompanyID(),//@uw_cmp_id
                            item.itemWarehouse,//@uw_wa_name
                            item.itemId,//@uw_it_id
                            item.itemOpenQty,//@uw_openqty
                            null,//@uw_qtyonhand
                            item.itemLocation,//@uw_location
                            SettingsModel.currentUser?.userUsername,//@uw_userstamp
                            SettingsModel.currentCompany?.cmp_multibranchcode,//@branchcode
                            null,//@uw_minqty
                            0.0,//@uw_maxqty
                        )
                    ).succeed
                }

                return DataModel(
                    item,
                    succeed
                )
            }
        }
    }

    override suspend fun updateOpening(item: Item): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                return DataModel(null)
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return DataModel(null)
            }

            else -> {
                val succeed = if (!item.itemOpeningId.isNullOrEmpty()) {
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
                            item.itemOpenCost,
                            item.itemCostFirst,
                            item.itemCostSecond,
                            Timestamp(System.currentTimeMillis()),
                            SettingsModel.currentUser?.userUsername
                        ),
                        "op_id = '${item.itemOpeningId}'"
                    )
                } else {
                    SQLServerWrapper.executeProcedure(
                        "addst_opening",
                        listOf(
                            SettingsModel.getCompanyID(),//@op_cmp_id
                            item.itemId,//@op_it_id
                            Timestamp(System.currentTimeMillis()),//@op_date
                            item.itemOpenCost,//@op_unitcost
                            item.itemCostFirst,//@op_unitcostf
                            item.itemCostSecond,//@op_unitcosts
                            SettingsModel.currentUser?.userUsername,//@op_userstamp
                            SettingsModel.currentCompany?.cmp_multibranchcode,//@branchcode
                        )
                    ).succeed
                }
                return DataModel(
                    item,
                    succeed
                )
            }
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
            itemAltName = row.getStringValue("it_altname")
            itemSize = row.getStringValue("it_size")
            itemColor = row.getStringValue("it_color")
            itemCode = row.getStringValue("it_code")
            itemBranchName = row.getStringValue("it_bra_name")
            itemGroupButtonId = row.getStringValue("ib_gb_id")


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
            itemCostFirst = row.getDoubleValue("op_unitcostf")
            itemCostSecond = row.getDoubleValue("op_unitcosts")
            itemRealUnitPrice = 0.0
        }
    }

    private fun insertByProcedure(item: Item): DataModel {
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
                0,//@it_inactive
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
                getSecondCurrency(item.itemCurrencyId),//@secondcurr
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
                0,//@it_inactive
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
                getSecondCurrency(item.itemCurrencyId),//@secondcurr
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
        val dbResult = SQLServerWrapper.executeProcedure(
            "addst_item",
            parameters
        )
        if (dbResult.succeed) {
            SQLServerWrapper.executeProcedure(
                "showitemonpos",
                listOf(item.itemId, item.itemPos)
            )
        }
        return DataModel(
            item,
            dbResult.succeed
        )
    }

    private fun updateItem(
        item: Item,
        updateShowInPOS: Boolean = false
    ): DataModel {
        val succeed = SQLServerWrapper.update(
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
        if (succeed && updateShowInPOS) {
            SQLServerWrapper.executeProcedure(
                "showitemonpos",
                listOf(item.itemId, item.itemPos)
            )
        }
        return DataModel(
            item,
            succeed
        )
    }

    private fun deleteByProcedure(item: Item): DataModel {
        val dbResult = SQLServerWrapper.executeProcedure(
            "delst_item",
            listOf(
                item.itemId,
                item.itemUserStamp,
                SettingsModel.currentCompany?.cmp_multibranchcode
            )
        )
        return DataModel(
            item,
            dbResult.succeed
        )
    }

    private fun getSecondCurrency(itemCurr: String?): String? {
        return if (itemCurr == SettingsModel.currentCurrency?.currencyId) {
            SettingsModel.currentCurrency?.currencyDocumentId//secondOne
        } else {
            SettingsModel.currentCurrency?.currencyId//firstOne
        }
    }

}