package com.grid.pos.data.Receipt

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.pos.POSUtils
import com.grid.pos.utils.Extension.getStringValue
import kotlinx.coroutines.tasks.await
import java.sql.Timestamp

class ReceiptRepositoryImpl(
        private val receiptDao: ReceiptDao
) : ReceiptRepository {
    override suspend fun insert(
            receipt: Receipt
    ): Receipt {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val docRef = FirebaseFirestore.getInstance().collection("receipt").add(receipt)
                    .await()
                receipt.receiptDocumentId = docRef.id
            }

            CONNECTION_TYPE.LOCAL.key -> {
                receiptDao.insert(receipt)
            }

            else -> {
                insertHReceipt(receipt)
            }
        }
        return receipt
    }

    override suspend fun delete(
            receipt: Receipt
    ) {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                receipt.receiptDocumentId?.let {
                    FirebaseFirestore.getInstance().collection("receipt").document(it).delete()
                        .await()
                }
            }

            CONNECTION_TYPE.LOCAL.key -> {
                receiptDao.delete(receipt)
            }

            else -> {
                deleteHReceipt(receipt)
            }
        }
    }

    override suspend fun update(
            receipt: Receipt
    ) {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                receipt.receiptDocumentId?.let {
                    FirebaseFirestore.getInstance().collection("receipt").document(it)
                        .update(receipt.getMap()).await()
                }
            }

            CONNECTION_TYPE.LOCAL.key -> {
                receiptDao.update(receipt)
            }

            else -> {
                updateHReceipt(receipt)
            }
        }
    }

    override suspend fun getReceiptById(
            id: String
    ): Receipt? {
        return when (SettingsModel.connectionType) {
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
                document?.toObject(Receipt::class.java)
            }

            CONNECTION_TYPE.LOCAL.key -> {
                receiptDao.getReceiptById(id)
            }

            else -> {//CONNECTION_TYPE.SQL_SERVER.key
                null
            }
        }
    }

    override suspend fun getAllReceipts(): MutableList<Receipt> {
        return when (SettingsModel.connectionType) {
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
                receipts
            }

            CONNECTION_TYPE.LOCAL.key -> {
                receiptDao.getAllReceipts()
            }

            else -> {//CONNECTION_TYPE.SQL_SERVER.key
                mutableListOf()
            }
        }
    }

    override suspend fun getLastTransactionNo(): Receipt? {
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
                return document?.toObject(Receipt::class.java)
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return receiptDao.getLastTransactionByType(
                    SettingsModel.getCompanyID() ?: ""
                )
            }

            else -> {
                return null
            }
        }
    }

    private fun insertHReceipt(receipt: Receipt) {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                "null_String_output",//@hr_id
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
        } else {
            listOf(
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
        val id = SQLServerWrapper.executeProcedure(
            "addin_hreceipt",
            parameters
        )
        if (id.isNullOrEmpty()) {
            try {
                val dbResult = SQLServerWrapper.getQueryResult("select max(hr_id) as id from in_hreceipt")
                dbResult?.let {
                    while (it.next()) {
                        receipt.receiptId = it.getStringValue(
                            "id",
                            receipt.receiptId
                        )
                    }
                    SQLServerWrapper.closeResultSet(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            receipt.receiptId = id
        }
        insertReceipt(receipt)
    }

    private fun insertReceipt(receipt: Receipt) {
        val decimal = SettingsModel.currentCurrency?.currencyName1Dec ?: 3
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                null,//@rec_id
                receipt.receiptId,//@rec_hr_id
                null,//@rec_cha_ch_code
                null,//@rec_cha_code
                null,//@rec_cur_code
                POSUtils.formatDouble(
                    receipt.receiptAmount,
                    decimal
                ),//@rec_amt
                POSUtils.formatDouble(
                    receipt.receiptAmount,
                    decimal
                ),//@rec_amtf
                POSUtils.formatDouble(
                    receipt.receiptAmount,
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
                null,//@rec_cur_code//DSReceiptAcc.Tables("pos_receiptacc").Select("ra_name='" & LBPaymentMode.SelectedItem.ToString & "'").ElementAt(0).Item("ra_cur_code"))
                POSUtils.formatDouble(
                    receipt.receiptAmount,
                    decimal
                ),//@rec_amt
                POSUtils.formatDouble(
                    receipt.receiptAmount,
                    decimal
                ),//@rec_amtf
                POSUtils.formatDouble(
                    receipt.receiptAmount,
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
        SQLServerWrapper.executeProcedure(
            "addin_receipt",
            parameters
        )
        insertUnAllocatedReceipt(receipt)
    }

    private fun insertUnAllocatedReceipt(receipt: Receipt) {
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
        SQLServerWrapper.executeProcedure(
            "addin_unallocatedreceipt",
            parameters
        )
    }

    private fun updateHReceipt(receipt: Receipt) {
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
        SQLServerWrapper.executeProcedure(
            "updin_hreceipt",
            parameters
        )
        updateReceipt(receipt)
    }

    private fun updateReceipt(receipt: Receipt) {
        val decimal = SettingsModel.currentCurrency?.currencyName1Dec ?: 3
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                receipt.receiptInId,//@rec_id
                receipt.receiptId,//@rec_hr_id
                null,//@rec_cha_ch_code
                null,//@rec_cha_code
                null,//@rec_cur_code
                POSUtils.formatDouble(
                    receipt.receiptAmount,
                    decimal
                ),//@rec_amt
                POSUtils.formatDouble(
                    receipt.receiptAmount,
                    decimal
                ),//@rec_amtf
                POSUtils.formatDouble(
                    receipt.receiptAmount,
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
                null,//@rec_cur_code//DSReceiptAcc.Tables("pos_receiptacc").Select("ra_name='" & LBPaymentMode.SelectedItem.ToString & "'").ElementAt(0).Item("ra_cur_code"))
                POSUtils.formatDouble(
                    receipt.receiptAmount,
                    decimal
                ),//@rec_amt
                POSUtils.formatDouble(
                    receipt.receiptAmount,
                    decimal
                ),//@rec_amtf
                POSUtils.formatDouble(
                    receipt.receiptAmount,
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
        SQLServerWrapper.executeProcedure(
            "updin_receipt",
            parameters
        )
        updateUnAllocatedReceipt(receipt)
    }

    private fun updateUnAllocatedReceipt(receipt: Receipt) {
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
        SQLServerWrapper.executeProcedure(
            "addin_unallocatedreceipt",
            parameters
        )
    }

    private fun deleteHReceipt(receipt: Receipt) {
        SQLServerWrapper.executeProcedure(
            "delin_hreceipt",
            listOf(receipt.receiptId)
        )
        deleteReceipt(receipt)
    }

    private fun deleteReceipt(receipt: Receipt) {
        SQLServerWrapper.executeProcedure(
            "delin_receipt",
            listOf(receipt.receiptInId)
        )
        deleteUnAllocatedReceipt(receipt)
    }

    private fun deleteUnAllocatedReceipt(receipt: Receipt) {
        SQLServerWrapper.executeProcedure(
            "delin_unallocatedreceipt",
            listOf(receipt.unAllocatedReceiptId)
        )
    }
}