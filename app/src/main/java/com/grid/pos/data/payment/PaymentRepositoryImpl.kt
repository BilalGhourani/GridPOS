package com.grid.pos.data.payment

import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.Query
import com.grid.pos.data.FirebaseWrapper
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.DataModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.pos.POSUtils
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Extension.getDoubleValue
import com.grid.pos.utils.Extension.getObjectValue
import com.grid.pos.utils.Extension.getStringValue
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
                return FirebaseWrapper.insert(
                    "payment",
                    payment
                )
            }

            CONNECTION_TYPE.LOCAL.key -> {
                paymentDao.insert(payment)
                return DataModel(payment)
            }

            else -> {
                val dataModel = insertHPayment(payment)
                dataModel.data = getPaymentById(payment.paymentId)
                return dataModel

            }
        }
    }

    override suspend fun delete(
            payment: Payment
    ): DataModel {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                FirebaseWrapper.delete(
                    "payment",
                    payment
                )
            }

            CONNECTION_TYPE.LOCAL.key -> {
                paymentDao.delete(payment)
                DataModel(payment)
            }

            else -> {
                updateHPayment(payment)
            }
        }
    }

    override suspend fun update(
            payment: Payment
    ): DataModel {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                FirebaseWrapper.update(
                    "payment",
                    payment
                )
            }

            CONNECTION_TYPE.LOCAL.key -> {
                paymentDao.update(payment)
                DataModel(payment)
            }

            else -> {
                deleteHPayment(payment)
            }
        }
    }

    override suspend fun getPaymentById(
            id: String
    ): Payment? {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "payment",
                    limit = 1,
                    filters = mutableListOf(
                        Filter.equalTo(
                            "pay_id",
                            id
                        )
                    )
                )
                val document = querySnapshot?.documents?.firstOrNull()
                val payment = document?.toObject(Payment::class.java)
                payment?.paymentDocumentId = document?.id
                payment
            }

            CONNECTION_TYPE.LOCAL.key -> {
                paymentDao.getPaymentById(id)
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
                }
                return payment
            }
        }
    }

    override suspend fun getAllPayments(): MutableList<Payment> {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "payment",
                    filters = mutableListOf(
                        Filter.equalTo(
                            "pay_cmp_id",
                            SettingsModel.getCompanyID()
                        )
                    )
                )
                val size = querySnapshot?.size() ?: 0
                val companies = mutableListOf<Payment>()
                if (size > 0) {
                    for (document in querySnapshot!!) {
                        val obj = document.toObject(Payment::class.java)
                        if (obj.paymentId.isNotEmpty()) {
                            obj.paymentDocumentId = document.id
                            companies.add(obj)
                        }
                    }
                }
                companies
            }

            CONNECTION_TYPE.LOCAL.key -> {
                paymentDao.getAllPayments()
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
                }
                return payments
            }
        }
    }

    override suspend fun getLastTransactionNo(): Payment? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "payment",
                    limit = 1,
                    filters = mutableListOf(
                        Filter.equalTo(
                            "pay_cmp_id",
                            SettingsModel.getCompanyID()
                        ),
                        Filter.notEqualTo(
                            "pay_transno",
                            null
                        )
                    ),
                    orderBy = mutableListOf(
                        "pay_transno" to Query.Direction.DESCENDING
                    )
                )
                val document = querySnapshot?.documents?.firstOrNull()
                val payment = document?.toObject(Payment::class.java)
                payment?.paymentDocumentId = document?.id
                return  payment
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return paymentDao.getLastTransactionByType(
                    SettingsModel.getCompanyID() ?: ""
                )
            }

            else -> {
                return null/*val payments: MutableList<Payment> = mutableListOf()
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
            val id = queryResult.result
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
                false
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
                false
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
            queryResult.succeed
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
                false
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
                false
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
            queryResult.succeed
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
                false
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
                false
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
            queryResult.succeed
        )
    }
}