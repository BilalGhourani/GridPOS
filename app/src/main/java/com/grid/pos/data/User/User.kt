package com.grid.pos.data.User

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.grid.pos.data.DataModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Utils

@Entity(tableName = "set_users")
data class User(
        /**
         * User Id
         * */
        @PrimaryKey
        @ColumnInfo(name = "usr_id")
        @set:PropertyName("usr_id")
        @get:PropertyName("usr_id")
        var userId: String,

        @Ignore
        @get:Exclude
        var userDocumentId: String? = null,

        /**
         * User name
         * */
        @ColumnInfo(name = "usr_name")
        @set:PropertyName("usr_name")
        @get:PropertyName("usr_name")
        var userName: String? = null,

        /**
         * User username
         * */
        @ColumnInfo(name = "usr_username")
        @set:PropertyName("usr_username")
        @get:PropertyName("usr_username")
        var userUsername: String? = null,

        /**
         * User password
         * */
        @ColumnInfo(name = "usr_password")
        @set:PropertyName("usr_password")
        @get:PropertyName("usr_password")
        var userPassword: String? = null,

        /**
         * related Company Id
         * */
        @ColumnInfo(name = "usr_cmp_id")
        @set:PropertyName("usr_cmp_id")
        @get:PropertyName("usr_cmp_id")
        var userCompanyId: String? = null,

        /**
         * related Code
         * */
        @ColumnInfo(name = "usr_posmode")
        @set:PropertyName("usr_posmode")
        @get:PropertyName("usr_posmode")
        var userPosMode: Boolean = true,

        /**
         * related Email
         * */
        @ColumnInfo(name = "usr_tablemode")
        @set:PropertyName("usr_tablemode")
        @get:PropertyName("usr_tablemode")
        var userTableMode: Boolean = true,

        ) : DataModel() {
    constructor() : this("")

    @Exclude
    override fun getId(): String {
        return userId
    }

    @Exclude
    override fun getName(): String {
        return userName ?: ""
    }

    @Exclude
    override fun isNew(): Boolean {
        return if (SettingsModel.isConnectedToFireStore()) {
            userDocumentId.isNullOrEmpty()
        } else {
            userId.isEmpty()
        }
    }

    @Exclude
    override fun prepareForInsert() {
        if (userId.isEmpty()) {
            userId = Utils.generateRandomUuidString()
        }
        userCompanyId = SettingsModel.getCompanyID()
    }

    @Exclude
    fun getMap(): Map<String, Any?> {
        return mapOf(
            "usr_name" to userName,
            "usr_cmp_id" to userCompanyId,
            "usr_password" to userPassword,
            "usr_username" to userUsername,
            "usr_posmode" to userPosMode,
            "usr_tablemode" to userTableMode,
        )
    }
}
