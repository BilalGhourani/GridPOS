package com.grid.pos.ui.pos.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grid.pos.data.Item.Item
import com.grid.pos.data.ThirdParty.ThirdParty
import com.grid.pos.ui.common.SearchableDropdownMenu
import com.grid.pos.ui.theme.GridPOSTheme

@Composable
fun InvoiceFooterView(
    items: MutableList<Item> = mutableListOf(),
    thirdParties: MutableList<ThirdParty> = mutableListOf(),
    modifier: Modifier = Modifier,
    onItemSelected: (Item) -> Unit = {},
    onThirdPartySelected: (ThirdParty) -> Unit = {},
) {
    Row(
        modifier = modifier.wrapContentWidth()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .wrapContentHeight()
                .padding(5.dp)
        ) {
            Row(
                modifier = Modifier.wrapContentWidth(),
                horizontalArrangement = Arrangement.Absolute.Left
            ) {
                Text(text = "Tax:")
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "0")
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "USD")
            }

            Row(
                modifier = Modifier.wrapContentWidth(),
                horizontalArrangement = Arrangement.Absolute.Left
            ) {
                Text(text = "Tax2:")
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "0")
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "USD")
            }

            Row(
                modifier = Modifier.wrapContentWidth(),
                horizontalArrangement = Arrangement.Absolute.Left
            ) {
                Text(text = "Total:")
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "150.00")
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "USD")
            }

            Row(
                modifier = Modifier.wrapContentWidth(),
                horizontalArrangement = Arrangement.Absolute.Left
            ) {
                Text(text = "Total:")
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "15,000.00")
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "USD")
            }

            SearchableDropdownMenu(
                items = items.toMutableList(),
                modifier = Modifier
                    .padding(10.dp, 15.dp, 10.dp, 5.dp),
                label = "Search Items",
            ) { item ->
                onItemSelected.invoke(item as Item)
            }

        }
        Column(
            modifier = Modifier
                .weight(1f)
                .wrapContentHeight()
                .padding(5.dp)
        ) {
            Row(
                modifier = Modifier.wrapContentWidth(),
                horizontalArrangement = Arrangement.Absolute.Left
            ) {
                Text(text = "Tax2:")
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "0")
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "USD")
            }

            Row(
                modifier = Modifier.wrapContentWidth(),
                horizontalArrangement = Arrangement.Absolute.Left
            ) {
                Text(text = "Total Tax:")
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "0")
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "USD")
            }

            Row(
                modifier = Modifier.wrapContentWidth(),
                horizontalArrangement = Arrangement.Absolute.Left
            ) {
                Text(text = "Table Number:")
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "3")
            }

            Row(
                modifier = Modifier.wrapContentWidth(),
                horizontalArrangement = Arrangement.Absolute.Left
            ) {
                Text(text = "Client:")
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "Cash")
            }

            SearchableDropdownMenu(
                items = thirdParties.toMutableList(),
                modifier = Modifier
                    .padding(10.dp, 15.dp, 10.dp, 5.dp),
                label = "Customer Search",
            ) { thirdParty ->
                onThirdPartySelected.invoke(thirdParty as ThirdParty)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InvoiceFooterViewPreview() {
    GridPOSTheme {
        InvoiceFooterView()
    }
}
