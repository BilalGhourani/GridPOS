package com.grid.pos.data.User

import androidx.lifecycle.asLiveData
import com.grid.pos.interfaces.OnResult
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Extension.encryptCBC
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserRepositoryImpl(
        private val userDao: UserDao
) : UserRepository {
    override suspend fun insert(
            user: User,
            callback: OnResult?
    ) {
        if (SettingsModel.isConnectedToFireStore()) {
            FirebaseFirestore.getInstance().collection("set_users").add(user).addOnSuccessListener {
                user.userDocumentId = it.id
                callback?.onSuccess(user)
            }.addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }
        } else {
            userDao.insert(user)
            callback?.onSuccess(user)
        }
    }

    override suspend fun delete(
            user: User,
            callback: OnResult?
    ) {
        if (SettingsModel.isConnectedToFireStore()) {
            FirebaseFirestore.getInstance().collection("set_users").document(user.userDocumentId!!)
                .delete().addOnSuccessListener {
                callback?.onSuccess(user)
            }.addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }
        } else {
            userDao.delete(user)
            callback?.onSuccess(user)
        }
    }

    override suspend fun update(
            user: User,
            callback: OnResult?
    ) {
        if (SettingsModel.isConnectedToFireStore()) {
            FirebaseFirestore.getInstance().collection("set_users").document(user.userDocumentId!!)
                .update(user.getMap()).addOnSuccessListener {
                callback?.onSuccess(user)
            }.addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }
        } else {
            userDao.update(user)
            callback?.onSuccess(user)
        }
    }

    override suspend fun getUserById(
            id: String,
            callback: OnResult?
    ) {
        if (SettingsModel.isConnectedToFireStore()) {
            FirebaseFirestore.getInstance().collection("set_users").whereEqualTo(
                "usr_cmp_id", SettingsModel.companyID
            ).whereEqualTo(
                "usr_id", id
            ).get().addOnSuccessListener { result ->
                val document = result.documents.firstOrNull()
                if (document != null) {
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        callback?.onSuccess(user)
                        return@addOnSuccessListener
                    }
                }
                callback?.onFailure("not found.")
            }.addOnFailureListener { exception ->
                callback?.onFailure(
                    exception.message ?: "Network error! Can't get users from remote."
                )
            }
        } else {
            callback?.onSuccess(userDao.getUserById(id))
        }
    }

    override suspend fun getUserByCredentials(
            username: String,
            password: String,
            callback: OnResult?
    ) {
        if (SettingsModel.isConnectedToFireStore()) {
            FirebaseFirestore.getInstance().collection("set_users").whereEqualTo(
                "usr_username", username
            ).whereEqualTo(
                "usr_password", password
            ).whereEqualTo(
                "usr_cmp_id", SettingsModel.companyID
            ).get().addOnSuccessListener { result ->
                if (result.size() > 0) {
                    CoroutineScope(Dispatchers.IO).launch {
                        for (document in result) {
                            val obj = document.toObject(User::class.java)
                            if (!obj.userId.isNullOrEmpty()) {
                                obj.userDocumentId = document.id
                                callback?.onSuccess(obj)
                            }
                        }
                    }
                } else {
                    callback?.onFailure(
                        "Username or Password are incorrect!", 1
                    )
                }
            }.addOnFailureListener { exception ->
                callback?.onFailure(
                    exception.message ?: "Network error! Can't get users from remote!"
                )
            }
        } else {
            userDao.login(
                username, password
            ).collect {
                if (it.isNotEmpty()) {
                    callback?.onSuccess(it[0])
                } else {
                    callback?.onSuccess(User("", null, "temp user", "user", "1".encryptCBC()))
                }
                callback?.onSuccess(it)
            }
        }
    }

    override suspend fun getAllUsers(callback: OnResult?) {
        if (SettingsModel.isConnectedToFireStore()) {
            FirebaseFirestore.getInstance().collection("set_users").whereEqualTo(
                "usr_cmp_id", SettingsModel.companyID
            ).get().addOnSuccessListener { result ->
                val users = mutableListOf<User>()
                if (result.size() > 0) {
                    for (document in result) {
                        val obj = document.toObject(User::class.java)
                        if (!obj.userId.isNullOrEmpty()) {
                            obj.userDocumentId = document.id
                            users.add(obj)
                        }
                    }
                }
                callback?.onSuccess(users)
            }.addOnFailureListener { exception ->
                callback?.onFailure(
                    exception.message ?: "Network error! Can't get users from remote."
                )
            }
        } else {
            userDao.getAllUsers().collect {
                callback?.onSuccess(it)
            }
        }
    }
}