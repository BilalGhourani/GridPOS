package com.grid.pos.model

import com.grid.pos.data.DataModel

data class PaymentTypeModel(
        var type: String
) : DataModel() {

    override fun getId(): String {
        return type
    }

    override fun getName(): String {
        return type
    }

}
