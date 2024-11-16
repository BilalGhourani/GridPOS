package com.grid.pos.data.Receipt

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import kotlinx.coroutines.tasks.await

class ReceiptRepositoryImpl(
        private val receiptDao: ReceiptDao
) : ReceiptRepository {
    override suspend fun insert(
            receipt: Receipt
    ): Receipt {
        if (SettingsModel.isConnectedToFireStore()) {
            val docRef = FirebaseFirestore.getInstance().collection("receipt").add(receipt).await()
            receipt.receiptDocumentId = docRef.id
        } else {
            receiptDao.insert(receipt)
        }
        return receipt
    }

    override suspend fun delete(
            receipt: Receipt
    ) {
        if (SettingsModel.isConnectedToFireStore()) {
            receipt.receiptDocumentId?.let {
                FirebaseFirestore.getInstance().collection("receipt").document(it).delete().await()
            }
        } else {
            receiptDao.delete(receipt)
        }
    }

    override suspend fun update(
            receipt: Receipt
    ) {
        if (SettingsModel.isConnectedToFireStore()) {
            receipt.receiptDocumentId?.let {
                FirebaseFirestore.getInstance().collection("receipt").document(it)
                    .update(receipt.getMap()).await()
            }

        } else {
            receiptDao.update(receipt)
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
                        "pay_id",
                        id
                    ).get().await()
                val document = querySnapshot.documents.firstOrNull()
                document?.toObject(Receipt::class.java)
            }

            CONNECTION_TYPE.LOCAL.key -> {
                receiptDao.getReceiptById(id)
            }

            else -> {//CONNECTION_TYPE.SQL_SERVER.key
                val receipts: MutableList<Receipt> = mutableListOf()/*try {
                    val where = "cmp_id='$id'"
                    val dbResult = SQLServerWrapper.getListOf(
                        "company",
                        "",
                        mutableListOf("*"),
                        where
                    )
                    dbResult?.let {
                        while (it.next()) {
                            receipts.add(Company().apply {
                                companyId = it.getStringValue("cmp_id")
                                companyName = it.getStringValue("cmp_name")
                                companyPhone = it.getStringValue("cmp_phone")
                                companyAddress = it.getStringValue("cmp_address")
                                companyTaxRegno = it.getStringValue("cmp_vatregno")
                                companyTax = it.getDoubleValue("cmp_vat")
                                companyCurCodeTax = it.getStringValue("cmp_cur_codetax")
                                companyEmail = it.getStringValue("cmp_email")
                                companyWeb = it.getStringValue("cmp_web")
                                companyLogo = it.getStringValue("cmp_logo")
                                companySS = it.getBooleanValue("cmp_ss")
                                companyCountry = it.getStringValue("cmp_country")
                                companyTax1 = it.getDoubleValue("cmp_tax1")
                                companyTax1Regno = it.getStringValue("cmp_tax1regno")
                                companyTax2 = it.getDoubleValue("cmp_tax2")
                                companyTax2Regno = it.getStringValue("cmp_tax2regno")
                                cmp_multibranchcode = it.getStringValue("cmp_multibranchcode")
                            })
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }*/
                if (receipts.size > 0) receipts[0] else null
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
                val receipts: MutableList<Receipt> = mutableListOf()/*try {
                    val where = "cmp_id='${SettingsModel.getCompanyID()}'"
                    val dbResult = SQLServerWrapper.getListOf(
                        "company",
                        "",
                        mutableListOf("*"),
                        where
                    )
                    dbResult?.let {
                        while (it.next()) {
                            receipts.add(Receipt().apply {
                                companyId = it.getStringValue("cmp_id")
                                companyName = it.getStringValue("cmp_name")
                                companyPhone = it.getStringValue("cmp_phone")
                                companyAddress = it.getStringValue("cmp_address")
                                companyTaxRegno = it.getStringValue("cmp_vatregno")
                                companyTax = it.getDoubleValue("cmp_vat")
                                companyCurCodeTax = it.getStringValue("cmp_cur_codetax")
                                companyUpWithTax = it.getBooleanValue("cmp_upwithtax")
                                companyEmail = it.getStringValue("cmp_email")
                                companyWeb = it.getStringValue("cmp_web")
                                companyLogo = it.getStringValue("cmp_logo")
                                companySS = it.getBooleanValue("cmp_ss")
                                companyCountry = it.getStringValue("cmp_country")
                                companyTax1 = it.getDoubleValue("cmp_tax1")
                                companyTax1Regno = it.getStringValue("cmp_tax1regno")
                                companyTax2 = it.getDoubleValue("cmp_tax2")
                                companyTax2Regno = it.getStringValue("cmp_tax2regno")
                                cmp_multibranchcode = it.getStringValue("cmp_multibranchcode")
                            })
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }*/
                receipts
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
                        "pay_transno",
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
                return null/*val invoiceHeaders: MutableList<InvoiceHeader> = mutableListOf()
                try {
                    val where = "hi_cmp_id='${SettingsModel.getCompanyID()}' AND hi_tt_code = '$type' AND hi_transno IS NOT NULL AND hi_transno <> '' AND hi_transno <> '0'"
                    val dbResult = SQLServerWrapper.getListOf(
                        "in_hinvoice",
                        "TOP 1",
                        mutableListOf("*"),
                        where,
                        "ORDER BY hi_transno DESC"
                    )
                    dbResult?.let {
                        while (it.next()) {
                            invoiceHeaders.add(fillParams(it))
                        }
                        SQLServerWrapper.closeResultSet(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return if (invoiceHeaders.size > 0) invoiceHeaders[0] else null*/
            }
        }
    }
}