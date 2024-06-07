package com.grid.pos.utils

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Log
import android.webkit.WebView
import androidx.lifecycle.viewModelScope
import com.grid.pos.App
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.PosPrinter.PosPrinter
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.SettingsModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.PrintWriter
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
                    }</td> <td>$${
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
            Utils.getDateinFormat(
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
            invoiceHeader: InvoiceHeader,
            invoiceItemModels: MutableList<InvoiceItemModel>,
            printers: MutableList<PosPrinter>
    ) {
        val context = App.getInstance().applicationContext
        SettingsModel.currentCompany?.companyPrinterId?.let { companyPrinter ->
            val invoicePrinter = printers.firstOrNull { it.posPrinterId == companyPrinter }
            if (invoicePrinter != null) {
                val invoiceContent = getInvoiceReceiptHtmlContent(
                    context = context,
                    invoiceHeader,
                    invoiceItemModels
                )
                printInvoice(
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
                        invoiceContent,
                        itemsPrinter.posPrinterHost,
                        itemsPrinter.posPrinterPort
                    )
                }
            }
        }
    }

    fun printInvoice(
            content: String,
            host: String = "192.168.1.222",
            port: Int = 9100
    ) {
        try {
            val sock = Socket(
                host,
                port
            )
            val oStream = PrintWriter(
                sock.getOutputStream(),
                true
            )
            oStream.print(content)
            oStream.println("\n\n\n")
            oStream.close()
            sock.close()
        } catch (e: Exception) {
            Log.e(
                "exception",
                e.message.toString()
            )
        }
    }
}