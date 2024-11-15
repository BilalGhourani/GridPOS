package com.grid.pos.data.Payment

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import kotlinx.coroutines.tasks.await

class PaymentRepositoryImpl(
        private val paymentDao: PaymentDao
) : PaymentRepository {
    override suspend fun insert(
            payment: Payment
    ): Payment {
        if (SettingsModel.isConnectedToFireStore()) {
            val docRef = FirebaseFirestore.getInstance().collection("payment").add(payment).await()
            payment.paymentDocumentId = docRef.id
        } else {
            paymentDao.insert(payment)
        }
        return payment
    }

    override suspend fun delete(
            payment: Payment
    ) {
        if (SettingsModel.isConnectedToFireStore()) {
            payment.paymentDocumentId?.let {
                FirebaseFirestore.getInstance().collection("payment").document(it).delete().await()
            }
        } else {
            paymentDao.delete(payment)
        }
    }

    override suspend fun update(
            payment: Payment
    ) {
        if (SettingsModel.isConnectedToFireStore()) {
            payment.paymentDocumentId?.let {
                FirebaseFirestore.getInstance().collection("payment").document(it)
                    .update(payment.getMap()).await()
            }

        } else {
            paymentDao.update(payment)
        }
    }

    override suspend fun getPaymentById(
            id: String
    ): Payment? {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("payment")
                    .whereEqualTo(
                        "pay_id",
                        id
                    ).get().await()
                val document = querySnapshot.documents.firstOrNull()
                document?.toObject(Payment::class.java)
            }

            CONNECTION_TYPE.LOCAL.key -> {
                paymentDao.getPaymentById(id)
            }

            else -> {//CONNECTION_TYPE.SQL_SERVER.key
                val payments: MutableList<Payment> = mutableListOf()
                /*try {
                    val where = "cmp_id='$id'"
                    val dbResult = SQLServerWrapper.getListOf(
                        "company",
                        "",
                        mutableListOf("*"),
                        where
                    )
                    dbResult?.let {
                        while (it.next()) {
                            payments.add(Company().apply {
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
                if (payments.size > 0) payments[0] else null
            }
        }
    }

    override suspend fun getAllPayments(): MutableList<Payment> {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("payment").get()
                    .await()
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
                companies
            }

            CONNECTION_TYPE.LOCAL.key -> {
                paymentDao.getAllPayments()
            }

            else -> {//CONNECTION_TYPE.SQL_SERVER.key
                val companies: MutableList<Payment> = mutableListOf()
                /*try {
                    val where = "cmp_id='${SettingsModel.getCompanyID()}'"
                    val dbResult = SQLServerWrapper.getListOf(
                        "company",
                        "",
                        mutableListOf("*"),
                        where
                    )
                    dbResult?.let {
                        while (it.next()) {
                            companies.add(Payment().apply {
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
                companies
            }
        }
    }
}