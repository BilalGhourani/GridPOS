package com.grid.pos.data.User

import com.google.firebase.firestore.FirebaseFirestore
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
        if (SettingsModel.isConnectedToFireStore()) {
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
        } else {
            val users = userDao.login(
                username,
                password,
                SettingsModel.getCompanyID()?:""
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
    }

    override suspend fun getAllUsers(): MutableList<User> {
        if (SettingsModel.isConnectedToFireStore()) {
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
            return users
        } else {
            return userDao.getAllUsers(SettingsModel.getCompanyID()?:"")
        }
    }
}