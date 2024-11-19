package com.grid.pos.model

data class ReportResult(
        var found: Boolean = false,
        var printerName: String = "",
        var printerIP: String? = "",
        var printerPort: Int = 9100,
        var htmlContent: String = ""
)