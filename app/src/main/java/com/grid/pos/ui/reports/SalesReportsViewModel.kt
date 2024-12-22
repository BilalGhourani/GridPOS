package com.grid.pos.ui.reports

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.grid.pos.App
import com.grid.pos.data.currency.Currency
import com.grid.pos.data.currency.CurrencyRepository
import com.grid.pos.data.invoice.Invoice
import com.grid.pos.data.invoice.InvoiceRepository
import com.grid.pos.data.invoiceHeader.InvoiceHeader
import com.grid.pos.data.invoiceHeader.InvoiceHeaderRepository
import com.grid.pos.data.item.Item
import com.grid.pos.data.item.ItemRepository
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
        private val currencyRepository: CurrencyRepository,
        private val itemRepository: ItemRepository
) : BaseViewModel() {

    private var itemMap: Map<String, Item> = mutableMapOf()
    private var invoicesMap: Map<String, InvoiceHeader> = mutableMapOf()
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
        if (SettingsModel.currentCurrency == null && SettingsModel.isConnectedToSqlServer()) {
            val dataModel = currencyRepository.getAllCurrencies()
            if (dataModel.succeed) {
                val currencies = convertToMutableList(
                    dataModel.data,
                    Currency::class.java
                )
                val currency = if (currencies.size > 0) currencies[0] else Currency()
                SettingsModel.currentCurrency = currency
            } else if (dataModel.message != null) {
                showError(dataModel.message)
            }
        }
        val listOfItems = itemRepository.getAllItems()
        itemMap = listOfItems.associateBy { it.itemId }
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
                invoicesMap = listOfInvoices.associateBy { it.invoiceHeadId }
                if (invoicesMap.isNotEmpty()) {
                    fetchInvoiceItems(
                        from,
                        to,
                        invoicesMap.keys.toMutableList()
                    )
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

    private fun fetchInvoiceItems(
            from: Date,
            end: Date,
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
                )
                listOfInvoices.addAll(invoiceRepository.getInvoicesByIds(idsBatch))
                start = to + 1
            }
            invoiceItemMap = listOfInvoices.groupBy { it.invoiceItemId ?: "" }
            val filteredInvoiceItems = if (!SettingsModel.isConnectedToSqlite()) {
                listOfInvoices.filter { from.before(it.invoiceTimeStamp) && end.after(it.invoiceTimeStamp) }
            } else {
                val startTime = from.time
                val endTime = end.time
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

    private fun generateReportsExcel() {
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

    private fun generateFirstSheet(workbook: XSSFWorkbook) {
        val sheet = workbook.createSheet("Inventory & Profit Reports")

        // Obtaining Worksheet's cells collection
        val firstRow = sheet.createRow(0)
        firstRow.createCell(0).setCellValue("Item Name")
        firstRow.createCell(1).setCellValue("Open Qty")
        firstRow.createCell(2).setCellValue("Qty Sold")
        firstRow.createCell(3).setCellValue("Total Cost")
        firstRow.createCell(4).setCellValue("Total Discount")
        firstRow.createCell(5).setCellValue("Total Sales")
        firstRow.createCell(6).setCellValue("Rem.Qty")
        firstRow.createCell(7).setCellValue("Profit")

        val priceWithTax = SettingsModel.currentCompany?.companyUpWithTax ?: false
        itemMap.values.forEachIndexed { index, item ->
            val itemInvoices = invoiceItemMap[item.itemId]
            var quantitiesSold = 0.0
            var totalCost = 0.0
            var totalDisc = 0.0
            var totalSale = 0.0
            itemInvoices?.map {
                totalCost += it.invoiceQuantity.times(it.invoiceCost)
                quantitiesSold += it.invoiceQuantity
                val discAmt = it.getDiscountAmount()
                val itemSale = if (priceWithTax) (it.getAmount() - discAmt) else it.getNetAmount()
                val invDiscount = invoicesMap[it.invoiceHeaderId]?.invoiceHeadDiscount ?: 0.0
                val invDiscountAmount = itemSale.times(invDiscount.times(0.01))
                totalDisc += discAmt + invDiscountAmount
                totalSale += itemSale - invDiscountAmount
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
                    totalDisc
                )
            )
            row.createCell(5).setCellValue(
                String.format(
                    "%,.${currency.currencyName1Dec}f",
                    totalSale
                )
            )
            row.createCell(6).setCellValue(item.itemRemQty)
            row.createCell(7).setCellValue(
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
                val discAmt = it.getDiscountAmount()
                val itemSale = if (priceWithTax) (it.getAmount() - discAmt) else it.getNetAmount()
                val invDiscount = invoicesMap[it.invoiceHeaderId]?.invoiceHeadDiscount ?: 0.0
                val invDiscountAmount = itemSale.times(invDiscount.times(0.01))
                totalSale += itemSale - invDiscountAmount
            }
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(item?.itemName ?: "N/A")
            row.createCell(1).setCellValue(quantitiesSold)
            row.createCell(2).setCellValue(totalSale)
        }
    }

}