package com.grid.pos.data.Invoice

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.model.SettingsModel
import kotlinx.coroutines.tasks.await

class InvoiceRepositoryImpl(
        private val invoiceDao: InvoiceDao
) : InvoiceRepository {
    override suspend fun insert(
            invoice: Invoice
    ) :Invoice{
        if (SettingsModel.isConnectedToFireStore()) {
          val docRef=  FirebaseFirestore.getInstance().collection("in_invoice").add(invoice.getMap())
                .await()
            invoice.invoiceDocumentId = docRef.id
        } else {
            invoiceDao.insert(invoice)
        }
        return invoice
    }

    override suspend fun delete(
            invoice: Invoice
    ) {
        if (SettingsModel.isConnectedToFireStore()) {
            invoice.invoiceDocumentId?.let {
                FirebaseFirestore.getInstance().collection("in_invoice")
                    .document(it).delete().
                    await()
            }
        } else {
            invoiceDao.delete(invoice)
        }
    }

    override suspend fun update(
            invoice: Invoice
    ) {
        if (SettingsModel.isConnectedToFireStore()) {
            invoice.invoiceDocumentId?.let {
                FirebaseFirestore.getInstance().collection("in_invoice")
                    .document(it).update(invoice.getMap())
                    .await()
            }
        } else {
            invoiceDao.update(invoice)
        }
    }

    override suspend fun getAllInvoices(
            invoiceHeaderId: String
    ):MutableList<Invoice> {
        if (SettingsModel.isConnectedToFireStore()) {
          val querySnapshot =  FirebaseFirestore.getInstance().collection("in_invoice").whereEqualTo(
                "in_hi_id",
                invoiceHeaderId
            ).get().await()
                val invoiceItems = mutableListOf<Invoice>()
                if (querySnapshot.size() > 0) {
                    for (document in querySnapshot) {
                        val obj = document.toObject(Invoice::class.java)
                        if (obj.invoiceId.isNotEmpty()) {
                            obj.invoiceDocumentId = document.id
                            invoiceItems.add(obj)
                        }
                    }
                }
             return invoiceItems
        } else {
            return invoiceDao.getAllInvoiceItems(invoiceHeaderId)
        }
    }

    override suspend fun getInvoicesByIds(
            ids: List<String>
    ) :MutableList<Invoice>{
        if (SettingsModel.isConnectedToFireStore()) {
            val querySnapshot =  FirebaseFirestore.getInstance().collection("in_invoice").whereIn(
                    "in_hi_id",
                    ids
                ).get().await()
                    val invoiceItems = mutableListOf<Invoice>()
                    if (querySnapshot.size() > 0) {
                        for (document in querySnapshot) {
                            val obj = document.toObject(Invoice::class.java)
                            if (obj.invoiceId.isNotEmpty()) {
                                obj.invoiceDocumentId = document.id
                                invoiceItems.add(obj)
                            }
                        }
                    }
                return invoiceItems
        } else {
            return invoiceDao.getInvoicesByIds(ids)
        }
    }
}