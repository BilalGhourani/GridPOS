package com.grid.pos.data.posPrinter

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.DataModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Extension.getStringValue
import kotlinx.coroutines.tasks.await
import java.sql.ResultSet

class PosPrinterRepositoryImpl(
        private val posPrinterDao: PosPrinterDao
) : PosPrinterRepository {
    override suspend fun insert(posPrinter: PosPrinter): DataModel {
        if (SettingsModel.isConnectedToFireStore()) {
            val docRef = FirebaseFirestore.getInstance().collection("pos_printer")
                .add(posPrinter.getMap()).await()
            posPrinter.posPrinterDocumentId = docRef.id
            return DataModel(posPrinter)
        } else {
            posPrinterDao.insert(posPrinter)
            return DataModel(posPrinter)
        }
    }

    override suspend fun delete(posPrinter: PosPrinter): DataModel {
        if (SettingsModel.isConnectedToFireStore()) {
            posPrinter.posPrinterDocumentId?.let {
                FirebaseFirestore.getInstance().collection("pos_printer").document(it).delete()
                    .await()
                return DataModel(posPrinter)
            }
            return DataModel(
                posPrinter,
                false
            )
        } else {
            posPrinterDao.delete(posPrinter)
            return DataModel(posPrinter)
        }
    }

    override suspend fun update(posPrinter: PosPrinter): DataModel {
        if (SettingsModel.isConnectedToFireStore()) {
            posPrinter.posPrinterDocumentId?.let {
                FirebaseFirestore.getInstance().collection("pos_printer").document(it)
                    .update(posPrinter.getMap()).await()
                return DataModel(posPrinter)
            }
            return DataModel(
                posPrinter,
                false
            )
        } else {
            posPrinterDao.update(posPrinter)
            return DataModel(posPrinter)
        }
    }

    override suspend fun getAllPosPrinters(): DataModel {
        when (SettingsModel.connectionType) {
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
                return DataModel(printers)
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return DataModel(posPrinterDao.getAllPosPrinters(SettingsModel.getCompanyID() ?: ""))
            }

            else -> {
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

                        dbResult?.let {
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
                        return DataModel(printers)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return DataModel(
                            null,
                            false,
                            e.message
                        )
                    }
                } else {
                    try {
                        val dbResult = SQLServerWrapper.getListOf(
                            "pos_display",
                            "",
                            mutableListOf("*"),
                            ""
                        )

                        dbResult?.let {
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
                        return DataModel(printers)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return DataModel(
                            null,
                            false,
                            e.message
                        )
                    }
                }
            }
        }
    }

    override suspend fun getOnePosPrinter(companyId: String): DataModel {
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
                            return DataModel(obj)
                        }
                    }
                }
                return DataModel(null)
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return DataModel(posPrinterDao.getOnePosPrinter(companyId))
            }

            else -> {
                if (SettingsModel.isSqlServerWebDb) {
                    try {
                        var printer: PosPrinter? = null
                        val where = "dia_di_name=di_name and di_cmp_id='$companyId'"
                        val dbResult = SQLServerWrapper.getListOf(
                            "pos_displayaux,pos_display",
                            "TOP 1",
                            mutableListOf("*"),
                            where
                        )
                        dbResult?.let {
                            while (it.next()) {
                                printer = PosPrinter().apply {
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
                        return DataModel(printer)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return DataModel(
                            null,
                            false,
                            e.message
                        )
                    }
                } else {
                    try {
                        var printer: PosPrinter? = null
                        val dbResult = SQLServerWrapper.getListOf(
                            "pos_display",
                            "TOP 1",
                            mutableListOf("*"),
                            ""
                        )
                        dbResult?.let {
                            while (it.next()) {
                                printer = PosPrinter().apply {
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
                        return DataModel(printer)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return DataModel(
                            null,
                            false,
                            e.message
                        )
                    }
                }
            }
        }
    }

}