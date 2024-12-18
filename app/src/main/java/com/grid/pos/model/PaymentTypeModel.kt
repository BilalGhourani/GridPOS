package com.grid.pos.model

import com.grid.pos.data.EntityModel

data class PaymentTypeModel(
        var type: String
) : EntityModel() {

    override fun getId(): String {
        return type
    }

    override fun getName(): String {
        return type
    }

}
