package com.grid.pos.ui.pos

import android.content.res.Configuration
import com.grid.pos.data.family.Family
import com.grid.pos.data.invoiceHeader.InvoiceHeader
import com.grid.pos.data.item.Item
import com.grid.pos.data.posPrinter.PosPrinter
import com.grid.pos.data.posReceipt.PosReceipt
import com.grid.pos.data.thirdParty.ThirdParty
import com.grid.pos.data.user.User
import com.grid.pos.model.InvoiceItemModel

data class POSState(
    var invoiceHeader: InvoiceHeader = InvoiceHeader(),
    val invoiceItems: MutableList<InvoiceItemModel> = mutableListOf(),
    var posReceipt: PosReceipt = PosReceipt(),
    var families: MutableList<Family> = mutableListOf(),
    var items: MutableList<Item> = mutableListOf(),
    var thirdParties: MutableList<ThirdParty> = mutableListOf(),
    var invoiceHeaders: MutableList<InvoiceHeader> = mutableListOf(),
    var users: MutableList<User> = mutableListOf(),
    var printers: MutableList<PosPrinter> = mutableListOf(),
    var selectedThirdParty: ThirdParty = ThirdParty(),

    val orientation: Int = Configuration.ORIENTATION_PORTRAIT,
) {

    val isLandscape: Boolean
        get() {
            return orientation == Configuration.ORIENTATION_LANDSCAPE
        }
}