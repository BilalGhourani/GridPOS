package com.grid.pos.data.User

import com.grid.pos.interfaces.OnResult

interface UserRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(user: User, callback: OnResult? = null)

    // Delete an User
    suspend fun delete(user: User, callback: OnResult? = null)

    // Update an User
    suspend fun update(user: User, callback: OnResult? = null)

    // Get User by it's ID
    suspend fun getUserById(id: String, callback: OnResult?)

    // Get User by it's ID
    suspend fun getUserByCredentials(username: String, password: String, callback: OnResult?)

    // Get all Users as stream.
    suspend fun getAllUsers(callback: OnResult? = null)
}
