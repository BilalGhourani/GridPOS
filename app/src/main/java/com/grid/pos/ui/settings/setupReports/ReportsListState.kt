package com.grid.pos.ui.settings.setupReports

import com.grid.pos.model.FileModel

data class ReportsListState(
        val allReports: MutableList<FileModel> = mutableListOf(),
        val selectedType: String = ReportTypeEnum.PAY_SLIP.key,
        var fileModel: FileModel = FileModel()
)