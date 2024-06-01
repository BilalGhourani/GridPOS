package com.grid.pos.data.PosPrinter

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.model.SettingsModel
import kotlinx.coroutines.tasks.await

class PosPrinterRepositoryImpl(
        private val posPrinterDao: PosPrinterDao
) : PosPrinterRepository {
    override suspend fun insert(posPrinter: PosPrinter): PosPrinter {
        if (SettingsModel.isConnectedToFireStore()) {
            val docRef = FirebaseFirestore.getInstance().collection("pos_printer")
                .add(posPrinter.getMap()).await()
            posPrinter.posPrinterDocumentId = docRef.id
        } else {
            posPrinterDao.insert(posPrinter)
        }
        return posPrinter
    }

    override suspend fun delete(posPrinter: PosPrinter) {
        if (SettingsModel.isConnectedToFireStore()) {
            posPrinter.posPrinterDocumentId?.let {
                FirebaseFirestore.getInstance().collection("pos_printer")
                    .document(it).delete().await()
            }
        } else {
            posPrinterDao.delete(posPrinter)
        }
    }

    override suspend fun update(posPrinter: PosPrinter) {
        if (SettingsModel.isConnectedToFireStore()) {
            posPrinter.posPrinterDocumentId?.let {
                FirebaseFirestore.getInstance().collection("pos_printer")
                    .document(it).update(posPrinter.getMap()).await()
            }
        } else {
            posPrinterDao.update(posPrinter)
        }
    }

    override suspend fun getPosPrinterById(id: String): PosPrinter {
        return posPrinterDao.getPosPrinterById(id)
    }

    override suspend fun getAllPosPrinters(): MutableList<PosPrinter> {
        if (SettingsModel.isConnectedToFireStore()) {
            val querySnapshot = FirebaseFirestore.getInstance().collection("pos_printer")
                .whereEqualTo(
                    "pp_cmp_id",
                    SettingsModel.companyID
                ).get().await()

            val printers = mutableListOf<PosPrinter>()
            if (querySnapshot.size() > 0) {
                for (document in querySnapshot) {
                    val obj = document.toObject(PosPrinter::class.java)
                    if (obj.posPrinterId.isNotEmpty()) {
                        obj.posPrinterDocumentId = document.id
                        printers.add(obj)
                    }
                }
            }
            return printers

        } else {
            return posPrinterDao.getAllPosPrinters()
        }
    }

}