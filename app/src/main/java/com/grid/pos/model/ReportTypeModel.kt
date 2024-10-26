package com.grid.pos.model

import com.grid.pos.data.DataModel

data class ReportTypeModel(
    val type : String,
): DataModel() {

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