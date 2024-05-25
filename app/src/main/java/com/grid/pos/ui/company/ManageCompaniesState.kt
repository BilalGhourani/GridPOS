package com.grid.pos.ui.company

import com.grid.pos.data.Company.Company
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.PosPrinter.PosPrinter
import com.grid.pos.model.Event

data class ManageCompaniesState(
        val companies: MutableList<Company> = mutableListOf(),
        val currencies: MutableList<Currency> = mutableListOf(),
        val printers: MutableList<PosPrinter> = mutableListOf(),
        var selectedCompany: Company = Company(),
        val isLoading: Boolean = false,
        var clear: Boolean = false,
        val warning: Event<String>? = null,
)