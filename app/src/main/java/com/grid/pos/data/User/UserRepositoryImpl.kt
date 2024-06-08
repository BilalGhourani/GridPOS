package com.grid.pos.data.User

import com.google.firebase.firestore.FirebaseFirestore
import com.grid.pos.data.Company.Company
import com.grid.pos.data.SQLServerWrapper
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Extension.encryptCBC
import com.grid.pos.utils.Utils
import kotlinx.coroutines.tasks.await
import java.util.Date

class UserRepositoryImpl(
        private val userDao: UserDao
) : UserRepository {
    override suspend fun insert(
            user: User
    ): User {
        if (SettingsModel.isConnectedToFireStore()) {
            val docRef = FirebaseFirestore.getInstance().collection("set_users").add(user).await()
            user.userDocumentId = docRef.id
        } else {
            userDao.insert(user)
        }
        return user
    }

    override suspend fun delete(
            user: User
    ) {
        if (SettingsModel.isConnectedToFireStore()) {
            user.userDocumentId?.let {
                FirebaseFirestore.getInstance().collection("set_users").document(it).delete()
                    .await()
            }
        } else {
            userDao.delete(user)
        }
    }

    override suspend fun update(
            user: User
    ) {
        if (SettingsModel.isConnectedToFireStore()) {
            user.userDocumentId?.let {
                FirebaseFirestore.getInstance().collection("set_users").document(it)
                    .update(user.getMap()).await()
            }

        } else {
            userDao.update(user)
        }
    }

    override suspend fun getUserByCredentials(
            username: String,
            password: String
    ): User? {
        when (SettingsModel.connectionType) {
            CONNECTION_TYPE.FIRESTORE.key -> {
                val querySnapshot = FirebaseFirestore.getInstance().collection("set_users")
                    .whereEqualTo(
                        "usr_username",
                        username
                    ).whereEqualTo(
                        "usr_password",
                        password
                    ).whereEqualTo(
                        "usr_cmp_id",
                        SettingsModel.getCompanyID()
                    ).get().await()
                if (querySnapshot.size() > 0) {
                    for (document in querySnapshot) {
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
                val users = userDao.login(
                    username,
                    password,
                    SettingsModel.getCompanyID() ?: ""
                )
                return if (users.isNotEmpty()) {
                    users[0]
                } else {
                    User(
                        "",
                        null,
                        "temp user",
                        "user",
                        "1".encryptCBC()
                    )
                }
            }

            else -> {
                val where = "usr_username = $username AND usr_password = $password AND usr_cmp_id='${SettingsModel.getCompanyID()}'"
                val dbResult = SQLServerWrapper.getListOf(
                    "set_users",
                    mutableListOf("*"),
                    where
                )
                val users: MutableList<User> = mutableListOf()
                dbResult.forEach { obj ->
                    users.add(User().apply {
                        userId = obj.optString("usr_id")
                        userName = obj.optString("usr_name")
                        userUsername = obj.optString("usr_username")
                        userPassword = obj.optString("usr_password")
                        userCompanyId = obj.optString("usr_cmp_id")
                        userPosMode = true
                        userTableMode = true
                    })
                }
                return if (users.isNotEmpty()) users[0] else null
            }
        }
    }

    override suspend fun getAllUsers(): MutableList<User> {
        return when (SettingsModel.connectionType) {
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
                users
            }

            CONNECTION_TYPE.LOCAL.key -> {
                userDao.getAllUsers(SettingsModel.getCompanyID() ?: "")
            }

            else -> {//CONNECTION_TYPE.SQL_SERVER.key
                val where = "usr_cmp_id='${SettingsModel.getCompanyID()}'"
                val dbResult = SQLServerWrapper.getListOf(
                    "set_users",
                    mutableListOf("*"),
                    where
                )
                val users: MutableList<User> = mutableListOf()
                dbResult.forEach { obj ->
                    users.add(User().apply {
                        userId = obj.optString("usr_id")
                        userName = obj.optString("usr_name")
                        userUsername = obj.optString("usr_username")
                        userPassword = obj.optString("usr_password")
                        userCompanyId = obj.optString("usr_cmp_id")
                        userPosMode = true
                        userTableMode = true
                    })
                }
                users
            }
        }

    }
}