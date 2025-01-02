package com.grid.pos.data.family

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.grid.pos.data.EntityModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Utils

@Entity(tableName = "st_family")
data class Family(
        /**
         * Family Id
         * */
        @PrimaryKey
        @ColumnInfo(name = "fa_id")
        @set:PropertyName("fa_id")
        @get:PropertyName("fa_id")
        var familyId: String,

        @Ignore
        @get:Exclude
        var familyDocumentId: String? = null,

        /**
         * Family name
         * */
        @ColumnInfo(name = "fa_name")
        @set:PropertyName("fa_name")
        @get:PropertyName("fa_name")
        var familyName: String? = null,

        /**
         * related Company Id
         * */
        @ColumnInfo(name = "fa_cmp_id")
        @set:PropertyName("fa_cmp_id")
        @get:PropertyName("fa_cmp_id")
        var familyCompanyId: String? = null,

        /**
         * family image
         * */
        @ColumnInfo(name = "fa_image")
        @set:PropertyName("fa_image")
        @get:PropertyName("fa_image")
        var familyImage: String? = null,

        ) : EntityModel() {
    constructor() : this("")

    @Exclude
    override fun getId(): String {
        return familyId
    }

    @Exclude
    override fun getName(): String {
        return familyName ?: ""
    }

    @Exclude
    override fun search(key: String): Boolean {
        return getName().contains(
            key,
            ignoreCase = true
        )
    }

    @Exclude
    fun getFullFamilyImage(): String {
        familyImage?.let {
            if (it.startsWith("/")) {
                return "file://$it"
            }
            return it
        }
        return ""
    }

    @Exclude
    override fun isNew(): Boolean {
        return if (SettingsModel.isConnectedToFireStore()) {
            familyDocumentId.isNullOrEmpty()
        } else {
            familyId.isEmpty()
        }
    }

    @Exclude
    override fun prepareForInsert() {
        if (familyId.isEmpty()) {
            familyId = Utils.generateRandomUuidString()
        }
        familyCompanyId = SettingsModel.getCompanyID()
    }

    @Exclude
    fun didChanged(family: Family): Boolean {
        return !family.familyName.equals(familyName)
                || !family.familyImage.equals(familyImage)
    }

    @Exclude
    override fun setDocumentId(documentId: String) {
        familyDocumentId = documentId
    }

    @Exclude
    override fun getDocumentId(): String? {
        return familyDocumentId
    }

    @Exclude
    override fun getMap(): Map<String, Any?> {
        return mapOf(
            "fa_name" to familyName, "fa_cmp_id" to familyCompanyId, "fa_image" to familyImage
        )
    }
}