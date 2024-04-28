package com.grid.pos.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoadingIndicator(
    label: String = "Loading...",
    show: Boolean = true
) {
    AnimatedVisibility(
        visible = show,
        enter = fadeIn(),
        exit = fadeOut()
    ) {

        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = .6f))
        ) {
            Card(
                Modifier
                    .size(100.dp, 100.dp)
                    .align(Alignment.Center)
                    .background(color = Color.Transparent, shape = RoundedCornerShape(8.dp))
                    .shadow(8.dp)
            ) {
                CircularProgressIndicator(
                    Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    strokeWidth = 3.dp
                )
            }
        }
    }
}