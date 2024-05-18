package com.grid.pos.ui.family

import com.grid.pos.data.Company.Company
import com.grid.pos.data.Family.Family
import com.grid.pos.model.Event

data class ManageFamiliesState(
        val families: MutableList<Family> = mutableListOf(),
        var selectedFamily: Family = Family(),
        var isLoading: Boolean = false,
        var clear: Boolean = false,
        val warning: Event<String>? = null,
    )