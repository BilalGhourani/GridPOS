package com.grid.pos.data.Invoice

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

@Entity(tableName = "in_invoice")
data class Invoice(
    /**
     * Invoice Id
     * */
    @PrimaryKey
    @ColumnInfo(name = "in_id")
    @set:PropertyName("in_id")
    @get:PropertyName("in_id")
    var invoiceId: String,

    @Ignore
    @get:Exclude
    var invoiceDocumentId: String? = null,

    /**
     * related Invoice header id
     * */
    @ColumnInfo(name = "in_hi_id")
    @set:PropertyName("in_hi_id")
    @get:PropertyName("in_hi_id")
    var invoiceHeaderId: String? = null,

    /**
     * Invoice item id
     * */
    @ColumnInfo(name = "in_it_id")
    @set:PropertyName("in_it_id")
    @get:PropertyName("in_it_id")
    var invoiceItemId: String? = null,

    /**
     * Invoice quantity
     * */
    @ColumnInfo(name = "in_qty")
    @set:PropertyName("in_qty")
    @get:PropertyName("in_qty")
    var invoiceQuantity: Double = 0.0,

    /**
     * Invoice price
     * */
    @ColumnInfo(name = "in_price")
    @set:PropertyName("in_price")
    @get:PropertyName("in_price")
    var invoicePrice: Double = 0.0,

    /**
     * Invoice discount
     * */
    @ColumnInfo(name = "in_disc")
    @set:PropertyName("in_disc")
    @get:PropertyName("in_disc")
    var invoiceDiscount: Double = 0.0,

    /**
     * Invoice discamt
     * */
    @ColumnInfo(name = "in_discamt")
    @set:PropertyName("in_discamt")
    @get:PropertyName("in_discamt")
    var invoiceDiscamt: Double = 0.0,

    /**
     * Invoice tax
     * */
    @ColumnInfo(name = "in_tax")
    @set:PropertyName("in_tax")
    @get:PropertyName("in_tax")
    var invoiceTax: Double = 0.0,

    /**
     * Invoice tax 1
     * */
    @ColumnInfo(name = "in_tax1")
    @set:PropertyName("in_tax1")
    @get:PropertyName("in_tax1")
    var invoiceTax1: Double = 0.0,

    /**
     * Invoice tax 2
     * */
    @ColumnInfo(name = "in_tax2")
    @set:PropertyName("in_tax2")
    @get:PropertyName("in_tax2")
    var invoiceTax2: Double = 0.0,

    /**
     * Invoice note
     * */
    @ColumnInfo(name = "in_note")
    @set:PropertyName("in_note")
    @get:PropertyName("in_note")
    var invoicNote: String? = null,

    /**
     * Invoice cost
     * */
    @ColumnInfo(name = "in_cost")
    @set:PropertyName("in_cost")
    @get:PropertyName("in_cost")
    var invoicCost: Double = 0.0,

    /**
     * Invoice remaining quantity
     * */
    @ColumnInfo(name = "in_remqty")
    @set:PropertyName("in_remqty")
    @get:PropertyName("in_remqty")
    var invoicRemQty: Double = 0.0,

    /**
     * Invoice timestamp
     * */
    @ColumnInfo(name = "in_timestamp")
    @set:PropertyName("in_timestamp")
    @get:PropertyName("in_timestamp")
    var invoicTimeStamp: String? = null,

    /**
     * Invoice userstamp
     * */
    @ColumnInfo(name = "in_userstamp")
    @set:PropertyName("in_userstamp")
    @get:PropertyName("in_userstamp")
    var invoicUserStamp: String? = null,

    /**
     * Invoice extra name
     * */
    @ColumnInfo(name = "in_extraname")
    @set:PropertyName("in_extraname")
    @get:PropertyName("in_extraname")
    var invoicExtraName: String? = null,

    ) : DataModel() {
    constructor() : this("")

    @Exclude
    override fun getId(): String {
        return invoiceId
    }

    @Exclude
    override fun getName(): String {
        return ""
    }

    @Exclude
    override fun prepareForInsert() {
        if (invoiceId.isNullOrEmpty()) {
            invoiceId = Utils.generateRandomUuidString()
        }
        invoicUserStamp = SettingsModel.currentUserId
        invoicTimeStamp =  Utils.getDateinFormat()
    }

    @Exclude
    fun getMap(): Map<String, Any?> {
        return mapOf(
            "in_hi_id" to invoiceHeaderId,
            "in_it_id" to invoiceItemId,
            "in_qty" to invoiceQuantity,
            "in_price" to invoicePrice,
            "in_disc" to invoiceDiscount,
            "in_discamt" to invoiceDiscamt,
            "in_tax" to invoiceTax,
            "in_tax1" to invoiceTax1,
            "in_tax2" to invoiceTax2,
            "in_note" to invoicNote,
            "in_cost" to invoicCost,
            "in_remqty" to invoicRemQty,
            "in_timestamp" to invoicTimeStamp,
            "in_userstamp" to invoicUserStamp,
            "in_extraname" to invoicExtraName,
        )
    }
}
