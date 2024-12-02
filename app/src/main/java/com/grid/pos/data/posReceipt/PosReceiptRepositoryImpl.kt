package com.grid.pos.data.posReceipt

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.pos.POSUtils
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Extension.getDoubleValue
import com.grid.pos.utils.Extension.getObjectValue
import com.grid.pos.utils.Extension.getStringValue
import kotlinx.coroutines.tasks.await
import java.sql.Timestamp
import java.util.Date

class PosReceiptRepositoryImpl(
        private val posReceiptDao: PosReceiptDao
) : PosReceiptRepository {
    override suspend fun insert(
            posReceipt: PosReceipt
    ): PosReceipt {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val docRef = FirebaseFirestore.getInstance().collection("pos_receipt")
                    .add(posReceipt.getMap()).await()
                posReceipt.posReceiptDocumentId = docRef.id
            }

            CONNECTION_TYPE.LOCAL.key -> {
                posReceiptDao.insert(posReceipt)
            }

            else -> {
                val rate = SettingsModel.currentCurrency?.currencyRate ?: 1.0
                if (posReceipt.posReceiptCash > 0.0 && !SettingsModel.posReceiptAccCashId.isNullOrEmpty()) {
                    posReceipt.posReceiptCashID = insertPOSReceiptByProcedure(
                        posReceipt.posReceiptInvoiceId!!,
                        true,
                        POSUtils.formatDouble(posReceipt.posReceiptCash),
                        POSUtils.formatDouble(posReceipt.posReceiptCash.times(rate)),
                        SettingsModel.posReceiptAccCashId!!
                    )
                }

                if (posReceipt.posReceiptCashs > 0.0 && !SettingsModel.posReceiptAccCash1Id.isNullOrEmpty()) {
                    posReceipt.posReceiptCashsID = insertPOSReceiptByProcedure(
                        posReceipt.posReceiptInvoiceId!!,
                        false,
                        POSUtils.formatDouble(posReceipt.posReceiptCashs.div(rate)),
                        POSUtils.formatDouble(posReceipt.posReceiptCashs),
                        SettingsModel.posReceiptAccCash1Id!!
                    )
                }

                if (posReceipt.posReceiptCredit > 0.0 && !SettingsModel.posReceiptAccCreditId.isNullOrEmpty()) {
                    posReceipt.posReceiptCreditID = insertPOSReceiptByProcedure(
                        posReceipt.posReceiptInvoiceId!!,
                        true,
                        POSUtils.formatDouble(posReceipt.posReceiptCredit),
                        POSUtils.formatDouble(posReceipt.posReceiptCredit.times(rate)),
                        SettingsModel.posReceiptAccCreditId!!
                    )
                }

                if (posReceipt.posReceiptCredits > 0.0 && !SettingsModel.posReceiptAccCredit1Id.isNullOrEmpty()) {
                    posReceipt.posReceiptCreditsID = insertPOSReceiptByProcedure(
                        posReceipt.posReceiptInvoiceId!!,
                        false,
                        POSUtils.formatDouble(posReceipt.posReceiptCredits.div(rate)),
                        POSUtils.formatDouble(posReceipt.posReceiptCredits),
                        SettingsModel.posReceiptAccCredit1Id!!
                    )
                }

                if (posReceipt.posReceiptDebit > 0.0 && !SettingsModel.posReceiptAccDebitId.isNullOrEmpty()) {
                    posReceipt.posReceiptDebitID = insertPOSReceiptByProcedure(
                        posReceipt.posReceiptInvoiceId!!,
                        true,
                        POSUtils.formatDouble(posReceipt.posReceiptDebit),
                        POSUtils.formatDouble(posReceipt.posReceiptDebit.times(rate)),
                        SettingsModel.posReceiptAccDebitId!!
                    )
                }

                if (posReceipt.posReceiptDebits > 0.0 && !SettingsModel.posReceiptAccDebit1Id.isNullOrEmpty()) {
                    posReceipt.posReceiptDebitsID = insertPOSReceiptByProcedure(
                        posReceipt.posReceiptInvoiceId!!,
                        false,
                        POSUtils.formatDouble(posReceipt.posReceiptDebits.div(rate)),
                        POSUtils.formatDouble(posReceipt.posReceiptDebits),
                        SettingsModel.posReceiptAccDebit1Id!!
                    )
                }
            }
        }
        return posReceipt
    }

    override suspend fun delete(
            posReceipt: PosReceipt
    ) {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                posReceipt.posReceiptDocumentId?.let {
                    FirebaseFirestore.getInstance().collection("pos_receipt").document(it).delete()
                        .await()
                }
            }

            CONNECTION_TYPE.LOCAL.key -> {
                posReceiptDao.delete(posReceipt)
            }

            else -> {
                posReceipt.posReceiptCashID?.let {
                    SQLServerWrapper.executeProcedure(
                        "delpos_receipt",
                        listOf(it)
                    )
                }

                posReceipt.posReceiptCashsID?.let {
                    SQLServerWrapper.executeProcedure(
                        "delpos_receipt",
                        listOf(it)
                    )
                }

                posReceipt.posReceiptCreditID?.let {
                    SQLServerWrapper.executeProcedure(
                        "delpos_receipt",
                        listOf(it)
                    )
                }

                posReceipt.posReceiptCreditsID?.let {
                    SQLServerWrapper.executeProcedure(
                        "delpos_receipt",
                        listOf(it)
                    )
                }

                posReceipt.posReceiptDebitID?.let {
                    SQLServerWrapper.executeProcedure(
                        "delpos_receipt",
                        listOf(it)
                    )
                }

                posReceipt.posReceiptDebitsID?.let {
                    SQLServerWrapper.executeProcedure(
                        "delpos_receipt",
                        listOf(it)
                    )
                }
            }
        }
    }

    override suspend fun update(
            posReceipt: PosReceipt
    ) {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                posReceipt.posReceiptDocumentId?.let {
                    FirebaseFirestore.getInstance().collection("pos_receipt").document(it)
                        .update(posReceipt.getMap()).await()
                }
            }

            CONNECTION_TYPE.LOCAL.key -> {
                posReceiptDao.update(posReceipt)
            }

            else -> {
                val rate = SettingsModel.currentCurrency?.currencyRate ?: 1.0
                if (!posReceipt.posReceiptCashID.isNullOrEmpty()) {
                    updatePOSReceiptByProcedure(
                        posReceipt.posReceiptCashID!!,
                        posReceipt.posReceiptInvoiceId!!,
                        true,
                        POSUtils.formatDouble(posReceipt.posReceiptCash),
                        POSUtils.formatDouble(posReceipt.posReceiptCash.times(rate)),
                        posReceipt.posReceiptCash_hsid,
                        SettingsModel.posReceiptAccCashId!!
                    )
                } else if (posReceipt.posReceiptCash > 0.0 && !SettingsModel.posReceiptAccCashId.isNullOrEmpty()) {
                    posReceipt.posReceiptCashID = insertPOSReceiptByProcedure(
                        posReceipt.posReceiptInvoiceId!!,
                        true,
                        POSUtils.formatDouble(posReceipt.posReceiptCash),
                        POSUtils.formatDouble(posReceipt.posReceiptCash.times(rate)),
                        SettingsModel.posReceiptAccCashId!!
                    )
                }
                if (!posReceipt.posReceiptCashsID.isNullOrEmpty()) {
                    updatePOSReceiptByProcedure(
                        posReceipt.posReceiptCashsID!!,
                        posReceipt.posReceiptInvoiceId!!,
                        false,
                        POSUtils.formatDouble(posReceipt.posReceiptCashs.div(rate)),
                        POSUtils.formatDouble(posReceipt.posReceiptCashs),
                        posReceipt.posReceiptCashs_hsid,
                        SettingsModel.posReceiptAccCash1Id!!
                    )
                } else if (posReceipt.posReceiptCashs > 0.0 && !SettingsModel.posReceiptAccCash1Id.isNullOrEmpty()) {
                    posReceipt.posReceiptCashsID = insertPOSReceiptByProcedure(
                        posReceipt.posReceiptInvoiceId!!,
                        false,
                        POSUtils.formatDouble(posReceipt.posReceiptCashs.div(rate)),
                        POSUtils.formatDouble(posReceipt.posReceiptCashs),
                        SettingsModel.posReceiptAccCash1Id!!
                    )
                }
                if (!posReceipt.posReceiptCreditID.isNullOrEmpty()) {
                    updatePOSReceiptByProcedure(
                        posReceipt.posReceiptCreditID!!,
                        posReceipt.posReceiptInvoiceId!!,
                        true,
                        POSUtils.formatDouble(posReceipt.posReceiptCredit),
                        POSUtils.formatDouble(posReceipt.posReceiptCredit.times(rate)),
                        posReceipt.posReceiptCredit_hsid,
                        SettingsModel.posReceiptAccCreditId!!
                    )
                } else if (posReceipt.posReceiptCredit > 0.0 && !SettingsModel.posReceiptAccCreditId.isNullOrEmpty()) {
                    posReceipt.posReceiptCreditID = insertPOSReceiptByProcedure(
                        posReceipt.posReceiptInvoiceId!!,
                        true,
                        POSUtils.formatDouble(posReceipt.posReceiptCredit),
                        POSUtils.formatDouble(posReceipt.posReceiptCredit.times(rate)),
                        SettingsModel.posReceiptAccCreditId!!
                    )
                }
                if (!posReceipt.posReceiptCreditsID.isNullOrEmpty()) {
                    updatePOSReceiptByProcedure(
                        posReceipt.posReceiptCreditsID!!,
                        posReceipt.posReceiptInvoiceId!!,
                        false,
                        POSUtils.formatDouble(posReceipt.posReceiptCredits.div(rate)),
                        POSUtils.formatDouble(posReceipt.posReceiptCredits),
                        posReceipt.posReceiptCredits_hsid,
                        SettingsModel.posReceiptAccCredit1Id!!
                    )
                } else if (posReceipt.posReceiptCredits > 0.0 && !SettingsModel.posReceiptAccCredit1Id.isNullOrEmpty()) {
                    posReceipt.posReceiptCreditsID = insertPOSReceiptByProcedure(
                        posReceipt.posReceiptInvoiceId!!,
                        false,
                        POSUtils.formatDouble(posReceipt.posReceiptCredits.div(rate)),
                        POSUtils.formatDouble(posReceipt.posReceiptCredits),
                        SettingsModel.posReceiptAccCredit1Id!!
                    )
                }
                if (!posReceipt.posReceiptDebitID.isNullOrEmpty()) {
                    updatePOSReceiptByProcedure(
                        posReceipt.posReceiptDebitID!!,
                        posReceipt.posReceiptInvoiceId!!,
                        true,
                        POSUtils.formatDouble(posReceipt.posReceiptDebit),
                        POSUtils.formatDouble(posReceipt.posReceiptDebit.times(rate)),
                        posReceipt.posReceiptDebit_hsid,
                        SettingsModel.posReceiptAccDebitId!!
                    )
                } else if (posReceipt.posReceiptDebit > 0.0 && !SettingsModel.posReceiptAccDebitId.isNullOrEmpty()) {
                    posReceipt.posReceiptDebitID = insertPOSReceiptByProcedure(
                        posReceipt.posReceiptInvoiceId!!,
                        true,
                        POSUtils.formatDouble(posReceipt.posReceiptDebit),
                        POSUtils.formatDouble(posReceipt.posReceiptDebit.times(rate)),
                        SettingsModel.posReceiptAccDebitId!!
                    )
                }
                if (!posReceipt.posReceiptDebitsID.isNullOrEmpty()) {
                    updatePOSReceiptByProcedure(
                        posReceipt.posReceiptDebitsID!!,
                        posReceipt.posReceiptInvoiceId!!,
                        false,
                        POSUtils.formatDouble(posReceipt.posReceiptDebits.div(rate)),
                        POSUtils.formatDouble(posReceipt.posReceiptDebits),
                        posReceipt.posReceiptDebits_hsid,
                        SettingsModel.posReceiptAccDebit1Id!!
                    )
                } else if (posReceipt.posReceiptDebits > 0.0 && !SettingsModel.posReceiptAccDebit1Id.isNullOrEmpty()) {
                    posReceipt.posReceiptDebitsID = insertPOSReceiptByProcedure(
                        posReceipt.posReceiptInvoiceId!!,
                        false,
                        POSUtils.formatDouble(posReceipt.posReceiptDebits.div(rate)),
                        POSUtils.formatDouble(posReceipt.posReceiptDebits),
                        SettingsModel.posReceiptAccDebit1Id!!
                    )
                }
            }
        }
    }

    override suspend fun getPosReceiptByInvoice(
            invoiceHeaderId: String
    ): PosReceipt? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("pos_receipt")
                    .whereEqualTo(
                        "pr_hi_id",
                        invoiceHeaderId
                    ).get().await()
                val document = querySnapshot.documents.firstOrNull()
                if (document != null) {
                    val obj = document.toObject(PosReceipt::class.java)
                    if (obj != null && obj.posReceiptId.isNotEmpty()) {
                        obj.posReceiptDocumentId = document.id
                        return obj
                    }
                }
                return null
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return posReceiptDao.getPosReceiptByInvoice(invoiceHeaderId)
            }

            else -> {
                val where = "pr_hi_id='$invoiceHeaderId'"
                val dbResult = SQLServerWrapper.getListOf(
                    "pos_receipt",
                    "",
                    mutableListOf("*"),
                    where,
                    "ORDER BY ra_order ASC",
                    " INNER JOIN pos_receiptacc on pr_ra_id = ra_id"
                )

                try {
                    val posReceipt = PosReceipt()
                    var commonAreFilled = false
                    dbResult?.let {
                        while (it.next()) {
                            if (!commonAreFilled) {
                                commonAreFilled = true
                                posReceipt.posReceiptId = it.getStringValue("pr_id")
                                posReceipt.posReceiptInvoiceId = it.getStringValue("pr_hi_id")

                                val timeStamp = it.getObjectValue("pr_timestamp")
                                posReceipt.posReceiptTimeStamp = when (timeStamp) {
                                    is Date -> timeStamp
                                    is String -> DateHelper.getDateFromString(
                                        timeStamp,
                                        "yyyy-MM-dd hh:mm:ss.SSS"
                                    )
                                    else -> null
                                }
                                posReceipt.posReceiptDateTime = posReceipt.posReceiptTimeStamp!!.time
                                posReceipt.posReceiptUserStamp = it.getStringValue("pr_userstamp")
                            }
                            val raAccId = it.getStringValue("pr_ra_id","unknown")
                            val raType = it.getStringValue("ra_type")
                            when (raType) {
                                "Cash" -> {
                                    if (raAccId == SettingsModel.posReceiptAccCashId) {
                                        posReceipt.posReceiptCashID = it.getStringValue("pr_id")
                                        posReceipt.posReceiptCash_hsid = it.getStringValue("pr_hsid")
                                        posReceipt.posReceiptCash = it.getDoubleValue("pr_amt")
                                    } else if (raAccId == SettingsModel.posReceiptAccCash1Id) {
                                        posReceipt.posReceiptCashsID = it.getStringValue("pr_id")
                                        posReceipt.posReceiptCashs_hsid = it.getStringValue("pr_hsid")
                                        posReceipt.posReceiptCashs = it.getDoubleValue("pr_amt")
                                    }
                                }

                                "Credit","Credit Card" -> {
                                    if (raAccId == SettingsModel.posReceiptAccCreditId) {
                                        posReceipt.posReceiptCreditID = it.getStringValue("pr_id")
                                        posReceipt.posReceiptCredit_hsid = it.getStringValue("pr_hsid")
                                        posReceipt.posReceiptCredit = it.getDoubleValue("pr_amt")
                                    } else if (raAccId == SettingsModel.posReceiptAccCredit1Id)  {
                                        posReceipt.posReceiptCreditsID = it.getStringValue("pr_id")
                                        posReceipt.posReceiptCredits_hsid = it.getStringValue("pr_hsid")
                                        posReceipt.posReceiptCredits = it.getDoubleValue("pr_amt")
                                    }

                                }

                                "Debit","Debit Card" -> {
                                    if (raAccId == SettingsModel.posReceiptAccDebitId) {
                                        posReceipt.posReceiptDebitID = it.getStringValue("pr_id")
                                        posReceipt.posReceiptDebit_hsid = it.getStringValue("pr_hsid")
                                        posReceipt.posReceiptDebit = it.getDoubleValue("pr_amt")
                                    } else if (raAccId == SettingsModel.posReceiptAccDebit1Id) {
                                        posReceipt.posReceiptDebitsID = it.getStringValue("pr_id")
                                        posReceipt.posReceiptDebits_hsid = it.getStringValue("pr_hsid")
                                        posReceipt.posReceiptDebits = it.getDoubleValue("pr_amt")
                                    }

                                }
                            }
                        }
                        SQLServerWrapper.closeResultSet(it)
                        return posReceipt
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return null
            }
        }
    }

    private fun insertPOSReceiptByProcedure(
            posReceiptInvoiceId: String,
            isFirst: Boolean,
            firstValue: String,
            secondValue: String,
            receiptAccId: String
    ): String {
        val parameters = mutableListOf(
            null,//pr_id
            posReceiptInvoiceId,//pr_hi_id
            receiptAccId,//pr_ra_id
            if(isFirst) firstValue else secondValue,
            firstValue,
            secondValue,
            SettingsModel.currentCompany?.cmp_multibranchcode,
            firstValue,//pr_amtinvcurr
            null,//pr_note
            Timestamp(System.currentTimeMillis()),//pr_date
            Timestamp(System.currentTimeMillis()),//pr_timestamp
            SettingsModel.currentUser?.userUsername,//pr_userstamp
            0,//pr_commission
        )
        if (SettingsModel.isSqlServerWebDb) {
            parameters.add(null)//pr_denomination
            parameters.add(null)//pr_count
        }
        return SQLServerWrapper.executeProcedure(
            "addpos_receipt",
            parameters
        ) ?: ""
    }

    private fun updatePOSReceiptByProcedure(
            posReceiptId: String,
            posReceiptInvoiceId: String,
            isFirst: Boolean,
            firstValue: String,
            secondValue: String,
            pr_hsid: String?,
            receiptAccId: String
    ) {
        val parameters = mutableListOf(
            posReceiptId,//pr_id
            posReceiptInvoiceId,//pr_hi_id
            receiptAccId,//pr_ra_id
            if(isFirst) firstValue else secondValue,
            firstValue,
            secondValue,
            firstValue,//pr_amtinvcurr
            null,//pr_note
            Timestamp(System.currentTimeMillis()),//pr_date
            pr_hsid,//@pr_hsid
            Timestamp(System.currentTimeMillis()),//pr_timestamp
            SettingsModel.currentUser?.userUsername,//pr_userstamp
            0,//pr_commission
        )
        if (SettingsModel.isSqlServerWebDb) {
            parameters.add(null)//pr_denomination
            parameters.add(null)//pr_count
        }
        SQLServerWrapper.executeProcedure(
            "updpos_receipt",
            parameters
        )
    }


}