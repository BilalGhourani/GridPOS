package com.grid.pos.data.PosReceipt

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DateHelper
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
                SQLServerWrapper.insert(
                    "pos_receipt",
                    getColumns(),
                    getValues(posReceipt)
                )
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
                    "pr_id = ${posReceipt.posReceiptId}"
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
                SQLServerWrapper.update(
                    "pos_receipt",
                    getColumns(),
                    getValues(posReceipt),
                    "pr_id = ${posReceipt.posReceiptId}"
                )
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
                val posReceipts: MutableList<PosReceipt> = mutableListOf()
                dbResult.forEach { obj ->
                    posReceipts.add(fillParams(obj))
                }
                return posReceipts[0]
            }
        }
    }

    private fun fillParams(obj: JSONObject): PosReceipt {
        return PosReceipt().apply {
            posReceiptId = obj.optString("pr_id")
            posReceiptInvoiceId = obj.optString("pr_hi_id")
            posReceiptCash = obj.optDouble("pr_amt")
            posReceiptCashs = obj.optDouble("pr_amtf")/*posReceiptCredit = obj.optDouble("pr_amtf")
                        posReceiptCzredits = obj.optDouble("pr_amtf")
                        posReceiptDebit = obj.optDouble("pr_amtf")
                        posReceiptDebits = obj.optDouble("pr_amtf")*/
            val timeStamp = obj.opt("pr_timestamp")
            posReceiptTimeStamp = if (timeStamp is Date) timeStamp else DateHelper.getDateFromString(
                timeStamp as String,
                "yyyy-MM-dd hh:mm:ss.SSS"
            )
            posReceiptDateTime = posReceiptTimeStamp!!.time
            posReceiptUserStamp = obj.optString("pr_userstamp")
        }
    }

    private fun getColumns(): List<String> {
        return listOf(
            "pr_id",
            "pr_hi_id",
            "pr_amt",
            "pr_amtf",
            "pr_timestamp",
            "pr_userstamp",
        )
    }

    private fun getValues(posReceipt: PosReceipt): List<Any?> {
        return listOf(
            posReceipt.posReceiptId,
            posReceipt.posReceiptInvoiceId,
            posReceipt.posReceiptCash,
            posReceipt.posReceiptCashs,
            posReceipt.posReceiptTimeStamp,
            posReceipt.posReceiptUserStamp,
        )
    }


}