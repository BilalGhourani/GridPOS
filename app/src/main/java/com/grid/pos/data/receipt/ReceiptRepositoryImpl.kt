package com.grid.pos.data.receipt

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.DataModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.pos.POSUtils
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Extension.getDoubleValue
import com.grid.pos.utils.Extension.getObjectValue
import com.grid.pos.utils.Extension.getStringValue
import kotlinx.coroutines.tasks.await
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.Date

class ReceiptRepositoryImpl(
        private val receiptDao: ReceiptDao
) : ReceiptRepository {
    override suspend fun insert(
            receipt: Receipt
    ): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val docRef = FirebaseFirestore.getInstance().collection("receipt").add(receipt)
                    .await()
                receipt.receiptDocumentId = docRef.id
                return DataModel(receipt)
            }

            CONNECTION_TYPE.LOCAL.key -> {
                receiptDao.insert(receipt)
                return DataModel(receipt)
            }

            else -> {
                val dataModel = insertHReceipt(receipt)
                return if (dataModel.succeed) {
                    getReceiptById(receipt.receiptId)
                } else {
                    dataModel
                }
            }
        }
    }

    override suspend fun delete(
            receipt: Receipt
    ): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                receipt.receiptDocumentId?.let {
                    FirebaseFirestore.getInstance().collection("receipt").document(it).delete()
                        .await()
                    return DataModel(receipt)
                }
                return DataModel(
                    receipt,
                    false
                )
            }

            CONNECTION_TYPE.LOCAL.key -> {
                receiptDao.delete(receipt)
                return DataModel(receipt)
            }

            else -> {
                return deleteHReceipt(receipt)
            }
        }
    }

    override suspend fun update(
            receipt: Receipt
    ): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                receipt.receiptDocumentId?.let {
                    FirebaseFirestore.getInstance().collection("receipt").document(it)
                        .update(receipt.getMap()).await()
                    return DataModel(receipt)
                }
                return DataModel(
                    receipt,
                    false
                )
            }

            CONNECTION_TYPE.LOCAL.key -> {
                receiptDao.update(receipt)
                return DataModel(receipt)
            }

            else -> {
                return updateHReceipt(receipt)
            }
        }
    }

    override suspend fun getReceiptById(
            id: String
    ): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("receipt")
                    .whereEqualTo(
                        "rec_cmp_id",
                        SettingsModel.getCompanyID()
                    ).whereEqualTo(
                        "rec_id",
                        id
                    ).get().await()
                val document = querySnapshot.documents.firstOrNull()
                return DataModel(document?.toObject(Receipt::class.java))
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return DataModel(receiptDao.getReceiptById(id))
            }

            else -> {//CONNECTION_TYPE.SQL_SERVER.key
                var receipt: Receipt? = null
                try {
                    val where = if (SettingsModel.isSqlServerWebDb) "hr_cmp_id='${SettingsModel.getCompanyID()}' AND hr_id = '$id'" else "hr_id = '$id'"
                    val dbResult = SQLServerWrapper.getListOf(
                        "in_hreceipt",
                        "TOP 1",
                        mutableListOf("*"),
                        where,
                        "ORDER BY hr_date DESC",
                        "INNER JOIN in_receipt on hr_id = rec_hr_id INNER JOIN in_unallocatedreceipt on hr_id = ur_hr_id"
                    )
                    if (dbResult.succeed) {
                        (dbResult.result as? ResultSet)?.let {
                            while (it.next()) {
                                receipt = fillParams(it)
                            }
                            SQLServerWrapper.closeResultSet(it)
                        }
                    } else {
                        return DataModel(
                            null,
                            false,
                            dbResult.result as? String
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    return DataModel(
                        null,
                        false,
                        e.message
                    )
                }
                return DataModel(receipt)
            }
        }
    }

    override suspend fun getAllReceipts(): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("receipt")
                    .whereEqualTo(
                        "rec_cmp_id",
                        SettingsModel.getCompanyID()
                    ).get().await()
                val receipts = mutableListOf<Receipt>()
                if (querySnapshot.size() > 0) {
                    for (document in querySnapshot) {
                        val obj = document.toObject(Receipt::class.java)
                        if (obj.receiptId.isNotEmpty()) {
                            obj.receiptDocumentId = document.id
                            receipts.add(obj)
                        }
                    }
                }
                return DataModel(receipts)
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return DataModel(receiptDao.getAllReceipts())
            }

            else -> {//CONNECTION_TYPE.SQL_SERVER.key
                val receipts: MutableList<Receipt> = mutableListOf()
                try {
                    val where = if (SettingsModel.isSqlServerWebDb) "hr_cmp_id='${SettingsModel.getCompanyID()}'" else ""
                    val dbResult = SQLServerWrapper.getListOf(
                        "in_hreceipt",
                        "TOP 100",
                        mutableListOf("*"),
                        where,
                        "ORDER BY hr_date DESC",
                        "INNER JOIN in_receipt on hr_id = rec_hr_id INNER JOIN in_unallocatedreceipt on hr_id = ur_hr_id"
                    )
                    if (dbResult.succeed) {
                        (dbResult.result as? ResultSet)?.let {
                            while (it.next()) {
                                receipts.add(
                                    fillParams(
                                        it
                                    )
                                )
                            }
                            SQLServerWrapper.closeResultSet(it)
                        }
                    } else {
                        return DataModel(
                            null,
                            false,
                            dbResult.result as? String
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    return DataModel(
                        null,
                        false,
                        e.message
                    )
                }
                return DataModel(receipts)
            }
        }
    }

    override suspend fun getLastTransactionNo(): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("receipt")
                    .whereEqualTo(
                        "rec_cmp_id",
                        SettingsModel.getCompanyID()
                    ).whereNotEqualTo(
                        "rec_transno",
                        null
                    ).orderBy(
                        "rec_transno",
                        Query.Direction.DESCENDING
                    ).limit(1).get().await()
                val document = querySnapshot.firstOrNull()
                return DataModel(document?.toObject(Receipt::class.java))
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return DataModel(
                    receiptDao.getLastTransactionByType(
                        SettingsModel.getCompanyID() ?: ""
                    )
                )
            }

            else -> {
                return DataModel(null)/*val receipts: MutableList<Receipt> = mutableListOf()
                try {
                    val where = if (SettingsModel.isSqlServerWebDb) "hr_cmp_id='${SettingsModel.getCompanyID()}'" else ""
                    val dbResult = SQLServerWrapper.getListOf(
                        "in_hreceipt",
                        "TOP 1",
                        mutableListOf("*"),
                        where,
                        "ORDER BY hr_transno DESC",
                        "INNER JOIN in_receipt on hr_id = rec_hr_id INNER JOIN in_unallocatedreceipt on hr_id = ur_hr_id"
                    )
                    dbResult?.let {
                        while (it.next()) {
                            receipts.add(
                                fillParams(
                                    it
                                )
                            )
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return if (receipts.isNotEmpty()) receipts[0] else null*/
            }
        }
    }

    private fun fillParams(
            obj: ResultSet
    ): Receipt {
        return Receipt().apply {
            receiptId = obj.getStringValue("hr_id")
            receiptInId = obj.getStringValue("rec_id")
            unAllocatedReceiptId = obj.getStringValue("ur_id")
            receiptNo = obj.getStringValue("hr_no")
            receiptCompanyId = obj.getStringValue("hr_cmp_id")
            receiptType = obj.getStringValue("")
            receiptTransCode = obj.getStringValue("hr_tt_code")
            receiptTransNo = obj.getStringValue("hr_transno")
            receiptThirdParty = obj.getStringValue("hr_tp_name")
            receiptCurrency = obj.getStringValue("rec_cur_code")
            receiptAmount = obj.getDoubleValue("rec_amt")
            receiptAmountFirst = obj.getDoubleValue("rec_amtf")
            receiptAmountSecond = obj.getDoubleValue("rec_amts")
            receiptDesc = obj.getStringValue("hr_desc")
            receiptNote = obj.getStringValue("hr_note")
            val timeStamp = obj.getObjectValue("hr_timestamp")
            receiptTimeStamp = if (timeStamp is Date) timeStamp else DateHelper.getDateFromString(
                timeStamp as String,
                "yyyy-MM-dd hh:mm:ss.SSS"
            )
            receiptDateTime = receiptTimeStamp!!.time
            receiptUserStamp = obj.getStringValue("hr_userstamp")
        }
    }

    private fun insertHReceipt(receipt: Receipt): DataModel {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                "null_String_output",//@hr_id
                receipt.receiptCompanyId,//@hr_cmp_id
                Timestamp(System.currentTimeMillis()),//@hr_date
                SettingsModel.rvTransactionType,//@hr_tt_code
                null,/*receipt.receiptTransNo*///@hr_transno
                null,//@hr_status
                receipt.receiptDesc,//@hr_desc
                null,//@hr_refno
                receipt.receiptNote,//@hr_note
                receipt.receiptThirdParty,//@hr_tp_name
                null,//@hr_cashname
                SettingsModel.currentUser?.userUsername,//@hr_userstamp
                null,//@hr_sessionpointer
                SettingsModel.currentCompany?.cmp_multibranchcode,//@BranchCode
                Timestamp(System.currentTimeMillis()),//@hr_valuedate
                null,//@hr_employee
                null,//@hr_pathtodoc
            )
        } else {
            listOf(
                receipt.receiptCompanyId,//@hr_cmp_id
                Timestamp(System.currentTimeMillis()),//@hr_date
                SettingsModel.rvTransactionType,//@hr_tt_code
                null,/*receipt.receiptTransNo*///@hr_transno
                null,//@hr_status
                receipt.receiptDesc,//@hr_desc
                null,//@hr_refno
                receipt.receiptNote,//@hr_note
                receipt.receiptThirdParty,//@hr_tp_name
                null,//@hr_cashname
                SettingsModel.currentUser?.userUsername,//@hr_userstamp
                null,//@hr_sessionpointer
                SettingsModel.currentCompany?.cmp_multibranchcode,//@BranchCode
                Timestamp(System.currentTimeMillis()),//@hr_valuedate
                null,//@hr_employee
                null,//@hr_pathtodoc
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "addin_hreceipt",
            parameters
        )
        if (queryResult.succeed) {
            val id = queryResult.result as? String
            if (id.isNullOrEmpty()) {
                try {
                    val dbResult = SQLServerWrapper.getQueryResult("select max(hr_id) as id from in_hreceipt")
                    if (dbResult.succeed) {
                        (dbResult.result as? ResultSet)?.let {
                            while (it.next()) {
                                receipt.receiptId = it.getStringValue(
                                    "id",
                                    receipt.receiptId
                                )
                            }
                            SQLServerWrapper.closeResultSet(it)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                receipt.receiptId = id
            }
            return insertReceipt(receipt)
        } else {
            return DataModel(
                null,
                false,
                queryResult.result as? String
            )
        }
    }

    private fun insertReceipt(receipt: Receipt): DataModel {
        val decimal = SettingsModel.currentCurrency?.currencyName1Dec ?: 3
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                null,//@rec_id
                receipt.receiptId,//@rec_hr_id
                null,//@rec_cha_ch_code
                null,//@rec_cha_code
                receipt.receiptCurrency,//@rec_cur_code
                POSUtils.formatDouble(
                    receipt.receiptAmount,
                    decimal
                ),//@rec_amt
                POSUtils.formatDouble(
                    receipt.receiptAmountFirst,
                    decimal
                ),//@rec_amtf
                POSUtils.formatDouble(
                    receipt.receiptAmountSecond,
                    decimal
                ),//@rec_amts
                null,//@rec_bank
                null,//@rec_chequeno
                Timestamp(System.currentTimeMillis()),//@rec_date
                SettingsModel.defaultSqlServerBranch,//@rec_bra_name
                null,//@rec_prj_name
                null,//@rec_note
                SettingsModel.currentUser?.userUsername,//@rec_userstamp
                null,//@rec_ra_id//TODO
                SettingsModel.currentCompany?.cmp_multibranchcode,//@BranchCode
                null,//@rec_div_name
                false,//@rec_tax
                0.0,//@rec_denomination
                0.0//@rec_count
            )
        } else {
            listOf(
                null,//@rec_id
                receipt.receiptId,//@rec_hr_id
                null,//@rec_cha_ch_code//DSReceiptAcc.Tables("pos_receiptacc").Select("ra_name='" & LBPaymentMode.SelectedItem.ToString & "'").ElementAt(0).Item("ra_chcode"))
                null,//@rec_cha_code
                receipt.receiptCurrency,//@rec_cur_code//DSReceiptAcc.Tables("pos_receiptacc").Select("ra_name='" & LBPaymentMode.SelectedItem.ToString & "'").ElementAt(0).Item("ra_cur_code"))
                POSUtils.formatDouble(
                    receipt.receiptAmount,
                    decimal
                ),//@rec_amt
                POSUtils.formatDouble(
                    receipt.receiptAmountFirst,
                    decimal
                ),//@rec_amtf
                POSUtils.formatDouble(
                    receipt.receiptAmountSecond,
                    decimal
                ),//@rec_amts
                null,//@rec_bank
                null,//@rec_chequeno
                Timestamp(System.currentTimeMillis()),//@rec_date
                SettingsModel.defaultSqlServerBranch,//@rec_bra_name
                null,//@rec_prj_name
                null,//@rec_note
                SettingsModel.currentUser?.userUsername,//@rec_userstamp
                null,//@rec_ra_id//DSReceiptAcc.Tables("pos_receiptacc").Select("ra_name='" & LBPaymentMode.SelectedItem.ToString & "'").ElementAt(0).Item("ra_id"))
                SettingsModel.currentCompany?.cmp_multibranchcode,//@BranchCode
                null,//@rec_div_name
                false,//@rec_tax
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "addin_receipt",
            parameters
        )
        return if (queryResult.succeed) {
            insertUnAllocatedReceipt(receipt)
        } else {
            DataModel(
                null,
                false,
                queryResult.result as? String
            )
        }
    }

    private fun insertUnAllocatedReceipt(receipt: Receipt): DataModel {
        val decimal = SettingsModel.currentCurrency?.currencyName1Dec ?: 3
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                receipt.receiptId,//@ur_hr_id
                receipt.receiptDesc,//@ur_desc
                receipt.receiptCurrency,//@ur_cur_code
                POSUtils.formatDouble(
                    receipt.receiptAmount,
                    decimal
                ),//@ur_amt
                POSUtils.formatDouble(
                    receipt.receiptAmountFirst,
                    decimal
                ),//@ur_amtf
                POSUtils.formatDouble(
                    receipt.receiptAmountSecond,
                    decimal
                ),//@ur_amts
                false,//@ur_allocated
                null,//@ur_note
                SettingsModel.currentUser?.userUsername,//@ur_userstamp
                SettingsModel.currentCompany?.cmp_multibranchcode,//@BranchCode
                SettingsModel.defaultSqlServerBranch,//@ur_bra_name
                null,//@ur_prj_name
                null,//@ur_div_name
            )
        } else {
            listOf(
                null,//@ur_id
                receipt.receiptId,//@ur_hr_id
                receipt.receiptDesc,//@ur_desc
                receipt.receiptCurrency,//@ur_cur_code
                POSUtils.formatDouble(
                    receipt.receiptAmount,
                    decimal
                ),//@ur_amt
                POSUtils.formatDouble(
                    receipt.receiptAmountFirst,
                    decimal
                ),//@ur_amtf
                POSUtils.formatDouble(
                    receipt.receiptAmountSecond,
                    decimal
                ),//@ur_amts
                false,//@ur_allocated
                null,//@ur_note
                SettingsModel.currentUser?.userUsername,//@ur_userstamp
                SettingsModel.currentCompany?.cmp_multibranchcode,//@BranchCode
                SettingsModel.defaultSqlServerBranch,//@ur_bra_name
                null,//@ur_prj_name
                null,//@ur_div_name
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "addin_unallocatedreceipt",
            parameters
        )
        return DataModel(
            receipt,
            queryResult.succeed,
            queryResult.result as? String
        )
    }

    private fun updateHReceipt(receipt: Receipt): DataModel {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                receipt.receiptId,//@hr_id
                receipt.receiptNo,//@hr_no
                receipt.receiptCompanyId,//@hr_cmp_id
                Timestamp(System.currentTimeMillis()),//@hr_date
                SettingsModel.rvTransactionType,//@hr_tt_code
                receipt.receiptTransNo,//@hr_transno
                null,//@hr_status
                receipt.receiptDesc,//@hr_desc
                null,//@hr_refno
                receipt.receiptNote,//@hr_note
                receipt.receiptThirdParty,//@hr_tp_name
                null,//@hr_cashname
                SettingsModel.currentUser?.userUsername,//@hr_userstamp
                Timestamp(System.currentTimeMillis()),//@hr_valuedate
                null,//@hr_employee
                null,//@hr_pathtodoc
            )
        } else {
            listOf(
                receipt.receiptId,//@hr_id
                receipt.receiptNo,//@hr_no
                receipt.receiptCompanyId,//@hr_cmp_id
                Timestamp(System.currentTimeMillis()),//@hr_date
                SettingsModel.rvTransactionType,//@hr_tt_code
                receipt.receiptTransNo,//@hr_transno
                null,//@hr_status
                receipt.receiptDesc,//@hr_desc
                null,//@hr_refno
                receipt.receiptNote,//@hr_note
                receipt.receiptThirdParty,//@hr_tp_name
                null,//@hr_cashname
                SettingsModel.currentUser?.userUsername,//@hr_userstamp
                null,//@hr_sessionpointer
                SettingsModel.currentCompany?.cmp_multibranchcode,//@BranchCode
                Timestamp(System.currentTimeMillis()),//@hr_valuedate
                null,//@hr_employee
                null,//@hr_pathtodoc
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "updin_hreceipt",
            parameters
        )
        return if (queryResult.succeed) {
            updateReceipt(receipt)
        } else {
            DataModel(
                receipt,
                false,
                queryResult.result as? String
            )
        }
    }

    private fun updateReceipt(receipt: Receipt): DataModel {
        val decimal = SettingsModel.currentCurrency?.currencyName1Dec ?: 3
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                receipt.receiptInId,//@rec_id
                receipt.receiptId,//@rec_hr_id
                null,//@rec_cha_ch_code
                null,//@rec_cha_code
                receipt.receiptCurrency,//@rec_cur_code
                POSUtils.formatDouble(
                    receipt.receiptAmount,
                    decimal
                ),//@rec_amt
                POSUtils.formatDouble(
                    receipt.receiptAmountFirst,
                    decimal
                ),//@rec_amtf
                POSUtils.formatDouble(
                    receipt.receiptAmountSecond,
                    decimal
                ),//@rec_amts
                null,//@rec_bank
                null,//@rec_chequeno
                Timestamp(System.currentTimeMillis()),//@rec_date
                SettingsModel.defaultSqlServerBranch,//@rec_bra_name
                null,//@rec_prj_name
                null,//@rec_note
                SettingsModel.currentUser?.userUsername,//@rec_userstamp
                null,//@rec_ra_id//TODO
                null,//@rec_div_name
                false,//@rec_tax
                null,//@rec_denomination
                null//@rec_count
            )
        } else {
            listOf(
                receipt.receiptInId,//@rec_id
                receipt.receiptId,//@rec_hr_id
                null,//@rec_cha_ch_code//DSReceiptAcc.Tables("pos_receiptacc").Select("ra_name='" & LBPaymentMode.SelectedItem.ToString & "'").ElementAt(0).Item("ra_chcode"))
                null,//@rec_cha_code
                receipt.receiptCurrency,//@rec_cur_code//DSReceiptAcc.Tables("pos_receiptacc").Select("ra_name='" & LBPaymentMode.SelectedItem.ToString & "'").ElementAt(0).Item("ra_cur_code"))
                POSUtils.formatDouble(
                    receipt.receiptAmount,
                    decimal
                ),//@rec_amt
                POSUtils.formatDouble(
                    receipt.receiptAmountFirst,
                    decimal
                ),//@rec_amtf
                POSUtils.formatDouble(
                    receipt.receiptAmountSecond,
                    decimal
                ),//@rec_amts
                null,//@rec_bank
                null,//@rec_chequeno
                Timestamp(System.currentTimeMillis()),//@rec_date
                SettingsModel.defaultSqlServerBranch,//@rec_bra_name
                null,//@rec_prj_name
                null,//@rec_note
                SettingsModel.currentUser?.userUsername,//@rec_userstamp
                null,//@rec_ra_id//DSReceiptAcc.Tables("pos_receiptacc").Select("ra_name='" & LBPaymentMode.SelectedItem.ToString & "'").ElementAt(0).Item("ra_id"))
                null,//@rec_div_name
                false,//@rec_tax
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "updin_receipt",
            parameters
        )
        return if (queryResult.succeed) {
            updateUnAllocatedReceipt(receipt)
        } else {
            DataModel(
                receipt,
                false,
                queryResult.result as? String
            )
        }
    }

    private fun updateUnAllocatedReceipt(receipt: Receipt): DataModel {
        val decimal = SettingsModel.currentCurrency?.currencyName1Dec ?: 3
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                receipt.unAllocatedReceiptId,//@ur_id
                receipt.receiptId,//@ur_hr_id
                receipt.receiptDesc,//@ur_desc
                receipt.receiptCurrency,//@ur_cur_code
                POSUtils.formatDouble(
                    receipt.receiptAmount,
                    decimal
                ),//@ur_amt
                POSUtils.formatDouble(
                    receipt.receiptAmountFirst,
                    decimal
                ),//@ur_amtf
                POSUtils.formatDouble(
                    receipt.receiptAmountSecond,
                    decimal
                ),//@ur_amts
                false,//@ur_allocated
                null,//@ur_note
                SettingsModel.currentUser?.userUsername,//@ur_userstamp
                SettingsModel.defaultSqlServerBranch,//@ur_bra_name
                null,//@ur_prj_name
                null,//@ur_div_name
            )
        } else {
            listOf(
                receipt.unAllocatedReceiptId,//@ur_id
                receipt.receiptId,//@ur_hr_id
                receipt.receiptDesc,//@ur_desc
                receipt.receiptCurrency,//@ur_cur_code
                POSUtils.formatDouble(
                    receipt.receiptAmount,
                    decimal
                ),//@ur_amt
                POSUtils.formatDouble(
                    receipt.receiptAmountFirst,
                    decimal
                ),//@ur_amtf
                POSUtils.formatDouble(
                    receipt.receiptAmountSecond,
                    decimal
                ),//@ur_amts
                false,//@ur_allocated
                null,//@ur_note
                SettingsModel.currentUser?.userUsername,//@ur_userstamp
                SettingsModel.defaultSqlServerBranch,//@ur_bra_name
                null,//@ur_prj_name
                null,//@ur_div_name
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "addin_unallocatedreceipt",
            parameters
        )
        return DataModel(
            receipt,
            queryResult.succeed,
            queryResult.result as? String
        )
    }

    private fun deleteHReceipt(receipt: Receipt): DataModel {
        val queryResult = SQLServerWrapper.executeProcedure(
            "delin_hreceipt",
            listOf(receipt.receiptId)
        )
        return if (queryResult.succeed) {
            deleteReceipt(receipt)
        } else {
            DataModel(
                receipt,
                false,
                queryResult.result as? String
            )
        }
    }

    private fun deleteReceipt(receipt: Receipt): DataModel {
        val queryResult = SQLServerWrapper.executeProcedure(
            "delin_receipt",
            listOf(receipt.receiptInId)
        )
        return if (queryResult.succeed) {
            deleteUnAllocatedReceipt(receipt)
        } else {
            DataModel(
                receipt,
                false,
                queryResult.result as? String
            )
        }

    }

    private fun deleteUnAllocatedReceipt(receipt: Receipt): DataModel {
        val queryResult = SQLServerWrapper.executeProcedure(
            "delin_unallocatedreceipt",
            listOf(receipt.unAllocatedReceiptId)
        )
        return DataModel(
            receipt,
            queryResult.succeed,
            queryResult.result as? String
        )
    }
}