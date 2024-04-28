package com.grid.pos.ui.common

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import com.grid.pos.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UIBottomSheet(
    composable: @Composable() () -> Unit,
    isBottomSheetVisible: Boolean,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    if (isBottomSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = Color.Transparent,
            contentColor = White,
            shape = RectangleShape,
            dragHandle = null,
            scrimColor = Color.Black.copy(alpha = .5f),
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            composable
        }
    }
}