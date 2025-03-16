package com.grid.pos.data.stockAdjustment

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

class StockAdjustmentRepositoryImpl : StockAdjustmentRepository {
    override suspend fun insert(
        stockAdjustment: StockAdjustment,
        source: String
    ): DataModel {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                DataModel(stockAdjustment)
            }

            else -> {
                if (source.equals("stkadj", ignoreCase = true)) {
                    insertStkAdjByProcedure(stockAdjustment)
                } else {
                    insertQtyOnHandByProcedure(stockAdjustment)
                }
            }
        }
    }

    override suspend fun delete(stockAdjustment: StockAdjustment): DataModel {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                DataModel(stockAdjustment)
            }

            else -> {
                deleteByProcedure(stockAdjustment)
            }
        }
    }

    override suspend fun update(
        stockAdjustment: StockAdjustment,
        source: String
    ): DataModel {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                DataModel(stockAdjustment)
            }

            else -> {
                if (source.equals("stkadj", ignoreCase = true)) {
                    updateStkAdjByProcedure(stockAdjustment)
                } else {
                    updateQtyOnHandByProcedure(stockAdjustment)
                }
            }
        }
    }

    override suspend fun getAllStockAdjustments(stockHeaderAdjId: String): MutableList<StockAdjustment> {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                return mutableListOf()
            }

            else -> {
                val stockAdjustments: MutableList<StockAdjustment> = mutableListOf()
                try {
                    val dbResult = SQLServerWrapper.getListOf(
                        "st_stockadjustment",
                        "",
                        mutableListOf("*"),
                        "sa_hsa_id='$stockHeaderAdjId'",
                        if (SettingsModel.isSqlServerWebDb) "ORDER BY sa_lineno ASC" else "ORDER BY sa_timestamp ASC"
                    )
                    dbResult?.let {
                        while (it.next()) {
                            stockAdjustments.add(
                                fillParams(it)
                            )
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return stockAdjustments
            }
        }
    }

    private fun fillParams(
        obj: ResultSet
    ): StockAdjustment {
        return StockAdjustment().apply {
            stockAdjId = obj.getStringValue("sa_id")
            stockAdjHeaderId = obj.getStringValue("sa_hsa_id")
            stockAdjItemId = obj.getStringValue("sa_it_id")
            stockAdjReason = obj.getStringValue("sa_reason")
            stockAdjWaName = obj.getStringValue("sa_wa_name").ifEmpty { null }
            stockAdjQty = obj.getStringValue("sa_qty").toDoubleOrNull()
            stockAdjPuId = obj.getStringValue("sa_pu_id").ifEmpty { null }
            stockAdjItemIdInPack = obj.getStringValue("sa_it_idinpack").ifEmpty { null }
            stockAdjQtyInPack = obj.getStringValue("sa_qtyinpack").toDoubleOrNull()
            stockAdjCost = obj.getStringValue("sa_cost").toDoubleOrNull()
            stockAdjCurrRateF = obj.getStringValue("sa_mcurratef").toDoubleOrNull()
            stockAdjCurrRateS = obj.getStringValue("sa_mcurrates").toDoubleOrNull()
            stockAdjRemQty = obj.getStringValue("sa_remqty").toDoubleOrNull()
            stockAdjRemQtyWa = obj.getStringValue("sa_remqtywa").toDoubleOrNull()
            stockAdjLineNo = obj.getIntValue("sa_lineno")

            val timeStamp = obj.getObjectValue("sa_timestamp")
            stockAdjTimeStamp = if (timeStamp is Date) timeStamp else DateHelper.getDateFromString(
                timeStamp as String,
                "yyyy-MM-dd hh:mm:ss.SSS"
            )
            stockAdjUserStamp = obj.getStringValue("sa_userstamp")
            stockAdjRowguid = obj.getStringValue("sa_rowguid").ifEmpty { null }
            stockAdjDivName = obj.getStringValue("sa_div_name").ifEmpty { null }

            val saDate = obj.getObjectValue("sa_date")
            stockAdjDate = if (saDate is Date) saDate else DateHelper.getDateFromString(
                saDate as String,
                "yyyy-MM-dd hh:mm:ss.SSS"
            )
        }
    }

    private fun insertStkAdjByProcedure(stockAdjustment: StockAdjustment): DataModel {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                null,//@sa_id
                stockAdjustment.stockAdjHeaderId,//@sa_hsa_id
                stockAdjustment.stockAdjItemId,//@sa_it_id
                stockAdjustment.stockAdjReason,//@sa_reason
                stockAdjustment.stockAdjWaName,//@sa_wa_name
                stockAdjustment.stockAdjQty,//@sa_qty
                stockAdjustment.stockAdjPuId,//@sa_pu_id
                SettingsModel.currentUser?.userUsername,//@sa_userstamp
                SettingsModel.currentCompany?.cmp_multibranchcode,//@branchcode
                stockAdjustment.stockAdjDivName,//@sa_div_name
                Timestamp(System.currentTimeMillis()),//@sa_date
                stockAdjustment.stockAdjLineNo,//@sa_lineno
            )
        } else {
            listOf(
                null,//@sa_id
                stockAdjustment.stockAdjHeaderId,//@sa_hsa_id
                stockAdjustment.stockAdjItemId,//@sa_it_id
                stockAdjustment.stockAdjReason,//@sa_reason
                stockAdjustment.stockAdjWaName,//@sa_wa_name
                stockAdjustment.stockAdjQty,//@sa_qty
                stockAdjustment.stockAdjPuId,//@sa_pu_id
                SettingsModel.currentUser?.userUsername,//@sa_userstamp
                SettingsModel.currentCompany?.cmp_multibranchcode,//@branchcode
                stockAdjustment.stockAdjDivName,//@sa_div_name
                Timestamp(System.currentTimeMillis()),//@sa_date
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "addst_stockadjustment",
            parameters
        )
        return if (queryResult.succeed) {
            stockAdjustment.stockAdjId = queryResult.result ?: ""
            DataModel(stockAdjustment)
        } else {
            DataModel(
                stockAdjustment,
                false
            )
        }
    }

    private fun insertQtyOnHandByProcedure(stockAdjustment: StockAdjustment): DataModel {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                null,//@sa_id
                stockAdjustment.stockAdjHeaderId,//@sa_hsa_id
                stockAdjustment.stockAdjItemId,//@sa_it_id
                stockAdjustment.stockAdjReason,//@sa_reason
                stockAdjustment.stockAdjWaName,//@sa_wa_name
                stockAdjustment.stockAdjQty,//@sa_qty
                stockAdjustment.stockAdjPuId,//@sa_pu_id
                stockAdjustment.stockAdjCost,//@sa_cost
                stockAdjustment.stockAdjRemQty,//@sa_remqty
                stockAdjustment.stockAdjRemQtyWa,//@sa_remqtywa
                SettingsModel.currentUser?.userUsername,//@sa_userstamp
                SettingsModel.currentCompany?.cmp_multibranchcode,//@branchcode
                stockAdjustment.stockAdjDivName,//@sa_div_name
                Timestamp(System.currentTimeMillis()),//@sa_date
                stockAdjustment.stockAdjLineNo,//@sa_lineno
            )
        } else {
            listOf(
                null,//@sa_id
                stockAdjustment.stockAdjHeaderId,//@sa_hsa_id
                stockAdjustment.stockAdjItemId,//@sa_it_id
                stockAdjustment.stockAdjReason,//@sa_reason
                stockAdjustment.stockAdjWaName,//@sa_wa_name
                stockAdjustment.stockAdjQty,//@sa_qty
                stockAdjustment.stockAdjPuId,//@sa_pu_id
                stockAdjustment.stockAdjCost,//@sa_cost
                stockAdjustment.stockAdjRemQty,//@sa_remqty
                stockAdjustment.stockAdjRemQtyWa,//@sa_remqtywa
                SettingsModel.currentUser?.userUsername,//@sa_userstamp
                SettingsModel.currentCompany?.cmp_multibranchcode,//@branchcode
                stockAdjustment.stockAdjDivName,//@sa_div_name
                Timestamp(System.currentTimeMillis()),//@sa_date
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "addst_stockadjustmentqtyonhand",
            parameters
        )
        return if (queryResult.succeed) {
            stockAdjustment.stockAdjId = queryResult.result ?: ""
            DataModel(stockAdjustment)
        } else {
            DataModel(
                stockAdjustment,
                false
            )
        }
    }

    private fun updateStkAdjByProcedure(stockAdjustment: StockAdjustment): DataModel {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                stockAdjustment.stockAdjId,//@sa_id
                stockAdjustment.stockAdjHeaderId,//@sa_hsa_id
                stockAdjustment.stockAdjItemId,//@sa_it_id
                stockAdjustment.stockAdjReason,//@sa_reason
                stockAdjustment.stockAdjWaName,//@sa_wa_name
                stockAdjustment.stockAdjQty,//@sa_qty
                stockAdjustment.stockAdjPuId,//@sa_pu_id
                SettingsModel.currentUser?.userUsername,//@sa_userstamp
                stockAdjustment.stockAdjDivName,//@sa_div_name
                Timestamp(System.currentTimeMillis()),//@sa_date
                stockAdjustment.stockAdjLineNo,//@sa_lineno
            )
        } else {
            listOf(
                stockAdjustment.stockAdjId,//@sa_id
                stockAdjustment.stockAdjHeaderId,//@sa_hsa_id
                stockAdjustment.stockAdjItemId,//@sa_it_id
                stockAdjustment.stockAdjReason,//@sa_reason
                stockAdjustment.stockAdjWaName,//@sa_wa_name
                stockAdjustment.stockAdjQty,//@sa_qty
                stockAdjustment.stockAdjPuId,//@sa_pu_id
                SettingsModel.currentUser?.userUsername,//@sa_userstamp
                stockAdjustment.stockAdjDivName,//@sa_div_name
                Timestamp(System.currentTimeMillis()),//@sa_date
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "updst_stockadjustment",
            parameters
        )
        return if (queryResult.succeed) {
            DataModel(stockAdjustment)
        } else {
            DataModel(
                stockAdjustment,
                false
            )
        }
    }

    private fun updateQtyOnHandByProcedure(stockAdjustment: StockAdjustment): DataModel {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                stockAdjustment.stockAdjId,//@sa_id
                stockAdjustment.stockAdjHeaderId,//@sa_hsa_id
                stockAdjustment.stockAdjItemId,//@sa_it_id
                stockAdjustment.stockAdjReason,//@sa_reason
                stockAdjustment.stockAdjWaName,//@sa_wa_name
                stockAdjustment.stockAdjQty,//@sa_qty
                stockAdjustment.stockAdjPuId,//@sa_pu_id
                stockAdjustment.stockAdjCost,//@sa_cost
                stockAdjustment.stockAdjRemQty,//@sa_remqty
                stockAdjustment.stockAdjRemQtyWa,//@sa_remqtywa
                SettingsModel.currentUser?.userUsername,//@sa_userstamp
                stockAdjustment.stockAdjDivName,//@sa_div_name
                Timestamp(System.currentTimeMillis()),//@sa_date
                stockAdjustment.stockAdjLineNo,//@sa_lineno
            )
        } else {
            listOf(
                stockAdjustment.stockAdjId,//@sa_id
                stockAdjustment.stockAdjHeaderId,//@sa_hsa_id
                stockAdjustment.stockAdjItemId,//@sa_it_id
                stockAdjustment.stockAdjReason,//@sa_reason
                stockAdjustment.stockAdjWaName,//@sa_wa_name
                stockAdjustment.stockAdjQty,//@sa_qty
                stockAdjustment.stockAdjPuId,//@sa_pu_id
                stockAdjustment.stockAdjCost,//@sa_cost
                stockAdjustment.stockAdjRemQty,//@sa_remqty
                stockAdjustment.stockAdjRemQtyWa,//@sa_remqtywa
                SettingsModel.currentUser?.userUsername,//@sa_userstamp
                stockAdjustment.stockAdjDivName,//@sa_div_name
                Timestamp(System.currentTimeMillis()),//@sa_date
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "updst_stockadjustmentqtyonhand",
            parameters
        )
        return if (queryResult.succeed) {
            DataModel(stockAdjustment)
        } else {
            DataModel(
                stockAdjustment,
                false
            )
        }
    }

    private fun deleteByProcedure(stockAdjustment: StockAdjustment): DataModel {
        val queryResult = SQLServerWrapper.executeProcedure(
            "delst_stockadjustment",
            listOf(stockAdjustment.stockAdjId)
        )
        return if (queryResult.succeed) {
            DataModel(stockAdjustment)
        } else {
            DataModel(
                stockAdjustment,
                false
            )
        }
    }
}