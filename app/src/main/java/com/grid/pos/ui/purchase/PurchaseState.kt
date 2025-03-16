package com.grid.pos.ui.purchase

import android.content.res.Configuration
import com.grid.pos.data.family.Family
import com.grid.pos.data.invoiceHeader.InvoiceHeader
import com.grid.pos.data.item.Item
import com.grid.pos.data.posPrinter.PosPrinter
import com.grid.pos.data.posReceipt.PosReceipt
import com.grid.pos.data.purchaseHeader.PurchaseHeader
import com.grid.pos.data.stockHeaderInOut.StockHeaderInOut
import com.grid.pos.data.thirdParty.ThirdParty
import com.grid.pos.data.user.User
import com.grid.pos.model.DivisionModel
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.TransactionTypeModel
import com.grid.pos.model.WarehouseModel

data class PurchaseState(
    val purchaseHeaders: MutableList<PurchaseHeader> = mutableListOf(),
    val items: MutableList<Item> = mutableListOf(),
    var suppliers: MutableList<ThirdParty> = mutableListOf(),
    val warehouses: MutableList<WarehouseModel> = mutableListOf(),
    val divisions: MutableList<DivisionModel> = mutableListOf(),
    val transactionTypes: MutableList<TransactionTypeModel> = mutableListOf(),
)