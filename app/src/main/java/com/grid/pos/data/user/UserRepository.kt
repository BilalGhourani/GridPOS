package com.grid.pos.data.user

import com.grid.pos.model.DataModel
import com.grid.pos.model.LoginResponse

interface UserRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(user: User): DataModel

    // Delete an User
    suspend fun delete(user: User):DataModel

    // Update an User
    suspend fun update(user: User):DataModel

    // Get User by it's ID
    suspend fun getUserByCredentials(
            username: String,
            password: String
    ): LoginResponse

    // Get all Users as stream.
    suspend fun getAllUsers(): DataModel

    suspend fun getOneUser(companyId: String): DataModel
    suspend fun getUserById(userId: String): DataModel
}
