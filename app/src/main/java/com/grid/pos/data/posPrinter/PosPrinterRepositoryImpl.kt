package com.grid.pos.data.posPrinter

import com.google.firebase.firestore.Filter
import com.grid.pos.data.FirebaseWrapper
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.DataModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Extension.getStringValue

class PosPrinterRepositoryImpl(
        private val posPrinterDao: PosPrinterDao
) : PosPrinterRepository {
    override suspend fun insert(posPrinter: PosPrinter): DataModel {
        return if (SettingsModel.isConnectedToFireStore()) {
            FirebaseWrapper.insert(
                "pos_printer",
                posPrinter
            )
        } else {
            posPrinterDao.insert(posPrinter)
            DataModel(posPrinter)
        }
    }

    override suspend fun delete(posPrinter: PosPrinter): DataModel {
        return if (SettingsModel.isConnectedToFireStore()) {
            FirebaseWrapper.delete(
                "pos_printer",
                posPrinter
            )
        } else {
            posPrinterDao.delete(posPrinter)
            DataModel(posPrinter)
        }
    }

    override suspend fun update(posPrinter: PosPrinter): DataModel {
        return if (SettingsModel.isConnectedToFireStore()) {
            FirebaseWrapper.update(
                "pos_printer",
                posPrinter
            )
        } else {
            posPrinterDao.update(posPrinter)
            DataModel(posPrinter)
        }
    }

    override suspend fun getAllPosPrinters(): MutableList<PosPrinter> {
         when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "pos_printer",
                    filters = mutableListOf(
                        Filter.equalTo(
                            "pp_cmp_id",
                            SettingsModel.getCompanyID()
                        )
                    )
                )
                val size = querySnapshot?.size() ?: 0
                val printers = mutableListOf<PosPrinter>()
                if (size > 0) {
                    for (document in querySnapshot!!) {
                        val obj = document.toObject(PosPrinter::class.java)
                        if (obj.posPrinterId.isNotEmpty()) {
                            obj.posPrinterDocumentId = document.id
                            printers.add(obj)
                        }
                    }
                }
                return printers
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return posPrinterDao.getAllPosPrinters(SettingsModel.getCompanyID() ?: "")
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
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                return printers
            }
        }
    }

    override suspend fun getOnePosPrinter(companyId: String): PosPrinter? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "pos_printer",
                    limit = 1,
                    filters = mutableListOf(
                        Filter.equalTo(
                            "pp_cmp_id",
                            companyId
                        )
                    )
                )
                val size = querySnapshot?.size() ?: 0
                if (size > 0) {
                    for (document in querySnapshot!!) {
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
                        return printer
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return null
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
                        return printer
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return null
                }
            }
        }
    }

}