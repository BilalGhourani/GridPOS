package com.grid.pos.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.InetAddresses
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Log
import android.util.Patterns
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.grid.pos.data.Company.Company
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.PosPrinter.PosPrinter
import com.grid.pos.data.PosReceipt.PosReceipt
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.data.User.User
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.Language
import com.grid.pos.model.PrintPicture
import com.grid.pos.model.ReportResult
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.pos.POSUtils
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.net.Socket
import java.util.Date
import java.util.Locale

object PrinterUtils {

    //line space => byteArrayOf(0x1B, 0x33, n.toByte())
    private val TAG_SPACE = byteArrayOf(0x09)
    private val NEW_LINE = byteArrayOf(0x0A)
    private val DOUBLE_SIZE = byteArrayOf(
        0x1B,
        0x21,
        0x30
    )
    private val NORMAL_SIZE = byteArrayOf(
        0x1B,
        0x21,
        0x00
    )
    private val BOLD_ON = byteArrayOf(
        0x1B,
        0x45,
        0x01
    )
    private val BOLD_OFF = byteArrayOf(
        0x1B,
        0x45,
        0x00
    )
    private val UNDERLINE_ON = byteArrayOf(
        0x1B,
        0x2D,
        0x01
    )
    private val UNDERLINE_OFF = byteArrayOf(
        0x1B,
        0x2D,
        0x00
    )
    private val ITALIC_ON = byteArrayOf(
        0x1B,
        0x34
    )
    private val ITALIC_OFF = byteArrayOf(
        0x1B,
        0x35
    )
    private val ALIGN_LEFT = byteArrayOf(
        27,
        97,
        0
    )
    private val ALIGN_CENTER = byteArrayOf(
        27,
        97,
        1
    )
    private val ALIGN_RIGHT = byteArrayOf(
        27,
        97,
        2
    )
    private val LINE_FEED = byteArrayOf(
        10
    )
    private val CUT_PAPER = byteArrayOf(
        27,
        109
    )

    private fun getPaySlipHtmlContent(context: Context): ReportResult {
        var payslip = FileUtils.getHtmlFile(
            context,
            "${SettingsModel.defaultReportLanguage}/payslip.html"
        )
        if (payslip.isEmpty()) {
            payslip = FileUtils.getHtmlFile(
                context,
                "${Language.DEFAULT.value}/payslip.html"
            )
        }
        if (payslip.isNotEmpty()) {
            return ReportResult(
                true,
                payslip
            )
        }

        payslip = FileUtils.readFileFromAssets(
            "fallback.html",
            context
        )
        return ReportResult(
            false,
            payslip
        )
    }

    fun getInvoiceReceiptHtmlContent(
            context: Context,
            invoiceHeader: InvoiceHeader,
            invoiceItemModels: MutableList<InvoiceItemModel>,
            posReceipt: PosReceipt,
            thirdParty: ThirdParty? = null,
            user: User? = SettingsModel.currentUser,
            company: Company? = SettingsModel.currentCompany,
            currency: Currency? = SettingsModel.currentCurrency,
    ): ReportResult {
        val defaultLocal = Locale.getDefault()
        val result = getPaySlipHtmlContent(context)
        if (!result.found) {
            return result
        }
        var htmlContent = result.htmlContent
        val invDate = DateHelper.getDateFromString(
            invoiceHeader.invoiceHeadDate,
            if (invoiceHeader.invoiceHeadDate.contains("at")) "MMMM dd, yyyy 'at' hh:mm:ss a 'Z'" else "yyyy-MM-dd HH:mm:ss.S"
        )

        if (!company?.companyLogo.isNullOrEmpty()) {
            val logoBitmap = FileUtils.getBitmapFromPath(
                context,
                Uri.parse(company?.companyLogo)
            )
            val base64Result = convertBitmapToBase64(logoBitmap)
            htmlContent = htmlContent.replace(
                "{company_logo}",
                "<img src=\"data:image/png;base64,$base64Result\" width=\"100px\" height=\"50px\"/>"
            )
        } else {
            htmlContent = htmlContent.replace(
                "{company_logo}",
                ""
            )
        }

        htmlContent = htmlContent.replace(
            "{company_name}",
            company?.companyName ?: ""
        ).replace(
            "{company_addr}",
            company?.companyAddress ?: ""
        ).replace(
            "{company_phone}",
            company?.companyPhone ?: ""
        ).replace(
            "{invoicedatevalue}",
            DateHelper.getDateInFormat(
                invDate,
                "dd/MM/yyyy hh:mm:ss"
            )
        )

        var invoiceNo = invoiceHeader.invoiceHeadTransNo
        if (!invoiceNo.isNullOrEmpty() && invoiceNo.length > 1) {
            htmlContent = htmlContent.replace(
                "{invoicenumbervalue}",
                "Invoice# ${invoiceNo ?: ""}"
            ).replace(
                "{invoice_no_display}",
                "block"
            )
        } else if (!invoiceHeader.invoiceHeadOrderNo.isNullOrEmpty()) {
            invoiceNo = invoiceHeader.invoiceHeadOrderNo
            htmlContent = htmlContent.replace(
                "{invoicenumbervalue}",
                "Order# ${invoiceNo ?: ""}"
            ).replace(
                "{invoice_no_display}",
                "block"
            )
        } else {
            htmlContent = htmlContent.replace(
                "{invoice_no_display}",
                "none"
            )
        }

        htmlContent = if (!thirdParty?.thirdPartyName.isNullOrEmpty() || !invoiceHeader.invoiceHeadCashName.isNullOrEmpty()) {
            htmlContent.replace(
                "{clientnamevalue}",
                "${thirdParty?.thirdPartyName ?: ""} ${invoiceHeader.invoiceHeadCashName ?: ""}"
            ).replace(
                "{client_display}",
                "block"
            )
        } else {
            htmlContent.replace(
                "{client_display}",
                "none"
            )
        }

        htmlContent = if (!thirdParty?.thirdPartyFn.isNullOrEmpty()) {
            htmlContent.replace(
                "{clientfnvalue}",
                thirdParty?.thirdPartyFn ?: ""
            ).replace(
                "{fn_display}",
                "block"
            )
        } else {
            htmlContent.replace(
                "{fn_display}",
                "none"
            )
        }
        var phone = ""
        if (!thirdParty?.thirdPartyPhone1.isNullOrEmpty()) {
            phone += thirdParty?.thirdPartyPhone1
        }

        if (!thirdParty?.thirdPartyPhone2.isNullOrEmpty()) {
            phone += if (phone.isNotEmpty()) " - " + thirdParty?.thirdPartyPhone2 else thirdParty?.thirdPartyPhone2
        }

        htmlContent = if (phone.isNotEmpty()) {
            htmlContent.replace(
                "{clientphonevalue}",
                phone
            ).replace(
                "{phone_display}",
                "block"
            )
        } else {
            htmlContent.replace(
                "{phone_display}",
                "none"
            )
        }

        htmlContent = if (!thirdParty?.thirdPartyAddress.isNullOrEmpty()) {
            htmlContent.replace(
                "{clientaddressvalue}",
                thirdParty?.thirdPartyAddress ?: ""
            ).replace(
                "{addr_display}",
                "block"
            )
        } else {
            htmlContent.replace(
                "{addr_display}",
                "none"
            )
        }

        htmlContent = if (!invoiceHeader.invoiceHeadTaName.isNullOrEmpty()) {
            htmlContent.replace(
                "{table_name}",
                invoiceHeader.invoiceHeadTaName ?: ""
            ).replace(
                "{table_no_display}",
                "block"
            )
        } else {
            htmlContent.replace(
                "{table_no_display}",
                "none"
            )
        }

        htmlContent = if (!user?.userName.isNullOrEmpty()) {
            htmlContent.replace(
                "{invoiceuservalue}",
                user?.userName ?: ""
            ).replace(
                "{servedby_display}",
                "block"
            )
        } else {
            htmlContent.replace(
                "{servedby_display}",
                "none"
            )
        }

        htmlContent = if (invoiceHeader.invoiceHeadPrinted > 1) {
            htmlContent.replace(
                "{rp_disp}",
                "flex"
            )
        } else {
            htmlContent.replace(
                "{rp_disp}",
                "none"
            )
        }
        var discountAmount = invoiceHeader.invoiceHeadDiscountAmount
        val start = htmlContent.indexOf("{row_start}")
        val end = htmlContent.indexOf("{row_end}") + 9
        val extractedSubstring = htmlContent.substring(
            start,
            end
        ).replace(
            "{row_start}",
            ""
        ).replace(
            "{row_end}",
            ""
        )
        if (invoiceItemModels.isNotEmpty()) {
            val trs = StringBuilder("")
            var size = 0
            invoiceItemModels.forEach { item ->
                if (!item.isDeleted) {
                    size++
                    discountAmount += item.invoice.getDiscountAmount()
                    val qty = String.format(
                        "%,.0f",
                        item.invoice.invoiceQuantity
                    )
                    val price = String.format(
                        "%,.2f",
                        item.invoice.getPrice()
                    )
                    val amount = String.format(
                        "%,.2f",
                        item.invoice.getAmount()
                    )
                    trs.append(
                        extractedSubstring.replace(
                            "item_name",
                            item.getFullName()
                        ).replace(
                            "item_qty",
                            qty
                        ).replace(
                            "item_price",
                            price
                        ).replace(
                            "item_amount",
                            amount
                        )
                    )
                }
            }
            htmlContent = htmlContent.replaceRange(
                start,
                end,
                trs.toString()
            ).replace(
                "{numberofitemsvalue}",
                "$size"
            )
        } else {
            htmlContent = htmlContent.replaceRange(
                start,
                end,
                ""
            )
        }

        htmlContent = if (discountAmount > 0.0) {
            htmlContent.replace(
                "{inv_disc_amount}",
                POSUtils.formatDouble(
                    discountAmount,
                    2
                )
            ).replace(
                "{inv_disc_amount_disp}",
                "table-row"
            )
        } else {
            htmlContent.replace(
                "{inv_disc_amount_disp}",
                "none"
            )
        }

        htmlContent = if (SettingsModel.currentCompany?.companyUpWithTax == true && (SettingsModel.showTax || SettingsModel.showTax1 || SettingsModel.showTax2)) {
            htmlContent.replace(
                "{total_befor_tax}",
                POSUtils.formatDouble(
                    invoiceHeader.invoiceHeadTotal - invoiceHeader.invoiceHeadTotalTax,
                    2
                )
            ).replace(
                "{total_befor_tax_disp}",
                "table-row"
            )
        } else {
            htmlContent.replace(
                "{total_befor_tax_disp}",
                "none"
            )
        }

        var showTotalTax = false
        if (SettingsModel.showTax) {
            htmlContent = if (invoiceHeader.invoiceHeadTaxAmt > 0) {
                showTotalTax = true
                htmlContent.replace(
                    "{inv_tax_amount}",
                    POSUtils.formatDouble(
                        invoiceHeader.invoiceHeadTaxAmt,
                        2
                    )
                ).replace(
                    "{tax_perc}",
                    "${Utils.getDoubleOrZero(company?.companyTax)}%"
                ).replace(
                    "{inv_tax_disp}",
                    "table-row"
                )
            } else {
                htmlContent.replace(
                    "{inv_tax_disp}",
                    "none"
                )
            }
            htmlContent = if (!company?.companyTaxRegno.isNullOrEmpty()) {
                htmlContent.replace(
                    "{taxregno}",
                    company?.companyTaxRegno ?: ""
                ).replace(
                    "{tax_display}",
                    "block"
                )
            } else {
                htmlContent.replace(
                    "{tax_display}",
                    "none"
                )
            }
        } else {
            htmlContent = htmlContent.replace(
                "{tax_display}",
                "none"
            )
        }
        if (SettingsModel.showTax1) {
            htmlContent = if (invoiceHeader.invoiceHeadTax1Amt > 0) {
                showTotalTax = true
                htmlContent.replace(
                    "{inv_tax1_amount}",
                    POSUtils.formatDouble(
                        invoiceHeader.invoiceHeadTax1Amt,
                        2
                    )
                ).replace(
                    "{tax1_perc}",
                    "${Utils.getDoubleOrZero(company?.companyTax1)}%"
                ).replace(
                    "{inv_tax1_disp}",
                    "table-row"
                )
            } else {
                htmlContent.replace(
                    "{inv_tax1_disp}",
                    "none"
                )
            }
            htmlContent = if (!company?.companyTax1Regno.isNullOrEmpty()) {
                htmlContent.replace(
                    "{taxregno1}",
                    company?.companyTax1Regno ?: ""
                ).replace(
                    "{tax1_display}",
                    "block"
                )
            } else {
                htmlContent.replace(
                    "{tax1_display}",
                    "none"
                )
            }
        } else {
            htmlContent = htmlContent.replace(
                "{tax1_display}",
                "none"
            )
        }
        if (SettingsModel.showTax2) {
            htmlContent = if (invoiceHeader.invoiceHeadTax2Amt > 0) {
                showTotalTax = true
                htmlContent.replace(
                    "{inv_tax2_amount}",
                    POSUtils.formatDouble(
                        invoiceHeader.invoiceHeadTax2Amt,
                        2
                    )
                ).replace(
                    "{tax2_perc}",
                    "${Utils.getDoubleOrZero(company?.companyTax2)}%"
                ).replace(
                    "{inv_tax2_disp}",
                    "table-row"
                )
            } else {
                htmlContent.replace(
                    "{inv_tax2_disp}",
                    "none"
                )
            }
            htmlContent = if (!company?.companyTax2Regno.isNullOrEmpty()) {
                htmlContent.replace(
                    "{taxregno2}",
                    company?.companyTax2Regno ?: ""
                ).replace(
                    "{tax2_display}",
                    "block"
                )
            } else {
                htmlContent.replace(
                    "{tax2_display}",
                    "none"
                )
            }
        } else {
            htmlContent = htmlContent.replace(
                "{tax2_display}",
                "none"
            )
        }
        htmlContent = if (showTotalTax) {
            htmlContent.replace(
                "{inv_total_tax_amount}",
                POSUtils.formatDouble(
                    invoiceHeader.invoiceHeadTotalTax,
                    2
                )
            ).replace(
                "{inv_total_tax_disp}",
                "table-row"
            )
        } else {
            htmlContent.replace(
                "{inv_total_tax_disp}",
                "none"
            )
        }

        htmlContent = htmlContent.replace(
            "{inv_total}",
            POSUtils.formatDouble(
                invoiceHeader.invoiceHeadTotal,
                2
            )
        ).replace(
            "{curr1_code}",
            currency?.currencyCode1 ?: ""
        ).replace(
            "{inv_total1}",
            POSUtils.formatDouble(
                invoiceHeader.invoiceHeadTotal1,
                2
            )
        ).replace(
            "{curr2_code}",
            currency?.currencyCode2 ?: ""
        )

        var displayReceiptDashed = false

        val prCash = Utils.getDoubleOrZero(posReceipt.posReceiptCash)
        htmlContent = if (prCash > 0.0) {
            displayReceiptDashed = true
            htmlContent.replace(
                "{receipt_cash}",
                POSUtils.formatDouble(
                    prCash,
                    2
                )
            ).replace(
                "{receipt_cash_disp}",
                "table-row"
            )
        } else {
            htmlContent.replace(
                "{receipt_cash_disp}",
                "none"
            )
        }

        val prCashs = Utils.getDoubleOrZero(posReceipt.posReceiptCashs)
        htmlContent = if (prCashs > 0.0) {
            displayReceiptDashed = true
            htmlContent.replace(
                "{receipt_cashs}",
                POSUtils.formatDouble(
                    prCashs,
                    2
                )
            ).replace(
                "{receipt_cashs_disp}",
                "table-row"
            )
        } else {
            htmlContent.replace(
                "{receipt_cashs_disp}",
                "none"
            )
        }

        val prCredit = Utils.getDoubleOrZero(posReceipt.posReceiptCredit)
        htmlContent = if (prCredit > 0.0) {
            displayReceiptDashed = true
            htmlContent.replace(
                "{receipt_credit}",
                POSUtils.formatDouble(
                    prCredit,
                    2
                )
            ).replace(
                "{receipt_credit_disp}",
                "table-row"
            )
        } else {
            htmlContent.replace(
                "{receipt_credit_disp}",
                "none"
            )
        }
        val prCredits = Utils.getDoubleOrZero(posReceipt.posReceiptCredits)
        htmlContent = if (prCredits > 0.0) {
            displayReceiptDashed = true
            htmlContent.replace(
                "{receipt_credits}",
                POSUtils.formatDouble(
                    prCredits,
                    2
                )
            ).replace(
                "{receipt_credits_disp}",
                "table-row"
            )
        } else {
            htmlContent.replace(
                "{receipt_credits_disp}",
                "none"
            )
        }

        val prDebit = Utils.getDoubleOrZero(posReceipt.posReceiptDebit)
        htmlContent = if (prDebit > 0.0) {
            displayReceiptDashed = true
            htmlContent.replace(
                "{receipt_debit}",
                POSUtils.formatDouble(
                    prDebit,
                    2
                )
            ).replace(
                "{receipt_debit_disp}",
                "table-row"
            )
        } else {
            htmlContent.replace(
                "{receipt_debit_disp}",
                "none"
            )
        }
        val prDebits = Utils.getDoubleOrZero(posReceipt.posReceiptDebits)
        htmlContent = if (prDebits > 0.0) {
            displayReceiptDashed = true
            htmlContent.replace(
                "{receipt_debits}",
                POSUtils.formatDouble(
                    prDebits,
                    2
                )
            ).replace(
                "{receipt_debits_disp}",
                "table-row"
            )
        } else {
            htmlContent.replace(
                "{receipt_debits_disp}",
                "none"
            )
        }
        val change = Utils.getDoubleOrZero(invoiceHeader.invoiceHeadChange)
        htmlContent = if (change != 0.0) {
            displayReceiptDashed = true
            htmlContent.replace(
                "{inv_change}",
                POSUtils.formatDouble(
                    change,
                    2
                )
            ).replace(
                "{inv_change_disp}",
                "table-row"
            )
        } else {
            htmlContent.replace(
                "{inv_change_disp}",
                "none"
            )
        }

        htmlContent = htmlContent.replace(
            "{prsReceipt_dashed_display}",
            if (displayReceiptDashed) "block" else "none"
        )


        htmlContent = if (!invoiceHeader.invoiceHeadNote.isNullOrEmpty()) {
            htmlContent.replace(
                "{invoicenotevalue}",
                invoiceHeader.invoiceHeadNote!!
            ).replace(
                "{note_display}",
                "block"
            )
        } else {
            htmlContent.replace(
                "{note_display}",
                "none"
            )
        }

        if (!invoiceNo.isNullOrEmpty()) {
            val barcodeBitmap = generateBarcodeBitmapWithText(
                invoiceNo,
                400,
                150,
                true
            )
            val base64Barcode = convertBitmapToBase64(barcodeBitmap)
            htmlContent = htmlContent.replace(
                "{barcodeContent}",
                " <img style=\"width:100%;margin-start: 20px !important;margin-end: 20px !important;height:100px;\" src=\"data:image/png;base64,$base64Barcode\" alt=\"Barcode\"/>"
            ).replace(
                "{barcode_display}",
                "block"
            )
        } else {
            htmlContent = htmlContent.replace(
                "{barcode_display}",
                "none"
            )
        }

        return ReportResult(
            true,
            htmlContent
        )
    }

    private fun parseHtmlContent(htmlContent: String): ByteArray {
        // Parse HTML content
        val document = Jsoup.parse(htmlContent)
        val body = document.body()
        var result = parseHtmlElement(body)
        for (i in 0 until 5) {
            result += LINE_FEED
        }
        result += CUT_PAPER
        return result
    }

    private fun parseHtmlElement(element: Element): ByteArray {

        var result = byteArrayOf()
        val parentStyle = element.attr("style").lowercase().replace(
            " ",
            ""
        )
        if (parentStyle.contains("display:none")) {
            return result
        }

        val children = element.children()
        if (children.size > 0) {
            for (child in children) {
                when (child.tagName()) {
                    "b", "strong" -> {
                        result += BOLD_ON
                        result += parseHtmlElement(child)
                        result += BOLD_OFF
                    }

                    "i", "em" -> {
                        result += ITALIC_ON
                        result += parseHtmlElement(child)
                        result += ITALIC_OFF
                    }

                    "u" -> {
                        result += UNDERLINE_ON
                        result += parseHtmlElement(child)
                        result += UNDERLINE_OFF
                    }

                    "font" -> {
                        result += DOUBLE_SIZE
                        result += parseHtmlElement(child)
                        result += NORMAL_SIZE
                    }

                    "img" -> {
                        val src = child.attr("src")
                        val base64Data = src.substringAfter("base64,")
                        if (base64Data.isNotEmpty()) {
                            val decodedString: ByteArray = Base64.decode(
                                base64Data,
                                Base64.DEFAULT
                            )
                            val decodedByte = BitmapFactory.decodeByteArray(
                                decodedString,
                                0,
                                decodedString.size
                            )
                            val printPic = PrintPicture.getInstance()
                            printPic.init(decodedByte)
                            result += ALIGN_CENTER
                            result += printPic.printDraw()
                            result += ALIGN_LEFT
                        }
                    }

                    "hr" -> {
                        result += ALIGN_CENTER
                        result += getFontSizeByteCommand(16f)
                        result += "------------------------------\n".toByteArray()
                        result += ALIGN_LEFT
                    }

                    "table" -> {
                        for (row in child.select("tr")) {
                            val rowStyle = row.attr("style").lowercase().replace(
                                " ",
                                ""
                            )
                            if (!rowStyle.contains("display:none")) {
                                val cells = row.select("td")
                                for ((index, cell) in cells.withIndex()) {
                                    result += parseHtmlElement(cell)
                                    if (index < cells.size - 1) {
                                        result += TAG_SPACE// Add tab space between cells
                                    }
                                }
                                result += NEW_LINE
                            }
                        }
                    }

                    else -> {
                        result += parseHtmlElement(child)
                    }
                }
            }
        } else {
            val text = element.text() ?: ""
            if (text.isNotEmpty()) {
                result += styleElement(
                    parentStyle
                )
                result += text.toByteArray()
                if (element.tagName().equals("div") || element.tagName().equals("p")) {
                    result += NEW_LINE
                }
            }
        }

        return result
    }

    private fun styleElement(
            style: String
    ): ByteArray {
        var res = byteArrayOf()
        res += if (style.contains("align-items:right") || style.contains("text-align:right") || style.contains("justify-content:right")) {
            ALIGN_RIGHT
        } else if (style.contains("align-items:center") || style.contains("text-align:center") || style.contains("justify-content:center")) {
            ALIGN_CENTER
        } else {
            ALIGN_LEFT
        }

        res += if (style.contains("font-size")) {
            getFontSizeByteCommand(getFontSizeFromStyle(style))
        } else {
            getFontSizeByteCommand(16f)
        }

        if (style.contains("font-weight")) {
            res += if (style.contains("bold")) BOLD_ON else BOLD_OFF
        }

        return res
    }

    private fun getFontSizeFromStyle(style: String): Float {
        val regex = Regex("""font-size:\s*(\d+(\.\d+)?)(px|em|rem|pt);?""")
        val matchResult = regex.find(style)
        val sizeValue = matchResult?.groups?.get(1)?.value?.toFloat()
        val unit = matchResult?.groups?.get(3)?.value

        return when (unit) {
            "px" -> sizeValue ?: 16f
            "em", "rem" -> sizeValue?.times(16) ?: 16f  // Assuming 1em = 16px
            "pt" -> sizeValue?.times(1.33333f) ?: 16f // 1pt = 1.33333px
            else -> 16f
        }
    }

    private fun getFontSizeByteCommand(fontSizePx: Float): ByteArray {
        return when {
            fontSizePx <= 16 -> byteArrayOf(
                0x1B,
                0x21,
                0x00
            ) // Normal size
            fontSizePx <= 20 -> byteArrayOf(
                0x1B,
                0x21,
                0x10
            ) // Slightly larger (double height)
            fontSizePx <= 24 -> byteArrayOf(
                0x1B,
                0x21,
                0x20
            ) // Double width
            fontSizePx <= 30 -> byteArrayOf(
                0x1B,
                0x21,
                0x30
            ) // Double height and width
            else -> byteArrayOf(
                0x1B,
                0x21,
                0x30
            ) // Largest supported size (fallback)
        }
    }

    private fun getPayTicketHtmlContent(context: Context): ReportResult {
        var payslip = FileUtils.getHtmlFile(
            context,
            "${SettingsModel.defaultReportLanguage}/pay_ticket.html"
        )
        if (payslip.isEmpty()) {
            payslip = FileUtils.getHtmlFile(
                context,
                "${Language.DEFAULT.value}/pay_ticket.html"
            )
        }
        if (payslip.isNotEmpty()) {
            return ReportResult(
                true,
                payslip
            )
        }

        payslip = FileUtils.readFileFromAssets(
            "item_receipt.html",
            context
        )
        return ReportResult(
            false,
            payslip
        )
    }

    private fun getItemReceiptHtmlContent(
            context: Context,
            invoiceHeader: InvoiceHeader,
            invItemModels: List<InvoiceItemModel>
    ): ReportResult {
        val result = getPayTicketHtmlContent(context)
        if (!result.found) {
            return result
        }
        var htmlContent = result.htmlContent
        htmlContent = if (!invoiceHeader.invoiceHeadTaName.isNullOrEmpty()) {
            htmlContent.replace(
                "{table_name}",
                invoiceHeader.invoiceHeadTaName ?: ""
            ).replace(
                "{table_no_display}",
                "block"
            )
        } else {
            htmlContent.replace(
                "{table_no_display}",
                "none"
            )
        }
        htmlContent = if (!invoiceHeader.invoiceHeadOrderNo.isNullOrEmpty()) {
            htmlContent.replace(
                "{order_no}",
                invoiceHeader.invoiceHeadOrderNo ?: ""
            ).replace(
                "{order_no_display}",
                "block"
            )
        } else {
            htmlContent.replace(
                "{order_no_display}",
                "none"
            )
        }
        htmlContent = if (!invoiceHeader.invoiceHeadTransNo.isNullOrEmpty() && !invoiceHeader.invoiceHeadTransNo.equals("0")) {
            htmlContent.replace(
                "{trans_no}",
                invoiceHeader.invoiceHeadTransNo ?: ""
            ).replace(
                "{trans_no_display}",
                "block"
            )
        } else {
            htmlContent.replace(
                "{trans_no_display}",
                "none"
            )
        }

        htmlContent = htmlContent.replace(
            "{invoice_time}",
            DateHelper.getDateInFormat(
                invoiceHeader.invoiceHeadTimeStamp ?: Date(
                    invoiceHeader.invoiceHeadDateTime.div(
                        1000
                    )
                ),
                "dd/MM/yyyy hh:mm:ss"
            )
        )
        val start = htmlContent.indexOf("{row_start}")
        val end = htmlContent.indexOf("{row_end}") + 9
        val extractedSubstring = htmlContent.substring(
            start,
            end
        ).replace(
            "{row_start}",
            ""
        ).replace(
            "{row_end}",
            ""
        )
        if (invItemModels.isNotEmpty()) {
            val trs = StringBuilder("")
            invItemModels.forEach { item ->
                if (item.shouldPrint || item.isDeleted) {
                    val qty = String.format(
                        "%,.2f",
                        item.invoice.invoiceQuantity
                    )
                    trs.append(
                        extractedSubstring.replace(
                            "item_qty",
                            qty
                        ).replace(
                            "item_name",
                            item.getTicketFullName()
                        )
                    )
                }
            }
            htmlContent = htmlContent.replaceRange(
                start,
                end,
                trs.toString()
            )
        } else {
            htmlContent = htmlContent.replaceRange(
                start,
                end,
                ""
            )
        }
        return ReportResult(
            true,
            htmlContent
        )
    }

    suspend fun print(
            context: Context,
            invoiceHeader: InvoiceHeader,
            invoiceItemModels: MutableList<InvoiceItemModel>,
            posReceipt: PosReceipt,
            thirdParty: ThirdParty?,
            user: User?,
            company: Company?,
            printers: MutableList<PosPrinter>,
            reportResult: ReportResult?,
            printInvoice: Boolean = true
    ) {
        if (printInvoice && !SettingsModel.cashPrinter.isNullOrEmpty()) {
            val reportRes = reportResult ?: getInvoiceReceiptHtmlContent(
                context = context,
                invoiceHeader = invoiceHeader,
                invoiceItemModels = invoiceItemModels,
                posReceipt = posReceipt,
                thirdParty = thirdParty,
                user = user,
                company = company
            )
            if (reportRes.found) {
                val output = parseHtmlContent(reportRes.htmlContent)
                var ipAddress = ""
                var port = ""
                if (SettingsModel.cashPrinter!!.contains(":")) {
                    val printerDetails = SettingsModel.cashPrinter!!.split(":")
                    if (printerDetails.size == 2) {
                        if (isIpValid(printerDetails[0])) {
                            ipAddress = printerDetails[0]
                            port = printerDetails[1]
                        } else {
                            ipAddress = SettingsModel.cashPrinter!!
                            port = "9100"
                        }
                    }
                } else {
                    ipAddress = SettingsModel.cashPrinter!!
                    port = "9100"
                }
                printOutput(
                    context = context,
                    output = output,
                    printerName = SettingsModel.cashPrinter,
                    printerIP = ipAddress,
                    printerPort = port.toIntOrNull() ?: 9100
                )
            }
        }

        if (SettingsModel.autoPrintTickets) {
            printTickets(
                context = context,
                invoiceHeader = invoiceHeader,
                invoiceItemModels = invoiceItemModels,
                printers = printers
            )
        }
    }

    suspend fun printTickets(
            context: Context,
            invoiceHeader: InvoiceHeader,
            invoiceItemModels: MutableList<InvoiceItemModel>,
            printers: MutableList<PosPrinter>,
    ) {
        val itemsPrintersMap = invoiceItemModels.filter { it.shouldPrint || it.isDeleted }
            .groupBy { it.invoiceItem.itemPrinter ?: "" }
        itemsPrintersMap.entries.forEach { entry ->
            if (entry.key.isNotEmpty()) {
                val itemsPrinter = printers.firstOrNull { it.posPrinterId == entry.key }
                if (itemsPrinter != null) {
                    val reportResult = getItemReceiptHtmlContent(
                        context = context,
                        invoiceHeader = invoiceHeader,
                        invItemModels = entry.value
                    )
                    if (reportResult.found) {
                        val output = parseHtmlContent(reportResult.htmlContent)
                        printOutput(
                            context = context,
                            output = output,
                            printerName = itemsPrinter.posPrinterName,
                            printerIP = itemsPrinter.posPrinterHost,
                            printerPort = itemsPrinter.posPrinterPort
                        )
                    }
                }
            }
        }
    }

    private suspend fun printOutput(
            context: Context,
            output: ByteArray,
            printerName: String? = null,
            printerIP: String = "",
            printerPort: Int = 9100
    ) {
        if (printerIP.isNotEmpty() && printerPort != -1) {
            try {
                withContext(Dispatchers.IO) {
                    val socket = Socket()
                    socket.connect(
                        java.net.InetSocketAddress(
                            printerIP,
                            printerPort
                        ),
                        5000
                    )
                    val outputStream: OutputStream = socket.getOutputStream()
                    var offset = 0
                    val batchSize = 1024
                    while (offset < output.size) {
                        val end = minOf(
                            offset + batchSize,
                            output.size
                        )
                        outputStream.write(
                            output,
                            offset,
                            end - offset
                        )
                        offset += batchSize
                    }
                    outputStream.flush()
                    delay(2000L)// Assuming it takes 2 seconds to print the data
                    socket.close()
                }
            } catch (e: Exception) {
                Log.e(
                    "exception",
                    e.message.toString()
                )
            }
        } else {
            val printer = BluetoothPrinter()
            if (!printerName.isNullOrEmpty() && printer.connectToPrinter(
                    context,
                    printerName
                )
            ) {
                printer.printData(output)
                delay(2000L)// Assuming it takes 2 seconds to print the data
                printer.disconnectPrinter()
            }
        }
    }

    private fun generateBarcodeBitmapWithText(
            barcodeData: String,
            width: Int,
            height: Int,
            withText: Boolean
    ): Bitmap? {
        try {
            // Generate the barcode bitmap
            val barcodeEncoder = BarcodeEncoder()
            val hints = hashMapOf<EncodeHintType, Any>(EncodeHintType.CHARACTER_SET to "UTF-8")
            val barcodeBitmap = barcodeEncoder.encodeBitmap(
                barcodeData,
                BarcodeFormat.CODE_128,
                width,
                if (withText) (height * 0.75).toInt() else height,
                hints
            )

            if (!withText) {
                return barcodeBitmap
            }
            // Create a new bitmap with space for the barcode and text
            val bitmapWithText = Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmapWithText)

            // Draw the barcode on the upper half of the bitmap
            canvas.drawBitmap(
                barcodeBitmap,
                0f,
                0f,
                null
            )

            // Draw the barcode text below the barcode
            val paint = Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 20f // Adjust the text size as needed
                typeface = Typeface.create(
                    Typeface.SANS_SERIF,
                    Typeface.NORMAL
                )
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.5f
            }

            // Calculate the position for the text
            val xPos = width / 2f
            val yPos = (height * 0.875).toFloat() // Adjust this to position the text

            canvas.drawText(
                barcodeData,
                xPos,
                yPos,
                paint
            )

            return bitmapWithText
        } catch (e: WriterException) {
            e.printStackTrace()
        }

        return null
    }

    private fun convertBitmapToBase64(bitmap: Bitmap?): String? {
        if (bitmap == null) {
            return null
        }
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(
            Bitmap.CompressFormat.PNG,
            100,
            byteArrayOutputStream
        )
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(
            byteArray,
            Base64.NO_PADDING
        )
    }

    fun isIpValid(ip: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            InetAddresses.isNumericAddress(ip)
        } else {
            Patterns.IP_ADDRESS.matcher(ip).matches()
        }
    }
}