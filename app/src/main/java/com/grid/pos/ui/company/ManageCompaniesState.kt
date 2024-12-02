package com.grid.pos.ui.company

import com.grid.pos.data.company.Company
import com.grid.pos.data.currency.Currency
import com.grid.pos.data.posPrinter.PosPrinter
import com.grid.pos.model.Event

data class ManageCompaniesState(
        val companies: MutableList<Company> = mutableListOf(),
        val currencies: MutableList<Currency> = mutableListOf(),
        val printers: MutableList<PosPrinter> = mutableListOf(),
        var selectedCompany: Company = Company(),
        val isLoading: Boolean = false,
        val clear: Boolean = false,
        val warning: Event<String>? = null,
        val actionLabel: String? = null,
)