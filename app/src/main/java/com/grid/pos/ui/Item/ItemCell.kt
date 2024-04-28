package com.grid.pos.ui.Item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grid.pos.data.Item.Item
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.ui.theme.Red

@Composable
fun ItemCell(item: Item, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(120.dp)
            .height(120.dp)
            .padding(2.dp)
            .background(color = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Red, shape = RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column( // Use a Box to achieve content alignment
                modifier = Modifier
                    .wrapContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally // Center content within the Box
            ) {
                val price = item.itemUnitPrice.toString() ?: "0.0"
                Text(
                    text = item.itemName ?: "",
                    color = Color.White,
                    modifier = Modifier
                        .padding(5.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "$price US",
                    color = Color.White,
                    modifier = Modifier
                        .padding(5.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "DOLLAR",
                    color = Color.White,
                    modifier = Modifier
                        .padding(5.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ItemCellPreview() {
    GridPOSTheme {
        ItemCell(Item("1", "CHICKEN", itemUnitPrice = "100.0"))
    }
}