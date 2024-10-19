package com.grid.pos.model

import com.grid.pos.ui.settings.setupReports.ReportTypeEnum
import java.io.File

data class FileModel(
        var fileName: String = "",
        var parentCountryName: String= "",
        var parentLanguageName: String= "",
        var reportType: String = ReportTypeEnum.PAY_SLIP.key,
        var selected: Boolean = false,
) {
    fun getFullName(): String {
        return "$parentCountryName/$parentLanguageName/$fileName"
    }

    fun getParents(): String {
        return "$parentCountryName/$parentLanguageName"
    }

    fun getFile(rootFile: File): File {
        return File(
            rootFile,
            getFullName()
        )
    }

    fun isLangSelected():Boolean{
       return parentCountryName == Country.DEFAULT.value && parentLanguageName == SettingsModel.defaultReportLanguage
    }
    fun isBothDefault():Boolean{
       return parentCountryName == Country.DEFAULT.value && parentLanguageName == Language.DEFAULT.value
    }
}

data class ReportResult(
        var found: Boolean,
        var htmlContent: String
)