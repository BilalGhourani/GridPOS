package com.grid.pos.data.user

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.DataModel
import com.grid.pos.model.LoginResponse
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Constants
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Extension.encryptCBC
import com.grid.pos.utils.Extension.getStringValue
import kotlinx.coroutines.tasks.await
import java.sql.ResultSet
import java.util.Date

class UserRepositoryImpl(
        private val userDao: UserDao
) : UserRepository {
    override suspend fun insert(
            user: User
    ): DataModel {
        if (SettingsModel.isConnectedToFireStore()) {
            val docRef = FirebaseFirestore.getInstance().collection("set_users").add(user).await()
            user.userDocumentId = docRef.id
            return DataModel(user)
        } else {
            userDao.insert(user)
            return DataModel(user)
        }
    }

    override suspend fun delete(
            user: User
    ): DataModel {
        if (SettingsModel.isConnectedToFireStore()) {
            user.userDocumentId?.let {
                FirebaseFirestore.getInstance().collection("set_users").document(it).delete()
                    .await()
                return DataModel(user)
            }
            return DataModel(
                user,
                false
            )
        } else {
            userDao.delete(user)
            return DataModel(user)
        }
    }

    override suspend fun update(
            user: User
    ): DataModel {
        if (SettingsModel.isConnectedToFireStore()) {
            user.userDocumentId?.let {
                FirebaseFirestore.getInstance().collection("set_users").document(it)
                    .update(user.getMap()).await()
                return DataModel(user)
            }
            return DataModel(
                user,
                false
            )
        } else {
            userDao.update(user)
            return DataModel(user)
        }
    }

    override suspend fun getUserByCredentials(
            username: String,
            password: String
    ): LoginResponse {
        val encryptedPassword = password.encryptCBC()
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("set_users")
                    .whereEqualTo(
                        "usr_cmp_id",
                        SettingsModel.getCompanyID()
                    ).get().await()
                val size = querySnapshot.size()
                val result = LoginResponse(allUsersSize = size)
                if (size > 0) {
                    for (document in querySnapshot) {
                        val obj = document.toObject(User::class.java)
                        if (obj.userId.isNotEmpty() && username.equals(
                                obj.userUsername,
                                ignoreCase = true
                            ) && encryptedPassword.equals(
                                obj.userPassword,
                                ignoreCase = true
                            )
                        ) {
                            obj.userDocumentId = document.id
                            result.user = obj
                            return result
                        }
                    }
                }
                if (username.equals(
                        "administrator",
                        ignoreCase = true
                    )
                ) {
                    result.user = User(
                        "administrator",
                        null,
                        "Administrator",
                        "administrator",
                        DateHelper.getDateInFormat(
                            Date(),
                            "dd-MMM-yyyy"
                        ).encryptCBC()
                    )
                }
                return result
            }

            CONNECTION_TYPE.LOCAL.key -> {
                val users = userDao.getAllUsers(SettingsModel.getCompanyID() ?: "")
                val size = users.size
                val result = LoginResponse(allUsersSize = size)
                users.forEach { user ->
                    if (username.equals(
                            user.userUsername,
                            ignoreCase = true
                        ) && encryptedPassword.equals(
                            user.userPassword,
                            ignoreCase = true
                        )
                    ) {
                        result.user = user
                        return result
                    }
                }


                if (username.equals(
                        "administrator",
                        ignoreCase = true
                    )
                ) {
                    result.user = User(
                        "administrator",
                        null,
                        "Administrator",
                        "administrator",
                        DateHelper.getDateInFormat(
                            Date(),
                            "dd-MMM-yyyy"
                        ).encryptCBC()
                    )
                }
                return result

            }

            else -> {
                if (SettingsModel.isSqlServerWebDb) {
                    val users: MutableList<User> = mutableListOf()
                    var error: String? = null
                    try {
                        val where = "usr_username = '$username' AND usr_password = hashBytes('SHA2_512', CONVERT(nvarchar(4000),'$password'+cast(usr_salt as nvarchar(36)))) AND usr_expiry > getdate() AND usr_cmp_id='${SettingsModel.getCompanyID()}'"
                        val dbResult = SQLServerWrapper.getListOf(
                            "set_users",
                            "",
                            mutableListOf("*"),
                            where
                        )
                        if (dbResult.succeed) {
                            (dbResult.result as? ResultSet)?.let {
                                while (it.next()) {
                                    users.add(User().apply {
                                        userId = it.getStringValue("usr_id")
                                        userName = it.getStringValue("usr_name")
                                        userUsername = it.getStringValue("usr_username")
                                        userPassword = it.getStringValue("usr_password")
                                        userCompanyId = it.getStringValue("usr_cmp_id")
                                        userGrpDesc = it.getStringValue("usr_grp_desc")
                                        userPosMode = Constants.SQL_USER_POS_MODE
                                        userTableMode = true
                                    })
                                }
                                SQLServerWrapper.closeResultSet(it)
                            } ?: run { error = (dbResult.result as? String) ?: "database connection error" }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (users.isNotEmpty()) {
                        return LoginResponse(
                            allUsersSize = 1,
                            user = users[0],
                            error = error
                        )
                    }
                    return LoginResponse(
                        allUsersSize = 1,
                        error = error
                    )
                } else {
                    val users: MutableList<User> = mutableListOf()
                    var error: String? = null
                    try {
                        val where = "emp_username = '$username' AND emp_password = hashBytes('SHA', CONVERT(nvarchar(4000),'$password')) and (emp_inactive IS NULL or emp_inactive = 0)"
                        val dbResult = SQLServerWrapper.getListOf(
                            "pay_employees",
                            "",
                            mutableListOf("*"),
                            where
                        )
                        if (dbResult.succeed) {
                            (dbResult.result as? ResultSet)?.let {
                                while (it.next()) {
                                    users.add(User().apply {
                                        userId = it.getStringValue("emp_id")
                                        userName = it.getStringValue("emp_name")
                                        userUsername = it.getStringValue("emp_username")
                                        userPassword = it.getStringValue("emp_password")
                                        userGrpDesc = it.getStringValue("emp_grp_desc")
                                        userCompanyId = SettingsModel.getCompanyID()//obj.optString("usr_cmp_id")
                                        userPosMode = Constants.SQL_USER_POS_MODE
                                        userTableMode = true
                                    })
                                }
                                SQLServerWrapper.closeResultSet(it)
                            } ?: run { error = (dbResult.result as? String) ?: "database connection error" }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (users.isNotEmpty()) {
                        return LoginResponse(
                            allUsersSize = 1,
                            user = users[0],
                            error = error
                        )
                    }
                    return LoginResponse(
                        allUsersSize = 1,
                        error = error
                    )
                }
            }
        }
    }

    override suspend fun getAllUsers(): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("set_users")
                    .whereEqualTo(
                        "usr_cmp_id",
                        SettingsModel.getCompanyID()
                    ).get().await()

                val users = mutableListOf<User>()
                if (querySnapshot.size() > 0) {
                    for (document in querySnapshot) {
                        val obj = document.toObject(User::class.java)
                        if (obj.userId.isNotEmpty()) {
                            obj.userDocumentId = document.id
                            users.add(obj)
                        }
                    }
                }
                return DataModel(users)
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return DataModel(userDao.getAllUsers(SettingsModel.getCompanyID() ?: ""))
            }

            else -> {//CONNECTION_TYPE.SQL_SERVER.key
                if (SettingsModel.isSqlServerWebDb) {
                    val users: MutableList<User> = mutableListOf()
                    try {
                        val where = "usr_cmp_id='${SettingsModel.getCompanyID()}'"
                        val dbResult = SQLServerWrapper.getListOf(
                            "set_users",
                            "",
                            mutableListOf("*"),
                            where
                        )
                        if (dbResult.succeed) {
                            (dbResult.result as? ResultSet)?.let {
                                while (it.next()) {
                                    users.add(User().apply {
                                        userId = it.getStringValue("usr_id")
                                        userName = it.getStringValue("usr_name")
                                        userUsername = it.getStringValue("usr_username")
                                        userPassword = it.getStringValue("usr_password")
                                        userCompanyId = it.getStringValue("usr_cmp_id")
                                        userPosMode = Constants.SQL_USER_POS_MODE
                                        userTableMode = true
                                    })
                                }
                                SQLServerWrapper.closeResultSet(it)
                            }
                        } else {
                            return DataModel(
                                null,
                                false,
                                dbResult.result as? String
                            )
                        }
                        return DataModel(users)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return DataModel(
                            null,
                            false,
                            e.message
                        )
                    }
                } else {
                    val users: MutableList<User> = mutableListOf()
                    try {
                        val dbResult = SQLServerWrapper.getListOf(
                            "pay_employees",
                            "",
                            mutableListOf("*"),
                            ""
                        )

                        if (dbResult.succeed) {
                            (dbResult.result as? ResultSet)?.let {
                                while (it.next()) {
                                    users.add(User().apply {
                                        userId = it.getStringValue("emp_id")
                                        userName = it.getStringValue("emp_name")
                                        userUsername = it.getStringValue("emp_username")
                                        userPassword = it.getStringValue("emp_password")
                                        userCompanyId = SettingsModel.getCompanyID()//obj.optString("usr_cmp_id")
                                        userPosMode = Constants.SQL_USER_POS_MODE
                                        userTableMode = true
                                    })
                                }
                                SQLServerWrapper.closeResultSet(it)
                            }
                        } else {
                            return DataModel(
                                null,
                                false,
                                dbResult.result as? String
                            )
                        }
                        return DataModel(users)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return DataModel(
                            null,
                            false,
                            e.message
                        )
                    }
                }
            }
        }
    }

    override suspend fun getOneUser(companyId: String): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("set_users")
                    .whereEqualTo(
                        "usr_cmp_id",
                        companyId
                    ).limit(1).get().await()
                if (querySnapshot.size() > 0) {
                    for (document in querySnapshot) {
                        val obj = document.toObject(User::class.java)
                        if (obj.userId.isNotEmpty()) {
                            obj.userDocumentId = document.id
                            return DataModel(obj)
                        }
                    }
                }
                return DataModel(null)
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return DataModel(userDao.getOneUser(companyId))
            }

            else -> {//CONNECTION_TYPE.SQL_SERVER.key
                if (SettingsModel.isSqlServerWebDb) {
                    try {
                        var user: User? = null
                        val where = "usr_cmp_id='$companyId'"
                        val dbResult = SQLServerWrapper.getListOf(
                            "set_users",
                            "TOP 1",
                            mutableListOf("*"),
                            where
                        )

                        if (dbResult.succeed) {
                            (dbResult.result as? ResultSet)?.let {
                                if (it.next()) {
                                    user = User().apply {
                                        userId = it.getStringValue("usr_id")
                                        userName = it.getStringValue("usr_name")
                                        userUsername = it.getStringValue("usr_username")
                                        userPassword = it.getStringValue("usr_password")
                                        userCompanyId = it.getStringValue("usr_cmp_id")
                                        userPosMode = Constants.SQL_USER_POS_MODE
                                        userTableMode = true
                                    }
                                }
                                SQLServerWrapper.closeResultSet(it)
                            }
                        } else {
                            return DataModel(
                                null,
                                false,
                                dbResult.result as? String
                            )
                        }
                        return DataModel(user)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return DataModel(
                            null,
                            false,
                            e.message
                        )
                    }
                } else {
                    try {
                        var user: User? = null
                        val dbResult = SQLServerWrapper.getListOf(
                            "pay_employees",
                            "TOP 1",
                            mutableListOf("*"),
                            ""
                        )
                        if (dbResult.succeed) {
                            (dbResult.result as? ResultSet)?.let {
                                while (it.next()) {
                                    user = User().apply {
                                        userId = it.getStringValue("emp_id")
                                        userName = it.getStringValue("emp_name")
                                        userUsername = it.getStringValue("emp_username")
                                        userPassword = it.getStringValue("emp_password")
                                        userCompanyId = SettingsModel.getCompanyID()//obj.optString("usr_cmp_id")
                                        userPosMode = Constants.SQL_USER_POS_MODE
                                        userTableMode = true
                                    }
                                }
                                SQLServerWrapper.closeResultSet(it)
                            }
                        } else {
                            return DataModel(
                                null,
                                false,
                                dbResult.result as? String
                            )
                        }
                        return DataModel(user)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return DataModel(
                            null,
                            false,
                            e.message
                        )
                    }
                }
            }
        }
    }

    override suspend fun getUserById(userId: String): DataModel {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("set_users")
                    .whereEqualTo(
                        "usr_id",
                        userId
                    ).limit(1).get().await()
                if (querySnapshot.size() > 0) {
                    for (document in querySnapshot) {
                        val obj = document.toObject(User::class.java)
                        if (obj.userId.isNotEmpty()) {
                            obj.userDocumentId = document.id
                            return DataModel(obj)
                        }
                    }
                }
                return DataModel(null)
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return DataModel(userDao.getUserById(userId))
            }

            else -> {//CONNECTION_TYPE.SQL_SERVER.key
                if (SettingsModel.isSqlServerWebDb) {
                    try {
                        var user: User? = null
                        val where = "usr_username='$userId'"
                        val dbResult = SQLServerWrapper.getListOf(
                            "set_users",
                            "TOP 1",
                            mutableListOf("*"),
                            where
                        )

                        if (dbResult.succeed) {
                            (dbResult.result as? ResultSet)?.let {
                                if (it.next()) {
                                    user = User().apply {
                                        this.userId = it.getStringValue("usr_id")
                                        userName = it.getStringValue("usr_name")
                                        userUsername = it.getStringValue("usr_username")
                                        userPassword = it.getStringValue("usr_password")
                                        userCompanyId = it.getStringValue("usr_cmp_id")
                                        userPosMode = Constants.SQL_USER_POS_MODE
                                        userTableMode = true
                                    }
                                }
                                SQLServerWrapper.closeResultSet(it)
                            }
                        } else {
                            return DataModel(
                                null,
                                false,
                                dbResult.result as? String
                            )
                        }
                        return DataModel(user)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return DataModel(
                            null,
                            false,
                            e.message
                        )
                    }
                } else {
                    try {
                        var user: User? = null
                        val where = "emp_username='$userId'"
                        val dbResult = SQLServerWrapper.getListOf(
                            "pay_employees",
                            "TOP 1",
                            mutableListOf("*"),
                            where
                        )
                        if (dbResult.succeed) {
                            (dbResult.result as? ResultSet)?.let {
                                while (it.next()) {
                                    user = User().apply {
                                        this.userId = it.getStringValue("emp_id")
                                        userName = it.getStringValue("emp_name")
                                        userUsername = it.getStringValue("emp_username")
                                        userPassword = it.getStringValue("emp_password")
                                        userCompanyId = SettingsModel.getCompanyID()//obj.optString("usr_cmp_id")
                                        userPosMode = Constants.SQL_USER_POS_MODE
                                        userTableMode = true
                                    }
                                }
                                SQLServerWrapper.closeResultSet(it)
                            }
                        } else {
                            return DataModel(
                                null,
                                false,
                                dbResult.result as? String
                            )
                        }
                        return DataModel(user)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return DataModel(
                            null,
                            false,
                            e.message
                        )
                    }
                }
            }
        }
    }
}