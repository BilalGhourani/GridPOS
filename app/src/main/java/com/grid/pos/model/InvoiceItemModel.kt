package com.grid.pos.model

import com.grid.pos.data.Invoice.Invoice
import com.grid.pos.data.Item.Item

data class InvoiceItemModel(
    var name: String = "Item",
    val invoice: Invoice = Invoice(),
) {

    fun setItem(item: Item) {
        name = item.itemName ?: "Item"
        invoice.invoiceItemId = item.itemId
        invoice.invoiceQuantity = 1.0
        invoice.invoicePrice = item.itemUnitPrice?.toDouble() ?: 0.0
        invoice.invoiceDiscount = 0.0
        invoice.invoiceDiscamt = 0.0
        invoice.invoiceTax = item.itemTax?.toDouble() ?: 0.0
        invoice.invoiceTax1 = item.itemTax1?.toDouble() ?: 0.0
        invoice.invoiceTax2 = item.itemTax2?.toDouble() ?: 0.0
        invoice.invoicCost = item.itemOpenCost?.toDouble() ?: 0.0
        invoice.invoicRemQty = item.itemOpenQty?.toDouble() ?: 0.0
    }

    fun getName(isHeader: Boolean = false): String {
        if (isHeader) {
            return "Item"
        }
        return name
    }

    fun getQuantity(): Double {
        return invoice.invoiceQuantity ?: 0.0
    }

    fun getPrice(): Double {
        return invoice.invoicePrice ?: 0.0
    }

    fun getDiscount(): Double {
        return invoice.invoiceDiscount ?: 0.0
    }

    fun getTax(): Double {
        return invoice.invoiceTax ?: 0.0
    }

    fun getTax1(): Double {
        return invoice.invoiceTax1 ?: 0.0
    }

    fun getTax2(): Double {
        return invoice.invoiceTax2 ?: 0.0
    }

    fun getAmount(): Double {
        val quantity = invoice.invoiceQuantity ?: 1.0
        val price = invoice.invoicePrice ?: 0.0
        return quantity.times(price)
    }
}
