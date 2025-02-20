package com.grid.pos.ui.pos.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.grid.pos.ui.common.UIButton

@Composable
fun InvoiceHeaderDetails(
        modifier: Modifier = Modifier,
        isPayEnabled: Boolean = false,
        isDeleteEnabled: Boolean = false,
        onAddItem: () -> Unit = {},
        onPay: () -> Unit = {},
        onDelete: () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Absolute.SpaceBetween
    ) {
        UIButton(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(
                    3.dp,
                    5.dp,
                    3.dp,
                    5.dp
                ),
            text = "Add Item",
            textAlign = TextAlign.Center,
            shape = RoundedCornerShape(15.dp)
        ) {
            onAddItem.invoke()
        }

        UIButton(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(
                    3.dp,
                    5.dp,
                    3.dp,
                    5.dp
                ),
            text = "Pay",
            enabled = isPayEnabled,
            textAlign = TextAlign.Center,
            shape = RoundedCornerShape(15.dp)
        ) {
            onPay.invoke()
        }

        if (isDeleteEnabled) {
            UIButton(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(
                        3.dp,
                        5.dp,
                        3.dp,
                        5.dp
                    ),
                text = "Delete",
                textAlign = TextAlign.Center,
                shape = RoundedCornerShape(15.dp)
            ) {
                onDelete.invoke()
            }
        }
    }
}
