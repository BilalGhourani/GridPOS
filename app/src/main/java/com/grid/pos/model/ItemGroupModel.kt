package com.grid.pos.model

import com.grid.pos.data.EntityModel

data class ItemGroupModel(
        var groupName: String
) : EntityModel() {

    override fun getId(): String {
        return groupName
    }

    override fun getName(): String {
        return groupName
    }

}
