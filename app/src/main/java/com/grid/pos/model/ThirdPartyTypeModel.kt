package com.grid.pos.model

import com.grid.pos.data.DataModel

data class ThirdPartyTypeModel(
        var thirdPartyType: ThirdPartyType,
) : DataModel() {

    override fun getId(): String {
        return thirdPartyType.type
    }

    override fun getName(): String {
        return thirdPartyType.type
    }

    override fun search(key: String): Boolean {
        return getName().contains(
            key,
            ignoreCase = true
        )
    }
}

//Payable, Receivable, Payable and Receivable, Expenses, Salaries
enum class ThirdPartyType(val type: String) {
    PAYABLE("Payable"), RECEIVALBE("Receivable"), PAYABLE_RECEIVALBE("Payable and Receivable"), EXPENSES("Expenses"), SALARIES("Salaries")
}
