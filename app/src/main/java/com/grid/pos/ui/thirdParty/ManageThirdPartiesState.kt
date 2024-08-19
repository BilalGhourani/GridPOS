package com.grid.pos.ui.thirdParty

import com.grid.pos.data.Company.Company
import com.grid.pos.data.Family.Family
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.model.Event

data class ManageThirdPartiesState(
        val thirdParties: MutableList<ThirdParty> = mutableListOf(),
        var selectedThirdParty: ThirdParty = ThirdParty(),
        var enableIsDefault: Boolean = true,
        val isLoading: Boolean = false,
        var clear: Boolean = false,
        val warning: Event<String>? = null,
)