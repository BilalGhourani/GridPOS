package com.grid.pos.ui.license

import com.grid.pos.model.Event

data class LicenseState(
        var filePath: String? = null,
        var isDone: Boolean = false,
        var isLoading: Boolean = false,
        val warning: Event<String>? = null,
        val action: String? = null,
)