package com.grid.pos.model

data class TableModel(
        var table_id: String = "",
        var table_name: String = "",
        var table_type: String? = null
) {
    fun getName(): String {
        return table_name
    }
}
