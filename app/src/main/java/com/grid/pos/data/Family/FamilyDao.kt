package com.grid.pos.data.Family

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.grid.pos.data.User.User
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyDao {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(family: Family)

    // insert list of families
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(order: List<Family>)

    // Delete a call log
    @Delete
    suspend fun delete(family: Family)

    // Delete all families
    @Query("DELETE FROM st_family")
    suspend fun deleteAll()

    // Update a call log
    @Update
    suspend fun update(family: Family)

    // Get call log by it's ID
    @Query("SELECT * FROM st_family WHERE fa_id = :id")
    suspend fun getFamilyById(id: String): Family

    // Get all call logs as stream.
    @Query("SELECT * FROM `st_family`")
    fun getAllFamilies(): Flow<List<Family>>

    // Get all call logs as stream.
    @Query("SELECT * FROM `st_family` WHERE fa_name LIKE '%' || :key || '%'")
    fun searchForFamilies(key: String): Flow<List<Family>>
}