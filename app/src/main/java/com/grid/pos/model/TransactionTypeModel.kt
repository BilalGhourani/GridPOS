package com.grid.pos.model

import com.grid.pos.data.EntityModel

data class TransactionTypeModel(
    var transactionTypeId: String = "",
    var transactionTypeCode: String = "",
    var transactionTypeDesc: String = "",
    var transactionTypeDefault: Int = 0,
) : EntityModel() {
    override fun getId(): String {
        return transactionTypeId
    }

    override fun getName(): String {
        return "$transactionTypeCode - $transactionTypeDesc"
    }

    override fun search(key: String): Boolean {
        return getName().contains(
            key,
            ignoreCase = true
        )
    }
}
