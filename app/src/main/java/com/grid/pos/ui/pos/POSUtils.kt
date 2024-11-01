package com.grid.pos.ui.pos

import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Utils
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import kotlin.math.min

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
        var serialNo = if (sections.size > 1) sections[1] else "0"
        if (!invYearStr.equals(
                currentYear,
                ignoreCase = true
            )
        ) {
            invYearStr = currentYear
            serialNo = "0"
        }
        val serialInt = (serialNo.toIntOrNull() ?: 1) + 1
        if (serialInt < 10) {
            return "$invYearStr-0${serialInt}"
        }
        return "$invYearStr-${serialInt}"
    }

    fun refreshValues(
            invoiceList: MutableList<InvoiceItemModel>,
            invHeader: InvoiceHeader
    ): InvoiceHeader {
        val isUpWithTax = SettingsModel.currentCompany?.companyUpWithTax ?: false
        val currency = SettingsModel.currentCurrency ?: Currency()
        var totalTax = 0.0
        var totalTax1 = 0.0
        var totalTax2 = 0.0
        var grossAmount = 0.0
        var netTotal = 0.0
        invoiceList.forEach {
            if (isUpWithTax) {
                val amount = it.invoice.getAmount() - it.invoice.getDiscountAmount()
                totalTax += it.invoice.getIncludedTaxPerc(amount)
                totalTax1 += it.invoice.getIncludedTax1Perc(amount)
                totalTax2 += it.invoice.getIncludedTax2Perc(amount)
                grossAmount += amount
                netTotal += amount
            } else {
                val amount = it.invoice.getAmount() - it.invoice.getDiscountAmount()
                val tax = it.invoice.getTaxValue(amount)
                val tax1 = it.invoice.getTax1Value(amount)
                val tax2 = it.invoice.getTax2Value(amount)
                totalTax += tax
                totalTax1 += tax1
                totalTax2 += tax2
                grossAmount += amount
                netTotal += amount
            }
        }

        val taxDivider = 1 - (invHeader.invoiceHeadDiscount * 0.01)
        totalTax = totalTax.times(taxDivider)
        totalTax1 = totalTax1.times(taxDivider)
        totalTax2 = totalTax2.times(taxDivider)
        invHeader.invoiceHeadTaxAmt = totalTax
        invHeader.invoiceHeadTax1Amt = totalTax1
        invHeader.invoiceHeadTax2Amt = totalTax2
        invHeader.invoiceHeadTotalTax = totalTax + totalTax1 + totalTax2
        invHeader.invoiceHeadGrossAmount = grossAmount
        if (!isUpWithTax) {
            netTotal += totalTax + totalTax1 + totalTax2
        }
        val discountAmount = grossAmount.times(invHeader.invoiceHeadDiscount * 0.01)
        invHeader.invoiceHeadDiscountAmount = discountAmount
        val finalTotal = netTotal - discountAmount
        invHeader.invoiceHeadTotalNetAmount = finalTotal
        invHeader.invoiceHeadTotal = finalTotal
        invHeader.invoiceHeadTotal1 = finalTotal.times(currency.currencyRate)
        invHeader.invoiceHeadRate = currency.currencyRate
        return invHeader
    }

    fun getInvoiceType(invoiceHeader: InvoiceHeader): String {
        return invoiceHeader.invoiceHeadTtCode.takeIf { !it.isNullOrEmpty() } ?:run { SettingsModel.getTransactionType(invoiceHeader.invoiceHeadTotal) }
    }

    fun formatDouble(
            number: Double,
            defaultScale: Int = 6
    ): String {
        val numberString = BigDecimal(if (number.isNaN()) 0.0 else number)
        val newScale = min(
            numberString.scale(),
            defaultScale
        )
        return numberString.setScale(
            newScale,
            RoundingMode.HALF_UP
        ).toPlainString()
    }
}