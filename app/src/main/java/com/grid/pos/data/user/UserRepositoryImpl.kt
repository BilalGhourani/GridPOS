package com.grid.pos.data.user

import com.google.firebase.firestore.Filter
import com.grid.pos.data.FirebaseWrapper
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.DataModel
import com.grid.pos.model.LoginResponse
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Constants
import com.grid.pos.utils.DateHelper
import com.grid.pos.utils.Extension.encryptCBC
import com.grid.pos.utils.Extension.getStringValue
import java.util.Date

class UserRepositoryImpl(
        private val userDao: UserDao
) : UserRepository {
    override suspend fun insert(
            user: User
    ): DataModel {
        return if (SettingsModel.isConnectedToFireStore()) {
            FirebaseWrapper.insert(
                "set_users",
                user
            )
        } else {
            userDao.insert(user)
            DataModel(user)
        }
    }

    override suspend fun delete(
            user: User
    ): DataModel {
        return if (SettingsModel.isConnectedToFireStore()) {
            FirebaseWrapper.delete(
                "set_users",
                user
            )
        } else {
            userDao.delete(user)
            DataModel(user)
        }
    }

    override suspend fun update(
            user: User
    ): DataModel {
        return if (SettingsModel.isConnectedToFireStore()) {
            FirebaseWrapper.update(
                "set_users",
                user
            )
        } else {
            userDao.update(user)
            DataModel(user)
        }
    }

    override suspend fun getUserByCredentials(
            username: String,
            password: String
    ): LoginResponse {
        val encryptedPassword = password.encryptCBC()
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "set_users",
                    filters = mutableListOf(
                        Filter.equalTo(
                            "usr_cmp_id",
                            SettingsModel.getCompanyID()
                        )
                    )
                )
                val size = querySnapshot?.size() ?: 0
                val result = LoginResponse(allUsersSize = size)
                if (size > 0) {
                    for (document in querySnapshot!!) {
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
                        dbResult?.let {
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
                        } ?: run { error = "database connection error" }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (users.isNotEmpty()) {
                        return LoginResponse(
                            allUsersSize = 1,
                            user = users[0]
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
                        dbResult?.let {
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
                        } ?: run { error = "database connection error" }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (users.isNotEmpty()) {
                        return LoginResponse(
                            allUsersSize = 1,
                            user = users[0]
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

    override suspend fun getAllUsers(): MutableList<User> {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "set_users",
                    filters = mutableListOf(
                        Filter.equalTo(
                            "usr_cmp_id",
                            SettingsModel.getCompanyID()
                        )
                    )
                )
                val size = querySnapshot?.size() ?: 0
                val users = mutableListOf<User>()
                if (size > 0) {
                    for (document in querySnapshot!!) {
                        val obj = document.toObject(User::class.java)
                        if (obj.userId.isNotEmpty()) {
                            obj.userDocumentId = document.id
                            users.add(obj)
                        }
                    }
                }
                return   users
            }

            CONNECTION_TYPE.LOCAL.key -> {
              return  userDao.getAllUsers(SettingsModel.getCompanyID() ?: "")
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
                        dbResult?.let {
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
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                   return users
                } else {
                    val users: MutableList<User> = mutableListOf()
                    try {
                        val dbResult = SQLServerWrapper.getListOf(
                            "pay_employees",
                            "",
                            mutableListOf("*"),
                            ""
                        )

                        dbResult?.let {
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
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                  return  users
                }
            }
        }
    }

    override suspend fun getOneUser(companyId: String): User? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "set_users",
                    limit = 1,
                    filters = mutableListOf(
                        Filter.equalTo(
                            "usr_cmp_id",
                            SettingsModel.getCompanyID()
                        )
                    )
                )
                val size = querySnapshot?.size() ?: 0
                if (size > 0) {
                    for (document in querySnapshot!!) {
                        val obj = document.toObject(User::class.java)
                        if (obj.userId.isNotEmpty()) {
                            obj.userDocumentId = document.id
                            return obj
                        }
                    }
                }
                return null
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return userDao.getOneUser(companyId)
            }

            else -> {//CONNECTION_TYPE.SQL_SERVER.key
                if (SettingsModel.isSqlServerWebDb) {
                    var user: User? = null
                    try {
                        val where = "usr_cmp_id='$companyId'"
                        val dbResult = SQLServerWrapper.getListOf(
                            "set_users",
                            "TOP 1",
                            mutableListOf("*"),
                            where
                        )

                        dbResult?.let {
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
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return user
                } else {
                    var user: User? = null
                    try {
                        val dbResult = SQLServerWrapper.getListOf(
                            "pay_employees",
                            "TOP 1",
                            mutableListOf("*"),
                            ""
                        )
                        dbResult?.let {
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
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return user
                }
            }
        }
    }

    override suspend fun getUserById(userId: String): User? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseWrapper.getQuerySnapshot(
                    collection = "set_users",
                    limit = 1,
                    filters = mutableListOf(
                        Filter.equalTo(
                            "usr_id",
                            userId
                        )
                    )
                )
                val size = querySnapshot?.size() ?: 0
                if (size > 0) {
                    for (document in querySnapshot!!) {
                        val obj = document.toObject(User::class.java)
                        if (obj.userId.isNotEmpty()) {
                            obj.userDocumentId = document.id
                            return obj
                        }
                    }
                }
                return null
            }

            CONNECTION_TYPE.LOCAL.key -> {
                return userDao.getUserById(userId)
            }

            else -> {//CONNECTION_TYPE.SQL_SERVER.key
                if (SettingsModel.isSqlServerWebDb) {
                    var user: User? = null
                    try {
                        val where = "usr_username='$userId'"
                        val dbResult = SQLServerWrapper.getListOf(
                            "set_users",
                            "TOP 1",
                            mutableListOf("*"),
                            where
                        )

                        dbResult?.let {
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
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return user
                } else {
                    var user: User? = null
                    try {
                        val where = "emp_username='$userId'"
                        val dbResult = SQLServerWrapper.getListOf(
                            "pay_employees",
                            "TOP 1",
                            mutableListOf("*"),
                            where
                        )
                        dbResult?.let {
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
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return user
                }
            }
        }
    }
}