package com.grid.pos.utils

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Log
import android.webkit.WebView
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.PosPrinter.PosPrinter
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.SettingsModel
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
        content: String = FileUtils.readFileFromAssets(
            "invoice_receipt.html",
            context
        )
    ): String {
        var result = content.ifEmpty { FileUtils.getDefaultReceipt() }
        if (invoiceItemModels.isNotEmpty()) {
            val trs = StringBuilder("")
            invoiceItemModels.forEach { item ->
                trs.append(
                    "<tr> <td>${item.getName()}</td>  <td>${
                        String.format(
                            "%.2f",
                            item.getQuantity()
                        )
                    }</td> <td>${
                        String.format(
                            "%.2f",
                            item.getNetAmount()
                        )
                    }</td>  </tr>"
                )
            }
            result = result.replace(
                "{rows_content}",
                trs.toString()
            )
            result = result.replace(
                "{total}",
                String.format(
                    "%.2f",
                    invoiceHeader.invoiceHeadGrossAmount
                )
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

    fun print(
        context:Context,
        invoiceHeader: InvoiceHeader,
        invoiceItemModels: MutableList<InvoiceItemModel>,
        printers: MutableList<PosPrinter>
    ) {
        SettingsModel.cashPrinter?.let { companyPrinter ->
            val invoicePrinter = printers.firstOrNull { it.posPrinterId == companyPrinter }
            if (invoicePrinter != null) {
                val invoiceContent = getInvoiceReceiptHtmlContent(
                    context = context,
                    invoiceHeader,
                    invoiceItemModels
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
            val pdfFile = FileUtils.getHtmlFile(context, htmlContent)
            Socket(host, port).use { socket ->
                val outputStream: OutputStream = socket.getOutputStream()
                FileInputStream(pdfFile).use { fileInputStream ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int

                    // Read the PDF file and send its bytes to the printer
                    while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }

                    outputStream.flush()
                    socket.close()
                }
                /*PrintWriter(sock.getOutputStream(), true).use { printWriter ->
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