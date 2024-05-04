package com.grid.pos.ui.pos

import com.grid.pos.data.Family.Family
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.Item.Item
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.model.InvoiceItemModel

data class POSState(
    var invoices: MutableList<InvoiceItemModel> = mutableListOf(),
    val families: MutableList<Family> = mutableListOf(),
    val items: MutableList<Item> = mutableListOf(),
    val thirdParties: MutableList<ThirdParty> = mutableListOf(),
    var invoiceHeader: InvoiceHeader = InvoiceHeader(),
    val isLoading: Boolean = false,
    val warning: String? = null,
) {
    fun refreshValues(): InvoiceHeader {
        var tax = 0.0
        var tax1 = 0.0
        var tax2 = 0.0
        var total = 0.0
        invoices.forEach {
            tax += it.getTax()
            tax1 += it.getTax1()
            tax2 += it.getTax2()
            total += it.getAmount()
        }
        invoiceHeader.invoicHeadTaxAmt = tax
        invoiceHeader.invoicHeadTax1Amt = tax1
        invoiceHeader.invoicHeadTax2Amt = tax2
        invoiceHeader.invoicHeadTotalTax = tax + tax1 + tax2
        invoiceHeader.invoicHeadTotal = total
        invoiceHeader.invoicHeadTotal1 = total
        return invoiceHeader
    }
}