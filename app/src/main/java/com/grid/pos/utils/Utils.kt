package com.grid.pos.utils

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.print.PrintAttributes
import android.print.PrintAttributes.MediaSize
import android.print.PrintManager
import android.util.Log
import android.webkit.WebView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grid.pos.MainActivity
import com.grid.pos.data.DataModel
import com.grid.pos.model.CONNECTION_TYPE
import com.grid.pos.model.ConnectionModel
import com.grid.pos.model.HomeSectionModel
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.model.SettingsModel
import java.io.PrintWriter
import java.net.Socket
import java.text.SimpleDateFormat
import java.time.Year
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

object Utils {

    val connections = mutableListOf<DataModel>(
        ConnectionModel(CONNECTION_TYPE.LOCAL.key),
        ConnectionModel(CONNECTION_TYPE.FIRESTORE.key),
        ConnectionModel(CONNECTION_TYPE.SQL_SERVER.key)
    )

    fun getHomeList(): MutableList<HomeSectionModel> {
        val list = mutableListOf(
            HomeSectionModel(
                "Currency",
                "ManageCurrenciesView"
            ),
            HomeSectionModel(
                "Company",
                "ManageCompaniesView"
            ),
            HomeSectionModel(
                "User",
                "ManageUsersView"
            ),
            HomeSectionModel(
                "Third Party",
                "ManageThirdPartiesView"
            ),
            HomeSectionModel(
                "Family",
                "ManageFamiliesView"
            ),
            HomeSectionModel(
                "Item",
                "ManageItemsView"
            ),
            HomeSectionModel(
                "Printer",
                "POSPrinterView"
            ),
            HomeSectionModel(
                "Reports",
                "ReportsView"
            ),
            HomeSectionModel(
                "POS",
                "POSView"
            ),
            HomeSectionModel(
                "Table",
                "TablesView"
            ), HomeSectionModel(
                "Backup",
                "BackupView"
            )
        )
        return if (SettingsModel.isConnectedToSqlite()) {
            list
        } else {
            list.subList(0, list.size - 2)
        }
    }

    fun generateRandomUuidString(): String {
        return UUID.randomUUID().toString()
    }

    fun getDateinFormat(
        date: Date = Date(),
        format: String = "MMMM dd, yyyy 'at' hh:mm:ss a 'Z'"
    ): String {
        val parserFormat = SimpleDateFormat(
            format,
            Locale.getDefault()
        )
        parserFormat.timeZone = TimeZone.getTimeZone("UTC")
        return parserFormat.format(date)
    }

    fun editDate(
        date: Date = Date(),
        hours: Int = 23,
        minutes: Int = 59,
        seconds: Int = 59
    ): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date

        calendar.set(
            Calendar.HOUR_OF_DAY,
            hours
        )
        calendar.set(
            Calendar.MINUTE,
            minutes
        )
        calendar.set(
            Calendar.SECOND,
            seconds
        )
        return calendar.time
    }

    fun floatToColor(
        hue: Float,
        saturation: Float = 1f,
        brightness: Float = 1f
    ): Color {
        // Convert HSV to RGB
        val hsv = floatArrayOf(
            hue,
            saturation,
            brightness
        )
        return Color(android.graphics.Color.HSVToColor(hsv))
    }

    fun getDoubleValue(
        new: String,
        old: String
    ): String {
        return if (new.isEmpty()) {
            new
        } else {
            when (new.toDoubleOrNull()) {
                null -> old //old value
                else -> new   //new value
            }
        }
    }

    fun getIntValue(
        new: String,
        old: String
    ): String {
        return if (new.isEmpty()) {
            new
        } else {
            when (new.toIntOrNull()) {
                null -> old //old value
                else -> new  //new value
            }
        }
    }

    fun getItemsNumberStr(
        items: MutableList<InvoiceItemModel>
    ): String {
        val size = items.size
        return if (size <= 1) {
            "$size item"
        } else {
            "$size items"
        }
    }

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
                .setMediaSize(MediaSize.ISO_A4/*getMediaSize()*/)
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS).build()
            printManager.print(
                jobName,
                printAdapter,
                printAttributes
            )
        }
    }

    private fun getMediaSize(): MediaSize {
        // Define the width and height in inches
        val widthInches = 3 // Typical width for POS receipt paper
        val heightInches = 11 // You can adjust this based on your needs

        // Convert inches to micrometers (1 inch = 25400 micrometers)
        val widthMicrometers = widthInches * 25400
        val heightMicrometers = heightInches * 25400

        return MediaSize(
            "POS Receipt",
            "POS Receipt",
            widthMicrometers,
            heightMicrometers
        )
    }


    fun isTablet(configuration: Configuration): Boolean {
        return configuration.screenWidthDp > 840
    }

    fun getListHeight(
        listSize: Int = 0,
        cellHeight: Int,
        min: Int = 1,
        max: Int = 8
    ): Dp {
        var size = listSize
        if (size < min) size = min
        else if (size > max) size = max
        return (size * cellHeight).dp + 50.dp
    }

    fun getCurrentYear(): String {
        val calendar: Calendar = Calendar.getInstance()
        val currentYear = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Year.now().value
        } else {
            calendar[Calendar.YEAR]
        }
        return currentYear.toString()
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