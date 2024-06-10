package com.grid.pos.data.Company

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(company: Company)

    // insert list of Companies
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(order: List<Company>)

    // Delete a Company
    @Delete
    suspend fun delete(company: Company)

    // Delete all companies
    @Query("DELETE FROM company")
    suspend fun deleteAll()

    // Update a Company
    @Update
    suspend fun update(company: Company)

    // Update list of Companies
    @Update
    suspend fun updateAll(order: List<Company>)

    // Get Company by it's ID
    @Query("SELECT * FROM company WHERE cmp_id = :id")
    suspend fun getCompanyById(id: String): Company

    // Get all Companies as stream.
    @Query("SELECT * FROM `company`")
    fun getAllCompanies(): MutableList<Company>

    // Get searched Companies as stream.
    @Query("SELECT * FROM `company` WHERE cmp_name LIKE '%' || :key || '%'")
    fun searchForCompanies(key: String): List<Company>
}