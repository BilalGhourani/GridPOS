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
        invoice.invoicCost = item.itemOpenCost
        invoice.invoicRemQty = item.itemOpenQty
    }

    fun getName(): String {
        return invoiceItem.itemName ?: "Item"
    }

    fun getQuantity(): Double {
        return invoice.invoiceQuantity
    }

    fun getPrice(): Double {
        return invoice.invoicePrice
    }

    fun getPriceWithTax(): Double {
        return getAmount() + getTax() + getTax1() + getTax2()
    }

    fun getDiscount(): Double {
        return invoice.invoiceDiscount
    }
    fun getDiscountAmount(): Double {
        return invoice.invoiceDiscamt
    }

    fun getTax(): Double {
        return getAmount().times(invoice.invoiceTax.div(100.0))
    }

    fun getTax1(): Double {
        return getAmount().times(invoice.invoiceTax1.div(100.0))
    }

    fun getTax2(): Double {
        return getAmount().times(invoice.invoiceTax2.div(100.0))
    }

    fun getAmount(): Double {
        val quantity = invoice.invoiceQuantity
        val price = invoice.invoicePrice
        return quantity.times(price)
    }

    fun getNetAmount(): Double {
        val quantity = invoice.invoiceQuantity
        val discountPercentage = getDiscount().div(100.0)
        var totalAfterDisc = getPriceWithTax()
        if (discountPercentage > 0.0) {
            val discountAmount = (totalAfterDisc).times(discountPercentage)
            totalAfterDisc -= discountAmount
        }
        return quantity.times(totalAfterDisc)
    }
}
