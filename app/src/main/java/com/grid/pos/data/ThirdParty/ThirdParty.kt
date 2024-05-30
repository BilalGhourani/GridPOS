package com.grid.pos.data.ThirdParty

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

@Entity(tableName = "thirdparty")
data class ThirdParty(
        /**
         * Third Party Id
         * */
        @PrimaryKey
        @ColumnInfo(name = "tp_id")
        @set:PropertyName("tp_id")
        @get:PropertyName("tp_id")
        var thirdPartyId: String,

        @Ignore
        @get:Exclude
        var thirdPartyDocumentId: String? = null,

        /**
         * Third Party name
         * */
        @ColumnInfo(name = "tp_name")
        @set:PropertyName("tp_name")
        @get:PropertyName("tp_name")
        var thirdPartyName: String? = null,

        /**
         * Third Party fn
         * */
        @ColumnInfo(name = "tp_fn")
        @set:PropertyName("tp_fn")
        @get:PropertyName("tp_fn")
        var thirdPartyFn: String? = null,

        /**
         * Third Party company id
         * */
        @ColumnInfo(name = "tp_cmp_id")
        @set:PropertyName("tp_cmp_id")
        @get:PropertyName("tp_cmp_id")
        var thirdPartyCompId: String? = null,

        /**
         * Third Party phone 1
         * */
        @ColumnInfo(name = "tp_phone1")
        @set:PropertyName("tp_phone1")
        @get:PropertyName("tp_phone1")
        var thirdPartyPhone1: String? = null,

        /**
         * Third Party phone 2
         * */
        @ColumnInfo(name = "tp_phone2")
        @set:PropertyName("tp_phone2")
        @get:PropertyName("tp_phone2")
        var thirdPartyPhone2: String? = null,

        /**
         * Third Party address
         * */
        @ColumnInfo(name = "tp_address")
        @set:PropertyName("tp_address")
        @get:PropertyName("tp_address")
        var thirdPartyAddress: String? = null,

        /**
         * Third Party default for POS
         * */
        @ColumnInfo(name = "tp_default")
        @set:PropertyName("tp_default")
        @get:PropertyName("tp_default")
        var thirdPartyDefault: Boolean = false,

        /**
         * Third Party time stamp
         * */
        @Ignore
        @set:PropertyName("tp_timestamp")
        @get:PropertyName("tp_timestamp")
        @ServerTimestamp
        var thirdPartyTimeStamp: Date? = null,

        /**
         * Third Party time stamp
         * */
        @ColumnInfo(name = "tp_datetime")
        @set:PropertyName("tp_datetime")
        @get:PropertyName("tp_datetime")
        var thirdPartyDateTime: Long = System.currentTimeMillis(),

        /**
         * Third Party user stamp
         * */
        @ColumnInfo(name = "tp_userstamp")
        @set:PropertyName("tp_userstamp")
        @get:PropertyName("tp_userstamp")
        var thirdPartyUserStamp: String? = null,

        ) : DataModel() {
    constructor() : this("")

    @Exclude
    override fun getId(): String {
        return thirdPartyId
    }

    @Exclude
    override fun getName(): String {
        return thirdPartyName ?: ""
    }

    @Exclude
    override fun isNew(): Boolean {
        return if (SettingsModel.isConnectedToSqlite()) {
            thirdPartyId.isEmpty()
        } else {
            thirdPartyDocumentId.isNullOrEmpty()
        }
    }

    @Exclude
    override fun prepareForInsert() {
        if (thirdPartyId.isNullOrEmpty()) {
            thirdPartyId = Utils.generateRandomUuidString()
        }
        thirdPartyCompId = SettingsModel.companyID
        thirdPartyUserStamp = SettingsModel.currentUserId
    }

    @Exclude
    fun getMap(): Map<String, Any?> {
        return mapOf(
            "tp_id" to thirdPartyId,
            "tp_name" to thirdPartyName,
            "tp_fn" to thirdPartyFn,
            "tp_cmp_id" to thirdPartyCompId,
            "tp_phone1" to thirdPartyPhone1,
            "tp_phone2" to thirdPartyPhone2,
            "tp_address" to thirdPartyAddress,
            "tp_default" to thirdPartyDefault,
            "tp_timestamp" to FieldValue.serverTimestamp(),
            "tp_userstamp" to thirdPartyUserStamp,
            "tp_datetime" to thirdPartyDateTime,
        )
    }
}
