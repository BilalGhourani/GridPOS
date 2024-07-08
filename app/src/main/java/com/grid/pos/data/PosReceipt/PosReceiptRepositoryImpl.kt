package com.grid.pos.data.PosReceipt

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Utils
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
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
                val currency = SettingsModel.currentCurrency
                val companyId = SettingsModel.getCompanyID()
                val columns = getColumns()
                columns.add("pr_ra_id")
                if (posReceipt.posReceiptCash > 0.0) {
                    insertPosReceipt(
                        columns,
                        currency,
                        companyId,
                        posReceipt,
                        rate,
                        1,
                        0
                    )
                }

                if (posReceipt.posReceiptCashs > 0.0) {
                    insertPosReceipt(
                        columns,
                        currency,
                        companyId,
                        posReceipt,
                        rate,
                        2,
                        1
                    )
                }

                if (posReceipt.posReceiptCredit > 0.0) {
                    insertPosReceipt(
                        columns,
                        currency,
                        companyId,
                        posReceipt,
                        rate,
                        1,
                        2
                    )
                }

                if (posReceipt.posReceiptCredits > 0.0) {
                    insertPosReceipt(
                        columns,
                        currency,
                        companyId,
                        posReceipt,
                        rate,
                        2,
                        3
                    )
                }

                if (posReceipt.posReceiptDebit > 0.0) {
                    insertPosReceipt(
                        columns,
                        currency,
                        companyId,
                        posReceipt,
                        rate,
                        1,
                        4
                    )
                }

                if (posReceipt.posReceiptDebits > 0.0) {
                    insertPosReceipt(
                        columns,
                        currency,
                        companyId,
                        posReceipt,
                        rate,
                        2,
                        5
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
                SQLServerWrapper.delete(
                    "pos_receipt",
                    "pr_id = ${posReceipt.posReceiptCashID}"
                )
                SQLServerWrapper.delete(
                    "pos_receipt",
                    "pr_id = ${posReceipt.posReceiptCashsID}"
                )
                SQLServerWrapper.delete(
                    "pos_receipt",
                    "pr_id = ${posReceipt.posReceiptCreditID}"
                )
                SQLServerWrapper.delete(
                    "pos_receipt",
                    "pr_id = ${posReceipt.posReceiptCreditsID}"
                )
                SQLServerWrapper.delete(
                    "pos_receipt",
                    "pr_id = ${posReceipt.posReceiptDebitID}"
                )
                SQLServerWrapper.delete(
                    "pos_receipt",
                    "pr_id = ${posReceipt.posReceiptDebitsID}"
                )
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
                    if (obj != null && !obj.posReceiptId.isNullOrEmpty()) {
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
                val where = "pr_hi_id='$invoiceHeaderId'  ORDER BY pra.ra_order ASC"
                val dbResult = SQLServerWrapper.getListOf(
                    "pos_receipt",
                    mutableListOf("*"),
                    where,
                    " INNER JOIN pos_receiptacc on pr_ra_id = ra_id"
                )
                if (dbResult.isNotEmpty()) {
                    val posReceipt = PosReceipt()
                    var commonAreFilled = false
                    dbResult.forEachIndexed { index, obj ->
                        if (!commonAreFilled) {
                            commonAreFilled = true
                            posReceipt.posReceiptId = obj.optString("pr_id")
                            posReceipt.posReceiptInvoiceId = obj.optString("pr_hi_id")

                            val timeStamp = obj.opt("pr_timestamp")
                            posReceipt.posReceiptTimeStamp = if (timeStamp is Date) timeStamp else DateHelper.getDateFromString(
                                timeStamp as String,
                                "yyyy-MM-dd hh:mm:ss.SSS"
                            )
                            posReceipt.posReceiptDateTime = posReceipt.posReceiptTimeStamp!!.time
                            posReceipt.posReceiptUserStamp = obj.optString("pr_userstamp")
                        }
                        val ra_type = obj.optString("ra_type")
                        val ra_order = obj.optInt("ra_order")
                        when (ra_type) {
                            "Cash" -> {
                                if (ra_order == 1) {
                                    posReceipt.posReceiptCashID = obj.optString("pr_id")
                                    posReceipt.posReceiptCash = obj.optDouble("pr_amt")
                                } else {
                                    posReceipt.posReceiptCashsID = obj.optString("pr_id")
                                    posReceipt.posReceiptCashs = obj.optDouble("pr_amt")
                                }
                            }

                            "Credit" -> {
                                if (ra_order == 1) {
                                    posReceipt.posReceiptCreditID = obj.optString("pr_id")
                                    posReceipt.posReceiptCredit = obj.optDouble("pr_amt")
                                } else {
                                    posReceipt.posReceiptCreditsID = obj.optString("pr_id")
                                    posReceipt.posReceiptCredits = obj.optDouble("pr_amt")
                                }

                            }

                            "Debit" -> {
                                if (ra_order == 1) {
                                    posReceipt.posReceiptDebitID = obj.optString("pr_id")
                                    posReceipt.posReceiptDebit = obj.optDouble("pr_amt")
                                } else {
                                    posReceipt.posReceiptDebitsID = obj.optString("pr_id")
                                    posReceipt.posReceiptDebits = obj.optDouble("pr_amt")
                                }

                            }
                        }
                    }
                    return posReceipt
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
        return when (index) {
            0 -> mutableListOf(
                id,
                posReceipt.posReceiptInvoiceId,
                posReceipt.posReceiptCash,
                posReceipt.posReceiptCash,
                posReceipt.posReceiptCash.times(rate),
                posReceipt.posReceiptCash,
                posReceipt.posReceiptTimeStamp,
                posReceipt.posReceiptUserStamp,
            )

            1 -> mutableListOf(
                id,
                posReceipt.posReceiptInvoiceId,
                posReceipt.posReceiptCashs,
                posReceipt.posReceiptCashs,
                posReceipt.posReceiptCash.div(rate),
                posReceipt.posReceiptCashs,
                posReceipt.posReceiptTimeStamp,
                posReceipt.posReceiptUserStamp,
            )

            2 -> mutableListOf(
                id,
                posReceipt.posReceiptInvoiceId,
                posReceipt.posReceiptCredit,
                posReceipt.posReceiptCredit,
                posReceipt.posReceiptCredit.times(rate),
                posReceipt.posReceiptCredit,
                posReceipt.posReceiptTimeStamp,
                posReceipt.posReceiptUserStamp,
            )

            3 -> mutableListOf(
                id,
                posReceipt.posReceiptInvoiceId,
                posReceipt.posReceiptCredits,
                posReceipt.posReceiptCredits,
                posReceipt.posReceiptCredits.div(rate),
                posReceipt.posReceiptCredits,
                posReceipt.posReceiptTimeStamp,
                posReceipt.posReceiptUserStamp,
            )

            4 -> mutableListOf(
                id,
                posReceipt.posReceiptInvoiceId,
                posReceipt.posReceiptDebit,
                posReceipt.posReceiptDebit,
                posReceipt.posReceiptDebit.times(rate),
                posReceipt.posReceiptDebit,
                posReceipt.posReceiptTimeStamp,
                posReceipt.posReceiptUserStamp,
            )

            else -> mutableListOf(
                id,
                posReceipt.posReceiptInvoiceId,
                posReceipt.posReceiptDebits,
                posReceipt.posReceiptDebits,
                posReceipt.posReceiptDebits.div(rate),
                posReceipt.posReceiptDebits,
                posReceipt.posReceiptTimeStamp,
                posReceipt.posReceiptUserStamp,
            )

        }
    }

    private fun getReceiptAccColumns(): List<String> {
        return listOf(
            "ra_id",
            "ra_cur_code",
            "ra_name",
            "ra_order",
            "ra_type",
            "ra_cmp_id"
        )
    }

    private fun insertPosReceipt(
            columns: MutableList<String>,
            currency: Currency?,
            companyId: String?,
            posReceipt: PosReceipt,
            rate: Double,
            order: Int,
            index: Int
    ) {
        val pr_id = Utils.generateRandomUuidString()
        val ra_id_cash1 = Utils.generateRandomUuidString()
        SQLServerWrapper.insert(
            "pos_receiptacc",
            getReceiptAccColumns(),
            listOf(
                ra_id_cash1,
                currency?.currencyId,
                "Cash $order",
                order,
                "Cash",
                companyId
            )
        )
        val values = getValues(
            pr_id,
            posReceipt,
            index,
            rate
        )
        values.add(ra_id_cash1)
        SQLServerWrapper.insert(
            "pos_receipt",
            columns,
            values
        )
    }


}