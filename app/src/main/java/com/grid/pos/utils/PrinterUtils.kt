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
                            item.invoice.getNetAmount()
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

    private fun printInvoiceReceipt(
            context: Context,
            outputStream: OutputStream,
            invoiceHeader: InvoiceHeader,
            invoiceItemModels: MutableList<InvoiceItemModel>,
            posReceipt: PosReceipt,
            thirdParty: ThirdParty? = null,
            user: User? = SettingsModel.currentUser,
            company: Company? = SettingsModel.currentCompany,
            currency: Currency? = SettingsModel.currentCurrency,
    ) {
        Log.d(
            "printInvoiceReceipt",
            "1"
        )
        val invDate = DateHelper.getDateFromString(
            invoiceHeader.invoiceHeadDate,
            "MMMM dd, yyyy 'at' hh:mm:ss a 'Z'"
        )
        outputStream.write(ALIGN_CENTER)
        if (!company?.companyLogo.isNullOrEmpty()) {
            val logoBitmap = FileUtils.getBitmapFromPath(
                context,
                Uri.parse(company?.companyLogo)
            )
            logoBitmap?.let {
                // Convert image data to byte array
                val imageData = convertToByteArray(it)

                // Send image data to printer with appropriate escape sequences
                outputStream.write(IMAGE_PRINT_COMMAND) // Replace with your printer's command
                outputStream.write(imageData)
                outputStream.write(IMAGE_END_COMMAND) // Replace with your printer's command

            }
        }
        Log.d(
            "printInvoiceReceipt",
            "2"
        )
        if (!company?.companyName.isNullOrEmpty()) {
            outputStream.write(DOUBLE_SIZE)
            outputStream.write(BOLD)
            outputStream.write("${company?.companyName}\n".toByteArray())
            outputStream.write(NORMAL)
            outputStream.write(NORMAL_SIZE)
        }
        if (!company?.companyAddress.isNullOrEmpty()) {
            outputStream.write("${company?.companyAddress}\n".toByteArray())
        }
        if (!company?.companyPhone.isNullOrEmpty()) {
            outputStream.write("${company?.companyPhone}\n".toByteArray())
        }
        if (!invoiceHeader.invoiceHeadTransNo.isNullOrEmpty()) {
            outputStream.write("Invoice# ${invoiceHeader.invoiceHeadTransNo}\n".toByteArray())
        }

        val invDateStr = DateHelper.getDateInFormat(
            invDate,
            "dd/MM/yyyy hh:mm:ss"
        )
        outputStream.write(ALIGN_CENTER)
        outputStream.write("$invDateStr\n".toByteArray())
        Log.d(
            "printInvoiceReceipt",
            "3"
        )

        outputStream.write(ALIGN_LEFT)
        if (!thirdParty?.thirdPartyName.isNullOrEmpty() || !invoiceHeader.invoiceHeadCashName.isNullOrEmpty()) {
            outputStream.write("Client: ${thirdParty?.thirdPartyName ?: ""} ${invoiceHeader.invoiceHeadCashName ?: ""}\n".toByteArray())
        }
        if (!thirdParty?.thirdPartyFn.isNullOrEmpty()) {
            outputStream.write("F/N: ${thirdParty?.thirdPartyFn ?: ""}\n".toByteArray())
        }
        if (!thirdParty?.thirdPartyPhone1.isNullOrEmpty() || !thirdParty?.thirdPartyPhone2.isNullOrEmpty()) {
            outputStream.write("Phone: ${thirdParty?.thirdPartyPhone1 ?: thirdParty?.thirdPartyPhone2}\n".toByteArray())
        }

        if (!thirdParty?.thirdPartyAddress.isNullOrEmpty()) {
            outputStream.write("Addr: ${thirdParty?.thirdPartyAddress}\n".toByteArray())
        }

        if (!user?.userName.isNullOrEmpty()) {
            outputStream.write("Served By: ${user?.userName}\n".toByteArray())
        }

        Log.d(
            "printInvoiceReceipt",
            "4"
        )

        if (invoiceHeader.invoiceHeadPrint > 1) {
            outputStream.write(ALIGN_CENTER)
            outputStream.write(BOLD)
            outputStream.write(DOUBLE_SIZE)
            outputStream.write("* * REPRINTED * *\n".toByteArray())
            outputStream.write(NORMAL_SIZE)
            outputStream.write(NORMAL)
        }
        Log.d(
            "printInvoiceReceipt",
            "5"
        )
        if (invoiceItemModels.isNotEmpty()) {
            Log.d(
                "printInvoiceReceipt",
                "6"
            )
            outputStream.write(ALIGN_LEFT)
            outputStream.write(("Description\tQty\tUP\tT.Price\n").toByteArray())
            invoiceItemModels.forEach { item ->
                val name = item.getFullName()
                val qty = String.format(
                    "%.2f",
                    item.invoice.invoiceQuantity
                )
                val up = String.format(
                    "%.2f",
                    item.invoice.getPrice()
                )
                val price = String.format(
                    "%.2f",
                    item.invoice.getAmount()
                )
                outputStream.write(ALIGN_LEFT)
                outputStream.write(("$name\t$qty\t$up\t$price\n").toByteArray())
            }
        }
        Log.d(
            "printInvoiceReceipt",
            "7"
        )
        outputStream.write(ALIGN_CENTER)
        outputStream.write("------------------------------\n".toByteArray())
        outputStream.write(ALIGN_LEFT)
        outputStream.write(
            ("Disc Amount: \t" + String.format(
                "%.2f",
                Utils.getDoubleOrZero(invoiceHeader.invoiceHeadDiscountAmount)
            ) + "\n").toByteArray()
        )

        Log.d(
            "printInvoiceReceipt",
            "8"
        )
        var showTotalTax = false
        if (SettingsModel.showTax && invoiceHeader.invoiceHeadTaxAmt > 0) {
            showTotalTax = true
            outputStream.write(
                ("Tax(${
                    String.format(
                        "%.0f",
                        Utils.getDoubleOrZero(company?.companyTax)
                    )
                }%): \t ${
                    String.format(
                        "%.2f",
                        Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTaxAmt)
                    )
                } \n").toByteArray()
            )
        }

        if (SettingsModel.showTax1 && invoiceHeader.invoiceHeadTax1Amt > 0) {
            showTotalTax = true
            outputStream.write(
                ("Tax1(${
                    String.format(
                        "%.0f",
                        Utils.getDoubleOrZero(company?.companyTax1)
                    )
                }%): \t ${
                    String.format(
                        "%.2f",
                        Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTax1Amt)
                    )
                } \n").toByteArray()
            )
        }

        if (SettingsModel.showTax2 && invoiceHeader.invoiceHeadTax2Amt > 0) {
            showTotalTax = true
            outputStream.write(
                ("Tax2(${
                    String.format(
                        "%.0f",
                        Utils.getDoubleOrZero(company?.companyTax2)
                    )
                }%): \t ${
                    String.format(
                        "%.2f",
                        Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTax2Amt)
                    )
                } \n").toByteArray()
            )
        }
        if (showTotalTax) {
            outputStream.write(
                ("T.Tax: \t ${
                    String.format(
                        "%.2f",
                        Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTotalTax)
                    )
                } \n").toByteArray()
            )
        }
        Log.d(
            "printInvoiceReceipt",
            "9"
        )
        outputStream.write(BOLD)
        outputStream.write(
            ("Total ${currency?.currencyCode1 ?: ""}: \t ${
                String.format(
                    "%.2f",
                    Utils.getDoubleOrZero(invoiceHeader.invoiceHeadGrossAmount)
                )
            } \n").toByteArray()
        )

        outputStream.write(
            ("Total ${currency?.currencyCode2 ?: ""}: \t ${
                String.format(
                    "%.2f",
                    Utils.getDoubleOrZero(invoiceHeader.invoiceHeadGrossAmount) * (currency?.currencyRate ?: 1.0)
                )
            } \n").toByteArray()
        )
        Log.d(
            "printInvoiceReceipt",
            "10"
        )
        outputStream.write(NORMAL)
        outputStream.write(ALIGN_CENTER)
        outputStream.write("------------------------------\n".toByteArray())
        outputStream.write(ALIGN_LEFT)
        Log.d(
            "printInvoiceReceipt",
            "11"
        )
        outputStream.write("Number Of Items: ${invoiceItemModels.size}\n".toByteArray())
        outputStream.write(ALIGN_CENTER)
        outputStream.write("------------------------------\n".toByteArray())
        outputStream.write(ALIGN_LEFT)

        val prCash = Utils.getDoubleOrZero(posReceipt.posReceiptCash)
        if (prCash > 0.0) {
            outputStream.write(
                ("Cash \t ${currency?.currencyCode1 ?: ""} \t ${
                    String.format(
                        "%.2f",
                        prCash
                    )
                } \n").toByteArray()
            )
        }
        val prCashs = Utils.getDoubleOrZero(posReceipt.posReceiptCashs)
        if (prCashs > 0.0) {
            outputStream.write(
                ("Cash \t ${currency?.currencyCode2 ?: ""} \t ${
                    String.format(
                        "%.2f",
                        prCashs
                    )
                } \n").toByteArray()
            )
        }

        val prCredit = Utils.getDoubleOrZero(posReceipt.posReceiptCredit)
        if (prCredit > 0.0) {
            outputStream.write(
                ("Credit \t ${currency?.currencyCode1 ?: ""} \t ${
                    String.format(
                        "%.2f",
                        prCredit
                    )
                } \n").toByteArray()
            )
        }
        val prCredits = Utils.getDoubleOrZero(posReceipt.posReceiptCredits)
        if (prCredits > 0.0) {
            outputStream.write(
                ("Credit \t ${currency?.currencyCode2 ?: ""} \t ${
                    String.format(
                        "%.2f",
                        prCredits
                    )
                } \n").toByteArray()
            )
        }

        val prDebit = Utils.getDoubleOrZero(posReceipt.posReceiptDebit)
        if (prDebit > 0.0) {
            outputStream.write(
                ("Debit \t ${currency?.currencyCode1 ?: ""} \t ${
                    String.format(
                        "%.2f",
                        prDebit
                    )
                } \n").toByteArray()
            )
        }
        val prDebits = Utils.getDoubleOrZero(posReceipt.posReceiptDebits)
        if (prDebits > 0.0) {
            outputStream.write(
                ("Debit \t ${currency?.currencyCode2 ?: ""} \t ${
                    String.format(
                        "%.2f",
                        prDebits
                    )
                } \n").toByteArray()
            )
        }

        outputStream.write(
            ("Change \t ${
                String.format(
                    "%.2f",
                    Utils.getDoubleOrZero(invoiceHeader.invoiceHeadChange)
                )
            } \t ${currency?.currencyCode2 ?: ""} \n").toByteArray()
        )
        Log.d(
            "printInvoiceReceipt",
            "12"
        )
        outputStream.write(ALIGN_CENTER)
        outputStream.write("------------------------------\n".toByteArray())
        outputStream.write(ALIGN_LEFT)
        Log.d(
            "printInvoiceReceipt",
            "13"
        )

        if (!invoiceHeader.invoiceHeadNote.isNullOrEmpty()) {
            outputStream.write("${invoiceHeader.invoiceHeadNote}\n".toByteArray())
            outputStream.write(ALIGN_CENTER)
            outputStream.write("------------------------------\n".toByteArray())
            outputStream.write(ALIGN_LEFT)
        }
        Log.d(
            "printInvoiceReceipt",
            "14"
        )
        var displayTaxDashed = false
        if (SettingsModel.showTax && !company?.companyTaxRegno.isNullOrEmpty()) {
            displayTaxDashed = true
            outputStream.write(
                ("Tax \t No: \t ${company?.companyTax1Regno} \n").toByteArray()
            )
        }
        if (SettingsModel.showTax1 && !company?.companyTax1Regno.isNullOrEmpty()) {
            displayTaxDashed = true
            outputStream.write(
                ("Tax1 \t No: \t ${company?.companyTax1Regno} \n").toByteArray()
            )
        }
        if (SettingsModel.showTax2 && !company?.companyTax2Regno.isNullOrEmpty()) {
            displayTaxDashed = true
            outputStream.write(
                ("Tax2 \t No: \t ${company?.companyTax2Regno} \n").toByteArray()
            )
        }
        if (displayTaxDashed) {
            outputStream.write(ALIGN_CENTER)
            outputStream.write("------------------------------\n".toByteArray())
        }

        Log.d(
            "printInvoiceReceipt",
            "15"
        )
        /*if (!invoiceHeader.invoiceHeadTransNo.isNullOrEmpty()) {*//* //GS H = HRI position
            outputStream.write(0x1D);
            outputStream.write("H".toByteArray());
            outputStream.write(2); //0=no print, 1=above, 2=below, 3=above & below

            //GS f = set barcode characters
            outputStream.write(0x1D);
            outputStream.write("f".toByteArray());
            outputStream.write(font);

            //GS h = sets barcode height
            outputStream.write(0x1D);
            outputStream.write("h".toByteArray());
            outputStream.write(100);

            //GS w = sets barcode width
            outputStream.write(0x1D);
            outputStream.write("w".toByteArray());
            outputStream.write(1);//module = 1-6

            //GS k
            outputStream.write(0x1D); //GS
            outputStream.write("k".toByteArray()); //k
            outputStream.write(1);//m = barcode type 0-6
            outputStream.write(invoiceHeader.invoiceHeadTransNo?.length?:0); //length of encoded string
            outputStream.write(invoiceHeader.invoiceHeadTransNo?.toByteArray());//d1-dk
            outputStream.write(0);//print barcode*//*
            // Generate barcode image from barcode data and type
            val barcodeImage = generateBarcode(invoiceHeader.invoiceHeadTransNo!!)
            if (barcodeImage != null) {
                // Convert barcode image to byte array
                val barcodeByteArray = convertToByteArray(barcodeImage)

                // Send barcode data to printer with appropriate commands
                outputStream.write(IMAGE_PRINT_COMMAND)
                outputStream.write(barcodeByteArray)
                outputStream.write(IMAGE_END_COMMAND)
            }
        }*/
        Log.d(
            "printInvoiceReceipt",
            "16"
        )
        outputStream.write("THANK YOU\n".toByteArray())
        outputStream.write("GRIDS Software - www.gridsco.com\n".toByteArray())
        outputStream.write(CUT_PAPER)
        outputStream.flush()
        Log.d(
            "printInvoiceReceipt",
            "17"
        )
    }

    private fun convertToByteArray(image: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
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
                    outputStream.write(byteData)
                    byteData = 0
                }
            }
            // Handle remaining bits
            if (width % 8 != 0) {
                outputStream.write(byteData shl (8 - width % 8))
            }
        }
        outputStream.flush()
        return outputStream.toByteArray()
    }

    private fun printItemReceipt(
            outputStream: OutputStream,
            invoiceHeader: InvoiceHeader,
            invItemModels: List<InvoiceItemModel>
    ) {
        outputStream.write(ALIGN_LEFT)
        outputStream.write("Cash\n".toByteArray())
        outputStream.write("Table Number: ${invoiceHeader.invoiceHeadTaName ?: ""}\n".toByteArray())
        outputStream.write("Order: ${invoiceHeader.invoiceHeadOrderNo ?: ""}\n".toByteArray())
        outputStream.write("Inv: ${invoiceHeader.invoiceHeadTransNo ?: ""}\n".toByteArray())
        outputStream.write(
            "${
                DateHelper.getDateInFormat(
                    invoiceHeader.invoiceHeadTimeStamp ?: Date(
                        invoiceHeader.invoiceHeadDateTime.div(
                            1000
                        )
                    ),
                    "dd/MM/yyyy hh:mm:ss"
                )
            }\n".toByteArray()
        )


        if (invItemModels.isNotEmpty()) {
            outputStream.write("Qty\t Item\n".toByteArray())
            invItemModels.forEach { item ->
                val qty = String.format(
                    "%.2f",
                    item.invoice.invoiceQuantity
                )
                outputStream.write("$qty\t ${item.getName()}\n".toByteArray())
            }
        }
        outputStream.write(CUT_PAPER)
        outputStream.flush()
    }

    fun print(
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
            connectToPrinter(context = context,
                printerName = SettingsModel.cashPrinter,
                printRunnable = { outputStream ->
                    printInvoiceReceipt(
                        context = context,
                        outputStream = outputStream,
                        invoiceHeader = invoiceHeader,
                        invoiceItemModels = invoiceItemModels,
                        posReceipt = posReceipt,
                        thirdParty = thirdParty,
                        user = user,
                        company = company
                    )
                })
        }

        val itemsPrintersMap = invoiceItemModels.groupBy { it.invoiceItem.itemPrinter ?: "" }
        itemsPrintersMap.entries.forEach { entry ->
            if (entry.key.isNotEmpty()) {
                val itemsPrinter = printers.firstOrNull { it.posPrinterId == entry.key }
                if (itemsPrinter != null) {
                    connectToPrinter(context = context,
                        printerName = itemsPrinter.posPrinterName,
                        printerIP = itemsPrinter.posPrinterHost,
                        printerPort = itemsPrinter.posPrinterPort,
                        printRunnable = { outputStream ->
                            printItemReceipt(
                                outputStream = outputStream,
                                invoiceHeader = invoiceHeader,
                                invItemModels = entry.value
                            )
                        })
                }
            }
        }
    }

    private fun connectToPrinter(
            context: Context,
            printerName: String? = null,
            printerIP: String = "",
            printerPort: Int = -1,
            printRunnable: (OutputStream) -> Unit
    ) {
        val printer = BluetoothPrinter()
        if (!printerName.isNullOrEmpty() && printer.connectToPrinter(
                context,
                printerName
            )
        ) {
            if (printer.outputStream != null) {
                printRunnable.invoke(printer.outputStream!!)
            }
            printer.disconnectPrinter()
        } else if (printerIP.isNotEmpty() && printerPort != -1) {
            try {
                val socket = Socket(
                    printerIP,
                    printerPort
                )
                printRunnable.invoke(socket.getOutputStream())
                socket.close()
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