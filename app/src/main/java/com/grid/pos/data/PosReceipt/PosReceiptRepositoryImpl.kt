package com.grid.pos.data.PosReceipt

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.pos.POSUtils
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Extension.getDoubleValue
import com.grid.pos.utils.Extension.getIntValue
import com.grid.pos.utils.Extension.getObjectValue
import com.grid.pos.utils.Extension.getStringValue
import com.grid.pos.utils.Utils
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
                    insertPOSReceiptByProcedure(
                        posReceipt.posReceiptInvoiceId!!,
                        POSUtils.formatDouble(posReceipt.posReceiptCash),
                        POSUtils.formatDouble(posReceipt.posReceiptCash.times(rate)),
                        SettingsModel.posReceiptAccCashId!!
                    )
                }

                if (posReceipt.posReceiptCashs > 0.0 && !SettingsModel.posReceiptAccCash1Id.isNullOrEmpty()) {
                    insertPOSReceiptByProcedure(
                        posReceipt.posReceiptInvoiceId!!,
                        POSUtils.formatDouble(posReceipt.posReceiptCashs),
                        POSUtils.formatDouble(posReceipt.posReceiptCashs.div(rate)),
                        SettingsModel.posReceiptAccCash1Id!!
                    )
                }

                if (posReceipt.posReceiptCredit > 0.0 && !SettingsModel.posReceiptAccCreditId.isNullOrEmpty()) {
                    insertPOSReceiptByProcedure(
                        posReceipt.posReceiptInvoiceId!!,
                        POSUtils.formatDouble(posReceipt.posReceiptCredit),
                        POSUtils.formatDouble(posReceipt.posReceiptCredit.times(rate)),
                        SettingsModel.posReceiptAccCreditId!!
                    )
                }

                if (posReceipt.posReceiptCredits > 0.0 && !SettingsModel.posReceiptAccCredit1Id.isNullOrEmpty()) {
                    insertPOSReceiptByProcedure(
                        posReceipt.posReceiptInvoiceId!!,
                        POSUtils.formatDouble(posReceipt.posReceiptCredits),
                        POSUtils.formatDouble(posReceipt.posReceiptCredits.div(rate)),
                        SettingsModel.posReceiptAccCredit1Id!!
                    )
                }

                if (posReceipt.posReceiptDebit > 0.0 && !SettingsModel.posReceiptAccDebitId.isNullOrEmpty()) {
                    insertPOSReceiptByProcedure(
                        posReceipt.posReceiptInvoiceId!!,
                        POSUtils.formatDouble(posReceipt.posReceiptDebit),
                        POSUtils.formatDouble(posReceipt.posReceiptDebit.times(rate)),
                        SettingsModel.posReceiptAccDebitId!!
                    )
                }

                if (posReceipt.posReceiptDebits > 0.0 && !SettingsModel.posReceiptAccDebit1Id.isNullOrEmpty()) {
                    insertPOSReceiptByProcedure(
                        posReceipt.posReceiptInvoiceId!!,
                        POSUtils.formatDouble(posReceipt.posReceiptDebits),
                        POSUtils.formatDouble(posReceipt.posReceiptDebits.div(rate)),
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
                    SQLServerWrapper.delete(
                        "pos_receipt",
                        "pr_id = '$it'"
                    )
                    SQLServerWrapper.delete(
                        "pos_receiptacc",
                        "ra_id =  '${posReceipt.posReceiptCashRaID}'"
                    )
                }

                posReceipt.posReceiptCashsID?.let {
                    SQLServerWrapper.delete(
                        "pos_receipt",
                        "pr_id = '$it'"
                    )
                    SQLServerWrapper.delete(
                        "pos_receiptacc",
                        "ra_id =  '${posReceipt.posReceiptCashsRaID}'"
                    )
                }

                posReceipt.posReceiptCreditID?.let {
                    SQLServerWrapper.delete(
                        "pos_receipt",
                        "pr_id = '$it'"
                    )
                    SQLServerWrapper.delete(
                        "pos_receiptacc",
                        "ra_id =  '${posReceipt.posReceiptCreditRaID}'"
                    )
                }

                posReceipt.posReceiptCreditsID?.let {
                    SQLServerWrapper.delete(
                        "pos_receipt",
                        "pr_id = '$it'"
                    )
                    SQLServerWrapper.delete(
                        "pos_receiptacc",
                        "ra_id =  '${posReceipt.posReceiptCreditsRaID}'"
                    )
                }

                posReceipt.posReceiptDebitID?.let {
                    SQLServerWrapper.delete(
                        "pos_receipt",
                        "pr_id = '$it'"
                    )
                    SQLServerWrapper.delete(
                        "pos_receiptacc",
                        "ra_id =  '${posReceipt.posReceiptDebitRaID}'"
                    )
                }

                posReceipt.posReceiptDebitsID?.let {
                    SQLServerWrapper.delete(
                        "pos_receipt",
                        "pr_id = '$it'"
                    )
                    SQLServerWrapper.delete(
                        "pos_receiptacc",
                        "ra_id =  '${posReceipt.posReceiptDebitsRaID}'"
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
                val defColumns = getColumns()
                if (!posReceipt.posReceiptCashID.isNullOrEmpty()) {
                    SQLServerWrapper.update(
                        "pos_receipt",
                        defColumns,
                        getValues(
                            posReceipt.posReceiptCashID!!,
                            posReceipt,
                            0,
                            rate
                        ),
                        "pr_id = ${posReceipt.posReceiptCashID!!}"
                    )
                } else if (posReceipt.posReceiptCash > 0.0) {
                    val columns = getColumns()
                    columns.add("pr_ra_id")
                    insertPosReceipt(
                        columns,
                        SettingsModel.currentCurrency,
                        SettingsModel.getCompanyID(),
                        posReceipt,
                        rate,
                        1,
                        0
                    )
                }
                if (!posReceipt.posReceiptCashsID.isNullOrEmpty()) {
                    SQLServerWrapper.update(
                        "pos_receipt",
                        getColumns(),
                        getValues(
                            posReceipt.posReceiptCashsID!!,
                            posReceipt,
                            1,
                            rate
                        ),
                        "pr_id = ${posReceipt.posReceiptCashsID!!}"
                    )
                } else if (posReceipt.posReceiptCashs > 0.0) {
                    val columns = getColumns()
                    columns.add("pr_ra_id")
                    insertPosReceipt(
                        columns,
                        SettingsModel.currentCurrency,
                        SettingsModel.getCompanyID(),
                        posReceipt,
                        rate,
                        2,
                        1
                    )
                }
                if (!posReceipt.posReceiptCreditID.isNullOrEmpty()) {
                    SQLServerWrapper.update(
                        "pos_receipt",
                        getColumns(),
                        getValues(
                            posReceipt.posReceiptCreditID!!,
                            posReceipt,
                            2,
                            rate
                        ),
                        "pr_id = ${posReceipt.posReceiptCreditID!!}"
                    )
                } else if (posReceipt.posReceiptCredit > 0.0) {
                    val columns = getColumns()
                    columns.add("pr_ra_id")
                    insertPosReceipt(
                        columns,
                        SettingsModel.currentCurrency,
                        SettingsModel.getCompanyID(),
                        posReceipt,
                        rate,
                        1,
                        2
                    )
                }
                if (!posReceipt.posReceiptCreditsID.isNullOrEmpty()) {
                    SQLServerWrapper.update(
                        "pos_receipt",
                        getColumns(),
                        getValues(
                            posReceipt.posReceiptCreditsID!!,
                            posReceipt,
                            3,
                            rate
                        ),
                        "pr_id = ${posReceipt.posReceiptCreditsID!!}"
                    )
                } else if (posReceipt.posReceiptCredit > 0.0) {
                    val columns = getColumns()
                    columns.add("pr_ra_id")
                    insertPosReceipt(
                        columns,
                        SettingsModel.currentCurrency,
                        SettingsModel.getCompanyID(),
                        posReceipt,
                        rate,
                        2,
                        3
                    )
                }
                if (!posReceipt.posReceiptDebitID.isNullOrEmpty()) {
                    SQLServerWrapper.update(
                        "pos_receipt",
                        getColumns(),
                        getValues(
                            posReceipt.posReceiptDebitID!!,
                            posReceipt,
                            4,
                            rate
                        ),
                        "pr_id = ${posReceipt.posReceiptDebitID!!}"
                    )
                } else if (posReceipt.posReceiptDebit > 0.0) {
                    val columns = getColumns()
                    columns.add("pr_ra_id")
                    insertPosReceipt(
                        columns,
                        SettingsModel.currentCurrency,
                        SettingsModel.getCompanyID(),
                        posReceipt,
                        rate,
                        1,
                        4
                    )
                }
                if (!posReceipt.posReceiptDebitsID.isNullOrEmpty()) {
                    SQLServerWrapper.update(
                        "pos_receipt",
                        getColumns(),
                        getValues(
                            posReceipt.posReceiptDebitsID!!,
                            posReceipt,
                            5,
                            rate
                        ),
                        "pr_id = ${posReceipt.posReceiptDebitsID!!}"
                    )
                } else if (posReceipt.posReceiptDebits > 0.0) {
                    val columns = getColumns()
                    columns.add("pr_ra_id")
                    insertPosReceipt(
                        columns,
                        SettingsModel.currentCurrency,
                        SettingsModel.getCompanyID(),
                        posReceipt,
                        rate,
                        2,
                        5
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
                                posReceipt.posReceiptTimeStamp = if (timeStamp is Date) timeStamp else DateHelper.getDateFromString(
                                    timeStamp as String,
                                    "yyyy-MM-dd hh:mm:ss.SSS"
                                )
                                posReceipt.posReceiptDateTime = posReceipt.posReceiptTimeStamp!!.time
                                posReceipt.posReceiptUserStamp = it.getStringValue("pr_userstamp")
                            }
                            val raType = it.getStringValue("ra_type")
                            val raOrder = it.getIntValue("ra_order")
                            when (raType) {
                                "Cash" -> {
                                    if (raOrder == 1) {
                                        posReceipt.posReceiptCashID = it.getStringValue("pr_id")
                                        posReceipt.posReceiptCashRaID = it.getStringValue("pr_ra_id")
                                        posReceipt.posReceiptCash = it.getDoubleValue("pr_amt")
                                    } else {
                                        posReceipt.posReceiptCashsID = it.getStringValue("pr_id")
                                        posReceipt.posReceiptCashsRaID = it.getStringValue("pr_ra_id")
                                        posReceipt.posReceiptCashs = it.getDoubleValue("pr_amt")
                                    }
                                }

                                "Credit" -> {
                                    if (raOrder == 1) {
                                        posReceipt.posReceiptCreditID = it.getStringValue("pr_id")
                                        posReceipt.posReceiptCreditRaID = it.getStringValue("pr_ra_id")
                                        posReceipt.posReceiptCredit = it.getDoubleValue("pr_amt")
                                    } else {
                                        posReceipt.posReceiptCreditsID = it.getStringValue("pr_id")
                                        posReceipt.posReceiptCreditsRaID = it.getStringValue("pr_ra_id")
                                        posReceipt.posReceiptCredits = it.getDoubleValue("pr_amt")
                                    }

                                }

                                "Debit" -> {
                                    if (raOrder == 1) {
                                        posReceipt.posReceiptDebitID = it.getStringValue("pr_id")
                                        posReceipt.posReceiptDebitRaID = it.getStringValue("pr_ra_id")
                                        posReceipt.posReceiptDebit = it.getDoubleValue("pr_amt")
                                    } else {
                                        posReceipt.posReceiptDebitsID = it.getStringValue("pr_id")
                                        posReceipt.posReceiptDebitsRaID = it.getStringValue("pr_ra_id")
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

    private fun getColumns(): MutableList<String> {
        return mutableListOf(
            "pr_id",
            "pr_hi_id",
            "pr_amt",
            "pr_amtf",
            "pr_amts",
            "pr_amtinvcurr",
            "pr_timestamp",
            "pr_userstamp",
        )
    }

    private fun getValues(
            id: String,
            posReceipt: PosReceipt,
            index: Int,
            rate: Double
    ): MutableList<Any?> {
        val dateTime = Timestamp.valueOf(
            DateHelper.getDateInFormat(
                Date(),
                "yyyy-MM-dd HH:mm:ss"
            )
        )
        return when (index) {
            0 -> mutableListOf(
                id,
                posReceipt.posReceiptInvoiceId,
                posReceipt.posReceiptCash,
                posReceipt.posReceiptCash,
                POSUtils.formatDouble(posReceipt.posReceiptCash.times(rate)),
                posReceipt.posReceiptCash,
                posReceipt.posReceiptTimeStamp ?: dateTime,
                posReceipt.posReceiptUserStamp,
            )

            1 -> mutableListOf(
                id,
                posReceipt.posReceiptInvoiceId,
                posReceipt.posReceiptCashs,
                posReceipt.posReceiptCashs,
                POSUtils.formatDouble(posReceipt.posReceiptCash.div(rate)),
                posReceipt.posReceiptCashs,
                posReceipt.posReceiptTimeStamp ?: dateTime,
                posReceipt.posReceiptUserStamp,
            )

            2 -> mutableListOf(
                id,
                posReceipt.posReceiptInvoiceId,
                posReceipt.posReceiptCredit,
                posReceipt.posReceiptCredit,
                POSUtils.formatDouble(posReceipt.posReceiptCredit.times(rate)),
                posReceipt.posReceiptCredit,
                posReceipt.posReceiptTimeStamp ?: dateTime,
                posReceipt.posReceiptUserStamp,
            )

            3 -> mutableListOf(
                id,
                posReceipt.posReceiptInvoiceId,
                posReceipt.posReceiptCredits,
                posReceipt.posReceiptCredits,
                POSUtils.formatDouble(posReceipt.posReceiptCredits.div(rate)),
                posReceipt.posReceiptCredits,
                posReceipt.posReceiptTimeStamp ?: dateTime,
                posReceipt.posReceiptUserStamp,
            )

            4 -> mutableListOf(
                id,
                posReceipt.posReceiptInvoiceId,
                posReceipt.posReceiptDebit,
                posReceipt.posReceiptDebit,
                POSUtils.formatDouble(posReceipt.posReceiptDebit.times(rate)),
                posReceipt.posReceiptDebit,
                posReceipt.posReceiptTimeStamp ?: dateTime,
                posReceipt.posReceiptUserStamp,
            )

            else -> mutableListOf(
                id,
                posReceipt.posReceiptInvoiceId,
                posReceipt.posReceiptDebits,
                posReceipt.posReceiptDebits,
                POSUtils.formatDouble(posReceipt.posReceiptDebits.div(rate)),
                posReceipt.posReceiptDebits,
                posReceipt.posReceiptTimeStamp ?: dateTime,
                posReceipt.posReceiptUserStamp,
            )

        }
    }

    private fun getReceiptAccColumns(): List<String> {
        return if (SettingsModel.isSqlServerWebDb) {
            listOf(
                "ra_id",
                "ra_chcode",
                "ra_cur_code",
                "ra_name",
                "ra_order",
                "ra_type",
                "ra_cmp_id"
            )
        } else {
            listOf(
                "ra_id",
                "ra_chcode",
                "ra_cur_code",
                "ra_name",
                "ra_order",
                "ra_type"
            )
        }
    }


    private fun insertPOSReceiptByProcedure(
            posReceiptInvoiceId: String,
            firstValue: String,
            secondValue: String,
            receiptAccId: String
    ){
        val parameters = mutableListOf(
            posReceiptInvoiceId,//pr_hi_id
            receiptAccId,//pr_ra_id
            firstValue,
            firstValue,
            secondValue,
            SettingsModel.currentCompany?.cmp_multibranchcode,
            firstValue,//pr_amtinvcurr
            "null",//pr_note
            "getDate()",//pr_date
            "getDate()",//pr_timestamp
            SettingsModel.currentUser?.userUsername,//pr_userstamp
            0,//pr_commission
        )
        if(SettingsModel.isSqlServerWebDb){
            parameters.add(0,"Newid()")
            parameters.add("null")//pr_denomination
            parameters.add("null")//pr_count
        }else{
            parameters.add(0,"null")
        }
        SQLServerWrapper.executeProcedure(
            "addpos_receipt",
            parameters
        )
    }


}