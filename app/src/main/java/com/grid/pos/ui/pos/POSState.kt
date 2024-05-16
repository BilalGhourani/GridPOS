package com.grid.pos.ui.pos

import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.Family.Family
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.Item.Item
import com.grid.pos.data.PosReceipt.PosReceipt
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.SettingsModel

data class POSState(
    val families: MutableList<Family> = mutableListOf(),
    val items: MutableList<Item> = mutableListOf(),
    val thirdParties: MutableList<ThirdParty> = mutableListOf(),
    val invoiceHeaders: MutableList<InvoiceHeader> = mutableListOf(),
    var posReceipt: PosReceipt = PosReceipt(),
    var selectedThirdParty: ThirdParty = ThirdParty(),
    var isSaved: Boolean = false,
    var isLoading: Boolean = false,
    val warning: String? = null,
) {

}