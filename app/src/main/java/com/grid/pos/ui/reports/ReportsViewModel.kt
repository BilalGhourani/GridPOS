package com.grid.pos.ui.reports

import androidx.lifecycle.ViewModel
import com.grid.pos.data.InvoiceHeader.InvoiceHeaderRepository
import com.grid.pos.model.Event
import com.grid.pos.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.apache.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val invoiceHeaderRepository: InvoiceHeaderRepository
) : ViewModel() {

    private val _reportsState = MutableStateFlow(ReportsState())
    val reportsState: MutableStateFlow<ReportsState> = _reportsState

    fun showError(message: String) {
        reportsState.value = reportsState.value.copy(
            warning = Event(message),
            isLoading = false
        )
    }

    fun generateReportsExcel() {
        val workbook = XSSFWorkbook()

        generateFirstSheet(workbook)
        generateSecondSheet(workbook)

        // Write the workbook to a file
        val outputStream =
            FileOutputStream(File("excel_" + Utils.getDateinFormat(Date(), "yyyyMMddhhmmss")))
        workbook.write(outputStream)
        outputStream.close()
    }

    private fun generateFirstSheet(workbook: XSSFWorkbook) {
        val sheet = workbook.createSheet("Sheet1")

        // Write headers (assuming first row in data list is header)
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Name")
        headerRow.createCell(1).setCellValue("Qty")
        headerRow.createCell(1).setCellValue("Amount")

        reportsState.value.invoiceHeaders.forEachIndexed { index, invoiceHeader ->
            val dataRow = sheet.createRow(index + 1)
            dataRow.createCell(0).setCellValue("Name")
            dataRow.createCell(1).setCellValue("Qty")
            dataRow.createCell(1).setCellValue("Amount")
        }
    }

    private fun generateSecondSheet(workbook: XSSFWorkbook) {
        val sheet = workbook.createSheet("Sheet2")

        // Write headers (assuming first row in data list is header)
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Name")
        headerRow.createCell(1).setCellValue("Qty")
        headerRow.createCell(1).setCellValue("Amount")

        reportsState.value.invoiceHeaders.forEachIndexed { index, invoiceHeader ->
            val dataRow = sheet.createRow(index + 1)
            dataRow.createCell(0).setCellValue("Name")
            dataRow.createCell(1).setCellValue("Qty")
            dataRow.createCell(1).setCellValue("Amount")
        }
    }

}