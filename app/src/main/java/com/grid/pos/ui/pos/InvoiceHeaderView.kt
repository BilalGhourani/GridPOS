package com.grid.pos.ui.pos

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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.grid.pos.ui.common.SearchableDropdownMenu
import com.grid.pos.ui.theme.Blue
import com.grid.pos.utils.Utils

@Composable
fun InvoiceHeaderDetails(
    navController: NavController? = null,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit = {},
    onAddCustomer: () -> Unit = {},
    onPay: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        horizontalArrangement = Arrangement.Absolute.Right
    ) {
        ElevatedButton(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(3.dp, 5.dp, 5.dp, 5.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue),
            onClick = { onEdit.invoke() }
        ) {
            Text("Edit")
        }

        ElevatedButton(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(3.dp, 5.dp, 5.dp, 5.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue),
            onClick = { onAddCustomer.invoke() }
        ) {
            Text("Add Customer")
        }

        ElevatedButton(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(3.dp, 5.dp, 5.dp, 5.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue),
            onClick = { onPay.invoke() }
        ) {
            Text("Pay")
        }

        ElevatedButton(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(3.dp, 5.dp, 5.dp, 5.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue),
            onClick = { navController?.navigateUp() }
        ) {
            Text("Back")
        }
    }
}
