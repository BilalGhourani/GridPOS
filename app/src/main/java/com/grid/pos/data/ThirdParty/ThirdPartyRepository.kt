package com.grid.pos.data.ThirdParty

import com.grid.pos.interfaces.OnResult
import kotlinx.coroutines.flow.Flow

interface ThirdPartyRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(thirdParty: ThirdParty, callback: OnResult?)

    // Delete a ThirdParty
    suspend fun delete(thirdParty: ThirdParty, callback: OnResult?)

    // Update a ThirdParty
    suspend fun update(thirdParty: ThirdParty, callback: OnResult?)

    // Get ThirdParty by it's ID
    suspend fun getThirdPartyById(id: String): ThirdParty

    // Get all ThirdParties as stream.
    suspend fun getAllThirdParties(callback: OnResult?)

}
