package com.grid.pos.ui.family

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.grid.pos.data.Family.Family
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.theme.Blue
import com.grid.pos.ui.theme.Grey
import com.grid.pos.ui.theme.GridPOSTheme
import java.io.File
import java.io.FileNotFoundException

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
        var selectedColor = SettingsModel.buttonColor
        var unSelectedColor = Grey
        if (!image.isEmpty()) {
            selectedColor = selectedColor.copy(alpha = .5f)
            unSelectedColor = unSelectedColor.copy(alpha = .5f)
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(image).build(),
                contentScale = ContentScale.FillBounds, contentDescription = "Item image",
                modifier = Modifier.fillMaxSize()
            )
        }
        UIButton(
            modifier = modifier.fillMaxSize(), buttonColor = if (selected) {
                selectedColor
            } else {
                unSelectedColor
            }, text = family.familyName ?: "N/A"
        ) {
            onClick.invoke()
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