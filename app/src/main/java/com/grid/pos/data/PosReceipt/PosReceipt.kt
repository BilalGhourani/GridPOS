package com.grid.pos.data.PosReceipt

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
    @ColumnInfo(name = "pr_cash")
    @set:PropertyName("pr_cash")
    @get:PropertyName("pr_cash")
    var posReceiptCash: Double = 0.0,

        /**
     *  POS Receipt Cash Amount 2
     * */
    @ColumnInfo(name = "pr_cashs")
    @set:PropertyName("pr_cashs")
    @get:PropertyName("pr_cashs")
    var posReceiptCashs: Double = 0.0,

        /**
     *  POS Receipt Credit Amount
     * */
    @ColumnInfo(name = "pr_credit")
    @set:PropertyName("pr_credit")
    @get:PropertyName("pr_credit")
    var posReceiptCredit: Double = 0.0,

        /**
     *  POS Receipt Credit Amounts
     * */
    @ColumnInfo(name = "pr_credits")
    @set:PropertyName("pr_credits")
    @get:PropertyName("pr_credits")
    var posReceiptCredits: Double = 0.0,

        /**
     *  POS Receipt Debit Amount
     * */
    @ColumnInfo(name = "pr_debit")
    @set:PropertyName("pr_debit")
    @get:PropertyName("pr_debit")
    var posReceiptDebit: Double = 0.0,

        /**
     *  POS Receipt Debit Amount 2
     * */
    @ColumnInfo(name = "pr_debits")
    @set:PropertyName("pr_debits")
    @get:PropertyName("pr_debits")
    var posReceiptDebits: Double = 0.0,


        /**
     *  POS Receipt Date
     * */
    @Ignore
    @set:PropertyName("pr_timestamp")
    @get:PropertyName("pr_timestamp")
    @ServerTimestamp
    var posReceiptTimeStamp: Date? = null,

        /**
     *  POS Receipt Date
     * */
    @ColumnInfo(name = "pr_datetime")
    @set:PropertyName("pr_datetime")
    @get:PropertyName("pr_datetime")
    var posReceiptDateTime: Long = System.currentTimeMillis(),


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
    override fun isNew(): Boolean {
        return if (SettingsModel.isConnectedToSqlite()) {
            posReceiptId.isEmpty()
        } else {
            posReceiptDocumentId.isNullOrEmpty()
        }
    }

    @Exclude
    override fun prepareForInsert() {
        if (posReceiptId.isNullOrEmpty()) {
            posReceiptId = Utils.generateRandomUuidString()
        }
        posReceiptUserStamp = SettingsModel.currentUserId
    }

    @Exclude
    fun getMap(): Map<String, Any?> {
        return mapOf(
            "pr_id" to posReceiptId,
            "pr_hi_id" to posReceiptInvoiceId,
            "pr_cash" to posReceiptCash,
            "pr_cashs" to posReceiptCashs,
            "pr_debit" to posReceiptDebit,
            "pr_debits" to posReceiptDebits,
            "pr_credit" to posReceiptCredit,
            "pr_credits" to posReceiptCredits,
            "pr_timestamp" to FieldValue.serverTimestamp(),
            "pr_userstamp" to posReceiptUserStamp,
            "pr_datetime" to posReceiptDateTime
        )
    }
}
