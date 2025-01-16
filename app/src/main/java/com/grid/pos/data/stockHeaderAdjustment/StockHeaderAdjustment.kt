package com.grid.pos.data.stockHeaderAdjustment

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.grid.pos.data.EntityModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Utils
import java.util.Date

@Entity(tableName = "st_hstockadjustment")
data class StockHeaderAdjustment(
    /**
     * Invoice Header Id
     * */
    @PrimaryKey
    @ColumnInfo(name = "hsa_id")
    @set:PropertyName("hsa_id")
    @get:PropertyName("hsa_id")
    var stockHAId: String = "",

    @Ignore
    @get:Exclude
    var stockHADocumentId: String? = null,

    @Ignore
    @get:Exclude
    var stockHANo: String? = null,

    /**
     * related Invoice header id
     * */
    @ColumnInfo(name = "hsa_cmp_id")
    @set:PropertyName("hsa_cmp_id")
    @get:PropertyName("hsa_cmp_id")
    var stockHACompId: String? = null,

    /**
     * Invoice Header Date
     * */
    @ColumnInfo(name = "hsa_date")
    @set:PropertyName("hsa_date")
    @get:PropertyName("hsa_date")
    var stockHADate: String = DateHelper.getDateInFormat(),


    /**
     * Invoice Header tt code
     * */
    @ColumnInfo(name = "hsa_tt_code")
    @set:PropertyName("hsa_tt_code")
    @get:PropertyName("hsa_tt_code")
    var stockHATtCode: String? = null,

    @Ignore
    @get:Exclude
    var stockHATtCodeName: String? = null,

    /**
     * Invoice Header Trans number
     * */
    @ColumnInfo(name = "hsa_transno")
    @set:PropertyName("hsa_transno")
    @get:PropertyName("hsa_transno")
    var stockHATransNo: String? = null,

    /**
     * Invoice Header Status
     * */
    @ColumnInfo(name = "hsa_desc")
    @set:PropertyName("hsa_desc")
    @get:PropertyName("hsa_desc")
    var stockHADesc: String? = null,

    @Ignore
    @get:Exclude
    var stockHAProjName: String? = null,

    @Ignore
    @get:Exclude
    var stockHABraName: String? = null,

    @Ignore
    @get:Exclude
    var stockHAWaName: String? = null,

    @Ignore
    @get:Exclude
    var stockHASessionPointer: String? = null,

    @Ignore
    @get:Exclude
    var stockHARowguid: String? = null,


    /**
     * Invoice Header timestamp
     * */
    @Ignore
    @set:PropertyName("hsa_timestamp")
    @get:PropertyName("hsa_timestamp")
    @ServerTimestamp
    var stockHATimeStamp: Date? = null,

    /**
     * Invoice Header userstamp
     * */
    @ColumnInfo(name = "hsa_userstamp")
    @set:PropertyName("hsa_userstamp")
    @get:PropertyName("hsa_userstamp")
    var stockHAUserStamp: String? = null,

    /**
     * Invoice Header timestamp
     * */
    @ColumnInfo(name = "hsa_valuedate")
    @set:PropertyName("hsa_valuedate")
    @get:PropertyName("hsa_valuedate")
    var stockHAValueDate: Date? = null,

    @Ignore
    @get:Exclude
    var stockHASource: String? = null,

    @Ignore
    @get:Exclude
    var stockHAHjNo: String? = null,

    ) : EntityModel() {
    constructor() : this("")

    @Exclude
    override fun getName(): String {
        return "${stockHATtCodeName ?: ""} ${stockHATransNo ?: ""}"
    }

    @Exclude
    override fun search(key: String): Boolean {
        return getName().contains(
            key,
            ignoreCase = true
        )
    }


    @Exclude
    override fun isNew(): Boolean {
        return if (SettingsModel.isConnectedToFireStore()) {
            stockHADocumentId.isNullOrEmpty()
        } else {
            stockHAId.isEmpty()
        }
    }

    @Exclude
    override fun prepareForInsert() {
        if (stockHAId.isEmpty() && !SettingsModel.isConnectedToSqlServer()) {
            stockHAId = Utils.generateRandomUuidString()
        }
        stockHACompId = SettingsModel.getCompanyID()
        stockHAUserStamp = SettingsModel.currentUser?.userId
    }

    @Exclude
    override fun setDocumentId(documentId: String) {
        stockHADocumentId = documentId
    }

    @Exclude
    override fun getDocumentId(): String? {
        return stockHADocumentId
    }

    @Exclude
    override fun getMap(): Map<String, Any?> {
        return mapOf()
    }

    @Exclude
    fun didChanged(stockHeadAdj: StockHeaderAdjustment): Boolean {
        return !stockHeadAdj.stockHADesc.equals(stockHADesc) ||
                !stockHeadAdj.stockHASource.equals(stockHASource)
    }

}
