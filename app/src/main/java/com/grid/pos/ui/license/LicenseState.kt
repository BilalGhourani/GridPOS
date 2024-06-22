package com.grid.pos.ui.license

import com.grid.pos.data.InvoiceHeader.InvoiceHeader
import com.grid.pos.model.Event

data class LicenseState(
        var isDone: Boolean = false,
        var isLoading: Boolean = false,
        val warning: Event<String>? = null,
        val action: String? = null,
)