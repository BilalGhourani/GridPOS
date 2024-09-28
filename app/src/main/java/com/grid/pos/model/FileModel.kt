package com.grid.pos.model

import java.io.File

data class FileModel(
        var fileName: String = "",
        var parentName: String= "",
        var isPaySlip: Boolean = false,
        var selected: Boolean = false,
) {
    fun getFullName(): String {
        return "$parentName/$fileName"
    }

    fun getFile(rootFile: File): File {
        return File(
            rootFile,
            getFullName()
        )
    }
}

data class ReportResult(
        var found: Boolean,
        var htmlContent: String
)