package com.grid.pos.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import android.util.Log
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
import kotlinx.coroutines.delay
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.net.Socket
import java.util.Date

object PrinterUtils {

    //line space => byteArrayOf(0x1B, 0x33, n.toByte())
    private val DOUBLE_SIZE = byteArrayOf(
        0x1B,
        0x21,
        0x01
    )
    private val NORMAL_SIZE = byteArrayOf(
        0x1B,
        0x21,
        0x00
    )
    private val BOLD = byteArrayOf(
        27,
        69,
        1
    )
    private val NORMAL = byteArrayOf(
        27,
        69,
        0
    )
    private val ITALIC = byteArrayOf(0x1B, 0x34)
    private val DISABLE_ITALIC = byteArrayOf(0x1B, 0x35)
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
    private val DOUBLE_HEIGHT = byteArrayOf(
        27,
        33,
        1
    )
    private val DOUBLE_WIDTH = byteArrayOf(
        27,
        33,
        0
    )
    private val DEFAULT_SIZE = byteArrayOf(
        27,
        33,
        0
    )
    private val CUT_PAPER = byteArrayOf(
        27,
        109
    )
    private val IMAGE_PRINT_COMMAND = byteArrayOf(
        0x1B,
        0x42
    ) // Example for ESC/POS
    private val IMAGE_END_COMMAND = byteArrayOf(
        0x1B,
        0x42
    ) // Example for ESC/POS

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

        result =
            if (!thirdParty?.thirdPartyName.isNullOrEmpty() || !invoiceHeader.invoiceHeadCashName.isNullOrEmpty()) {
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

        result =
            if (!thirdParty?.thirdPartyPhone1.isNullOrEmpty() || !thirdParty?.thirdPartyPhone2.isNullOrEmpty()) {
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
            val trs =
                StringBuilder("<tr> <td>Description</td>  <td>Qty</td> <td>UP</td> <td>T.Price</td>  </tr>")
            invoiceItemModels.forEach { item ->
                trs.append("<tr>")
                trs.append("<td>${item.getFullName()}</td> ")
                trs.append(
                    "<td>${
                        String.format(
                            "%.2f",
                            item.invoice.invoiceQuantity
                        )
                    }</td>"
                )
                trs.append(
                    "<td>${
                        String.format(
                            "%.2f",
                            item.invoice.getPrice()
                        )
                    }</td>"
                )
                trs.append(
                    "<td>${
                        String.format(
                            "%.2f",
                            item.invoice.getAmount()
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

        var showTotalTax = false
        if (SettingsModel.showTax) {
            if (invoiceHeader.invoiceHeadTaxAmt > 0) {
                showTotalTax = true
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
            }
            result = if (!company?.companyTaxRegno.isNullOrEmpty()) {
                result.replace(
                    "{taxregno}",
                    "<div class=\"text1\">Tax &nbsp; No:${company?.companyTaxRegno ?: ""}</div>"
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
            }
            result = if (!company?.companyTax1Regno.isNullOrEmpty()) {
                result.replace(
                    "{taxregno1}",
                    "<div class=\"text1\">Tax1 No:${company?.companyTax1Regno ?: ""}</div>"
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
            }
            result = if (!company?.companyTax2Regno.isNullOrEmpty()) {
                result.replace(
                    "{taxregno2}",
                    "<div class=\"text1\">Tax2 No:${company?.companyTax2Regno ?: ""}</div>"
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
        if (showTotalTax) {
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
                    Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTotal)
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
                    Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTotal1)
                )
            }</td>"
        )
        invAmountVal.append("</tr>")

        result = result.replace(
            "{tableinvoiceAmountvalue}",
            invAmountVal.toString()
        )

        val posReceiptValues = StringBuilder("")

        val prCash = Utils.getDoubleOrZero(posReceipt.posReceiptCash)
        if (prCash > 0.0) {
            posReceiptValues.append("<tr>")
            posReceiptValues.append("<td>Cash</td> ")
            posReceiptValues.append("<td>${currency?.currencyCode1 ?: ""}</td>")
            posReceiptValues.append(
                "<td>${
                    String.format(
                        "%.2f",
                        prCash
                    )
                }</td>"
            )
            posReceiptValues.append("</tr>")
        }
        val prCashs = Utils.getDoubleOrZero(posReceipt.posReceiptCashs)
        if (prCashs > 0.0) {
            posReceiptValues.append("<tr>")
            posReceiptValues.append("<td>Cash</td> ")
            posReceiptValues.append("<td>${currency?.currencyCode2 ?: ""}</td>")
            posReceiptValues.append(
                "<td>${
                    String.format(
                        "%.2f",
                        prCashs
                    )
                }</td>"
            )
            posReceiptValues.append("</tr>")
        }

        val prCredit = Utils.getDoubleOrZero(posReceipt.posReceiptCredit)
        if (prCredit > 0.0) {
            posReceiptValues.append("<tr>")
            posReceiptValues.append("<td>Credit</td> ")
            posReceiptValues.append("<td>${currency?.currencyCode1 ?: ""}</td>")
            posReceiptValues.append(
                "<td>${
                    String.format(
                        "%.2f",
                        prCredit
                    )
                }</td>"
            )
            posReceiptValues.append("</tr>")
        }
        val prCredits = Utils.getDoubleOrZero(posReceipt.posReceiptCredits)
        if (prCredits > 0.0) {
            posReceiptValues.append("<tr>")
            posReceiptValues.append("<td>Credit</td> ")
            posReceiptValues.append("<td>${currency?.currencyCode2 ?: ""}</td>")
            posReceiptValues.append(
                "<td>${
                    String.format(
                        "%.2f",
                        prCredits
                    )
                }</td>"
            )
            posReceiptValues.append("</tr>")
        }

        val prDebit = Utils.getDoubleOrZero(posReceipt.posReceiptDebit)
        if (prDebit > 0.0) {
            posReceiptValues.append("<tr>")
            posReceiptValues.append("<td>Debit</td> ")
            posReceiptValues.append("<td>${currency?.currencyCode1 ?: ""}</td>")
            posReceiptValues.append(
                "<td>${
                    String.format(
                        "%.2f",
                        prDebit
                    )
                }</td>"
            )
            posReceiptValues.append("</tr>")
        }
        val prDebits = Utils.getDoubleOrZero(posReceipt.posReceiptDebits)
        if (prDebits > 0.0) {
            posReceiptValues.append("<tr>")
            posReceiptValues.append("<td>Debit</td> ")
            posReceiptValues.append("<td>${currency?.currencyCode2 ?: ""}</td>")
            posReceiptValues.append(
                "<td>${
                    String.format(
                        "%.2f",
                        prDebits
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


        result = if (!invoiceHeader.invoiceHeadNote.isNullOrEmpty()) {
            result.replace(
                "{invoicenotevalue}",
                "<hr class=\"dashed\">\n" + "    <div style=\"width: 100%;display: flex; align-items: start; justify-content: start; flex-direction: column;\">\n" + "        <div class=\"text1\">${invoiceHeader.invoiceHeadNote}</div>\n" + "    </div>"
            )
        } else {
            result.replace(
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

    fun testHtmlPrinting(): ByteArray {
        val htmlContent = """
        <div>
            <p><b>Bold Text</b></p>
            <p><i>Italic Text</i></p>
            <p><u>Underlined Text</u></p>
            <p><font size="2">Small Text</font></p>
            <p><font size="4">Large Text</font></p>
            <table>
                <tr>
                    <td>Item</td>
                    <td>Price</td>
                </tr>
                <tr>
                    <td>Apple</td>
                    <td>$1.00</td>
                </tr>
            </table>
        </div>
    """.trimIndent()

        return parseHtmlContent(htmlContent)
    }

    fun parseHtmlContent(htmlContent: String): ByteArray {
        // Parse HTML content
        val document = Jsoup.parse(htmlContent)
        val body = document.body()
        return parseHtmlElement(body)
    }

    private fun parseHtmlElement(element: Element): ByteArray {

        var result = byteArrayOf()

        val children = element.children()
        if(children.size>0) {
            for (child in children) {
                when (child.tagName()) {
                    "p" -> {
                        result += "\n".toByteArray()
                        result += parseHtmlElement(child)
                        result += "\n".toByteArray()
                    }

                    "b", "strong" -> {
                        result += BOLD
                        result += child.text().toByteArray()
                        result += NORMAL
                    }

                    "i", "em" -> {
                        result += ITALIC
                        result += child.text().toByteArray()
                        result += DISABLE_ITALIC
                    }

                    "u" -> {
                        result += byteArrayOf(
                            0x1B,
                            0x2D,
                            0x01
                        )//underline to be checked
                        result += child.text().toByteArray()
                        result += NORMAL
                    }

                    "font" -> {
                        val size = child.attr("size")
                        result += DOUBLE_SIZE//to be checked
                        result += child.text().toByteArray()
                        result += NORMAL_SIZE
                    }

                    "img" -> {
                        val src = child.attr("src")
                        result += IMAGE_PRINT_COMMAND // Replace with your printer's command
                        result += src.toByteArray()
                        result += IMAGE_END_COMMAND // Replace with your printer's command
                    }

                    "div" -> {
                        result += parseHtmlElement(child)
                        result += "\n".toByteArray()
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
                        result += child.text().toByteArray()
                    }
                }
            }
        }else{
            result += element.text().toByteArray()
        }

        return result
    }

    private fun printInvoiceReceipt(
        context: Context,
        invoiceHeader: InvoiceHeader,
        invoiceItemModels: MutableList<InvoiceItemModel>,
        posReceipt: PosReceipt,
        thirdParty: ThirdParty? = null,
        user: User? = SettingsModel.currentUser,
        company: Company? = SettingsModel.currentCompany,
        currency: Currency? = SettingsModel.currentCurrency,
    ): ByteArray {
        var result: ByteArray = byteArrayOf()
        val invDate = DateHelper.getDateFromString(
            invoiceHeader.invoiceHeadDate,
            "MMMM dd, yyyy 'at' hh:mm:ss a 'Z'"
        )
        result += ALIGN_CENTER
        if (!company?.companyLogo.isNullOrEmpty()) {
            val logoBitmap = FileUtils.getBitmapFromPath(
                context,
                Uri.parse(company?.companyLogo)
            )
            logoBitmap?.let {
                // Convert image data to byte array
                val imageData = convertToByteArray(it)

                // Send image data to printer with appropriate escape sequences
                result += IMAGE_PRINT_COMMAND // Replace with your printer's command
                result += imageData
                result += IMAGE_END_COMMAND // Replace with your printer's command

            }
        }

        if (!company?.companyName.isNullOrEmpty()) {
            result += DOUBLE_HEIGHT
            result += DOUBLE_WIDTH
            result += BOLD
            result += "${company?.companyName}\n".toByteArray()
            result += NORMAL
            result += NORMAL_SIZE
        }
        if (!company?.companyAddress.isNullOrEmpty()) {
            result += "${company?.companyAddress}\n".toByteArray()
        }
        if (!company?.companyPhone.isNullOrEmpty()) {
            result += "${company?.companyPhone}\n".toByteArray()
        }
        if (!invoiceHeader.invoiceHeadTransNo.isNullOrEmpty()) {
            result += "Invoice# ${invoiceHeader.invoiceHeadTransNo}\n".toByteArray()
        }

        val invDateStr = DateHelper.getDateInFormat(
            invDate,
            "dd/MM/yyyy hh:mm:ss"
        )
        result += ALIGN_CENTER
        result += "$invDateStr\n".toByteArray()

        result += ALIGN_LEFT
        if (!thirdParty?.thirdPartyName.isNullOrEmpty() || !invoiceHeader.invoiceHeadCashName.isNullOrEmpty()) {
            result += "Client: ${thirdParty?.thirdPartyName ?: ""} ${invoiceHeader.invoiceHeadCashName ?: ""}\n".toByteArray()
        }
        if (!thirdParty?.thirdPartyFn.isNullOrEmpty()) {
            result += "F/N: ${thirdParty?.thirdPartyFn ?: ""}\n".toByteArray()
        }
        if (!thirdParty?.thirdPartyPhone1.isNullOrEmpty() || !thirdParty?.thirdPartyPhone2.isNullOrEmpty()) {
            result += "Phone: ${thirdParty?.thirdPartyPhone1 ?: thirdParty?.thirdPartyPhone2}\n".toByteArray()
        }

        if (!thirdParty?.thirdPartyAddress.isNullOrEmpty()) {
            result += "Addr: ${thirdParty?.thirdPartyAddress}\n".toByteArray()
        }

        if (!user?.userName.isNullOrEmpty()) {
            result += "Served By: ${user?.userName}\n".toByteArray()
        }

        if (invoiceHeader.invoiceHeadPrint > 1) {
            result += ALIGN_CENTER
            result += BOLD
            result += DOUBLE_SIZE
            result += "* * REPRINTED * *\n".toByteArray()
            result += NORMAL_SIZE
            result += NORMAL
        }

        if (invoiceItemModels.isNotEmpty()) {
            result += ALIGN_CENTER
            result += "------------------------------\n".toByteArray()
            result += ALIGN_LEFT
            result += String.format("%-20s %10s\n", "Description", "T.Price").toByteArray()
            invoiceItemModels.forEach { item ->
                val qtyAndPrice = String.format(
                    "%.0f x %.2f",
                    item.invoice.invoiceQuantity,
                    item.invoice.getPrice()
                )
                result += String.format("%-32s\n", item.getFullName(), item.invoice.getAmount())
                    .toByteArray()
                result += String.format("%-17s%13.2f\n", qtyAndPrice, item.invoice.getAmount())
                    .toByteArray()
            }
        }

        result += ALIGN_CENTER
        result += "------------------------------\n".toByteArray()
        result += ALIGN_LEFT
        result += String.format(
            "Disc Amount: \t %.2f\n",
            Utils.getDoubleOrZero(company?.companyTax),
            Utils.getDoubleOrZero(invoiceHeader.invoiceHeadDiscountAmount)
        ).toByteArray()

        var showTotalTax = false
        if (SettingsModel.showTax && invoiceHeader.invoiceHeadTaxAmt > 0) {
            showTotalTax = true
            result += String.format(
                "Tax (%.0f%s: \t %.2f\n",
                Utils.getDoubleOrZero(company?.companyTax),
                "%)",
                Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTaxAmt)
            ).toByteArray()
        }

        if (SettingsModel.showTax1 && invoiceHeader.invoiceHeadTax1Amt > 0) {
            showTotalTax = true
            result += String.format(
                "Tax1(%.0f%s: \t %.2f\n",
                Utils.getDoubleOrZero(company?.companyTax1),
                "%)",
                Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTax1Amt)
            ).toByteArray()
        }

        if (SettingsModel.showTax2 && invoiceHeader.invoiceHeadTax2Amt > 0) {
            showTotalTax = true
            result += String.format(
                "Tax2(%.0f%s: \t %.2f\n",
                Utils.getDoubleOrZero(company?.companyTax2),
                "%)",
                Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTax2Amt)
            ).toByteArray()
        }
        if (showTotalTax) {
            result += String.format(
                "T.Tax:     \t %.2f\n",
                Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTotalTax)
            ).toByteArray()
        }

        result += BOLD
        result += String.format(
            "Total \t %s \t %.2f\n",
            currency?.currencyCode1 ?: "",
            Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTotal)
        ).toByteArray()
        result += String.format(
            "Total \t %s \t %.2f\n",
            currency?.currencyCode2 ?: "",
            Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTotal1)
        ).toByteArray()
        result += NORMAL

        result += (ALIGN_CENTER)
        result += ("------------------------------\n".toByteArray())
        result += (ALIGN_LEFT)

        result += ("Number Of Items: ${invoiceItemModels.size}\n".toByteArray())
        result += (ALIGN_CENTER)
        result += ("------------------------------\n".toByteArray())
        result += (ALIGN_LEFT)

        val prCash = Utils.getDoubleOrZero(posReceipt.posReceiptCash)
        if (prCash > 0.0) {
            result += (String.format(
                "Cash \t %s \t %.2f\n",
                currency?.currencyCode1 ?: "",
                prCash
            )).toByteArray()
        }

        val prCashs = Utils.getDoubleOrZero(posReceipt.posReceiptCashs)
        if (prCashs > 0.0) {
            result += (String.format(
                "Cash \t %s \t %.2f\n",
                currency?.currencyCode2 ?: "",
                prCashs
            )).toByteArray()
        }

        val prCredit = Utils.getDoubleOrZero(posReceipt.posReceiptCredit)
        if (prCredit > 0.0) {
            result += (String.format(
                "Credit \t %s \t %.2f\n",
                currency?.currencyCode1 ?: "",
                prCredit
            )).toByteArray()
        }
        val prCredits = Utils.getDoubleOrZero(posReceipt.posReceiptCredits)
        if (prCredits > 0.0) {
            result += (String.format(
                "Credit \t %s \t %.2f\n",
                currency?.currencyCode2 ?: "",
                prCredits
            )).toByteArray()
        }

        val prDebit = Utils.getDoubleOrZero(posReceipt.posReceiptDebit)
        if (prDebit > 0.0) {
            result += (String.format(
                "Debit \t %s \t %.2f\n",
                currency?.currencyCode1 ?: "",
                prDebit
            )).toByteArray()
        }
        val prDebits = Utils.getDoubleOrZero(posReceipt.posReceiptDebits)
        if (prDebits > 0.0) {
            result += (String.format(
                "Debit \t %s \t %.2f\n",
                currency?.currencyCode2 ?: "",
                prDebits
            )).toByteArray()
        }

        result += (String.format(
            "Change \t %s \t %.2f\n",
            currency?.currencyCode1 ?: "",
            Utils.getDoubleOrZero(invoiceHeader.invoiceHeadChange)
        )).toByteArray()

        result += (ALIGN_CENTER)
        result += ("------------------------------\n".toByteArray())
        result += (ALIGN_LEFT)


        if (!invoiceHeader.invoiceHeadNote.isNullOrEmpty()) {
            result += ("${invoiceHeader.invoiceHeadNote}\n".toByteArray())
            result += (ALIGN_CENTER)
            result += ("------------------------------\n".toByteArray())
            result += (ALIGN_LEFT)
        }

        var displayTaxDashed = false
        if (SettingsModel.showTax && !company?.companyTaxRegno.isNullOrEmpty()) {
            displayTaxDashed = true
            result += String.format("Tax \t No: \t %s\n", company?.companyTax1Regno).toByteArray()
        }
        if (SettingsModel.showTax1 && !company?.companyTax1Regno.isNullOrEmpty()) {
            displayTaxDashed = true
            result += String.format("Tax1 \t No: \t %s\n", company?.companyTax1Regno).toByteArray()
        }
        if (SettingsModel.showTax2 && !company?.companyTax2Regno.isNullOrEmpty()) {
            displayTaxDashed = true
            result += String.format("Tax2 \t No: \t %s\n", company?.companyTax1Regno).toByteArray()
        }
        if (displayTaxDashed) {
            result += (ALIGN_CENTER)
            result += ("------------------------------\n".toByteArray())
        }

        if (!invoiceHeader.invoiceHeadTransNo.isNullOrEmpty()) { //GS H = HRI position
            result += getBarcodeArray(invoiceHeader.invoiceHeadTransNo!!)
        }

        result += ("\n".toByteArray())
        result += (ALIGN_CENTER)
        result += ("THANK YOU\n".toByteArray())
        result += ("GRIDS Software - www.gridsco.com\n".toByteArray())
        result += ByteArray(3) { 0x0A }

        return result
    }

    private fun convertToByteArray(image: Bitmap): ByteArray {
        var byteArray = byteArrayOf()
        val width = image.width
        val height = image.height

        // Iterate through pixels and convert to byte array based on printer format
        for (y in 0 until height) {
            var byteData = 0
            for (x in 0 until width) {
                val pixel = image.getPixel(
                    x,
                    y
                )
                val isBlack = pixel != -1 // Adjust for your image format
                byteData = byteData shl 1
                if (isBlack) {
                    byteData = byteData or 1
                }
                if (x % 8 == 7) {
                    byteArray += byteData.toByte()
                    byteData = 0
                }
            }
            // Handle remaining bits
            if (width % 8 != 0) {
                byteArray += (byteData shl (8 - width % 8)).toByte()
            }
        }
        return byteArray
    }

    private fun printItemReceipt(
        invoiceHeader: InvoiceHeader,
        invItemModels: List<InvoiceItemModel>
    ): ByteArray {
        var result: ByteArray = byteArrayOf()
        result += (ALIGN_LEFT)
        result += ("Cash\n".toByteArray())
        result += ("Table Number: ${invoiceHeader.invoiceHeadTaName ?: ""}\n".toByteArray())
        result += ("Order: ${invoiceHeader.invoiceHeadOrderNo ?: ""}\n".toByteArray())
        result += ("Inv: ${invoiceHeader.invoiceHeadTransNo ?: ""}\n".toByteArray())
        result += (String.format(
            "%s\n", DateHelper.getDateInFormat(
                invoiceHeader.invoiceHeadTimeStamp ?: Date(
                    invoiceHeader.invoiceHeadDateTime.div(
                        1000
                    )
                ),
                "dd/MM/yyyy hh:mm:ss"
            )
        ).toByteArray())
        result += ("------------------------------\n".toByteArray())

        if (invItemModels.isNotEmpty()) {
            result += BOLD
            result += String.format("%-20s %10s\n", "Qty", "Item").toByteArray()
            result += NORMAL
            invItemModels.forEach { item ->
                result += String.format(
                    "%-25s %5.0f\n",
                    item.getName(),
                    item.invoice.invoiceQuantity
                ).toByteArray()
            }
        }
        result += ByteArray(3) { 0x0A }
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
           /* val htmlContent = getInvoiceReceiptHtmlContent(
                context = context,
                invoiceHeader = invoiceHeader,
                invoiceItemModels = invoiceItemModels,
                posReceipt = posReceipt,
                thirdParty = thirdParty,
                user = user,
                company = company
            )
            val output = parseHtmlContent(htmlContent)*/
            val output = printInvoiceReceipt(
                context = context,
                invoiceHeader = invoiceHeader,
                invoiceItemModels = invoiceItemModels,
                posReceipt = posReceipt,
                thirdParty = thirdParty,
                user = user,
                company = company
            )
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
                    val output = printItemReceipt(
                        invoiceHeader = invoiceHeader,
                        invItemModels = entry.value
                    )
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

    suspend fun printOutput(
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
                300,
                80
            )
            barcodeEncoder.createBitmap(bitMatrix)
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

    private fun getBarcodeArray(content: String): ByteArray {
        val result: ByteArray = content.toByteArray()
// include the content length after the mode selector (0x49)
// include the content length after the mode selector (0x49)
        val formats = byteArrayOf(
            0x1d.toByte(),
            0x6b.toByte(),
            0x49.toByte(),
            content.length.toByte()
        )

        val bytes = ByteArray(formats.size + result.size)

        System.arraycopy(
            formats,
            0,
            bytes,
            0,
            formats.size
        )
        System.arraycopy(
            result,
            0,
            bytes,
            formats.size,
            result.size
        )
        return result
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
            Base64.NO_WRAP
        )
    }
}