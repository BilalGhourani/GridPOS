package com.grid.pos.data.Invoice

import androidx.lifecycle.asLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.grid.pos.data.Family.Family
import com.grid.pos.interfaces.OnResult
import com.grid.pos.model.SettingsModel
import java.util.Date

class InvoiceRepositoryImpl(
        private val invoiceDao: InvoiceDao
) : InvoiceRepository {
    override suspend fun insert(
            invoice: Invoice,
            callback: OnResult?
    ) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("in_invoice").add(invoice)
                .addOnSuccessListener {
                    invoice.invoiceDocumentId = it.id
                    callback?.onSuccess(invoice)
                }.addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        } else {
            invoiceDao.insert(invoice)
            callback?.onSuccess(invoice)
        }
    }

    override suspend fun delete(
            invoice: Invoice,
            callback: OnResult?
    ) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("in_invoice")
                .document(invoice.invoiceDocumentId!!).delete().addOnSuccessListener {
                    callback?.onSuccess(invoice)
                }.addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        } else {
            invoiceDao.delete(invoice)
            callback?.onSuccess(invoice)
        }
    }

    override suspend fun update(
            invoice: Invoice,
            callback: OnResult?
    ) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("in_invoice")
                .document(invoice.invoiceDocumentId!!).update(invoice.getMap())
                .addOnSuccessListener {
                    callback?.onSuccess(invoice)
                }.addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        } else {
            invoiceDao.update(invoice)
            callback?.onSuccess(invoice)
        }
    }

    override suspend fun getInvoiceById(id: String): Invoice {
        return invoiceDao.getInvoiceById(id)
    }

    override suspend fun getAllInvoices(
            invoiceHeaderId: String,
            callback: OnResult?
    ) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("in_invoice").whereEqualTo(
                    "in_hi_id",
                    invoiceHeaderId
                ).get().addOnSuccessListener { result ->
                    val invoiceItems = mutableListOf<Invoice>()
                    if (result.size() > 0) {
                        for (document in result) {
                            val obj = document.toObject(Invoice::class.java)
                            if (!obj.invoiceId.isNullOrEmpty()) {
                                obj.invoiceDocumentId = document.id
                                invoiceItems.add(obj)
                            }
                        }
                        callback?.onSuccess(invoiceItems)
                    }
                }.addOnFailureListener { exception ->
                    callback?.onFailure(
                        exception.message ?: "Network error! Can't get items from remote."
                    )
                }
        } else {
            invoiceDao.getAllInvoiceItems(invoiceHeaderId).collect {
                callback?.onSuccess(it)
            }
        }
    }

    override suspend fun getInvoicesBetween(
            from: Date,
            to: Date,
            callback: OnResult?
    ) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("in_invoice").whereGreaterThanOrEqualTo(
                    "in_timestamp",
                    from
                ).whereLessThan(
                    "in_timestamp",
                    to
                ).get().addOnSuccessListener { result ->
                    val invoiceItems = mutableListOf<Invoice>()
                    if (result.size() > 0) {
                        for (document in result) {
                            val obj = document.toObject(Invoice::class.java)
                            if (!obj.invoiceId.isNullOrEmpty()) {
                                obj.invoiceDocumentId = document.id
                                invoiceItems.add(obj)
                            }
                        }
                        callback?.onSuccess(invoiceItems)
                    }
                }.addOnFailureListener { exception ->
                    callback?.onFailure(
                        exception.message ?: "Network error! Can't get items from remote."
                    )
                }
        } else {
            invoiceDao.getInvoicesBetween(
                from.time * 1000,
                to.time * 1000
            ).collect {
                callback?.onSuccess(it)
            }
        }
    }
}