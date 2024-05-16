package com.grid.pos.utils

import android.content.Context
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
import com.grid.pos.data.DataModel
import com.grid.pos.data.Family.Family
import com.grid.pos.data.Item.Item
import com.grid.pos.data.User.User
import com.grid.pos.model.HomeSectionModel
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.time.Year
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

object Utils {

    val homeSections = mutableListOf(
        HomeSectionModel(
            "Currency", "ManageCurrenciesView"
        ),
        HomeSectionModel(
            "Company", "ManageCompaniesView"
        ),
        HomeSectionModel(
            "User", "ManageUsersView"
        ),
        HomeSectionModel(
            "Third Party", "ManageThirdPartiesView"
        ),
        HomeSectionModel(
            "Family", "ManageFamiliesView"
        ),
        HomeSectionModel(
            "Item", "ManageItemsView"
        ),
        HomeSectionModel(
            "POS", "PosView"
        ),
        HomeSectionModel(
            "Table", "TablesView"
        ),
    )

    val users = mutableListOf(
        User(
            userId = "1", userName = "Bilal", userPassword = "123456"
        ) as DataModel,
        User(
            userId = "1", userName = "Ziad", userPassword = "133442"
        ) as DataModel,
        User(
            userId = "1", userName = "Zakariya", userPassword = "123432"
        ) as DataModel,
        User(
            userId = "1", userName = "Mohammad", userPassword = "432785"
        ) as DataModel,
        User(
            userId = "1", userName = "Ahmad", userPassword = "009988"
        ) as DataModel,
        User(
            userId = "1", userName = "Samir", userPassword = "225577"
        ) as DataModel,
        User(
            userId = "1", userName = "Omar", userPassword = "113311"
        ) as DataModel,
        User(
            userId = "1", userName = "Abed Al Rahman", userPassword = "112345"
        ) as DataModel,
        User(
            userId = "1", userName = "Abdullah", userPassword = "998888"
        ) as DataModel,
    )

    val categories = mutableListOf(
        Family(
            "1", "Chicken"
        ), Family(
            "2", "Meat"
        ), Family(
            "3", "Salad"
        ), Family(
            "4", "Veg"
        ), Family(
            "5", "Other"
        )
    )

    val listOfItems = mutableListOf(
        Item(
            itemId = "1", itemName = "Chicken", itemUnitPrice = 100.0
        ),
        Item(
            itemId = "2", itemName = "Salad", itemUnitPrice = 100.0
        ),
        Item(
            itemId = "3", itemName = "Veg", itemUnitPrice = 100.0
        ),
        Item(
            itemId = "4", itemName = "Other", itemUnitPrice = 100.0
        ),
        Item(
            itemId = "5", itemName = "Other1", itemUnitPrice = 100.0
        ),
        Item(
            itemId = "6", itemName = "Other2", itemUnitPrice = 100.0
        ),
        Item(
            itemId = "7", itemName = "Other3", itemUnitPrice = 100.0
        ),
    )

    fun calculateColumns(
            cellWidth: Dp,
            screenWidth: Dp
    ): Int {
        val availableSpace = screenWidth - Dp((2 * 16F))// Account for paddings (adjust as needed)
        return (availableSpace / cellWidth).toInt().coerceAtLeast(1) // Ensure at least 1 column
    }

    fun generateRandomUuidString(): String {
        return UUID.randomUUID().toString()
    }

    fun getDateinFormat(
            date: Date = Date(),
            format: String = "MMMM dd, yyyy 'at' hh:mm:ss a 'Z'"
    ): String {
        val parserFormat = SimpleDateFormat(
            format, Locale.getDefault()
        )
        parserFormat.timeZone = TimeZone.getTimeZone("UTC")
        return parserFormat.format(date)
    }

    fun floatToColor(
            hue: Float,
            saturation: Float = 1f,
            brightness: Float = 1f
    ): Color {
        // Convert HSV to RGB
        val hsv = floatArrayOf(
            hue, saturation, brightness
        )
        return Color(android.graphics.Color.HSVToColor(hsv))
    }

    fun generateNameFromUsername(username: String): String {
        return username.replace(
            "_", " "
        )
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

    fun printWebPage(
            webView: WebView?,
            context: Context
    ) {
        if (webView != null) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val jobName = "webpage_" + System.currentTimeMillis()
            val printAdapter = webView.createPrintDocumentAdapter(jobName)

            // Define Print Attributes (optional)
            val printAttributes = PrintAttributes.Builder().setMediaSize(
                getMediaSize()
            ).setMinMargins(PrintAttributes.Margins.NO_MARGINS).build()

            printManager.print(
                jobName, printAdapter, printAttributes
            )
        }
    }

    fun getMediaSize(): MediaSize {
        if (true) {
            return PrintAttributes.MediaSize.ISO_A4
        }
        // Define the width and height in inches
        val widthInches = 3 // Typical width for POS receipt paper
        val heightInches = 11 // You can adjust this based on your needs

        // Convert inches to micrometers (1 inch = 25400 micrometers)
        val widthMicrometers = widthInches * 25400
        val heightMicrometers = heightInches * 25400

        return MediaSize(
            "POS Receipt", "POS Receipt", widthMicrometers, heightMicrometers
        )
    }

    fun readFileFromAssets(
            fileName: String,
            context: Context
    ): String {
        return try {
            val inputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            stringBuilder.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun getDefaultReceipt(): String {
        return "<!DOCTYPE html>\n" + "<html lang=\"en\">\n" + "<head>\n" + "    <meta charset=\"UTF-8\">\n" + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" + "    <title>Receipt</title>\n" + "    <style>\n" + "        body {\n" + "            font-family: Arial, sans-serif;\n" + "            margin: 0;\n" + "            padding: 20px;\n" + "        }\n" + "        .container {\n" + "            max-width: 400px;\n" + "            margin: 0 auto;\n" + "            border: 1px solid #ccc;\n" + "            padding: 20px;\n" + "            border-radius: 5px;\n" + "        }\n" + "        .receipt-header {\n" + "            text-align: center;\n" + "            margin-bottom: 20px;\n" + "        }\n" + "        .receipt-items {\n" + "            border-collapse: collapse;\n" + "            width: 100%;\n" + "        }\n" + "        .receipt-items th, .receipt-items td {\n" + "            border: 1px solid #ddd;\n" + "            padding: 8px;\n" + "            text-align: left;\n" + "        }\n" + "        .receipt-items th {\n" + "            background-color: #f2f2f2;\n" + "        }\n" + "        .total {\n" + "            margin-top: 20px;\n" + "            text-align: right;\n" + "        }\n" + "    </style>\n" + "</head>\n" + "<body>\n" + "<div class=\"container\">\n" + "    <div class=\"receipt-header\">\n" + "        <h2>Receipt</h2>\n" + "    </div>\n" + "    <table class=\"receipt-items\">\n" + "        <thead>\n" + "        <tr>\n" + "            <th>Item</th>\n" + "            <th>Quantity</th>\n" + "            <th>Price</th>\n" + "        </tr>\n" + "        </thead>\n" + "        <tbody>\n" + "        {rows_content}\n" + "        </tbody>\n" + "    </table>\n" + "    <div class=\"total\">\n" + "        <strong>Total: {total}</strong>\n" + "    </div>\n" + "</div>\n" + "</body>\n" + "</html>"
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

    fun getInvoiceTransactionNo(oldInvoiceTransNo: String?): String {
        val currentYear = getCurrentYear()
        var invNoStr = oldInvoiceTransNo.takeIf { !it.isNullOrEmpty() } ?: (currentYear + "000000000")
        if (invNoStr.length > 4 && !invNoStr.substring(
                0, 4
            ).equals(
                currentYear, ignoreCase = true
            )
        ) {
            invNoStr = currentYear + "000000000"
        }
        return (invNoStr.toBigInteger().plus(BigInteger("1"))).toString()
    }

    fun getInvoiceNo(oldInvoiceNo: String?): String {
        val currentYear = getCurrentYear()
        val sections = if (oldInvoiceNo.isNullOrEmpty()) listOf(
            currentYear, "0"
        ) else oldInvoiceNo.split("-")
        var invYearStr = if (sections.isNotEmpty()) sections[0] else currentYear
        val serialNo = if (sections.size > 1) sections[1] else "0"
        if (!invYearStr.equals(currentYear, ignoreCase = true)) {
            invYearStr = currentYear
        }
        val serialInt = (serialNo.toIntOrNull() ?: 1) + 1
        return "$invYearStr-${serialInt}"
    }

    private fun getCurrentYear(): String {
        val calendar: Calendar = Calendar.getInstance()
        val currentYear = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Year.now().value
        } else {
            calendar[Calendar.YEAR]
        }
        return currentYear.toString()
    }

    fun saveToInternalStorage(
            context: Context,
            parent: String = "family",
            sourceFile: Uri,
            destName: String
    ): String? {
        val storageDir = File(context.filesDir, "images")
        if (!storageDir.exists()) {
            storageDir.mkdir()
        }
        val parentDir = File(storageDir, parent)
        if (!parentDir.exists()) {
            parentDir.mkdir()
        }
        val name = "$destName.jpg"
        val destinationFile = File(parentDir, name)

        copyImage(context, sourceFile, destinationFile)
        return destinationFile.absolutePath
    }

    fun copyImage(
            context: Context,
            sourceFilePath: Uri,
            destinationFile: File
    ) {
        val contentResolver = context.contentResolver
        try {
            val sourceFile = File(sourceFilePath.toString())
            val inputStream: InputStream = if (!sourceFile.exists()) {
                // Opening from gallery using content URI
                contentResolver.openInputStream(sourceFilePath)!!
            } else {
                // Opening from internal storage using path
                FileInputStream(sourceFile)
            }
            val outputStream = destinationFile.outputStream()
            val buffer = ByteArray(1024) // Adjust buffer size as needed
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } > 0) {
                outputStream.write(buffer, 0, bytesRead)
            }
            inputStream.close()
            outputStream.close()
        } catch (e: IOException) {
            Log.e("tag", "Failed to copy image", e)
        }
    }

}