package com.grid.pos.ui.family

import com.grid.pos.data.family.Family

data class ManageFamiliesState(
        val families: MutableList<Family> = mutableListOf(),
        val family: Family = Family()
    )