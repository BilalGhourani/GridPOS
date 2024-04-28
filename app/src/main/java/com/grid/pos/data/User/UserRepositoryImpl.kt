package com.grid.pos.data.User

import androidx.lifecycle.asLiveData
import com.grid.pos.interfaces.OnResult
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class UserRepositoryImpl(
    private val userDao: UserDao
) : UserRepository {
    override suspend fun insert(user: User, callback: OnResult?) {
        FirebaseFirestore.getInstance().collection("set_users")
            .add(user)
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    userDao.insert(user)
                    callback?.onSuccess(user)
                }
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }
    }

    override suspend fun delete(user: User, callback: OnResult?) {
        FirebaseFirestore.getInstance().collection("set_users")
            .document(user.userDocumentId!!)
            .delete()
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    userDao.delete(user)
                    callback?.onSuccess(user)
                }
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
            }
    }

    override suspend fun update(user: User, callback: OnResult?) {
        FirebaseFirestore.getInstance().collection("set_users")
            .document(user.userDocumentId!!)
            .update(user.getMap())
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    userDao.update(user)
                    callback?.onSuccess(user)
                }
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e.message.toString())
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
        FirebaseFirestore.getInstance().collection("set_users")
            .whereEqualTo("usr_username", username)
            .whereEqualTo("usr_password", password)
            .get()
            .addOnSuccessListener { result ->
                CoroutineScope(Dispatchers.IO).launch {
                    if (result.size() > 0) {
                        for (document in result) {
                            val obj = document.toObject(User::class.java)
                            if (!obj.userId.isNullOrEmpty()) {
                                obj.userDocumentId = document.id
                                callback?.onSuccess(obj)
                                return@launch
                            }
                        }
                    } else {
                        callback?.onFailure(
                            "Username or Password are incorrect!"
                        )
                    }
                }
            }.addOnFailureListener { exception ->
                callback?.onFailure(
                    exception.message ?: "Network error! Can't get users from remote!"
                )
            }
    }

    override suspend fun getAllUsers(callback: OnResult?) {
        val localUsers = userDao.getAllUsers().asLiveData().value
        if (!localUsers.isNullOrEmpty()) {
            callback?.onSuccess(localUsers)
        }
        FirebaseFirestore.getInstance().collection("set_users").get()
            .addOnSuccessListener { result ->
                CoroutineScope(Dispatchers.IO).launch {
                    val users = mutableListOf<User>()
                    userDao.deleteAll()
                    if (result.size() > 0) {
                        for (document in result) {
                            val obj = document.toObject(User::class.java)
                            if (!obj.userId.isNullOrEmpty()) {
                                obj.userDocumentId = document.id
                                users.add(obj)
                            }
                        }
                        userDao.insertAll(users.toList())
                    }
                    callback?.onSuccess(users)
                }
            }.addOnFailureListener { exception ->
                callback?.onFailure(
                    exception.message ?: "Network error! Can't get users from remote."
                )
            }
    }
}