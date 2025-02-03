package com.grid.pos.ui.settings.setupReports

import com.grid.pos.model.Country
import com.grid.pos.model.Event
import com.grid.pos.model.FileModel
import com.grid.pos.model.Language
import com.grid.pos.model.ReportCountry

data class SetupReportState(
    var countries: MutableList<ReportCountry> = mutableListOf(),
    var country: String = Country.DEFAULT.value,
    var language: Language = Language.DEFAULT,
    var isLoading: Boolean = false,
    var isDone: Boolean = false,
    var clear: Boolean = false,
    var warning: Event<String>? = null,
    var actionLabel: String? = null,
)