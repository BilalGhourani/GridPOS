package com.grid.pos.data.ThirdParty

interface ThirdPartyRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(thirdParty: ThirdParty): ThirdParty

    // Delete a ThirdParty
    suspend fun delete(thirdParty: ThirdParty)

    // Update a ThirdParty
    suspend fun update(thirdParty: ThirdParty)

    // Get all ThirdParties as stream.
    suspend fun getAllThirdParties(): MutableList<ThirdParty>

    suspend fun getOneThirdPartyByCompanyID(companyId: String): ThirdParty?

    suspend fun getOneThirdPartyByUserID(userId: String): ThirdParty?

    suspend fun getDefaultThirdParties(): ThirdParty?

}
