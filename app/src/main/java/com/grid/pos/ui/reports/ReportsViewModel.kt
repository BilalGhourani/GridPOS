package com.grid.pos.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grid.pos.App
import com.grid.pos.data.Currency.Currency
import com.grid.pos.data.Family.FamilyRepository
import com.grid.pos.data.Invoice.Invoice
import com.grid.pos.data.Invoice.InvoiceRepository
import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.data.InvoiceHeader.InvoiceHeaderRepository
import com.grid.pos.data.Item.Item
import com.grid.pos.data.Item.ItemRepository
import com.grid.pos.data.PosReceipt.PosReceiptRepository
import com.grid.pos.data.ThirdParty.ThirdPartyRepository
import com.grid.pos.interfaces.OnResult
import com.grid.pos.model.Event
import com.grid.pos.model.SettingsModel
import com.grid.pos.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.apache.poi.xssf.usermodel.XSSFWorkbook
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
    private var invoiceMap: Map<String, List<Invoice>> = mutableMapOf()
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
            end: Date
    ) {
        reportsState.value = reportsState.value.copy(
            isLoading = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            invoiceRepository.getInvoicesBetween(from,
                end,
                object : OnResult {
                    override fun onSuccess(result: Any) {
                        val listOfInvoices = mutableListOf<Invoice>()
                        (result as List<*>).forEach {
                            listOfInvoices.add(it as Invoice)
                        }
                        invoiceMap = listOfInvoices.groupBy { it.invoiceItemId ?: "" }
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
            val workbook = XSSFWorkbook()

            generateFirstSheet(workbook)
            generateSecondSheet(workbook)

            // Write the workbook to a file
            val outputStream = FileOutputStream(
                getReportFile()
            )
            workbook.write(outputStream)
            outputStream.close()
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
            "grids_report_excel"
        )
        if (child.exists()) {
            child.delete()
            child.createNewFile()
        }
        return child
    }

    private fun generateFirstSheet(workbook: XSSFWorkbook) {
        val sheet = workbook.createSheet("Inventory & Profit Reports")

        // Write headers (assuming first row in data list is header)
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Item Name")
        headerRow.createCell(1).setCellValue("Open Qty")
        headerRow.createCell(2).setCellValue("Qty Sold")
        headerRow.createCell(3).setCellValue("Total Cost")
        headerRow.createCell(4).setCellValue("Total Sales")
        headerRow.createCell(5).setCellValue("Rem.Qty")
        headerRow.createCell(6).setCellValue("Profit")


        itemMap.values.forEachIndexed { index, item ->
            val dataRow = sheet.createRow(index + 1)
            val itemInvoices = invoiceMap[item.itemId]
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
            dataRow.createCell(0).setCellValue(item.itemName)
            dataRow.createCell(1).setCellValue(item.itemOpenQty)
            dataRow.createCell(2).setCellValue(quantitiesSold)
            dataRow.createCell(3).setCellValue(
                String.format(
                    "%.${currency.currencyName1Dec}f",
                    totalCost
                )
            )
            dataRow.createCell(4).setCellValue(
                String.format(
                    "%.${currency.currencyName1Dec}f",
                    totalSale
                )
            )
            dataRow.createCell(5).setCellValue(remQty)
            dataRow.createCell(6).setCellValue(
                String.format(
                    "%.${currency.currencyName1Dec}f",
                    totalSale - totalCost
                )
            )
        }
    }

    private fun generateSecondSheet(workbook: XSSFWorkbook) {
        val sheet = workbook.createSheet("Sales Reports")

        // Write headers (assuming first row in data list is header)
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Name")
        headerRow.createCell(1).setCellValue("Qty Sold")
        headerRow.createCell(1).setCellValue("Total")

        itemMap.values.forEachIndexed { index, item ->
            val dataRow = sheet.createRow(index + 1)
            val itemInvoices = invoiceMap[item.itemId]
            var quantitiesSold = 0.0
            var totalSale = 0.0
            itemInvoices?.map {
                quantitiesSold += it.invoiceQuantity
                totalSale += it.getNetAmount()
            }
            dataRow.createCell(0).setCellValue(item.itemName)
            dataRow.createCell(1).setCellValue(quantitiesSold)
            dataRow.createCell(1).setCellValue(totalSale)
        }
    }

}