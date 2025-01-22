package com.grid.pos.data.stockInOut.header

import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.data.stockHeadInOut.header.StockHeaderInOut
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.DataModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Extension.getObjectValue
import com.grid.pos.utils.Extension.getStringValue
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.Date

class StockHeaderInOutRepositoryImpl : StockHeaderInOutRepository {
    override suspend fun insert(stockHeaderInOut: StockHeaderInOut): DataModel {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                DataModel(stockHeaderInOut)
            }

            else -> {
                insertByProcedure(stockHeaderInOut)
            }
        }
    }

    override suspend fun delete(stockHeaderInOut: StockHeaderInOut): DataModel {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                DataModel(stockHeaderInOut)
            }

            else -> {
                deleteByProcedure(stockHeaderInOut)
            }
        }
    }

    override suspend fun update(stockHeaderInOut: StockHeaderInOut): DataModel {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                DataModel(stockHeaderInOut)
            }

            else -> {
                updateByProcedure(stockHeaderInOut)
            }
        }
    }

    override suspend fun getAllStockHeaderInOuts(): MutableList<StockHeaderInOut> {
        val limit = 100
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                return mutableListOf()
            }

            else -> {
                val stockHeaderAdjustments: MutableList<StockHeaderInOut> = mutableListOf()
                try {
                    val where = "hio_cmp_id='${SettingsModel.getCompanyID()}'"
                    val dbResult = SQLServerWrapper.getListOf(
                        "st_hstockinout",
                        "TOP $limit",
                        if (SettingsModel.isSqlServerWebDb) mutableListOf("*,tt.tt_newcode") else mutableListOf(
                            "*"
                        ),
                        where,
                        "ORDER BY hio_date DESC",
                        if (SettingsModel.isSqlServerWebDb) "INNER JOIN acc_transactiontype tt on hio_tt_code = tt.tt_code" else ""
                    )
                    dbResult?.let {
                        while (it.next()) {
                            stockHeaderAdjustments.add(
                                fillParams(it)
                            )
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return stockHeaderAdjustments
            }
        }
    }

    private fun fillParams(
        obj: ResultSet
    ): StockHeaderInOut {
        return StockHeaderInOut().apply {
            stockHeadInOutId = obj.getStringValue("hio_id")
            stockHeadInOutNo = obj.getStringValue("hio_no")
            stockHeadInOutCmpId = obj.getStringValue("hio_cmp_id")
            stockHeadInOutWaName = obj.getStringValue("hio_wa_name")
            stockHeadInOutInOut = obj.getStringValue("hio_inout")
            stockHeadInOutType = obj.getStringValue("hio_type")
            stockHeadInOutWaTpName = obj.getStringValue("hio_wa_tp_name")
            stockHeadInOutDate = obj.getStringValue("hio_date")
            stockHeadInOutTtCode = obj.getStringValue("tt_newcode", obj.getStringValue("hio_tt_code"))
            stockHeadInOutTransNo = obj.getStringValue("hio_transno")
            stockHeadInOutDesc = obj.getStringValue("hio_desc")
            stockHeadInOutPrjName = obj.getStringValue("hio_prj_name")
            stockHeadInOutBraName = obj.getStringValue("hio_bra_name")
            stockHeadInOutNote = obj.getStringValue("hio_note")

            val timeStamp = obj.getObjectValue("hio_timestamp")
            stockHeadInOutTimeStamp = if (timeStamp is Date) timeStamp else DateHelper.getDateFromString(
                timeStamp as String,
                "yyyy-MM-dd hh:mm:ss.SSS"
            )
            stockHeadInOutUserStamp = obj.getStringValue("hio_userstamp")
            val valueDate = obj.getObjectValue("hio_valuedate")
            stockHeadInOutValueDate = if (valueDate is Date) valueDate else DateHelper.getDateFromString(
                valueDate as String,
                "yyyy-MM-dd hh:mm:ss.SSS"
            )
            stockHeadInOutHjNo = obj.getStringValue("hio_hj_no")
        }
    }

    private fun insertByProcedure(stockHeaderInOut:StockHeaderInOut): DataModel {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                "null_string_output",//@hio_id
                stockHeaderInOut.stockHeadInOutCmpId,//@hio_cmp_id
                stockHeaderInOut.stockHeadInOutWaName,//@hio_wa_name
                stockHeaderInOut.stockHeadInOutInOut,//@hio_inout
                stockHeaderInOut.stockHeadInOutType,//@hio_type
                stockHeaderInOut.stockHeadInOutWaTpName,//@hio_wa_tp_name
                Timestamp(System.currentTimeMillis()),//@hio_date
                stockHeaderInOut.stockHeadInOutTtCode,//@hio_tt_code
                stockHeaderInOut.stockHeadInOutTransNo,//@hio_transno
                stockHeaderInOut.stockHeadInOutDesc,//@hio_desc
                stockHeaderInOut.stockHeadInOutPrjName,//@hio_prj_name
                SettingsModel.defaultSqlServerBranch,//@hio_bra_name
                stockHeaderInOut.stockHeadInOutNote,//@hio_note
                SettingsModel.currentUser?.userUsername,//@hio_userstamp
                null,//@hio_sessionpointer
                SettingsModel.currentCompany?.cmp_multibranchcode,//@branchcode
                Timestamp(System.currentTimeMillis()),//@hio_valuedate
            )
        } else {
            listOf(
                "null_string_output",//@hio_id
                stockHeaderInOut.stockHeadInOutCmpId,//@hio_cmp_id
                stockHeaderInOut.stockHeadInOutWaName,//@hio_wa_name
                stockHeaderInOut.stockHeadInOutInOut,//@hio_inout
                stockHeaderInOut.stockHeadInOutType,//@hio_type
                stockHeaderInOut.stockHeadInOutWaTpName,//@hio_wa_tp_name
                Timestamp(System.currentTimeMillis()),//@hio_date
                stockHeaderInOut.stockHeadInOutTtCode,//@hio_tt_code
                stockHeaderInOut.stockHeadInOutTransNo,//@hio_transno
                stockHeaderInOut.stockHeadInOutDesc,//@hio_desc
                stockHeaderInOut.stockHeadInOutPrjName,//@hio_prj_name
                SettingsModel.defaultSqlServerBranch,//@hio_bra_name
                stockHeaderInOut.stockHeadInOutNote,//@hio_note
                SettingsModel.currentUser?.userUsername,//@hio_userstamp
                null,//@hio_sessionpointer
                SettingsModel.currentCompany?.cmp_multibranchcode,//@branchcode
                Timestamp(System.currentTimeMillis()),//@hio_valuedate
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "addst_hstockinout",
            parameters
        )
        return if (queryResult.succeed) {
            stockHeaderInOut.stockHeadInOutId = queryResult.result ?: ""
            DataModel(stockHeaderInOut)
        } else {
            DataModel(
                stockHeaderInOut,
                false
            )
        }
    }

    private fun updateByProcedure(stockHeaderInOut:StockHeaderInOut): DataModel {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                stockHeaderInOut.stockHeadInOutId,//@hio_id
                stockHeaderInOut.stockHeadInOutCmpId,//@hio_cmp_id
                stockHeaderInOut.stockHeadInOutWaName,//@hio_wa_name
                stockHeaderInOut.stockHeadInOutInOut,//@hio_inout
                stockHeaderInOut.stockHeadInOutType,//@hio_type
                stockHeaderInOut.stockHeadInOutWaTpName,//@hio_wa_tp_name
                stockHeaderInOut.stockHeadInOutDate,//@hio_date
                stockHeaderInOut.stockHeadInOutTtCode,//@hio_tt_code
                stockHeaderInOut.stockHeadInOutTransNo,//@hio_transno
                stockHeaderInOut.stockHeadInOutDesc,//@hio_desc
                stockHeaderInOut.stockHeadInOutPrjName,//@hio_prj_name
                stockHeaderInOut.stockHeadInOutBraName,//@hio_bra_name
                stockHeaderInOut.stockHeadInOutNote,//@hio_note
                SettingsModel.currentUser?.userUsername,//@hio_userstamp
                stockHeaderInOut.stockHeadInOutValueDate,//@hio_valuedate
            )
        } else {
            listOf(
                stockHeaderInOut.stockHeadInOutId,//@hio_id
                stockHeaderInOut.stockHeadInOutCmpId,//@hio_cmp_id
                stockHeaderInOut.stockHeadInOutWaName,//@hio_wa_name
                stockHeaderInOut.stockHeadInOutInOut,//@hio_inout
                stockHeaderInOut.stockHeadInOutType,//@hio_type
                stockHeaderInOut.stockHeadInOutWaTpName,//@hio_wa_tp_name
                stockHeaderInOut.stockHeadInOutDate,//@hio_date
                stockHeaderInOut.stockHeadInOutTtCode,//@hio_tt_code
                stockHeaderInOut.stockHeadInOutTransNo,//@hio_transno
                stockHeaderInOut.stockHeadInOutDesc,//@hio_desc
                stockHeaderInOut.stockHeadInOutPrjName,//@hio_prj_name
                stockHeaderInOut.stockHeadInOutBraName,//@hio_bra_name
                stockHeaderInOut.stockHeadInOutNote,//@hio_note
                SettingsModel.currentUser?.userUsername,//@hio_userstamp
                stockHeaderInOut.stockHeadInOutValueDate,//@hio_valuedate
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "updst_hstockinout",
            parameters
        )
        return if (queryResult.succeed) {
            DataModel(stockHeaderInOut)
        } else {
            DataModel(
                stockHeaderInOut,
                false
            )
        }
    }

    private fun deleteByProcedure(stockHeaderInOut:StockHeaderInOut): DataModel {
        val queryResult = SQLServerWrapper.executeProcedure(
            "delst_hstockinout",
            listOf(stockHeaderInOut.stockHeadInOutId)
        )
        return if (queryResult.succeed) {
            DataModel(stockHeaderInOut)
        } else {
            DataModel(
                stockHeaderInOut,
                false
            )
        }
    }

}