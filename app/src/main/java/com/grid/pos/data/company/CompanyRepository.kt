package com.grid.pos.data.company

interface CompanyRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(company: Company): Company

    // Delete a Company
    suspend fun delete(company: Company)

    // Update a Company
    suspend fun update(company: Company)

    // Get Company by it's ID
    suspend fun getCompanyById(id: String): Company?

    // Get all Companies logs as stream.
    suspend fun getAllCompanies(): MutableList<Company>

    suspend fun getLocalCompanies(): MutableList<Company>

    // Get all Companies logs as stream.
    suspend fun disableCompanies(disabled: Boolean)
}
