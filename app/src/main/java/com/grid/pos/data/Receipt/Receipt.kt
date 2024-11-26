package com.grid.pos.data.Receipt

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.grid.pos.data.DataModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Utils
import java.util.Date

@Entity(tableName = "receipt")
data class Receipt(
        /**
         * Receipt id
         * */
        @PrimaryKey
        @ColumnInfo(name = "rec_id")
        @set:PropertyName("rec_id")
        @get:PropertyName("rec_id")
        var receiptId: String,

        @Ignore
        @get:Exclude
        var receiptDocumentId: String? = null,

        @Ignore
        @get:Exclude
        var receiptInId: String? = null,

        @Ignore
        @get:Exclude
        var unAllocatedReceiptId: String? = null,

        @Ignore
        @get:Exclude
        var receiptNo: String? = null,

        /**
         * Receipt name
         * */
        @ColumnInfo(name = "rec_cmp_id")
        @set:PropertyName("rec_cmp_id")
        @get:PropertyName("rec_cmp_id")
        var receiptCompanyId: String? = null,

        /**
         * Receipt Type
         * */
        @ColumnInfo(name = "rec_type")
        @set:PropertyName("rec_type")
        @get:PropertyName("rec_type")
        var receiptType: String? = null,

        /**
         * Receipt TransactionCode
         * */
        @ColumnInfo(name = "rec_tt_code")
        @set:PropertyName("rec_tt_code")
        @get:PropertyName("rec_tt_code")
        var receiptTransCode: String? = null,

        /**
         * Receipt Address
         * */
        @ColumnInfo(name = "rec_transno")
        @set:PropertyName("rec_transno")
        @get:PropertyName("rec_transno")
        var receiptTransNo: String? = null,

        /**
         * Receipt ThirdParty
         * */
        @ColumnInfo(name = "rec_tp_name")
        @set:PropertyName("rec_tp_name")
        @get:PropertyName("rec_tp_name")
        var receiptThirdParty: String? = null,

        /**
         * Receipt ThirdParty name
         * */
        @Ignore
        @get:Exclude
        @set:Exclude
        var receiptThirdPartyName: String? = null,

        /**
         * Receipt Currency
         * */
        @ColumnInfo(name = "rec_cur_code")
        @set:PropertyName("rec_cur_code")
        @get:PropertyName("rec_cur_code")
        var receiptCurrency: String? = null,

        /**
         * Receipt Currency Code
         * */
        @Ignore
        @get:Exclude
        @set:Exclude
        var receiptCurrencyCode: String? = null,

        /**
         * Receipt Amount
         * */
        @ColumnInfo(name = "rec_amt")
        @set:PropertyName("rec_amt")
        @get:PropertyName("rec_amt")
        var receiptAmount: Double = 0.0,

        /**
         * Receipt Amount first
         * */
        @ColumnInfo(name = "rec_amtf")
        @set:PropertyName("rec_amtf")
        @get:PropertyName("rec_amtf")
        var receiptAmountFirst: Double = 0.0,

        /**
         * Receipt Amount second
         * */
        @ColumnInfo(name = "rec_amts")
        @set:PropertyName("rec_amts")
        @get:PropertyName("rec_amts")
        var receiptAmountSecond: Double = 0.0,

        /**
         * Receipt Description
         * */
        @ColumnInfo(name = "rec_desc")
        @set:PropertyName("rec_desc")
        @get:PropertyName("rec_desc")
        var receiptDesc: String? = null,

        /**
         * Receipt Note
         * */
        @ColumnInfo(name = "rec_note")
        @set:PropertyName("rec_note")
        @get:PropertyName("rec_note")
        var receiptNote: String? = null,

        /**
         * Receipt timestamp
         * */
        @Ignore
        @set:PropertyName("rec_timestamp")
        @get:PropertyName("rec_timestamp")
        @ServerTimestamp
        var receiptTimeStamp: Date? = null,

        /**
         * Receipt timestamp
         * */
        @ColumnInfo(name = "rec_datetime")
        @set:PropertyName("rec_datetime")
        @get:PropertyName("rec_datetime")
        var receiptDateTime: Long = System.currentTimeMillis(),

        /**
         * Receipt user stamp
         * */
        @ColumnInfo(name = "rec_userstamp")
        @set:PropertyName("rec_userstamp")
        @get:PropertyName("rec_userstamp")
        var receiptUserStamp: String? = null
) : DataModel() {
    constructor() : this("")

    @Exclude
    override fun getId(): String {
        return receiptId
    }

    @Exclude
    override fun getName(): String {
        val transNo = "${receiptTransCode ?: ""} ${receiptTransNo ?: ""}"
        val total = String.format(
            "%,.${SettingsModel.currentCurrency?.currencyName1Dec ?: 2}f",
            receiptAmount
        )
        val clientName = receiptThirdPartyName ?: ""
        return "$transNo $total $clientName"
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
            receiptDocumentId.isNullOrEmpty()
        } else {
            receiptId.isEmpty()
        }
    }

    @Exclude
    override fun prepareForInsert() {
        if (receiptId.isEmpty()) {
            receiptId = Utils.generateRandomUuidString()
        }

        receiptCompanyId = SettingsModel.getCompanyID()
        receiptUserStamp = SettingsModel.currentUserId
    }

    @Exclude
    fun didChanged(receipt: Receipt): Boolean {
        return !receipt.receiptDesc.equals(receiptDesc) || !receipt.receiptNote.equals(receiptNote) || !receipt.receiptThirdParty.equals(receiptThirdParty) || !receipt.receiptCurrency.equals(receiptCurrency) || !receipt.receiptType.equals(receiptType) || !receipt.receiptAmount.equals(receiptAmount)
    }

    @Exclude
    fun getSelectedCurrencyIndex(): Int {
        return when (receiptCurrency) {
            SettingsModel.currentCurrency?.currencyId, SettingsModel.currentCurrency?.currencyCode1 -> {
                1
            }

            SettingsModel.currentCurrency?.currencyDocumentId, SettingsModel.currentCurrency?.currencyCode2 -> {
                2
            }

            else -> {
                0
            }
        }
    }

    @Exclude
    fun calculateAmountsIfNeeded() {
        when (receiptCurrency) {
            SettingsModel.currentCurrency?.currencyId, SettingsModel.currentCurrency?.currencyCode1 -> {
                receiptAmountFirst = receiptAmount
            }

            SettingsModel.currentCurrency?.currencyDocumentId, SettingsModel.currentCurrency?.currencyCode2 -> {
                receiptAmountSecond = receiptAmount
            }

            else -> {
            }
        }
    }

    @Exclude
    fun getMap(): Map<String, Any?> {
        return mapOf(
            "rec_cmp_id" to receiptCompanyId,
            "rec_type" to receiptType,
            "rec_tt_code" to receiptTransCode,
            "rec_transno" to receiptTransNo,
            "rec_tp_name" to receiptThirdParty,
            "rec_cur_code" to receiptCurrency,
            "rec_amt" to receiptAmount,
            "rec_amtf" to receiptAmountFirst,
            "rec_amts" to receiptAmountSecond,
            "rec_desc" to receiptDesc,
            "rec_note" to receiptNote,
            "rec_timestamp" to receiptTimeStamp,
            "rec_userstamp" to receiptUserStamp,
        )
    }

    @Exclude
    fun getPaymentDate(): Date {
        if (receiptTimeStamp != null) {
            return receiptTimeStamp!!
        }
        return Date(receiptDateTime)
    }
}
