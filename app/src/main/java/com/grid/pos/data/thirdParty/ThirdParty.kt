package com.grid.pos.data.thirdParty

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
import com.grid.pos.utils.Utils
import java.util.Date

@Entity(tableName = "thirdparty")
data class ThirdParty(
        /**
         * ThirdParty Id
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
         * ThirdParty name
         * */
        @ColumnInfo(name = "tp_name")
        @set:PropertyName("tp_name")
        @get:PropertyName("tp_name")
        var thirdPartyName: String? = null,

        /**
         * ThirdParty fn
         * */
        @ColumnInfo(name = "tp_fn")
        @set:PropertyName("tp_fn")
        @get:PropertyName("tp_fn")
        var thirdPartyFn: String? = null,

        /**
         * ThirdParty company id
         * */
        @ColumnInfo(name = "tp_cmp_id")
        @set:PropertyName("tp_cmp_id")
        @get:PropertyName("tp_cmp_id")
        var thirdPartyCompId: String? = null,

        /**
         * ThirdParty Type
         * */
        @ColumnInfo(name = "tp_cse")
        @set:PropertyName("tp_cse")
        @get:PropertyName("tp_cse")
        var thirdPartyType: String? = null,

        /**
         * ThirdParty phone 1
         * */
        @ColumnInfo(name = "tp_phone1")
        @set:PropertyName("tp_phone1")
        @get:PropertyName("tp_phone1")
        var thirdPartyPhone1: String? = null,

        /**
         * ThirdParty phone 2
         * */
        @ColumnInfo(name = "tp_phone2")
        @set:PropertyName("tp_phone2")
        @get:PropertyName("tp_phone2")
        var thirdPartyPhone2: String? = null,

        /**
         * ThirdParty address
         * */
        @ColumnInfo(name = "tp_address")
        @set:PropertyName("tp_address")
        @get:PropertyName("tp_address")
        var thirdPartyAddress: String? = null,

        /**
         * ThirdParty default for POS
         * */
        @ColumnInfo(name = "tp_default")
        @set:PropertyName("tp_default")
        @get:PropertyName("tp_default")
        var thirdPartyDefault: Boolean = false,

        /**
         * ThirdParty time stamp
         * */
        @Ignore
        @set:PropertyName("tp_timestamp")
        @get:PropertyName("tp_timestamp")
        @ServerTimestamp
        var thirdPartyTimeStamp: Date? = null,

        /**
         * ThirdParty time stamp
         * */
        @ColumnInfo(name = "tp_datetime")
        @set:PropertyName("tp_datetime")
        @get:PropertyName("tp_datetime")
        var thirdPartyDateTime: Long = System.currentTimeMillis(),

        /**
         * ThirdParty user stamp
         * */
        @ColumnInfo(name = "tp_userstamp")
        @set:PropertyName("tp_userstamp")
        @get:PropertyName("tp_userstamp")
        var thirdPartyUserStamp: String? = null,

        ) : EntityModel() {
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
    override fun search(key: String): Boolean {
        return getName().contains(
            key,
            ignoreCase = true
        )
    }

    @Exclude
    override fun isNew(): Boolean {
        return if (SettingsModel.isConnectedToFireStore()) {
            thirdPartyDocumentId.isNullOrEmpty()
        } else {
            thirdPartyId.isEmpty()
        }
    }

    @Exclude
    override fun prepareForInsert() {
        if (thirdPartyId.isEmpty()) {
            thirdPartyId = Utils.generateRandomUuidString()
        }
        thirdPartyCompId = SettingsModel.getCompanyID()
        thirdPartyUserStamp = SettingsModel.currentUser?.userId
    }

    @Exclude
    fun didChanged(thirdParty: ThirdParty): Boolean {
        return !thirdParty.thirdPartyName.equals(thirdPartyName)
                || !thirdParty.thirdPartyFn.equals(thirdPartyFn)
                || !thirdParty.thirdPartyAddress.equals(thirdPartyAddress)
                || !thirdParty.thirdPartyPhone1.equals(thirdPartyPhone1)
                || !thirdParty.thirdPartyPhone2.equals(thirdPartyPhone2)
    }

    @Exclude
    override fun setDocumentId(documentId: String) {
        thirdPartyDocumentId = documentId
    }

    @Exclude
    override fun getDocumentId(): String? {
        return thirdPartyDocumentId
    }

    @Exclude
    override fun getMap(): Map<String, Any?> {
        return mapOf(
            "tp_id" to thirdPartyId,
            "tp_name" to thirdPartyName,
            "tp_fn" to thirdPartyFn,
            "tp_cmp_id" to thirdPartyCompId,
            "tp_cse" to thirdPartyType,
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