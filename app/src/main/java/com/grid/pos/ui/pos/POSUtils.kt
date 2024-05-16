package com.grid.pos.ui.pos

import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Utils
import java.math.BigInteger

object POSUtils {


    fun getInvoiceTransactionNo(oldInvoiceTransNo: String?): String {
        val currentYear = Utils.getCurrentYear()
        var invNoStr = oldInvoiceTransNo.takeIf { !it.isNullOrEmpty() } ?: (currentYear + "000000000")
        if (invNoStr.length > 4 && !invNoStr.substring(
                0, 4
            ).equals(
                currentYear, ignoreCase = true
            )
        ) {
            invNoStr = currentYear + "000000000"
        }
        return (invNoStr.toBigInteger().plus(BigInteger("1"))).toString()
    }

    fun getInvoiceNo(oldInvoiceNo: String?): String {
        val currentYear = Utils.getCurrentYear()
        val sections = if (oldInvoiceNo.isNullOrEmpty()) listOf(
            currentYear, "0"
        ) else oldInvoiceNo.split("-")
        var invYearStr = if (sections.isNotEmpty()) sections[0] else currentYear
        val serialNo = if (sections.size > 1) sections[1] else "0"
        if (!invYearStr.equals(currentYear, ignoreCase = true)) {
            invYearStr = currentYear
        }
        val serialInt = (serialNo.toIntOrNull() ?: 1) + 1
        return "$invYearStr-${serialInt}"
    }

    fun refreshValues(
        invoiceList: MutableList<InvoiceItemModel>,
        invHeader: InvoiceHeader
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
        invHeader.invoiceHeadRate = currency.currencyRate
        return invHeader
    }

    fun getInvoiceType(invoiceHeader: InvoiceHeader): String {
        return invoiceHeader.invoiceHeadTtCode ?: if (invoiceHeader.invoiceHeadTotal > 0) "SI" else "RS"
    }
}