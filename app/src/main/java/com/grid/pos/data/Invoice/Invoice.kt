package com.grid.pos.data.Invoice

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.grid.pos.data.DataModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Utils
import java.util.Date

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
        var invoiceNote: String? = null,

        /**
         * Invoice cost
         * */
        @ColumnInfo(name = "in_cost")
        @set:PropertyName("in_cost")
        @get:PropertyName("in_cost")
        var invoiceCost: Double = 0.0,

        /**
         * Invoice remaining quantity
         * */
        @ColumnInfo(name = "in_remqty")
        @set:PropertyName("in_remqty")
        @get:PropertyName("in_remqty")
        var invoiceRemQty: Double = 0.0,

        /**
         * Invoice timestamp
         * */
        @Ignore
        @set:PropertyName("in_timestamp")
        @get:PropertyName("in_timestamp")
        @ServerTimestamp
        var invoiceTimeStamp: Date? = null,

        /**
         * Invoice timestamp
         * */
        @ColumnInfo(name = "in_datetime")
        @set:PropertyName("in_datetime")
        @get:PropertyName("in_datetime")
        var invoiceDateTime: Long = System.currentTimeMillis(),

        /**
         * Invoice userstamp
         * */
        @ColumnInfo(name = "in_userstamp")
        @set:PropertyName("in_userstamp")
        @get:PropertyName("in_userstamp")
        var invoiceUserStamp: String? = null,

        /**
         * Invoice extra name
         * */
        @ColumnInfo(name = "in_extraname")
        @set:PropertyName("in_extraname")
        @get:PropertyName("in_extraname")
        var invoiceExtraName: String? = null,

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
        if (invoiceId.isEmpty()) {
            invoiceId = Utils.generateRandomUuidString()
        }
        invoiceUserStamp = SettingsModel.currentUserId
    }

    @Exclude
    fun getMap(): Map<String, Any?> {
        return mapOf(
            "in_id" to invoiceId,
            "in_hi_id" to invoiceHeaderId,
            "in_it_id" to invoiceItemId,
            "in_qty" to invoiceQuantity,
            "in_price" to invoicePrice,
            "in_disc" to invoiceDiscount,
            "in_discamt" to invoiceDiscamt,
            "in_tax" to invoiceTax,
            "in_tax1" to invoiceTax1,
            "in_tax2" to invoiceTax2,
            "in_note" to invoiceNote,
            "in_cost" to invoiceCost,
            "in_remqty" to invoiceRemQty,
            "in_timestamp" to FieldValue.serverTimestamp(),
            "in_datetime" to invoiceDateTime,
            "in_userstamp" to invoiceUserStamp,
            "in_extraname" to invoiceExtraName,
        )
    }

    @Exclude
    fun getDiscount(): Double {
        return invoiceDiscount
    }

    @Exclude
    fun getDiscountAmount(): Double {
        return invoiceDiscamt
    }

    @Exclude
    fun getAmount(): Double {
        return invoiceQuantity.times(invoicePrice)
    }

    @Exclude
    fun getTax(): Double {
        return getAmount().times(invoiceTax.div(100.0))
    }

    @Exclude
    fun getTax1(): Double {
        return getAmount().times(invoiceTax1.div(100.0))
    }

    @Exclude
    fun getTax2(): Double {
        return getAmount().times(invoiceTax2.div(100.0))
    }

    @Exclude
    fun getPriceWithTax(): Double {
        return getAmount() + getTax() + getTax1() + getTax2()
    }

    @Exclude
    fun getNetAmount(): Double {
        return getPriceWithTax() - getDiscountAmount()
    }
}
