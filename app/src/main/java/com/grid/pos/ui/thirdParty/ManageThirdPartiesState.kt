package com.grid.pos.ui.thirdParty

import com.grid.pos.data.Company.Company
import com.grid.pos.data.Family.Family
import com.grid.pos.data.ThirdParty.ThirdParty

data class ManageThirdPartiesState(
    val thirdParties: MutableList<ThirdParty> = mutableListOf(),
    val companies: MutableList<Company> = mutableListOf(),
    var selectedThirdParty: ThirdParty = ThirdParty(),
    val isLoading: Boolean = false,
    var clear: Boolean = false,
    val warning: String? = null,
)