package com.grid.pos.data.InvoiceHeader

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.grid.pos.model.SettingsModel
import kotlinx.coroutines.tasks.await
import java.util.Date

class InvoiceHeaderRepositoryImpl(
        private val invoiceHeaderDao: InvoiceHeaderDao
) : InvoiceHeaderRepository {
    override suspend fun insert(invoiceHeader: InvoiceHeader): InvoiceHeader {
        if (SettingsModel.isConnectedToFireStore()) {
            val docRef = FirebaseFirestore.getInstance().collection("in_hinvoice")
                .add(invoiceHeader.getMap()).await()
            invoiceHeader.invoiceHeadDocumentId = docRef.id
        } else {
            invoiceHeaderDao.insert(invoiceHeader)
        }
        return invoiceHeader
    }

    override suspend fun delete(
            invoiceHeader: InvoiceHeader
    ) {
        if (SettingsModel.isConnectedToFireStore()) {
            invoiceHeader.invoiceHeadDocumentId?.let {
                FirebaseFirestore.getInstance().collection("in_hinvoice")
                    .document(it).delete().await()
            }
        } else {
            invoiceHeaderDao.delete(invoiceHeader)
        }
    }

    override suspend fun update(
            invoiceHeader: InvoiceHeader
    ) {
        if (SettingsModel.isConnectedToFireStore()) {
            invoiceHeader.invoiceHeadDocumentId?.let {
                FirebaseFirestore.getInstance().collection("in_hinvoice")
                    .document(it).update(invoiceHeader.getMap())
                    .await()
            }
        } else {
            invoiceHeaderDao.update(invoiceHeader)
        }
    }

    override suspend fun getAllInvoiceHeaders(): MutableList<InvoiceHeader> {
        if (SettingsModel.isConnectedToFireStore()) {
            val querySnapshot = FirebaseFirestore.getInstance().collection("in_hinvoice")
                .whereEqualTo(
                    "hi_cmp_id",
                    SettingsModel.getCompanyID()
                ).get().await()
            val invoices = mutableListOf<InvoiceHeader>()
            if (querySnapshot.size() > 0) {
                for (document in querySnapshot) {
                    val obj = document.toObject(InvoiceHeader::class.java)
                    if (obj.invoiceHeadId.isNotEmpty()) {
                        obj.invoiceHeadDocumentId = document.id
                        invoices.add(obj)
                    }
                }
            }
            return invoices
        } else {
            return invoiceHeaderDao.getAllInvoiceHeaders(SettingsModel.getCompanyID() ?: "")
        }
    }

    override suspend fun getLastInvoiceByType(
            type: String
    ): InvoiceHeader? {
        if (SettingsModel.isConnectedToFireStore()) {
            val querySnapshot = FirebaseFirestore.getInstance().collection("in_hinvoice")
                .whereEqualTo(
                    "hi_cmp_id",
                    SettingsModel.getCompanyID()
                ).whereEqualTo(
                    "hi_tt_code",
                    type
                ).whereNotEqualTo(
                    "hi_orderno",
                    null
                ).orderBy(
                    "hi_orderno",
                    Query.Direction.DESCENDING
                ).limit(1).get().await()
            val document = querySnapshot.firstOrNull()
            if (document != null) {
                val obj = document.toObject(InvoiceHeader::class.java)
                return obj
            }
        } else {
            return invoiceHeaderDao.getLastInvoiceNo(type,SettingsModel.getCompanyID() ?: "")
        }
        return null
    }

    override suspend fun getInvoiceByTable(
            tableNo: String
    ): InvoiceHeader? {
        if (SettingsModel.isConnectedToFireStore()) {
            val querySnapshot = FirebaseFirestore.getInstance().collection("in_hinvoice")
                .whereEqualTo(
                    "hi_cmp_id",
                    SettingsModel.getCompanyID()
                ).whereEqualTo(
                    "hi_ta_name",
                    tableNo
                ).whereEqualTo(
                    "hi_transno",
                    null
                ).limit(1).get().await()
            val document = querySnapshot.firstOrNull()
            if (document != null) {
                val obj = document.toObject(InvoiceHeader::class.java)
                return obj
            }
            return null
        } else {
            return invoiceHeaderDao.getInvoiceByTable(tableNo,SettingsModel.getCompanyID() ?: "") ?: InvoiceHeader()
        }
    }

    override suspend fun getInvoicesBetween(
            from: Date,
            to: Date
    ): MutableList<InvoiceHeader> {
        if (SettingsModel.isConnectedToFireStore()) {
            val querySnapshot = FirebaseFirestore.getInstance().collection("in_hinvoice")
                .whereEqualTo(
                    "hi_cmp_id",
                    SettingsModel.getCompanyID()
                )/*  .whereGreaterThanOrEqualTo(
                      "hi_timestamp",
                      from
                  ).whereLessThan(
                      "hi_timestamp",
                      to
                  )*/.get().await()
            val invoices = mutableListOf<InvoiceHeader>()
            if (querySnapshot.size() > 0) {
                for (document in querySnapshot) {
                    val obj = document.toObject(InvoiceHeader::class.java)
                    if (obj.invoiceHeadId.isNotEmpty()) {
                        obj.invoiceHeadDocumentId = document.id
                        invoices.add(obj)
                    }
                }
            }
            return invoices
        } else {
            return invoiceHeaderDao.getInvoicesBetween(
                from.time * 1000,
                to.time * 1000,
                SettingsModel.getCompanyID() ?: ""
            )
        }
    }
}