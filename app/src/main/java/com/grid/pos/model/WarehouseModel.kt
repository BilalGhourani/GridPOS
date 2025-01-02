package com.grid.pos.model

import com.grid.pos.data.EntityModel

data class WarehouseModel(
        var warehouseId: String = "",
        var warehouseName: String = "",
        var warehouseOrder: String? = null,
) : EntityModel() {
    override fun getId(): String {
        return warehouseId
    }
    override fun getName(): String {
        return warehouseName
    }

    override fun search(key: String): Boolean {
        return getName().contains(
            key,
            ignoreCase = true
        )
    }
}