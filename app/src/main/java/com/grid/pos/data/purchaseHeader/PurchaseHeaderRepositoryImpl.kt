package com.grid.pos.data.purchaseHeader

import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.DataModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Extension.getBooleanValue
import com.grid.pos.utils.Extension.getDoubleValue
import com.grid.pos.utils.Extension.getObjectValue
import com.grid.pos.utils.Extension.getStringValue
import java.math.BigInteger
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.Date
import java.util.Random

class PurchaseHeaderRepositoryImpl : PurchaseHeaderRepository {
    override suspend fun insert(purchaseHeader: PurchaseHeader): DataModel {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                DataModel(purchaseHeader)
            }

            else -> {
                val dataModel = insertByProcedure(purchaseHeader)
                if (dataModel.succeed && purchaseHeader.purchaseHeaderId.isNotEmpty()) {
                    dataModel.data =
                        getPurchaseHeaderById(purchaseHeader.purchaseHeaderId) ?: dataModel.data
                }
                dataModel
            }
        }
    }

    override suspend fun delete(purchaseHeader: PurchaseHeader): DataModel {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                DataModel(purchaseHeader)
            }

            else -> {
                deleteByProcedure(purchaseHeader)
            }
        }
    }

    override suspend fun update(purchaseHeader: PurchaseHeader): DataModel {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                DataModel(purchaseHeader)
            }

            else -> {
                updateByProcedure(purchaseHeader)
            }
        }
    }

    override suspend fun getAllPurchaseHeaders(): MutableList<PurchaseHeader> {
        val limit = 100
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                return mutableListOf()
            }

            else -> {
                val purchaseHeaders: MutableList<PurchaseHeader> = mutableListOf()
                try {
                    val where = "hp_cmp_id='${SettingsModel.getCompanyID()}'"
                    val dbResult = SQLServerWrapper.getListOf(
                        "in_hpurchase",
                        "TOP $limit",
                        if (SettingsModel.isSqlServerWebDb) mutableListOf("*,tt.tt_newcode") else mutableListOf(
                            "*"
                        ),
                        where,
                        "ORDER BY hp_date DESC",
                        if (SettingsModel.isSqlServerWebDb) "INNER JOIN acc_transactiontype tt on hp_tt_code = tt.tt_code" else ""
                    )
                    dbResult?.let {
                        while (it.next()) {
                            purchaseHeaders.add(
                                fillParams(it)
                            )
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return purchaseHeaders
            }
        }
    }

    override suspend fun getPurchaseHeaderById(id: String): PurchaseHeader? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key,
            CONNECTION_TYPE.LOCAL.key -> {
                return PurchaseHeader()
            }

            else -> {
                var purchaseHeader: PurchaseHeader? = null
                try {
                    val where = "hp_id='$id'"
                    val dbResult = SQLServerWrapper.getListOf(
                        "in_hpurchase",
                        "TOP 1",
                        if (SettingsModel.isSqlServerWebDb) mutableListOf("*,tt.tt_newcode") else mutableListOf(
                            "*"
                        ),
                        where,
                        "",
                        if (SettingsModel.isSqlServerWebDb) "INNER JOIN acc_transactiontype tt on hp_tt_code = tt.tt_code" else ""
                    )
                    dbResult?.let {
                        while (it.next()) {
                            purchaseHeader = fillParams(it)
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return purchaseHeader
            }
        }
    }


    private fun fillParams(
        obj: ResultSet
    ): PurchaseHeader {
        return PurchaseHeader().apply {
            purchaseHeaderId = obj.getStringValue("hp_id")
            purchaseHeaderCmpId = obj.getStringValue("hp_cmp_id")
            purchaseHeaderCategory = obj.getStringValue("hp_category").ifEmpty { null }
            purchaseHeaderDate = obj.getStringValue("hp_date")
            purchaseHeaderNo = obj.getStringValue("hp_no")
            purchaseHeaderOrderNo = obj.getStringValue("hp_orderno")
            purchaseHeaderTtCode = obj.getStringValue("hp_tt_code")
            purchaseHeaderTtCodeName =
                obj.getStringValue("tt_newcode", obj.getStringValue("hio_tt_code"))
            purchaseHeaderTransNo = obj.getStringValue("hp_transno").ifEmpty { null }
            purchaseHeaderStatus = obj.getStringValue("hp_status").ifEmpty { null }
            purchaseHeaderPlnName = obj.getStringValue("hp_pln_name").ifEmpty { null }
            purchaseHeaderCurCode = obj.getStringValue("hp_cur_code").ifEmpty { null }
            purchaseHeaderDisc = obj.getDoubleValue("hp_disc")
            purchaseHeaderDiscAmt = obj.getDoubleValue("hp_discamt")
            purchaseHeaderWaName = obj.getStringValue("hp_wa_name").ifEmpty { null }
            purchaseHeaderBraName = obj.getStringValue("hp_bra_name").ifEmpty { null }
            purchaseHeaderPrjName = obj.getStringValue("hp_prj_name").ifEmpty { null }
            purchaseHeaderNote = obj.getStringValue("hp_note").ifEmpty { null }
            purchaseHeaderTpName = obj.getStringValue("hp_tp_name").ifEmpty { null }
            purchaseHeaderCashName = obj.getStringValue("hp_cashname").ifEmpty { null }
            purchaseHeaderNotPaid = obj.getBooleanValue("hp_notpaid")
            purchaseHeaderPhoneOrder = obj.getBooleanValue("hp_phoneorder")
            purchaseHeaderNetAmt = obj.getDoubleValue("hp_netamt")
            purchaseHeaderVatAmt = obj.getDoubleValue("hp_vatamt")
            purchaseHeaderTotal1 = obj.getDoubleValue("hp_total1")
            purchaseHeaderRateF = obj.getDoubleValue("hp_ratef")
            purchaseHeaderRateS = obj.getDoubleValue("hp_rates")
            purchaseHeaderRateTax = obj.getDoubleValue("hp_ratetax")
            purchaseHeaderEmployee = obj.getStringValue("hp_employee").ifEmpty { null }
            purchaseHeaderDelivered = obj.getBooleanValue("hp_delivered")

            val timeStamp = obj.getObjectValue("hp_timestamp")
            purchaseHeaderTimestamp =
                if (timeStamp is Date) timeStamp else DateHelper.getDateFromString(
                    timeStamp as String,
                    "yyyy-MM-dd hh:mm:ss.SSS"
                )
            purchaseHeaderUserStamp = obj.getStringValue("hp_userstamp")
            purchaseHeaderHsId = obj.getStringValue("hp_hsid").ifEmpty { null }
            val valueDate = obj.getObjectValue("hp_valuedate")
            purchaseHeaderValueDate =
                when (valueDate) {
                    is Date -> valueDate
                    is String -> {
                        DateHelper.getDateFromString(valueDate, "yyyy-MM-dd hh:mm:ss.SSS")
                    }

                    else -> null
                }

            purchaseHeaderHjNo = obj.getStringValue("hp_hj_no").ifEmpty { null }
            purchaseHeaderPathToDoc = obj.getStringValue("hp_pathtodoc").ifEmpty { null }
            val dueDate = obj.getObjectValue("hp_duedate")
            purchaseHeaderDueDate =
                when (dueDate) {
                    is Date -> dueDate
                    is String -> {
                        DateHelper.getDateFromString(dueDate, "yyyy-MM-dd hh:mm:ss.SSS")
                    }

                    else -> null
                }

            purchaseHeaderTotal = obj.getDoubleValue("hp_total")
            purchaseHeaderRateTaxF = obj.getDoubleValue("hp_ratetaxf")
            purchaseHeaderRateTaxS = obj.getDoubleValue("hp_ratetaxs")
            purchaseHeaderTaxAmt = obj.getDoubleValue("hp_taxamt")
            purchaseHeaderTax1Amt = obj.getDoubleValue("hp_tax1amt")
            purchaseHeaderTax2Amt = obj.getDoubleValue("hp_tax2amt")
        }
    }

    private fun insertByProcedure(purchaseHeader: PurchaseHeader): DataModel {
        val sessionPointer = (BigInteger(
            24,
            Random()
        )).toString()
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                purchaseHeader.purchaseHeaderCmpId,//@hp_cmp_id
                purchaseHeader.purchaseHeaderCategory,//@hp_category
                getDateInTimestamp(purchaseHeader.purchaseHeaderDate),//@hp_date
                purchaseHeader.purchaseHeaderOrderNo,//@hp_orderno
                purchaseHeader.purchaseHeaderTtCode,//@hp_tt_code
                purchaseHeader.purchaseHeaderTransNo,//@hp_transno
                purchaseHeader.purchaseHeaderStatus,//@hp_status
                purchaseHeader.purchaseHeaderPlnName,//@hp_pln_name
                purchaseHeader.purchaseHeaderCurCode,//@hp_cur_code
                purchaseHeader.purchaseHeaderDisc,//@hp_disc
                purchaseHeader.purchaseHeaderDiscAmt,//@hp_discamt
                purchaseHeader.purchaseHeaderWaName,//@hp_wa_name
                SettingsModel.defaultSqlServerBranch,//@hp_bra_name
                purchaseHeader.purchaseHeaderPrjName,//@hp_prj_name
                purchaseHeader.purchaseHeaderNote,//@hp_note
                purchaseHeader.purchaseHeaderTpName,//@hp_tp_name
                purchaseHeader.purchaseHeaderCashName,//@hp_cashname
                purchaseHeader.purchaseHeaderNotPaid,//@hp_notpaid
                purchaseHeader.purchaseHeaderPhoneOrder,//@hp_phoneorder
                purchaseHeader.purchaseHeaderNetAmt,//@hp_netamt
                purchaseHeader.purchaseHeaderVatAmt,//@hp_vatamt
                purchaseHeader.purchaseHeaderTotal1,//@hp_total1
                purchaseHeader.purchaseHeaderRateF,//@hp_ratef
                purchaseHeader.purchaseHeaderRateS,//@hp_rates
                purchaseHeader.purchaseHeaderRateTax,//@hp_ratetax
                purchaseHeader.purchaseHeaderEmployee,//@hp_employee
                purchaseHeader.purchaseHeaderDelivered,//@hp_delivered
                SettingsModel.currentUser?.userUsername,//@hp_userstamp
                null,//@hp_sessionpointer
                SettingsModel.currentCompany?.cmp_multibranchcode,//@branchcode
                getValueDateInTimestamp(purchaseHeader.purchaseHeaderValueDate),//@hp_valuedate
                purchaseHeader.purchaseHeaderPathToDoc,//@hp_pathtodoc
                getValueDateInTimestamp(purchaseHeader.purchaseHeaderDueDate),//@hp_duedate
                purchaseHeader.purchaseHeaderTotal,//@hp_total
                purchaseHeader.purchaseHeaderRateTaxF,//@hp_ratetaxf
                purchaseHeader.purchaseHeaderRateTaxS,//@hp_ratetaxs
                purchaseHeader.purchaseHeaderTaxAmt,//@hp_taxamt
                purchaseHeader.purchaseHeaderTax1Amt,//@hp_tax1amt
                purchaseHeader.purchaseHeaderTax2Amt,//@hp_tax2amt
                "null_string_output"//hp_id
            )
        } else {
            listOf(
                purchaseHeader.purchaseHeaderCmpId,//@hp_cmp_id
                purchaseHeader.purchaseHeaderCategory,//@hp_category
                getDateInTimestamp(purchaseHeader.purchaseHeaderDate),//@hp_date
                purchaseHeader.purchaseHeaderOrderNo,//@hp_orderno
                purchaseHeader.purchaseHeaderTtCode,//@hp_tt_code
                purchaseHeader.purchaseHeaderTransNo,//@hp_transno
                purchaseHeader.purchaseHeaderStatus,//@hp_status
                purchaseHeader.purchaseHeaderPlnName,//@hp_pln_name
                purchaseHeader.purchaseHeaderCurCode,//@hp_cur_code
                purchaseHeader.purchaseHeaderDisc,//@hp_disc
                purchaseHeader.purchaseHeaderDiscAmt,//@hp_discamt
                purchaseHeader.purchaseHeaderWaName,//@hp_wa_name
                SettingsModel.defaultSqlServerBranch,//@hp_bra_name
                purchaseHeader.purchaseHeaderPrjName,//@hp_prj_name
                purchaseHeader.purchaseHeaderNote,//@hp_note
                purchaseHeader.purchaseHeaderTpName,//@hp_tp_name
                purchaseHeader.purchaseHeaderCashName,//@hp_cashname
                purchaseHeader.purchaseHeaderNotPaid,//@hp_notpaid
                purchaseHeader.purchaseHeaderPhoneOrder,//@hp_phoneorder
                purchaseHeader.purchaseHeaderNetAmt,//@hp_netamt
                purchaseHeader.purchaseHeaderVatAmt,//@hp_vatamt
                purchaseHeader.purchaseHeaderTotal1,//@hp_total1
                purchaseHeader.purchaseHeaderRateF,//@hp_ratef
                purchaseHeader.purchaseHeaderRateS,//@hp_rates
                purchaseHeader.purchaseHeaderRateTax,//@hp_ratetax
                purchaseHeader.purchaseHeaderEmployee,//@hp_employee
                purchaseHeader.purchaseHeaderDelivered,//@hp_delivered
                SettingsModel.currentUser?.userUsername,//@hp_userstamp
                sessionPointer,//@hp_sessionpointer
                SettingsModel.currentCompany?.cmp_multibranchcode,//@branchcode
                getValueDateInTimestamp(purchaseHeader.purchaseHeaderValueDate),//@hp_valuedate
                purchaseHeader.purchaseHeaderPathToDoc,//@hp_pathtodoc
                getValueDateInTimestamp(purchaseHeader.purchaseHeaderDueDate),//@hp_duedate
                purchaseHeader.purchaseHeaderRateTaxF,//@hp_ratetaxf
                purchaseHeader.purchaseHeaderRateTaxS,//@hp_ratetaxs
                purchaseHeader.purchaseHeaderTaxAmt,//@hp_taxamt
                purchaseHeader.purchaseHeaderTax1Amt,//@hp_tax1amt
                purchaseHeader.purchaseHeaderTax2Amt,//@hp_tax2amt
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "addin_hpurchase",
            parameters
        )
        return if (queryResult.succeed) {
            val id = queryResult.result ?: ""
            if (id.isNotEmpty()) {
                purchaseHeader.purchaseHeaderId = id
            } else {
                try {
                    val dbResult =
                        SQLServerWrapper.getQueryResult("select max(hp_id) as id from in_hpurchase where hp_sessionpointer = '$sessionPointer'")
                    dbResult?.let {
                        if (it.next()) {
                            purchaseHeader.purchaseHeaderId = it.getStringValue(
                                "id",
                                purchaseHeader.purchaseHeaderId
                            )
                        }
                        SQLServerWrapper.closeResultSet(it)
                        SQLServerWrapper.update(
                            "in_hpurchase",
                            listOf("hp_sessionpointer"),
                            listOf(null),
                            "hp_sessionpointer = '$sessionPointer'"
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            DataModel(purchaseHeader)
        } else {
            DataModel(
                purchaseHeader,
                false
            )
        }
    }

    private fun updateByProcedure(purchaseHeader: PurchaseHeader): DataModel {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                purchaseHeader.purchaseHeaderId,//@hp_id
                purchaseHeader.purchaseHeaderNo,//@@hp_no
                purchaseHeader.purchaseHeaderCmpId,//@hp_cmp_id
                purchaseHeader.purchaseHeaderCategory,//@hp_category
                getDateInTimestamp(purchaseHeader.purchaseHeaderDate),//@hp_date
                purchaseHeader.purchaseHeaderOrderNo,//@hp_orderno
                purchaseHeader.purchaseHeaderTtCode,//@hp_tt_code
                purchaseHeader.purchaseHeaderTransNo,//@hp_transno
                purchaseHeader.purchaseHeaderStatus,//@hp_status
                purchaseHeader.purchaseHeaderPlnName,//@hp_pln_name
                purchaseHeader.purchaseHeaderCurCode,//@hp_cur_code
                purchaseHeader.purchaseHeaderDisc,//@hp_disc
                purchaseHeader.purchaseHeaderDiscAmt,//@hp_discamt
                purchaseHeader.purchaseHeaderWaName,//@hp_wa_name
                SettingsModel.defaultSqlServerBranch,//@hp_bra_name
                purchaseHeader.purchaseHeaderPrjName,//@hp_prj_name
                purchaseHeader.purchaseHeaderNote,//@hp_note
                purchaseHeader.purchaseHeaderTpName,//@hp_tp_name
                purchaseHeader.purchaseHeaderCashName,//@hp_cashname
                purchaseHeader.purchaseHeaderNotPaid,//@hp_notpaid
                purchaseHeader.purchaseHeaderPhoneOrder,//@hp_phoneorder
                purchaseHeader.purchaseHeaderNetAmt,//@hp_netamt
                purchaseHeader.purchaseHeaderVatAmt,//@hp_vatamt
                purchaseHeader.purchaseHeaderTotal1,//@hp_total1
                purchaseHeader.purchaseHeaderRateF,//@hp_ratef
                purchaseHeader.purchaseHeaderRateS,//@hp_rates
                purchaseHeader.purchaseHeaderRateTax,//@hp_ratetax
                purchaseHeader.purchaseHeaderEmployee,//@hp_employee
                purchaseHeader.purchaseHeaderDelivered,//@hp_delivered
                SettingsModel.currentUser?.userUsername,//@hp_userstamp
                getValueDateInTimestamp(purchaseHeader.purchaseHeaderValueDate),//@hp_valuedate
                purchaseHeader.purchaseHeaderPathToDoc,//@hp_pathtodoc
                getValueDateInTimestamp(purchaseHeader.purchaseHeaderDueDate),//@hp_duedate
                purchaseHeader.purchaseHeaderTotal,//@hp_total
                purchaseHeader.purchaseHeaderRateTaxF,//@hp_ratetaxf
                purchaseHeader.purchaseHeaderRateTaxS,//@hp_ratetaxs
                purchaseHeader.purchaseHeaderTaxAmt,//@hp_taxamt
                purchaseHeader.purchaseHeaderTax1Amt,//@hp_tax1amt
                purchaseHeader.purchaseHeaderTax2Amt,//@hp_tax2amt
            )
        } else {
            listOf(
                purchaseHeader.purchaseHeaderId,//@hp_id
                purchaseHeader.purchaseHeaderNo,//@@hp_no
                purchaseHeader.purchaseHeaderCmpId,//@hp_cmp_id
                purchaseHeader.purchaseHeaderCategory,//@hp_category
                getDateInTimestamp(purchaseHeader.purchaseHeaderDate),//@hp_date
                purchaseHeader.purchaseHeaderOrderNo,//@hp_orderno
                purchaseHeader.purchaseHeaderTtCode,//@hp_tt_code
                purchaseHeader.purchaseHeaderTransNo,//@hp_transno
                purchaseHeader.purchaseHeaderStatus,//@hp_status
                purchaseHeader.purchaseHeaderPlnName,//@hp_pln_name
                purchaseHeader.purchaseHeaderCurCode,//@hp_cur_code
                purchaseHeader.purchaseHeaderDisc,//@hp_disc
                purchaseHeader.purchaseHeaderDiscAmt,//@hp_discamt
                purchaseHeader.purchaseHeaderWaName,//@hp_wa_name
                SettingsModel.defaultSqlServerBranch,//@hp_bra_name
                purchaseHeader.purchaseHeaderPrjName,//@hp_prj_name
                purchaseHeader.purchaseHeaderNote,//@hp_note
                purchaseHeader.purchaseHeaderTpName,//@hp_tp_name
                purchaseHeader.purchaseHeaderCashName,//@hp_cashname
                purchaseHeader.purchaseHeaderNotPaid,//@hp_notpaid
                purchaseHeader.purchaseHeaderPhoneOrder,//@hp_phoneorder
                purchaseHeader.purchaseHeaderNetAmt,//@hp_netamt
                purchaseHeader.purchaseHeaderVatAmt,//@hp_vatamt
                purchaseHeader.purchaseHeaderTotal1,//@hp_total1
                purchaseHeader.purchaseHeaderRateF,//@hp_ratef
                purchaseHeader.purchaseHeaderRateS,//@hp_rates
                purchaseHeader.purchaseHeaderRateTax,//@hp_ratetax
                purchaseHeader.purchaseHeaderEmployee,//@hp_employee
                purchaseHeader.purchaseHeaderDelivered,//@hp_delivered
                SettingsModel.currentUser?.userUsername,//@hp_userstamp
                getValueDateInTimestamp(purchaseHeader.purchaseHeaderValueDate),//@hp_valuedate
                purchaseHeader.purchaseHeaderPathToDoc,//@hp_pathtodoc
                getValueDateInTimestamp(purchaseHeader.purchaseHeaderDueDate),//@hp_duedate
                purchaseHeader.purchaseHeaderRateTaxF,//@hp_ratetaxf
                purchaseHeader.purchaseHeaderRateTaxS,//@hp_ratetaxs
                purchaseHeader.purchaseHeaderTaxAmt,//@hp_taxamt
                purchaseHeader.purchaseHeaderTax1Amt,//@hp_tax1amt
                purchaseHeader.purchaseHeaderTax2Amt,//@hp_tax2amt
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "updin_hpurchase",
            parameters
        )
        return if (queryResult.succeed) {
            DataModel(purchaseHeader)
        } else {
            DataModel(
                purchaseHeader,
                false
            )
        }
    }

    private fun deleteByProcedure(purchaseHeader: PurchaseHeader): DataModel {
        val queryResult = SQLServerWrapper.executeProcedure(
            "delin_hpurchase",
            listOf(purchaseHeader.purchaseHeaderId)
        )
        return if (queryResult.succeed) {
            DataModel(purchaseHeader)
        } else {
            DataModel(
                purchaseHeader,
                false
            )
        }
    }

    private fun getDateInTimestamp(dateStr: String?): Timestamp {
        if (dateStr != null) {
            val date = DateHelper.getDateFromString(dateStr, "yyyy-MM-dd HH:mm:ss.SSS")
            return Timestamp(date.time)
        }
        return Timestamp(System.currentTimeMillis())
    }

    private fun getValueDateInTimestamp(valueDate: Date?): Timestamp {
        if (valueDate != null) {
            return Timestamp(valueDate.time)
        }
        return Timestamp(System.currentTimeMillis())
    }

}