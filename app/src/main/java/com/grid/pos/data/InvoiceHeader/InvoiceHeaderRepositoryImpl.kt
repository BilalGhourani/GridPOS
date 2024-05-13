package com.grid.pos.data.InvoiceHeader

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.grid.pos.interfaces.OnResult
import com.grid.pos.model.SettingsModel

class InvoiceHeaderRepositoryImpl(
    private val invoiceHeaderDao: InvoiceHeaderDao
) : InvoiceHeaderRepository {
    override suspend fun insert(invoiceHeader: InvoiceHeader, callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("in_hinvoice")
                .add(invoiceHeader)
                .addOnSuccessListener {
                    invoiceHeader.invoiceHeadDocumentId = it.id
                    callback?.onSuccess(invoiceHeader)
                }
                .addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        } else {
            invoiceHeaderDao.insert(invoiceHeader)
            callback?.onSuccess(invoiceHeader)
        }
    }

    override suspend fun delete(invoiceHeader: InvoiceHeader, callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("in_hinvoice")
                .document(invoiceHeader.invoiceHeadDocumentId!!)
                .delete()
                .addOnSuccessListener {
                    callback?.onSuccess(invoiceHeader)
                }
                .addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        } else {
            invoiceHeaderDao.delete(invoiceHeader)
            callback?.onSuccess(invoiceHeader)
        }
    }

    override suspend fun update(invoiceHeader: InvoiceHeader, callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("in_hinvoice")
                .document(invoiceHeader.invoiceHeadDocumentId!!)
                .update(invoiceHeader.getMap())
                .addOnSuccessListener {
                    callback?.onSuccess(invoiceHeader)
                }
                .addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        } else {
            invoiceHeaderDao.update(invoiceHeader)
            callback?.onSuccess(invoiceHeader)
        }
    }

    override suspend fun getInvoiceHeaderById(id: String): InvoiceHeader {
        return invoiceHeaderDao.getInvoiceHeaderById(id)
    }

    override suspend fun getAllInvoiceHeaders(callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("in_hinvoice")
                .whereEqualTo("hi_cmp_id", SettingsModel.companyID)
                .get()
                .addOnSuccessListener { result ->
                    val invoices = mutableListOf<InvoiceHeader>()
                    if (result.size() > 0) {
                        for (document in result) {
                            val obj = document.toObject(InvoiceHeader::class.java)
                            if (!obj.invoiceHeadId.isNullOrEmpty()) {
                                obj.invoiceHeadDocumentId = document.id
                                invoices.add(obj)
                            }
                        }
                    }
                    callback?.onSuccess(invoices)
                }.addOnFailureListener { exception ->
                    callback?.onFailure(
                        exception.message ?: "Network error! Can't get items from remote."
                    )
                }
        } else {
            invoiceHeaderDao.getAllInvoiceHeaders().collect {
                callback?.onSuccess(it)
            }
        }
    }

    override suspend fun getLastInvoiceTransNo(type: String, callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("in_hinvoice")
                .whereEqualTo("hi_cmp_id", SettingsModel.companyID)
                .whereEqualTo("hi_tt_code", type)
                .whereNotEqualTo("hi_orderno", null)
                .orderBy("hi_orderno", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { result ->
                    val document = result.firstOrNull()
                    if (document != null) {
                        val obj = document.toObject(InvoiceHeader::class.java)
                        callback?.onSuccess(obj)
                    }

                }.addOnFailureListener { exception ->
                    callback?.onFailure(
                        exception.message ?: "Network error."
                    )
                }

        } else {
            invoiceHeaderDao.getLastInvoiceNo(type).collect {
                callback?.onSuccess(it)
            }
        }
    }
}