package com.grid.pos.ui.settings.setupReports

import com.grid.pos.model.Event
import com.grid.pos.model.FileModel

data class ReportsListState(
        val allReports: MutableList<FileModel> = mutableListOf(),
        var isLoading: Boolean = false,
        var isDone: Boolean = false,
        var clear: Boolean = false,
        var warning: Event<String>? = null,
) {
    fun getFileModels(type: String): List<FileModel> {
       return allReports.filter { it.reportType == type }
    }
}