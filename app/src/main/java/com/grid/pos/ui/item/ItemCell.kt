package com.grid.pos.ui.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.grid.pos.data.Item.Item
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.theme.GridPOSTheme

@Composable
fun ItemCell(
    item: Item,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val image = item.getFullItemImage()
    var itemColor = if (item.itemBtnColor.isNullOrEmpty()) SettingsModel.buttonColor else {
        Color(item.itemBtnColor!!.toColorInt())
    }
    val itemTextColor =
        if (item.itemBtnTextColor.isNullOrEmpty()) SettingsModel.buttonTextColor else {
            Color(item.itemBtnTextColor!!.toColorInt())
        }
    Box(
        modifier = modifier
            .width(120.dp)
            .height(120.dp)
            .padding(2.dp)
            .background(color = Color.Transparent)
            .clip(RoundedCornerShape(15.dp))
    ) {
        if (image.isNotEmpty()) {
            itemColor = itemColor.copy(alpha = .5f)
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(image).build(),
                contentScale = ContentScale.FillBounds, contentDescription = "Item image",
                modifier = Modifier.fillMaxSize()
            )
        }
        Button(modifier = modifier
            .fillMaxSize(),
            colors = ButtonDefaults.buttonColors(
                containerColor = itemColor
            ),
            contentPadding = PaddingValues(0.dp), shape = RoundedCornerShape(15.dp), onClick = {
                onClick.invoke()
            }) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                val price = item.itemUnitPrice.toString()
                Text(
                    text = item.itemName ?: "", color = itemTextColor,
                    style = TextStyle(
                        textDecoration = TextDecoration.None,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ), maxLines = if (SettingsModel.showPriceInItemBtn) 3 else 4,
                    modifier = Modifier.padding(
                        top = 3.dp,
                        start = 3.dp,
                        end = 3.dp,
                        bottom = 5.dp
                    ), textAlign = TextAlign.Center
                )
                if (SettingsModel.showPriceInItemBtn) {
                    Text(
                        text = "$price ${SettingsModel.currentCurrency?.currencyCode1 ?: ""}",
                        color = itemTextColor,
                        style = TextStyle(
                            textDecoration = TextDecoration.None,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ), modifier = Modifier.padding(bottom = 3.dp),
                        textAlign = TextAlign.Center
                    )
                }
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