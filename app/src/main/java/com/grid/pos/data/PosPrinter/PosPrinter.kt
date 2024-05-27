package com.grid.pos.data.PosPrinter

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.errorprone.annotations.Keep
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.grid.pos.data.DataModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Utils

@Entity(tableName = "pos_printer")
data class PosPrinter(
        /**
         * POS Printer Id
         * */
        @PrimaryKey
        @ColumnInfo(name = "pp_id")
        @set:PropertyName("pp_id")
        @get:PropertyName("pp_id")
        var posPrinterId: String,

        @Ignore
        @get:Exclude
        var posPrinterDocumentId: String? = null,

        /**
         * related  POS Printer type company id
         * */
        @ColumnInfo(name = "pp_cmp_id")
        @set:PropertyName("pp_cmp_id")
        @get:PropertyName("pp_cmp_id")
        var posPrinterCompId: String? = null,

        /**
         *   POS Printer name
         * */
        @ColumnInfo(name = "pp_name")
        @set:PropertyName("pp_name")
        @get:PropertyName("pp_name")
        var posPrinterName: String? = null,

        /**
         *   POS Printer host
         * */
        @ColumnInfo(name = "pp_host")
        @set:PropertyName("pp_host")
        @get:PropertyName("pp_host")
        var posPrinterHost: String = "127.0.0.1",

        /**
         *   POS Printer printer
         * */
        @ColumnInfo(name = "pp_port")
        @set:PropertyName("pp_port")
        @get:PropertyName("pp_port")
        var posPrinterPort: Int = -1,

        /**
         *  POS Printer type
         * */
        @ColumnInfo(name = "pp_type")
        @set:PropertyName("pp_type")
        @get:PropertyName("pp_type")
        var posPrinterType: String? = null,

        ) : DataModel() {
    constructor() : this("")

    @Exclude
    override fun getId(): String {
        return posPrinterId
    }

    @Exclude
    override fun getName(): String {
        return posPrinterName ?: ""
    }

    @Exclude
    override fun prepareForInsert() {
        if (posPrinterId.isNullOrEmpty()) {
            posPrinterId = Utils.generateRandomUuidString()
        }
        posPrinterCompId = SettingsModel.companyID
    }

    @Exclude
    fun getMap(): Map<String, Any?> {
        return mapOf(
            "pp_id" to posPrinterId,
            "pp_cmp_id" to posPrinterCompId,
            "pp_name" to posPrinterName,
            "pp_host" to posPrinterHost,
            "pp_port" to posPrinterPort,
            "pp_type" to posPrinterType,
        )
    }
}
