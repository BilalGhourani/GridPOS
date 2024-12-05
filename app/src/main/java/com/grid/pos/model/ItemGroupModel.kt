package com.grid.pos.model

import com.grid.pos.data.DataModel

data class ItemGroupModel(
        var groupName: String
) : DataModel() {

    override fun getId(): String {
        return groupName
    }

    override fun getName(): String {
        return groupName
    }

}
