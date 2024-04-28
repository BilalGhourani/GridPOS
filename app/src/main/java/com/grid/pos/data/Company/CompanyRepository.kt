package com.grid.pos.data.Company

import com.grid.pos.interfaces.OnResult

interface CompanyRepository {

    // suspend is a coroutine keyword,
    // instead of having a callback we can just wait till insert is done
    suspend fun insert(company: Company, callback: OnResult? = null)

    // Delete a Company
    suspend fun delete(company: Company, callback: OnResult? = null)

    // Update a Company
    suspend fun update(company: Company, callback: OnResult? = null)

    // Get Company by it's ID
    suspend fun getCompanyById(id: String): Company

    // Get all Companies logs as stream.
    suspend fun getAllCompanies(callback: OnResult? = null)


}
