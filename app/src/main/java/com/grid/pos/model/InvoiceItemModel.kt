package com.grid.pos.model

import com.grid.pos.data.Invoice.Invoice
import com.grid.pos.data.Item.Item

data class InvoiceItemModel(
        val invoice: Invoice = Invoice(),
        var invoiceItem: Item = Item(),
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
        return if (invoice.invoiceExtraName.isNullOrEmpty()) {
            invoiceItem.itemName ?: "Item"
        } else {
            (invoiceItem.itemName ?: "Item") + " - " + invoice.invoiceExtraName
        }
    }

    fun getFullName(): String {
        val disc = getDiscount()
        val discountVal = if (disc > 0.0) "-%${disc.toInt()}" else ""
        val taxableVal = if (getTotalTax() > 0.0) "*" else ""
        return if (invoice.invoiceExtraName.isNullOrEmpty()) {
            "${invoiceItem.itemName ?: "Item"}$taxableVal $discountVal"
        } else {
            "${invoiceItem.itemName ?: "Item"}-${invoice.invoiceExtraName}$taxableVal $discountVal"
        }
    }

    fun getQuantity(): Double {
        return invoice.invoiceQuantity
    }

    fun getPrice(): Double {
        return invoice.invoicePrice
    }

    fun getTotalTax(): Double {
        return getTax() + getTax1() + getTax2()
    }

    fun getPriceWithTax(): Double {
        return getAmount() + getTax() + getTax1() + getTax2()
    }

    fun getDiscount(): Double {
        return invoice.getDiscount()
    }

    fun getDiscountAmount(): Double {
        return invoice.getDiscountAmount()
    }

    fun getTax(): Double {
        return invoice.getTax()
    }

    fun getTax1(): Double {
        return invoice.getTax1()
    }

    fun getTax2(): Double {
        return invoice.getTax2()
    }

    fun getAmount(): Double {
        return invoice.getAmount()
    }

    fun getNetAmount(): Double {
        return invoice.getNetAmount()
    }
}
