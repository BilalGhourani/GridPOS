package com.grid.pos.data.stockAdjustment

import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.DataModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Extension.getDoubleValue
import com.grid.pos.utils.Extension.getObjectValue
import com.grid.pos.utils.Extension.getStringValue
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.Date

class StockAdjustmentRepositoryImpl : StockAdjustmentRepository {
    override suspend fun insert(stockAdjustment: StockAdjustment): DataModel {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                DataModel(stockAdjustment)
            }

            else -> {
                insertByProcedure(stockAdjustment)
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

    override suspend fun update(stockAdjustment: StockAdjustment): DataModel {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                DataModel(stockAdjustment)
            }

            else -> {
                updateByProcedure(stockAdjustment)
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
                        "sa_hsa_id='$stockHeaderAdjId'"
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
            stockAdjWaName = obj.getStringValue("sa_wa_name")
            stockAdjQty = obj.getDoubleValue("sa_qty")
            stockAdjPuId = obj.getStringValue("sa_pu_id")
            stockAdjItemIdInPack = obj.getStringValue("sa_it_idinpack")
            stockAdjQtyInPack = obj.getDoubleValue("sa_qtyinpack")
            stockAdjCost = obj.getDoubleValue("sa_cost")
            stockAdjCurrRateF = obj.getDoubleValue("sa_mcurratef")
            stockAdjCurrRateS = obj.getDoubleValue("sa_mcurrates")
            stockAdjRemQty = obj.getDoubleValue("sa_remqty")
            stockAdjRemQtyWa = obj.getDoubleValue("sa_remqtywa")

            val timeStamp = obj.getObjectValue("sa_timestamp")
            stockAdjTimeStamp = if (timeStamp is Date) timeStamp else DateHelper.getDateFromString(
                timeStamp as String,
                "yyyy-MM-dd hh:mm:ss.SSS"
            )
            stockAdjUserStamp = obj.getStringValue("sa_userstamp")
            stockAdjRowguid = obj.getStringValue("sa_rowguid")
            stockAdjDivName = obj.getStringValue("sa_div_name")

            val saDate = obj.getObjectValue("sa_date")
            stockAdjDate = if (saDate is Date) saDate else DateHelper.getDateFromString(
                saDate as String,
                "yyyy-MM-dd hh:mm:ss.SSS"
            )
        }
    }

    private fun insertByProcedure(stockAdjustment: StockAdjustment): DataModel {
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
                SettingsModel.currentCompany?.cmp_multibranchcode,//@branchcode
                stockAdjustment.stockAdjDivName,//@sa_div_name
                Timestamp(System.currentTimeMillis()),//@sa_date
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

    private fun updateByProcedure(stockAdjustment: StockAdjustment): DataModel {
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