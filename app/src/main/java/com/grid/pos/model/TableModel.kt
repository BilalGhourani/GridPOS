package com.grid.pos.model

data class TableModel(
        var table_id: String = "",
        var table_name: String = "",
        var table_type: String? = null,
        var table_inv_id: String? = null,
        var table_user: String? = null,
        var table_locked: Int = 0
) {
    fun getName(): String {
        return table_name
    }
}
