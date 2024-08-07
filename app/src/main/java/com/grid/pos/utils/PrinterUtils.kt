package com.grid.pos.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
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
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
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
            currency: Currency?=SettingsModel.currentCurrency,
            content: String = FileUtils.readFileFromAssets(
                "invoice_receipt.html",
                context
            )
    ): String {
        var result = content.ifEmpty { FileUtils.getDefaultReceipt() }
        val invDate = DateHelper.getDateFromString(invoiceHeader.invoiceHeadDate, "MMMM dd, yyyy 'at' hh:mm:ss a 'Z'")
        result = result.replace("{company_name}", company?.companyName ?: "")
            .replace("{company_addr}", company?.companyAddress ?: "")
            .replace("{company_phone}", company?.companyPhone ?: "")
            .replace("{invoicenumbervalue}", invoiceHeader.invoiceHeadTransNo ?: "")
            .replace("{invoicedatevalue}", DateHelper.getDateInFormat(invDate, "dd/MM/yyyy hh:mm:ss"))

        if(!company?.companyLogo.isNullOrEmpty()){
            val barcodeBitmap = FileUtils.getBitmapFromPath(context,Uri.parse(company?.companyLogo))
            val base64Barcode = convertBitmapToBase64(barcodeBitmap)
            result = result.replace("{company_logo}","<img src=\"data:image/png;base64,$base64Barcode\" width=\"50px\" height=\"50px\"/>")
        }else{
            result = result.replace("{company_logo}","")
        }


        result = result.replace("{clientnamevalue}", "${thirdParty?.thirdPartyName ?: ""} ${invoiceHeader.invoiceHeadCashName ?: ""}")
            .replace("{clientfnvalue}", thirdParty?.thirdPartyFn ?: "")
            .replace("{clientphonevalue}", thirdParty?.thirdPartyPhone1 ?: thirdParty?.thirdPartyPhone2 ?: "")
            .replace("{clientaddressvalue}", thirdParty?.thirdPartyAddress ?: "")
            .replace("{invoiceuservalue}", user?.userName ?: "")

        if (invoiceHeader.invoiceHeadPrint > 1) {
            result = result.replace(
                "{reprinted}",
                "<hr class=\"dashed\"> <div style=\"display: flex; align-items: center; justify-content: center;\">\n" + "            <div style=\"font-size: 30px; font-weight: bold;\"> * * REPRINTED * * </div>\n" + "        </div>"
            )
        }else{
            result = result.replace(
                "{reprinted}",
                ""
            )
        }
        if (invoiceItemModels.isNotEmpty()) {
            val trs = StringBuilder("<tr> <td>Description</td>  <td>Qty</td> <td>UP</td> <td>T.Price</td>  </tr>")
            invoiceItemModels.forEach { item ->
                trs.append("<tr>")
                trs.append("<td>${item.getFullName()}</td> ")
                trs.append("<td>${String.format("%.2f", item.getQuantity())}</td>")
                trs.append("<td>${String.format("%.2f", item.getPrice())}</td>")
                trs.append("<td>${String.format("%.2f", item.getNetAmount())}</td>")
                trs.append("</tr>")
            }
            result = result.replace("{tableinvoiceitemsvalue}", trs.toString())
                .replace("{numberofitemsvalue}", "${invoiceItemModels.size}")
        }
        val invAmountVal = StringBuilder("")
        invAmountVal.append("<tr>")
        invAmountVal.append("<td>Disc Amount:</td> ")
        invAmountVal.append("<td>${String.format("%.2f", Utils.getDoubleOrZero(invoiceHeader.invoiceHeadDiscountAmount))}</td>")
        invAmountVal.append("</tr>")

        if(SettingsModel.showTax) {
            invAmountVal.append("<tr>")
            invAmountVal.append("<td>Tax(${String.format("%.0f",Utils.getDoubleOrZero(company?.companyTax))}%):</td> ")
            invAmountVal.append("<td>${String.format("%.2f", Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTaxAmt))}</td>")
            invAmountVal.append("</tr>")
            result = result
                .replace("{taxregno}", "<div class=\"text1\">Tax No:${company?.companyTaxRegno ?: ""}</div>")
        }else{
            result = result.replace("{taxregno}", "")
        }
        if(SettingsModel.showTax1) {
            invAmountVal.append("<tr>")
            invAmountVal.append("<td>Tax1(${String.format("%.0f",Utils.getDoubleOrZero(company?.companyTax1))}%):</td> ")
            invAmountVal.append("<td>${String.format("%.2f", Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTax1Amt))}</td>")
            invAmountVal.append("</tr>")
            result = result
                .replace("{taxregno1}", "<div class=\"text1\">Tax1 No:${company?.companyTax1Regno ?: ""}</div>")
        }else{
            result = result.replace("{taxregno1}", "")
        }
        if(SettingsModel.showTax2) {
            invAmountVal.append("<tr>")
            invAmountVal.append("<td>Tax2(${String.format("%.0f",Utils.getDoubleOrZero(company?.companyTax2))}%):</td> ")
            invAmountVal.append("<td>${String.format("%.2f", Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTax2Amt))}</td>")
            invAmountVal.append("</tr>")
            result = result
                .replace("{taxregno2}", "<div class=\"text1\">Tax2 No:${company?.companyTax2Regno ?: ""}</div>")
        }else{
            result = result.replace("{taxregno2}", "")
        }
        if(SettingsModel.showTax2||SettingsModel.showTax2||SettingsModel.showTax2) {
            invAmountVal.append("<tr>")
            invAmountVal.append("<td>T.Tax:</td> ")
            invAmountVal.append("<td>${ String.format("%.2f", Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTotalTax))}</td>")
            invAmountVal.append("</tr>")
            result = result.replace("{taxdashed}", "<hr class=\"dashed\">")
        }else{
            result = result.replace("{taxdashed}", "")
        }

        invAmountVal.append("<tr>")
        invAmountVal.append("<td class=\"text2\">Total {${currency?.currencyCode1?:""}}:</td> ")
        invAmountVal.append("<td class=\"text2\">${String.format("%.2f", Utils.getDoubleOrZero(invoiceHeader.invoiceHeadGrossAmount))}</td>")
        invAmountVal.append("</tr>")

        invAmountVal.append("<tr>")
        invAmountVal.append("<td class=\"text2\">Total {${currency?.currencyCode2?:""}}:</td> ")
        invAmountVal.append("<td class=\"text2\">${String.format("%.2f", Utils.getDoubleOrZero(invoiceHeader.invoiceHeadGrossAmount) * (currency?.currencyRate?:1.0))}</td>")
        invAmountVal.append("</tr>")

        result = result.replace("{tableinvoiceAmountvalue}", invAmountVal.toString())
            .replace("{firstcurrencyvalue}", currency?.currencyCode1?:"")
            .replace("{secondcurrencyvalue}", currency?.currencyCode2?:"")

        result = result.replace("{pr_cash}", String.format("%.2f",Utils.getDoubleOrZero(posReceipt.posReceiptCash)))
            .replace("{pr_cashs}",  String.format("%.2f",Utils.getDoubleOrZero(posReceipt.posReceiptCashs)))
            .replace("{hi_change}",  String.format("%.2f",Utils.getDoubleOrZero(invoiceHeader.invoiceHeadChange)))
        if(!invoiceHeader.invoiceHeadNote.isNullOrEmpty()) {
            result = result.replace(
                "{invoicenotevalue}",
                "<hr class=\"dashed\">\n" + "    <div style=\"width: 100%;display: flex; align-items: start; justify-content: start; flex-direction: column;\">\n" + "        <div class=\"text1\">${invoiceHeader.invoiceHeadNote}</div>\n" + "    </div>"
            )
        }else{
            result = result.replace(
                "{invoicenotevalue}", ""
            )
        }

        if(!invoiceHeader.invoiceHeadTransNo.isNullOrEmpty()){
            val barcodeBitmap = generateBarcode(invoiceHeader.invoiceHeadTransNo!!)
            val base64Barcode = convertBitmapToBase64(barcodeBitmap)
            result = result.replace("{barcodeContent}"," <img class=\"barcode\" src=\"data:image/png;base64,$base64Barcode\" alt=\"Barcode\"/>")
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
        SettingsModel.cashPrinter?.let { companyPrinter ->
            val invoicePrinter = printers.firstOrNull { it.posPrinterId == companyPrinter }
            if (invoicePrinter != null) {
                val invoiceContent = getInvoiceReceiptHtmlContent(
                    context = context,
                    invoiceHeader,
                    invoiceItemModels,
                    posReceipt,
                    thirdParty,
                    user,
                    company
                )
                printInvoice(
                    context,
                    invoiceContent,
                    invoicePrinter.posPrinterHost,
                    invoicePrinter.posPrinterPort
                )
            }
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
                    printInvoice(
                        context,
                        invoiceContent,
                        itemsPrinter.posPrinterHost,
                        itemsPrinter.posPrinterPort
                    )
                }
            }
        }
    }

    fun printInvoice(
            context: Context,
            htmlContent: String,
            host: String = "192.168.1.222",
            port: Int = 9100
    ) {
        try {
            val pdfFile = FileUtils.getHtmlFile(
                context,
                htmlContent
            )
            Socket(
                host,
                port
            ).use { socket ->
                val outputStream: OutputStream = socket.getOutputStream()
                FileInputStream(pdfFile).use { fileInputStream ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int

                    // Read the PDF file and send its bytes to the printer
                    while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(
                            buffer,
                            0,
                            bytesRead
                        )
                    }

                    outputStream.flush()
                    socket.close()
                }/*PrintWriter(sock.getOutputStream(), true).use { printWriter ->
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

    private fun generateBarcode(data: String): Bitmap? {
        val barcodeEncoder = BarcodeEncoder()
        return try {
            val bitMatrix: BitMatrix = barcodeEncoder.encode(data, BarcodeFormat.CODE_128, 400, 150)
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
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}