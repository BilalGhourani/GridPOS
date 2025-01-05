package com.grid.pos.data.family

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

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

    // Get all Families as stream.
    @Query("SELECT * FROM `st_family` WHERE fa_cmp_id = :companyId")
    fun getAllFamilies(companyId:String): MutableList<Family>

    // Get one Family as stream.
    @Query("SELECT * FROM `st_family` WHERE fa_cmp_id = :companyId LIMIT 1")
    fun getOneFamily(companyId:String): Family?

    // Get one Family as stream.
    @Query("SELECT * FROM `st_family` WHERE fa_id = :familyId LIMIT 1")
    fun getFamilyById(familyId:String): Family?

}