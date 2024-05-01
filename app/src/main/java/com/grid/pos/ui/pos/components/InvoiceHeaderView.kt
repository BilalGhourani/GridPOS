package com.grid.pos.ui.pos.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.grid.pos.ui.common.SearchableDropdownMenu
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.theme.Blue
import com.grid.pos.utils.Utils

@Composable
fun InvoiceHeaderDetails(
    modifier: Modifier = Modifier,
    onEdit: () -> Unit = {},
    onPay: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Absolute.Right
    ) {
        UIButton(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(3.dp, 5.dp, 3.dp, 5.dp),
            text = "Edit",
            textAlign = TextAlign.Center,
            shape = RoundedCornerShape(
                topStart = 15.dp,
                topEnd = 15.dp,
                bottomEnd = 15.dp,
                bottomStart = 15.dp
            )
        ) {
            onEdit.invoke()
        }

        UIButton(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(3.dp, 5.dp, 3.dp, 5.dp),
            text = "Pay",
            textAlign = TextAlign.Center,
            shape = RoundedCornerShape(
                topStart = 15.dp,
                topEnd = 15.dp,
                bottomEnd = 15.dp,
                bottomStart = 15.dp
            )
        ) {
            onPay.invoke()
        }
    }
}
