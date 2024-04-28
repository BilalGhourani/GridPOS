package com.grid.pos.data.Invoice

import androidx.lifecycle.asLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.interfaces.OnResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InvoiceRepositoryImpl(
    private val invoiceDao: InvoiceDao
) : InvoiceRepository {
    override suspend fun insert(invoice: Invoice, callback: OnResult?) {
        FirebaseFirestore.getInstance().collection("in_invoice")
            .add(invoice)
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    invoiceDao.insert(invoice)
                    invoice.invoiceDocumentId = it.id
                    callback?.onSuccess(invoice)
                }
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }
    }

    override suspend fun delete(invoice: Invoice, callback: OnResult?) {
        FirebaseFirestore.getInstance().collection("in_invoice")
            .document(invoice.invoiceDocumentId!!)
            .delete()
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    invoiceDao.delete(invoice)
                    callback?.onSuccess(invoice)
                }
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }
    }

    override suspend fun update(invoice: Invoice, callback: OnResult?) {
        FirebaseFirestore.getInstance().collection("in_invoice")
            .document(invoice.invoiceDocumentId!!)
            .update(invoice.getMap())
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    invoiceDao.update(invoice)
                    callback?.onSuccess(invoice)
                }
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }
    }

    override suspend fun getInvoiceById(id: String): Invoice {
        return invoiceDao.getInvoiceById(id)
    }

    override suspend fun getAllInvoices(invoiceHeaderId: String, callback: OnResult?) {
        val localInvoiceItems = invoiceDao.getAllInvoiceItems(invoiceHeaderId).asLiveData().value
        if (!localInvoiceItems.isNullOrEmpty()) {
            callback?.onSuccess(localInvoiceItems)
        }
        FirebaseFirestore.getInstance().collection("in_invoice").get()
            .addOnSuccessListener { result ->
                CoroutineScope(Dispatchers.IO).launch {
                    val invoiceItems = mutableListOf<Invoice>()
                    invoiceDao.deleteAll()
                    if (result.size() > 0) {
                        for (document in result) {
                            val obj = document.toObject(Invoice::class.java)
                            if (!obj.invoiceId.isNullOrEmpty()) {
                                obj.invoiceDocumentId = document.id
                                invoiceItems.add(obj)
                            }
                        }
                        invoiceDao.insertAll(invoiceItems.toList())
                    }
                    callback?.onSuccess(invoiceItems)
                }
            }.addOnFailureListener { exception ->
                callback?.onFailure(
                    exception.message ?: "Network error! Can't get items from remote."
                )
            }
    }
}