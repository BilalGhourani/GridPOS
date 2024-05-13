package com.grid.pos.data.PosReceipt

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.grid.pos.data.DataModel
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Utils
import java.util.Date

@Entity(tableName = "pos_receipt")
data class PosReceipt(
    /**
     * POS Receipt Id
     * */
    @PrimaryKey
    @ColumnInfo(name = "pr_id")
    @set:PropertyName("pr_id")
    @get:PropertyName("pr_id")
    var posReceiptId: String,

    @Ignore
    @get:Exclude
    var posReceiptDocumentId: String? = null,

    /**
     * related POS Receipt Invoice Id
     * */
    @ColumnInfo(name = "pr_hi_id")
    @set:PropertyName("pr_hi_id")
    @get:PropertyName("pr_hi_id")
    var posReceiptInvoiceId: String? = null,

    /**
     *  POS Receipt Cash Amount
     * */
    @ColumnInfo(name = "pr_cash_amt")
    @set:PropertyName("pr_cash_amt")
    @get:PropertyName("pr_cash_amt")
    var posReceiptCashAmount: Double? = null,

    /**
     *  POS Receipt Cash Amount 2
     * */
    @ColumnInfo(name = "pr_cash_amts")
    @set:PropertyName("pr_cash_amts")
    @get:PropertyName("pr_cash_amts")
    var posReceiptCashAmount2: Double? = null,

    /**
     *  POS Receipt Debit Amount
     * */
    @ColumnInfo(name = "pr_debit_amt")
    @set:PropertyName("pr_debit_amt")
    @get:PropertyName("pr_debit_amt")
    var posReceiptDebitAmount: Double? = null,

    /**
     *  POS Receipt Debit Amount 2
     * */
    @ColumnInfo(name = "pr_debit_amts")
    @set:PropertyName("pr_debit_amts")
    @get:PropertyName("pr_debit_amts")
    var posReceiptDebitAmount2: Double? = null,

    /**
     *  POS Receipt Credit Amount
     * */
    @ColumnInfo(name = "pr_credit_amt")
    @set:PropertyName("pr_credit_amt")
    @get:PropertyName("pr_credit_amt")
    var posReceiptCreditAmount: Double? = null,

    /**
     *  POS Receipt Credit Amounts
     * */
    @ColumnInfo(name = "pr_credit_amts")
    @set:PropertyName("pr_credit_amts")
    @get:PropertyName("pr_credit_amts")
    var posReceiptCreditAmount2: Double? = null,

    /**
     *  POS Receipt Date
     * */
    @ColumnInfo(name = "pr_timestamp")
    @set:PropertyName("pr_timestamp")
    @get:PropertyName("pr_timestamp")
    var posReceiptTimeStamp: String? = null,


    /**
     *  POS Receipt UserStamp
     * */
    @ColumnInfo(name = "pr_userstamp")
    @set:PropertyName("pr_userstamp")
    @get:PropertyName("pr_userstamp")
    var posReceiptUserStamp: String? = null,
) : DataModel() {
    constructor() : this("")

    @Exclude
    override fun getId(): String {
        return posReceiptId
    }

    @Exclude
    override fun getName(): String {
        return ""
    }

    @Exclude
    override fun prepareForInsert() {
        if (posReceiptId.isNullOrEmpty()) {
            posReceiptId = Utils.generateRandomUuidString()
        }
        posReceiptUserStamp = SettingsModel.currentUserId
        posReceiptTimeStamp = Utils.getDateinFormat()
    }

    @Exclude
    fun getMap(): Map<String, Any?> {
        return mapOf(
            "pr_hi_id" to posReceiptInvoiceId,
            "pr_cash_amt" to posReceiptCashAmount,
            "pr_cash_amts" to posReceiptCashAmount2,
            "pr_debit_amt" to posReceiptDebitAmount,
            "pr_debit_amts" to posReceiptDebitAmount2,
            "pr_credit_amt" to posReceiptCreditAmount,
            "pr_credit_amts" to posReceiptCreditAmount2,
            "pr_timestamp" to posReceiptTimeStamp,
            "pr_userstamp" to posReceiptUserStamp
        )
    }
}
