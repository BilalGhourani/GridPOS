package com.grid.pos.data.PosReceipt

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.model.SettingsModel
import kotlinx.coroutines.tasks.await

class PosReceiptRepositoryImpl(
        private val posReceiptDao: PosReceiptDao
) : PosReceiptRepository {
    override suspend fun insert(
            posReceipt: PosReceipt
    ): PosReceipt {
        if (SettingsModel.isConnectedToFireStore()) {
            val docRef = FirebaseFirestore.getInstance().collection("pos_receipt")
                .add(posReceipt.getMap()).await()
            posReceipt.posReceiptDocumentId = docRef.id
        } else {
            posReceiptDao.insert(posReceipt)
        }
        return posReceipt
    }

    override suspend fun delete(
            posReceipt: PosReceipt
    ) {
        if (SettingsModel.isConnectedToFireStore()) {
            posReceipt.posReceiptDocumentId?.let {
                FirebaseFirestore.getInstance().collection("pos_receipt").document(it).delete()
                    .await()
            }
        } else {
            posReceiptDao.delete(posReceipt)
        }
    }

    override suspend fun update(
            posReceipt: PosReceipt
    ) {
        if (SettingsModel.isConnectedToFireStore()) {
            posReceipt.posReceiptDocumentId?.let {
                FirebaseFirestore.getInstance().collection("pos_receipt").document(it)
                    .update(posReceipt.getMap()).await()
            }
        } else {
            posReceiptDao.update(posReceipt)
        }
    }

    override suspend fun getPosReceiptById(id: String): PosReceipt {
        return posReceiptDao.getPosReceiptById(id)
    }

    override suspend fun getAllPosReceipts(): MutableList<PosReceipt> {
        if (SettingsModel.isConnectedToFireStore()) {
            val querySnapshot = FirebaseFirestore.getInstance().collection("pos_receipt").get()
                .await()
            val receipts = mutableListOf<PosReceipt>()
            if (querySnapshot.size() > 0) {
                for (document in querySnapshot) {
                    val obj = document.toObject(PosReceipt::class.java)
                    if (!obj.posReceiptId.isNullOrEmpty()) {
                        obj.posReceiptDocumentId = document.id
                        receipts.add(obj)
                    }
                }
            }
            return receipts
        } else {
            return posReceiptDao.getAllPosReceipts()
        }
    }

    override suspend fun getPosReceiptByInvoice(
            invoiceHeaderId: String
    ): PosReceipt? {
        if (SettingsModel.isConnectedToFireStore()) {
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
        } else {
            return posReceiptDao.getPosReceiptByInvoice(invoiceHeaderId)
        }
    }


}