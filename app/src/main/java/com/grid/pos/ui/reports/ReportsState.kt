package com.grid.pos.ui.reports

import com.grid.pos.model.Event

data class ReportsState(
        var isDone: Boolean = false,
        var isLoading: Boolean = false,
        var warning: Event<String>? = null,
)