package com.grid.pos.ui.payments

import com.grid.pos.data.DataModel
import com.grid.pos.data.Payment.Payment
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.model.CurrencyModel
import com.grid.pos.model.Event

data class PaymentsState(
        val payments: MutableList<Payment> = mutableListOf(),
        val thirdParties: MutableList<ThirdParty> = mutableListOf(),
        val currencies: MutableList<CurrencyModel> = mutableListOf(),
        var selectedPayment: Payment = Payment(),
        val isLoading: Boolean = false,
        var clear: Boolean = false,
        val warning: Event<String>? = null,
)