package com.grid.pos.data.User

import androidx.lifecycle.asLiveData
import com.grid.pos.interfaces.OnResult
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.grid.pos.model.SettingsModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class UserRepositoryImpl(
    private val userDao: UserDao
) : UserRepository {
    override suspend fun insert(user: User, callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("set_users")
                .add(user)
                .addOnSuccessListener {
                    user.userDocumentId = it.id
                    callback?.onSuccess(user)
                }
                .addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        } else {
            userDao.insert(user)
            callback?.onSuccess(user)
        }
    }

    override suspend fun delete(user: User, callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("set_users")
                .document(user.userDocumentId!!)
                .delete()
                .addOnSuccessListener {
                    callback?.onSuccess(user)
                }
                .addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        } else {
            userDao.delete(user)
            callback?.onSuccess(user)
        }
    }

    override suspend fun update(user: User, callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("set_users")
                .document(user.userDocumentId!!)
                .update(user.getMap())
                .addOnSuccessListener {
                    callback?.onSuccess(user)
                }
                .addOnFailureListener { e ->
                    callback?.onFailure(e.message.toString())
                }
        } else {
            userDao.update(user)
            callback?.onSuccess(user)
        }
    }

    override suspend fun getUserById(id: String): User {
        return userDao.getUserById(id)
    }

    override suspend fun getUserByCredentials(
        username: String,
        password: String,
        callback: OnResult?
    ) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("set_users")
                .whereEqualTo("usr_username", username)
                .whereEqualTo("usr_password", password)
                .get()
                .addOnSuccessListener { result ->
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
                            "Username or Password are incorrect!"
                        )
                    }
                }.addOnFailureListener { exception ->
                    callback?.onFailure(
                        exception.message ?: "Network error! Can't get users from remote!"
                    )
                }
        } else {
            val localUsers = userDao.login(username, password).asLiveData().value
            if (!localUsers.isNullOrEmpty()) {
                callback?.onSuccess(localUsers)
            } else {
                callback?.onFailure(
                    "no user found!"
                )
            }
        }
    }

    override suspend fun getAllUsers(callback: OnResult?) {
        if (SettingsModel.loadFromRemote) {
            FirebaseFirestore.getInstance().collection("set_users").get()
                .addOnSuccessListener { result ->
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
            val localUsers = userDao.getAllUsers().asLiveData().value
            if (!localUsers.isNullOrEmpty()) {
                callback?.onSuccess(localUsers)
            }
        }
    }
}