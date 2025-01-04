package com.grid.pos.model

data class ToastModel(
        var message: String = "",
        var actionButton: String? = null,
        var onActionClick: () -> Unit = {},
        val onDismiss: () -> Unit = {},
        var timeout: Long = 3000L
)
