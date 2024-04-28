package com.grid.pos.data.Family

import com.grid.pos.interfaces.OnResult
import kotlinx.coroutines.flow.Flow

interface FamilyRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(family: Family, callback: OnResult?)

    // Delete a Family
    suspend fun delete(family: Family, callback: OnResult?)

    // Update a Family
    suspend fun update(family: Family, callback: OnResult?)

    // Get Family by it's ID
    suspend fun getFamilyById(id: String): Family

    // Get all Families as stream.
    fun getAllFamilies(callback: OnResult?)

}
