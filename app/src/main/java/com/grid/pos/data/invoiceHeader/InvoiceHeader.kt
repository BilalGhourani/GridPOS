package com.grid.pos.data.invoiceHeader

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.grid.pos.data.EntityModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Utils
import java.util.Date

@Entity(tableName = "in_hinvoice")
data class InvoiceHeader(
        /**
         * Invoice Header Id
         * */
        @PrimaryKey
        @ColumnInfo(name = "hi_id")
        @set:PropertyName("hi_id")
        @get:PropertyName("hi_id")
        var invoiceHeadId: String = "",

        @Ignore
        @get:Exclude
        var invoiceHeadDocumentId: String? = null,

        @Ignore
        @get:Exclude
        var invoiceHeadNo: String? = null,

        /**
         * related Invoice header id
         * */
        @ColumnInfo(name = "hi_cmp_id")
        @set:PropertyName("hi_cmp_id")
        @get:PropertyName("hi_cmp_id")
        var invoiceHeadCompId: String? = null,

        /**
         * Invoice Header Date
         * */
        @ColumnInfo(name = "hi_date")
        @set:PropertyName("hi_date")
        @get:PropertyName("hi_date")
        var invoiceHeadDate: String = DateHelper.getDateInFormat(),

        /**
         * Invoice Header Order Number
         * */
        @ColumnInfo(name = "hi_orderno")
        @set:PropertyName("hi_orderno")
        @get:PropertyName("hi_orderno")
        var invoiceHeadOrderNo: String? = null,

        /**
         * Invoice Header tt code
         * */
        @ColumnInfo(name = "hi_tt_code")
        @set:PropertyName("hi_tt_code")
        @get:PropertyName("hi_tt_code")
        var invoiceHeadTtCode: String? = null,

        /**
         * Invoice Header Trans number
         * */
        @ColumnInfo(name = "hi_transno")
        @set:PropertyName("hi_transno")
        @get:PropertyName("hi_transno")
        var invoiceHeadTransNo: String? = null,

        /**
         * Invoice Header Status
         * */
        @ColumnInfo(name = "hi_status")
        @set:PropertyName("hi_status")
        @get:PropertyName("hi_status")
        var invoiceHeadStatus: String? = null,

        /**
         * Invoice Header Note
         * */
        @ColumnInfo(name = "hi_note")
        @set:PropertyName("hi_note")
        @get:PropertyName("hi_note")
        var invoiceHeadNote: String? = null,

        /**
         * invoice Header ThirdParty id
         * */
        @ColumnInfo(name = "hi_tp_name")
        @set:PropertyName("hi_tp_name")
        @get:PropertyName("hi_tp_name")
        var invoiceHeadThirdPartyName: String? = null,

        /**
         * invoice Header ThirdParty id
         * */
        @Ignore
        @get:Exclude
        @set:Exclude
        var invoiceHeadThirdPartyNewName: String? = null,

        /**
         * Invoice Header cash name
         * */
        @ColumnInfo(name = "hi_cashname")
        @set:PropertyName("hi_cashname")
        @get:PropertyName("hi_cashname")
        var invoiceHeadCashName: String? = null,

        /**
         * Invoice Header Grossmont
         * */
        @Ignore
        @get:Exclude
        var invoiceHeadTotalNetAmount: Double = 0.0,

        /**
         * Invoice Header Grossmont
         * */
        @ColumnInfo(name = "hi_grossamt")
        @set:PropertyName("hi_grossamt")
        @get:PropertyName("hi_grossamt")
        var invoiceHeadGrossAmount: Double = 0.0,

        /**
         * Invoice Header Discount
         * */
        @ColumnInfo(name = "hi_disc")
        @set:PropertyName("hi_disc")
        @get:PropertyName("hi_disc")
        var invoiceHeadDiscount: Double = 0.0,

        /**
         * Invoice Header Discamt
         * */
        @ColumnInfo(name = "hi_discamt")
        @set:PropertyName("hi_discamt")
        @get:PropertyName("hi_discamt")
        var invoiceHeadDiscountAmount: Double = 0.0,

        /**
         * Invoice header tax amount
         * */
        @ColumnInfo(name = "hi_taxamt")
        @set:PropertyName("hi_taxamt")
        @get:PropertyName("hi_taxamt")
        var invoiceHeadTaxAmt: Double = 0.0,

        /**
         * Invoice header tax 1 amount
         * */
        @ColumnInfo(name = "hi_tax1amt")
        @set:PropertyName("hi_tax1amt")
        @get:PropertyName("hi_tax1amt")
        var invoiceHeadTax1Amt: Double = 0.0,

        /**
         * Invoice header tax 2 amount
         * */
        @ColumnInfo(name = "hi_tax2amt")
        @set:PropertyName("hi_tax2amt")
        @get:PropertyName("hi_tax2amt")
        var invoiceHeadTax2Amt: Double = 0.0,

        /**
         * Invoice header total tax
         * */
        @ColumnInfo(name = "hi_totaltax")
        @set:PropertyName("hi_totaltax")
        @get:PropertyName("hi_totaltax")
        var invoiceHeadTotalTax: Double = 0.0,

        /**
         * Invoice header total
         * */
        @ColumnInfo(name = "hi_total")
        @set:PropertyName("hi_total")
        @get:PropertyName("hi_total")
        var invoiceHeadTotal: Double = 0.0,

        /**
         * Invoice header total 1
         * */
        @ColumnInfo(name = "hi_total1")
        @set:PropertyName("hi_total1")
        @get:PropertyName("hi_total1")
        var invoiceHeadTotal1: Double = 0.0,

        /**
         * Invoice header Rate
         * */
        @ColumnInfo(name = "hi_rates")
        @set:PropertyName("hi_rates")
        @get:PropertyName("hi_rates")
        var invoiceHeadRate: Double = 0.0,

        /**
         * Invoice header tax Rate
         * */
        @Ignore
        @get:Exclude
        var invoiceHeadTaxRate: Double? = null,

        /**
         * Invoice header Ta name
         * */
        @ColumnInfo(name = "hi_ta_name")
        @set:PropertyName("hi_ta_name")
        @get:PropertyName("hi_ta_name")
        var invoiceHeadTaName: String? = null,

        /**
         * Invoice header Table id
         * */
        @Ignore
        @get:Exclude
        var invoiceHeadTableId: String? = null,

        /**
         * Invoice header Table type
         * */
        @Ignore
        @get:Exclude
        var invoiceHeadTableType: String? = null,

        /**
         * Invoice header Clients Count
         * */
        @ColumnInfo(name = "hi_clientscount")
        @set:PropertyName("hi_clientscount")
        @get:PropertyName("hi_clientscount")
        var invoiceHeadClientsCount: Int = 1,

        /**
         * Invoice header Change
         * */
        @ColumnInfo(name = "hi_change")
        @set:PropertyName("hi_change")
        @get:PropertyName("hi_change")
        var invoiceHeadChange: Double = 0.0,

        /**
         * Invoice header Change
         * */
        @ColumnInfo(name = "hi_printed")
        @set:PropertyName("hi_printed")
        @get:PropertyName("hi_printed")
        var invoiceHeadPrinted: Int = 0,

        /**
         * Invoice Header timestamp
         * */
        @Ignore
        @set:PropertyName("hi_timestamp")
        @get:PropertyName("hi_timestamp")
        @ServerTimestamp
        var invoiceHeadTimeStamp: Date? = null,

        /**
         * Invoice Header timestamp
         * */
        @ColumnInfo(name = "hi_datetime")
        @set:PropertyName("hi_datetime")
        @get:PropertyName("hi_datetime")
        var invoiceHeadDateTime: Long = System.currentTimeMillis(),

        /**
         * Invoice Header userstamp
         * */
        @ColumnInfo(name = "hi_userstamp")
        @set:PropertyName("hi_userstamp")
        @get:PropertyName("hi_userstamp")
        var invoiceHeadUserStamp: String? = null,

        ) : EntityModel() {
    constructor() : this("")

    @Exclude
    override fun getName(): String {
        val table = if (invoiceHeadTaName.isNullOrEmpty()) "" else " $invoiceHeadTaName"
        val transNo = if (invoiceHeadTransNo.isNullOrEmpty()) "$invoiceHeadOrderNo" else if (invoiceHeadTtCode.isNullOrEmpty()) "$invoiceHeadTransNo" else " $invoiceHeadTtCode$invoiceHeadTransNo"
        val total = String.format(
            "%,.${SettingsModel.currentCurrency?.currencyName1Dec ?: 2}f",
            invoiceHeadTotal
        )
        val clientName = invoiceHeadThirdPartyNewName ?: ""
        return "$transNo $total $table $clientName"
    }

    @Exclude
    override fun search(key: String): Boolean {
        return getName().contains(
            key,
            ignoreCase = true
        )
    }

    @Exclude
    fun getTotalNetAmount(): Double {
        if (invoiceHeadTotalNetAmount.isNaN()) {
            return 0.0
        }
        return invoiceHeadTotalNetAmount
    }

    @Exclude
    override fun isNew(): Boolean {
        return if (SettingsModel.isConnectedToFireStore()) {
            invoiceHeadDocumentId.isNullOrEmpty()
        } else {
            invoiceHeadId.isEmpty()
        }
    }

    @Exclude
    override fun prepareForInsert() {
        if (invoiceHeadId.isEmpty() && !SettingsModel.isConnectedToSqlServer()) {
            invoiceHeadId = Utils.generateRandomUuidString()
        }
        invoiceHeadCompId = SettingsModel.getCompanyID()
        invoiceHeadUserStamp = SettingsModel.currentUser?.userId
    }

    @Exclude
    override fun setDocumentId(documentId: String) {
        invoiceHeadDocumentId = documentId
    }

    @Exclude
    override fun getDocumentId(): String? {
        return invoiceHeadDocumentId
    }

    @Exclude
    override fun getMap(): Map<String, Any?> {
        return mapOf(
            "hi_id" to invoiceHeadId,
            "hi_cmp_id" to invoiceHeadCompId,
            "hi_date" to invoiceHeadDate,
            "hi_orderno" to invoiceHeadOrderNo,
            "hi_tt_code" to invoiceHeadTtCode,
            "hi_transno" to invoiceHeadTransNo,
            "hi_status" to invoiceHeadStatus,
            "hi_note" to invoiceHeadNote,
            "hi_tp_name" to invoiceHeadThirdPartyName,
            "hi_cashname" to invoiceHeadCashName,
            "hi_grossamt" to invoiceHeadGrossAmount,
            "hi_disc" to invoiceHeadDiscount,
            "hi_discamt" to invoiceHeadDiscountAmount,
            "hi_taxamt" to invoiceHeadTaxAmt,
            "hi_tax1amt" to invoiceHeadTax1Amt,
            "hi_tax2amt" to invoiceHeadTax2Amt,
            "hi_totaltax" to invoiceHeadTotalTax,
            "hi_total" to invoiceHeadTotal,
            "hi_total1" to invoiceHeadTotal1,
            "hi_rates" to invoiceHeadRate,
            "hi_ta_name" to invoiceHeadTaName,
            "hi_clientscount" to invoiceHeadClientsCount,
            "hi_change" to invoiceHeadChange,
            "hi_printed" to invoiceHeadPrinted,
            "hi_timestamp" to FieldValue.serverTimestamp(),
            "hi_userstamp" to invoiceHeadUserStamp,
            "hi_datetime" to invoiceHeadDateTime,
        )
    }

    @Exclude
    fun didChanged(invoiceHeader: InvoiceHeader): Boolean {
        return !invoiceHeader.invoiceHeadNote.equals(invoiceHeadNote) || !invoiceHeader.invoiceHeadCashName.equals(invoiceHeadCashName) || invoiceHeader.invoiceHeadDiscount != invoiceHeadDiscount || invoiceHeader.invoiceHeadDiscountAmount != invoiceHeadDiscountAmount
    }

    @Exclude
    fun getVatAmount(): Double {
        return invoiceHeadTaxAmt + invoiceHeadTax1Amt + invoiceHeadTax2Amt
    }

    @Exclude
    fun isFinished(): Boolean {
        return !invoiceHeadTransNo.isNullOrEmpty() && !invoiceHeadTransNo.equals("0")
    }
}
