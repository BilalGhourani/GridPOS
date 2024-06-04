package com.grid.pos.ui.reports

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aspose.cells.FileFormatType
import com.aspose.cells.Workbook
import com.grid.pos.App
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.Invoice.Invoice
import com.grid.pos.data.Invoice.InvoiceRepository
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.InvoiceHeader.InvoiceHeaderRepository
import com.grid.pos.data.Item.Item
import com.grid.pos.data.Item.ItemRepository
import com.grid.pos.model.Event
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.FileUtils
import com.grid.pos.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val invoiceHeaderRepository: InvoiceHeaderRepository,
    private val invoiceRepository: InvoiceRepository,
    private val itemRepository: ItemRepository
) : ViewModel() {

    private var itemMap: Map<String, Item> = mutableMapOf()
    private var invoices: MutableList<InvoiceHeader> = mutableListOf()
    private var invoiceItemMap: Map<String, List<Invoice>> = mutableMapOf()
    private var filteredInvoiceItemMap: Map<String, List<Invoice>> = mutableMapOf()
    private var currency = SettingsModel.currentCurrency ?: Currency()
    var reportFile: File? = null
    private val _reportsState = MutableStateFlow(ReportsState())
    val reportsState: MutableStateFlow<ReportsState> = _reportsState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            fetchItems()
        }
    }

    private suspend fun fetchItems() {
        val listOfItems = itemRepository.getAllItems()
        itemMap = listOfItems.map { it.itemId to it }.toMap()
    }

    fun fetchInvoices(
        from: Date,
        to: Date
    ) {
        reportsState.value = reportsState.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            val listOfInvoices = invoiceHeaderRepository.getAllInvoiceHeaders()
            invoices = listOfInvoices
            if (invoices.isNotEmpty()) {
                fetchInvoiceItems(
                    from,
                    to,
                    listOfInvoices.map { it.invoiceHeadId })
            } else {
                reportsState.value = reportsState.value.copy(
                    warning = Event("no data found in the date range!"),
                    isLoading = false
                )
            }
        }
    }

    fun fetchInvoiceItems(
        from: Date,
        to: Date,
        ids: List<String>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val listOfInvoices = invoiceRepository.getInvoicesByIds(ids)
            invoiceItemMap = listOfInvoices.groupBy { it.invoiceItemId ?: "" }
            val filteredInvoiceItems = if (!SettingsModel.isConnectedToSqlite()) {
                listOfInvoices.filter { from.before(it.invoiceTimeStamp) && to.after(it.invoiceTimeStamp) }
            } else {
                val startTime = from.time
                val endTime = to.time
                listOfInvoices.filter { it.invoiceDateTime in (startTime + 1)..<endTime }
            }
            filteredInvoiceItemMap =
                filteredInvoiceItems.groupBy { it.invoiceItemId ?: "" }
            generateReportsExcel()
        }
    }

    fun showError(message: String) {
        reportsState.value = reportsState.value.copy(
            warning = Event(message),
            isLoading = false
        )
    }

    fun generateReportsExcel() {
        viewModelScope.launch(Dispatchers.IO) {
            //val file = getFile()
            val workbook = Workbook()

            generateFirstSheet(workbook)
            generateSecondSheet(workbook)

            val context = App.getInstance().applicationContext
            // Write the workbook to a file
            val path = FileUtils.saveToExternalStorage(
                context = context,
                parent = "report",
                Uri.parse(""),
                "Sales_Report",
                "excel",
                workbook
            )
            /*val outputStream = FileOutputStream(file)
            workbook.save(
                outputStream,
                FileFormatType.XLSX
            )*/
            reportFile = FileUtils.getFileFromUri(context, Uri.parse(path))
            withContext(Dispatchers.Main) {
                reportsState.value = reportsState.value.copy(
                    isDone = true,
                    isLoading = false
                )
            }
        }
    }

    private fun getFile(): File {
        val context = App.getInstance().applicationContext
        val storageDir = File(
            context.filesDir,
            "reports"
        )
        if (!storageDir.exists()) {
            storageDir.mkdir()
        }
        val child = File(
            storageDir,
            "Sales_Report.xlsx"
        )
        if (!child.exists()) {
            // child.delete()
            child.createNewFile()
        }
        return child
    }

    private fun generateFirstSheet(workbook: Workbook) {
        val sheet = workbook.worksheets[0]
        sheet.name = "Inventory & Profit Reports"

        // Obtaining Worksheet's cells collection
        val cells = sheet.cells
        cells.get(0, 0).value = "Item Name"
        cells.get(0, 1).value = "Open Qty"
        cells.get(0, 2).value = "Qty Sold"
        cells.get(0, 3).value = "Total Cost"
        cells.get(0, 4).value = "Total Sales"
        cells.get(0, 5).value = "Rem.Qty"
        cells.get(0, 6).value = "Profit"


        itemMap.values.forEachIndexed { index, item ->
            val itemInvoices = invoiceItemMap[item.itemId]
            var quantitiesSold = 0.0
            var totalCost = 0.0
            var totalSale = 0.0
            var remQty = -1.0
            itemInvoices?.map {
                if (remQty < 0) {//to take the last invoice remQty
                    remQty = it.invoiceRemQty
                }
                totalCost += it.invoiceQuantity.times(it.invoiceCost)
                quantitiesSold += it.invoiceQuantity
                totalSale += it.getNetAmount()
            }
            cells.get(index + 1, 0).value = item.itemName
            cells.get(index + 1, 1).value = item.itemOpenQty
            cells.get(index + 1, 2).value = quantitiesSold
            cells.get(index + 1, 3).value = String.format(
                "%.${currency.currencyName1Dec}f",
                totalCost
            )

            cells.get(index + 1, 4).value = String.format(
                "%.${currency.currencyName1Dec}f",
                totalSale
            )

            cells.get(index + 1, 5).value = remQty
            cells.get(index + 1, 6).value = String.format(
                "%.${currency.currencyName1Dec}f",
                totalSale - totalCost
            )
        }
    }

    private fun generateSecondSheet(workbook: Workbook) {
        val sheet = workbook.worksheets.add("Sales Reports")

        // Obtaining Worksheet's cells collection
        val cells = sheet.cells
        sheet.cells.get(0, 0).value = "Name"
        sheet.cells.get(0, 1).value = "Qty Sold"
        sheet.cells.get(0, 2).value = "Total"

        filteredInvoiceItemMap.keys.forEachIndexed { index, itemId ->
            val item = itemMap[itemId]
            var quantitiesSold = 0.0
            var totalSale = 0.0
            filteredInvoiceItemMap[itemId]?.map {
                quantitiesSold += it.invoiceQuantity
                totalSale += it.getNetAmount()
            }
            cells.get(index + 1, 0).value = item?.itemName ?: "N/A"
            cells.get(index + 1, 1).value = quantitiesSold
            cells.get(index + 1, 2).value = totalSale
        }
    }

}