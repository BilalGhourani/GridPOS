package com.grid.pos.data.stockInOut

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
import java.util.Date

class StockInOutRepositoryImpl : StockInOutRepository {
    override suspend fun insert(stockInOut: StockInOut): DataModel {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                DataModel(stockInOut)
            }

            else -> {
                insertByProcedure(stockInOut)
            }
        }
    }

    override suspend fun delete(stockInOut: StockInOut): DataModel {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                DataModel(stockInOut)
            }

            else -> {
                deleteByProcedure(stockInOut)
            }
        }
    }

    override suspend fun update(stockInOut: StockInOut): DataModel {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                DataModel(stockInOut)
            }

            else -> {
                updateByProcedure(stockInOut)
            }
        }
    }

    override suspend fun getAllStockInOuts(stockHeaderInOutId: String): MutableList<StockInOut> {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                return mutableListOf()
            }

            else -> {
                val stockInOuts: MutableList<StockInOut> = mutableListOf()
                try {
                    val dbResult = SQLServerWrapper.getListOf(
                        "st_stockinout",
                        "",
                        mutableListOf("*"),
                        "io_hio_id='$stockHeaderInOutId'",
                       if(SettingsModel.isSqlServerWebDb) "ORDER BY io_lineno ASC" else "ORDER BY io_timestamp ASC"
                    )
                    dbResult?.let {
                        while (it.next()) {
                            stockInOuts.add(
                                fillParams(it)
                            )
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return stockInOuts
            }
        }
    }

    private fun fillParams(
        obj: ResultSet
    ): StockInOut {
        return StockInOut().apply {
            stockInOutId = obj.getStringValue("io_id")
            stockInOutHeaderId = obj.getStringValue("io_hio_id")
            stockInOutItemId = obj.getStringValue("io_it_id")
            stockInOutQty = obj.getDoubleValue("io_qty")
            stockInOutType = obj.getStringValue("io_type")
            stockInOutWaTpName = obj.getStringValue("io_wa_tp_name")
            stockInOutRemQtyWa = obj.getDoubleValue("io_remqtywa")
            stockInOutItemIdInPack = obj.getStringValue("io_it_idinpack")
            stockInOutQtyInPack = obj.getDoubleValue("io_qtyinpack")
            stockInOutNote = obj.getStringValue("io_note")

            val timeStamp = obj.getObjectValue("io_timestamp")
            stockInOutTimeStamp =
                if (timeStamp is Date) timeStamp else DateHelper.getDateFromString(
                    timeStamp as String,
                    "yyyy-MM-dd hh:mm:ss.SSS"
                )
            stockInOutUserStamp = obj.getStringValue("io_userstamp")
            stockInOutDivName = obj.getStringValue("io_div_name")
            stockInOutRemQtyWa = obj.getDoubleValue("io_remqtywahio")
            stockInOutCost = obj.getDoubleValue("io_cost")
            stockInOutCurRateF = obj.getDoubleValue("io_mcurratef")
            stockInOutCurRateS = obj.getDoubleValue("io_mcurrates")
            stockInOutOrder = obj.getIntValue("io_order")
            stockInOutLineNo = obj.getIntValue("io_lineno")
        }
    }

    private fun insertByProcedure(stockInOut: StockInOut): DataModel {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                null,//stockInOut.stockInOutId,//@io_id
                stockInOut.stockInOutHeaderId,//@io_hio_id
                stockInOut.stockInOutItemId,//@io_it_id
                stockInOut.stockInOutQty,//@io_qty
                stockInOut.stockInOutType,//@io_type
                stockInOut.stockInOutWaTpName,//@io_wa_tp_name
                stockInOut.stockInOutNote,//@io_note
                SettingsModel.currentUser?.userUsername,//@io_userstamp
                SettingsModel.currentCompany?.cmp_multibranchcode,//@branchcode
                stockInOut.stockInOutDivName,//@sa_div_name
                stockInOut.stockInOutLineNo,//@io_lineno
            )
        } else {
            listOf(
                null,//stockInOut.stockInOutId,//@io_id
                stockInOut.stockInOutHeaderId,//@io_hio_id
                stockInOut.stockInOutItemId,//@io_it_id
                stockInOut.stockInOutQty,//@io_qty
                stockInOut.stockInOutType,//@io_type
                stockInOut.stockInOutWaTpName,//@io_wa_tp_name
                stockInOut.stockInOutNote,//@io_note
                SettingsModel.currentUser?.userUsername,//@io_userstamp
                SettingsModel.currentCompany?.cmp_multibranchcode,//@branchcode
                stockInOut.stockInOutDivName,//@sa_div_name
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "addst_stockinout",
            parameters
        )
        return if (queryResult.succeed) {
            stockInOut.stockInOutId = queryResult.result ?: ""
            DataModel(stockInOut)
        } else {
            DataModel(
                stockInOut,
                false
            )
        }
    }

    private fun updateByProcedure(stockInOut: StockInOut): DataModel {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                stockInOut.stockInOutId,//@io_id
                stockInOut.stockInOutHeaderId,//@io_hio_id
                stockInOut.stockInOutItemId,//@io_it_id
                stockInOut.stockInOutQty,//@io_qty
                stockInOut.stockInOutType,//@io_type
                stockInOut.stockInOutWaTpName,//@io_wa_tp_name
                stockInOut.stockInOutNote,//@io_note
                SettingsModel.currentUser?.userUsername,//@io_userstamp
                stockInOut.stockInOutDivName,//@sa_div_name
                stockInOut.stockInOutLineNo,//@io_lineno
            )
        } else {
            listOf(
                stockInOut.stockInOutId,//@io_id
                stockInOut.stockInOutHeaderId,//@io_hio_id
                stockInOut.stockInOutItemId,//@io_it_id
                stockInOut.stockInOutQty,//@io_qty
                stockInOut.stockInOutType,//@io_type
                stockInOut.stockInOutWaTpName,//@io_wa_tp_name
                stockInOut.stockInOutNote,//@io_note
                SettingsModel.currentUser?.userUsername,//@io_userstamp
                stockInOut.stockInOutDivName,//@sa_div_name
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "updst_stockinout",
            parameters
        )
        return if (queryResult.succeed) {
            DataModel(stockInOut)
        } else {
            DataModel(
                stockInOut,
                false
            )
        }
    }

    private fun deleteByProcedure(stockInOut: StockInOut): DataModel {
        val queryResult = SQLServerWrapper.executeProcedure(
            "delst_stockinout",
            listOf(stockInOut.stockInOutId)
        )
        return if (queryResult.succeed) {
            DataModel(stockInOut)
        } else {
            DataModel(
                stockInOut,
                false
            )
        }
    }
}