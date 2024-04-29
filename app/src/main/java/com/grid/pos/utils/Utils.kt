package com.grid.pos.utils

import android.content.Context
import android.content.res.Configuration
import android.print.PrintAttributes
import android.print.PrintAttributes.MediaSize
import android.print.PrintManager
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import com.grid.pos.App
import com.grid.pos.data.DataModel
import com.grid.pos.data.Family.Family
import com.grid.pos.data.Invoice.Invoice
import com.grid.pos.data.Item.Item
import com.grid.pos.data.User.User
import com.grid.pos.model.HomeSectionModel
import com.grid.pos.model.InvoiceItemModel
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID

object Utils {

    val homeSections = mutableListOf(
        HomeSectionModel("Currency", "ManageCurrenciesView"),
        HomeSectionModel("Company", "ManageCompaniesView"),
        HomeSectionModel("User", "ManageUsersView"),
        HomeSectionModel("Third Party", "ManageThirdPartiesView"),
        HomeSectionModel("Family", "ManageFamiliesView"),
        HomeSectionModel("Item", "ManageItemsView"),
        HomeSectionModel("POS", "PosView"),
        HomeSectionModel("Table", "ManageTablesView"),
    )

    val users = mutableListOf(
        User(userId = "1", userName = "Bilal", userPassword = "123456") as DataModel,
        User(userId = "1", userName = "Ziad", userPassword = "133442") as DataModel,
        User(userId = "1", userName = "Zakariya", userPassword = "123432") as DataModel,
        User(userId = "1", userName = "Mohammad", userPassword = "432785") as DataModel,
        User(userId = "1", userName = "Ahmad", userPassword = "009988") as DataModel,
        User(userId = "1", userName = "Samir", userPassword = "225577") as DataModel,
        User(userId = "1", userName = "Omar", userPassword = "113311") as DataModel,
        User(userId = "1", userName = "Abed Al Rahman", userPassword = "112345") as DataModel,
        User(userId = "1", userName = "Abdullah", userPassword = "998888") as DataModel,
    )

    val categories = mutableListOf(
        Family("1", "Chicken"),
        Family("2", "Meat"),
        Family("3", "Salad"),
        Family("4", "Veg"),
        Family("5", "Other")
    )

    val listOfItems = mutableListOf(
        Item(itemId = "1", itemName = "Chicken", itemUnitPrice = "100.0"),
        Item(itemId = "2", itemName = "Salad", itemUnitPrice = "100.0"),
        Item(itemId = "3", itemName = "Veg", itemUnitPrice = "100.0"),
        Item(itemId = "4", itemName = "Other", itemUnitPrice = "100.0"),
        Item(itemId = "5", itemName = "Other1", itemUnitPrice = "100.0"),
        Item(itemId = "6", itemName = "Other2", itemUnitPrice = "100.0"),
        Item(itemId = "7", itemName = "Other3", itemUnitPrice = "100.0"),
    )

    fun calculateColumns(cellWidth: Dp, screenWidth: Dp): Int {
        val availableSpace = screenWidth - Dp((2 * 16F))// Account for paddings (adjust as needed)
        return (availableSpace / cellWidth).toInt().coerceAtLeast(1) // Ensure at least 1 column
    }

    fun generateRandomUuidString(): String {
        return UUID.randomUUID().toString()
    }

    fun generateNameFromUsername(username: String): String {
        return username.replace("_", " ")
    }


    fun printWebPage(webView: WebView?, context: Context) {
        if (webView != null) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val jobName = "webpage_" + System.currentTimeMillis()
            val printAdapter = webView.createPrintDocumentAdapter(jobName)

            // Define Print Attributes (optional)
            val printAttributes = PrintAttributes.Builder()
                .setMediaSize(getMediaSize())
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build()

            printManager.print(jobName, printAdapter, printAttributes)
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

        return MediaSize("POS Receipt", "POS Receipt", widthMicrometers, heightMicrometers)
    }

    fun readHtmlFromAssets(fileName: String, context: Context): String {
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

    fun modifyHtmlContent(htmlContent: String): String {
        // Modify HTML content here as needed
        return htmlContent.replace("original_value", "new_value")
    }

    fun isTablet(configuration: Configuration): Boolean {
        return configuration.screenWidthDp > 840
    }

    fun getInvoiceModelFromList(invoices: MutableList<Invoice>): MutableList<InvoiceItemModel> {
        val invoiceItemModels: MutableList<InvoiceItemModel> = mutableListOf()
        invoices.forEach {
            val price = it.invoicePrice ?: 0.0
            val quantity = it.invoiceQuantity ?: 0.0
            val model = InvoiceItemModel(
                it.invoicExtraName ?: "",
                quantity.toString(),
                price.toString(),
                it.invoiceDiscount.toString(),
                it.invoiceTax.toString(),
                it.invoiceTax1.toString(),
                it.invoiceTax2.toString(),
                (price * quantity).toString()
            )
            invoiceItemModels.add(model)
        }
        return invoiceItemModels
    }

    fun getInvoiceFromItem(item: Item): Invoice {
        val invoice = Invoice()
        invoice.invoiceId = generateRandomUuidString()
        invoice.invoiceQuantity = 1.0
        invoice.invoiceItemId = item.itemId
        invoice.invoicCost = item.itemOpenCost?.toDouble() ?: 0.0
        invoice.invoicRemQty = item.itemOpenQty?.toDouble() ?: 0.0
        invoice.invoiceTax = item.itemTax?.toDouble() ?: 0.0
        invoice.invoiceTax1 = item.itemTax1?.toDouble() ?: 0.0
        invoice.invoiceTax2 = item.itemTax2?.toDouble() ?: 0.0
        return invoice
    }
}