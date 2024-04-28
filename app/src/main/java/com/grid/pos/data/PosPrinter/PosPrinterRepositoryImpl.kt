package com.grid.pos.data.PosPrinter

import androidx.lifecycle.asLiveData
import com.grid.pos.interfaces.OnResult
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PosPrinterRepositoryImpl(
    private val posPrinterDao: PosPrinterDao
) : PosPrinterRepository {
    override suspend fun insert(posPrinter: PosPrinter, callback: OnResult?) {
        FirebaseFirestore.getInstance().collection("pos_printer")
            .add(posPrinter)
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    posPrinterDao.insert(posPrinter)
                    callback?.onSuccess(posPrinter)
                }
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }
    }

    override suspend fun delete(posPrinter: PosPrinter, callback: OnResult?) {
        FirebaseFirestore.getInstance().collection("pos_printer")
            .document(posPrinter.posPrinterDocumentId!!)
            .delete()
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    posPrinterDao.delete(posPrinter)
                    callback?.onSuccess(posPrinter)
                }
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }
    }

    override suspend fun update(posPrinter: PosPrinter, callback: OnResult?) {
        FirebaseFirestore.getInstance().collection("pos_printer")
            .document(posPrinter.posPrinterDocumentId!!)
            .update(posPrinter.getMap())
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    posPrinterDao.update(posPrinter)
                    callback?.onSuccess(posPrinter)
                }
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }
    }

    override suspend fun getPosPrinterById(id: String): PosPrinter {
        return posPrinterDao.getPosPrinterById(id)
    }

    override suspend fun getAllPosPrinters(callback: OnResult?) {
        val localPrinters = posPrinterDao.getAllPosPrinters().asLiveData().value
        if (!localPrinters.isNullOrEmpty()) {
            callback?.onSuccess(localPrinters)
        }
        FirebaseFirestore.getInstance().collection("pos_printer").get()
            .addOnSuccessListener { result ->
                CoroutineScope(Dispatchers.IO).launch {
                    val printers = mutableListOf<PosPrinter>()
                    posPrinterDao.deleteAll()
                    if (result.size() > 0) {
                        for (document in result) {
                            val obj = document.toObject(PosPrinter::class.java)
                            if (!obj.posPrinterId.isNullOrEmpty()) {
                                obj.posPrinterDocumentId = document.id
                                printers.add(obj)
                            }
                        }
                        posPrinterDao.insertAll(printers.toList())
                    }
                    callback?.onSuccess(printers)
                }
            }.addOnFailureListener { exception ->
                callback?.onFailure(
                    exception.message ?: "Network error! Can't get printers from remote."
                )
            }
    }

}