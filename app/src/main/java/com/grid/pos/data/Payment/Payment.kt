package com.grid.pos.data.Payment

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

@Entity(tableName = "payment")
data class Payment(
        /**
         * Payment id
         * */
        @PrimaryKey
        @ColumnInfo(name = "pay_id")
        @set:PropertyName("pay_id")
        @get:PropertyName("pay_id")
        var paymentId: String,

        @Ignore
        @get:Exclude
        var paymentDocumentId: String? = null,

        /**
         * Payment name
         * */
        @ColumnInfo(name = "pay_cmp_id")
        @set:PropertyName("pay_cmp_id")
        @get:PropertyName("pay_cmp_id")
        var paymentCompanyId: String? = null,

        /**
         * Payment Type
         * */
        @ColumnInfo(name = "pay_type")
        @set:PropertyName("pay_type")
        @get:PropertyName("pay_type")
        var paymentType: String? = null,

        /**
         * Payment TransactionCode
         * */
        @ColumnInfo(name = "pay_tt_code")
        @set:PropertyName("pay_tt_code")
        @get:PropertyName("pay_tt_code")
        var paymentTransCode: String? = null,

        /**
         * Payment Address
         * */
        @ColumnInfo(name = "pay_transno")
        @set:PropertyName("pay_transno")
        @get:PropertyName("pay_transno")
        var paymentTransNo: String? = null,

        /**
         * Payment ThirdParty
         * */
        @ColumnInfo(name = "pay_tp_name")
        @set:PropertyName("pay_tp_name")
        @get:PropertyName("pay_tp_name")
        var paymentThirdParty: String? = null,

        /**
         * payment ThirdParty name
         * */
        @Ignore
        @get:Exclude
        @set:Exclude
        var paymentThirdPartyName: String? = null,

        /**
         * Payment Currency
         * */
        @ColumnInfo(name = "pay_cur_code")
        @set:PropertyName("pay_cur_code")
        @get:PropertyName("pay_cur_code")
        var paymentCurrency: String? = null,

        /**
         * Payment Amount
         * */
        @ColumnInfo(name = "pay_amt")
        @set:PropertyName("pay_amt")
        @get:PropertyName("pay_amt")
        var paymentAmount: Double = 0.0,

        /**
         * Payment Amount first
         * */
        @ColumnInfo(name = "pay_amtf")
        @set:PropertyName("pay_amtf")
        @get:PropertyName("pay_amtf")
        var paymentAmountFirst: Double = 0.0,

        /**
         * Payment Amount second
         * */
        @ColumnInfo(name = "pay_amts")
        @set:PropertyName("pay_amts")
        @get:PropertyName("pay_amts")
        var paymentAmountSecond: Double = 0.0,

        /**
         * Payment Description
         * */
        @ColumnInfo(name = "pay_desc")
        @set:PropertyName("pay_desc")
        @get:PropertyName("pay_desc")
        var paymentDesc: String? = null,

        /**
         * Payment Note
         * */
        @ColumnInfo(name = "pay_note")
        @set:PropertyName("pay_note")
        @get:PropertyName("pay_note")
        var paymentNote: String? = null,

        /**
         * Payment timestamp
         * */
        @Ignore
        @set:PropertyName("pay_timestamp")
        @get:PropertyName("pay_timestamp")
        @ServerTimestamp
        var paymentTimeStamp: Date? = null,

        /**
         * Payment timestamp
         * */
        @ColumnInfo(name = "pay_datetime")
        @set:PropertyName("pay_datetime")
        @get:PropertyName("pay_datetime")
        var paymentDateTime: Long = System.currentTimeMillis(),

        /**
         * Payment user stamp
         * */
        @ColumnInfo(name = "pay_userstamp")
        @set:PropertyName("pay_userstamp")
        @get:PropertyName("pay_userstamp")
        var paymentUserStamp: String? = null
) : DataModel() {
    constructor() : this("")

    @Exclude
    override fun getId(): String {
        return paymentId
    }

    @Exclude
    override fun getName(): String {
        val transNo = paymentTransNo ?: ""
        val total = String.format(
            "%,.${SettingsModel.currentCurrency?.currencyName1Dec ?: 2}f",
            paymentAmount
        )
        val clientName = paymentThirdPartyName ?: ""
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
            paymentDocumentId.isNullOrEmpty()
        } else {
            paymentId.isEmpty()
        }
    }

    @Exclude
    override fun prepareForInsert() {
        if (paymentId.isEmpty()) {
            paymentId = Utils.generateRandomUuidString()
        }

        paymentCompanyId = SettingsModel.getCompanyID()
        paymentUserStamp = SettingsModel.currentUserId
    }

    @Exclude
    fun didChanged(payment: Payment): Boolean {
        return !payment.paymentDesc.equals(paymentDesc) || !payment.paymentNote.equals(paymentNote) || !payment.paymentThirdParty.equals(paymentThirdParty) || !payment.paymentCurrency.equals(paymentCurrency) || !payment.paymentType.equals(paymentType) || !payment.paymentAmount.equals(paymentAmount)
    }

    @Exclude
    fun getSelectedCurrencyIndex(): Int {
        return when (paymentCurrency) {
            SettingsModel.currentCurrency?.currencyCode1 -> {
                1
            }

            SettingsModel.currentCurrency?.currencyCode2 -> {
                2
            }

            else -> {
                0
            }
        }
    }

    @Exclude
    fun calculateAmountsIfNeeded() {
        when (paymentCurrency) {
            SettingsModel.currentCurrency?.currencyCode1 -> {
                paymentAmountFirst = paymentAmount
            }

            SettingsModel.currentCurrency?.currencyCode2 -> {
                paymentAmountSecond = paymentAmount
            }

            else -> {
            }
        }
    }

    @Exclude
    fun getMap(): Map<String, Any?> {
        return mapOf(
            "pay_cmp_id" to paymentCompanyId,
            "pay_type" to paymentType,
            "pay_tt_code" to paymentTransCode,
            "pay_transno" to paymentTransNo,
            "pay_tp_name" to paymentThirdParty,
            "pay_cur_code" to paymentCurrency,
            "pay_amt" to paymentAmount,
            "pay_amtf" to paymentAmountFirst,
            "pay_amts" to paymentAmountSecond,
            "pay_desc" to paymentDesc,
            "pay_note" to paymentNote,
            "pay_timestamp" to paymentTimeStamp,
            "pay_userstamp" to paymentUserStamp,
        )
    }
}
