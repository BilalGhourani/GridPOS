package com.grid.pos.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.text.Html
import android.text.Spannable
import android.text.Spanned
import android.util.Base64
import android.util.Log
import android.webkit.WebView
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.grid.pos.data.Company.Company
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.PosPrinter.PosPrinter
import com.grid.pos.data.PosReceipt.PosReceipt
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.data.User.User
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.SettingsModel
import com.journeyapps.barcodescanner.BarcodeEncoder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.util.Date

object PrinterUtils {
    fun printWebPage(
            webView: WebView?,
            context: Context
    ) {
        if (webView != null) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val jobName = "webpage_" + System.currentTimeMillis()
            val printAdapter = webView.createPrintDocumentAdapter(jobName)

            // Define Print Attributes (optional)
            val printAttributes = PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4/*getMediaSize()*/)
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS).build()
            printManager.print(
                jobName,
                printAdapter,
                printAttributes
            )
        }
    }

    private fun getMediaSize(): PrintAttributes.MediaSize {
        // Define the width and height in inches
        val widthInches = 3 // Typical width for POS receipt paper
        val heightInches = 11 // You can adjust this based on your needs

        // Convert inches to micrometers (1 inch = 25400 micrometers)
        val widthMicrometers = widthInches * 25400
        val heightMicrometers = heightInches * 25400

        return PrintAttributes.MediaSize(
            "POS Receipt",
            "POS Receipt",
            widthMicrometers,
            heightMicrometers
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
            content: String = FileUtils.readFileFromAssets(
                "invoice_receipt.html",
                context
            )
    ): String {
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
            "{invoicenumbervalue}",
            invoiceHeader.invoiceHeadTransNo ?: ""
        ).replace(
            "{invoicedatevalue}",
            DateHelper.getDateInFormat(
                invDate,
                "dd/MM/yyyy hh:mm:ss"
            )
        )

        if (!company?.companyLogo.isNullOrEmpty()) {
            val barcodeBitmap = FileUtils.getBitmapFromPath(
                context,
                Uri.parse(company?.companyLogo)
            )
            val base64Barcode = convertBitmapToBase64(barcodeBitmap)
            result = result.replace(
                "{company_logo}",
                "<img src=\"data:image/png;base64,$base64Barcode\" width=\"50px\" height=\"50px\"/>"
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
                "<div class=\"text1\">Client: ${thirdParty?.thirdPartyName ?: ""} ${invoiceHeader.invoiceHeadCashName ?: ""}</div>"
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
                "<div class=\"text1\">F/N: ${thirdParty?.thirdPartyFn}</div>"
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
                "<div class=\"text1\">Phone: ${thirdParty?.thirdPartyPhone1 ?: thirdParty?.thirdPartyPhone2}</div>"
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
                "<div class=\"text1\">Addr: ${thirdParty?.thirdPartyAddress}</div>"
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
                "<div class=\"text1\">Served By: ${user?.userName}</div>"
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
                "<hr class=\"dashed\"> <div style=\"display: flex; align-items: center; justify-content: center;\">\n" + "            <div style=\"font-size: 30px; font-weight: bold;\"> * * REPRINTED * * </div>\n" + "        </div>"
            )
        } else {
            result.replace(
                "{reprinted}",
                ""
            )
        }
        if (invoiceItemModels.isNotEmpty()) {
            val trs = StringBuilder("<tr> <td>Description</td>  <td>Qty</td> <td>UP</td> <td>T.Price</td>  </tr>")
            invoiceItemModels.forEach { item ->
                trs.append("<tr>")
                trs.append("<td>${item.getFullName()}</td> ")
                trs.append(
                    "<td>${
                        String.format(
                            "%.2f",
                            item.getQuantity()
                        )
                    }</td>"
                )
                trs.append(
                    "<td>${
                        String.format(
                            "%.2f",
                            item.getPrice()
                        )
                    }</td>"
                )
                trs.append(
                    "<td>${
                        String.format(
                            "%.2f",
                            item.getNetAmount()
                        )
                    }</td>"
                )
                trs.append("</tr>")
            }
            result = result.replace(
                "{tableinvoiceitemsvalue}",
                trs.toString()
            ).replace(
                "{numberofitemsvalue}",
                "${invoiceItemModels.size}"
            )
        }
        val invAmountVal = StringBuilder("")
        invAmountVal.append("<tr>")
        invAmountVal.append("<td>Disc Amount:</td> ")
        invAmountVal.append(
            "<td>${
                String.format(
                    "%.2f",
                    Utils.getDoubleOrZero(invoiceHeader.invoiceHeadDiscountAmount)
                )
            }</td>"
        )
        invAmountVal.append("</tr>")

        if (SettingsModel.showTax) {
            invAmountVal.append("<tr>")
            invAmountVal.append(
                "<td>Tax(${
                    String.format(
                        "%.0f",
                        Utils.getDoubleOrZero(company?.companyTax)
                    )
                }%):</td> "
            )
            invAmountVal.append(
                "<td>${
                    String.format(
                        "%.2f",
                        Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTaxAmt)
                    )
                }</td>"
            )
            invAmountVal.append("</tr>")
            result = result.replace(
                "{taxregno}",
                "<div class=\"text1\">Tax &nbsp; No:${company?.companyTaxRegno ?: ""}</div>"
            )
        } else {
            result = result.replace(
                "{taxregno}",
                ""
            )
        }
        if (SettingsModel.showTax1) {
            invAmountVal.append("<tr>")
            invAmountVal.append(
                "<td>Tax1(${
                    String.format(
                        "%.0f",
                        Utils.getDoubleOrZero(company?.companyTax1)
                    )
                }%):</td> "
            )
            invAmountVal.append(
                "<td>${
                    String.format(
                        "%.2f",
                        Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTax1Amt)
                    )
                }</td>"
            )
            invAmountVal.append("</tr>")
            result = result.replace(
                "{taxregno1}",
                "<div class=\"text1\">Tax1 No:${company?.companyTax1Regno ?: ""}</div>"
            )
        } else {
            result = result.replace(
                "{taxregno1}",
                ""
            )
        }
        if (SettingsModel.showTax2) {
            invAmountVal.append("<tr>")
            invAmountVal.append(
                "<td>Tax2(${
                    String.format(
                        "%.0f",
                        Utils.getDoubleOrZero(company?.companyTax2)
                    )
                }%):</td> "
            )
            invAmountVal.append(
                "<td>${
                    String.format(
                        "%.2f",
                        Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTax2Amt)
                    )
                }</td>"
            )
            invAmountVal.append("</tr>")
            result = result.replace(
                "{taxregno2}",
                "<div class=\"text1\">Tax2 No:${company?.companyTax2Regno ?: ""}</div>"
            )
        } else {
            result = result.replace(
                "{taxregno2}",
                ""
            )
        }
        if (SettingsModel.showTax2 || SettingsModel.showTax2 || SettingsModel.showTax2) {
            invAmountVal.append("<tr>")
            invAmountVal.append("<td>T.Tax:</td> ")
            invAmountVal.append(
                "<td>${
                    String.format(
                        "%.2f",
                        Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTotalTax)
                    )
                }</td>"
            )
            invAmountVal.append("</tr>")
            result = result.replace(
                "{taxdashed}",
                "<hr class=\"dashed\">"
            )
        } else {
            result = result.replace(
                "{taxdashed}",
                ""
            )
        }

        invAmountVal.append("<tr>")
        invAmountVal.append("<td class=\"text2\">Total ${currency?.currencyCode1 ?: ""}:</td> ")
        invAmountVal.append(
            "<td class=\"text2\">${
                String.format(
                    "%.2f",
                    Utils.getDoubleOrZero(invoiceHeader.invoiceHeadGrossAmount)
                )
            }</td>"
        )
        invAmountVal.append("</tr>")

        invAmountVal.append("<tr>")
        invAmountVal.append("<td class=\"text2\">Total ${currency?.currencyCode2 ?: ""}:</td> ")
        invAmountVal.append(
            "<td class=\"text2\">${
                String.format(
                    "%.2f",
                    Utils.getDoubleOrZero(invoiceHeader.invoiceHeadGrossAmount) * (currency?.currencyRate ?: 1.0)
                )
            }</td>"
        )
        invAmountVal.append("</tr>")

        result = result.replace(
            "{tableinvoiceAmountvalue}",
            invAmountVal.toString()
        )

        val posReceiptValues = StringBuilder("")

        val pr_cash = Utils.getDoubleOrZero(posReceipt.posReceiptCash)
        if (pr_cash > 0.0) {
            posReceiptValues.append("<tr>")
            posReceiptValues.append("<td>Cash</td> ")
            posReceiptValues.append("<td>${currency?.currencyCode1 ?: ""}</td>")
            posReceiptValues.append(
                "<td>${
                    String.format(
                        "%.2f",
                        pr_cash
                    )
                }</td>"
            )
            posReceiptValues.append("</tr>")
        }
        val pr_cashs = Utils.getDoubleOrZero(posReceipt.posReceiptCashs)
        if (pr_cashs > 0.0) {
            posReceiptValues.append("<tr>")
            posReceiptValues.append("<td>Cash</td> ")
            posReceiptValues.append("<td>${currency?.currencyCode2 ?: ""}</td>")
            posReceiptValues.append(
                "<td>${
                    String.format(
                        "%.2f",
                        pr_cashs
                    )
                }</td>"
            )
            posReceiptValues.append("</tr>")
        }

        val pr_credit = Utils.getDoubleOrZero(posReceipt.posReceiptCredit)
        if (pr_credit > 0.0) {
            posReceiptValues.append("<tr>")
            posReceiptValues.append("<td>Credit</td> ")
            posReceiptValues.append("<td>${currency?.currencyCode1 ?: ""}</td>")
            posReceiptValues.append(
                "<td>${
                    String.format(
                        "%.2f",
                        pr_credit
                    )
                }</td>"
            )
            posReceiptValues.append("</tr>")
        }
        val pr_credits = Utils.getDoubleOrZero(posReceipt.posReceiptCredits)
        if (pr_credits > 0.0) {
            posReceiptValues.append("<tr>")
            posReceiptValues.append("<td>Credit</td> ")
            posReceiptValues.append("<td>${currency?.currencyCode2 ?: ""}</td>")
            posReceiptValues.append(
                "<td>${
                    String.format(
                        "%.2f",
                        pr_credits
                    )
                }</td>"
            )
            posReceiptValues.append("</tr>")
        }

        val pr_debit = Utils.getDoubleOrZero(posReceipt.posReceiptDebit)
        if (pr_debit > 0.0) {
            posReceiptValues.append("<tr>")
            posReceiptValues.append("<td>Debit</td> ")
            posReceiptValues.append("<td>${currency?.currencyCode1 ?: ""}</td>")
            posReceiptValues.append(
                "<td>${
                    String.format(
                        "%.2f",
                        pr_debit
                    )
                }</td>"
            )
            posReceiptValues.append("</tr>")
        }
        val pr_debits = Utils.getDoubleOrZero(posReceipt.posReceiptDebits)
        if (pr_debits > 0.0) {
            posReceiptValues.append("<tr>")
            posReceiptValues.append("<td>Debit</td> ")
            posReceiptValues.append("<td>${currency?.currencyCode2 ?: ""}</td>")
            posReceiptValues.append(
                "<td>${
                    String.format(
                        "%.2f",
                        pr_debits
                    )
                }</td>"
            )
            posReceiptValues.append("</tr>")
        }

        posReceiptValues.append("<tr>")
        posReceiptValues.append("<td>Change</td> ")
        posReceiptValues.append(
            "<td>${
                String.format(
                    "%.2f",
                    Utils.getDoubleOrZero(invoiceHeader.invoiceHeadChange)
                )
            }</td>"
        )
        posReceiptValues.append("<td>${currency?.currencyCode2 ?: ""}</td>")
        posReceiptValues.append("</tr>")

        result = result.replace(
            "{posReceiptValues}",
            posReceiptValues.toString()
        )


        if (!invoiceHeader.invoiceHeadNote.isNullOrEmpty()) {
            result = result.replace(
                "{invoicenotevalue}",
                "<hr class=\"dashed\">\n" + "    <div style=\"width: 100%;display: flex; align-items: start; justify-content: start; flex-direction: column;\">\n" + "        <div class=\"text1\">${invoiceHeader.invoiceHeadNote}</div>\n" + "    </div>"
            )
        } else {
            result = result.replace(
                "{invoicenotevalue}",
                ""
            )
        }

        if (!invoiceHeader.invoiceHeadTransNo.isNullOrEmpty()) {
            val barcodeBitmap = generateBarcode(invoiceHeader.invoiceHeadTransNo!!)
            val base64Barcode = convertBitmapToBase64(barcodeBitmap)
            result = result.replace(
                "{barcodeContent}",
                " <img class=\"barcode\" src=\"data:image/png;base64,$base64Barcode\" alt=\"Barcode\"/>"
            )
        }

        return result
    }

    fun getItemReceiptHtmlContent(
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
        if (invItemModels.isNotEmpty()) {
            val trs = StringBuilder("")
            invItemModels.forEach { item ->
                trs.append(
                    "<tr> <td>${
                        String.format(
                            "%.2f",
                            item.getQuantity()
                        )
                    }</td> <td>${item.getName()}</td>  </tr>"
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
        SettingsModel.cashPrinter?.let { companyPrinter ->
            val invoiceContent = getInvoiceReceiptHtmlContent(
                context = context,
                invoiceHeader,
                invoiceItemModels,
                posReceipt,
                thirdParty,
                user,
                company
            )
            val document = parseHtml(invoiceContent)
            printInvoice(
                context = context,
                byteArray = convertHtmlToEscPos(document),
                printerName = companyPrinter
            )
        }

        val itemsPrintersMap = invoiceItemModels.groupBy { it.invoiceItem.itemPrinter ?: "" }
        itemsPrintersMap.entries.forEach { entry ->
            if (entry.key.isNotEmpty()) {
                val itemsPrinter = printers.firstOrNull { it.posPrinterId == entry.key }
                if (itemsPrinter != null) {
                    val invoiceContent = getItemReceiptHtmlContent(
                        context = context,
                        invoiceHeader = invoiceHeader,
                        invItemModels = entry.value
                    )
                    val document = parseHtml(invoiceContent)
                    printInvoice(
                        context = context,
                        byteArray = convertHtmlToEscPos(document),
                        printerName = itemsPrinter.posPrinterName,
                        printerIP = itemsPrinter.posPrinterHost,
                        printerPort = itemsPrinter.posPrinterPort
                    )
                }
            }
        }
    }

    fun printInvoice(
            context: Context,
            byteArray: ByteArray,
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
            printer.printData(byteArray)
            printer.disconnectPrinter()
        } else if (printerIP.isNotEmpty() && printerPort!=-1) {
            try {
                Socket(
                    printerIP,
                    printerPort
                ).use { socket ->
                    val outputStream: OutputStream = socket.getOutputStream()
                    val inputStream: InputStream = ByteArrayInputStream(byteArray)
                    var bytesRead: Int

                    // Read the PDF file and send its bytes to the printer
                    while (inputStream.read(byteArray).also { bytesRead = it } != -1) {
                        outputStream.write(
                            byteArray,
                            0,
                            bytesRead
                        )
                    }

                    outputStream.flush()
                    socket.close()/*PrintWriter(sock.getOutputStream(), true).use { printWriter ->
                    printWriter.println(htmlContent.parseAsHtml())
                    printWriter.flush()
                    sock.close()
                }*/
                }
            } catch (e: Exception) {
                Log.e(
                    "exception",
                    e.message.toString()
                )
            }
        }
    }

    private fun generateBarcode(data: String): Bitmap? {
        val barcodeEncoder = BarcodeEncoder()
        return try {
            val bitMatrix: BitMatrix = barcodeEncoder.encode(
                data,
                BarcodeFormat.CODE_128,
                400,
                150
            )
            barcodeEncoder.createBitmap(bitMatrix)
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

    fun convertBitmapToBase64(bitmap: Bitmap?): String? {
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
            Base64.NO_WRAP
        )
    }

    fun parseHtml(html: String): Document {
       return Jsoup.parse(html)
    }

    fun convertHtmlToEscPos(document: Document): ByteArray {
        val esc = 0x1B.toChar() // ESC character
        val sb = StringBuilder()

        fun processElement(element: Element) {
            when (element.tagName()) {
                "b" -> sb.append("$esc" + "E" + 0x01.toChar()) // Bold on
                "i" -> sb.append("$esc" + "4") // Italic on
                "u" -> sb.append("$esc" + "-1") // Underline on
                "p" -> sb.append("\n") // Paragraph break (new line)
                "div" -> sb.append("\n") // Div break (new line)
                "table" -> sb.append("\n") // Table break (new line)
                "tr" -> sb.append("\n") // Table row break (new line)
                "td" -> sb.append("\t") // Table cell (tab space, adjust if necessary)
                "font" -> {
                    val color = element.attr("color")
                    when (color) {
                        "red" -> sb.append("$esc" + "r1") // Red text
                        // Add more color mappings as needed
                    }
                }
                // Add more tag mappings as needed
            }

            // Process the element's text content
            sb.append(element.text())

            // Recursively process child elements
            for (child in element.children()) {
                processElement(child)
            }

            // Turn off styles after processing the element
            when (element.tagName()) {
                "b" -> sb.append("$esc" + "E" + 0x00.toChar()) // Bold off
                "i" -> sb.append("$esc" + "5") // Italic off
                "u" -> sb.append("$esc" + "-0") // Underline off
                "font" -> sb.append("$esc" + "r0") // Reset color
            }
        }

        processElement(document.body())

        return sb.toString().toByteArray(Charsets.UTF_8)
    }
}