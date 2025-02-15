package com.grid.pos.ui.settings.setupReports

import com.grid.pos.model.Country
import com.grid.pos.model.Language
import com.grid.pos.model.ReportCountry

data class SetupReportState(
    var countries: MutableList<ReportCountry> = mutableListOf(),
    var country: String = Country.DEFAULT.value,
    var language: Language = Language.DEFAULT,
)