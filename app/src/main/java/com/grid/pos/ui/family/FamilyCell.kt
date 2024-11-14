package com.grid.pos.ui.family

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.grid.pos.data.Family.Family
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.theme.Grey
import com.grid.pos.ui.theme.GridPOSTheme

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun CategoryCell(
        family: Family,
        selected: Boolean,
        modifier: Modifier = Modifier,
        onClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier
            .width(120.dp)
            .height(80.dp)
            .padding(end = 5.dp)
            .background(color = Color.Transparent)
            .clip(RoundedCornerShape(15.dp)),
        shadowElevation = 10.dp
    ) {
        val image = family.getFullFamilyImage()
        var selectedColor = Color.Blue
        var unSelectedColor = Grey
        if (!image.isEmpty()) {
            selectedColor = Color.White.copy(alpha = .5f)
            unSelectedColor = Color.DarkGray.copy(alpha = .6f)
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(image).build(),
                contentScale = ContentScale.FillBounds, contentDescription = "Item image",
                modifier = Modifier.fillMaxSize()
            )
        }
        Button(
            modifier = modifier.fillMaxSize(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selected) {
                    selectedColor
                } else {
                    unSelectedColor
                },
            ),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(15.dp),
            onClick = {
                onClick.invoke()
            }
        ) {
            Text(
                text = family.familyName ?: "N/A",
                textAlign = TextAlign.Center,
                style = TextStyle(
                    textDecoration = TextDecoration.None,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = Color.White
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun CategoryCellPreview() {
    GridPOSTheme {
        CategoryCell(Family("1", "Bilal", "1"), true)
    }
}