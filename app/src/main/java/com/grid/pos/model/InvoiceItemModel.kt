package com.grid.pos.model

import com.grid.pos.data.Invoice.Invoice
import com.grid.pos.data.Item.Item
import com.grid.pos.utils.Extension.isNullOrEmptyOrNullStr

data class InvoiceItemModel(
        val invoice: Invoice = Invoice(),
        var invoiceItem: Item = Item(),
        val isDeleted : Boolean = false,
        var shouldPrint: Boolean = false
) {

    fun setItem(item: Item) {
        invoiceItem = item
        invoice.invoiceItemId = item.itemId
        invoice.invoiceQuantity = 1.0
        invoice.invoicePrice = item.itemUnitPrice
        invoice.invoiceDiscount = 0.0
        invoice.invoiceDiscamt = 0.0
        invoice.invoiceTax = item.itemTax
        invoice.invoiceTax1 = item.itemTax1
        invoice.invoiceTax2 = item.itemTax2
        invoice.invoiceCost = item.itemOpenCost
        invoice.invoiceRemQty = item.itemOpenQty
    }

    fun getName(): String {
        return if (invoice.invoiceExtraName.isNullOrEmptyOrNullStr()) {
            invoiceItem.itemName ?: "Item"
        } else {
            (invoiceItem.itemName ?: "Item") + " - " + invoice.invoiceExtraName
        }
    }

    fun getFullName(): String {
        val disc = invoice.getDiscount()
        val discountVal = if (disc > 0.0) "-%${disc.toInt()}" else ""
        val taxableVal = if (getTotalTax() > 0.0) "*" else ""
        return if (invoice.invoiceExtraName.isNullOrEmptyOrNullStr()) {
            "${invoiceItem.itemName ?: "Item"}$taxableVal $discountVal"
        } else {
            "${invoiceItem.itemName ?: "Item"}-${invoice.invoiceExtraName}$taxableVal $discountVal"
        }
    }

    private fun getTotalTax(): Double {
        return invoice.getTax() + invoice.getTax1() + invoice.getTax2()
    }
}
