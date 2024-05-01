package com.grid.pos.data.PosPrinter

import androidx.lifecycle.asLiveData
import com.grid.pos.interfaces.OnResult
import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.model.SettingsModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PosPrinterRepositoryImpl(
    private val posPrinterDao: PosPrinterDao
) : PosPrinterRepository {
    override suspend fun insert(posPrinter: PosPrinter, callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("pos_printer")
                .add(posPrinter)
                .addOnSuccessListener {
                    posPrinter.posPrinterDocumentId = it.id
                    callback?.onSuccess(posPrinter)
                }
                .addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        } else {
            posPrinterDao.insert(posPrinter)
            callback?.onSuccess(posPrinter)
        }
    }

    override suspend fun delete(posPrinter: PosPrinter, callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("pos_printer")
                .document(posPrinter.posPrinterDocumentId!!)
                .delete()
                .addOnSuccessListener {
                    callback?.onSuccess(posPrinter)
                }
                .addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        } else {
            posPrinterDao.delete(posPrinter)
            callback?.onSuccess(posPrinter)
        }
    }

    override suspend fun update(posPrinter: PosPrinter, callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("pos_printer")
                .document(posPrinter.posPrinterDocumentId!!)
                .update(posPrinter.getMap())
                .addOnSuccessListener {
                    callback?.onSuccess(posPrinter)
                }
                .addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        } else {
            posPrinterDao.update(posPrinter)
            callback?.onSuccess(posPrinter)
        }
    }

    override suspend fun getPosPrinterById(id: String): PosPrinter {
        return posPrinterDao.getPosPrinterById(id)
    }

    override suspend fun getAllPosPrinters(callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("pos_printer").get()
                .addOnSuccessListener { result ->
                    val printers = mutableListOf<PosPrinter>()
                    if (result.size() > 0) {
                        for (document in result) {
                            val obj = document.toObject(PosPrinter::class.java)
                            if (!obj.posPrinterId.isNullOrEmpty()) {
                                obj.posPrinterDocumentId = document.id
                                printers.add(obj)
                            }
                        }
                    }
                    callback?.onSuccess(printers)
                }.addOnFailureListener { exception ->
                    callback?.onFailure(
                        exception.message ?: "Network error! Can't get printers from remote."
                    )
                }
        } else {
            val localPrinters = posPrinterDao.getAllPosPrinters().asLiveData().value
            if (!localPrinters.isNullOrEmpty()) {
                callback?.onSuccess(localPrinters)
            }
        }
    }

}