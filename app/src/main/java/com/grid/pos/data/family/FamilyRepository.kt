package com.grid.pos.data.family

import com.grid.pos.model.DataModel

interface FamilyRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(family: Family): DataModel

    // Delete a Family
    suspend fun delete(family: Family):DataModel

    // Update a Family
    suspend fun update(family: Family): DataModel

    // Get all Families as stream.
    suspend fun getAllFamilies(): DataModel

    // Get all Families as stream.
    suspend fun getOneFamily(companyId:String): DataModel

}
