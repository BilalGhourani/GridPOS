package com.grid.pos.data.company

import com.grid.pos.model.DataModel

interface CompanyRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(company: Company): Company

    // Delete a Company
    suspend fun delete(company: Company)

    // Update a Company
    suspend fun update(company: Company)

    // Get Company by it's ID
    suspend fun getCompanyById(id: String): DataModel

    // Get all Companies logs as stream.
    suspend fun getAllCompanies(): DataModel

    suspend fun getLocalCompanies(): MutableList<Company>

    // Get all Companies logs as stream.
    suspend fun disableCompanies(disabled: Boolean)
}
