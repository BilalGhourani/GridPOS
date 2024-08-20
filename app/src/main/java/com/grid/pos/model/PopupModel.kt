package com.grid.pos.model

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class PopupModel(
    var onDismissRequest: () -> Unit = {},
    var onConfirmation: () -> Unit= {},
    var dialogTitle: String="",
    var dialogText: String="",
    var positiveBtnText: String = "OK",
    var negativeBtnText: String? = "CANCEL",
    var icon: ImageVector?=null,
    var height: Dp = 200.dp
)