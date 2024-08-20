package com.grid.pos.data.PosPrinter

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
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
                FirebaseFirestore.getInstance().collection("pos_printer").document(it).delete()
                    .await()
            }
        } else {
            posPrinterDao.delete(posPrinter)
        }
    }

    override suspend fun update(posPrinter: PosPrinter) {
        if (SettingsModel.isConnectedToFireStore()) {
            posPrinter.posPrinterDocumentId?.let {
                FirebaseFirestore.getInstance().collection("pos_printer").document(it)
                    .update(posPrinter.getMap()).await()
            }
        } else {
            posPrinterDao.update(posPrinter)
        }
    }

    override suspend fun getAllPosPrinters(): MutableList<PosPrinter> {
        return when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("pos_printer")
                    .whereEqualTo(
                        "pp_cmp_id",
                        SettingsModel.getCompanyID()
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
                printers
            }

            CONNECTION_TYPE.LOCAL.key -> {
                posPrinterDao.getAllPosPrinters(SettingsModel.getCompanyID() ?: "")
            }

            CONNECTION_TYPE.SQL_SERVER.key -> {
                val printers: MutableList<PosPrinter> = mutableListOf()
                if (SettingsModel.isSqlServerWebDb) {
                    val where =
                        "dia_di_name=di_name and di_cmp_id='${SettingsModel.getCompanyID()}'"
                    val dbResult = SQLServerWrapper.getListOf(
                        "pos_displayaux,pos_display",
                        "",
                        mutableListOf("*"),
                        where
                    )
                    dbResult.forEach { obj ->
                        printers.add(PosPrinter().apply {
                            posPrinterId = obj.optString("di_name")
                            posPrinterCompId = obj.optString("di_cmp_id")
                            posPrinterName = obj.optString("di_printer")
                            val dia_appprinters = obj.optString("dia_appprinter").split(":")
                            posPrinterHost =
                                if (dia_appprinters.size > 0) dia_appprinters[0] else ""
                            val port = if (dia_appprinters.size > 1) dia_appprinters[1] else "-1"
                            posPrinterPort = port.toIntOrNull() ?: -1
                            posPrinterType = obj.optString("usr_cmp_id")
                        })
                    }
                } else {
                    val dbResult = SQLServerWrapper.getListOf(
                        "pos_display",
                        "",
                        mutableListOf("*"),
                        ""
                    )
                    dbResult.forEach { obj ->
                        printers.add(PosPrinter().apply {
                            posPrinterId = obj.optString("di_name")
                            posPrinterCompId = obj.optString("di_bra_name")//branch
                            posPrinterName = obj.optString("di_printer")
                            val dia_appprinters = obj.optString("di_appprinter").split(":")
                            posPrinterHost =
                                if (dia_appprinters.size > 0) dia_appprinters[0] else ""
                            val port = if (dia_appprinters.size > 1) dia_appprinters[1] else "-1"
                            posPrinterPort = port.toIntOrNull() ?: -1
                        })
                    }
                }
                printers
            }

            else -> {
                mutableListOf()
            }
        }
    }

    override suspend fun getOnePosPrinter(companyId: String): PosPrinter? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("pos_printer")
                    .whereEqualTo(
                        "pp_cmp_id",
                        companyId
                    ).limit(1).get().await()

                if (querySnapshot.size() > 0) {
                    for (document in querySnapshot) {
                        val obj = document.toObject(PosPrinter::class.java)
                        if (obj.posPrinterId.isNotEmpty()) {
                            obj.posPrinterDocumentId = document.id
                            return obj
                        }
                    }
                }
                return null
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return posPrinterDao.getOnePosPrinter(companyId)
            }

            else -> {
                if (SettingsModel.isSqlServerWebDb) {
                    val where =
                        "dia_di_name=di_name and di_cmp_id='$companyId'"
                    val dbResult = SQLServerWrapper.getListOf(
                        "pos_displayaux,pos_display",
                        "TOP 1",
                        mutableListOf("*"),
                        where
                    )
                    dbResult.forEach { obj ->
                        return PosPrinter().apply {
                            posPrinterId = obj.optString("di_name")
                            posPrinterCompId = obj.optString("di_cmp_id")
                            posPrinterName = obj.optString("di_printer")
                            val dia_appprinters = obj.optString("dia_appprinter").split(":")
                            posPrinterHost =
                                if (dia_appprinters.size > 0) dia_appprinters[0] else ""
                            val port = if (dia_appprinters.size > 1) dia_appprinters[1] else "-1"
                            posPrinterPort = port.toIntOrNull() ?: -1
                            posPrinterType = obj.optString("usr_cmp_id")
                        }
                    }
                } else {
                    val dbResult = SQLServerWrapper.getListOf(
                        "pos_display",
                        "TOP 1",
                        mutableListOf("*"),
                        ""
                    )
                    dbResult.forEach { obj ->
                        return PosPrinter().apply {
                            posPrinterId = obj.optString("di_name")
                            posPrinterCompId = obj.optString("di_bra_name")//branch
                            posPrinterName = obj.optString("di_printer")
                            val dia_appprinters = obj.optString("di_appprinter").split(":")
                            posPrinterHost =
                                if (dia_appprinters.size > 0) dia_appprinters[0] else ""
                            val port = if (dia_appprinters.size > 1) dia_appprinters[1] else "-1"
                            posPrinterPort = port.toIntOrNull() ?: -1
                        }
                    }
                }
                return null
            }
        }
    }

}