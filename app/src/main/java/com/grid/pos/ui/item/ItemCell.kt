package com.grid.pos.ui.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.grid.pos.data.Item.Item
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.theme.GridPOSTheme

@Composable
fun ItemCell(
        item: Item, modifier: Modifier = Modifier, onClick: () -> Unit = {}
) {
    ElevatedButton(modifier = modifier
        .width(120.dp)
        .height(120.dp)
        .padding(2.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = item.itemBtnColor?.let { Color(it.toColorInt()) }.run { Color.Red }),
        shape = RoundedCornerShape(15.dp), onClick = {
            onClick.invoke()
        }) {
        Column(
            modifier = Modifier.wrapContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val price = item.itemUnitPrice.toString()
            Text(
                text = item.itemName ?: "", color = SettingsModel.textColor,
                modifier = Modifier.padding(5.dp), textAlign = TextAlign.Center
            )
            if (SettingsModel.showPriceInItemBtn) {
                Text(
                    text = "$price US", color = SettingsModel.textColor,
                    modifier = Modifier.padding(5.dp), textAlign = TextAlign.Center
                )

                Text(
                    text = "DOLLAR", color = SettingsModel.textColor,
                    modifier = Modifier.padding(5.dp), textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ItemCellPreview() {
    GridPOSTheme {
        ItemCell(Item("1", "CHICKEN", itemUnitPrice = 100.0))
    }
}