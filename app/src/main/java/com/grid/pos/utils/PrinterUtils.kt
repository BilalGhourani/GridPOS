package com.grid.pos.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.util.Base64
import android.util.Log
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
import com.grid.pos.model.PrintPicture
import com.grid.pos.model.SettingsModel
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
    private val CUT_PAPER = byteArrayOf(
        27,
        109
    )

    fun getInvoiceReceiptHtmlContent(
            context: Context,
            invoiceHeader: InvoiceHeader,
            invoiceItemModels: MutableList<InvoiceItemModel>,
            posReceipt: PosReceipt,
            thirdParty: ThirdParty? = null,
            user: User? = SettingsModel.currentUser,
            company: Company? = SettingsModel.currentCompany,
            currency: Currency? = SettingsModel.currentCurrency,
            content: String = FileUtils.readFileFromAssets(
                "invoice_receipt.html",
                context
            )
    ): String {
        val defaultLocal = Locale.getDefault()
        var result = content.ifEmpty { FileUtils.getDefaultReceipt() }
        val invDate = DateHelper.getDateFromString(
            invoiceHeader.invoiceHeadDate,
            "MMMM dd, yyyy 'at' hh:mm:ss a 'Z'"
        )
        result = result.replace(
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

        if (!invoiceHeader.invoiceHeadOrderNo.isNullOrEmpty()) {
            result = result.replace(
                "{invoicenumbervalue}",
                "<div style=\"font-weight: Normal;align-items: center;font-size: 16px;\">Invoice# ${invoiceHeader.invoiceHeadOrderNo}</div>"
            )
        }

        if (!company?.companyLogo.isNullOrEmpty()) {
            val logoBitmap = FileUtils.getBitmapFromPath(
                context,
                Uri.parse(company?.companyLogo)
            )
            val base64Result = convertBitmapToBase64(logoBitmap)
            result = result.replace(
                "{company_logo}",
                "<img src=\"data:image/png;base64,$base64Result\" width=\"100px\" height=\"50px\"/>"
            )
        } else {
            result = result.replace(
                "{company_logo}",
                ""
            )
        }

        result = if (!thirdParty?.thirdPartyName.isNullOrEmpty() || !invoiceHeader.invoiceHeadCashName.isNullOrEmpty()) {
            result.replace(
                "{clientnamevalue}",
                "<div style=\"font-weight: Normal;font-size: 16px;\">Client: ${thirdParty?.thirdPartyName ?: ""} ${invoiceHeader.invoiceHeadCashName ?: ""}</div>"
            )
        } else {
            result.replace(
                "{clientnamevalue}",
                ""
            )
        }

        result = if (!thirdParty?.thirdPartyFn.isNullOrEmpty()) {
            result.replace(
                "{clientfnvalue}",
                "<div style=\"font-weight: Normal;font-size: 16px;\">F/N: ${thirdParty?.thirdPartyFn}</div>"
            )
        } else {
            result.replace(
                "{clientfnvalue}",
                ""
            )
        }

        result = if (!thirdParty?.thirdPartyPhone1.isNullOrEmpty() || !thirdParty?.thirdPartyPhone2.isNullOrEmpty()) {
            result.replace(
                "{clientphonevalue}",
                "<div style=\"font-weight: Normal;font-size: 16px;\">Phone: ${thirdParty?.thirdPartyPhone1 ?: thirdParty?.thirdPartyPhone2}</div>"
            )
        } else {
            result.replace(
                "{clientphonevalue}",
                ""
            )
        }

        result = if (!thirdParty?.thirdPartyAddress.isNullOrEmpty()) {
            result.replace(
                "{clientaddressvalue}",
                "<div style=\"font-weight: Normal;font-size: 16px;\">Addr: ${thirdParty?.thirdPartyAddress}</div>"
            )
        } else {
            result.replace(
                "{clientaddressvalue}",
                ""
            )
        }

        result = if (!user?.userName.isNullOrEmpty()) {
            result.replace(
                "{invoiceuservalue}",
                "<div style=\"font-weight: Normal;font-size: 16px;\">Served By: ${user?.userName}</div>"
            )
        } else {
            result.replace(
                "{invoiceuservalue}",
                ""
            )
        }

        result = if (invoiceHeader.invoiceHeadPrint > 1) {
            result.replace(
                "{reprinted}",
                "<hr class=\"dashed\"> <div style=\"display: flex; align-items: center; justify-content: center;\">\n" + "            <div style=\"font-size: large; font-weight: bold;\"> * * REPRINTED * * </div>\n" + "        </div>"
            )
        } else {
            result.replace(
                "{reprinted}",
                ""
            )
        }
        if (invoiceItemModels.isNotEmpty()) {
            val trs = StringBuilder("<tr><td><b>Description</b></td><td><b>Total</b</td> </tr>")
            invoiceItemModels.forEach { item ->
                trs.append(
                    String.format(
                        defaultLocal,
                        "<tr><td>%s</td></tr> <tr><td>%.0f x %.2f</td> <td>%.2f</td> </tr>",
                        item.getFullName(),
                        item.invoice.invoiceQuantity,
                        item.invoice.getPrice(),
                        item.invoice.getAmount()
                    )
                )
            }
            result = result.replace(
                "{tableinvoiceitemsvalue}",
                trs.toString()
            ).replace(
                "{numberofitemsvalue}",
                "${invoiceItemModels.size}"
            )
        }
        val invAmountVal = StringBuilder(
            String.format(
                defaultLocal,
                "<tr><td>Disc Amount&nbsp;:</td><td>%.2f</td></tr>",
                Utils.getDoubleOrZero(invoiceHeader.invoiceHeadDiscountAmount)
            )
        )

        if (SettingsModel.currentCompany?.companyUpWithTax == true && (SettingsModel.showTax || SettingsModel.showTax1 || SettingsModel.showTax2)) {
            invAmountVal.append(
                String.format(
                    defaultLocal,
                    "<tr><td>Before Tax:</td><td>%.2f</td></tr>",
                    Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTotal - invoiceHeader.invoiceHeadTotalTax)
                )
            )
        }

        var showTotalTax = false
        if (SettingsModel.showTax) {
            if (invoiceHeader.invoiceHeadTaxAmt > 0) {
                showTotalTax = true
                invAmountVal.append(
                    String.format(
                        defaultLocal,
                        "<tr><td>Tax  (%.0f%s:</td><td>%.2f</td></tr>",
                        Utils.getDoubleOrZero(company?.companyTax),
                        "%)",
                        Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTaxAmt)
                    )
                )
            }
            result = if (!company?.companyTaxRegno.isNullOrEmpty()) {
                result.replace(
                    "{taxregno}",
                    "<div style=\"font-weight: Normal;font-size: 16px;\">Tax &nbsp; No:${company?.companyTaxRegno ?: ""}</div>"
                )
            } else {
                result.replace(
                    "{taxregno}",
                    ""
                )
            }
        } else {
            result = result.replace(
                "{taxregno}",
                ""
            )
        }
        if (SettingsModel.showTax1) {
            if (invoiceHeader.invoiceHeadTax1Amt > 0) {
                showTotalTax = true
                invAmountVal.append(
                    String.format(
                        defaultLocal,
                        "<tr><td>Tax1 (%.0f%s:</td><td>%.2f</td></tr>",
                        Utils.getDoubleOrZero(company?.companyTax1),
                        "%)",
                        Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTax1Amt)
                    )
                )
            }
            result = if (!company?.companyTax1Regno.isNullOrEmpty()) {
                result.replace(
                    "{taxregno1}",
                    "<div style=\"font-weight: Normal;font-size: 16px;\">Tax1 No:${company?.companyTax1Regno ?: ""}</div>"
                )
            } else {
                result.replace(
                    "{taxregno1}",
                    ""
                )
            }
        } else {
            result = result.replace(
                "{taxregno1}",
                ""
            )
        }
        if (SettingsModel.showTax2) {
            if (invoiceHeader.invoiceHeadTax2Amt > 0) {
                showTotalTax = true
                invAmountVal.append(
                    String.format(
                        defaultLocal,
                        "<tr><td>Tax2 (%.0f%s:</td><td>%.2f</td></tr>",
                        Utils.getDoubleOrZero(company?.companyTax2),
                        "%)",
                        Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTax2Amt)
                    )
                )
            }
            result = if (!company?.companyTax2Regno.isNullOrEmpty()) {
                result.replace(
                    "{taxregno2}",
                    "<div style=\"font-weight: Normal;font-size: 16px;\">Tax2 No:${company?.companyTax2Regno ?: ""}</div>"
                )
            } else {
                result.replace(
                    "{taxregno2}",
                    ""
                )
            }
        } else {
            result = result.replace(
                "{taxregno2}",
                ""
            )
        }
        result = if (showTotalTax) {
            invAmountVal.append(
                String.format(
                    defaultLocal,
                    "<tr><td>%s</td><td>%.2f</td></tr>",
                    "Total Tax:",
                    Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTotalTax)
                )
            )
            result.replace(
                "{taxdashed}",
                "<hr class=\"dashed\">"
            )
        } else {
            result.replace(
                "{taxdashed}",
                ""
            )
        }

        invAmountVal.append(
            String.format(
                defaultLocal,
                "<tr><td style=\"font-weight: bold;font-size: 16px;\">Total %s:&nbsp;&nbsp;</td><td style=\"font-weight: bold;font-size: 16px;\">%.2f</td></tr>",
                currency?.currencyCode1 ?: "",
                Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTotal)
            )
        )

        invAmountVal.append(
            String.format(
                defaultLocal,
                "<tr><td style=\"font-weight: bold;font-size: 16px;\">Total %s:&nbsp;&nbsp;</td><td style=\"font-weight: bold;font-size: 16px;\">%.2f</td></tr>",
                currency?.currencyCode2 ?: "",
                Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTotal1)
            )
        )

        result = result.replace(
            "{tableinvoiceAmountvalue}",
            invAmountVal.toString()
        )

        val posReceiptValues = StringBuilder("")

        val prCash = Utils.getDoubleOrZero(posReceipt.posReceiptCash)
        if (prCash > 0.0) {
            posReceiptValues.append(
                String.format(
                    defaultLocal,
                    "<tr><td>%s</td><td>%s</td><td>%.2f</td></tr>",
                    "Cash",
                    currency?.currencyCode1 ?: "",
                    prCash
                )
            )
        }
        val prCashs = Utils.getDoubleOrZero(posReceipt.posReceiptCashs)
        if (prCashs > 0.0) {
            posReceiptValues.append(
                String.format(
                    defaultLocal,
                    "<tr><td>%s</td><td>%s</td><td>%.2f</td></tr>",
                    "Cash",
                    currency?.currencyCode2 ?: "",
                    prCashs
                )
            )
        }

        val prCredit = Utils.getDoubleOrZero(posReceipt.posReceiptCredit)
        if (prCredit > 0.0) {
            posReceiptValues.append(
                String.format(
                    defaultLocal,
                    "<tr><td>%s</td><td>%s</td><td>%.2f</td></tr>",
                    "Credit",
                    currency?.currencyCode1 ?: "",
                    prCredit
                )
            )
        }
        val prCredits = Utils.getDoubleOrZero(posReceipt.posReceiptCredits)
        if (prCredits > 0.0) {
            posReceiptValues.append(
                String.format(
                    defaultLocal,
                    "<tr><td>%s</td><td>%s</td><td>%.2f</td></tr>",
                    "Credit",
                    currency?.currencyCode2 ?: "",
                    prCredits
                )
            )
        }

        val prDebit = Utils.getDoubleOrZero(posReceipt.posReceiptDebit)
        if (prDebit > 0.0) {
            posReceiptValues.append(
                String.format(
                    defaultLocal,
                    "<tr><td>%s</td><td>%s</td><td>%.2f</td></tr>",
                    "Debit",
                    currency?.currencyCode1 ?: "",
                    prDebit
                )
            )
        }
        val prDebits = Utils.getDoubleOrZero(posReceipt.posReceiptDebits)
        if (prDebits > 0.0) {
            posReceiptValues.append(
                String.format(
                    defaultLocal,
                    "<tr><td>%s</td><td>%s</td><td>%.2f</td></tr>",
                    "Debit",
                    currency?.currencyCode2 ?: "",
                    prDebits
                )
            )
        }

        posReceiptValues.append(
            String.format(
                defaultLocal,
                "<tr><td>%s</td><td>%s</td><td>%.2f</td></tr>",
                "Change",
                currency?.currencyCode1 ?: "",
                Utils.getDoubleOrZero(invoiceHeader.invoiceHeadChange)
            )
        )

        result = result.replace(
            "{posReceiptValues}",
            posReceiptValues.toString()
        )


        result = if (!invoiceHeader.invoiceHeadNote.isNullOrEmpty()) {
            result.replace(
                "{invoicenotevalue}",
                "<hr class=\"dashed\">\n" + "    <div style=\"width: 100%;display: flex; align-items: start; justify-content: start; flex-direction: column;\">\n" + "        <div style=\"font-weight: Normal;font-size: 16px;\">${invoiceHeader.invoiceHeadNote}</div>\n" + "    </div>"
            )
        } else {
            result.replace(
                "{invoicenotevalue}",
                ""
            )
        }

        if (!invoiceHeader.invoiceHeadOrderNo.isNullOrEmpty()) {
            val barcodeBitmap = generateBarcodeBitmapWithText(
                invoiceHeader.invoiceHeadOrderNo!!,
                400,
                150,
                true
            )
            val base64Barcode = convertBitmapToBase64(barcodeBitmap)
            result = result.replace(
                "{barcodeContent}",
                " <img style=\"width:100%;margin-start: 20px !important;margin-end: 20px !important;height:100px;\" src=\"data:image/png;base64,$base64Barcode\" alt=\"Barcode\"/>"
            )
        } else {
            result = result.replace(
                "{barcodeContent}",
                ""
            )
        }

        return result
    }

    private fun parseHtmlContent(htmlContent: String): ByteArray {
        // Parse HTML content
        val document = Jsoup.parse(htmlContent)
        val body = document.body()
        return parseHtmlElement(body) + ByteArray(3) { 0x0A } + CUT_PAPER
    }

    private fun parseHtmlElement(element: Element): ByteArray {

        var result = byteArrayOf()

        val children = element.children()
        if (children.size > 0) {
            for (child in children) {
                when (child.tagName()) {
                    "p" -> {
                        result += parseHtmlElement(child)
                        result += "\n".toByteArray()
                    }

                    "b", "strong" -> {
                        result += BOLD_ON
                        result += child.text().toByteArray()
                        result += BOLD_OFF
                    }

                    "i", "em" -> {
                        result += ITALIC_ON
                        result += child.text().toByteArray()
                        result += ITALIC_OFF
                    }

                    "u" -> {
                        result += UNDERLINE_ON
                        result += child.text().toByteArray()
                        result += UNDERLINE_OFF
                    }

                    "font" -> {
                        result += DOUBLE_SIZE
                        result += child.text().toByteArray()
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
                        result += "------------------------------\n".toByteArray()
                        result += ALIGN_LEFT
                    }

                    "div" -> {
                        val style = element.attr("style")
                        if (style.contains("text-align") || style.contains("justify-content")) {
                            result += if (style.contains("center")) ALIGN_CENTER else if (style.contains(
                                    "right"
                                ) || style.contains("end")
                            ) ALIGN_RIGHT else ALIGN_LEFT
                        }
                        result += parseHtmlElement(child)
                        result += ALIGN_LEFT
                    }

                    "span" -> {
                        result += parseHtmlElement(child)
                    }

                    "table" -> {
                        for (row in child.select("tr")) {
                            for (cell in row.select("td")) {
                                result += parseHtmlElement(cell)
                                result += "\t".toByteArray()// Add tab space between cells
                            }
                            result += "\n".toByteArray()
                        }
                    }

                    else -> {
                        result += styleElement(child)
                        result += child.text().toByteArray()
                        result += BOLD_OFF + ALIGN_LEFT + NORMAL_SIZE
                        if (child.tagName().equals("div")) {
                            result += "\n".toByteArray()
                        }
                    }
                }
            }
        } else {
            result += styleElement(element)
            result += element.text().toByteArray()
            if (element.tagName().equals("div")) {
                result += "\n".toByteArray()
            }
        }

        return result
    }

    private fun styleElement(element: Element): ByteArray {
        var res = byteArrayOf()
        val style = element.attr("style")
        if (style.contains("text-align") || style.contains("justify-content")) {
            res += if (style.contains("center")) ALIGN_CENTER else if (style.contains("right") || style.contains(
                    "end"
                )
            ) ALIGN_RIGHT else ALIGN_LEFT
        }

        if (style.contains("font-size")) {
            res += getFontSizeByteCommand(getFontSizeFromStyle(style.toString()))
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
            fontSizePx <= 32 -> byteArrayOf(
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

    private fun getItemReceiptHtmlContent(
            context: Context,
            content: String = FileUtils.readFileFromAssets(
                "item_receipt.html",
                context
            ),
            invoiceHeader: InvoiceHeader,
            invItemModels: List<InvoiceItemModel>
    ): String {
        var result = content.ifEmpty { FileUtils.getDefaultItemReceipt() }
        result = result.replace(
            "{table_name}",
            invoiceHeader.invoiceHeadTaName ?: ""
        )
        result = result.replace(
            "{order_no}",
            invoiceHeader.invoiceHeadOrderNo ?: ""
        )
        result = result.replace(
            "{trans_no}",
            invoiceHeader.invoiceHeadTransNo ?: ""
        )

        result = result.replace(
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
        val defaultLocal = Locale.getDefault()
        if (invItemModels.isNotEmpty()) {
            val trs = StringBuilder("")
            invItemModels.forEach { item ->
                trs.append(
                    String.format(
                        defaultLocal,
                        "<tr> <td>%.2f</td> <td>%s</td></tr>",
                        item.invoice.invoiceQuantity,
                        item.getName()
                    )
                )
            }
            result = result.replace(
                "{rows_content}",
                trs.toString()
            )
        }
        return result
    }

    suspend fun print(
            context: Context,
            invoiceHeader: InvoiceHeader,
            invoiceItemModels: MutableList<InvoiceItemModel>,
            posReceipt: PosReceipt,
            thirdParty: ThirdParty?,
            user: User?,
            company: Company?,
            printers: MutableList<PosPrinter>
    ) {
        if (!SettingsModel.cashPrinter.isNullOrEmpty()) {
            val htmlContent = getInvoiceReceiptHtmlContent(
                context = context,
                invoiceHeader = invoiceHeader,
                invoiceItemModels = invoiceItemModels,
                posReceipt = posReceipt,
                thirdParty = thirdParty,
                user = user,
                company = company
            )
            val output = parseHtmlContent(htmlContent)
            printOutput(
                context = context,
                output = output,
                printerName = SettingsModel.cashPrinter
            )
        }

        val itemsPrintersMap = invoiceItemModels.groupBy { it.invoiceItem.itemPrinter ?: "" }
        itemsPrintersMap.entries.forEach { entry ->
            if (entry.key.isNotEmpty()) {
                val itemsPrinter = printers.firstOrNull { it.posPrinterId == entry.key }
                if (itemsPrinter != null) {
                    val htmlContent = getItemReceiptHtmlContent(
                        context = context,
                        invoiceHeader = invoiceHeader,
                        invItemModels = entry.value
                    )
                    val output = parseHtmlContent(htmlContent)
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

    private suspend fun printOutput(
            context: Context,
            output: ByteArray,
            printerName: String? = null,
            printerIP: String = "",
            printerPort: Int = -1
    ) {
        val printer = BluetoothPrinter()
        if (!printerName.isNullOrEmpty() && printer.connectToPrinter(
                context,
                printerName
            )
        ) {
            printer.printData(output)
            delay(2000L)// Assuming it takes 2 seconds to print the data
            printer.disconnectPrinter()
        } else if (printerIP.isNotEmpty() && printerPort != -1) {
            try {
                withContext(Dispatchers.IO) {
                    Socket(
                        printerIP,
                        printerPort
                    ).use { socket ->
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
                }
            } catch (e: Exception) {
                Log.e(
                    "exception",
                    e.message.toString()
                )
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
}