package com.grid.pos.utils

import android.content.Context
import android.graphics.Bitmap
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
            result = result.replace(
                "{tableinvoiceitemsvalue}",
                trs.toString()
            )
        }
        result = result
            .replace("{invoicediscamtvalue}", String.format("%.2f", Utils.getDoubleOrZero(invoiceHeader.invoiceHeadDiscountAmount)))
            .replace("{vatcompanyvalue}", String.format("%.0f",Utils.getDoubleOrZero(company?.companyTax)))
            .replace("{invoicetaxamtvalue}", String.format("%.2f", Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTaxAmt)))
            .replace("{tax1companyvalue}", String.format("%.0f",Utils.getDoubleOrZero(company?.companyTax1)))
            .replace("{invoicetax1amtvalue}", String.format("%.2f", Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTax1Amt)))
            .replace("{tax2companyvalue}", String.format("%.0f",Utils.getDoubleOrZero(company?.companyTax2)))
            .replace("{invoicetax2amtvalue}", String.format("%.2f", Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTax2Amt)))
            .replace("{invoicevatamtvalue}", String.format("%.2f", Utils.getDoubleOrZero(invoiceHeader.invoiceHeadTotalTax)))
            .replace("{firstcurrencyvalue}", currency?.currencyName1?:"")
            .replace("{invoicetotalvalue}", String.format("%.2f", Utils.getDoubleOrZero(invoiceHeader.invoiceHeadGrossAmount)))
            .replace("{secondcurrencyvalue}", currency?.currencyName2?:"")
            .replace("{invoicetotal1value}", String.format("%.2f", Utils.getDoubleOrZero(invoiceHeader.invoiceHeadGrossAmount) * (currency?.currencyRate?:1.0)))

        result = result.replace("{pr_cash}", String.format("%.2f",Utils.getDoubleOrZero(posReceipt.posReceiptCash)))
            .replace("{pr_cashs}",  String.format("%.2f",Utils.getDoubleOrZero(posReceipt.posReceiptCashs)))
            .replace("{hi_change}",  String.format("%.2f",Utils.getDoubleOrZero(invoiceHeader.invoiceHeadChange)))
            .replace("{taxregno}",  company?.companyTaxRegno?:"")
            .replace("{taxregno1}",  company?.companyTax1Regno?:"")
            .replace("{taxregno2}",  company?.companyTax2Regno?:"")

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