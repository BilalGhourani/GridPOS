package com.grid.pos.data.InvoiceHeader

import androidx.lifecycle.asLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.interfaces.OnResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InvoiceHeaderRepositoryImpl(
    private val invoiceHeaderDao: InvoiceHeaderDao
) : InvoiceHeaderRepository {
    override suspend fun insert(invoiceHeader: InvoiceHeader, callback: OnResult?) {
        FirebaseFirestore.getInstance().collection("in_hinvoice")
            .add(invoiceHeader)
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    invoiceHeaderDao.insert(invoiceHeader)
                    invoiceHeader.invoiceHeadDocumentId = it.id
                    callback?.onSuccess(invoiceHeader)
                }
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }
    }

    override suspend fun delete(invoiceHeader: InvoiceHeader, callback: OnResult?) {
        FirebaseFirestore.getInstance().collection("in_hinvoice")
            .document(invoiceHeader.invoiceHeadDocumentId!!)
            .delete()
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    invoiceHeaderDao.delete(invoiceHeader)
                    callback?.onSuccess(invoiceHeader)
                }
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }
    }

    override suspend fun update(invoiceHeader: InvoiceHeader, callback: OnResult?) {
        FirebaseFirestore.getInstance().collection("in_hinvoice")
            .document(invoiceHeader.invoiceHeadDocumentId!!)
            .update(invoiceHeader.getMap())
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    invoiceHeaderDao.update(invoiceHeader)
                    callback?.onSuccess(invoiceHeader)
                }
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }
    }

    override suspend fun getInvoiceHeaderById(id: String): InvoiceHeader {
        return invoiceHeaderDao.getInvoiceHeaderById(id)
    }

    override fun getAllInvoiceHeaders(callback: OnResult?) {
        val localInvoices = invoiceHeaderDao.getAllInvoiceHeaders().asLiveData().value
        if (!localInvoices.isNullOrEmpty()) {
            callback?.onSuccess(localInvoices)
        }
        FirebaseFirestore.getInstance().collection("in_hinvoice").get()
            .addOnSuccessListener { result ->
                CoroutineScope(Dispatchers.IO).launch {
                    val invoices = mutableListOf<InvoiceHeader>()
                    invoiceHeaderDao.deleteAll()
                    if (result.size() > 0) {
                        for (document in result) {
                            val obj = document.toObject(InvoiceHeader::class.java)
                            if (!obj.invoiceHeadId.isNullOrEmpty()) {
                                obj.invoiceHeadDocumentId = document.id
                                invoices.add(obj)
                            }
                        }
                        invoiceHeaderDao.insertAll(invoices.toList())
                    }
                    callback?.onSuccess(invoices)
                }
            }.addOnFailureListener { exception ->
                callback?.onFailure(
                    exception.message ?: "Network error! Can't get items from remote."
                )
            }
    }
}