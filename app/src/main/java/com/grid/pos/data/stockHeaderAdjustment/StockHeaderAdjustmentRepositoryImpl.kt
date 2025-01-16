package com.grid.pos.data.stockHeaderAdjustment

import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.DataModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Extension.getObjectValue
import com.grid.pos.utils.Extension.getStringValue
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.Date

class StockHeaderAdjustmentRepositoryImpl : StockHeaderAdjustmentRepository {
    override suspend fun insert(stockHeaderAdjustment: StockHeaderAdjustment): DataModel {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                DataModel(stockHeaderAdjustment)
            }

            else -> {
                insertByProcedure(stockHeaderAdjustment)
            }
        }
    }

    override suspend fun delete(stockHeaderAdjustment: StockHeaderAdjustment): DataModel {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                DataModel(stockHeaderAdjustment)
            }

            else -> {
                deleteByProcedure(stockHeaderAdjustment)
            }
        }
    }

    override suspend fun update(stockHeaderAdjustment: StockHeaderAdjustment): DataModel {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                DataModel(stockHeaderAdjustment)
            }

            else -> {
                updateByProcedure(stockHeaderAdjustment)
            }
        }
    }

    override suspend fun getAllStockHeaderAdjustments(): MutableList<StockHeaderAdjustment> {
        val limit = 100
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                return mutableListOf()
            }

            else -> {
                val stockHeaderAdjustments: MutableList<StockHeaderAdjustment> = mutableListOf()
                try {
                    val where = "hsa_cmp_id='${SettingsModel.getCompanyID()}'"
                    val dbResult = SQLServerWrapper.getListOf(
                        "st_hstockadjustment",
                        "TOP $limit",
                        if (SettingsModel.isSqlServerWebDb) mutableListOf("*,tt.tt_newcode") else mutableListOf(
                            "*"
                        ),
                        where,
                        "ORDER BY hsa_transno DESC",
                        if (SettingsModel.isSqlServerWebDb) "INNER JOIN acc_transactiontype tt on hsa_tt_code = tt.tt_code" else ""
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
    ): StockHeaderAdjustment {
        return StockHeaderAdjustment().apply {
            stockHAId = obj.getStringValue("hsa_id")
            stockHANo = obj.getStringValue("hsa_no")
            stockHACompId = obj.getStringValue("hsa_cmp_id")
            stockHADate = obj.getStringValue("hsa_date")
            stockHATtCode = obj.getStringValue("hsa_tt_code")
            stockHATtCodeName = obj.getStringValue("tt_newcode", obj.getStringValue("hsa_tt_code"))
            stockHATransNo = obj.getStringValue("hsa_transno")
            stockHADesc = obj.getStringValue("hsa_desc")
            stockHAProjName = obj.getStringValue("hsa_prj_name")
            stockHABraName = obj.getStringValue("hsa_bra_name")
            stockHAWaName = obj.getStringValue("hsa_wa_name")
            stockHASessionPointer = obj.getStringValue("hsa_sessionpointer")
            stockHARowguid = obj.getStringValue("hsa_rowguid")
            stockHASource = obj.getStringValue("hsa_source")
            stockHAHjNo = obj.getStringValue("hsa_hj_no")

            val valueDate = obj.getObjectValue("hsa_valuedate")
            stockHAValueDate = if (valueDate is Date) valueDate else DateHelper.getDateFromString(
                valueDate as String,
                "yyyy-MM-dd hh:mm:ss.SSS"
            )

            val timeStamp = obj.getObjectValue("hsa_timestamp")
            stockHATimeStamp = if (timeStamp is Date) timeStamp else DateHelper.getDateFromString(
                timeStamp as String,
                "yyyy-MM-dd hh:mm:ss.SSS"
            )
            stockHAUserStamp = obj.getStringValue("hsa_userstamp")
        }
    }

    private fun insertByProcedure(stockHeaderAdjustment: StockHeaderAdjustment): DataModel {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                stockHeaderAdjustment.stockHACompId,//@hsa_cmp_id
                Timestamp(System.currentTimeMillis()),//@hsa_date
                stockHeaderAdjustment.stockHATtCode,//@hsa_tt_code
                stockHeaderAdjustment.stockHATransNo,//@hsa_transno
                stockHeaderAdjustment.stockHADesc,//@hsa_desc
                null,//@hsa_prj_name
                SettingsModel.defaultSqlServerBranch,//@hsa_bra_name
                stockHeaderAdjustment.stockHAWaName,//@hsa_wa_name
                SettingsModel.currentUser?.userUsername,//@hsa_userstamp
                null,//@hsa_sessionpointer
                SettingsModel.currentCompany?.cmp_multibranchcode,//@branchcode
                Timestamp(System.currentTimeMillis()),//@hsa_valuedate
                stockHeaderAdjustment.stockHASource,//@hsa_source
            )
        } else {
            listOf(
                stockHeaderAdjustment.stockHACompId,//@hsa_cmp_id
                Timestamp(System.currentTimeMillis()),//@hsa_date
                stockHeaderAdjustment.stockHATtCode,//@hsa_tt_code
                stockHeaderAdjustment.stockHATransNo,//@hsa_transno
                stockHeaderAdjustment.stockHADesc,//@hsa_desc
                null,//@hsa_prj_name
                SettingsModel.defaultSqlServerBranch,//@hsa_bra_name
                stockHeaderAdjustment.stockHAWaName,//@hsa_wa_name
                SettingsModel.currentUser?.userUsername,//@hsa_userstamp
                null,//@hsa_sessionpointer
                SettingsModel.currentCompany?.cmp_multibranchcode,//@branchcode
                Timestamp(System.currentTimeMillis()),//@hsa_valuedate
                stockHeaderAdjustment.stockHASource,//@hsa_source
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "addst_hstockadjustment",
            parameters
        )
        return if (queryResult.succeed) {
            stockHeaderAdjustment.stockHAId = queryResult.result ?: ""
            DataModel(stockHeaderAdjustment)
        } else {
            DataModel(
                stockHeaderAdjustment,
                false
            )
        }
    }

    private fun updateByProcedure(stockHeaderAdjustment: StockHeaderAdjustment): DataModel {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                stockHeaderAdjustment.stockHAId,//@hsa_id
                stockHeaderAdjustment.stockHANo,//@hsa_no
                stockHeaderAdjustment.stockHACompId,//@hsa_cmp_id
                stockHeaderAdjustment.stockHADate,//@hsa_date
                stockHeaderAdjustment.stockHATtCode,//@hsa_tt_code
                stockHeaderAdjustment.stockHATransNo,//@hsa_transno
                stockHeaderAdjustment.stockHADesc,//@hsa_desc
                stockHeaderAdjustment.stockHAProjName,//@hsa_prj_name
                stockHeaderAdjustment.stockHABraName,//@hsa_bra_name
                stockHeaderAdjustment.stockHAWaName,//@hsa_wa_name
                SettingsModel.currentUser?.userUsername,//@hsa_userstamp
                Timestamp(System.currentTimeMillis()),//@hsa_valuedate
            )
        } else {
            listOf(
                stockHeaderAdjustment.stockHAId,//@hsa_id
                stockHeaderAdjustment.stockHANo,//@hsa_no
                stockHeaderAdjustment.stockHACompId,//@hsa_cmp_id
                stockHeaderAdjustment.stockHADate,//@hsa_date
                stockHeaderAdjustment.stockHATtCode,//@hsa_tt_code
                stockHeaderAdjustment.stockHATransNo,//@hsa_transno
                stockHeaderAdjustment.stockHADesc,//@hsa_desc
                stockHeaderAdjustment.stockHAProjName,//@hsa_prj_name
                stockHeaderAdjustment.stockHABraName,//@hsa_bra_name
                stockHeaderAdjustment.stockHAWaName,//@hsa_wa_name
                SettingsModel.currentUser?.userUsername,//@hsa_userstamp
                Timestamp(System.currentTimeMillis()),//@hsa_valuedate
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "updst_hstockadjustment",
            parameters
        )
        return if (queryResult.succeed) {
            DataModel(stockHeaderAdjustment)
        } else {
            DataModel(
                stockHeaderAdjustment,
                false
            )
        }
    }

    private fun deleteByProcedure(stockHeaderAdjustment: StockHeaderAdjustment): DataModel {
        val queryResult = SQLServerWrapper.executeProcedure(
            "delst_hstockadjustment",
            listOf(stockHeaderAdjustment.stockHAId)
        )
        return if (queryResult.succeed) {
            DataModel(stockHeaderAdjustment)
        } else {
            DataModel(
                stockHeaderAdjustment,
                false
            )
        }
    }

}