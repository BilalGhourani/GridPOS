package com.grid.pos.model

import com.grid.pos.data.EntityModel

data class DivisionModel(
        var divisionId: String = "",
        var divisionName: String = ""
) : EntityModel() {
    override fun getId(): String {
        return divisionId
    }
    override fun getName(): String {
        return divisionName
    }

    override fun search(key: String): Boolean {
        return getName().contains(
            key,
            ignoreCase = true
        )
    }
}
