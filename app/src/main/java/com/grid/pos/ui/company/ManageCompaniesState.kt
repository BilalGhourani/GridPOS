package com.grid.pos.ui.company

import com.grid.pos.data.company.Company
import com.grid.pos.data.currency.Currency
import com.grid.pos.model.Event

data class ManageCompaniesState(
        val companies: MutableList<Company> = mutableListOf(),
        val currencies: MutableList<Currency> = mutableListOf(),
        val company: Company = Company(),
        val companyTaxStr:String="",
        val companyTax1Str:String="",
        val companyTax2Str:String=""
)