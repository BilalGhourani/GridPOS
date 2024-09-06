package com.grid.pos.data.ThirdParty

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ThirdPartyDao {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(thirdParty: ThirdParty)

    // insert list of Third Parties
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(order: List<ThirdParty>)

    // Delete a Third Party
    @Delete
    suspend fun delete(thirdParty: ThirdParty)

    // Delete all Third Parties
    @Query("DELETE FROM thirdparty")
    suspend fun deleteAll()

    // Update a Third Party
    @Update
    suspend fun update(thirdParty: ThirdParty)

    // Get all Third Parties as stream.
    @Query("SELECT * FROM `thirdparty` WHERE tp_cmp_id=:companyId")
    fun getAllThirdParties(companyId: String): MutableList<ThirdParty>

    // Get one Third Party as stream.
    @Query("SELECT * FROM `thirdparty` WHERE tp_cmp_id=:companyId LIMIT 1")
    fun getOneThirdPartyByCompanyID(companyId: String): ThirdParty?

    // Get one Third Party as stream.
    @Query("SELECT * FROM `thirdparty` WHERE tp_userstamp=:userID LIMIT 1")
    fun getOneThirdPartyByUserID(userID: String): ThirdParty?

    // Get one Third Party as stream.
    @Query("SELECT * FROM `thirdparty` WHERE tp_cmp_id=:companyId AND tp_default=1 LIMIT 1")
    fun getDefaultThirdParties(companyId: String): ThirdParty?
}