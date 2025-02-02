package com.grid.pos.ui.receipts

import com.grid.pos.data.receipt.Receipt
import com.grid.pos.data.thirdParty.ThirdParty
import com.grid.pos.data.user.User
import com.grid.pos.model.CurrencyModel
import com.grid.pos.model.Event

data class ReceiptsState(
        val receipts: MutableList<Receipt> = mutableListOf(),
        val thirdParties: MutableList<ThirdParty> = mutableListOf(),
        val currencies: MutableList<CurrencyModel> = mutableListOf(),
        var users: MutableList<User> = mutableListOf(),
        val isLoading: Boolean = false,
        var isSaved: Boolean = false,
        var clear: Boolean = false,
        val warning: Event<String>? = null,
)