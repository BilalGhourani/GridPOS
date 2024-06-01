package com.grid.pos.data.Family

interface FamilyRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(family: Family): Family

    // Delete a Family
    suspend fun delete(family: Family)

    // Update a Family
    suspend fun update(family: Family)

    // Get Family by it's ID
    suspend fun getFamilyById(id: String): Family?

    // Get all Families as stream.
    suspend fun getAllFamilies(): MutableList<Family>

}
