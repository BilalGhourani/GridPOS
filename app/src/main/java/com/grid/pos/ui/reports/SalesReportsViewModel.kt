package com.grid.pos.ui.reports

import android.net.Uri
import androidx.lifecycle.viewModelScope
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
import com.grid.pos.ui.common.BaseViewModel
import com.grid.pos.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbookType
import java.io.File
import java.util.Date
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class SalesReportsViewModel @Inject constructor(
        private val invoiceHeaderRepository: InvoiceHeaderRepository,
        private val invoiceRepository: InvoiceRepository,
        private val itemRepository: ItemRepository
) : BaseViewModel() {

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
            openConnectionIfNeeded()
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
            try {
                val listOfInvoices = invoiceHeaderRepository.getInvoicesBetween(
                    from,
                    to
                )
                invoices = listOfInvoices
                if (invoices.isNotEmpty()) {
                    fetchInvoiceItems(from,
                        to,
                        listOfInvoices.map { it.invoiceHeadId })
                } else {
                    withContext(Dispatchers.Main) {
                        reportsState.value = reportsState.value.copy(
                            warning = Event("no data found in the date range!"),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    reportsState.value = reportsState.value.copy(
                        warning = Event("an error has occurred!"),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun fetchInvoiceItems(
            from: Date,
            to: Date,
            ids: List<String>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val listOfInvoices = mutableListOf<Invoice>()
            val batchSize = 30
            var start = 0
            val size = ids.size
            while (start < size) {
                val to = min(
                    start + batchSize,
                    size
                )
                val idsBatch = ids.subList(
                    start,
                    to
                );
                listOfInvoices.addAll(invoiceRepository.getInvoicesByIds(idsBatch))
                start = to + 1;
            }
            invoiceItemMap = listOfInvoices.groupBy { it.invoiceItemId ?: "" }
            val filteredInvoiceItems = if (!SettingsModel.isConnectedToSqlite()) {
                listOfInvoices.filter { from.before(it.invoiceTimeStamp) && to.after(it.invoiceTimeStamp) }
            } else {
                val startTime = from.time
                val endTime = to.time
                listOfInvoices.filter { it.invoiceDateTime in (startTime + 1)..<endTime }
            }
            filteredInvoiceItemMap = filteredInvoiceItems.groupBy { it.invoiceItemId ?: "" }
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
            val workbook = XSSFWorkbook(XSSFWorkbookType.XLSX)

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
            reportFile = FileUtils.getFileFromUri(
                context,
                Uri.parse(path)
            )
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

    private fun generateFirstSheet(workbook: XSSFWorkbook) {
        val sheet = workbook.createSheet("Inventory & Profit Reports")

        // Obtaining Worksheet's cells collection
        val firstRow = sheet.createRow(0)
        firstRow.createCell(0).setCellValue("Item Name")
        firstRow.createCell(1).setCellValue("Open Qty")
        firstRow.createCell(2).setCellValue("Qty Sold")
        firstRow.createCell(3).setCellValue("Total Cost")
        firstRow.createCell(4).setCellValue("Total Sales")
        firstRow.createCell(5).setCellValue("Rem.Qty")
        firstRow.createCell(6).setCellValue("Profit")

        val priceWithTax = SettingsModel.currentCompany?.companyUpWithTax ?: false
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
                totalSale += if (priceWithTax) it.getAmount() else it.getNetAmount()
            }
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(item.itemName)
            row.createCell(1).setCellValue(item.itemOpenQty)
            row.createCell(2).setCellValue(quantitiesSold)
            row.createCell(3).setCellValue(
                String.format(
                    "%,.${currency.currencyName1Dec}f",
                    totalCost
                )
            )
            row.createCell(4).setCellValue(
                String.format(
                    "%,.${currency.currencyName1Dec}f",
                    totalSale
                )
            )
            row.createCell(5).setCellValue(remQty)
            row.createCell(6).setCellValue(
                String.format(
                    "%,.${currency.currencyName1Dec}f",
                    totalSale - totalCost
                )
            )
        }
    }

    private fun generateSecondSheet(workbook: XSSFWorkbook) {
        val sheet = workbook.createSheet("Sales Reports")

        // Obtaining Worksheet's cells collection
        val firstRow = sheet.createRow(0)
        firstRow.createCell(0).setCellValue("Name")
        firstRow.createCell(1).setCellValue("Qty Sold")
        firstRow.createCell(2).setCellValue("Total")

        val priceWithTax = SettingsModel.currentCompany?.companyUpWithTax ?: false
        filteredInvoiceItemMap.keys.forEachIndexed { index, itemId ->
            val item = itemMap[itemId]
            var quantitiesSold = 0.0
            var totalSale = 0.0
            filteredInvoiceItemMap[itemId]?.map {
                quantitiesSold += it.invoiceQuantity
                totalSale += if (priceWithTax) it.getAmount() else it.getNetAmount()
            }
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(item?.itemName ?: "N/A")
            row.createCell(1).setCellValue(quantitiesSold)
            row.createCell(2).setCellValue(totalSale)
        }
    }

}