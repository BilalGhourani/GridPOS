package com.grid.pos.ui.reports

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
import com.grid.pos.interfaces.OnResult
import com.grid.pos.model.Event
import com.grid.pos.model.SettingsModel
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
    private val _reportsState = MutableStateFlow(ReportsState())
    val reportsState: MutableStateFlow<ReportsState> = _reportsState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            fetchItems()
        }
    }

    private suspend fun fetchItems() {
        itemRepository.getAllItems(object : OnResult {
            override fun onSuccess(result: Any) {
                val listOfItems = mutableListOf<Item>()
                (result as List<*>).forEach {
                    listOfItems.add(it as Item)
                }
                itemMap = listOfItems.map { it.itemId to it }.toMap()
            }

            override fun onFailure(
                    message: String,
                    errorCode: Int
            ) {

            }

        })
    }

    fun fetchInvoices(
            from: Date,
            to: Date
    ) {
        reportsState.value = reportsState.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            invoiceHeaderRepository.getAllInvoiceHeaders(
                object : OnResult {
                    override fun onSuccess(result: Any) {
                        val listOfInvoices = mutableListOf<InvoiceHeader>()
                        (result as List<*>).forEach {
                            listOfInvoices.add(it as InvoiceHeader)
                        }
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

                    override fun onFailure(
                            message: String,
                            errorCode: Int
                    ) {

                    }

                })
        }
    }

    fun fetchInvoiceItems(
            from: Date,
            to: Date,
            ids: List<String>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            invoiceRepository.getInvoicesByIds(ids,
                object : OnResult {
                    override fun onSuccess(result: Any) {
                        val listOfInvoices = mutableListOf<Invoice>()
                        (result as List<*>).forEach {
                            listOfInvoices.add(it as Invoice)
                        }
                        invoiceItemMap = listOfInvoices.groupBy { it.invoiceItemId ?: "" }
                        val filteredInvoiceItems = listOfInvoices.filter { from.before(it.invoiceTimeStamp) && to.after(it.invoiceTimeStamp) }
                        filteredInvoiceItemMap = filteredInvoiceItems.groupBy { it.invoiceItemId ?: "" }
                        generateReportsExcel()
                    }

                    override fun onFailure(
                            message: String,
                            errorCode: Int
                    ) {

                    }

                })
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
            val file = getReportFile()
            val workbook = Workbook()

            generateFirstSheet(workbook)
            generateSecondSheet(workbook)

            // Write the workbook to a file
            val outputStream = FileOutputStream(file)
            workbook.save(
                outputStream,
                FileFormatType.XLSX
            )
            withContext(Dispatchers.Main) {
                reportsState.value = reportsState.value.copy(
                    filePath = file.path,
                    isLoading = false
                )
            }
        }
    }

    private fun getReportFile(): File {
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
            "grids_report_excel.xlsx"
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

        var rowIndex = 1
        // Obtaining Worksheet's cells collection
        val cells = sheet.cells
        cells.get("A$rowIndex").value = "Item Name"
        cells.get("B$rowIndex").value = "Open Qty"
        cells.get("C$rowIndex").value = "Qty Sold"
        cells.get("D$rowIndex").value = "Total Cost"
        cells.get("E$rowIndex").value = "Total Sales"
        cells.get("F$rowIndex").value = "Rem.Qty"
        cells.get("G$rowIndex").value = "Profit"


        itemMap.values.forEach { item ->
            rowIndex++
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
            cells.get("A$rowIndex").value = item.itemName
            cells.get("B$rowIndex").value = item.itemOpenQty
            cells.get("C$rowIndex").value = quantitiesSold
            cells.get("D$rowIndex").value = String.format(
                "%.${currency.currencyName1Dec}f",
                totalCost
            )

            cells.get("E$rowIndex").value = String.format(
                "%.${currency.currencyName1Dec}f",
                totalSale
            )

            cells.get("F$rowIndex").value = remQty
            cells.get("G$rowIndex").value = String.format(
                "%.${currency.currencyName1Dec}f",
                totalSale - totalCost
            )
        }
    }

    private fun generateSecondSheet(workbook: Workbook) {
        val sheet = workbook.worksheets.add("Sales Reports")

        var rowIndex = 1
        // Obtaining Worksheet's cells collection
        val cells = sheet.cells
        cells.get("A$rowIndex").value = "Name"
        cells.get("B$rowIndex").value = "Qty Sold"
        cells.get("C$rowIndex").value = "Total"

        filteredInvoiceItemMap.keys.forEach { itemId ->
            rowIndex++
            val item = itemMap[itemId]
            var quantitiesSold = 0.0
            var totalSale = 0.0
            filteredInvoiceItemMap[itemId]?.map {
                quantitiesSold += it.invoiceQuantity
                totalSale += it.getNetAmount()
            }
            cells.get("A$rowIndex").value = item?.itemName ?: "N/A"
            cells.get("B$rowIndex").value = quantitiesSold
            cells.get("C$rowIndex").value = totalSale
        }
    }

}