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
    suspend fun getAllThirdParties(): DataModel
    suspend fun getAllThirdParties(types: List<String>): DataModel

    suspend fun getThirdPartyByID(thirdpartyId: String): DataModel
    suspend fun getOneThirdPartyByCompanyID(companyId: String): DataModel

    suspend fun getOneThirdPartyByUserID(userId: String): DataModel

    suspend fun getDefaultThirdParty(): DataModel

}
