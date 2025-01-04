package com.grid.pos.ui.common

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grid.pos.model.SettingsModel
import com.grid.pos.model.ToastModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CustomSnackBar(
    show: Boolean,
    toastModel: ToastModel,
    onDismiss: () -> Unit,
) {
    // Local state to control visibility
    val scope = rememberCoroutineScope()
    // Automatically dismiss after 3 seconds
    LaunchedEffect(show) {
        if (show) {
            scope.launch(Dispatchers.Default) {
                delay(toastModel.timeout)  // Wait for 3 seconds
                withContext(Dispatchers.Main) {
                    onDismiss.invoke()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(
                40.dp,
                120.dp
            )
            .padding(16.dp), // Occupies the full screen
        contentAlignment = Alignment.BottomCenter // Aligns content at the bottom center
    ) {
        AnimatedVisibility(visible = show,
                           enter = slideInVertically { it } + fadeIn(),
                           exit = slideOutVertically { it } + fadeOut()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.Black,
                        shape = RoundedCornerShape(15.dp)
                    )
                    .padding(16.dp), // Inner padding
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = toastModel.message,
                    color = Color.White,
                    style = TextStyle(fontSize = 16.sp)
                )
                if (!toastModel.actionButton.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            toastModel.onActionClick.invoke()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = toastModel.actionButton!!,
                            color = Color.Black,
                            style = TextStyle(fontSize = 16.sp)
                        )
                    }
                }
            }
        }
    }
}
