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
    var invoices: MutableList<InvoiceItemModel> = mutableListOf(),
    val families: MutableList<Family> = mutableListOf(),
    val items: MutableList<Item> = mutableListOf(),
    val thirdParties: MutableList<ThirdParty> = mutableListOf(),
    val invoiceHeaders: MutableList<InvoiceHeader> = mutableListOf(),
    var invoiceHeader: InvoiceHeader = InvoiceHeader(),
    var posReceipt: PosReceipt = PosReceipt(),
    var selectedThirdParty: ThirdParty = ThirdParty(),
    var isSaved: Boolean = false,
    var isLoading: Boolean = false,
    val warning: String? = null,
) {
    fun refreshValues(
            invoiceList: MutableList<InvoiceItemModel> = invoices,
            invHeader: InvoiceHeader = invoiceHeader
    ): InvoiceHeader {
        val currency = SettingsModel.currentCurrency ?: Currency()
        var discount = 0.0
        var tax = 0.0
        var tax1 = 0.0
        var tax2 = 0.0
        var total = 0.0
        invoiceList.forEach {
            discount += it.getDiscount()
            tax += it.getTax()
            tax1 += it.getTax1()
            tax2 += it.getTax2()
            total += it.getAmount()
        }
        invHeader.invoiceHeadTaxAmt = tax
        invHeader.invoiceHeadTax1Amt = tax1
        invHeader.invoiceHeadTax2Amt = tax2
        invHeader.invoiceHeadTotalTax = tax + tax1 + tax2
        invHeader.invoiceHeadTotal = total
        invHeader.invoiceHeadTotal1 = total.times(currency.currencyRate)
        invHeader.invoiceHeadDiscount = discount
        invHeader.invoiceHeadDiscountAmount = total.times(discount.div(100.0))
        invHeader.invoiceHeadGrossAmount = (total - invHeader.invoiceHeadTotalTax) - invHeader.invoiceHeadDiscountAmount
        invHeader.invoiceHeadTtCode = if (total > 0) "SI" else "RS"
        invHeader.invoiceHeadRate = currency.currencyRate
        return invHeader
    }

    fun getInvoiceType(): String {
        return invoiceHeader.invoiceHeadTtCode ?: if (invoiceHeader.invoiceHeadTotal > 0) "SI" else "RS"
    }
}