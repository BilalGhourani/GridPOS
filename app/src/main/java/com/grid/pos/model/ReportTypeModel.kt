package com.grid.pos.model

import com.grid.pos.data.EntityModel

data class ReportTypeModel(
    val type : String,
): EntityModel() {

    override fun getId(): String {
        return type
    }

    override fun getName(): String {
        return type
    }

    override fun search(key: String): Boolean {
        return getName().contains(
            key,
            ignoreCase = true
        )
    }
}
