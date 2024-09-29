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
import com.grid.pos.data.Item.Item
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
    override fun isNew(): Boolean {
        return if (SettingsModel.isConnectedToFireStore()) {
            invoiceDocumentId.isNullOrEmpty()
        } else {
            invoiceId.isEmpty()
        }
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
    fun getPrice(): Double {
        if (invoicePrice.isNaN()) {
            return 0.0
        }
        return invoicePrice
    }

    @Exclude
    fun getDiscount(): Double {
        if (invoiceDiscount.isNaN()) {
            return 0.0
        }
        return invoiceDiscount
    }

    @Exclude
    fun getDiscountAmount(): Double {
        if (invoiceDiscamt.isNaN()) {
            return 0.0
        }
        return invoiceDiscamt
    }

    @Exclude
    fun getAmount(): Double {
        return invoiceQuantity.times(invoicePrice)
    }

    @Exclude
    fun getTax(): Double {
        if (invoiceTax.isNaN()) {
            return 0.0
        }
        return invoiceTax
    }

    @Exclude
    fun getTaxValue(amount: Double = getAmount()): Double {
        return amount.times(getTax().times(0.01))
    }

    @Exclude
    fun getIncludedTaxPerc(amount: Double = getAmount()): Double {
        val netAmount = amount.div(1 + (getTax().times(0.01)))
        return amount - netAmount
    }

    @Exclude
    fun getTax1(): Double {
        if (invoiceTax1.isNaN()) {
            return 0.0
        }
        return invoiceTax1
    }

    @Exclude
    fun getTax1Value(amount: Double = getAmount()): Double {
        return amount.times(getTax1().times(0.01))
    }

    @Exclude
    fun getIncludedTax1Perc(amount: Double = getAmount()): Double {
        val netAmount = amount.div(1 + (getTax1().times(0.01)))
        return amount - netAmount
    }

    @Exclude
    fun getTax2(): Double {
        if (invoiceTax2.isNaN()) {
            return 0.0
        }
        return invoiceTax2
    }

    @Exclude
    fun getTax2Value(amount: Double = getAmount()): Double {
        return amount.times(getTax2().times(0.01))
    }

    @Exclude
    fun getIncludedTax2Perc(amount: Double = getAmount()): Double {
        val netAmount = amount.div(1 + (getTax2().times(0.01)))
        return amount - netAmount
    }

    @Exclude
    fun getInvoiceCostOrZero(): Double {
        if (invoiceCost.isNaN()) {
            return 0.0
        }
        return invoiceCost
    }

    @Exclude
    fun getRemainingQtyOrZero(): Double {
        if (invoiceRemQty.isNaN()) {
            return 0.0
        }
        return invoiceRemQty
    }

    @Exclude
    fun getNetAmount(): Double {
        val amount = getAmount() - getDiscountAmount()
        return amount + getTaxValue(amount) + getTax1Value(amount) + getTax2Value(amount)
    }

    @Exclude
    fun didChanged(invoice: Invoice): Boolean {
        return !invoice.invoiceExtraName.equals(invoiceExtraName) || !invoice.invoiceNote.equals(invoiceNote) || invoice.invoicePrice != invoicePrice || invoice.invoiceDiscount != invoiceDiscount || invoice.invoiceDiscamt != invoiceDiscamt || invoice.invoiceQuantity != invoiceQuantity || invoice.invoiceTax != invoiceTax || invoice.invoiceTax1 != invoiceTax1 || invoice.invoiceTax2 != invoiceTax2
    }
}
