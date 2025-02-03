package com.grid.pos.ui.settings.setupReports

import com.grid.pos.model.Event
import com.grid.pos.model.FileModel

data class ReportsListState(
        val allReports: MutableList<FileModel> = mutableListOf(),
        val selectedType: String = ReportTypeEnum.PAY_SLIP.key,
        var fileModel: FileModel = FileModel(),
        var isLoading: Boolean = false,
        var isDone: Boolean = false,
        var clear: Boolean = false,
        var warning: Event<String>? = null,
)