package com.grid.pos.ui.receipts

import com.grid.pos.data.DataModel
import com.grid.pos.data.Payment.Payment
import com.grid.pos.data.Receipt.Receipt
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.model.CurrencyModel
import com.grid.pos.model.Event

data class ReceiptsState(
        val receipts: MutableList<Receipt> = mutableListOf(),
        val thirdParties: MutableList<ThirdParty> = mutableListOf(),
        val currencies: MutableList<CurrencyModel> = mutableListOf(),
        var selectedReceipt: Receipt = Receipt(),
        val isLoading: Boolean = false,
        var clear: Boolean = false,
        val warning: Event<String>? = null,
)