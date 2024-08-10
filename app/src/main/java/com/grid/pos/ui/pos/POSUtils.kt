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
                0,
                4
            ).equals(
                currentYear,
                ignoreCase = true
            )
        ) {
            invNoStr = currentYear + "000000000"
        }
        return (invNoStr.toBigInteger().plus(BigInteger("1"))).toString()
    }

    fun getInvoiceNo(oldInvoiceNo: String?): String {
        val currentYear = Utils.getCurrentYear()
        val sections = if (oldInvoiceNo.isNullOrEmpty()) listOf(
            currentYear,
            "0"
        ) else oldInvoiceNo.split("-")
        var invYearStr = if (sections.isNotEmpty()) sections[0] else currentYear
        val serialNo = if (sections.size > 1) sections[1] else "0"
        if (!invYearStr.equals(
                currentYear,
                ignoreCase = true
            )
        ) {
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
        val tax = SettingsModel.currentCompany?.companyTax?:0.0
        val tax1 = SettingsModel.currentCompany?.companyTax1?:0.0
        val tax2 = SettingsModel.currentCompany?.companyTax2?:0.0

        val total = invoiceList.sumOf {it.getNetAmount() }
        val discamt = total.times(invHeader.invoiceHeadDiscount.div(100.0))
        val netTotal = total - discamt

        invHeader.invoiceHeadTotal = total
        invHeader.invoiceHeadTotal1 = total.times(currency.currencyRate)
        invHeader.invoiceHeadTotalNetAmount = netTotal

        invHeader.invoiceHeadTaxAmt = netTotal.times(tax.div(100.0))
        invHeader.invoiceHeadTax1Amt = netTotal.times(tax1.div(100.0))
        invHeader.invoiceHeadTax2Amt = netTotal.times(tax2.div(100.0))
        invHeader.invoiceHeadTotalTax = invHeader.invoiceHeadTaxAmt  + invHeader.invoiceHeadTax1Amt  + invHeader.invoiceHeadTax2Amt

        invHeader.invoiceHeadGrossAmount = netTotal + invHeader.invoiceHeadTotalTax
        invHeader.invoiceHeadRate = currency.currencyRate
        return invHeader
    }

    fun getInvoiceType(invoiceHeader: InvoiceHeader): String {
        return invoiceHeader.invoiceHeadTtCode ?: SettingsModel.getTransactionType(invoiceHeader.invoiceHeadTotal)
    }
}