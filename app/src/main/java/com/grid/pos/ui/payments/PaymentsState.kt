package com.grid.pos.ui.payments

import com.grid.pos.data.payment.Payment
import com.grid.pos.data.thirdParty.ThirdParty
import com.grid.pos.data.user.User
import com.grid.pos.model.CurrencyModel

data class PaymentsState(
    val payments: MutableList<Payment> = mutableListOf(),
    val thirdParties: MutableList<ThirdParty> = mutableListOf(),
    val currencies: MutableList<CurrencyModel> = mutableListOf(),
    var users: MutableList<User> = mutableListOf(),

    val payment: Payment = Payment(),
    val paymentAmountStr :String = "",
    val paymentAmountFirstStr :String = "",
    val paymentAmountSecondStr :String = "",

    val currencyIndex: Int = 0,
)