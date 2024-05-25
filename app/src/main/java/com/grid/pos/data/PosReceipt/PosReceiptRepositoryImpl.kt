package com.grid.pos.data.PosReceipt

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.interfaces.OnResult
import com.grid.pos.model.SettingsModel

class PosReceiptRepositoryImpl(
        private val posReceiptDao: PosReceiptDao
) : PosReceiptRepository {
    override suspend fun insert(
            posReceipt: PosReceipt,
            callback: OnResult?
    ) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("pos_receipt").add(posReceipt.getMap())
                .addOnSuccessListener {
                    posReceipt.posReceiptDocumentId = it.id
                    callback?.onSuccess(posReceipt)
                }.addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        } else {
            posReceiptDao.insert(posReceipt)
            callback?.onSuccess(posReceipt)
        }
    }

    override suspend fun delete(
            posReceipt: PosReceipt,
            callback: OnResult?
    ) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("pos_receipt")
                .document(posReceipt.posReceiptDocumentId!!).delete().addOnSuccessListener {
                    callback?.onSuccess(posReceipt)
                }.addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        } else {
            posReceiptDao.delete(posReceipt)
            callback?.onSuccess(posReceipt)
        }
    }

    override suspend fun update(
            posReceipt: PosReceipt,
            callback: OnResult?
    ) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("pos_receipt")
                .document(posReceipt.posReceiptDocumentId!!).update(posReceipt.getMap())
                .addOnSuccessListener {
                    callback?.onSuccess(posReceipt)
                }.addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        } else {
            posReceiptDao.update(posReceipt)
            callback?.onSuccess(posReceipt)
        }
    }

    override suspend fun getPosReceiptById(id: String): PosReceipt {
        return posReceiptDao.getPosReceiptById(id)
    }

    override suspend fun getAllPosReceipts(callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("pos_receipt").get()
                .addOnSuccessListener { result ->
                    val receipts = mutableListOf<PosReceipt>()
                    if (result.size() > 0) {
                        for (document in result) {
                            val obj = document.toObject(PosReceipt::class.java)
                            if (!obj.posReceiptId.isNullOrEmpty()) {
                                obj.posReceiptDocumentId = document.id
                                receipts.add(obj)
                            }
                        }
                    }
                    callback?.onSuccess(receipts)
                }.addOnFailureListener { exception ->
                    callback?.onFailure(
                        exception.message ?: "Network error! Can't get receipts from remote."
                    )
                }
        } else {
            posReceiptDao.getAllPosReceipts().collect {
                callback?.onSuccess(it)
            }
        }
    }

    override suspend fun getPosReceiptByInvoice(
            invoiceHeaderId: String,
            callback: OnResult?
    ) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("pos_receipt")
                .whereEqualTo("pr_hi_id", invoiceHeaderId).get().addOnSuccessListener { result ->
                    val document = result.documents.firstOrNull()
                    if (document != null) {
                        val obj = document.toObject(PosReceipt::class.java)
                        if (obj != null && !obj.posReceiptId.isNullOrEmpty()) {
                            obj.posReceiptDocumentId = document.id
                            callback?.onSuccess(obj)
                            return@addOnSuccessListener
                        }
                    }
                    callback?.onFailure(
                        "not found."
                    )
                }.addOnFailureListener { exception ->
                    callback?.onFailure(
                        exception.message ?: "Network error! Can't get items from remote."
                    )
                }
        } else {
            callback?.onSuccess(posReceiptDao.getPosReceiptByInvoice(invoiceHeaderId))
        }
    }


}