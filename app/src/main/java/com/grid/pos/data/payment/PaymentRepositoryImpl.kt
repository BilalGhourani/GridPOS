package com.grid.pos.data.payment

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

class PaymentRepositoryImpl(
        private val paymentDao: PaymentDao
) : PaymentRepository {
    override suspend fun insert(
            payment: Payment
    ): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val docRef = FirebaseFirestore.getInstance().collection("payment").add(payment)
                    .await()
                payment.paymentDocumentId = docRef.id
                return DataModel(payment)
            }

            CONNECTION_TYPE.LOCAL.key -> {
                paymentDao.insert(payment)
                return DataModel(payment)
            }

            else -> {
                val dataModel = insertHPayment(payment)
                return if (dataModel.succeed) {
                    getPaymentById(payment.paymentId)
                } else {
                    dataModel
                }
            }
        }
    }

    override suspend fun delete(
            payment: Payment
    ): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                payment.paymentDocumentId?.let {
                    FirebaseFirestore.getInstance().collection("payment").document(it).delete()
                        .await()
                    return DataModel(payment)
                }
                return DataModel(
                    payment,
                    false
                )
            }

            CONNECTION_TYPE.LOCAL.key -> {
                paymentDao.delete(payment)
                return DataModel(payment)
            }

            else -> {
                return updateHPayment(payment)
            }
        }
    }

    override suspend fun update(
            payment: Payment
    ): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                payment.paymentDocumentId?.let {
                    FirebaseFirestore.getInstance().collection("payment").document(it)
                        .update(payment.getMap()).await()
                    return DataModel(payment)
                }
                return DataModel(
                    payment,
                    false
                )
            }

            CONNECTION_TYPE.LOCAL.key -> {
                paymentDao.update(payment)
                return DataModel(payment)
            }

            else -> {
                return deleteHPayment(payment)
            }
        }
    }

    override suspend fun getPaymentById(
            id: String
    ): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("payment")
                    .whereEqualTo(
                        "pay_cmp_id",
                        SettingsModel.getCompanyID()
                    ).whereEqualTo(
                        "pay_id",
                        id
                    ).get().await()
                val document = querySnapshot.documents.firstOrNull()
                return DataModel(document?.toObject(Payment::class.java))
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return DataModel(paymentDao.getPaymentById(id))
            }

            else -> {//CONNECTION_TYPE.SQL_SERVER.key
                var payment: Payment? = null
                try {
                    val where = if (SettingsModel.isSqlServerWebDb) "hpa_cmp_id='${SettingsModel.getCompanyID()}' AND hpa_id='$id'" else "hpa_id='$id'"
                    val dbResult = SQLServerWrapper.getListOf(
                        "in_hpayment",
                        "TOP 1",
                        mutableListOf("*"),
                        where,
                        "ORDER BY hpa_date DESC",
                        "INNER JOIN in_payment on hpa_id = pay_hpa_id INNER JOIN in_unallocatedpayment on hpa_id = up_hpa_id"
                    )
                    dbResult?.let {
                        while (it.next()) {
                            payment = fillParams(it)
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    return DataModel(
                        null,
                        false,
                        e.message
                    )
                }
                return DataModel(payment)
            }
        }
    }

    override suspend fun getAllPayments(): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("payment")
                    .whereEqualTo(
                        "pay_cmp_id",
                        SettingsModel.getCompanyID()
                    ).get().await()
                val companies = mutableListOf<Payment>()
                if (querySnapshot.size() > 0) {
                    for (document in querySnapshot) {
                        val obj = document.toObject(Payment::class.java)
                        if (obj.paymentId.isNotEmpty()) {
                            obj.paymentDocumentId = document.id
                            companies.add(obj)
                        }
                    }
                }
                return DataModel(companies)
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return DataModel(paymentDao.getAllPayments())
            }

            else -> {//CONNECTION_TYPE.SQL_SERVER.key
                val payments: MutableList<Payment> = mutableListOf()
                try {
                    val where = if (SettingsModel.isSqlServerWebDb) "hpa_cmp_id='${SettingsModel.getCompanyID()}'" else ""
                    val dbResult = SQLServerWrapper.getListOf(
                        "in_hpayment",
                        "TOP 100",
                        mutableListOf("*"),
                        where,
                        "ORDER BY hpa_date DESC",
                        "INNER JOIN in_payment on hpa_id = pay_hpa_id INNER JOIN in_unallocatedpayment on hpa_id = up_hpa_id"
                    )
                    dbResult?.let {
                        while (it.next()) {
                            payments.add(
                                fillParams(
                                    it
                                )
                            )
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    return DataModel(
                        payments,
                        false,
                        e.message
                    )
                }
                return DataModel(payments)
            }
        }
    }

    override suspend fun getLastTransactionNo(): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("payment")
                    .whereEqualTo(
                        "pay_cmp_id",
                        SettingsModel.getCompanyID()
                    ).whereNotEqualTo(
                        "pay_transno",
                        null
                    ).orderBy(
                        "pay_transno",
                        Query.Direction.DESCENDING
                    ).limit(1).get().await()
                val document = querySnapshot.firstOrNull()
                return DataModel(document?.toObject(Payment::class.java))
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return DataModel(
                    paymentDao.getLastTransactionByType(
                        SettingsModel.getCompanyID() ?: ""
                    )
                )
            }

            else -> {
                return DataModel(null)/*val payments: MutableList<Payment> = mutableListOf()
                try {
                    val where = if (SettingsModel.isSqlServerWebDb) "hpa_cmp_id='${SettingsModel.getCompanyID()}'" else ""
                    val dbResult = SQLServerWrapper.getListOf(
                        "in_hpayment",
                        "TOP 1",
                        mutableListOf("*"),
                        where,
                        "ORDER BY hpa_transno DESC",
                        "INNER JOIN in_payment on hpa_id = pay_hpa_id INNER JOIN in_unallocatedpayment on hpa_id = up_hpa_id"
                    )
                    dbResult?.let {
                        while (it.next()) {
                            payments.add(
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
                return if (payments.isNotEmpty()) payments[0] else null*/
            }
        }
    }

    private fun fillParams(
            obj: ResultSet
    ): Payment {
        return Payment().apply {
            paymentId = obj.getStringValue("hpa_id")
            paymentInId = obj.getStringValue("pay_id")
            unAllocatedPaymentId = obj.getStringValue("up_id")
            paymentNo = obj.getStringValue("hpa_no")
            paymentCompanyId = obj.getStringValue("hpa_cmp_id")
            paymentType = obj.getStringValue("")
            paymentTransCode = obj.getStringValue("hpa_tt_code")
            paymentTransNo = obj.getStringValue("hpa_transno")
            paymentThirdParty = obj.getStringValue("hpa_tp_name")
            paymentCurrency = obj.getStringValue("pay_cur_code")
            paymentAmount = obj.getDoubleValue("pay_amt")
            paymentAmountFirst = obj.getDoubleValue("pay_amtf")
            paymentAmountSecond = obj.getDoubleValue("pay_amts")
            paymentDesc = obj.getStringValue("hpa_desc")
            paymentNote = obj.getStringValue("hpa_note")
            val timeStamp = obj.getObjectValue("hpa_timestamp")
            paymentTimeStamp = if (timeStamp is Date) timeStamp else DateHelper.getDateFromString(
                timeStamp as String,
                "yyyy-MM-dd hh:mm:ss.SSS"
            )
            paymentDateTime = paymentTimeStamp!!.time
            paymentUserStamp = obj.getStringValue("hpa_userstamp")
        }
    }

    private fun insertHPayment(payment: Payment): DataModel {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                "null_String_output",//@hpa_id
                payment.paymentCompanyId,//@hpa_cmp_id
                Timestamp(System.currentTimeMillis()),//@hpa_date
                SettingsModel.pvTransactionType,//@hpa_tt_code
                null,/*payment.paymentTransNo*///@hpa_transno
                null,//@hpa_status
                payment.paymentDesc,//@hpa_desc
                null,//@hpa_refno
                payment.paymentNote,//@hpa_note
                payment.paymentThirdParty,//@hpa_tp_name
                null,//@hpa_cashname
                SettingsModel.currentUser?.userUsername,//@hpa_userstamp
                null,//@hpa_sessionpointer
                SettingsModel.currentCompany?.cmp_multibranchcode,//@BranchCode
                Timestamp(System.currentTimeMillis()),//@hpa_valuedate
                null,//@hpa_employee
                null,//@hpa_pathtodoc
            )
        } else {
            listOf(
                payment.paymentCompanyId,//@hpa_cmp_id
                Timestamp(System.currentTimeMillis()),//@hpa_date
                SettingsModel.pvTransactionType,//@hpa_tt_code
                null,/*payment.paymentTransNo*///@hpa_transno
                null,//@hpa_status
                payment.paymentDesc,//@hpa_desc
                null,//@hpa_refno
                payment.paymentNote,//@hpa_note
                payment.paymentThirdParty,//@hpa_tp_name
                null,//@hpa_cashname
                SettingsModel.currentUser?.userUsername,//@hpa_userstamp
                null,//@hpa_sessionpointer
                SettingsModel.currentCompany?.cmp_multibranchcode,//@BranchCode
                Timestamp(System.currentTimeMillis()),//@hpa_valuedate
                null,//@hpa_employee
                null,//@hpa_pathtodoc
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "addin_hpayment",
            parameters
        )
        if (queryResult.succeed) {
            val id = queryResult.result as? String
            if (id.isNullOrEmpty()) {
                try {
                    val dbResult = SQLServerWrapper.getQueryResult("select max(hpa_id) as id from in_hpayment")
                    dbResult?.let {
                        while (it.next()) {
                            payment.paymentId = it.getStringValue(
                                "id",
                                payment.paymentId
                            )
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                payment.paymentId = id
            }
            return insertPayment(payment)
        } else {
            return DataModel(
                null,
                false,
                queryResult.result as? String
            )
        }

    }

    private fun insertPayment(payment: Payment): DataModel {
        val decimal = SettingsModel.currentCurrency?.currencyName1Dec ?: 3
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                null,//@pay_id
                payment.paymentId,//@pay_hpa_id
                null,//@pay_cha_ch_code
                null,//@pay_cha_code
                payment.paymentCurrency,//@pay_cur_code
                POSUtils.formatDouble(
                    payment.paymentAmount,
                    decimal
                ),//@pay_amt
                POSUtils.formatDouble(
                    payment.paymentAmountFirst,
                    decimal
                ),//@pay_amtf
                POSUtils.formatDouble(
                    payment.paymentAmountSecond,
                    decimal
                ),//@pay_amts
                null,//@pay_bank
                null,//@pay_chequeno
                Timestamp(System.currentTimeMillis()),//@pay_date
                SettingsModel.defaultSqlServerBranch,//@pay_bra_name
                null,//@pay_prj_name
                null,//@pay_note
                SettingsModel.currentUser?.userUsername,//@pay_userstamp
                null,//@pay_ra_id//TODO
                SettingsModel.currentCompany?.cmp_multibranchcode,//@BranchCode
                null,//@pay_div_name
                false,//@pay_tax
                0.0,//@pay_denomination
                0.0//@pay_count
            )
        } else {
            listOf(
                null,//@pay_id
                payment.paymentId,//@pay_hpa_id
                null,//@pay_cha_ch_code//DSReceiptAcc.Tables("pos_receiptacc").Select("ra_name='" & LBPaymentMode.SelectedItem.ToString & "'").ElementAt(0).Item("ra_chcode"))
                null,//@pay_cha_code
                payment.paymentCurrency,//@pay_cur_code//DSReceiptAcc.Tables("pos_receiptacc").Select("ra_name='" & LBPaymentMode.SelectedItem.ToString & "'").ElementAt(0).Item("ra_cur_code"))
                POSUtils.formatDouble(
                    payment.paymentAmount,
                    decimal
                ),//@pay_amt
                POSUtils.formatDouble(
                    payment.paymentAmountFirst,
                    decimal
                ),//@pay_amtf
                POSUtils.formatDouble(
                    payment.paymentAmountSecond,
                    decimal
                ),//@pay_amts
                null,//@pay_bank
                null,//@pay_chequeno
                Timestamp(System.currentTimeMillis()),//@pay_date
                SettingsModel.defaultSqlServerBranch,//@pay_bra_name
                null,//@pay_prj_name
                null,//@pay_note
                SettingsModel.currentUser?.userUsername,//@pay_userstamp
                null,//@pay_ra_id//DSReceiptAcc.Tables("pos_receiptacc").Select("ra_name='" & LBPaymentMode.SelectedItem.ToString & "'").ElementAt(0).Item("ra_id"))
                SettingsModel.currentCompany?.cmp_multibranchcode,//@BranchCode
                null,//@pay_div_name
                false,//@pay_tax
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "addin_payment",
            parameters
        )
        return if (queryResult.succeed) {
            insertUnAllocatePayment(payment)
        } else {
            DataModel(
                null,
                false,
                queryResult.result as? String
            )
        }
    }

    private fun insertUnAllocatePayment(payment: Payment): DataModel {
        val decimal = SettingsModel.currentCurrency?.currencyName1Dec ?: 3
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                null,//@up_id
                payment.paymentId,//@up_hpa_id
                payment.paymentDesc,//@up_desc
                payment.paymentCurrency,//@up_cur_code
                POSUtils.formatDouble(
                    payment.paymentAmount,
                    decimal
                ),//@up_amt
                POSUtils.formatDouble(
                    payment.paymentAmountFirst,
                    decimal
                ),//@up_amtf
                POSUtils.formatDouble(
                    payment.paymentAmountSecond,
                    decimal
                ),//@up_amts
                false,//@up_allocated
                null,//@up_note
                SettingsModel.currentUser?.userUsername,//@up_userstamp
                SettingsModel.currentCompany?.cmp_multibranchcode,//@BranchCode
                null,//@up_cha_ch_code
                null,//@up_cha_code
                SettingsModel.defaultSqlServerBranch,//@up_bra_name
                null,//@up_prj_name
                null,//@up_div_name
            )
        } else {
            listOf(
                null,//@up_id
                payment.paymentId,//@up_hpa_id
                payment.paymentDesc,//@up_desc
                payment.paymentCurrency,//@up_cur_code
                POSUtils.formatDouble(
                    payment.paymentAmount,
                    decimal
                ),//@up_amt
                POSUtils.formatDouble(
                    payment.paymentAmountFirst,
                    decimal
                ),//@up_amtf
                POSUtils.formatDouble(
                    payment.paymentAmountSecond,
                    decimal
                ),//@up_amts
                false,//@up_allocated
                null,//@up_note
                SettingsModel.currentUser?.userUsername,//@up_userstamp
                SettingsModel.currentCompany?.cmp_multibranchcode,//@BranchCode
                null,//@up_cha_ch_code
                null,//@up_cha_code
                SettingsModel.defaultSqlServerBranch,//@up_bra_name
                null,//@up_prj_name
                null,//@up_div_name
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "addin_unallocatedpayment",
            parameters
        )
        return DataModel(
            payment,
            queryResult.succeed,
            queryResult.result as? String
        )
    }

    private fun updateHPayment(payment: Payment): DataModel {
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                payment.paymentId,//@hpa_id
                payment.paymentNo,//@hpa_no
                payment.paymentCompanyId,//@hpa_cmp_id
                Timestamp(System.currentTimeMillis()),//@hpa_date
                SettingsModel.pvTransactionType,//@hpa_tt_code
                payment.paymentTransNo,//@hpa_transno
                null,//@hpa_status
                payment.paymentDesc,//@hpa_desc
                null,//@hpa_refno
                payment.paymentNote,//@hpa_note
                payment.paymentThirdParty,//@hpa_tp_name
                null,//@hpa_cashname
                SettingsModel.currentUser?.userUsername,//@hpa_userstamp
                Timestamp(System.currentTimeMillis()),//@hpa_valuedate
                null,//@hpa_employee
                null,//@hpa_pathtodoc
            )
        } else {
            listOf(
                payment.paymentId,//@hpa_id
                payment.paymentNo,//@hpa_no
                payment.paymentCompanyId,//@hpa_cmp_id
                Timestamp(System.currentTimeMillis()),//@hpa_date
                SettingsModel.pvTransactionType,//@hpa_tt_code
                payment.paymentTransNo,//@hpa_transno
                null,//@hpa_status
                payment.paymentDesc,//@hpa_desc
                null,//@hpa_refno
                payment.paymentNote,//@hpa_note
                payment.paymentThirdParty,//@hpa_tp_name
                null,//@hpa_cashname
                SettingsModel.currentUser?.userUsername,//@hpa_userstamp
                Timestamp(System.currentTimeMillis()),//@hpa_valuedate
                null,//@hpa_employee
                null,//@hpa_pathtodoc
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "updin_hpayment",
            parameters
        )
        return if (queryResult.succeed) {
            updatePayment(payment)
        } else {
            DataModel(
                null,
                false,
                queryResult.result as? String
            )
        }
    }

    private fun updatePayment(payment: Payment): DataModel {
        val decimal = SettingsModel.currentCurrency?.currencyName1Dec ?: 3
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                payment.paymentInId,//@pay_id
                payment.paymentId,//@pay_hpa_id
                null,//@pay_cha_ch_code
                null,//@pay_cha_code
                payment.paymentCurrency,//@pay_cur_code
                POSUtils.formatDouble(
                    payment.paymentAmount,
                    decimal
                ),//@pay_amt
                POSUtils.formatDouble(
                    payment.paymentAmountFirst,
                    decimal
                ),//@pay_amtf
                POSUtils.formatDouble(
                    payment.paymentAmountSecond,
                    decimal
                ),//@pay_amts
                null,//@pay_bank
                null,//@pay_chequeno
                Timestamp(System.currentTimeMillis()),//@pay_date
                SettingsModel.defaultSqlServerBranch,//@pay_bra_name
                null,//@pay_prj_name
                null,//@pay_note
                SettingsModel.currentUser?.userUsername,//@pay_userstamp
                null,//@pay_ra_id//TODO
                null,//@pay_div_name
                false,//@pay_tax
                null,//@pay_denomination
                null//@pay_count
            )
        } else {
            listOf(
                payment.paymentInId,//@pay_id
                payment.paymentId,//@pay_hpa_id
                null,//@pay_cha_ch_code//DSReceiptAcc.Tables("pos_receiptacc").Select("ra_name='" & LBPaymentMode.SelectedItem.ToString & "'").ElementAt(0).Item("ra_chcode"))
                null,//@pay_cha_code
                payment.paymentCurrency,//@pay_cur_code//DSReceiptAcc.Tables("pos_receiptacc").Select("ra_name='" & LBPaymentMode.SelectedItem.ToString & "'").ElementAt(0).Item("ra_cur_code"))
                POSUtils.formatDouble(
                    payment.paymentAmount,
                    decimal
                ),//@pay_amt
                POSUtils.formatDouble(
                    payment.paymentAmountFirst,
                    decimal
                ),//@pay_amtf
                POSUtils.formatDouble(
                    payment.paymentAmountSecond,
                    decimal
                ),//@pay_amts
                null,//@pay_bank
                null,//@pay_chequeno
                Timestamp(System.currentTimeMillis()),//@pay_date
                SettingsModel.defaultSqlServerBranch,//@pay_bra_name
                null,//@pay_prj_name
                null,//@pay_note
                SettingsModel.currentUser?.userUsername,//@pay_userstamp
                null,//@pay_ra_id//DSReceiptAcc.Tables("pos_receiptacc").Select("ra_name='" & LBPaymentMode.SelectedItem.ToString & "'").ElementAt(0).Item("ra_id"))
                null,//@pay_div_name
                false,//@pay_tax
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "updin_payment",
            parameters
        )
        return if (queryResult.succeed) {
            updateUnAllocatedPayment(payment)
        } else {
            DataModel(
                null,
                false,
                queryResult.result as? String
            )
        }

    }

    private fun updateUnAllocatedPayment(payment: Payment): DataModel {
        val decimal = SettingsModel.currentCurrency?.currencyName1Dec ?: 3
        val parameters = if (SettingsModel.isSqlServerWebDb) {
            listOf(
                payment.unAllocatedPaymentId,//@up_id
                payment.paymentId,//@up_hpa_id
                payment.paymentDesc,//@up_desc
                payment.paymentCurrency,//@up_cur_code
                POSUtils.formatDouble(
                    payment.paymentAmount,
                    decimal
                ),//@up_amt
                POSUtils.formatDouble(
                    payment.paymentAmountFirst,
                    decimal
                ),//@up_amtf
                POSUtils.formatDouble(
                    payment.paymentAmountSecond,
                    decimal
                ),//@up_amts
                false,//@up_allocated
                null,//@up_note
                SettingsModel.currentUser?.userUsername,//@up_userstamp
                null,//@up_cha_ch_code
                null,//@up_cha_code
                SettingsModel.defaultSqlServerBranch,//@up_bra_name
                null,//@up_prj_name
                null,//@up_div_name
            )
        } else {
            listOf(
                payment.unAllocatedPaymentId,//@up_id
                payment.paymentId,//@up_hpa_id
                payment.paymentDesc,//@up_desc
                payment.paymentCurrency,//@up_cur_code
                POSUtils.formatDouble(
                    payment.paymentAmount,
                    decimal
                ),//@up_amt
                POSUtils.formatDouble(
                    payment.paymentAmountFirst,
                    decimal
                ),//@up_amtf
                POSUtils.formatDouble(
                    payment.paymentAmountSecond,
                    decimal
                ),//@up_amts
                false,//@up_allocated
                null,//@up_note
                SettingsModel.currentUser?.userUsername,//@up_userstamp
                null,//@up_cha_ch_code
                null,//@up_cha_code
                SettingsModel.defaultSqlServerBranch,//@up_bra_name
                null,//@up_prj_name
                null,//@up_div_name
            )
        }
        val queryResult = SQLServerWrapper.executeProcedure(
            "updin_unallocatedpayment",
            parameters
        )
        return DataModel(
            payment,
            queryResult.succeed,
            queryResult.result as? String
        )
    }

    private fun deleteHPayment(payment: Payment): DataModel {
        val queryResult = SQLServerWrapper.executeProcedure(
            "delin_hpayment",
            listOf(payment.paymentId)
        )
        return if (queryResult.succeed) {
            deletePayment(payment)
        } else {
            DataModel(
                null,
                false,
                queryResult.result as? String
            )
        }

    }

    private fun deletePayment(payment: Payment): DataModel {
        val queryResult = SQLServerWrapper.executeProcedure(
            "delin_payment",
            listOf(payment.paymentInId)
        )

        return if (queryResult.succeed) {
            deleteUnAllocatedPayment(payment)
        } else {
            DataModel(
                null,
                false,
                queryResult.result as? String
            )
        }
    }

    private fun deleteUnAllocatedPayment(payment: Payment): DataModel {
        val queryResult = SQLServerWrapper.executeProcedure(
            "addin_unallocatedpayment",
            listOf(payment.unAllocatedPaymentId)
        )
        return DataModel(
            payment,
            queryResult.succeed,
            queryResult.result as? String
        )
    }
}