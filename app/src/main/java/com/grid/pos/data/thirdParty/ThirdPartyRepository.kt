package com.grid.pos.data.thirdParty

import com.grid.pos.model.DataModel

interface ThirdPartyRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(thirdParty: ThirdParty): DataModel

    // Delete a ThirdParty
    suspend fun delete(thirdParty: ThirdParty): DataModel

    // Update a ThirdParty
    suspend fun update(
            thirdpartyId: String,
            thirdParty: ThirdParty
    ): DataModel

    // Get all ThirdParties as stream.
    suspend fun getAllThirdParties(): MutableList<ThirdParty>
    suspend fun getAllThirdParties(types: List<String>): MutableList<ThirdParty>

    suspend fun getThirdPartyByID(thirdpartyId: String): ThirdParty?
    suspend fun getOneThirdPartyByCompanyID(companyId: String): ThirdParty?

    suspend fun getOneThirdPartyByUserID(userId: String): ThirdParty?

    suspend fun getDefaultThirdParty(): ThirdParty?

}
