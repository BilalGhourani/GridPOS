package com.grid.pos.ui.company

import com.grid.pos.data.Company.Company
import com.grid.pos.data.Currency.Currency

data class ManageCompaniesState(
    val companies: MutableList<Company> = mutableListOf(),
    val currencies: MutableList<Currency> = mutableListOf(),
    var selectedCompany: Company = Company(),
    val isLoading: Boolean = false,
    var clear: Boolean = false,
    val warning: String? = null,
)