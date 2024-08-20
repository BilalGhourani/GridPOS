package com.grid.pos.data.User

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    // insert list of users
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(order: List<User>)

    // Delete an user
    @Delete
    suspend fun delete(user: User)

    // Delete all users
    @Query("DELETE FROM set_users")
    suspend fun deleteAll()

    // Update an user
    @Update
    suspend fun update(user: User)

    // Get all users as stream.
    @Query("SELECT * FROM `set_users` WHERE usr_cmp_id=:companyId")
    fun getAllUsers(companyId: String): MutableList<User>

    @Query("SELECT * FROM `set_users` WHERE usr_username = :usermame AND usr_password = :password AND usr_cmp_id=:companyId")
    fun login(
            usermame: String,
            password: String,
            companyId: String
    ): List<User>

    // Get all users as stream.
    @Query("SELECT * FROM `set_users` WHERE usr_cmp_id=:companyId LIMIT 1")
    fun getOneUser(companyId: String): User?
}