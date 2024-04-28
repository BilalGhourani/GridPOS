package com.grid.pos.ui.family

import com.grid.pos.data.Company.Company
import com.grid.pos.data.Family.Family

data class ManageFamiliesState(
    val families: MutableList<Family> = mutableListOf(),
    val companies: MutableList<Company> = mutableListOf(),
    var selectedFamily: Family = Family(),
    val isLoading: Boolean = false,
    val warning: String? = null,
    )