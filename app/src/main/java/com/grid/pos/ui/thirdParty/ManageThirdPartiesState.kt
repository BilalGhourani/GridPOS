package com.grid.pos.ui.thirdParty

import com.grid.pos.data.thirdParty.ThirdParty
import com.grid.pos.model.Event
import com.grid.pos.model.ThirdPartyTypeModel

data class ManageThirdPartiesState(
    val thirdParties: MutableList<ThirdParty> = mutableListOf(),
    val thirdPartyTypes: MutableList<ThirdPartyTypeModel> = mutableListOf(),
    val thirdParty: ThirdParty = ThirdParty(),
    var enableIsDefault: Boolean = false,
    val isLoading: Boolean = false,
    val clear: Boolean = false,
    val warning: Event<String>? = null,
)