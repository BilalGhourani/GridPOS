package com.grid.pos.ui.thirdParty

import com.grid.pos.data.thirdParty.ThirdParty
import com.grid.pos.model.Event

data class ManageThirdPartiesState(
        val thirdParties: MutableList<ThirdParty> = mutableListOf(),
        var selectedThirdParty: ThirdParty = ThirdParty(),
        var enableIsDefault: Boolean = false,
        val isLoading: Boolean = false,
        val clear: Boolean = false,
        val warning: Event<String>? = null,
)