package com.grid.pos.data.PosReceipt

import com.google.firebase.firestore.FirebaseFirestore
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
                SQLServerWrapper.insert("pos_receipt", getColumns(), getValues(Utils.generateRandomUuidString(),posReceipt,0,rate))
                SQLServerWrapper.insert("pos_receipt", getColumns(), getValues(Utils.generateRandomUuidString(),posReceipt,1,rate))
                SQLServerWrapper.insert("pos_receipt", getColumns(), getValues(Utils.generateRandomUuidString(),posReceipt,2,rate))
                SQLServerWrapper.insert("pos_receipt", getColumns(), getValues(Utils.generateRandomUuidString(),posReceipt,3,rate))
                SQLServerWrapper.insert("pos_receipt", getColumns(), getValues(Utils.generateRandomUuidString(),posReceipt,4,rate))
                SQLServerWrapper.insert("pos_receipt", getColumns(), getValues(Utils.generateRandomUuidString(),posReceipt,5,rate))
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
                SQLServerWrapper.delete("pos_receipt", "pr_id = ${posReceipt.posReceiptCashID}")
                SQLServerWrapper.delete("pos_receipt", "pr_id = ${posReceipt.posReceiptCashsID}")
                SQLServerWrapper.delete("pos_receipt", "pr_id = ${posReceipt.posReceiptCreditID}")
                SQLServerWrapper.delete("pos_receipt", "pr_id = ${posReceipt.posReceiptCreditsID}")
                SQLServerWrapper.delete("pos_receipt", "pr_id = ${posReceipt.posReceiptDebitID}")
                SQLServerWrapper.delete("pos_receipt", "pr_id = ${posReceipt.posReceiptDebitsID}")
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
                if(!posReceipt.posReceiptCashID.isNullOrEmpty()){
                    SQLServerWrapper.update("pos_receipt", getColumns(), getValues(posReceipt.posReceiptCashID!!,posReceipt,0,rate), "pr_id = ${posReceipt.posReceiptCashID!!}")
                }
                if(!posReceipt.posReceiptCashsID.isNullOrEmpty()){
                    SQLServerWrapper.update("pos_receipt", getColumns(), getValues(posReceipt.posReceiptCashsID!!,posReceipt,1,rate), "pr_id = ${posReceipt.posReceiptCashsID!!}")
                }
                if(!posReceipt.posReceiptCreditID.isNullOrEmpty()){
                    SQLServerWrapper.update("pos_receipt", getColumns(), getValues(posReceipt.posReceiptCreditID!!,posReceipt,2,rate), "pr_id = ${posReceipt.posReceiptCreditID!!}")
                }
                if(!posReceipt.posReceiptCreditsID.isNullOrEmpty()){
                    SQLServerWrapper.update("pos_receipt", getColumns(), getValues(posReceipt.posReceiptCreditsID!!,posReceipt,3,rate), "pr_id = ${posReceipt.posReceiptCreditsID!!}")
                }
                if(!posReceipt.posReceiptDebitID.isNullOrEmpty()){
                    SQLServerWrapper.update("pos_receipt", getColumns(), getValues(posReceipt.posReceiptDebitID!!,posReceipt,4,rate), "pr_id = ${posReceipt.posReceiptDebitID!!}")
                }
                if(!posReceipt.posReceiptDebitsID.isNullOrEmpty()){
                    SQLServerWrapper.update("pos_receipt", getColumns(), getValues(posReceipt.posReceiptDebitsID!!,posReceipt,5,rate), "pr_id = ${posReceipt.posReceiptDebitsID!!}")
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
                val where = "pr_hi_id='$invoiceHeaderId'"
                val dbResult = SQLServerWrapper.getListOf(
                    "pos_receipt",
                    mutableListOf("*"),
                    where
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
                        when (index) {
                            0 -> {
                                posReceipt.posReceiptCashID = obj.optString("pr_id")
                                posReceipt.posReceiptCash = obj.optDouble("pr_amt")
                            }

                            1 -> {
                                posReceipt.posReceiptCashsID = obj.optString("pr_id")
                                posReceipt.posReceiptCashs = obj.optDouble("pr_amt")
                            }

                            2 -> {
                                posReceipt.posReceiptCreditID = obj.optString("pr_id")
                                posReceipt.posReceiptCredit = obj.optDouble("pr_amt")
                            }

                            3 -> {
                                posReceipt.posReceiptCreditsID = obj.optString("pr_id")
                                posReceipt.posReceiptCredits = obj.optDouble("pr_amt")
                            }

                            4 -> {
                                posReceipt.posReceiptDebitID = obj.optString("pr_id")
                                posReceipt.posReceiptDebit = obj.optDouble("pr_amt")
                            }

                            5 -> {
                                posReceipt.posReceiptDebitsID = obj.optString("pr_id")
                                posReceipt.posReceiptDebits = obj.optDouble("pr_amt")
                            }
                        }
                    }
                    return posReceipt
                }
                return null
            }
        }
    }

    private fun getColumns(): List<String> {
        return listOf(
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
            id:String,
            posReceipt: PosReceipt,
            index: Int,
            rate : Double
    ): List<Any?> {
        return when (index) {
            0 -> listOf(
                id,
                posReceipt.posReceiptInvoiceId,
                posReceipt.posReceiptCash,
                posReceipt.posReceiptCash,
                posReceipt.posReceiptCash.times(rate),
                posReceipt.posReceiptCash,
                posReceipt.posReceiptTimeStamp,
                posReceipt.posReceiptUserStamp,
            )

            1 -> listOf(
                id,
                posReceipt.posReceiptInvoiceId,
                posReceipt.posReceiptCashs,
                posReceipt.posReceiptCashs,
                posReceipt.posReceiptCash.div(rate),
                posReceipt.posReceiptCashs,
                posReceipt.posReceiptTimeStamp,
                posReceipt.posReceiptUserStamp,
            )

            2 -> listOf(
                id,
                posReceipt.posReceiptInvoiceId,
                posReceipt.posReceiptCredit,
                posReceipt.posReceiptCredit,
                posReceipt.posReceiptCredit.times(rate),
                posReceipt.posReceiptCredit,
                posReceipt.posReceiptTimeStamp,
                posReceipt.posReceiptUserStamp,
            )

            3 -> listOf(
                id,
                posReceipt.posReceiptInvoiceId,
                posReceipt.posReceiptCredits,
                posReceipt.posReceiptCredits,
                posReceipt.posReceiptCredits.div(rate),
                posReceipt.posReceiptCredits,
                posReceipt.posReceiptTimeStamp,
                posReceipt.posReceiptUserStamp,
            )

            4 -> listOf(
                id,
                posReceipt.posReceiptInvoiceId,
                posReceipt.posReceiptDebit,
                posReceipt.posReceiptDebit,
                posReceipt.posReceiptDebit.times(rate),
                posReceipt.posReceiptDebit,
                posReceipt.posReceiptTimeStamp,
                posReceipt.posReceiptUserStamp,
            )

            else -> listOf(
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


}