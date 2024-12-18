package com.grid.pos.data.posPrinter

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Extension.getStringValue
import kotlinx.coroutines.tasks.await
import java.sql.ResultSet

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
                    try {
                        val where = "dia_di_name=di_name and di_cmp_id='${SettingsModel.getCompanyID()}'"
                        val dbResult = SQLServerWrapper.getListOf(
                            "pos_displayaux,pos_display",
                            "",
                            mutableListOf("*"),
                            where
                        )

                        if (dbResult.succeed) {
                            (dbResult.result as? ResultSet)?.let {
                                while (it.next()) {
                                    printers.add(PosPrinter().apply {
                                        posPrinterId = it.getStringValue("di_name")
                                        posPrinterCompId = it.getStringValue("di_cmp_id")
                                        posPrinterName = it.getStringValue("di_printer")
                                        val diaAppPrinters = it.getStringValue("dia_appprinter")
                                            .split(":")
                                        val size = diaAppPrinters.size
                                        posPrinterHost = if (size > 0) diaAppPrinters[0] else ""
                                        val port = if (size > 1) diaAppPrinters[1] else "-1"
                                        posPrinterPort = port.toIntOrNull() ?: -1
                                    })
                                }
                                SQLServerWrapper.closeResultSet(it)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    try {
                        val dbResult = SQLServerWrapper.getListOf(
                            "pos_display",
                            "",
                            mutableListOf("*"),
                            ""
                        )

                        if (dbResult.succeed) {
                            (dbResult.result as? ResultSet)?.let {
                                while (it.next()) {
                                    printers.add(PosPrinter().apply {
                                        posPrinterId = it.getStringValue("di_name")
                                        posPrinterCompId = it.getStringValue("di_bra_name")//branch
                                        posPrinterName = it.getStringValue("di_printer")
                                        val diaAppPrinters = it.getStringValue("di_appprinter")
                                            .split(":")
                                        val size = diaAppPrinters.size
                                        posPrinterHost = if (size > 0) diaAppPrinters[0] else ""
                                        val port = if (size > 1) diaAppPrinters[1] else "-1"
                                        posPrinterPort = port.toIntOrNull() ?: -1
                                    })
                                }
                                SQLServerWrapper.closeResultSet(it)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
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
                    try {
                        val where = "dia_di_name=di_name and di_cmp_id='$companyId'"
                        val dbResult = SQLServerWrapper.getListOf(
                            "pos_displayaux,pos_display",
                            "TOP 1",
                            mutableListOf("*"),
                            where
                        )
                        if (dbResult.succeed) {
                            (dbResult.result as? ResultSet)?.let {
                                while (it.next()) {
                                    return PosPrinter().apply {
                                        posPrinterId = it.getStringValue("di_name")
                                        posPrinterCompId = it.getStringValue("di_cmp_id")
                                        posPrinterName = it.getStringValue("di_printer")
                                        val diaAppPrinters = it.getStringValue("dia_appprinter")
                                            .split(":")
                                        val size = diaAppPrinters.size
                                        posPrinterHost = if (size > 0) diaAppPrinters[0] else ""
                                        val port = if (size > 1) diaAppPrinters[1] else "-1"
                                        posPrinterPort = port.toIntOrNull() ?: -1
                                        posPrinterType = it.getStringValue("usr_cmp_id")
                                    }
                                }
                                SQLServerWrapper.closeResultSet(it)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    try {
                        val dbResult = SQLServerWrapper.getListOf(
                            "pos_display",
                            "TOP 1",
                            mutableListOf("*"),
                            ""
                        )
                        if (dbResult.succeed) {
                            (dbResult.result as? ResultSet)?.let {
                                while (it.next()) {
                                    return PosPrinter().apply {
                                        posPrinterId = it.getStringValue("di_name")
                                        posPrinterCompId = it.getStringValue("di_bra_name")//branch
                                        posPrinterName = it.getStringValue("di_printer")
                                        val diaAppPrinters = it.getStringValue("di_appprinter")
                                            .split(":")
                                        val size = diaAppPrinters.size
                                        posPrinterHost = if (size > 0) diaAppPrinters[0] else ""
                                        val port = if (size > 1) diaAppPrinters[1] else "-1"
                                        posPrinterPort = port.toIntOrNull() ?: -1
                                    }
                                }
                                SQLServerWrapper.closeResultSet(it)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                return null
            }
        }
    }

}